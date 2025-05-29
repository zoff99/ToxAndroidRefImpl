CREATE TABLE IF NOT EXISTS "GroupPeerDB" (
  "id" INTEGER,
  "group_identifier" TEXT,
  "tox_group_peer_pubkey" TEXT,
  "peer_name" TEXT,
  "last_update_timestamp" INTEGER,
  "first_join_timestamp" INTEGER,
  "Tox_Group_Role" INTEGER,
  "notification_silent" BOOLEAN,
  PRIMARY KEY("id" AUTOINCREMENT)
);
