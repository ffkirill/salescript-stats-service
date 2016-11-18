package stats.models

import java.sql.Timestamp
import java.util.UUID

case class EventEntity(id: Option[Long] = None,
                       userId: Long,
                       scriptId: Long,
                       fromNodeId: UUID,
                       toNodeId: Option[UUID] = None,
                       reachedGoalId: Option[Long] = None,
                       textFrom: Option[String] = None,
                       textTo: Option[String] = None,
                       timestamp: Timestamp) {
}
