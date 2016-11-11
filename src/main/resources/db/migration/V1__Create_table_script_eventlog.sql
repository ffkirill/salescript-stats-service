CREATE TABLE "script_eventlog" (
  "id"       BIGSERIAL PRIMARY KEY,
  "user_id" BIGINT NOT NULL,
  "script_id" BIGINT NOT NULL,
  "from_node" UUID NOT NULL,
  "to_node" UUID NOT NULL,
  "reached_goal" SMALLINT DEFAULT NULL,
  "timestamp" TIMESTAMP NOT NULL DEFAULT NOW()
);