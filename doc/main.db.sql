BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS "BootstrapNodeEntryDB" (
	"num"	INTEGER NOT NULL,
	"udp_node"	BOOLEAN NOT NULL,
	"ip"	TEXT NOT NULL,
	"port"	INTEGER NOT NULL,
	"key_hex"	TEXT NOT NULL,
	"id"	INTEGER,
	PRIMARY KEY("id" AUTOINCREMENT)
);
CREATE TABLE IF NOT EXISTS "ConferenceDB" (
	"who_invited__tox_public_key_string"	TEXT NOT NULL,
	"name"	TEXT,
	"peer_count"	INTEGER NOT NULL DEFAULT -1,
	"own_peer_number"	INTEGER NOT NULL DEFAULT -1,
	"kind"	INTEGER NOT NULL DEFAULT 0,
	"tox_conference_number"	INTEGER NOT NULL DEFAULT -1,
	"conference_active"	BOOLEAN NOT NULL DEFAULT false,
	"notification_silent"	BOOLEAN DEFAULT false,
	"conference_identifier"	TEXT,
	PRIMARY KEY("conference_identifier")
);
CREATE TABLE IF NOT EXISTS "ConferenceMessage" (
	"message_id_tox"	TEXT,
	"conference_identifier"	TEXT NOT NULL DEFAULT -1,
	"tox_peerpubkey"	TEXT NOT NULL,
	"tox_peername"	TEXT,
	"direction"	INTEGER NOT NULL,
	"TOX_MESSAGE_TYPE"	INTEGER NOT NULL,
	"TRIFA_MESSAGE_TYPE"	INTEGER NOT NULL DEFAULT 0,
	"sent_timestamp"	INTEGER,
	"rcvd_timestamp"	INTEGER,
	"read"	BOOLEAN NOT NULL,
	"is_new"	BOOLEAN NOT NULL,
	"text"	TEXT,
	"was_synced"	BOOLEAN,
	"id"	INTEGER,
	PRIMARY KEY("id" AUTOINCREMENT)
);
CREATE TABLE IF NOT EXISTS "ConferencePeerCacheDB" (
	"conference_identifier"	TEXT NOT NULL,
	"peer_pubkey"	TEXT NOT NULL,
	"peer_name"	TEXT NOT NULL,
	"last_update_timestamp"	INTEGER NOT NULL DEFAULT -1,
	"id"	INTEGER,
	PRIMARY KEY("id" AUTOINCREMENT)
);
CREATE TABLE IF NOT EXISTS "FileDB" (
	"kind"	INTEGER NOT NULL,
	"direction"	INTEGER NOT NULL,
	"tox_public_key_string"	TEXT NOT NULL,
	"path_name"	TEXT NOT NULL,
	"file_name"	TEXT NOT NULL,
	"filesize"	INTEGER NOT NULL DEFAULT -1,
	"is_in_VFS"	BOOLEAN NOT NULL DEFAULT true,
	"id"	INTEGER,
	PRIMARY KEY("id" AUTOINCREMENT)
);
CREATE TABLE IF NOT EXISTS "Filetransfer" (
	"tox_public_key_string"	TEXT NOT NULL,
	"direction"	INTEGER NOT NULL,
	"file_number"	INTEGER NOT NULL,
	"kind"	INTEGER NOT NULL,
	"state"	INTEGER NOT NULL,
	"ft_accepted"	BOOLEAN NOT NULL DEFAULT false,
	"ft_outgoing_started"	BOOLEAN NOT NULL DEFAULT false,
	"path_name"	TEXT NOT NULL,
	"file_name"	TEXT NOT NULL,
	"fos_open"	BOOLEAN NOT NULL DEFAULT false,
	"filesize"	INTEGER NOT NULL DEFAULT -1,
	"current_position"	INTEGER NOT NULL DEFAULT 0,
	"message_id"	INTEGER NOT NULL DEFAULT -1,
	"storage_frame_work"	BOOLEAN NOT NULL DEFAULT false,
	"tox_file_id_hex"	TEXT,
	"id"	INTEGER,
	PRIMARY KEY("id" AUTOINCREMENT)
);
CREATE TABLE IF NOT EXISTS "FriendList" (
	"name"	TEXT,
	"alias_name"	TEXT,
	"status_message"	TEXT,
	"TOX_CONNECTION"	INTEGER NOT NULL DEFAULT 0,
	"TOX_CONNECTION_real"	INTEGER NOT NULL DEFAULT 0,
	"TOX_CONNECTION_on_off"	INTEGER NOT NULL DEFAULT 0,
	"TOX_CONNECTION_on_off_real"	INTEGER NOT NULL DEFAULT 0,
	"TOX_USER_STATUS"	INTEGER NOT NULL DEFAULT 0,
	"avatar_pathname"	TEXT,
	"avatar_filename"	TEXT,
	"avatar_ftid_hex"	TEXT,
	"avatar_update"	BOOLEAN DEFAULT false,
	"avatar_update_timestamp"	INTEGER NOT NULL DEFAULT -1,
	"notification_silent"	BOOLEAN DEFAULT false,
	"sort"	INTEGER NOT NULL DEFAULT 0,
	"last_online_timestamp"	INTEGER NOT NULL DEFAULT -1,
	"last_online_timestamp_real"	INTEGER NOT NULL DEFAULT -1,
	"added_timestamp"	INTEGER NOT NULL DEFAULT -1,
	"is_relay"	BOOLEAN DEFAULT false,
	"push_url"	TEXT,
	"ip_addr_str"	TEXT,
	"capabilities"	INTEGER NOT NULL DEFAULT 0,
	"msgv3_capability"	INTEGER NOT NULL DEFAULT 0,
	"tox_public_key_string"	TEXT,
	PRIMARY KEY("tox_public_key_string")
);
CREATE TABLE IF NOT EXISTS "GroupDB" (
	"who_invited__tox_public_key_string"	TEXT NOT NULL,
	"name"	TEXT,
	"topic"	TEXT,
	"peer_count"	INTEGER NOT NULL DEFAULT -1,
	"own_peer_number"	INTEGER NOT NULL DEFAULT -1,
	"privacy_state"	INTEGER NOT NULL DEFAULT 0,
	"tox_group_number"	INTEGER NOT NULL DEFAULT -1,
	"group_active"	BOOLEAN NOT NULL DEFAULT false,
	"group_we_left"	BOOLEAN NOT NULL DEFAULT false,
	"notification_silent"	BOOLEAN DEFAULT false,
	"group_identifier"	TEXT,
	PRIMARY KEY("group_identifier")
);
CREATE TABLE IF NOT EXISTS "GroupMessage" (
	"message_id_tox"	TEXT,
	"group_identifier"	TEXT NOT NULL DEFAULT -1,
	"tox_group_peer_pubkey"	TEXT NOT NULL,
	"tox_group_peer_role"	INTEGER NOT NULL DEFAULT -1,
	"private_message"	INTEGER,
	"tox_group_peername"	TEXT,
	"direction"	INTEGER NOT NULL,
	"TOX_MESSAGE_TYPE"	INTEGER NOT NULL,
	"TRIFA_MESSAGE_TYPE"	INTEGER NOT NULL DEFAULT 0,
	"sent_timestamp"	INTEGER,
	"rcvd_timestamp"	INTEGER,
	"read"	BOOLEAN NOT NULL,
	"is_new"	BOOLEAN NOT NULL,
	"text"	TEXT,
	"was_synced"	BOOLEAN,
	"TRIFA_SYNC_TYPE"	INTEGER,
	"sync_confirmations"	INTEGER NOT NULL DEFAULT 0,
	"tox_group_peer_pubkey_syncer_01"	TEXT,
	"tox_group_peer_pubkey_syncer_02"	TEXT,
	"tox_group_peer_pubkey_syncer_03"	TEXT,
	"tox_group_peer_pubkey_syncer_01_sent_timestamp"	INTEGER,
	"tox_group_peer_pubkey_syncer_02_sent_timestamp"	INTEGER,
	"tox_group_peer_pubkey_syncer_03_sent_timestamp"	INTEGER,
	"msg_id_hash"	TEXT,
	"sent_privately_to_tox_group_peer_pubkey"	TEXT,
	"path_name"	TEXT,
	"file_name"	TEXT,
	"filename_fullpath"	TEXT,
	"filesize"	INTEGER NOT NULL DEFAULT -1,
	"storage_frame_work"	BOOLEAN NOT NULL DEFAULT false,
	"id"	INTEGER,
	PRIMARY KEY("id" AUTOINCREMENT)
);
CREATE TABLE IF NOT EXISTS "GroupPeerDB" (
	"group_identifier"	TEXT NOT NULL,
	"tox_group_peer_pubkey"	TEXT NOT NULL,
	"peer_name"	TEXT,
	"last_update_timestamp"	INTEGER NOT NULL DEFAULT -1,
	"first_join_timestamp"	INTEGER NOT NULL DEFAULT -1,
	"Tox_Group_Role"	INTEGER NOT NULL DEFAULT 2,
	"id"	INTEGER,
	PRIMARY KEY("id" AUTOINCREMENT)
);
CREATE TABLE IF NOT EXISTS "Message" (
	"message_id"	INTEGER NOT NULL,
	"tox_friendpubkey"	TEXT NOT NULL,
	"direction"	INTEGER NOT NULL,
	"TOX_MESSAGE_TYPE"	INTEGER NOT NULL,
	"TRIFA_MESSAGE_TYPE"	INTEGER NOT NULL DEFAULT 0,
	"state"	INTEGER NOT NULL DEFAULT 1,
	"ft_accepted"	BOOLEAN NOT NULL DEFAULT false,
	"ft_outgoing_started"	BOOLEAN NOT NULL DEFAULT false,
	"filedb_id"	INTEGER NOT NULL DEFAULT -1,
	"filetransfer_id"	INTEGER NOT NULL DEFAULT -1,
	"sent_timestamp"	INTEGER DEFAULT 0,
	"sent_timestamp_ms"	INTEGER DEFAULT 0,
	"rcvd_timestamp"	INTEGER DEFAULT 0,
	"rcvd_timestamp_ms"	INTEGER DEFAULT 0,
	"read"	BOOLEAN NOT NULL,
	"send_retries"	INTEGER NOT NULL DEFAULT 0,
	"is_new"	BOOLEAN NOT NULL,
	"text"	TEXT,
	"filename_fullpath"	TEXT,
	"msg_id_hash"	TEXT,
	"raw_msgv2_bytes"	TEXT,
	"msg_version"	INTEGER NOT NULL DEFAULT 0,
	"resend_count"	INTEGER NOT NULL DEFAULT 4,
	"storage_frame_work"	BOOLEAN NOT NULL DEFAULT false,
	"ft_outgoing_queued"	BOOLEAN NOT NULL DEFAULT false,
	"msg_at_relay"	BOOLEAN NOT NULL DEFAULT false,
	"msg_idv3_hash"	TEXT,
	"sent_push"	INTEGER,
	"filetransfer_kind"	INTEGER DEFAULT 0,
	"id"	INTEGER,
	PRIMARY KEY("id" AUTOINCREMENT)
);
CREATE TABLE IF NOT EXISTS "RelayListDB" (
	"TOX_CONNECTION"	INTEGER NOT NULL DEFAULT 0,
	"TOX_CONNECTION_on_off"	INTEGER NOT NULL DEFAULT 0,
	"own_relay"	BOOLEAN NOT NULL DEFAULT false,
	"last_online_timestamp"	INTEGER NOT NULL DEFAULT -1,
	"tox_public_key_string_of_owner"	TEXT,
	"tox_public_key_string"	TEXT,
	PRIMARY KEY("tox_public_key_string")
);
CREATE TABLE IF NOT EXISTS "TRIFADatabaseGlobals" (
	"key"	TEXT NOT NULL,
	"value"	TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS "TRIFADatabaseGlobalsNew" (
	"value"	TEXT NOT NULL,
	"key"	TEXT,
	PRIMARY KEY("key")
);
CREATE TABLE IF NOT EXISTS "orma_migration_steps" (
	"id"	INTEGER,
	"version"	INTEGER NOT NULL,
	"sql"	TEXT,
	"created_timestamp"	DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY("id" AUTOINCREMENT)
);
CREATE TABLE IF NOT EXISTS "orma_schema_diff_migration_2" (
	"id"	INTEGER,
	"db_version"	INTEGER NOT NULL,
	"version_name"	TEXT NOT NULL,
	"version_code"	INTEGER NOT NULL,
	"schema_hash"	TEXT NOT NULL,
	"sql"	TEXT,
	"args"	TEXT,
	"created_timestamp"	DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY("id" AUTOINCREMENT)
);
CREATE TABLE IF NOT EXISTS "sqlite_stat4" (
	"tbl"	,
	"idx"	,
	"neq"	,
	"nlt"	,
	"ndlt"	,
	"sample"	
);
CREATE INDEX IF NOT EXISTS "index_TOX_CONNECTION_on_FriendList" ON "FriendList" (
	"TOX_CONNECTION"
);
CREATE INDEX IF NOT EXISTS "index_TOX_CONNECTION_on_RelayListDB" ON "RelayListDB" (
	"TOX_CONNECTION"
);
CREATE INDEX IF NOT EXISTS "index_TOX_CONNECTION_on_off_on_FriendList" ON "FriendList" (
	"TOX_CONNECTION_on_off"
);
CREATE INDEX IF NOT EXISTS "index_TOX_CONNECTION_on_off_on_RelayListDB" ON "RelayListDB" (
	"TOX_CONNECTION_on_off"
);
CREATE INDEX IF NOT EXISTS "index_TOX_CONNECTION_on_off_real_on_FriendList" ON "FriendList" (
	"TOX_CONNECTION_on_off_real"
);
CREATE INDEX IF NOT EXISTS "index_TOX_CONNECTION_real_on_FriendList" ON "FriendList" (
	"TOX_CONNECTION_real"
);
CREATE INDEX IF NOT EXISTS "index_TOX_MESSAGE_TYPE_on_ConferenceMessage" ON "ConferenceMessage" (
	"TOX_MESSAGE_TYPE"
);
CREATE INDEX IF NOT EXISTS "index_TOX_MESSAGE_TYPE_on_GroupMessage" ON "GroupMessage" (
	"TOX_MESSAGE_TYPE"
);
CREATE INDEX IF NOT EXISTS "index_TOX_MESSAGE_TYPE_on_Message" ON "Message" (
	"TOX_MESSAGE_TYPE"
);
CREATE INDEX IF NOT EXISTS "index_TOX_USER_STATUS_on_FriendList" ON "FriendList" (
	"TOX_USER_STATUS"
);
CREATE INDEX IF NOT EXISTS "index_TRIFA_MESSAGE_TYPE_on_ConferenceMessage" ON "ConferenceMessage" (
	"TRIFA_MESSAGE_TYPE"
);
CREATE INDEX IF NOT EXISTS "index_TRIFA_MESSAGE_TYPE_on_GroupMessage" ON "GroupMessage" (
	"TRIFA_MESSAGE_TYPE"
);
CREATE INDEX IF NOT EXISTS "index_TRIFA_MESSAGE_TYPE_on_Message" ON "Message" (
	"TRIFA_MESSAGE_TYPE"
);
CREATE INDEX IF NOT EXISTS "index_TRIFA_SYNC_TYPE_on_GroupMessage" ON "GroupMessage" (
	"TRIFA_SYNC_TYPE"
);
CREATE INDEX IF NOT EXISTS "index_Tox_Group_Role_on_GroupPeerDB" ON "GroupPeerDB" (
	"Tox_Group_Role"
);
CREATE INDEX IF NOT EXISTS "index_added_timestamp_on_FriendList" ON "FriendList" (
	"added_timestamp"
);
CREATE INDEX IF NOT EXISTS "index_alias_name_on_FriendList" ON "FriendList" (
	"alias_name"
);
CREATE INDEX IF NOT EXISTS "index_avatar_ftid_hex_on_FriendList" ON "FriendList" (
	"avatar_ftid_hex"
);
CREATE INDEX IF NOT EXISTS "index_avatar_update_on_FriendList" ON "FriendList" (
	"avatar_update"
);
CREATE INDEX IF NOT EXISTS "index_avatar_update_timestamp_on_FriendList" ON "FriendList" (
	"avatar_update_timestamp"
);
CREATE INDEX IF NOT EXISTS "index_capabilities_on_FriendList" ON "FriendList" (
	"capabilities"
);
CREATE INDEX IF NOT EXISTS "index_conference_active_on_ConferenceDB" ON "ConferenceDB" (
	"conference_active"
);
CREATE INDEX IF NOT EXISTS "index_conference_identifier_on_ConferenceMessage" ON "ConferenceMessage" (
	"conference_identifier"
);
CREATE INDEX IF NOT EXISTS "index_conference_identifier_on_ConferencePeerCacheDB" ON "ConferencePeerCacheDB" (
	"conference_identifier"
);
CREATE UNIQUE INDEX IF NOT EXISTS "index_conference_identifier_peer_pubkey_on_ConferencePeerCacheDB" ON "ConferencePeerCacheDB" (
	"conference_identifier",
	"peer_pubkey"
);
CREATE INDEX IF NOT EXISTS "index_direction_on_ConferenceMessage" ON "ConferenceMessage" (
	"direction"
);
CREATE INDEX IF NOT EXISTS "index_direction_on_FileDB" ON "FileDB" (
	"direction"
);
CREATE INDEX IF NOT EXISTS "index_direction_on_Filetransfer" ON "Filetransfer" (
	"direction"
);
CREATE INDEX IF NOT EXISTS "index_direction_on_GroupMessage" ON "GroupMessage" (
	"direction"
);
CREATE INDEX IF NOT EXISTS "index_direction_on_Message" ON "Message" (
	"direction"
);
CREATE INDEX IF NOT EXISTS "index_file_name_on_FileDB" ON "FileDB" (
	"file_name"
);
CREATE INDEX IF NOT EXISTS "index_file_name_on_Filetransfer" ON "Filetransfer" (
	"file_name"
);
CREATE INDEX IF NOT EXISTS "index_file_name_on_GroupMessage" ON "GroupMessage" (
	"file_name"
);
CREATE INDEX IF NOT EXISTS "index_file_number_on_Filetransfer" ON "Filetransfer" (
	"file_number"
);
CREATE INDEX IF NOT EXISTS "index_filedb_id_on_Message" ON "Message" (
	"filedb_id"
);
CREATE INDEX IF NOT EXISTS "index_filesize_on_FileDB" ON "FileDB" (
	"filesize"
);
CREATE INDEX IF NOT EXISTS "index_filesize_on_GroupMessage" ON "GroupMessage" (
	"filesize"
);
CREATE INDEX IF NOT EXISTS "index_filetransfer_id_on_Message" ON "Message" (
	"filetransfer_id"
);
CREATE INDEX IF NOT EXISTS "index_first_join_timestamp_on_GroupPeerDB" ON "GroupPeerDB" (
	"first_join_timestamp"
);
CREATE INDEX IF NOT EXISTS "index_ft_accepted_on_Filetransfer" ON "Filetransfer" (
	"ft_accepted"
);
CREATE INDEX IF NOT EXISTS "index_ft_accepted_on_Message" ON "Message" (
	"ft_accepted"
);
CREATE INDEX IF NOT EXISTS "index_ft_outgoing_queued_on_Message" ON "Message" (
	"ft_outgoing_queued"
);
CREATE INDEX IF NOT EXISTS "index_ft_outgoing_started_on_Filetransfer" ON "Filetransfer" (
	"ft_outgoing_started"
);
CREATE INDEX IF NOT EXISTS "index_ft_outgoing_started_on_Message" ON "Message" (
	"ft_outgoing_started"
);
CREATE INDEX IF NOT EXISTS "index_group_active_on_GroupDB" ON "GroupDB" (
	"group_active"
);
CREATE INDEX IF NOT EXISTS "index_group_identifier_on_GroupMessage" ON "GroupMessage" (
	"group_identifier"
);
CREATE INDEX IF NOT EXISTS "index_group_identifier_on_GroupPeerDB" ON "GroupPeerDB" (
	"group_identifier"
);
CREATE UNIQUE INDEX IF NOT EXISTS "index_group_identifier_tox_group_peer_pubkey_on_GroupPeerDB" ON "GroupPeerDB" (
	"group_identifier",
	"tox_group_peer_pubkey"
);
CREATE INDEX IF NOT EXISTS "index_group_we_left_on_GroupDB" ON "GroupDB" (
	"group_we_left"
);
CREATE INDEX IF NOT EXISTS "index_ip_addr_str_on_FriendList" ON "FriendList" (
	"ip_addr_str"
);
CREATE INDEX IF NOT EXISTS "index_ip_on_BootstrapNodeEntryDB" ON "BootstrapNodeEntryDB" (
	"ip"
);
CREATE INDEX IF NOT EXISTS "index_is_in_VFS_on_FileDB" ON "FileDB" (
	"is_in_VFS"
);
CREATE INDEX IF NOT EXISTS "index_is_new_on_ConferenceMessage" ON "ConferenceMessage" (
	"is_new"
);
CREATE INDEX IF NOT EXISTS "index_is_new_on_GroupMessage" ON "GroupMessage" (
	"is_new"
);
CREATE INDEX IF NOT EXISTS "index_is_new_on_Message" ON "Message" (
	"is_new"
);
CREATE INDEX IF NOT EXISTS "index_is_relay_on_FriendList" ON "FriendList" (
	"is_relay"
);
CREATE INDEX IF NOT EXISTS "index_key_hex_on_BootstrapNodeEntryDB" ON "BootstrapNodeEntryDB" (
	"key_hex"
);
CREATE INDEX IF NOT EXISTS "index_key_on_TRIFADatabaseGlobals" ON "TRIFADatabaseGlobals" (
	"key"
);
CREATE INDEX IF NOT EXISTS "index_kind_on_ConferenceDB" ON "ConferenceDB" (
	"kind"
);
CREATE INDEX IF NOT EXISTS "index_kind_on_FileDB" ON "FileDB" (
	"kind"
);
CREATE INDEX IF NOT EXISTS "index_kind_on_Filetransfer" ON "Filetransfer" (
	"kind"
);
CREATE INDEX IF NOT EXISTS "index_last_online_timestamp_on_FriendList" ON "FriendList" (
	"last_online_timestamp"
);
CREATE INDEX IF NOT EXISTS "index_last_online_timestamp_on_RelayListDB" ON "RelayListDB" (
	"last_online_timestamp"
);
CREATE INDEX IF NOT EXISTS "index_last_online_timestamp_real_on_FriendList" ON "FriendList" (
	"last_online_timestamp_real"
);
CREATE INDEX IF NOT EXISTS "index_last_update_timestamp_on_ConferencePeerCacheDB" ON "ConferencePeerCacheDB" (
	"last_update_timestamp"
);
CREATE INDEX IF NOT EXISTS "index_last_update_timestamp_on_GroupPeerDB" ON "GroupPeerDB" (
	"last_update_timestamp"
);
CREATE INDEX IF NOT EXISTS "index_message_id_on_Filetransfer" ON "Filetransfer" (
	"message_id"
);
CREATE INDEX IF NOT EXISTS "index_message_id_on_Message" ON "Message" (
	"message_id"
);
CREATE INDEX IF NOT EXISTS "index_message_id_tox_on_ConferenceMessage" ON "ConferenceMessage" (
	"message_id_tox"
);
CREATE INDEX IF NOT EXISTS "index_message_id_tox_on_GroupMessage" ON "GroupMessage" (
	"message_id_tox"
);
CREATE INDEX IF NOT EXISTS "index_msg_at_relay_on_Message" ON "Message" (
	"msg_at_relay"
);
CREATE INDEX IF NOT EXISTS "index_msg_id_hash_on_GroupMessage" ON "GroupMessage" (
	"msg_id_hash"
);
CREATE INDEX IF NOT EXISTS "index_msg_id_hash_on_Message" ON "Message" (
	"msg_id_hash"
);
CREATE INDEX IF NOT EXISTS "index_msg_idv3_hash_on_Message" ON "Message" (
	"msg_idv3_hash"
);
CREATE INDEX IF NOT EXISTS "index_msg_version_on_Message" ON "Message" (
	"msg_version"
);
CREATE INDEX IF NOT EXISTS "index_msgv3_capability_on_FriendList" ON "FriendList" (
	"msgv3_capability"
);
CREATE INDEX IF NOT EXISTS "index_name_on_ConferenceDB" ON "ConferenceDB" (
	"name"
);
CREATE INDEX IF NOT EXISTS "index_name_on_GroupDB" ON "GroupDB" (
	"name"
);
CREATE INDEX IF NOT EXISTS "index_notification_silent_on_ConferenceDB" ON "ConferenceDB" (
	"notification_silent"
);
CREATE INDEX IF NOT EXISTS "index_notification_silent_on_FriendList" ON "FriendList" (
	"notification_silent"
);
CREATE INDEX IF NOT EXISTS "index_notification_silent_on_GroupDB" ON "GroupDB" (
	"notification_silent"
);
CREATE INDEX IF NOT EXISTS "index_num_on_BootstrapNodeEntryDB" ON "BootstrapNodeEntryDB" (
	"num"
);
CREATE INDEX IF NOT EXISTS "index_own_peer_number_on_ConferenceDB" ON "ConferenceDB" (
	"own_peer_number"
);
CREATE INDEX IF NOT EXISTS "index_own_peer_number_on_GroupDB" ON "GroupDB" (
	"own_peer_number"
);
CREATE INDEX IF NOT EXISTS "index_own_relay_on_RelayListDB" ON "RelayListDB" (
	"own_relay"
);
CREATE INDEX IF NOT EXISTS "index_path_name_on_FileDB" ON "FileDB" (
	"path_name"
);
CREATE INDEX IF NOT EXISTS "index_path_name_on_Filetransfer" ON "Filetransfer" (
	"path_name"
);
CREATE INDEX IF NOT EXISTS "index_path_name_on_GroupMessage" ON "GroupMessage" (
	"path_name"
);
CREATE INDEX IF NOT EXISTS "index_peer_count_on_ConferenceDB" ON "ConferenceDB" (
	"peer_count"
);
CREATE INDEX IF NOT EXISTS "index_peer_count_on_GroupDB" ON "GroupDB" (
	"peer_count"
);
CREATE INDEX IF NOT EXISTS "index_peer_name_on_ConferencePeerCacheDB" ON "ConferencePeerCacheDB" (
	"peer_name"
);
CREATE INDEX IF NOT EXISTS "index_peer_name_on_GroupPeerDB" ON "GroupPeerDB" (
	"peer_name"
);
CREATE INDEX IF NOT EXISTS "index_peer_pubkey_on_ConferencePeerCacheDB" ON "ConferencePeerCacheDB" (
	"peer_pubkey"
);
CREATE INDEX IF NOT EXISTS "index_port_on_BootstrapNodeEntryDB" ON "BootstrapNodeEntryDB" (
	"port"
);
CREATE INDEX IF NOT EXISTS "index_privacy_state_on_GroupDB" ON "GroupDB" (
	"privacy_state"
);
CREATE INDEX IF NOT EXISTS "index_private_message_on_GroupMessage" ON "GroupMessage" (
	"private_message"
);
CREATE INDEX IF NOT EXISTS "index_push_url_on_FriendList" ON "FriendList" (
	"push_url"
);
CREATE INDEX IF NOT EXISTS "index_raw_msgv2_bytes_on_Message" ON "Message" (
	"raw_msgv2_bytes"
);
CREATE INDEX IF NOT EXISTS "index_rcvd_timestamp_ms_on_Message" ON "Message" (
	"rcvd_timestamp_ms"
);
CREATE INDEX IF NOT EXISTS "index_rcvd_timestamp_on_ConferenceMessage" ON "ConferenceMessage" (
	"rcvd_timestamp"
);
CREATE INDEX IF NOT EXISTS "index_rcvd_timestamp_on_GroupMessage" ON "GroupMessage" (
	"rcvd_timestamp"
);
CREATE INDEX IF NOT EXISTS "index_rcvd_timestamp_on_Message" ON "Message" (
	"rcvd_timestamp"
);
CREATE INDEX IF NOT EXISTS "index_resend_count_on_Message" ON "Message" (
	"resend_count"
);
CREATE INDEX IF NOT EXISTS "index_send_retries_on_Message" ON "Message" (
	"send_retries"
);
CREATE INDEX IF NOT EXISTS "index_sent_privately_to_tox_group_peer_pubkey_on_GroupMessage" ON "GroupMessage" (
	"sent_privately_to_tox_group_peer_pubkey"
);
CREATE INDEX IF NOT EXISTS "index_sort_on_FriendList" ON "FriendList" (
	"sort"
);
CREATE INDEX IF NOT EXISTS "index_state_on_Filetransfer" ON "Filetransfer" (
	"state"
);
CREATE INDEX IF NOT EXISTS "index_state_on_Message" ON "Message" (
	"state"
);
CREATE INDEX IF NOT EXISTS "index_storage_frame_work_on_Filetransfer" ON "Filetransfer" (
	"storage_frame_work"
);
CREATE INDEX IF NOT EXISTS "index_storage_frame_work_on_GroupMessage" ON "GroupMessage" (
	"storage_frame_work"
);
CREATE INDEX IF NOT EXISTS "index_storage_frame_work_on_Message" ON "Message" (
	"storage_frame_work"
);
CREATE INDEX IF NOT EXISTS "index_sync_confirmations_on_GroupMessage" ON "GroupMessage" (
	"sync_confirmations"
);
CREATE INDEX IF NOT EXISTS "index_text_on_Message" ON "Message" (
	"text"
);
CREATE INDEX IF NOT EXISTS "index_topic_on_GroupDB" ON "GroupDB" (
	"topic"
);
CREATE INDEX IF NOT EXISTS "index_tox_conference_number_on_ConferenceDB" ON "ConferenceDB" (
	"tox_conference_number"
);
CREATE INDEX IF NOT EXISTS "index_tox_file_id_hex_on_Filetransfer" ON "Filetransfer" (
	"tox_file_id_hex"
);
CREATE INDEX IF NOT EXISTS "index_tox_friendpubkey_on_Message" ON "Message" (
	"tox_friendpubkey"
);
CREATE INDEX IF NOT EXISTS "index_tox_group_number_on_GroupDB" ON "GroupDB" (
	"tox_group_number"
);
CREATE INDEX IF NOT EXISTS "index_tox_group_peer_pubkey_on_GroupMessage" ON "GroupMessage" (
	"tox_group_peer_pubkey"
);
CREATE INDEX IF NOT EXISTS "index_tox_group_peer_pubkey_on_GroupPeerDB" ON "GroupPeerDB" (
	"tox_group_peer_pubkey"
);
CREATE INDEX IF NOT EXISTS "index_tox_group_peer_pubkey_syncer_01_on_GroupMessage" ON "GroupMessage" (
	"tox_group_peer_pubkey_syncer_01"
);
CREATE INDEX IF NOT EXISTS "index_tox_group_peer_pubkey_syncer_01_sent_timestamp_on_GroupMessage" ON "GroupMessage" (
	"tox_group_peer_pubkey_syncer_01_sent_timestamp"
);
CREATE INDEX IF NOT EXISTS "index_tox_group_peer_pubkey_syncer_02_on_GroupMessage" ON "GroupMessage" (
	"tox_group_peer_pubkey_syncer_02"
);
CREATE INDEX IF NOT EXISTS "index_tox_group_peer_pubkey_syncer_02_sent_timestamp_on_GroupMessage" ON "GroupMessage" (
	"tox_group_peer_pubkey_syncer_02_sent_timestamp"
);
CREATE INDEX IF NOT EXISTS "index_tox_group_peer_pubkey_syncer_03_on_GroupMessage" ON "GroupMessage" (
	"tox_group_peer_pubkey_syncer_03"
);
CREATE INDEX IF NOT EXISTS "index_tox_group_peer_pubkey_syncer_03_sent_timestamp_on_GroupMessage" ON "GroupMessage" (
	"tox_group_peer_pubkey_syncer_03_sent_timestamp"
);
CREATE INDEX IF NOT EXISTS "index_tox_group_peer_role_on_GroupMessage" ON "GroupMessage" (
	"tox_group_peer_role"
);
CREATE INDEX IF NOT EXISTS "index_tox_group_peername_on_GroupMessage" ON "GroupMessage" (
	"tox_group_peername"
);
CREATE INDEX IF NOT EXISTS "index_tox_peername_on_ConferenceMessage" ON "ConferenceMessage" (
	"tox_peername"
);
CREATE INDEX IF NOT EXISTS "index_tox_peerpubkey_on_ConferenceMessage" ON "ConferenceMessage" (
	"tox_peerpubkey"
);
CREATE INDEX IF NOT EXISTS "index_tox_public_key_string_of_owner_on_RelayListDB" ON "RelayListDB" (
	"tox_public_key_string_of_owner"
);
CREATE INDEX IF NOT EXISTS "index_tox_public_key_string_on_FileDB" ON "FileDB" (
	"tox_public_key_string"
);
CREATE INDEX IF NOT EXISTS "index_tox_public_key_string_on_Filetransfer" ON "Filetransfer" (
	"tox_public_key_string"
);
CREATE INDEX IF NOT EXISTS "index_udp_node_on_BootstrapNodeEntryDB" ON "BootstrapNodeEntryDB" (
	"udp_node"
);
CREATE INDEX IF NOT EXISTS "index_value_on_TRIFADatabaseGlobals" ON "TRIFADatabaseGlobals" (
	"value"
);
CREATE INDEX IF NOT EXISTS "index_value_on_TRIFADatabaseGlobalsNew" ON "TRIFADatabaseGlobalsNew" (
	"value"
);
CREATE INDEX IF NOT EXISTS "index_was_synced_on_ConferenceMessage" ON "ConferenceMessage" (
	"was_synced"
);
CREATE INDEX IF NOT EXISTS "index_was_synced_on_GroupMessage" ON "GroupMessage" (
	"was_synced"
);
CREATE INDEX IF NOT EXISTS "index_who_invited__tox_public_key_string_on_ConferenceDB" ON "ConferenceDB" (
	"who_invited__tox_public_key_string"
);
CREATE INDEX IF NOT EXISTS "index_who_invited__tox_public_key_string_on_GroupDB" ON "GroupDB" (
	"who_invited__tox_public_key_string"
);
COMMIT;
