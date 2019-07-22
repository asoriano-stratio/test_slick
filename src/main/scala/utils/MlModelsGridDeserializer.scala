package utils

import java.io.InputStream
import java.util.concurrent.atomic.AtomicInteger

import com.salesforce.op.stages.impl.selector.ModelSelectorNames.EstimatorType
import data.model.configuration.session.modelselection.{BinaryClassMlModelsTypes, MultiClassMlModelsTypes, RegressionMlModelsTypes}
import org.apache.log4j.Logger
import org.apache.spark.ml.param.{ParamMap, Params}
import org.apache.spark.ml.tuning.ParamGridBuilder
import org.apache.spark.ml.util.Identifiable
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.read

import scala.io.Source
import scala.util.{Failure, Success, Try}

/**
  *  Case classes to deserialize mlModels hyper-parameters grid
  */
case class ModelsAndHyperparamGridsDescriptor(
                                                 modelsAndParameters: Seq[MlModelAndGridDescriptor]
                                             )
case class MlModelAndGridDescriptor(
                                       mlModelName: String,
                                       hyperparameterGrid: Seq[HyperparameterValuesDescriptor]
                                   )

case class HyperparameterValuesDescriptor(
                                             name: String,
                                             values: Seq[String],
                                             paramType:Option[String] = None
                                         )


/**
  * Unique Identifier (UID) generator
  */
case object DeserModelsUID {

    def fromString(uid: String): (String, String) = {
        try { uid.split("_") match { case Array(prefix, suffix) => prefix -> suffix } }
        catch {
            case _: Exception => throw new IllegalArgumentException(s"Invalid UID: $uid")
        }
    }

    /**
      * Resets the UID counter back to specified count.
      * Can be useful when generating workflows programmatically, but the UIDs needs to be the same.
      *
      * @param v reset count to value v (default: 0)
      * NOTE: Don't use this method unless you know what you are doing.
      */
    def reset(v: Int = 0): this.type = {
        counter.set(v)
        this
    }

    /**
      * Gets current UID count
      *
      * @return UID counter value
      */
    def count(): Int = counter.get()

    private val counter = new AtomicInteger(0)

    def makeUID(prefix: String, isSequential: Boolean): String = {
        if (isSequential) {
            val id = counter.incrementAndGet()
            String.format(s"${prefix}_%12s", Integer.toHexString(id)).replace(" ", "0")
        } else {
            Identifiable.randomUID(prefix = prefix)
        }
    }

}


object MlModelsGridDeserializer {

    private val classificationPackage = "com.salesforce.op.stages.impl.classification"
    private val regressionPackage = "com.salesforce.op.stages.impl.regression"
    val log: Logger = Logger.getLogger(getClass.getName)

    // Note: Binary and multiclass shares some implementations...
    val nameToClassMapper: Map[String, String] = Map(
        // Regression
        RegressionMlModelsTypes.LinearRegression -> s"$regressionPackage.OpLinearRegression",
        RegressionMlModelsTypes.RandomForestRegressor -> s"$regressionPackage.OpRandomForestRegressor",
        RegressionMlModelsTypes.GBTRegressor -> s"$regressionPackage.OpGBTRegressor",
        RegressionMlModelsTypes.DecisionTreeRegressor -> s"$regressionPackage.OpDecisionTreeRegressor",
        RegressionMlModelsTypes.GeneralizedLinearRegression -> s"$regressionPackage.OpGeneralizedLinearRegression",
        // Binary classification
        BinaryClassMlModelsTypes.LogisticRegression -> s"$classificationPackage.OpLogisticRegression",
        BinaryClassMlModelsTypes.RandomForestClassifier -> s"$classificationPackage.OpRandomForestClassifier",
        BinaryClassMlModelsTypes.GBTClassifier -> s"$classificationPackage.OpGBTClassifier",
        BinaryClassMlModelsTypes.LinearSVC -> s"$classificationPackage.OpLinearSVC",
        BinaryClassMlModelsTypes.NaiveBayes -> s"$classificationPackage.OpNaiveBayes",
        BinaryClassMlModelsTypes.DecisionTreeClassifier -> s"$classificationPackage.OpDecisionTreeClassifier",
        // Multi-class classification
        MultiClassMlModelsTypes.LogisticRegression -> s"$classificationPackage.OpLogisticRegression",
        MultiClassMlModelsTypes.RandomForestClassifier -> s"$classificationPackage.OpRandomForestClassifier",
        MultiClassMlModelsTypes.NaiveBayes -> s"$classificationPackage.OpNaiveBayes",
        MultiClassMlModelsTypes.DecisionTreeClassifier -> s"$classificationPackage.OpDecisionTreeClassifier"
    )

    val classToNameMapper:Map[String, String] = nameToClassMapper.map(_.swap)

    def getInstance(mlModelName: String): EstimatorType = {
        // · Instantiate class
        val stage: EstimatorType = Class.forName(
            nameToClassMapper(mlModelName)
        ).getConstructor(classOf[String]).newInstance(DeserModelsUID.makeUID(mlModelName, true)).asInstanceOf[EstimatorType]
        stage
    }

    def getModelsAndParameters(gridDescriptor:ModelsAndHyperparamGridsDescriptor): Seq[(EstimatorType, Array[ParamMap])] = {
        getModelsAndParameters(gridDescriptor.modelsAndParameters)
    }

    def getModelsAndParameters(modelsAndParameters:Seq[MlModelAndGridDescriptor]): Seq[(EstimatorType, Array[ParamMap])] =  {
        val hyperParamGrid = for(modelAndParameters <- modelsAndParameters) yield {
            Try {
                // · Instantiate model object using its name
                log.debug(s"- Instantiating mlmodel: ${modelAndParameters.mlModelName}")
                val modelInstance = Try{
                    MlModelsGridDeserializer.getInstance(modelAndParameters.mlModelName)
                }.getOrElse(throw new Exception(s"Error instantiating ml model '${modelAndParameters.mlModelName}'"))

                // · Create a ParamGridBuilder
                val gridBuilder = new ParamGridBuilder()

                log.debug(s"- Setting hyperparameters grid")
                val settedParams = modelAndParameters.hyperparameterGrid.map(param => Try {
                    // · Getting parameter from model object using its name
                    log.debug(s"· Getting hyperparameter ${param.name}")
                    val paramToSet = Try{
                        modelInstance.asInstanceOf[Params].getParam(param.name)
                    }.getOrElse(
                        throw new Exception(
                            s"Ml model ${modelAndParameters.mlModelName} do not have a parameter named '${param.name}'")
                    )
                    log.debug(s"\tParam: $paramToSet")

                    // · Getting array of hyperparameter values
                    log.debug(s"· Getting array of hyperparameter values")
                    val valuesToSet = Try{ param.values.map( p => {
                                log.debug(s"\tDecoding value: $p")
                                SparkMlSerializationUtils.decodeParamValue(paramToSet, p, param.paramType).get
                            })
                    }.getOrElse(
                        throw new Exception(
                            s"Error while decoding '${param.name}' values of ml model ${modelAndParameters.mlModelName}")
                    )
                    log.debug(s"\tValues: $valuesToSet")

                    // · Adding hyperparameter with all possible values to grid
                    Try{
                        val tmpGB = new ParamGridBuilder()
                        tmpGB.addGrid(paramToSet, valuesToSet)
                        tmpGB.build()
                    } match {
                        case Failure(e) => throw e
                        case Success(_) =>
                            if(valuesToSet.nonEmpty) {
                                log.debug(s"· Filling hyperparameter '${param.name}' with values: $valuesToSet")
                                gridBuilder.addGrid(paramToSet, valuesToSet)
                            }
                    }
                })
                Try(settedParams.map(_.get)).getOrElse(
                    throw new Exception(settedParams.collect { case Failure(t) => t }.map(_.getMessage).mkString("\n"))
                )

                modelInstance -> gridBuilder.build()
            }
        }

        if( hyperParamGrid.forall(_.isSuccess)) {
            hyperParamGrid.map(_.get).foreach(p => log.debug(p._2.toList))
            hyperParamGrid.map(_.get)
        }else {
            throw new Exception(hyperParamGrid.collect { case Failure(t) => t }.map(_.getMessage).mkString("\n"))
        }
    }
}


/**
  * Default hyperparameters grids used by transmogrifai
  */
object DefaultMlModelsGrid{

    implicit val formats: DefaultFormats.type = DefaultFormats

    lazy val binaryClassModelsAndParams: ModelsAndHyperparamGridsDescriptor = {
        val stream: InputStream = getClass.getResourceAsStream("/binaryClassificationGridDescriptor.json")
        read[ModelsAndHyperparamGridsDescriptor](Source.fromInputStream(stream).mkString)
    }
    lazy val multiclassClassModelsAndParams: ModelsAndHyperparamGridsDescriptor = {
        val stream: InputStream = getClass.getResourceAsStream("/multiclassClassificationGridDescriptor.json")
        read[ModelsAndHyperparamGridsDescriptor](Source.fromInputStream(stream).mkString)
    }
    lazy val regressionModelsAndParams: ModelsAndHyperparamGridsDescriptor = {
        val stream: InputStream = getClass.getResourceAsStream("/regressionGridDescriptor.json")
        read[ModelsAndHyperparamGridsDescriptor](Source.fromInputStream(stream).mkString)
    }

}