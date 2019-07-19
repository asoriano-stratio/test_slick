
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.Database

/**
  * Ver:
  *     https://github.com/tminglei/slick-pg
  *     https://github.com/tminglei/slick-pg/blob/master/addons/json4s/src/test/scala/com/github/tminglei/slickpg/PgJson4sSupportSuite.scala
  *
  */


object TestSlick extends App{

    val db: JdbcBackend.Database = Database.forConfig("mydb")


}
