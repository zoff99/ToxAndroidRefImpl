CREATE TABLE IF NOT EXISTS "RelayListDB" (
  "tox_public_key_string" TEXT,
  "TOX_CONNECTION" INTEGER,
  "TOX_CONNECTION_on_off" INTEGER,
  "own_relay" BOOLEAN,
  "last_online_timestamp" INTEGER,
  "tox_public_key_string_of_owner" TEXT,
  PRIMARY KEY("tox_public_key_string" )
);
