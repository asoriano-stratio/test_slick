package data.model.configuration.session.results

case class SparkDfAdequationSummary(
                                nSamples:Long,
                                notNullCounter:Map[String, Long],
                                label: LabelAdequation,
                                features: Seq[FeatureAdequation],
                                var stageMessages:Seq[String] = Seq()
                            )


case class LabelAdequation(
                              name: String,
                              fromSparkType: String, toSparkType: Option[String] = None,
                              stringIndexerLabels: Option[Map[String,Double]] = None,
                              var labelValuesMapper: Option[Map[String, String]] = None,
                              var attribute: Option[String] = None
                          )


case class FeatureAdequation(
                                name: String,
                                featureType:String,
                                fromSparkType: String, toSparkType: Option[String] = None,
                                transform: Option[Seq[String]] = None
                            )