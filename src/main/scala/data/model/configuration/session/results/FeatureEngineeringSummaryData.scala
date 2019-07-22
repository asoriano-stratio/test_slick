package data.model.configuration.session.results

import com.salesforce.op.{Insights, ModelInsights}
import org.apache.log4j.Logger
import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, JValue}

import scala.io.Source
import scala.util.Try


/** ------------------------------------------------------------------------
  * Case class for keeping feature engineering stage summary
  * ------------------------------------------------------------------------
  */
case class FeatureEngineeringSummary(
                                        features: JValue,
                                        stages: JValue,
                                        outputColumnName: String,
                                        outputFeaturesMetadata: JValue,
                                        rawColumnFeatures: Seq[RawColumn],
                                        var stageMessages:Seq[String] = Seq()
                                    )

case class FeatureEngineeringReducedSummary(
                                               elapsedTime:Double,
                                               selectedColumnsAsInitialFeatures:Int,
                                               featuresFinallyGenerated:Int
                                           )

// -----------------------------------------------------------
//  => Initial dataframe column information
// -----------------------------------------------------------

case class RawColumn(
                        columnName: String,
                        featureType: String,
                        description: String,
                        transformer: String,
                        derivedFeatures: Int,
                        derivedFeaturesInfo: Seq[FeatureRow]
                    )


// -----------------------------------------------------------
//  => Derived features from a initial dataframe column
// -----------------------------------------------------------

sealed trait FeatureRow {
    def featureName: String
}

// - Feature
case class FeatureSimpleRow(
                               featureName: String,
                               attributeType: String,
                               description: String
                           ) extends FeatureRow

// - Binary feature (after one hot encoder) derived from a categorical variable --> It contains its category
case class FeatureWithCategoryRow(
                                     featureName: String,
                                     attributeType: String,
                                     category: String,
                                     description: String,
                                     features:Option[Seq[FeatureWithCategoryRow]] = None
                                 ) extends FeatureRow

case class FeatureTextRow(
                                     featureName: String,
                                     attributeType: String,
                                     description: String,
                                     features:Option[Seq[String]] = None
                                 ) extends FeatureRow


/** ------------------------------------------------------------------------
  * Case class containing feature engineering statics descriptions
  * ------------------------------------------------------------------------
  */
case class FeatureEngineeringDescription(
                                            featureVectorizerMap: Map[String, String],
                                            vectorizersDescriptionMap: Map[String, String]
                                        ) {
    def getVectorizerDescription(featType: String): String = {
        vectorizersDescriptionMap(featureVectorizerMap(featType))
    }
}

/** ---------------------------------------------------------------------------------------
  *  Object with helper methods to extract the required feature engineering summary
  * ---------------------------------------------------------------------------------------
  */
object DerivedFeaturesSummaryExtractor {

    val log: Logger = Logger.getLogger(getClass.getName)
    lazy val featureEngineeringDescription: FeatureEngineeringDescription = Try {
        implicit val formats: DefaultFormats.type = DefaultFormats
        read[FeatureEngineeringDescription](
            Source.fromInputStream(getClass.getResourceAsStream("/featureEngineeringDescriptions.json")).mkString
        )
    }.getOrElse(throw new Exception("Error while reading descriptor file 'featureEngineeringDescriptions.json'"))

    private def getDerivedFeaturesFromCategorical(derivedFeatures: Seq[Insights]): Seq[FeatureWithCategoryRow] = {

        val (otherNullsIndicator, oneHotEncoder) = derivedFeatures.partition(
            f => Seq("OTHER", "NullIndicatorValue").contains(f.derivedFeatureValue.getOrElse("")))

        val indicatorVariables: Seq[FeatureWithCategoryRow] = otherNullsIndicator.map {
            case y if y.derivedFeatureValue.getOrElse("") == "OTHER" =>
                FeatureWithCategoryRow(
                    featureName = y.derivedFeatureName,
                    attributeType = "Binary",
                    category = "Other",
                    description = "This binary feature captures all categories that do not have the minimum required " +
                        "support and categories not seen in training."
                )
            case y if y.derivedFeatureValue.getOrElse("") == "NullIndicatorValue" =>
                FeatureWithCategoryRow(
                    featureName = y.derivedFeatureName,
                    attributeType = "Binary",
                    category = "Null indicator (missing value)",
                    description = "Missing values indicator as a binary variable (it takes the value '1.0' when " +
                        "the raw column presents a missing value)."
                )
            case _ => throw new Exception("Unknown feature indicator in raw categorical variable feature engineering.")
        }

        if( oneHotEncoder.isEmpty){
            indicatorVariables
        }else {
            val firstElem = oneHotEncoder.head
            val featNamePrefix = firstElem.derivedFeatureName.split(s"_${firstElem.derivedFeatureValue.get}").head
            val featName = s"${featNamePrefix}_{category}_{vector_index}"
            val categories = oneHotEncoder.map(_.derivedFeatureValue.get).mkString(", ")
            indicatorVariables :+ FeatureWithCategoryRow(
                featureName = featName,
                attributeType = s"${oneHotEncoder.length} Binary features (One hot encoder representation)",
                category = oneHotEncoder.map(_.derivedFeatureValue.get).mkString(", "),
                description = s"${oneHotEncoder.length} Binary features with the one hot encoder representation of " +
                    s"the categorical column variable; categories: $categories",
                features = Some(oneHotEncoder.map(y => FeatureWithCategoryRow(
                        featureName = y.derivedFeatureName, attributeType = "Binary",
                        category = y.derivedFeatureValue.get, description = ""
                    )
                ))
            )
        }
    }

    private def getDerivedFeaturesFromTextual(derivedFeatures: Seq[Insights]): Seq[FeatureTextRow] = {

        val (otherNullsIndicator, hashVectorFeatures) = derivedFeatures.partition(
            f => Seq("NullIndicatorValue").contains(f.derivedFeatureValue.getOrElse("")))

        val indicatorVariables: Seq[FeatureTextRow] = otherNullsIndicator.flatMap {
            case y if y.derivedFeatureValue.getOrElse("") == "NullIndicatorValue" =>
                Some(FeatureTextRow(
                    featureName = y.derivedFeatureName,
                    attributeType = "Binary",
                    description = "Missing values indicator as a binary variable (it takes the " +
                        "value '1.0' when the raw column presents a missing value)."
                ))
            case _ => None
        }

        if (hashVectorFeatures.isEmpty) {
            indicatorVariables
        } else {
            val firstElem = hashVectorFeatures.head
            val featNamePrefix = firstElem.derivedFeatureName.split("_").dropRight(1).mkString("_") + "_X"
            val featName = s"${featNamePrefix}_{category}_{vector_index}"
            indicatorVariables :+ FeatureTextRow(
                featureName = featName,
                attributeType = s"${hashVectorFeatures.length} Numeric features (Hash vector)",
                description = s"${hashVectorFeatures.length} numeric features representing the vector obtained after " +
                    "applying hashing transforming pipeline to textual content in raw column.",
                features = Some(hashVectorFeatures.map(_.derivedFeatureName))
            )
        }
    }


    def extract(insights: ModelInsights): Seq[RawColumn] = {
        insights.features.map(f => {
            val featureType = f.featureType.replace("com.salesforce.op.features.types.", "")
            log.info(s"Extracting derived features summary from column ${f.featureName}($featureType)")
            featureType match {
                case "RealNN" =>
                    assert(f.derivedFeatures.length == 1, "ReanNN feature with more than 1 derived feature")
                    RawColumn(
                        columnName = f.featureName,
                        featureType = featureType,
                        description = featureEngineeringDescription.getVectorizerDescription(featureType),
                        transformer = "RealNN transformer",
                        derivedFeatures = f.derivedFeatures.length,
                        derivedFeaturesInfo = f.derivedFeatures.map { y => {
                            FeatureSimpleRow(
                                featureName = y.derivedFeatureName,
                                attributeType = "Numeric",
                                description = "Feature based on raw column without any transformation (it is assumed to " +
                                    "be a non nullable variable without missing values)."
                            )
                        }
                        }
                    )

                case "Real" | "Currency" | "Percent" =>
                    RawColumn(
                        columnName = f.featureName,
                        featureType = featureType,
                        description = featureEngineeringDescription.getVectorizerDescription(featureType),
                        transformer = "Real transformer",
                        derivedFeatures = f.derivedFeatures.length,
                        derivedFeaturesInfo = f.derivedFeatures.map {
                            case y if y.derivedFeatureValue.isDefined =>
                                FeatureSimpleRow(
                                    featureName = y.derivedFeatureName,
                                    attributeType = "Binary",
                                    description = "Missing values indicator as a binary variable (it takes the value " +
                                        "'1.0' when the raw column presents a missing value)."
                                )
                            case y =>
                                FeatureSimpleRow(
                                    featureName = y.derivedFeatureName,
                                    attributeType = "Numeric",
                                    description = "Feature based on raw column with missing values (if any) imputed " +
                                        "with the mean of the rest of values."
                                )
                        }
                    )

                case "Integral" =>
                    RawColumn(
                        columnName = f.featureName,
                        featureType = featureType,
                        description = featureEngineeringDescription.getVectorizerDescription(featureType),
                        transformer = "Integral transformer",
                        derivedFeatures = f.derivedFeatures.length,
                        derivedFeaturesInfo = f.derivedFeatures.map {
                            case y if y.derivedFeatureValue.isDefined =>
                                FeatureSimpleRow(
                                    featureName = y.derivedFeatureName,
                                    attributeType = "Binary",
                                    description = "Missing values indicator as a binary variable (it takes the value " +
                                        "'1.0' when the raw column presents a missing value)."
                                )
                            case y =>
                                FeatureSimpleRow(
                                    featureName = y.derivedFeatureName,
                                    attributeType = "Numeric",
                                    description = "Feature based on raw column with missing values (if any) imputed " +
                                        "with the mode of the rest of values."
                                )
                        }
                    )

                case "Binary" =>
                    RawColumn(
                        columnName = f.featureName,
                        featureType = featureType,
                        description = featureEngineeringDescription.getVectorizerDescription(featureType),
                        transformer = "Binary transformer",
                        derivedFeatures = f.derivedFeatures.length,
                        derivedFeaturesInfo = f.derivedFeatures.map {
                            case y if y.derivedFeatureValue.isDefined =>
                                FeatureSimpleRow(
                                    featureName = y.derivedFeatureName,
                                    attributeType = "Binary",
                                    description = "Missing values indicator as a binary variable (it takes the value " +
                                        "'1.0' when the raw column presents a missing value)."
                                )
                            case y =>
                                FeatureSimpleRow(
                                    featureName = y.derivedFeatureName,
                                    attributeType = "Binary",
                                    description = "Feature based on raw column with missing values (if any) imputed " +
                                        "with 'false' ('0.0' value)."
                                )
                        }
                    )

                case "PickList" | "City" | "Country" | "State" | "Street" | "PostalCode" | "ID" =>
                    RawColumn(
                        columnName = f.featureName,
                        featureType = if (featureType == "PickList") "Categorical" else featureType,
                        description = featureEngineeringDescription.getVectorizerDescription(featureType),
                        transformer = "Categorical transformer",
                        derivedFeatures = f.derivedFeatures.length,
                        derivedFeaturesInfo = getDerivedFeaturesFromCategorical(f.derivedFeatures)
                    )

                case "Date" | "DateTime" =>
                    RawColumn(
                        columnName = f.featureName,
                        featureType = featureType,
                        description = featureEngineeringDescription.getVectorizerDescription(featureType),
                        transformer = "Date transformer",
                        derivedFeatures = f.derivedFeatures.length,
                        derivedFeaturesInfo = f.derivedFeatures.map {
                            case y if (y.derivedFeatureName.contains("_DateList_") || y.derivedFeatureName.contains("_DateTimeList_")) && y.derivedFeatureValue.isDefined =>
                                FeatureSimpleRow(
                                    featureName = y.derivedFeatureName,
                                    attributeType = "Binary",
                                    description = "Missing values indicator as a binary variable (it takes the value " +
                                        "'1.0' when the raw column presents a missing value)."
                                )

                            case y if (y.derivedFeatureName.contains("_DateList_") || y.derivedFeatureName.contains("_DateTimeList_")) && y.derivedFeatureValue.isEmpty =>
                                FeatureSimpleRow(
                                    featureName = y.derivedFeatureName,
                                    attributeType = "Numeric",
                                    description = "Feature representing the number of days between the last event" +
                                        " and reference date."
                                )

                            case y =>
                                val comp: Array[String] = y.derivedFeatureName.split("_").reverse.take(3)
                                assert(Seq("HourOfDay", "DayOfWeek", "DayOfMonth", "DayOfYear").contains(comp(1)))
                                assert(Seq("x", "y").contains(comp(2)))

                                FeatureSimpleRow(
                                    featureName = y.derivedFeatureName,
                                    attributeType = "Numeric",
                                    description = s"Cartesian '${comp(2)}' coordinate as representation of the " +
                                        s"${comp(1)} on the unit circle (the time period is extracted from timestamp " +
                                        s"and mapped onto the unit circle containing the number of time " +
                                        s"periods equally spaced)."
                                )
                        }
                    )

                case "URL" | "Email" =>
                    RawColumn(
                        columnName = f.featureName,
                        featureType = featureType,
                        description = featureEngineeringDescription.getVectorizerDescription(featureType),
                        transformer = "MIMEType extractor & categorical transformer",
                        derivedFeatures = f.derivedFeatures.length,
                        derivedFeaturesInfo = getDerivedFeaturesFromCategorical(f.derivedFeatures)
                    )

                case "Base64" =>
                    RawColumn(
                        columnName = f.featureName,
                        featureType = featureType,
                        description = featureEngineeringDescription.getVectorizerDescription(featureType),
                        transformer = "MIMEType extractor & categorical transformer",
                        derivedFeatures = f.derivedFeatures.length,
                        derivedFeaturesInfo = getDerivedFeaturesFromCategorical(f.derivedFeatures)
                    )

                case "Text" =>
                    RawColumn(
                        columnName = f.featureName,
                        featureType = featureType,
                        description = featureEngineeringDescription.getVectorizerDescription(featureType),
                        transformer = "Text transformer",
                        derivedFeatures = f.derivedFeatures.length,
                        derivedFeaturesInfo =
                            // - NLP Hashing pipeline
                            if (f.derivedFeatures.length >= 512) {
                                getDerivedFeaturesFromTextual(f.derivedFeatures)
                            // - Categorical feature
                            } else {
                                getDerivedFeaturesFromCategorical(f.derivedFeatures)
                            }
                    )

                case _ => throw new Exception(s"Unknown feature type: $featureType")
            }
        })
    }
}