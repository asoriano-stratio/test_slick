package data.model.configuration.session

import spark.ml.feature.rawdataanalyzer.SamplingMethod


// *****************************************
//  - Raw data analysis
// *****************************************

case class RawDataAnalysisStageConfig(
                                     var enableRawDataAnalysisStage: Option[Boolean] = None,
                                     var numSampleMax: Option[Int] = None,
                                     var samplingMethod: Option[String] = None,
                                     var persistRdd: Option[Boolean] = None,
                                     var numBinsHistogram: Option[Int] = None
                                 ) {
    // => Default values
    enableRawDataAnalysisStage = Some(enableRawDataAnalysisStage.getOrElse(true))
    samplingMethod = Some(samplingMethod.getOrElse(SamplingMethod.fullData))
    persistRdd = Some(persistRdd.getOrElse(true))
    numBinsHistogram = Some(numBinsHistogram.getOrElse(20))

    // => Validations
    SessionData.assert(SamplingMethod.samplingMethods.contains(samplingMethod.get),
        s"Raw data analysis parameter 'samplingMethod' with an invalid value ('${samplingMethod.get}'). " +
            s"Allowed values: ${SamplingMethod.samplingMethods.mkString("[", ", ", "]")}")
    SessionData.assert(numBinsHistogram.get > 1,
        "Raw data analysis parameter 'numBinsHistogram' must be greater than 1")
}