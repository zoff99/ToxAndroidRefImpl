CREATE TABLE IF NOT EXISTS "GroupDB" (
  "group_identifier" TEXT,
  "who_invited__tox_public_key_string" TEXT,
  "name" TEXT,
  "topic" TEXT,
  "peer_count" INTEGER,
  "own_peer_number" INTEGER,
  "privacy_state" INTEGER,
  "tox_group_number" INTEGER,
  "group_active" BOOLEAN,
  "group_we_left" BOOLEAN,
  "notification_silent" BOOLEAN,
  PRIMARY KEY("group_identifier" )
);
