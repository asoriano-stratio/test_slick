package data.model.configuration.session.results

import org.json4s.JValue


case class FeatureAnalysisSummary(
                                     exploratoryAnalysisRaw: JValue,
                                     featureSelection: Option[Seq[FeatureDroppedExplanation]],
                                     featureAnalysisData: FeatureAnalysisData,
                                     var stageMessages:Seq[String] = Seq()
                                 )

case class FeatureAnalysisData(
                                  sampleFraction: Double,
                                  count: Double,
                                  correlationType: String,
                                  numericStatistics: Option[Seq[ColumnNumericFeaturesStatistics]],
                                  categoricalStatistics: Option[Seq[ColumnCategoricalFeaturesStatistics]],
                                  invalidFeaturesInCorrelation: Option[Seq[InvalidFeaturesCorrelation]]
                              )

case class FeatureAnalysisReducedSummary(
                                            elapsedTime:Double,
                                            numberOfNumericFeaturesAnalyzed:Int,
                                            numberOfCategoricalFeaturesAnalyzed:Int,
                                            numberOfNonInformativeFeatures:Int
                                        )


// ----------------------------------------------------------------------------------------
// Numeric features - Statistics summary
// ----------------------------------------------------------------------------------------

case class ColumnNumericFeaturesStatistics(
                                              columnName: String,
                                              numberOfNumericFeatures: Int,
                                              featuresStatistics: Option[Seq[NumericFeatureStatistics]]
                                          )

case class NumericFeatureStatistics(
                                       featureName: String,
                                       maximumValue: Double,
                                       minimumValue: Double,
                                       mean: Double,
                                       standardDeviation: Double,
                                       var correlationWithLabel: Option[Double] = None
                                   )

// ----------------------------------------------------------------------------------------
// Categorical features - Statistics summary
// ----------------------------------------------------------------------------------------

case class ColumnCategoricalFeaturesStatistics(
                                                  columnName: String,
                                                  categoricalFeatures: Seq[String],
                                                  contingencyMatrix: Map[String, Array[Double]],
                                                  cramersV: Double,
                                                  mutualInfo: Double
                                              )

// ----------------------------------------------------------------------------------------
// Excluded features in correlation
// ----------------------------------------------------------------------------------------

case class InvalidFeaturesCorrelation(
                                         featureName: String,
                                         parentColumnName: String
                                     )

// ----------------------------------------------------------------------------------------
// Feature selection summary
// ----------------------------------------------------------------------------------------

case class FeatureDroppedExplanation(
                                        featureName: String,
                                        reasons: Seq[String],
                                        reasonsMap: Map[String, String]
                                    )