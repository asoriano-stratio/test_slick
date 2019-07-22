package data.model.configuration.session

import com.salesforce.op.stages.impl.selector.ModelSelectorNames.EstimatorType
import data.model.configuration.session.modelselection._
import exceptions.ConfigurationException
import org.apache.spark.ml.param.ParamMap
import utils.{DefaultMlModelsGrid, MlModelAndGridDescriptor, MlModelsGridDeserializer}


// *****************************************
//  Model selection configuration
// *****************************************

// TODO - Simplify - Avoid conditional deserialization
sealed trait ModelSelectionStageConfig {
    def hyperparameterGridObject: Seq[(EstimatorType, Array[ParamMap])]
    def enableModelSelectionStage: Option[Boolean]
    def enableModelSelectionStage_=(value: Option[Boolean]): Unit
}


case class RegressionModelSelectorConf(
                                          var enableModelSelectionStage: Option[Boolean] = None,
                                          var validationTechnique: Option[String] = None,
                                          var numFolds: Option[Int] = None,
                                          var trainRatio: Option[Double] = None,
                                          var seed: Option[Long] = None,
                                          var parallelism: Option[Int] = None,
                                          var validationMetric: Option[String] = None,
                                          var modelTypesToUse: Option[Seq[String]] = None,
                                          var modelsAndParameters: Option[Seq[MlModelAndGridDescriptor]] = None,
                                          var splitterConf: Option[DataSplitterConf] = None
                                      ) extends ModelSelectionStageConfig {

    val defaultExcludedModels: Seq[String] = Seq("DecisionTreeRegressor")

    // => Fixed parameters
    val trainTestEvaluators: List[String] = RegressionMetricsTypes.regressionMetricsTypesList

    // => Default values
    enableModelSelectionStage = Some(enableModelSelectionStage.getOrElse(true))
    // · Validation technique
    validationTechnique = Some(validationTechnique.getOrElse(ModelSelectionEvaluationTechnique.crossValidation))
    validationMetric = Some(validationMetric.getOrElse(RegressionMetricsTypes.rmse))
    numFolds = Some(numFolds.getOrElse(3))
    trainRatio = Some(trainRatio.getOrElse(0.75))
    seed = Some(seed.getOrElse(util.Random.nextLong))
    parallelism = Some(parallelism.getOrElse(8))
    // · Hold out / Validation dataset splitter
    splitterConf = Some(splitterConf.getOrElse(DataSplitterConf()))
    // · MlModels to train in a regression problem
    modelTypesToUse = Some(modelTypesToUse.getOrElse(
        RegressionMlModelsTypes.regressionMlModelsTypesList.filter(m => !defaultExcludedModels.contains(m))))
    // · Hyper-parameters grid for each MlModel
    modelsAndParameters = Some(modelsAndParameters.getOrElse(
        DefaultMlModelsGrid.regressionModelsAndParams.modelsAndParameters))
    modelsAndParameters = Some(modelsAndParameters.get.filter(m => modelTypesToUse.get.contains(m.mlModelName)))

    // => Validations
    SessionData.assert(ModelSelectionEvaluationTechnique.modelSelectionEvalTechList.contains(validationTechnique.get),
        s"Model selection stage configuration error: " +
            s"'${validationTechnique.get}' is not a valid model selection validation technique")
    SessionData.assert(RegressionMetricsTypes.regressionMetricsTypesList.contains(validationMetric.get),
        s"Model selection stage configuration error: " +
            s"'${validationMetric.get}' is not a valid regression validation metric")
    SessionData.assert(modelTypesToUse.get.forall(RegressionMlModelsTypes.regressionMlModelsTypesList.contains),
        s"Model selection stage configuration error: " +
            s"Invalid regression ml model in 'mlModelList': ${modelTypesToUse.mkString(", ")}; " +
            s"allowed values: ${RegressionMlModelsTypes.regressionMlModelsTypesList.mkString(", ")}")
    SessionData.assert(modelTypesToUse.get.nonEmpty, s"There not exists any Ml model selected.")
    SessionData.assert(modelsAndParameters.get.nonEmpty, s"There not exists any Ml model selected.")
    SessionData.assert(trainRatio.get >= 0 && trainRatio.get <= 1,
        "Model selection stage configuration error: trainRatio property must be defined in range [0,1].")
    SessionData.assert(numFolds.get > 2,
        "Model selection stage configuration error: numFolds property must be greater than 2")
    SessionData.assert(parallelism.get > 0,
        "Model selection stage configuration error: parallelism property must be greater than 0")

    // => Creating hyperparameter grid object
    val hyperparameterGridObject: Seq[(EstimatorType, Array[ParamMap])] = try {
            MlModelsGridDeserializer.getModelsAndParameters(modelsAndParameters.get)
    } catch {
        case e: Throwable => throw ConfigurationException(e.getMessage, e)
    }
}

case class BinaryClassificationModelSelectorConf(
                                                    var enableModelSelectionStage: Option[Boolean] = None,
                                                    var validationTechnique: Option[String] = None,
                                                    var numFolds: Option[Int] = None,
                                                    var trainRatio: Option[Double] = None,
                                                    var seed: Option[Long] = None,
                                                    var parallelism: Option[Int] = None,
                                                    var validationMetric: Option[String] = None,
                                                    var modelTypesToUse: Option[Seq[String]] = None,
                                                    var modelsAndParameters: Option[Seq[MlModelAndGridDescriptor]] = None,
                                                    var splitterConf: Option[DataBalancerConf] = None,
                                                    var stratify: Option[Boolean] = None
                                                ) extends ModelSelectionStageConfig {

    val defaultExcludedModels: Seq[String] = Seq("DecisionTreeClassifier", "NaiveBayes")

    // => Fixed parameters
    val trainTestEvaluators: List[String] = BinaryClassMetricsTypes.binaryClassMetricsTypesList

    // => Default values
    enableModelSelectionStage = Some(enableModelSelectionStage.getOrElse(true))
    // · Validation technique
    validationTechnique = Some(validationTechnique.getOrElse(ModelSelectionEvaluationTechnique.crossValidation))
    validationMetric = Some(validationMetric.getOrElse(BinaryClassMetricsTypes.auROC))
    numFolds = Some(numFolds.getOrElse(3))
    trainRatio = Some(trainRatio.getOrElse(0.75))
    seed = Some(seed.getOrElse(util.Random.nextLong))
    stratify = Some(stratify.getOrElse(false))
    parallelism = Some(parallelism.getOrElse(8))

    // · Hold out / Validation dataset splitter
    splitterConf = Some(splitterConf.getOrElse(DataBalancerConf()))
    // · MlModels to train in a binary classification problem
    modelTypesToUse = Some(modelTypesToUse.getOrElse(
        BinaryClassMlModelsTypes.binaryClassMlModelsTypesList.filter(m => !defaultExcludedModels.contains(m))))
    // · Hyper-parameters grid for each MlModel
    modelsAndParameters = Some(modelsAndParameters.getOrElse(
        DefaultMlModelsGrid.binaryClassModelsAndParams.modelsAndParameters))
    modelsAndParameters = Some(modelsAndParameters.get.filter(m => modelTypesToUse.get.contains(m.mlModelName)))

    // => Validations
    SessionData.assert(ModelSelectionEvaluationTechnique.modelSelectionEvalTechList.contains(validationTechnique.get),
        s"Model selection stage configuration error: " +
            s"'${validationTechnique.get}' is not a valid model selection validation technique")
    SessionData.assert(validationMetric.isDefined &&
        BinaryClassMetricsTypes.binaryClassMetricsTypesList.contains(validationMetric.get),
        s"'${validationMetric.get}' is not a valid binary classification validation metric")
    SessionData.assert(modelTypesToUse.get.forall(BinaryClassMlModelsTypes.binaryClassMlModelsTypesList.contains),
        s"Model selection stage configuration error: " +
        s"Invalid binary classification ml model in 'mlModelList': ${modelTypesToUse.mkString(", ")}; " +
            s"allowed values: ${BinaryClassMlModelsTypes.binaryClassMlModelsTypesList.mkString(", ")}")
    SessionData.assert(modelTypesToUse.get.nonEmpty, s"There not exists any Ml model selected.")
    SessionData.assert(modelsAndParameters.get.nonEmpty, s"There not exists any Ml model selected.")
    SessionData.assert(trainRatio.get >= 0 && trainRatio.get <= 1,
        "Model selection stage configuration error: trainRatio property must be defined in range [0,1].")
    SessionData.assert(numFolds.get > 2,
        "Model selection stage configuration error: numFolds property must be greater than 2")
    SessionData.assert(parallelism.get > 0,
        "Model selection stage configuration error: parallelism property must be greater than 0")

    // => Creating hyperparameter grid object
    val hyperparameterGridObject: Seq[(EstimatorType, Array[ParamMap])] = try {
        MlModelsGridDeserializer.getModelsAndParameters(modelsAndParameters.get)
    } catch {
        case e: Throwable => throw ConfigurationException(e.getMessage, e)
    }
}

case class MultiClassificationModelSelectorConf(
                                                   var enableModelSelectionStage: Option[Boolean] = None,
                                                   var validationTechnique: Option[String] = None,
                                                   var numFolds: Option[Int] = None,
                                                   var trainRatio: Option[Double] = None,
                                                   var seed: Option[Long] = None,
                                                   var parallelism: Option[Int] = None,
                                                   var validationMetric: Option[String] = None,
                                                   var modelTypesToUse: Option[Seq[String]] = None,
                                                   var modelsAndParameters: Option[Seq[MlModelAndGridDescriptor]] = None,
                                                   var splitterConf: Option[DataCutterConf] = None,
                                                   var stratify: Option[Boolean] = Some(false)
                                               ) extends ModelSelectionStageConfig {

    val defaultExcludedModels: Seq[String] = Seq("DecisionTreeClassifier", "NaiveBayes")

    // => Fixed parameters
    val trainTestEvaluators: List[String] = MultiClassMetricsTypes.multiClassMetricsTypesList

    // => Default values
    enableModelSelectionStage = Some(enableModelSelectionStage.getOrElse(true))
    // - Validation technique
    validationTechnique = Some(validationTechnique.getOrElse(ModelSelectionEvaluationTechnique.crossValidation))
    validationMetric = Some(validationMetric.getOrElse(MultiClassMetricsTypes.f1))
    numFolds = Some(numFolds.getOrElse(3))
    trainRatio = Some(trainRatio.getOrElse(0.75))
    seed = Some(seed.getOrElse(util.Random.nextLong))
    stratify = Some(stratify.getOrElse(false))
    parallelism = Some(parallelism.getOrElse(8))
    // - Hold out / Validation dataset splitter
    splitterConf = Some(splitterConf.getOrElse(DataCutterConf()))
    // - MlModels to train in a multi-class classification problem
    modelTypesToUse = Some(modelTypesToUse.getOrElse(
        MultiClassMlModelsTypes.multiClassMlModelsTypesList.filter(m => !defaultExcludedModels.contains(m))))
    // · Hyper-parameters grid for each MlModel
    modelsAndParameters = Some(modelsAndParameters.getOrElse(
        DefaultMlModelsGrid.multiclassClassModelsAndParams.modelsAndParameters))
    modelsAndParameters = Some(modelsAndParameters.get.filter(m => modelTypesToUse.get.contains(m.mlModelName)))

    // => Validations
    SessionData.assert(ModelSelectionEvaluationTechnique.modelSelectionEvalTechList.contains(validationTechnique.get),
        s"Model selection stage configuration error: " +
            s"'${validationTechnique.get}' is not a valid model selection validation technique")
    SessionData.assert(validationMetric.isDefined &&
        MultiClassMetricsTypes.multiClassMetricsTypesList.contains(validationMetric.get),
        s"'${validationMetric.get}' is not a valid multi-class classification validation metric")
    SessionData.assert(modelTypesToUse.get.forall(MultiClassMlModelsTypes.multiClassMlModelsTypesList.contains),
        s"Invalid multi-class classification ml model in 'mlModelList': ${modelTypesToUse.mkString(", ")}; " +
            s"allowed values: ${MultiClassMlModelsTypes.multiClassMlModelsTypesList.mkString(", ")}")
    SessionData.assert(modelTypesToUse.get.nonEmpty, s"There not exists any Ml model selected.")
    SessionData.assert(modelsAndParameters.get.nonEmpty, s"There not exists any Ml model selected.")
    SessionData.assert(trainRatio.get >= 0 && trainRatio.get <= 1,
        "Model selection stage configuration error: trainRatio property must be defined in range [0,1].")
    SessionData.assert(numFolds.get > 2,
        "Model selection stage configuration error: numFolds property must be greater than 2")
    SessionData.assert(parallelism.get > 0,
        "Model selection stage configuration error: parallelism property must be greater than 0")

    // => Creating hyperparameter grid object
    val hyperparameterGridObject: Seq[(EstimatorType, Array[ParamMap])] = try {
        MlModelsGridDeserializer.getModelsAndParameters(modelsAndParameters.get)
    } catch {
        case e: Throwable => throw ConfigurationException(e.getMessage, e)
    }
}

// · Train and holdOut data splitter configuration
case class DataSplitterConf(
                               var seed: Option[Long] = Some(42L),
                               var reserveTestFraction: Option[Double] = Some(0.1)
                           ) {
    // => Default values
    seed = Some(seed.getOrElse(42L))
    reserveTestFraction = Some(reserveTestFraction.getOrElse(0.1))

    // => Assertions
    SessionData.assert(reserveTestFraction.get > 0 && reserveTestFraction.get <= 1,
        s"Model selection stage configuration error: 'reserveTestFraction' parameter must be in range ]0, 1]")
}

case class DataBalancerConf(
                               var seed: Option[Long] = Some(42L),
                               var reserveTestFraction: Option[Double] = Some(0.1),
                               var enableBalancing: Option[Boolean] = Some(false),
                               var maxTrainingSample: Option[Int] = Some(1E6.toInt),
                               var sampleFraction: Option[Double] = Some(0.1)
                           ) {
    // => Default values
    seed = Some(seed.getOrElse(42L))
    reserveTestFraction = Some(reserveTestFraction.getOrElse(0.1))
    enableBalancing = Some(enableBalancing.getOrElse(false))
    maxTrainingSample = Some(maxTrainingSample.getOrElse(1E6.toInt))
    sampleFraction = Some(sampleFraction.getOrElse(0.1))

    // => Validations
    SessionData.assert(reserveTestFraction.get > 0 && reserveTestFraction.get <= 1,
        s"Model selection stage configuration error: 'reserveTestFraction' parameter must be in range ]0, 1]")
    SessionData.assert(maxTrainingSample.get > 0,
        s"Model selection stage configuration error: 'maxTrainingSample' parameter must be greater than 0")
    SessionData.assert(sampleFraction.get > 0 && sampleFraction.get <= 1,
        s"Model selection stage configuration error: 'sampleFraction' parameter must be in range ]0, 1]")
}

case class DataCutterConf(
                             var seed: Option[Long] = Some(42L),
                             var reserveTestFraction: Option[Double] = Some(0.1),
                             var filterCategories: Option[Boolean] = Some(false),
                             var maxLabelCategories: Option[Int] = Some(100),
                             var minLabelFraction: Option[Double] = Some(0.0)
                         ) {
    // => Default values
    seed = Some(seed.getOrElse(42L))
    reserveTestFraction = Some(reserveTestFraction.getOrElse(0.1))
    filterCategories = Some(filterCategories.getOrElse(false))
    maxLabelCategories = Some(maxLabelCategories.getOrElse(100))
    minLabelFraction = Some(minLabelFraction.getOrElse(0.0))

    // => Validations
    SessionData.assert(reserveTestFraction.get > 0 && reserveTestFraction.get <= 1,
        s"Model selection stage configuration error: 'reserveTestFraction' parameter must be in range ]0, 1]")
    SessionData.assert(maxLabelCategories.get > 0,
        s"Model selection stage configuration error: 'maxLabelCategories' parameter must be greater than 0")
    SessionData.assert(minLabelFraction.get >= 0.0 && minLabelFraction.get <= 1.0,
        s"Model selection stage configuration error: 'minLabelFraction' parameter must be in range [0, 1]")
}
