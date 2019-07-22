package data.model.configuration.session.results

import spark.ml.feature.rawdataanalyzer.{AnalyzedColumn, RawDataAnalysisSummarizer}


case class RawDataAnalysisSummaryData(
                                         nSamples: Long,
                                         numberOfPartitions: Int,
                                         analyzedColumns: Seq[AnalyzedColumn],
                                         var stageMessages:Seq[String] = Seq()
                                     ) {


}

case class RawDataAnalysisReducedSummary(
                                            elapsedTime:Double,
                                            numberOfRecordsInDataset:Long,
                                            numberOfPartitionsInDataset:Long,
                                            numberOfRecordsByPartition:Map[String, Int],
                                            numberOfColumnsInDataset:Long,
                                            numberOfSelectedColumns:Long,
                                            variableTypeAnalysis:Map[String, Int]
                                        )

object RawDataAnalysisSummaryData {
    def apply(analyzedDf: RawDataAnalysisSummarizer) =
        new RawDataAnalysisSummaryData(analyzedDf.nSamples, analyzedDf.partitionsCount.size, analyzedDf.analyzedColumns)
}