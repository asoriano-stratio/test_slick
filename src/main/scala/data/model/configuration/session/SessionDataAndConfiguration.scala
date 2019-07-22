package data.model.configuration.session

import data.model.configuration.session.modelselection.MlModelTypes
import exceptions.ConfigurationException


/** --------------------------------------------------------------------------------------------------------------
  * Case class for serialize/deserialize all information/configuration/results related with a ML session
  * -------------------------------------------------------------------------------------------------------------- */

case class SessionData(
                        // ArangoDb related properties
                        var _key: String,
                        // Session creation time
                        var timestamp: Option[Long] = None,
                        name:Option[String],
                        description:Option[String],
                        // Session - ML workflow spark job status
                        sessionStatus: SessionStatus,
                        // ML workflow configuration
                        jobConfiguration: MlWorkflowJobData
                      ) {
  timestamp = Some(timestamp.getOrElse(System.currentTimeMillis))
}

object SessionData {
  def assert(cond: Boolean, message: String): Unit = {
    if (!cond) throw ConfigurationException(message)
  }
}

// -----------------------------------------------------------
//  => Session (spark job) status
// -----------------------------------------------------------

object SessionStatusStateValues {
  val submitted: String = "submitted"
  val running: String = "running"
  val finished: String = "finished"
  val failed: String = "failed"
  val sessionStatusStateValuesList = List(submitted, running, finished, failed)
}

case class SessionStatus(
                          // DriverId assigned to Spark job when it's launched through Spark dispatcher
                          var sparkDispDriverId: Option[String] = None,
                          // Date when job submission to Spark dispatcher was executed
                          var submittedTimestamp: Option[Long] = None,
                          // Date when job entered in a running state
                          var runningTimestamp: Option[Long] = None,
                          // Date when job finished (correctly or with an error)
                          var finishedTimestamp: Option[Long] = None,
                          // Job state (submitted, running, finished, failed)
                          var state: Option[String] = None,
                          // ML workflow state
                          var currentStage: Option[String] = None,
                          var stageExecutionControl: Option[StageExecutionControl] = None,
                          // Error control
                          var errorMessage: Option[String] = None,
                          var errorStackTrace: Option[String] = None,
                          var warnMessages: Option[Seq[String]] = None
                        ) {

  // => Default values
  state = Some(state.getOrElse(SessionStatusStateValues.submitted))

  // => Validations
  SessionData.assert(SessionStatusStateValues.sessionStatusStateValuesList.contains(state.get),
    s"Invalid value for 'state'; possible values: " +
      s"${SessionStatusStateValues.sessionStatusStateValuesList.mkString(", ")}"
  )
}

case class StageExecutionControl(
                                  var readingInputDataStageStatus: Option[StageExecutionStatus] = None,
                                  var rawDataAnalysisStageStatus: Option[StageExecutionStatus] = None,
                                  var featureEngineeringStageStatus: Option[StageExecutionStatus] = None,
                                  var featureAnalysisStageStatus: Option[StageExecutionStatus] = None,
                                  var modelSelectionStageStatus: Option[StageExecutionStatus] = None
                                )

case class StageExecutionStatus(
                                 var status: String = StageExecutionStatus.notStarted,
                                 var initTimestamp: Option[Long] = None,
                                 var endTimestamp: Option[Long] = None,
                                 var elapsedTime: Option[Double] = None
                               )

object StageExecutionStatus {
  val finished = "finished"
  val running = "running"
  val notStarted = "notStarted"
}

// -----------------------------------------------------------
//  => Session (spark job) configuration
// -----------------------------------------------------------

case class MlWorkflowJobData(
                              // Data source configuration (only Crossdata)
                              var input: InputDef,
                              // Label/features definition
                              var trainData: DatasetDefinition,
                              // ML problem type to solve
                              var mlProblemType: String,
                              // ML workflow stages configuration
                              var globalSessionConfig: Option[GlobalSessionConfig] = None,
                              var rawDataAnalysisConfig: Option[RawDataAnalysisStageConfig] = None,
                              var featureEngineeringConfig: Option[FeatureEngineeringStageConfig] = None,
                              var featureAnalysisConfig: Option[FeatureAnalysisStageConfig] = None,
                              var modelSelectionConfig: Option[ModelSelectionStageConfig] = None
                            ) {

  // => Default values
  // TODO - Improve configuration - deep and pre-analysis
  globalSessionConfig = Some(globalSessionConfig.getOrElse(GlobalSessionConfig()))
  rawDataAnalysisConfig = Some(rawDataAnalysisConfig.getOrElse(RawDataAnalysisStageConfig()))
  featureEngineeringConfig = Some(featureEngineeringConfig.getOrElse(FeatureEngineeringStageConfig()))
  featureAnalysisConfig = Some(featureAnalysisConfig.getOrElse(FeatureAnalysisStageConfig()))
  modelSelectionConfig = Some(modelSelectionConfig.getOrElse(mlProblemType match {
    case MlModelTypes.regression => RegressionModelSelectorConf()
    case MlModelTypes.binaryClassification => BinaryClassificationModelSelectorConf()
    case MlModelTypes.multiClassification => MultiClassificationModelSelectorConf()
    case _ => null
  }))

  // => Validations
  SessionData.assert(MlModelTypes.mlProblemTypeList.contains(mlProblemType),
    s"ML session configuration property 'mlProblemType=$mlProblemType' not allowed; " +
      s"possible values: ${MlModelTypes.mlProblemTypeList.mkString(", ")}")
}


// *****************************************
//  - Raw data source - Only Crossdata
// *****************************************

case class InputDef(
                     var xdCatalogTable: String,
                     var xdCatalogDatabase: Option[String] = None
                   ) {

  // => Default values
  xdCatalogDatabase = Some(xdCatalogDatabase.getOrElse("default"))
}

// *****************************************
//  - Dataset definition
// *****************************************

/**
  * @param labelColName    Label column name
  * @param featuresColsDef Features columns specification
  */
case class DatasetDefinition(
                              var labelColName: String,
                              var featuresColsDef: Seq[ColFeatureDef]
                            ) {
  // => Validations
  SessionData.assert(featuresColsDef.nonEmpty, "Dataset definition error: there not exists any feature.")
  SessionData.assert(!featuresColsDef.map(_.colName).contains(labelColName),
    "Dataset definition error: label has been also included as a feature.")
}

/**
  * @param colName      Dataframe Column name
  * @param colSparkType Spark column type
  * @param featureType  Transmogrifai feature type ('Default' -> default Spark column type to trans. transformation)
  * @param metainfo     Other meta-information about column
  * @param isNullable   Flag for indicate if null values are allowed in column data
  */
case class ColFeatureDef(
                          var colName: String,
                          var colSparkType: Option[String] = None,
                          var featureType: Option[String] = None,
                          var inferredFeatureType: Option[String] = None,
                          var metainfo: Option[Map[String, String]] = Some(Map.empty[String, String]),
                          var isNullable: Option[Boolean] = Some(true) // Default: true
                        ) {

  // => Default values
  isNullable = Some(isNullable.getOrElse(true))
  metainfo = Some(metainfo.getOrElse(Map.empty[String, String]))

  // => Parameter values interceptor
  // · Automatic feature inference --> Feature type must be None.
  if (featureType.nonEmpty && featureType.get == "Auto") featureType = None
  if (featureType.nonEmpty && featureType.get == "Default") featureType = None
  // · In front, PickList feature has 'Categorical' name
  if (featureType.nonEmpty && featureType.get == "Categorical") featureType = Some("PickList")

}





