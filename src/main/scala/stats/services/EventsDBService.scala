package stats.services

import slick.dbio.Effect.Write
import slick.profile.FixedSqlAction
import stats.models.db.EventEntityTable
import stats.models.EventEntity
import stats.utils.DatabaseService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class EventsDBService(val databaseService: DatabaseService)
                     (implicit executionContext: ExecutionContext) extends EventEntityTable {

  import databaseService._
  import databaseService.driver.api._

  def getEvents: Future[Seq[EventEntity]] = db.run(events.result)

  def createEvent(event: EventEntity): Future[Try[EventEntity]] = {
    val query: FixedSqlAction[EventEntity, NoStream, Write] = (events returning events) += event
    db.run(query.asTry)
  }

}

object EventsDBService {
  def apply(databaseService: DatabaseService)
           (implicit executionContext: ExecutionContext): EventsDBService =
    new EventsDBService(databaseService)(executionContext)
}
