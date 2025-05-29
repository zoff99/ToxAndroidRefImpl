CREATE TABLE IF NOT EXISTS "BootstrapNodeEntryDB" (
  "id" INTEGER,
  "num" INTEGER,
  "udp_node" BOOLEAN,
  "ip" TEXT,
  "port" INTEGER,
  "key_hex" TEXT,
  PRIMARY KEY("id" AUTOINCREMENT)
);
