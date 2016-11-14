package stats.models.db

import java.sql.Timestamp
import java.util.UUID

import stats.models.EventEntity
import stats.utils.DatabaseService

trait EventEntityTable {

  protected val databaseService: DatabaseService
  import databaseService.driver.api._

  class Events(tag: Tag) extends Table[EventEntity](tag, "script_eventlog") {
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Long]("user_id")
    def scriptId = column[Long]("script_id")
    def fromNodeId = column[UUID]("from_node")
    def toNodeId = column[UUID]("to_node")
    def reachedGoalId = column[Option[Int]]("reached_goal")
    def timestamp = column[Timestamp]("timestamp", O.SqlType("timestamp default current_timestamp"))

    def * = (
      id,
      userId,
      scriptId,
      fromNodeId,
      toNodeId,
      reachedGoalId,
      timestamp) <> ((EventEntity.apply _).tupled, EventEntity.unapply)
  }

  protected val events = TableQuery[Events]

}
