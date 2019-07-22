package data.model.configuration.session

import com.salesforce.op.stages.impl.preparators.{CorrelationExclusion, CorrelationType}

// *****************************************
//  - Feature analysis stage configuration
// *****************************************

case class FeatureAnalysisStageConfig(
                                  var enableFeatureAnalysisStage: Option[Boolean] = None,
                                  var checkSample: Option[Double] = None,
                                  var sampleSeed: Option[Long] = None,
                                  var sampleLowerLimit: Option[Int] = None,
                                  var sampleUpperLimit: Option[Int] = None,
                                  var maxCorrelation: Option[Double] = None,
                                  var minCorrelation: Option[Double] = None,
                                  var maxCramersV: Option[Double] = None,
                                  var correlationType: Option[String] = None,
                                  var minVariance: Option[Double] = None,
                                  var removeBadFeatures: Option[Boolean] = None,
                                  var removeFeatureGroup: Option[Boolean] = None,
                                  var protectTextSharedHash: Option[Boolean] = None,
                                  var maxRuleConfidence: Option[Double] = None,
                                  var minRequiredRuleSupport: Option[Double] = None,
//                                  var featureLabelCorrOnly: Option[Boolean] = None,
//                                  var correlationExclusion: Option[String] = None,
                                  var categoricalLabel: Option[Boolean] = None
                              ) {

    // => Fixed values
    // TODO - Transmogrifai com.salesforce.op.stages.impl.preparators.SanityChecker line 636 - Not correctly implemented
    val featureLabelCorrOnly: Boolean = true
    // TODO - Possible bug in Transmogrifai (when 'HashedText', seem to keep all features index and in results throws an ArrayIndexOutOfBound exception)
    val correlationExclusion: String = CorrelationExclusionTypes.noExclusion


    // => Default values
    enableFeatureAnalysisStage = Some(enableFeatureAnalysisStage.getOrElse(true))
    checkSample = Some(checkSample.getOrElse(1.0))
    sampleSeed = Some(sampleSeed.getOrElse(util.Random.nextLong))
    sampleLowerLimit = Some(sampleLowerLimit.getOrElse(1E3.toInt))
    sampleUpperLimit = Some(sampleUpperLimit.getOrElse(1E6.toInt))
    maxCorrelation = Some(maxCorrelation.getOrElse(0.95))
    minCorrelation = Some(minCorrelation.getOrElse(0.0))
    maxCramersV = Some(maxCramersV.getOrElse(0.95))
    correlationType = Some(correlationType.getOrElse(CorrelationTypeDef.pearson))
    minVariance = Some(minVariance.getOrElse(0.0))
    removeBadFeatures = Some(removeBadFeatures.getOrElse(false))
    removeFeatureGroup = Some(removeFeatureGroup.getOrElse(true))
    protectTextSharedHash = Some(protectTextSharedHash.getOrElse(false))
    maxRuleConfidence = Some(maxRuleConfidence.getOrElse(1.0))
    minRequiredRuleSupport = Some(minRequiredRuleSupport.getOrElse(1.0))

    // => Validations
    SessionData.assert(checkSample.get > 0.0 && checkSample.get <= 1.0, "Feature analysis parameter 'checkSample' must be defined in range ]0,1]")
    SessionData.assert(sampleLowerLimit.get > 0.0, "Feature analysis parameter 'sampleLowerLimit' must be greater than 0")
    SessionData.assert(sampleUpperLimit.get > 0.0, "Feature analysis parameter 'sampleUpperLimit' must be greater than 0")
    SessionData.assert(sampleUpperLimit.get >= sampleLowerLimit.get, "Feature analysis parameter 'sampleUpperLimit' must be greater than parameter 'sampleLowerLimit' ")
    SessionData.assert(CorrelationTypeDef.correlationTypeDefList.contains(correlationType.get),
        s"Feature analysis parameter 'correlationType' with an invalid value (${correlationType.get}). Allowed values: ${CorrelationTypeDef.correlationTypeDefList.mkString("[", ",", "]")}")
    SessionData.assert(CorrelationExclusionTypes.correlationExclusionTypesList.contains(correlationExclusion),
        s"Feature analysis parameter 'correlationExclusion' with an invalid value ($correlationExclusion). Allowed values: ${CorrelationTypeDef.correlationTypeDefList.mkString("[", ",", "]")}")
    SessionData.assert(maxCorrelation.get >= 0.0 && maxCorrelation.get <= 1.0, "Feature analysis parameter 'maxCorrelation' must be defined in range [0,1]")
    SessionData.assert(minCorrelation.get >= 0.0 && minCorrelation.get <= 1.0, "Feature analysis parameter 'minCorrelation' must be defined in range [0,1]")
    SessionData.assert(maxCramersV.get >= 0.0 && maxCramersV.get <= 1.0, "Feature analysis parameter 'maxCramersV' must be defined in range [0,1]")
    SessionData.assert(maxRuleConfidence.get >= 0.0 && maxRuleConfidence.get <= 1.0, "Feature analysis parameter 'maxRuleConfidence' must be defined in range [0,1]")
    SessionData.assert(minRequiredRuleSupport.get >= 0.0 && minRequiredRuleSupport.get <= 1.0, "Feature analysis parameter 'minRequiredRuleSupport' must be defined in range [0,1]")
}

object CorrelationTypeDef {
    val pearson = "pearson"
    val spearman = "spearman"

    val correlationTypeDefList = List(pearson, spearman)

    def getCorrelationType(corr: String): CorrelationType = {
        corr match {
            case `pearson` => CorrelationType.Pearson
            case `spearman` => CorrelationType.Spearman
            case _ => throw new Exception(
                s"Invalid correlation value '$corr'; admitted values: ${correlationTypeDefList.mkString(", ")}")
        }
    }
}

object CorrelationExclusionTypes {
    val noExclusion = "NoExclusion"
    val hashedText = "HashedText"

    val correlationExclusionTypesList = List(noExclusion, hashedText)

    def getCorrelationExclusionType(corrEx: String): CorrelationExclusion = {
        corrEx match {
            case `noExclusion` => CorrelationExclusion.NoExclusion
            case `hashedText` => CorrelationExclusion.HashedText
            case _ => throw new Exception(
                s"Invalid correlation exclusion value '$corrEx'; admitted values: ${correlationExclusionTypesList.mkString(", ")}")
        }
    }
}