CREATE TABLE IF NOT EXISTS "FileDB" (
  "id" INTEGER,
  "kind" INTEGER,
  "direction" INTEGER,
  "tox_public_key_string" TEXT,
  "path_name" TEXT,
  "file_name" TEXT,
  "filesize" INTEGER,
  "is_in_VFS" BOOLEAN,
  PRIMARY KEY("id" AUTOINCREMENT)
);
