package spark.ml.feature.rawdataanalyzer

// -----------------------------------------------------------------------------------
//  => Process configuration
// -----------------------------------------------------------------------------------

object SamplingMethod {
    val limit: String = "limit"
    val sample: String = "sample"
    val fullData: String = "fullData"

    val samplingMethods: Seq[String] = Seq(limit, sample, fullData)
}

case class RawDataAnalyzerConf(
                                  numSampleMax: Int = 10000,
                                  samplingMethod: String = SamplingMethod.limit,
                                  numMaxCategoricalValues: Int = 30,
                                  numBinsHistogram: Int = 20,
                                  persistRdd: Boolean = true
                              )

// -----------------------------------------------------------------------------------
//  => Data representation
// -----------------------------------------------------------------------------------

case class RawDataAnalysisSummarizer(
                                        nSamples: Long,
                                        partitionsCount: Map[String, Int],
                                        analyzedColumns: Seq[AnalyzedColumn]
                                    )

sealed trait AnalyzedColumn{
    val name:String
    val numberOfMissingValues:Long
    val sparkColType:String
    def featureQuality:Int
    def featureQuality_=(value: Int): Unit
}

// => Spark column with not allowed format type
case class NonAllowedColumn(
                               name: String,
                               sparkColType: String,
                               numberOfMissingValues:Long = 0,
                               attribute: String = "notSupportedColumn",
                               var featureQuality:Int = 0
                           ) extends AnalyzedColumn

// => Column with a constant value
case class ConstantColumn(
                             name: String,
                             sparkColType: String,
                             value: String,
                             numberOfMissingValues: Long,
                             attribute: String = "constant",
                             var featureQuality:Int = 0
                         ) extends AnalyzedColumn

// => Continuous variable
case class NumericColumn(
                            name: String,
                            sparkColType: String,
                            minimumValue: Double,
                            maximumValue: Double,
                            mean: Double,
                            standardDeviation: Double,
                            numberOfMissingValues: Long,
                            attribute: String = "numeric",
                            var histogram: Option[Histogram] = None,
                            var histogramOfFrequencies: Option[Map[String, Long]] = None,
                            var featureQuality:Int = 100
                        ) extends AnalyzedColumn

case class Histogram(
                        buckets: Array[Double],
                        counts: Array[Long],
                        entropy: Double
                    )

// => Categorical column
case class CategoricalColumn(
                                name: String,
                                sparkColType: String,
                                values: Seq[String],
                                numberOfMissingValues: Long,
                                attribute: String = "categorical",
                                var occurrenceCounts: Option[Map[String, Long]] = None,
                                var featureQuality:Int = 100
                            ) extends AnalyzedColumn

// => Binary column
case class BinaryColumn(
                           name: String,
                           sparkColType: String,
                           values: Seq[String],
                           numberOfMissingValues: Long,
                           attribute: String = "binary",
                           var occurrenceCounts: Option[Map[String, Long]] = None,
                           var featureQuality:Int = 100
                       ) extends AnalyzedColumn

// => Textual column
case class TextualColumn(
                            name: String,
                            sparkColType: String,
                            numberOfMissingValues: Long,
                            canBeNumeric: Boolean,
                            numberOfNumericValues: Option[Long] = None,
                            asNumericFeature:Option[NumericColumn] = None,
                            attribute: String = "textual",
                            var featureQuality:Int = 100
                        ) extends AnalyzedColumn

// => Date columns
case class DateColumn(
                         name: String,
                         sparkColType: String,
                         numberOfMissingValues:Long = 0,
                         attribute: String = "date",
                         var featureQuality:Int = 100
                     ) extends AnalyzedColumn

