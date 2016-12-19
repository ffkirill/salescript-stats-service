package stats.services

import java.sql.Timestamp
import java.time.LocalDateTime

import stats.models.db.EventEntityTable
import stats.models.{EventEntity, ScriptGoals}
import stats.utils.DatabaseService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try


case class ScriptSummaryEntry(scriptId: Long,
                              userId: Option[Long],
                              date: Option[Timestamp],
                              runCount: Int,
                              successCount: Int,
                              failCount: Int,
                              noSuchReplyCount: Int,
                              allEventsCount: Int)

class EventsDBService(val databaseService: DatabaseService)
                     (implicit executionContext: ExecutionContext) extends EventEntityTable {

  import databaseService._
  import databaseService.driver.api._

  def get: Future[Seq[EventEntity]] = db.run(events.result)

  def getByScriptId(id: Long): Future[Seq[EventEntity]] =
    db.run(events.filter(_.scriptId === id).result)

  def getScriptSummary(ids: Option[Seq[Long]],
                       startDate: Option[LocalDateTime] = None,
                       endDate: Option[LocalDateTime] = None,
                       groupByUser: Boolean = false,
                       groupByDate: Option[DateTruncKind] = None): Future[Seq[ScriptSummaryEntry]] = {

    def aggGoalReachCount(query: Query[Events, EventEntity, Seq],
                          goal: ScriptGoals.Value) = query.map(p =>
      Case If (p.reachedGoalId === goal.id.toLong) Then 1 Else 0).sum.getOrElse(0)

    var sourceQuery: Query[Events, EventEntity, Seq] = events

    ids.foreach(v => sourceQuery = sourceQuery.filter(_.scriptId inSetBind v))
    startDate.foreach(v => sourceQuery = sourceQuery.filter(_.timestamp >= Timestamp.valueOf(v)))
    endDate.foreach(v => sourceQuery = sourceQuery.filter(_.timestamp <= Timestamp.valueOf(v)))

    def grouper(q: Events): (Rep[Long], Rep[Option[Long]], Rep[Option[Timestamp]]) = (
      q.scriptId,
      if (groupByUser) q.userId.? else None,
      groupByDate.map(v => q.timestamp.? trunc v.toString).getOrElse(None)
    )

    val query = for {
      (group, nestedData) <- sourceQuery
        .groupBy(grouper)
    } yield {
      (
        group._1,
        group._2,
        group._3,
        aggGoalReachCount(nestedData, ScriptGoals.scriptRan),
        aggGoalReachCount(nestedData, ScriptGoals.success),
        aggGoalReachCount(nestedData, ScriptGoals.failure),
        aggGoalReachCount(nestedData, ScriptGoals.noSuchReply),
        nestedData.length
      ) <> ((ScriptSummaryEntry.apply _).tupled, ScriptSummaryEntry.unapply)

    }

    val action = query.result
    action.statements.foreach(println)
    db.run(action)
  }

  def createEvent(event: EventEntity): Future[Try[EventEntity]] = {
    val query = (events returning events) += event
    db.run(query.asTry)
  }

}

object EventsDBService {
  def apply(databaseService: DatabaseService)
           (implicit executionContext: ExecutionContext): EventsDBService =
    new EventsDBService(databaseService)(executionContext)
}
