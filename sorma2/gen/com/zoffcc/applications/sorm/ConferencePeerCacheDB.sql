CREATE TABLE IF NOT EXISTS "ConferencePeerCacheDB" (
  "id" INTEGER,
  "conference_identifier" TEXT,
  "peer_pubkey" TEXT,
  "peer_name" TEXT,
  "last_update_timestamp" INTEGER,
  PRIMARY KEY("id" AUTOINCREMENT)
);
