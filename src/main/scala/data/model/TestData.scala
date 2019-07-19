package data.model

case class TestData(
                     id:Long,
                     name:String,
                     surname:Option[String] = None
                   )
