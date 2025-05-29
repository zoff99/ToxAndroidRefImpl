CREATE TABLE IF NOT EXISTS "ConferenceDB" (
  "conference_identifier" TEXT,
  "who_invited__tox_public_key_string" TEXT,
  "name" TEXT,
  "peer_count" INTEGER,
  "own_peer_number" INTEGER,
  "kind" INTEGER,
  "tox_conference_number" INTEGER,
  "conference_active" BOOLEAN,
  "notification_silent" BOOLEAN,
  PRIMARY KEY("conference_identifier" )
);
