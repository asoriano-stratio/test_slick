package utils

import java.io.InputStream

import org.apache.spark.ml.linalg.{Matrices, Matrix, Vector, Vectors}
import org.apache.spark.ml.param._
import org.apache.spark.ml.{Pipeline, PipelineStage}
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.read

import scala.io.Source
import scala.reflect.runtime.universe._
import scala.util.Try


/**
  * Pipeline Json/Object descriptors
  */
case class PipelineStageDescriptorDeser(
                                           name: String,
                                           uid: String,
                                           parameters: Seq[PipelineStageParamDeser]
                                       )

case class PipelineStageParamDeser(
                                      name: String,
                                      value: String,
                                      paramType: Option[String] = None
                                  )

/**
  * Object with helper functions to serialize/deserialize
  */
object SparkMlSerializationUtils {

    implicit val formats: DefaultFormats.type = DefaultFormats

    /** Map< pipeline stage name, class which implements it> */
    lazy val stageNameToClassMapper: Try[Map[String, String]] = Try {
        val stream: InputStream = getClass.getResourceAsStream("/mlpipeline-classes-mapper.json")
        val json: String = Source.fromInputStream(stream).mkString
        read[Map[String, String]](json)
    }

    /**
      * Encodes a pipeline stage's parameter as a string to include into a json descriptor
      */
    def encodeParamValue(params: Params, param: Param[_]): Try[(String, Option[String])] = Try {

        val x = params.get(param).get
        param match {
            case _: StringArrayParam | _: DoubleArrayParam | _: IntArrayParam =>
                val value = x.asInstanceOf[Array[_]]
                (if (value.length == 0) "" else value.mkString(","), None)
            case _: Param[_] if x.isInstanceOf[Matrix] =>
                val value: Matrix = x.asInstanceOf[Matrix]
                (if (value.numCols == 0) "" else value.rowIter.map(_.toArray.mkString(",")).mkString(";"), Some("matrix"))
            case _: Param[_] if x.isInstanceOf[Vector] =>
                val value = x.asInstanceOf[Vector]
                (if (value.size == 0) "" else value.toArray.mkString(","), Some("vector"))
            case _: Param[_] if x.isInstanceOf[String] =>
                val value = x.asInstanceOf[String]
                (value, Some("String"))
            case _ => (x.toString, None)
        }
    }

    /**
      * Decodes a pipeline stage's parameter in string format (from a json descriptor) and gets its real format value
      */
    def decodeParamValue[A: TypeTag](param: Param[A], value: String, paramType: Option[String] = None): Try[Any] = Try {
        val simpleParam: Option[Any] = param match {
            case _: BooleanParam => Some(value.toBoolean)
            case _: LongParam => Some(value.toLong)
            case _: DoubleParam => Some(value.toDouble)
            case _: IntParam => Some(value.toInt)
            case _: FloatParam => Some(value.toFloat)
            case _: StringArrayParam => Some(value.split(",").map(_.trim))
            case _: DoubleArrayParam => Some(value.split(",").map(_.trim.toDouble))
            case _: IntArrayParam => Some(value.split(",").map(_.trim.toInt))
            case _ => None
        }
        if (simpleParam.isEmpty) {
            paramType match {
                case Some("string") => value
                case Some("matrix") =>
                    val rows: Array[String] = value.split(";")
                    val nCols = if (rows.length == 0) 0 else rows.head.split(",").length
                    val data: Array[Double] = rows.flatMap(r => r.split(",").map(_.toDouble))
                    Matrices.dense(rows.length, nCols, data)
                case Some("vector") =>
                    Vectors.dense(value.split(",").map(_.trim.toDouble))
                case None => value
            }
        } else {
            simpleParam.get
        }
    }

    /**
      *  Constructs a pipeline from a Json descriptor (deserialized into case classes)
      */
    def getPipelineFromDescriptor(pipelineDescriptor: Array[PipelineStageDescriptorDeser]): Pipeline = {

        val stages: Array[PipelineStage] = for (stageDescriptor <- pipelineDescriptor) yield {

            // · Instantiate class
            val stage = Class.forName(
                stageNameToClassMapper.get(stageDescriptor.name)
            ).getConstructor(classOf[String]).newInstance(stageDescriptor.uid)

            // · Set parameters
            stageDescriptor.parameters.foreach(paramDescriptor => {
                val paramToSet: Param[Any] = stage.asInstanceOf[Params].getParam(paramDescriptor.name)
                val valueToSet = decodeParamValue(paramToSet, paramDescriptor.value)

                stage.asInstanceOf[Params].set(paramToSet, valueToSet.get)
            })

            stage.asInstanceOf[PipelineStage]
        }

        new Pipeline().setStages(stages)
    }

    /**
      *  Constructs Json descriptor (it representation with case classes) from a pipeline
      */
    def getDescriptorFromPipeline(pipeline: Pipeline): Array[PipelineStageDescriptorDeser] = {

        pipeline.getStages.map(e => {
            PipelineStageDescriptorDeser(
                name = e.getClass.getSimpleName,
                uid = e.uid,
                parameters = e.asInstanceOf[Params].params.flatMap(p => Try {
                    val (value, paramType) = encodeParamValue(e.asInstanceOf[Params], p).get
                    PipelineStageParamDeser(name = p.name, value = value, paramType = paramType)
                }.toOption).toSeq
            )
        })
    }
}