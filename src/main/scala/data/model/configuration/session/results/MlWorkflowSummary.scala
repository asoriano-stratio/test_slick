package data.model.configuration.session.results

case class MlWorkflowSummary(
                                var rawDataAnalysisReducedSummary:Option[RawDataAnalysisReducedSummary] = None,
                                var featureEngineeringReducedSummary:Option[FeatureEngineeringReducedSummary] = None,
                                var featureAnalysisReducedSummary:Option[FeatureAnalysisReducedSummary] = None,
                                var modelSelectionReducedSummary:Option[ModelSelectionReducedSummary] = None
                            )
