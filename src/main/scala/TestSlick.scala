
import data.model.TestData
import org.slf4j.{Logger, LoggerFactory}
import repository.dao.PostgressRepository
import slick.jdbc.JdbcBackend

/**
  * Ver:
  * https://github.com/tminglei/slick-pg
  * https://github.com/tminglei/slick-pg/blob/master/addons/json4s/src/test/scala/com/github/tminglei/slickpg/PgJson4sSupportSuite.scala
  *
  */


object TestSlick extends App {

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  // => Database connection
  val db: JdbcBackend.Database = JdbcBackend.Database.forConfig("mydb")

  val exampleRepository = new PostgressRepository(db, Some("test_slick_pg"))

  // => Schema and tables creation
  exampleRepository.initialization()

  // => Inserting some data
  exampleRepository.insertData(TestData(1, "Antonio"))
  exampleRepository.insertData(TestData(2, "Antonio", Some("Soriano")))
  exampleRepository.insertData(Seq(TestData(3, "a"),TestData(4, "a")))

  // => Getting all data
  exampleRepository.getAllData.foreach(println)
  exampleRepository.getAllData2.foreach(println)
  exampleRepository.getAllNames.foreach(println)
}
