package stats.services

import slick.dbio.Effect.Write
import slick.profile.FixedSqlAction
import stats.models.db.EventEntityTable
import stats.models.EventEntity
import stats.utils.DatabaseService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class EventsService(val databaseService: DatabaseService)
                   (implicit executionContext: ExecutionContext) extends EventEntityTable {

  import databaseService._
  import databaseService.driver.api._

  def getEvents: Future[Seq[EventEntity]] = db.run(events.result)

  def createEvent(event: EventEntity) = {
    val query: FixedSqlAction[EventEntity, NoStream, Write] = (events returning events) += event
    query.statements.foreach(println)
    db.run(query.asTry).map {
      case Success(res) =>
        println("success")
      case Failure(e) => println(e)
    }
  }


}

object EventsService {
  def apply(databaseService: DatabaseService)
           (implicit executionContext: ExecutionContext): EventsService =
    new EventsService(databaseService)(executionContext)
}
