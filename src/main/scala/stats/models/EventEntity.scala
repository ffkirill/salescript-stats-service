package stats.models

import java.sql.Timestamp
import java.util.UUID

case class EventEntity(id: Option[Long] = None,
                       userId: Long,
                       scriptId: Long,
                       fromNodeId: UUID,
                       toNodeId: UUID,
                       reachedGoalId: Option[Int],
                       timestamp: Timestamp) {
}
