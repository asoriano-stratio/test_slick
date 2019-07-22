package data.model.configuration.session.modelselection

import com.salesforce.op.evaluators.Evaluators


// --------------------------------------------------
//   Evaluation technique used in model selection
// --------------------------------------------------

object ModelSelectionEvaluationTechnique{
    val crossValidation = "crossValidation"
    val trainValidationSplit = "trainValidationSplit"

    val modelSelectionEvalTechList = List(crossValidation, trainValidationSplit)
}


// ----------------------------------
//   Evaluation metrics
// ----------------------------------

object RegressionMetricsTypes {
    val rmse = "rmse"
    val mse = "mse"
    val mae = "mae"
    val r2 = "r2"
    val regressionMetricsTypesList: List[String] = List(rmse, mse, mae, r2)

    def getRegressionEvaluator(validationMetric:Option[String]) = {
        validationMetric match {
            case Some("rmse") => Evaluators.Regression.rmse()
            case Some("mse") => Evaluators.Regression.mse()
            case Some("mae") => Evaluators.Regression.mae()
            case Some("r2") => Evaluators.Regression.r2()
            case Some(x) => throw new Exception(s"'$x' is not a valid regression validation metric")
            case None => Evaluators.Regression.rmse()
        }
    }

}
object BinaryClassMetricsTypes {
    val f1 = "f1"
    val auPR = "auPR"
    val auROC = "auROC"
    val error = "error"
    val precision = "precision"
    val recall = "recall"
    val binaryClassMetricsTypesList: List[String] = List(f1, auPR, auROC, error, precision, recall)

    def getBinaryClassEvaluator(validationMetric:Option[String]) = {
        validationMetric match {
            case Some("f1") => Evaluators.BinaryClassification.f1()
            case Some("auPR") => Evaluators.BinaryClassification.auPR()
            case Some("auROC") => Evaluators.BinaryClassification.auROC()
            case Some("error") => Evaluators.BinaryClassification.error()
            case Some("precision") => Evaluators.BinaryClassification.precision()
            case Some("recall") => Evaluators.BinaryClassification.recall()
            case Some(x) => throw new Exception(s"'$x' is not a valid binary classification validation metric")
            case None => Evaluators.BinaryClassification.auROC()
        }
    }
}
object MultiClassMetricsTypes {
    val precision = "precision"
    val recall = "recall"
    val f1 = "f1"
    val error = "error"
    val multiClassMetricsTypesList: List[String] = List(precision, recall, f1, error)

    def getMultiClassEvaluator(validationMetric:Option[String]) = {
        validationMetric match {
            case Some("precision") => Evaluators.MultiClassification.precision()
            case Some("recall") => Evaluators.MultiClassification.recall()
            case Some("f1") => Evaluators.MultiClassification.f1()
            case Some("error") => Evaluators.MultiClassification.error()
            case Some(x) => throw new Exception(s"'$x' is not a valid multi-class classifier validation metric")
            case None => Evaluators.MultiClassification.f1()
        }
    }
}