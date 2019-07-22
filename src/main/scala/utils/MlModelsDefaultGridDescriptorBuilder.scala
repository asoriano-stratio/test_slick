package utils

import com.salesforce.op.stages.impl.classification._
import com.salesforce.op.stages.impl.regression._
import com.salesforce.op.stages.impl.selector.DefaultSelectorParams
import org.apache.spark.ml.tuning.ParamGridBuilder
import org.json4s._
import org.json4s.jackson.Serialization.writePretty


object MlModelsDefaultGridDescriptorBuilder extends App {

    implicit val formats: DefaultFormats.type = DefaultFormats

    def serializeValues(values:Array[_]): Array[String] =values.map(_.toString)

    // ----------------------------------------------------------------------------
    // => Binary classification model selection default models and grids
    // ----------------------------------------------------------------------------

    // Note: See com.salesforce.op.stages.impl.classification.BinaryClassificationModelSelector.defaultModelsAndParams

    val lr = new OpLogisticRegression()
    val lrParams = new ParamGridBuilder()
        .addGrid(lr.fitIntercept, DefaultSelectorParams.FitIntercept)
        .addGrid(lr.elasticNetParam, DefaultSelectorParams.ElasticNet)
        .addGrid(lr.maxIter, DefaultSelectorParams.MaxIterLin)
        .addGrid(lr.regParam, DefaultSelectorParams.Regularization)
        .addGrid(lr.standardization, DefaultSelectorParams.Standardized)
        .addGrid(lr.tol, DefaultSelectorParams.Tol)
        .build()
    val lrDescriptor =
        MlModelAndGridDescriptor(
            mlModelName = MlModelsGridDeserializer.classToNameMapper(lr.getClass.getName),
            hyperparameterGrid = Seq(
                HyperparameterValuesDescriptor(name = lr.fitIntercept.name, values = serializeValues(DefaultSelectorParams.FitIntercept)),
                HyperparameterValuesDescriptor(name = lr.elasticNetParam.name, values = serializeValues(DefaultSelectorParams.ElasticNet)),
                HyperparameterValuesDescriptor(name = lr.maxIter.name, values = serializeValues(DefaultSelectorParams.MaxIterLin)),
                HyperparameterValuesDescriptor(name = lr.regParam.name, values = serializeValues(DefaultSelectorParams.Regularization)),
                HyperparameterValuesDescriptor(name = lr.standardization.name, values = serializeValues(DefaultSelectorParams.Standardized)),
                HyperparameterValuesDescriptor(name = lr.tol.name, values = serializeValues(DefaultSelectorParams.Tol))
            )
        )


    val rf = new OpRandomForestClassifier()
    val rfParams = new ParamGridBuilder()
        .addGrid(rf.maxDepth, DefaultSelectorParams.MaxDepth)
        .addGrid(rf.impurity, DefaultSelectorParams.ImpurityClass)
        .addGrid(rf.maxBins, DefaultSelectorParams.MaxBin)
        .addGrid(rf.minInfoGain, DefaultSelectorParams.MinInfoGain)
        .addGrid(rf.minInstancesPerNode, DefaultSelectorParams.MinInstancesPerNode)
        .addGrid(rf.numTrees, DefaultSelectorParams.MaxTrees)
        .addGrid(rf.subsamplingRate, DefaultSelectorParams.SubsampleRate)
        .build()
    val rfDescriptor =
        MlModelAndGridDescriptor(
            mlModelName = MlModelsGridDeserializer.classToNameMapper(rf.getClass.getName),
            hyperparameterGrid = Seq(
                HyperparameterValuesDescriptor(name = rf.maxDepth.name, values = serializeValues(DefaultSelectorParams.MaxDepth)),
                HyperparameterValuesDescriptor(name = rf.impurity.name, values = serializeValues(DefaultSelectorParams.ImpurityClass)),
                HyperparameterValuesDescriptor(name = rf.maxBins.name, values = serializeValues(DefaultSelectorParams.MaxBin)),
                HyperparameterValuesDescriptor(name = rf.minInfoGain.name, values = serializeValues(DefaultSelectorParams.MinInfoGain)),
                HyperparameterValuesDescriptor(name = rf.minInstancesPerNode.name, values = serializeValues(DefaultSelectorParams.MinInstancesPerNode)),
                HyperparameterValuesDescriptor(name = rf.numTrees.name, values = serializeValues(DefaultSelectorParams.MaxTrees)),
                HyperparameterValuesDescriptor(name = rf.subsamplingRate.name, values = serializeValues(DefaultSelectorParams.SubsampleRate))
            )
        )

    val gbt = new OpGBTClassifier()
    val gbtParams = new ParamGridBuilder()
        .addGrid(gbt.maxDepth, DefaultSelectorParams.MaxDepth)
        .addGrid(gbt.impurity, DefaultSelectorParams.ImpurityClass)
        .addGrid(gbt.maxBins, DefaultSelectorParams.MaxBin)
        .addGrid(gbt.minInfoGain, DefaultSelectorParams.MinInfoGain)
        .addGrid(gbt.minInstancesPerNode, DefaultSelectorParams.MinInstancesPerNode)
        .addGrid(gbt.maxIter, DefaultSelectorParams.MaxIterTree)
        .addGrid(gbt.subsamplingRate, DefaultSelectorParams.SubsampleRate)
        .addGrid(gbt.stepSize, DefaultSelectorParams.StepSize)
        .build()
    val gbtDescriptor =
        MlModelAndGridDescriptor(
            mlModelName = MlModelsGridDeserializer.classToNameMapper(gbt.getClass.getName),
            hyperparameterGrid = Seq(
                HyperparameterValuesDescriptor(name = gbt.maxDepth.name, values = serializeValues(DefaultSelectorParams.MaxDepth)),
                HyperparameterValuesDescriptor(name = gbt.impurity.name, values = serializeValues(DefaultSelectorParams.ImpurityClass)),
                HyperparameterValuesDescriptor(name = gbt.maxBins.name, values = serializeValues(DefaultSelectorParams.MaxBin)),
                HyperparameterValuesDescriptor(name = gbt.minInfoGain.name, values = serializeValues(DefaultSelectorParams.MinInfoGain)),
                HyperparameterValuesDescriptor(name = gbt.minInstancesPerNode.name, values = serializeValues(DefaultSelectorParams.MinInstancesPerNode)),
                HyperparameterValuesDescriptor(name = gbt.maxIter.name, values = serializeValues(DefaultSelectorParams.MaxIterTree)),
                HyperparameterValuesDescriptor(name = gbt.subsamplingRate.name, values = serializeValues(DefaultSelectorParams.SubsampleRate)),
                HyperparameterValuesDescriptor(name = gbt.stepSize.name, values = serializeValues(DefaultSelectorParams.StepSize))
            )
        )

    val svc = new OpLinearSVC()
    val svcParams = new ParamGridBuilder()
        .addGrid(svc.regParam, DefaultSelectorParams.Regularization)
        .addGrid(svc.maxIter, DefaultSelectorParams.MaxIterLin)
        .addGrid(svc.fitIntercept, DefaultSelectorParams.FitIntercept)
        .addGrid(svc.tol, DefaultSelectorParams.Tol)
        .addGrid(svc.standardization, DefaultSelectorParams.Standardized)
        .build()
    val svcDescriptor =
        MlModelAndGridDescriptor(
            mlModelName = MlModelsGridDeserializer.classToNameMapper(svc.getClass.getName),
            hyperparameterGrid = Seq(
                HyperparameterValuesDescriptor(name = svc.regParam.name, values = serializeValues(DefaultSelectorParams.Regularization)),
                HyperparameterValuesDescriptor(name = svc.maxIter.name, values = serializeValues(DefaultSelectorParams.MaxIterLin)),
                HyperparameterValuesDescriptor(name = svc.fitIntercept.name, values = serializeValues(DefaultSelectorParams.FitIntercept)),
                HyperparameterValuesDescriptor(name = svc.tol.name, values = serializeValues(DefaultSelectorParams.Tol)),
                HyperparameterValuesDescriptor(name = svc.standardization.name, values = serializeValues(DefaultSelectorParams.Standardized))
            )
        )


    val nb = new OpNaiveBayes()
    val nbParams = new ParamGridBuilder()
        .addGrid(nb.smoothing, DefaultSelectorParams.NbSmoothing)
        .build()
    val nbDescriptor =
        MlModelAndGridDescriptor(
            mlModelName = MlModelsGridDeserializer.classToNameMapper(nb.getClass.getName),
            hyperparameterGrid = Seq(
                HyperparameterValuesDescriptor(name = nb.smoothing.name, values = serializeValues(DefaultSelectorParams.NbSmoothing))
            )
        )

    val dt = new OpDecisionTreeClassifier()
    val dtParams = new ParamGridBuilder()
        .addGrid(dt.maxDepth, DefaultSelectorParams.MaxDepth)
        .addGrid(dt.impurity, DefaultSelectorParams.ImpurityClass)
        .addGrid(dt.maxBins, DefaultSelectorParams.MaxBin)
        .addGrid(dt.minInfoGain, DefaultSelectorParams.MinInfoGain)
        .addGrid(dt.minInstancesPerNode, DefaultSelectorParams.MinInstancesPerNode)
        .build()
    val dtDescriptor =
        MlModelAndGridDescriptor(
            mlModelName = MlModelsGridDeserializer.classToNameMapper(dt.getClass.getName),
            hyperparameterGrid = Seq(
                HyperparameterValuesDescriptor(name = dt.maxDepth.name, values = serializeValues(DefaultSelectorParams.MaxDepth)),
                HyperparameterValuesDescriptor(name = dt.impurity.name, values = serializeValues(DefaultSelectorParams.ImpurityClass)),
                HyperparameterValuesDescriptor(name = dt.maxBins.name, values = serializeValues(DefaultSelectorParams.MaxBin)),
                HyperparameterValuesDescriptor(name = dt.minInfoGain.name, values = serializeValues(DefaultSelectorParams.MinInfoGain)),
                HyperparameterValuesDescriptor(name = dt.minInstancesPerNode.name, values = serializeValues(DefaultSelectorParams.MinInstancesPerNode))
            )
        )


    val binClassModelGrids =
        Seq(lr -> lrParams, rf -> rfParams, gbt -> gbtParams, svc -> svcParams, nb -> nbParams, dt -> dtParams)


    // Definition of descriptor classes
    val binaryClassificationGridDescriptor = ModelsAndHyperparamGridsDescriptor(
        Seq(lrDescriptor, rfDescriptor, gbtDescriptor, svcDescriptor, nbDescriptor, dtDescriptor)
    )

    val binaryClassificationGridDescriptorJson: String = writePretty(binaryClassificationGridDescriptor)

    println(binaryClassificationGridDescriptorJson)


    // ----------------------------------------------------------------------------
    // => Multi-class classification model selection default models and grids
    // ----------------------------------------------------------------------------

    // Note: See com.salesforce.op.stages.impl.classification.MultiClassificationModelSelector.defaultModelsAndParams

    val mclr = new OpLogisticRegression()
    val mclrParams = new ParamGridBuilder()
        .addGrid(mclr.fitIntercept, DefaultSelectorParams.FitIntercept)
        .addGrid(mclr.maxIter, DefaultSelectorParams.MaxIterLin)
        .addGrid(mclr.regParam, DefaultSelectorParams.Regularization)
        .addGrid(mclr.elasticNetParam, DefaultSelectorParams.ElasticNet)
        .addGrid(mclr.standardization, DefaultSelectorParams.Standardized)
        .addGrid(mclr.tol, DefaultSelectorParams.Tol)
        .build()
    val mclrDescriptor =
        MlModelAndGridDescriptor(
            mlModelName = MlModelsGridDeserializer.classToNameMapper(mclr.getClass.getName),
            hyperparameterGrid = Seq(
                HyperparameterValuesDescriptor(name = mclr.fitIntercept.name, values = serializeValues(DefaultSelectorParams.FitIntercept)),
                HyperparameterValuesDescriptor(name = mclr.maxIter.name, values = serializeValues(DefaultSelectorParams.MaxIterLin)),
                HyperparameterValuesDescriptor(name = mclr.regParam.name, values = serializeValues(DefaultSelectorParams.Regularization)),
                HyperparameterValuesDescriptor(name = mclr.elasticNetParam.name, values = serializeValues(DefaultSelectorParams.ElasticNet)),
                HyperparameterValuesDescriptor(name = mclr.standardization.name, values = serializeValues(DefaultSelectorParams.Standardized)),
                HyperparameterValuesDescriptor(name = mclr.tol.name, values = serializeValues(DefaultSelectorParams.Tol))
            )
        )


    val mcrf = new OpRandomForestClassifier()
    val mcrfParams = new ParamGridBuilder()
        .addGrid(mcrf.maxDepth, DefaultSelectorParams.MaxDepth)
        .addGrid(mcrf.impurity, DefaultSelectorParams.ImpurityClass)
        .addGrid(mcrf.maxBins, DefaultSelectorParams.MaxBin)
        .addGrid(mcrf.minInfoGain, DefaultSelectorParams.MinInfoGain)
        .addGrid(mcrf.minInstancesPerNode, DefaultSelectorParams.MinInstancesPerNode)
        .addGrid(mcrf.numTrees, DefaultSelectorParams.MaxTrees)
        .addGrid(mcrf.subsamplingRate, DefaultSelectorParams.SubsampleRate)
        .build()

    val mcnb = new OpNaiveBayes()
    val mcnbParams = new ParamGridBuilder()
        .addGrid(mcnb.smoothing, DefaultSelectorParams.NbSmoothing)
        .build()

    val mcdt = new OpDecisionTreeClassifier()
    val mcdtParams = new ParamGridBuilder()
        .addGrid(mcdt.maxDepth, DefaultSelectorParams.MaxDepth)
        .addGrid(mcdt.impurity, DefaultSelectorParams.ImpurityClass)
        .addGrid(mcdt.maxBins, DefaultSelectorParams.MaxBin)
        .addGrid(mcdt.minInfoGain, DefaultSelectorParams.MinInfoGain)
        .addGrid(mcdt.minInstancesPerNode, DefaultSelectorParams.MinInstancesPerNode)
        .build()

    val multiClassModelGrids =
        Seq(mclr -> mclrParams, mcrf -> mcrfParams, mcnb -> mcnbParams, mcdt -> mcdtParams)


    // ----------------------------------------------------------------------------
    // => Regression model selection default models and grids
    // ----------------------------------------------------------------------------

    // Note: See com.salesforce.op.stages.impl.regression.RegressionModelSelector.defaultModelsAndParams
    val rmlr = new OpLinearRegression()
    val rmlrParams = new ParamGridBuilder()
        .addGrid(rmlr.fitIntercept, DefaultSelectorParams.FitIntercept)
        .addGrid(rmlr.elasticNetParam, DefaultSelectorParams.ElasticNet)
        .addGrid(rmlr.maxIter, DefaultSelectorParams.MaxIterLin)
        .addGrid(rmlr.regParam, DefaultSelectorParams.Regularization)
        .addGrid(rmlr.solver, DefaultSelectorParams.RegSolver)
        .addGrid(rmlr.standardization, DefaultSelectorParams.Standardized)
        .addGrid(rmlr.tol, DefaultSelectorParams.Tol)
        .build()

    val rmrf = new OpRandomForestRegressor()
    val rmrfParams = new ParamGridBuilder()
        .addGrid(rmrf.maxDepth, DefaultSelectorParams.MaxDepth)
        .addGrid(rmrf.maxBins, DefaultSelectorParams.MaxBin)
        .addGrid(rmrf.minInfoGain, DefaultSelectorParams.MinInfoGain)
        .addGrid(rmrf.minInstancesPerNode, DefaultSelectorParams.MinInstancesPerNode)
        .addGrid(rmrf.numTrees, DefaultSelectorParams.MaxTrees)
        .addGrid(rmrf.subsamplingRate, DefaultSelectorParams.SubsampleRate)
        .build()

    val rmgbt = new OpGBTRegressor()
    val rmgbtParams = new ParamGridBuilder()
        .addGrid(rmgbt.lossType, DefaultSelectorParams.TreeLossType)
        .addGrid(rmgbt.maxDepth, DefaultSelectorParams.MaxDepth)
        .addGrid(rmgbt.maxBins, DefaultSelectorParams.MaxBin)
        .addGrid(rmgbt.minInfoGain, DefaultSelectorParams.MinInfoGain)
        .addGrid(rmgbt.minInstancesPerNode, DefaultSelectorParams.MinInstancesPerNode)
        .addGrid(rmgbt.maxIter, DefaultSelectorParams.MaxIterTree)
        .addGrid(rmgbt.subsamplingRate, DefaultSelectorParams.SubsampleRate)
        .addGrid(rmgbt.stepSize, DefaultSelectorParams.StepSize)
        .build()

    val rmdt = new OpDecisionTreeRegressor()
    val rmdtParams = new ParamGridBuilder()
        .addGrid(rmdt.maxDepth, DefaultSelectorParams.MaxDepth)
        .addGrid(rmdt.maxBins, DefaultSelectorParams.MaxBin)
        .addGrid(rmdt.minInfoGain, DefaultSelectorParams.MinInfoGain)
        .addGrid(rmdt.minInstancesPerNode, DefaultSelectorParams.MinInstancesPerNode)
        .build()

    val rmglr = new OpGeneralizedLinearRegression()
    val rmglrParams = new ParamGridBuilder()
        .addGrid(rmglr.fitIntercept, DefaultSelectorParams.FitIntercept)
        .addGrid(rmglr.family, DefaultSelectorParams.DistFamily)
        .addGrid(rmglr.maxIter, DefaultSelectorParams.MaxIterLin)
        .addGrid(rmglr.regParam, DefaultSelectorParams.Regularization)
        .addGrid(rmglr.tol, DefaultSelectorParams.Tol)
        .build()

    Seq(rmlr -> rmlrParams, rmrf -> rmrfParams, rmgbt -> rmgbtParams, rmdt -> rmdtParams, rmglr -> rmglrParams)


}
