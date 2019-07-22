package repository.dao

import data.model.TestData
import data.model.configuration.session.{MlWorkflowJobData, SessionData, SessionStatus}
import org.slf4j.{Logger, LoggerFactory}
import repository.CustomPostgresProfile
import slick.jdbc.JdbcBackend
import slick.jdbc.meta.MTable
import slick.lifted.ProvenShape

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}


object PostgressRepository {

}

class PostgressRepository(
                           db: JdbcBackend.Database,
                           schemaName: Option[String] = Some("default")
                         ) {

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  val profile: CustomPostgresProfile.type = CustomPostgresProfile
  import profile.api._

  // => Table definition
  class TestTable(tag: Tag) extends Table[TestData](tag, schemaName, "test_table") {

    // This is the primary key column:
    def id: Rep[Long] = column[Long]("ID", O.PrimaryKey)

    def name: Rep[String] = column[String]("NAME")

    def surname: Rep[Option[String]] = column[Option[String]]("SURNAME")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, name, surname) <> ((TestData.apply _).tupled, TestData.unapply)

  }
  lazy val test = TableQuery[TestTable]

  class SessionTable(tag: Tag) extends Table[SessionData](tag, schemaName, "sessionTable") {

    def sessionId: Rep[String] = column[String]("sessionId", O.PrimaryKey)

    def timestamp: Rep[Option[Long]] = column[Option[Long]]("timestamp")

    def name: Rep[Option[String]] = column[Option[String]]("name")

    def description: Rep[Option[String]] = column[Option[String]]("description")

    def sessionStatus: Rep[SessionStatus] = column[SessionStatus]("sessionStatus")

    def jobConfiguration: Rep[MlWorkflowJobData] = column[MlWorkflowJobData]("jobConfiguration")

    def * : ProvenShape[SessionData] = (sessionId, timestamp, name, description, sessionStatus, jobConfiguration) <> ((SessionData.apply _).tupled, SessionData.unapply)
  }
  lazy val sessionTable = TableQuery[SessionTable]

  val tables = Map("sessionTable" -> sessionTable, "test_table" -> test)


  def initialization(): Unit = {
    createSchema(schemaName.getOrElse("default"))
    tableCreation("test_table")
    tableCreation("sessionTable")
  }

  /** ************************************************************************
    * DDL is Data Definition Language : it is used to define data structures.
    ************************************************************************** */

  private def createSchema(schemaName: String = "default"): Unit = {
    val createSchemaSql = s"""create schema if not exists "$schemaName";"""
    Try(Await.result(db.run(sqlu"#$createSchemaSql"), 20 seconds)) match {
      case Success(_) =>
        log.info(s"Schema $schemaName created if not exists")
      case Failure(e) =>
        throw e
    }
  }

  private def tableCreation(name: String): Unit = {
    Try(Await.result(db.run(MTable.getTables), 20 seconds)) match {
      case Success(result) =>
        val exists = result.exists(mTable => mTable.name.name == name && mTable.name.schema == schemaName)
        if (exists)
          log.info(s"Table $name already exists: skipping creation")
        else doCreateTable(name)
      case Failure(ex) =>
        log.error(ex.getLocalizedMessage, ex)
        throw ex
    }
  }

  private def doCreateTable(name: String): Unit = {
    log.info(s"Creating table $name")
    val action = tables(name).schema.create.transactionally

    Try(Await.result(db.run(action), 20 seconds)) match {
      case Success(_) =>
        log.info(s"Table $name created correctly")
      case Failure(e) =>
        throw e
    }
  }

  /** ************************************************************************
    * DML is Data Manipulation Language : it is used to manipulate data itself.
    ************************************************************************** */

  def insertData(data: TestData): Unit = {
    val action = test += data
    Try(Await.result(db.run(action), 20 seconds)) match {
      case Success(_) =>
        log.info(s"Data correctly inserted")
      case Failure(e) =>
        throw e
    }
  }

  def insertData(data: Seq[TestData]): Unit = {
    val action = test ++= data
    Try(Await.result(db.run(action), 20 seconds)) match {
      case Success(_) =>
        log.info(s"Data correctly inserted")
      case Failure(e) =>
        throw e
    }
  }

  def getAllData: Seq[TestData] ={
    val action = test.result
    Try(Await.result(db.run(action), 20 seconds)) match {
      case Success(x) =>
        log.info(s"Data correctly inserted")
        x
      case Failure(e) =>
        throw e
    }
  }

  def getAllData2: Seq[TestData] ={
    val action = (for( t <- test) yield t).result
    Try(Await.result(db.run(action), 20 seconds)) match {
      case Success(x) =>
        log.info(s"Data correctly inserted")
        x
      case Failure(e) =>
        throw e
    }
  }

  def getAllNames: Seq[String] ={
    val action = (for( t <- test) yield t.name).result
    Try(Await.result(db.run(action), 20 seconds)) match {
      case Success(x) =>
        log.info(s"Data correctly inserted")
        x
      case Failure(e) =>
        throw e
    }
  }
}

