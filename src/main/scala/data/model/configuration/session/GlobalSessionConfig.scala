package data.model.configuration.session

case class GlobalSessionConfig(
                                  var maxValuesCategorical: Option[Int] = None
                              ){

    // => Default value
    maxValuesCategorical = Some(maxValuesCategorical.getOrElse(30))

    // => Validations
    SessionData.assert(maxValuesCategorical.get > 1,
        "Global session parameter 'maxValuesCategorical' must be greater than 1")

    def MaxValuesCategorical: Int = maxValuesCategorical.get
}