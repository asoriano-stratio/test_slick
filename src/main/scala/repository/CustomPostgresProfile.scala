package repository

import com.github.tminglei.slickpg._
import data.model.configuration.session.{MlWorkflowJobData, SessionData, SessionStatus}
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.{read, write}
import slick.basic.Capability
import slick.jdbc.JdbcCapabilities

trait CustomPostgresProfile extends ExPostgresProfile
  with PgArraySupport
  with PgDate2Support
  with PgRangeSupport
  with PgHStoreSupport
  //                                        with PgJson4sSupport
  with PgSearchSupport
  with PgNetSupport
  with PgLTreeSupport {

  // Add back `capabilities.insertOrUpdate` to enable native `upsert` support; for postgres 9.5+
  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + JdbcCapabilities.insertOrUpdate

  override val api: CustomAPI.type = CustomAPI

  object CustomAPI extends API with ArrayImplicits
    with DateTimeImplicits
    //        with JsonImplicits
    with NetImplicits
    with LTreeImplicits
    with RangeImplicits
    with HStoreImplicits
    with SearchImplicits
    with SearchAssistants {

    implicit val formats: DefaultFormats.type = DefaultFormats

    implicit val sessionStatusType = MappedColumnType.base[SessionStatus, String](
      objToSerialize => write(objToSerialize),
      objToDeSerialize => read[SessionStatus](objToDeSerialize)
    )

    implicit val mlWorkflowJobDataType = MappedColumnType.base[MlWorkflowJobData, String](
      objToSerialize => write(objToSerialize),
      objToDeSerialize => read[MlWorkflowJobData](objToDeSerialize)
    )
  }

}

object CustomPostgresProfile extends CustomPostgresProfile