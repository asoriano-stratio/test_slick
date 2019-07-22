package data.model.configuration.session

import com.salesforce.op.stages.impl.feature._
import com.salesforce.op.utils.date.DateTimeUtils

// *****************************************
//  - Adequation + Feature engineering phase
// *****************************************

case class FeatureEngineeringStageConfig(
                                     var enableFeatureEngineeringStage: Option[Boolean] = None,
                                     // · Custom transmogrifier parameters
                                     var defaultNumOfFeatures: Option[Int] = None,
                                     var topK: Option[Int] = None,
                                     var minSupport: Option[Int] = None,
                                     var fillValue: Option[Int] = None,
                                     var binaryFillValue: Option[Boolean] = None,
                                     var hashWithIndex: Option[Boolean] = None,
                                     var prependFeatureName: Option[Boolean] = None,
                                     var cleanText: Option[Boolean] = None,
                                     var fillWithMode: Option[Boolean] = None,
                                     var fillWithMean: Option[Boolean] = None,
                                     var trackInvalid: Option[Boolean] = None,
                                     var minInfoGain: Option[Double] = None
                                 ) {

    // => Todo - Complex parameters
    val DateListDefault: DateListPivot = DateListPivot.SinceLast
    val ReferenceDate: org.joda.time.DateTime = DateTimeUtils.now()
    val HashSpaceStrategy: HashSpaceStrategy = com.salesforce.op.stages.impl.feature.HashSpaceStrategy.Auto
    val HashAlgorithm: HashAlgorithm = com.salesforce.op.stages.impl.feature.HashAlgorithm.MurMur3
    val CircularDateRepresentations: Seq[TimePeriod] =
        Seq(TimePeriod.HourOfDay, TimePeriod.DayOfWeek, TimePeriod.DayOfMonth, TimePeriod.DayOfYear)

    // => Default values
    enableFeatureEngineeringStage = Some(enableFeatureEngineeringStage.getOrElse(true))
    // · Custom transmogrifier parameters
    defaultNumOfFeatures = Some(defaultNumOfFeatures.getOrElse(512))
    topK = Some(topK.getOrElse(20))
    minSupport = Some(minSupport.getOrElse(10))
    fillValue = Some(fillValue.getOrElse(0))
    binaryFillValue = Some(binaryFillValue.getOrElse(false))
    hashWithIndex = Some(hashWithIndex.getOrElse(false))
    prependFeatureName = Some(prependFeatureName.getOrElse(true))
    cleanText = Some(cleanText.getOrElse(true))
    fillWithMode = Some(fillWithMode.getOrElse(true))
    fillWithMean = Some(fillWithMean.getOrElse(true))
    trackInvalid = Some(trackInvalid.getOrElse(false))
    minInfoGain = Some(minInfoGain.getOrElse(DecisionTreeNumericBucketizer.MinInfoGain))

    // => Accessors
    def DefaultNumOfFeatures: Int = defaultNumOfFeatures.get
    def TopK: Int = topK.get
    def MinSupport: Int = minSupport.get
    def FillValue: Int = fillValue.get
    def BinaryFillValue: Boolean = binaryFillValue.get
    def HashWithIndex: Boolean = hashWithIndex.get
    def PrependFeatureName: Boolean = prependFeatureName.get
    def CleanText: Boolean = cleanText.get
    def FillWithMode: Boolean = fillWithMode.get
    def FillWithMean: Boolean = fillWithMean.get
    def TrackInvalid: Boolean = trackInvalid.get
    def MinInfoGain: Double = minInfoGain.get
}