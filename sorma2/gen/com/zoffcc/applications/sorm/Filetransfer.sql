CREATE TABLE IF NOT EXISTS "Filetransfer" (
  "id" INTEGER,
  "tox_public_key_string" TEXT,
  "direction" INTEGER,
  "file_number" INTEGER,
  "kind" INTEGER,
  "state" INTEGER,
  "ft_accepted" BOOLEAN,
  "ft_outgoing_started" BOOLEAN,
  "path_name" TEXT,
  "file_name" TEXT,
  "fos_open" BOOLEAN,
  "filesize" INTEGER,
  "current_position" INTEGER,
  "message_id" INTEGER,
  "storage_frame_work" BOOLEAN,
  "tox_file_id_hex" TEXT,
  PRIMARY KEY("id" AUTOINCREMENT)
);
