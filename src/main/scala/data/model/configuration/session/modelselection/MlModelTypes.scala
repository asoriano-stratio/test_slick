package data.model.configuration.session.modelselection

import com.salesforce.op.stages.impl.classification.{BinaryClassificationModelsToTry, MultiClassClassificationModelsToTry}
import com.salesforce.op.stages.impl.regression.RegressionModelsToTry

// ----------------------------------
//   ML problems types
// ----------------------------------
object MlModelTypes {
    val regression = "regression"
    val binaryClassification = "binary_classification"
    val multiClassification = "multi_classification"
    val mlProblemTypeList = List(
        MlModelTypes.regression, MlModelTypes.binaryClassification, MlModelTypes.multiClassification)
}

// ----------------------------------
//   ML algorithms
// ----------------------------------
object RegressionMlModelsTypes {

    val RandomForestRegressor = "RandomForestRegressor"
    val LinearRegression = "LinearRegression"
    val DecisionTreeRegressor = "DecisionTreeRegressor"
    val GBTRegressor = "GBTRegressor"
    val GeneralizedLinearRegression = "GeneralizedLinearRegression"
    val regressionMlModelsTypesList: Seq[String] = Seq(
        RandomForestRegressor, LinearRegression, DecisionTreeRegressor, GBTRegressor, GeneralizedLinearRegression)

    def getRegressionModelsToTry(models:Seq[String]): Seq[RegressionModelsToTry] ={
        models.map {
            case "RandomForestRegressor" => RegressionModelsToTry.OpRandomForestRegressor
            case "LinearRegression" => RegressionModelsToTry.OpLinearRegression
            case "DecisionTreeRegressor" => RegressionModelsToTry.OpDecisionTreeRegressor
            case "GBTRegressor" => RegressionModelsToTry.OpGBTRegressor
            case "GeneralizedLinearRegression" => RegressionModelsToTry.OpGeneralizedLinearRegression
        }
    }

}
object BinaryClassMlModelsTypes {

    val LogisticRegression = "LogisticRegression"
    val RandomForestClassifier = "RandomForestClassifier"
    val GBTClassifier = "GBTClassifier"
    val LinearSVC = "LinearSVC"
    val DecisionTreeClassifier = "DecisionTreeClassifier"
    val NaiveBayes = "NaiveBayes"

    val binaryClassMlModelsTypesList: Seq[String] = Seq(
        LogisticRegression, RandomForestClassifier, GBTClassifier, LinearSVC, DecisionTreeClassifier, NaiveBayes)

    def getBinaryClassModelsToTry(models:Seq[String]): Seq[BinaryClassificationModelsToTry] ={
        models.map {
            case LogisticRegression => BinaryClassificationModelsToTry.OpLogisticRegression
            case RandomForestClassifier => BinaryClassificationModelsToTry.OpRandomForestClassifier
            case GBTClassifier => BinaryClassificationModelsToTry.OpGBTClassifier
            case LinearSVC => BinaryClassificationModelsToTry.OpLinearSVC
            case DecisionTreeClassifier => BinaryClassificationModelsToTry.OpDecisionTreeClassifier
            case NaiveBayes => BinaryClassificationModelsToTry.OpNaiveBayes
        }
    }
}
object MultiClassMlModelsTypes {

    val LogisticRegression = "LogisticRegression"
    val RandomForestClassifier = "RandomForestClassifier"
    val DecisionTreeClassifier = "DecisionTreeClassifier"
    val NaiveBayes = "NaiveBayes"

    val multiClassMlModelsTypesList: Seq[String] = Seq(
        LogisticRegression, RandomForestClassifier, DecisionTreeClassifier, NaiveBayes
    )

    def getMultiClassModelsToTry(models:Seq[String]): Seq[MultiClassClassificationModelsToTry] ={
        models.map {
            case "LogisticRegression" => MultiClassClassificationModelsToTry.OpLogisticRegression
            case "RandomForestClassifier" =>  MultiClassClassificationModelsToTry.OpRandomForestClassifier
            case "DecisionTreeClassifier" => MultiClassClassificationModelsToTry.OpDecisionTreeClassifier
            case "NaiveBayes" => MultiClassClassificationModelsToTry.OpNaiveBayes
        }
    }
}
