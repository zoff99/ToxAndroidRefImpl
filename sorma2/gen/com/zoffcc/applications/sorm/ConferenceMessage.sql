CREATE TABLE IF NOT EXISTS "ConferenceMessage" (
  "id" INTEGER,
  "message_id_tox" TEXT,
  "conference_identifier" TEXT,
  "tox_peerpubkey" TEXT,
  "tox_peername" TEXT,
  "direction" INTEGER,
  "TOX_MESSAGE_TYPE" INTEGER,
  "TRIFA_MESSAGE_TYPE" INTEGER,
  "sent_timestamp" INTEGER,
  "rcvd_timestamp" INTEGER,
  "read" BOOLEAN,
  "is_new" BOOLEAN,
  "text" TEXT,
  "was_synced" BOOLEAN,
  PRIMARY KEY("id" AUTOINCREMENT)
);
