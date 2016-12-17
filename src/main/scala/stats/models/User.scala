package stats.models

case class User(
  id: Int,
  username: String,
  email: String,
  isSuperuser: Boolean,
  ownScripts: Map[Long, ScriptEntity]
)
