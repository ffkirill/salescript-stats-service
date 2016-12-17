package stats.utils

import java.sql.Timestamp
import java.util.UUID

import io.circe.Encoder
import io.circe.java8.time._

trait Protocols {
  implicit val encodeUUID: Encoder[UUID] = Encoder.encodeString.contramap[UUID](_.toString)
  implicit val encodeTimestamp: Encoder[Timestamp] =
    t => encodeLocalDateTimeDefault(t.toLocalDateTime)
}
