package data.model.configuration.session.modelselection

import com.salesforce.op.evaluators.EvaluationMetrics
import com.salesforce.op.stages.impl.selector.{ModelEvaluation, ModelSelectorSummary}
import org.apache.spark.sql.types.Metadata
import org.json4s
import org.json4s.JsonAST.JNull


case class ModelSelectionSummaryData(
                                        // => Evaluation dataset
                                        DataPrepParameters: Map[String, Any],
                                        DataPrepResults: Option[Metadata],
                                        // => Model selection - Validation stage
                                        ProblemType: String,
                                        EvaluationMetric: String,
                                        ValidationType: String,
                                        ValidationParameters: Map[String, Any],
                                        ValidationResults: Seq[CustomModelEvaluation],
                                        // => Best model
                                        BestModelUID: String,
                                        BestModelName: String,
                                        BestModelType: String,
                                        BestModel:CustomModelEvaluation,
                                        TrainEvaluation: EvaluationMetrics,
                                        HoldoutEvaluation: Option[EvaluationMetrics],
                                        // => Stage data
                                        var oldSummaryData:json4s.JValue = JNull,
                                        var stageMessages:Seq[String] = Seq()
                                    )

object ModelSelectionSummaryData{
    def apply(metaData: ModelSelectorSummary): ModelSelectionSummaryData ={
        new ModelSelectionSummaryData(
            ValidationType = metaData.validationType.toString,
            ValidationParameters = metaData.validationParameters,
            DataPrepParameters = metaData.dataPrepParameters,
            DataPrepResults = metaData.dataPrepResults match {
                case Some(x) => Some(x.toMetadata())
                case _ => None
            },
            EvaluationMetric = metaData.evaluationMetric.humanFriendlyName,
            ProblemType = metaData.problemType.toString,
            BestModelUID = metaData.bestModelUID,
            BestModelName = metaData.bestModelName,
            BestModelType = metaData.bestModelType.stripPrefix("Op"),
            ValidationResults = metaData.validationResults.map( m => CustomModelEvaluation(m)),
            BestModel = CustomModelEvaluation(metaData.validationResults.filter(_.modelName == metaData.bestModelName).head),
            TrainEvaluation = metaData.trainEvaluation,
            HoldoutEvaluation = metaData.holdoutEvaluation
        )
    }
}

case class ModelSelectionReducedSummary(
                                            elapsedTime: Double,
                                            numberOfTrainedModels:Int
                                       )

case class CustomModelEvaluation(
                                    ModelUID: String,
                                    ModelName: String,
                                    ModelType: String,
                                    metricName: String,
                                    metricValue:Double,
                                    ModelParameters: Map[String, Any]
                                )
object CustomModelEvaluation{
    def apply(modelEvaluation: ModelEvaluation): CustomModelEvaluation =
        new CustomModelEvaluation(
            ModelUID = modelEvaluation.modelUID,
            ModelName = modelEvaluation.modelName.stripPrefix("Op"),
            ModelType = modelEvaluation.modelType.stripPrefix("Op"),
            metricName = modelEvaluation.metricValues.toMap.head._1,
            metricValue = modelEvaluation.metricValues.toMap.head._2.asInstanceOf[Double],
            ModelParameters = modelEvaluation.modelParameters - "inputFeatures"
        )
}