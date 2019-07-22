package data.model.configuration.preanalysis

import data.model.configuration.session.SessionStatus
import spark.ml.feature.rawdataanalyzer.{RawDataAnalysisSummarizer, SamplingMethod}

/** --------------------------------------------------------------------------------------------
  *   Case class for serialize/deserialize all information about a asset raw data analysis
  * -------------------------------------------------------------------------------------------- */

case class AssetData(
                        databaseName:String,
                        tableName:String,
                        rawDataPreAnalysis:RawDataPreAnalysisData
                    )

case class RawDataPreAnalysisData(
                                    status:SessionStatus,
                                    var config:Option[RawDataPreAnalysisJobConf] = None,
                                    var results:Option[RawDataAnalysisSummarizer]
                                 ){
    config = Some(config.getOrElse(RawDataPreAnalysisJobConf()))
}

case class RawDataPreAnalysisJobConf (
                                         var numSampleMax:Option[Int] = None,
                                         var samplingMethod: Option[String] = None,
                                         var numMaxCategoricalValues:Option[Int] = None,
                                         var numBinsHistogram: Option[Int] = None,
                                         var persistRdd:Option[Boolean] = None
                                     ){
    // => Default values
    numSampleMax = Some(numSampleMax.getOrElse(10000))
    samplingMethod = Some(samplingMethod.getOrElse(SamplingMethod.limit))
    numMaxCategoricalValues = Some(numMaxCategoricalValues.getOrElse(30))
    numBinsHistogram = Some(numBinsHistogram.getOrElse(20))
    persistRdd = Some(persistRdd.getOrElse(true))
}
