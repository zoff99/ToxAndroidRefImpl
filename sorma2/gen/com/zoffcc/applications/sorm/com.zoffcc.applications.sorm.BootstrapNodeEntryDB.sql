CREATE TABLE IF NOT EXISTS "com.zoffcc.applications.sorm.BootstrapNodeEntryDB" (
  "id" INTEGER,
  "num" INTEGER,
  "udp_node" BOOLEAN,
  "ip" TEXT,
  "port" INTEGER,
  "key_hex" TEXT,
  PRIMARY KEY("id" AUTOINCREMENT)
);
