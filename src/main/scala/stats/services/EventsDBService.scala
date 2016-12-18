package stats.services

import stats.models.db.EventEntityTable
import stats.models.{EventEntity, ScriptGoals}
import stats.utils.DatabaseService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class ScriptSummaryEntry(scriptId: Long,
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

  def getScriptSummary(ids: Seq[Long]): Future[Seq[ScriptSummaryEntry]] = {
    val query = for {
      (scriptId, nestedData) <- events.filter(_.scriptId inSetBind ids)
        .groupBy(_.scriptId)
    } yield {

      def aggGoalReachCount(query: nestedData.type , goal: ScriptGoals.Value) = query.map(p =>
        Case If (p.reachedGoalId === goal.id.toLong) Then 1 Else 0).sum.getOrElse(0)

      (
        scriptId,
        aggGoalReachCount(nestedData, ScriptGoals.scriptRan),
        aggGoalReachCount(nestedData, ScriptGoals.success),
        aggGoalReachCount(nestedData, ScriptGoals.failure),
        aggGoalReachCount(nestedData, ScriptGoals.noSuchReply),
        nestedData.length
      )

    }
    query.result.statements.foreach(println)
    val action = query.result.map(seq =>
      seq.map(row => (ScriptSummaryEntry.apply _).tupled(row))
    )
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
