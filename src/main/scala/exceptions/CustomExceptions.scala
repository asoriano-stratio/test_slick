package exceptions

case class ConfigurationException(private val message: String = "", private val cause: Throwable = None.orNull
                                 ) extends Exception(message, cause)

case class ValidationException(private val message: String = "", private val cause: Throwable = None.orNull
                              ) extends Exception(message, cause)
