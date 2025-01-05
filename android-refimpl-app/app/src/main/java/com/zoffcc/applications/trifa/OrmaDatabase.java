package com.zoffcc.applications.trifa;

import android.database.Cursor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class OrmaDatabase extends com.zoffcc.applications.sorm.OrmaDatabase
{
    private final String TAG = "OrmaDatabase_:";

    public OrmaDatabase(final String db_file_path, final String secrect_key, boolean wal_mode)
    {
        java.io.File db = new java.io.File(db_file_path);
        try
        {
            open_db(db_file_path, secrect_key, wal_mode);
            boolean db_open = check_db_open();
            if (!db_open)
            {
                System.out.println(TAG + "error opening DB");
                throw new RuntimeException();
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        make_db_tables();
    }

    void make_db_tables()
    {
        final String init_schema = "BEGIN TRANSACTION;\n" + "CREATE TABLE IF NOT EXISTS \"BootstrapNodeEntryDB\" (\n" +
                                   "\t\"num\"\tINTEGER ,\n" + "\t\"udp_node\"\tBOOLEAN ,\n" +
                                   "\t\"ip\"\tTEXT ,\n" + "\t\"port\"\tINTEGER ,\n" +
                                   "\t\"key_hex\"\tTEXT ,\n" + "\t\"id\"\tINTEGER,\n" +
                                   "\tPRIMARY KEY(\"id\" AUTOINCREMENT)\n" + ");\n" +
                                   "CREATE TABLE IF NOT EXISTS \"ConferenceDB\" (\n" +
                                   "\t\"who_invited__tox_public_key_string\"\tTEXT ,\n" +
                                   "\t\"name\"\tTEXT,\n" + "\t\"peer_count\"\tINTEGER  DEFAULT -1,\n" +
                                   "\t\"own_peer_number\"\tINTEGER  DEFAULT -1,\n" +
                                   "\t\"kind\"\tINTEGER  DEFAULT 0,\n" +
                                   "\t\"tox_conference_number\"\tINTEGER  DEFAULT -1,\n" +
                                   "\t\"conference_active\"\tBOOLEAN  DEFAULT false,\n" +
                                   "\t\"notification_silent\"\tBOOLEAN DEFAULT false,\n" +
                                   "\t\"conference_identifier\"\tTEXT,\n" +
                                   "\tPRIMARY KEY(\"conference_identifier\")\n" + ");\n" +
                                   "CREATE TABLE IF NOT EXISTS \"ConferenceMessage\" (\n" +
                                   "\t\"message_id_tox\"\tTEXT,\n" +
                                   "\t\"conference_identifier\"\tTEXT  DEFAULT -1,\n" +
                                   "\t\"tox_peerpubkey\"\tTEXT ,\n" + "\t\"tox_peername\"\tTEXT,\n" +
                                   "\t\"direction\"\tINTEGER ,\n" +
                                   "\t\"TOX_MESSAGE_TYPE\"\tINTEGER ,\n" +
                                   "\t\"TRIFA_MESSAGE_TYPE\"\tINTEGER  DEFAULT 0,\n" +
                                   "\t\"sent_timestamp\"\tINTEGER,\n" + "\t\"rcvd_timestamp\"\tINTEGER,\n" +
                                   "\t\"read\"\tBOOLEAN ,\n" + "\t\"is_new\"\tBOOLEAN ,\n" +
                                   "\t\"text\"\tTEXT,\n" + "\t\"was_synced\"\tBOOLEAN,\n" + "\t\"id\"\tINTEGER,\n" +
                                   "\tPRIMARY KEY(\"id\" AUTOINCREMENT)\n" + ");\n" +
                                   "CREATE TABLE IF NOT EXISTS \"ConferencePeerCacheDB\" (\n" +
                                   "\t\"conference_identifier\"\tTEXT ,\n" +
                                   "\t\"peer_pubkey\"\tTEXT ,\n" + "\t\"peer_name\"\tTEXT ,\n" +
                                   "\t\"last_update_timestamp\"\tINTEGER  DEFAULT -1,\n" +
                                   "\t\"id\"\tINTEGER,\n" + "\tPRIMARY KEY(\"id\" AUTOINCREMENT)\n" + ");\n" +
                                   "CREATE TABLE IF NOT EXISTS \"FileDB\" (\n" + "\t\"kind\"\tINTEGER ,\n" +
                                   "\t\"direction\"\tINTEGER ,\n" +
                                   "\t\"tox_public_key_string\"\tTEXT ,\n" +
                                   "\t\"path_name\"\tTEXT ,\n" + "\t\"file_name\"\tTEXT ,\n" +
                                   "\t\"filesize\"\tINTEGER  DEFAULT -1,\n" +
                                   "\t\"is_in_VFS\"\tBOOLEAN  DEFAULT true,\n" + "\t\"id\"\tINTEGER,\n" +
                                   "\tPRIMARY KEY(\"id\" AUTOINCREMENT)\n" + ");\n" +
                                   "CREATE TABLE IF NOT EXISTS \"Filetransfer\" (\n" +
                                   "\t\"tox_public_key_string\"\tTEXT ,\n" +
                                   "\t\"direction\"\tINTEGER ,\n" + "\t\"file_number\"\tINTEGER ,\n" +
                                   "\t\"kind\"\tINTEGER ,\n" + "\t\"state\"\tINTEGER ,\n" +
                                   "\t\"ft_accepted\"\tBOOLEAN  DEFAULT false,\n" +
                                   "\t\"ft_outgoing_started\"\tBOOLEAN  DEFAULT false,\n" +
                                   "\t\"path_name\"\tTEXT ,\n" + "\t\"file_name\"\tTEXT ,\n" +
                                   "\t\"fos_open\"\tBOOLEAN  DEFAULT false,\n" +
                                   "\t\"filesize\"\tINTEGER  DEFAULT -1,\n" +
                                   "\t\"current_position\"\tINTEGER  DEFAULT 0,\n" +
                                   "\t\"message_id\"\tINTEGER  DEFAULT -1,\n" +
                                   "\t\"storage_frame_work\"\tBOOLEAN  DEFAULT false,\n" +
                                   "\t\"tox_file_id_hex\"\tTEXT,\n" + "\t\"id\"\tINTEGER,\n" +
                                   "\tPRIMARY KEY(\"id\" AUTOINCREMENT)\n" + ");\n" +
                                   "CREATE TABLE IF NOT EXISTS \"FriendList\" (\n" + "\t\"name\"\tTEXT,\n" +
                                   "\t\"alias_name\"\tTEXT,\n" + "\t\"status_message\"\tTEXT,\n" +
                                   "\t\"TOX_CONNECTION\"\tINTEGER  DEFAULT 0,\n" +
                                   "\t\"TOX_CONNECTION_real\"\tINTEGER  DEFAULT 0,\n" +
                                   "\t\"TOX_CONNECTION_on_off\"\tINTEGER  DEFAULT 0,\n" +
                                   "\t\"TOX_CONNECTION_on_off_real\"\tINTEGER  DEFAULT 0,\n" +
                                   "\t\"TOX_USER_STATUS\"\tINTEGER  DEFAULT 0,\n" +
                                   "\t\"avatar_pathname\"\tTEXT,\n" + "\t\"avatar_filename\"\tTEXT,\n" +
                                   "\t\"avatar_ftid_hex\"\tTEXT,\n" + "\t\"avatar_update\"\tBOOLEAN DEFAULT false,\n" +
                                   "\t\"avatar_update_timestamp\"\tINTEGER  DEFAULT -1,\n" +
                                   "\t\"notification_silent\"\tBOOLEAN DEFAULT false,\n" +
                                   "\t\"sort\"\tINTEGER  DEFAULT 0,\n" +
                                   "\t\"last_online_timestamp\"\tINTEGER  DEFAULT -1,\n" +
                                   "\t\"last_online_timestamp_real\"\tINTEGER  DEFAULT -1,\n" +
                                   "\t\"added_timestamp\"\tINTEGER  DEFAULT -1,\n" +
                                   "\t\"is_relay\"\tBOOLEAN DEFAULT false,\n" + "\t\"push_url\"\tTEXT,\n" +
                                   "\t\"ip_addr_str\"\tTEXT,\n" + "\t\"capabilities\"\tINTEGER  DEFAULT 0,\n" +
                                   "\t\"msgv3_capability\"\tINTEGER  DEFAULT 0,\n" +
                                   "\t\"tox_public_key_string\"\tTEXT,\n" +
                                   "\tPRIMARY KEY(\"tox_public_key_string\")\n" + ");\n" +
                                   "CREATE TABLE IF NOT EXISTS \"GroupDB\" (\n" +
                                   "\t\"who_invited__tox_public_key_string\"\tTEXT ,\n" +
                                   "\t\"name\"\tTEXT,\n" + "\t\"topic\"\tTEXT,\n" +
                                   "\t\"peer_count\"\tINTEGER  DEFAULT -1,\n" +
                                   "\t\"own_peer_number\"\tINTEGER  DEFAULT -1,\n" +
                                   "\t\"privacy_state\"\tINTEGER  DEFAULT 0,\n" +
                                   "\t\"tox_group_number\"\tINTEGER  DEFAULT -1,\n" +
                                   "\t\"group_active\"\tBOOLEAN  DEFAULT false,\n" +
                                   "\t\"group_we_left\"\tBOOLEAN  DEFAULT false,\n" +
                                   "\t\"notification_silent\"\tBOOLEAN DEFAULT false,\n" +
                                   "\t\"group_identifier\"\tTEXT,\n" + "\tPRIMARY KEY(\"group_identifier\")\n" +
                                   ");\n" + "CREATE TABLE IF NOT EXISTS \"GroupMessage\" (\n" +
                                   "\t\"message_id_tox\"\tTEXT,\n" +
                                   "\t\"group_identifier\"\tTEXT  DEFAULT -1,\n" +
                                   "\t\"tox_group_peer_pubkey\"\tTEXT ,\n" +
                                   "\t\"tox_group_peer_role\"\tINTEGER  DEFAULT -1,\n" +
                                   "\t\"private_message\"\tINTEGER,\n" + "\t\"tox_group_peername\"\tTEXT,\n" +
                                   "\t\"direction\"\tINTEGER ,\n" +
                                   "\t\"TOX_MESSAGE_TYPE\"\tINTEGER ,\n" +
                                   "\t\"TRIFA_MESSAGE_TYPE\"\tINTEGER  DEFAULT 0,\n" +
                                   "\t\"sent_timestamp\"\tINTEGER,\n" + "\t\"rcvd_timestamp\"\tINTEGER,\n" +
                                   "\t\"read\"\tBOOLEAN ,\n" + "\t\"is_new\"\tBOOLEAN ,\n" +
                                   "\t\"text\"\tTEXT,\n" + "\t\"was_synced\"\tBOOLEAN,\n" +
                                   "\t\"TRIFA_SYNC_TYPE\"\tINTEGER,\n" +
                                   "\t\"sync_confirmations\"\tINTEGER  DEFAULT 0,\n" +
                                   "\t\"tox_group_peer_pubkey_syncer_01\"\tTEXT,\n" +
                                   "\t\"tox_group_peer_pubkey_syncer_02\"\tTEXT,\n" +
                                   "\t\"tox_group_peer_pubkey_syncer_03\"\tTEXT,\n" +
                                   "\t\"tox_group_peer_pubkey_syncer_01_sent_timestamp\"\tINTEGER,\n" +
                                   "\t\"tox_group_peer_pubkey_syncer_02_sent_timestamp\"\tINTEGER,\n" +
                                   "\t\"tox_group_peer_pubkey_syncer_03_sent_timestamp\"\tINTEGER,\n" +
                                   "\t\"msg_id_hash\"\tTEXT,\n" +
                                   "\t\"sent_privately_to_tox_group_peer_pubkey\"\tTEXT,\n" +
                                   "\t\"path_name\"\tTEXT,\n" + "\t\"file_name\"\tTEXT,\n" +
                                   "\t\"filename_fullpath\"\tTEXT,\n" +
                                   "\t\"filesize\"\tINTEGER  DEFAULT -1,\n" +
                                   "\t\"storage_frame_work\"\tBOOLEAN  DEFAULT false,\n" +
                                   "\t\"id\"\tINTEGER,\n" + "\tPRIMARY KEY(\"id\" AUTOINCREMENT)\n" + ");\n" +
                                   "CREATE TABLE IF NOT EXISTS \"GroupPeerDB\" (\n" +
                                   "\t\"group_identifier\"\tTEXT ,\n" +
                                   "\t\"tox_group_peer_pubkey\"\tTEXT ,\n" + "\t\"peer_name\"\tTEXT,\n" +
                                   "\t\"last_update_timestamp\"\tINTEGER  DEFAULT -1,\n" +
                                   "\t\"first_join_timestamp\"\tINTEGER  DEFAULT -1,\n" +
                                   "\t\"Tox_Group_Role\"\tINTEGER  DEFAULT 2,\n" + "\t\"id\"\tINTEGER,\n" +
                                   "\tPRIMARY KEY(\"id\" AUTOINCREMENT)\n" + ");\n" +
                                   "CREATE TABLE IF NOT EXISTS \"Message\" (\n" +
                                   "\t\"message_id\"\tINTEGER ,\n" +
                                   "\t\"tox_friendpubkey\"\tTEXT ,\n" + "\t\"direction\"\tINTEGER ,\n" +
                                   "\t\"TOX_MESSAGE_TYPE\"\tINTEGER ,\n" +
                                   "\t\"TRIFA_MESSAGE_TYPE\"\tINTEGER  DEFAULT 0,\n" +
                                   "\t\"state\"\tINTEGER  DEFAULT 1,\n" +
                                   "\t\"ft_accepted\"\tBOOLEAN  DEFAULT false,\n" +
                                   "\t\"ft_outgoing_started\"\tBOOLEAN  DEFAULT false,\n" +
                                   "\t\"filedb_id\"\tINTEGER  DEFAULT -1,\n" +
                                   "\t\"filetransfer_id\"\tINTEGER  DEFAULT -1,\n" +
                                   "\t\"sent_timestamp\"\tINTEGER DEFAULT 0,\n" +
                                   "\t\"sent_timestamp_ms\"\tINTEGER DEFAULT 0,\n" +
                                   "\t\"rcvd_timestamp\"\tINTEGER DEFAULT 0,\n" +
                                   "\t\"rcvd_timestamp_ms\"\tINTEGER DEFAULT 0,\n" + "\t\"read\"\tBOOLEAN ,\n" +
                                   "\t\"send_retries\"\tINTEGER  DEFAULT 0,\n" +
                                   "\t\"is_new\"\tBOOLEAN ,\n" + "\t\"text\"\tTEXT,\n" +
                                   "\t\"filename_fullpath\"\tTEXT,\n" + "\t\"msg_id_hash\"\tTEXT,\n" +
                                   "\t\"raw_msgv2_bytes\"\tTEXT,\n" +
                                   "\t\"msg_version\"\tINTEGER  DEFAULT 0,\n" +
                                   "\t\"resend_count\"\tINTEGER  DEFAULT 4,\n" +
                                   "\t\"storage_frame_work\"\tBOOLEAN  DEFAULT false,\n" +
                                   "\t\"ft_outgoing_queued\"\tBOOLEAN  DEFAULT false,\n" +
                                   "\t\"msg_at_relay\"\tBOOLEAN  DEFAULT false,\n" +
                                   "\t\"msg_idv3_hash\"\tTEXT,\n" + "\t\"sent_push\"\tINTEGER,\n" +
                                   "\t\"filetransfer_kind\"\tINTEGER DEFAULT 0,\n" + "\t\"id\"\tINTEGER,\n" +
                                   "\tPRIMARY KEY(\"id\" AUTOINCREMENT)\n" + ");\n" +
                                   "CREATE TABLE IF NOT EXISTS \"RelayListDB\" (\n" +
                                   "\t\"TOX_CONNECTION\"\tINTEGER  DEFAULT 0,\n" +
                                   "\t\"TOX_CONNECTION_on_off\"\tINTEGER  DEFAULT 0,\n" +
                                   "\t\"own_relay\"\tBOOLEAN  DEFAULT false,\n" +
                                   "\t\"last_online_timestamp\"\tINTEGER  DEFAULT -1,\n" +
                                   "\t\"tox_public_key_string_of_owner\"\tTEXT,\n" +
                                   "\t\"tox_public_key_string\"\tTEXT,\n" +
                                   "\tPRIMARY KEY(\"tox_public_key_string\")\n" + ");\n" +
                                   "CREATE TABLE IF NOT EXISTS \"TRIFADatabaseGlobals\" (\n" +
                                   "\t\"key\"\tTEXT ,\n" + "\t\"value\"\tTEXT \n" + ");\n" +
                                   "CREATE TABLE IF NOT EXISTS \"TRIFADatabaseGlobalsNew\" (\n" +
                                   "\t\"value\"\tTEXT ,\n" + "\t\"key\"\tTEXT,\n" + "\tPRIMARY KEY(\"key\")\n" +
                                   ");\n" + "CREATE TABLE IF NOT EXISTS \"orma_migration_steps\" (\n" +
                                   "\t\"id\"\tINTEGER,\n" + "\t\"version\"\tINTEGER ,\n" +
                                   "\t\"sql\"\tTEXT,\n" +
                                   "\t\"created_timestamp\"\tDATETIME  DEFAULT CURRENT_TIMESTAMP,\n" +
                                   "\tPRIMARY KEY(\"id\" AUTOINCREMENT)\n" + ");\n" +
                                   "CREATE TABLE IF NOT EXISTS \"orma_schema_diff_migration_2\" (\n" +
                                   "\t\"id\"\tINTEGER,\n" + "\t\"db_version\"\tINTEGER ,\n" +
                                   "\t\"version_name\"\tTEXT ,\n" + "\t\"version_code\"\tINTEGER ,\n" +
                                   "\t\"schema_hash\"\tTEXT ,\n" + "\t\"sql\"\tTEXT,\n" +
                                   "\t\"args\"\tTEXT,\n" +
                                   "\t\"created_timestamp\"\tDATETIME  DEFAULT CURRENT_TIMESTAMP,\n" +
                                   "\tPRIMARY KEY(\"id\" AUTOINCREMENT)\n" + ");\n" +
                                   "CREATE TABLE IF NOT EXISTS \"sqlite_stat4\" (\n" + "\t\"tbl\"\t,\n" +
                                   "\t\"idx\"\t,\n" + "\t\"neq\"\t,\n" + "\t\"nlt\"\t,\n" + "\t\"ndlt\"\t,\n" +
                                   "\t\"sample\"\t\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_TOX_CONNECTION_on_FriendList\" ON \"FriendList\" (\n" +
                                   "\t\"TOX_CONNECTION\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_TOX_CONNECTION_on_RelayListDB\" ON \"RelayListDB\" (\n" +
                                   "\t\"TOX_CONNECTION\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_TOX_CONNECTION_on_off_on_FriendList\" ON \"FriendList\" (\n" +
                                   "\t\"TOX_CONNECTION_on_off\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_TOX_CONNECTION_on_off_on_RelayListDB\" ON \"RelayListDB\" (\n" +
                                   "\t\"TOX_CONNECTION_on_off\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_TOX_CONNECTION_on_off_real_on_FriendList\" ON \"FriendList\" (\n" +
                                   "\t\"TOX_CONNECTION_on_off_real\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_TOX_CONNECTION_real_on_FriendList\" ON \"FriendList\" (\n" +
                                   "\t\"TOX_CONNECTION_real\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_TOX_MESSAGE_TYPE_on_ConferenceMessage\" ON \"ConferenceMessage\" (\n" +
                                   "\t\"TOX_MESSAGE_TYPE\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_TOX_MESSAGE_TYPE_on_GroupMessage\" ON \"GroupMessage\" (\n" +
                                   "\t\"TOX_MESSAGE_TYPE\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_TOX_MESSAGE_TYPE_on_Message\" ON \"Message\" (\n" +
                                   "\t\"TOX_MESSAGE_TYPE\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_TOX_USER_STATUS_on_FriendList\" ON \"FriendList\" (\n" +
                                   "\t\"TOX_USER_STATUS\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_TRIFA_MESSAGE_TYPE_on_ConferenceMessage\" ON \"ConferenceMessage\" (\n" +
                                   "\t\"TRIFA_MESSAGE_TYPE\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_TRIFA_MESSAGE_TYPE_on_GroupMessage\" ON \"GroupMessage\" (\n" +
                                   "\t\"TRIFA_MESSAGE_TYPE\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_TRIFA_MESSAGE_TYPE_on_Message\" ON \"Message\" (\n" +
                                   "\t\"TRIFA_MESSAGE_TYPE\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_TRIFA_SYNC_TYPE_on_GroupMessage\" ON \"GroupMessage\" (\n" +
                                   "\t\"TRIFA_SYNC_TYPE\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_Tox_Group_Role_on_GroupPeerDB\" ON \"GroupPeerDB\" (\n" +
                                   "\t\"Tox_Group_Role\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_added_timestamp_on_FriendList\" ON \"FriendList\" (\n" +
                                   "\t\"added_timestamp\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_alias_name_on_FriendList\" ON \"FriendList\" (\n" +
                                   "\t\"alias_name\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_avatar_ftid_hex_on_FriendList\" ON \"FriendList\" (\n" +
                                   "\t\"avatar_ftid_hex\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_avatar_update_on_FriendList\" ON \"FriendList\" (\n" +
                                   "\t\"avatar_update\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_avatar_update_timestamp_on_FriendList\" ON \"FriendList\" (\n" +
                                   "\t\"avatar_update_timestamp\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_capabilities_on_FriendList\" ON \"FriendList\" (\n" +
                                   "\t\"capabilities\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_conference_active_on_ConferenceDB\" ON \"ConferenceDB\" (\n" +
                                   "\t\"conference_active\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_conference_identifier_on_ConferenceMessage\" ON \"ConferenceMessage\" (\n" +
                                   "\t\"conference_identifier\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_conference_identifier_on_ConferencePeerCacheDB\" ON \"ConferencePeerCacheDB\" (\n" +
                                   "\t\"conference_identifier\"\n" + ");\n" +
                                   "CREATE UNIQUE INDEX IF NOT EXISTS \"index_conference_identifier_peer_pubkey_on_ConferencePeerCacheDB\" ON \"ConferencePeerCacheDB\" (\n" +
                                   "\t\"conference_identifier\",\n" + "\t\"peer_pubkey\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_direction_on_ConferenceMessage\" ON \"ConferenceMessage\" (\n" +
                                   "\t\"direction\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_direction_on_FileDB\" ON \"FileDB\" (\n" +
                                   "\t\"direction\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_direction_on_Filetransfer\" ON \"Filetransfer\" (\n" +
                                   "\t\"direction\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_direction_on_GroupMessage\" ON \"GroupMessage\" (\n" +
                                   "\t\"direction\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_direction_on_Message\" ON \"Message\" (\n" +
                                   "\t\"direction\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_file_name_on_FileDB\" ON \"FileDB\" (\n" +
                                   "\t\"file_name\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_file_name_on_Filetransfer\" ON \"Filetransfer\" (\n" +
                                   "\t\"file_name\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_file_name_on_GroupMessage\" ON \"GroupMessage\" (\n" +
                                   "\t\"file_name\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_file_number_on_Filetransfer\" ON \"Filetransfer\" (\n" +
                                   "\t\"file_number\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_filedb_id_on_Message\" ON \"Message\" (\n" +
                                   "\t\"filedb_id\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_filesize_on_FileDB\" ON \"FileDB\" (\n" +
                                   "\t\"filesize\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_filesize_on_GroupMessage\" ON \"GroupMessage\" (\n" +
                                   "\t\"filesize\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_filetransfer_id_on_Message\" ON \"Message\" (\n" +
                                   "\t\"filetransfer_id\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_first_join_timestamp_on_GroupPeerDB\" ON \"GroupPeerDB\" (\n" +
                                   "\t\"first_join_timestamp\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_ft_accepted_on_Filetransfer\" ON \"Filetransfer\" (\n" +
                                   "\t\"ft_accepted\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_ft_accepted_on_Message\" ON \"Message\" (\n" +
                                   "\t\"ft_accepted\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_ft_outgoing_queued_on_Message\" ON \"Message\" (\n" +
                                   "\t\"ft_outgoing_queued\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_ft_outgoing_started_on_Filetransfer\" ON \"Filetransfer\" (\n" +
                                   "\t\"ft_outgoing_started\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_ft_outgoing_started_on_Message\" ON \"Message\" (\n" +
                                   "\t\"ft_outgoing_started\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_group_active_on_GroupDB\" ON \"GroupDB\" (\n" +
                                   "\t\"group_active\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_group_identifier_on_GroupMessage\" ON \"GroupMessage\" (\n" +
                                   "\t\"group_identifier\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_group_identifier_on_GroupPeerDB\" ON \"GroupPeerDB\" (\n" +
                                   "\t\"group_identifier\"\n" + ");\n" +
                                   "CREATE UNIQUE INDEX IF NOT EXISTS \"index_group_identifier_tox_group_peer_pubkey_on_GroupPeerDB\" ON \"GroupPeerDB\" (\n" +
                                   "\t\"group_identifier\",\n" + "\t\"tox_group_peer_pubkey\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_group_we_left_on_GroupDB\" ON \"GroupDB\" (\n" +
                                   "\t\"group_we_left\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_ip_addr_str_on_FriendList\" ON \"FriendList\" (\n" +
                                   "\t\"ip_addr_str\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_ip_on_BootstrapNodeEntryDB\" ON \"BootstrapNodeEntryDB\" (\n" +
                                   "\t\"ip\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_is_in_VFS_on_FileDB\" ON \"FileDB\" (\n" +
                                   "\t\"is_in_VFS\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_is_new_on_ConferenceMessage\" ON \"ConferenceMessage\" (\n" +
                                   "\t\"is_new\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_is_new_on_GroupMessage\" ON \"GroupMessage\" (\n" +
                                   "\t\"is_new\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_is_new_on_Message\" ON \"Message\" (\n" +
                                   "\t\"is_new\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_is_relay_on_FriendList\" ON \"FriendList\" (\n" +
                                   "\t\"is_relay\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_key_hex_on_BootstrapNodeEntryDB\" ON \"BootstrapNodeEntryDB\" (\n" +
                                   "\t\"key_hex\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_key_on_TRIFADatabaseGlobals\" ON \"TRIFADatabaseGlobals\" (\n" +
                                   "\t\"key\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_kind_on_ConferenceDB\" ON \"ConferenceDB\" (\n" +
                                   "\t\"kind\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_kind_on_FileDB\" ON \"FileDB\" (\n" +
                                   "\t\"kind\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_kind_on_Filetransfer\" ON \"Filetransfer\" (\n" +
                                   "\t\"kind\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_last_online_timestamp_on_FriendList\" ON \"FriendList\" (\n" +
                                   "\t\"last_online_timestamp\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_last_online_timestamp_on_RelayListDB\" ON \"RelayListDB\" (\n" +
                                   "\t\"last_online_timestamp\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_last_online_timestamp_real_on_FriendList\" ON \"FriendList\" (\n" +
                                   "\t\"last_online_timestamp_real\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_last_update_timestamp_on_ConferencePeerCacheDB\" ON \"ConferencePeerCacheDB\" (\n" +
                                   "\t\"last_update_timestamp\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_last_update_timestamp_on_GroupPeerDB\" ON \"GroupPeerDB\" (\n" +
                                   "\t\"last_update_timestamp\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_message_id_on_Filetransfer\" ON \"Filetransfer\" (\n" +
                                   "\t\"message_id\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_message_id_on_Message\" ON \"Message\" (\n" +
                                   "\t\"message_id\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_message_id_tox_on_ConferenceMessage\" ON \"ConferenceMessage\" (\n" +
                                   "\t\"message_id_tox\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_message_id_tox_on_GroupMessage\" ON \"GroupMessage\" (\n" +
                                   "\t\"message_id_tox\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_msg_at_relay_on_Message\" ON \"Message\" (\n" +
                                   "\t\"msg_at_relay\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_msg_id_hash_on_GroupMessage\" ON \"GroupMessage\" (\n" +
                                   "\t\"msg_id_hash\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_msg_id_hash_on_Message\" ON \"Message\" (\n" +
                                   "\t\"msg_id_hash\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_msg_idv3_hash_on_Message\" ON \"Message\" (\n" +
                                   "\t\"msg_idv3_hash\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_msg_version_on_Message\" ON \"Message\" (\n" +
                                   "\t\"msg_version\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_msgv3_capability_on_FriendList\" ON \"FriendList\" (\n" +
                                   "\t\"msgv3_capability\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_name_on_ConferenceDB\" ON \"ConferenceDB\" (\n" +
                                   "\t\"name\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_name_on_GroupDB\" ON \"GroupDB\" (\n" +
                                   "\t\"name\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_notification_silent_on_ConferenceDB\" ON \"ConferenceDB\" (\n" +
                                   "\t\"notification_silent\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_notification_silent_on_FriendList\" ON \"FriendList\" (\n" +
                                   "\t\"notification_silent\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_notification_silent_on_GroupDB\" ON \"GroupDB\" (\n" +
                                   "\t\"notification_silent\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_num_on_BootstrapNodeEntryDB\" ON \"BootstrapNodeEntryDB\" (\n" +
                                   "\t\"num\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_own_peer_number_on_ConferenceDB\" ON \"ConferenceDB\" (\n" +
                                   "\t\"own_peer_number\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_own_peer_number_on_GroupDB\" ON \"GroupDB\" (\n" +
                                   "\t\"own_peer_number\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_own_relay_on_RelayListDB\" ON \"RelayListDB\" (\n" +
                                   "\t\"own_relay\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_path_name_on_FileDB\" ON \"FileDB\" (\n" +
                                   "\t\"path_name\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_path_name_on_Filetransfer\" ON \"Filetransfer\" (\n" +
                                   "\t\"path_name\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_path_name_on_GroupMessage\" ON \"GroupMessage\" (\n" +
                                   "\t\"path_name\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_peer_count_on_ConferenceDB\" ON \"ConferenceDB\" (\n" +
                                   "\t\"peer_count\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_peer_count_on_GroupDB\" ON \"GroupDB\" (\n" +
                                   "\t\"peer_count\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_peer_name_on_ConferencePeerCacheDB\" ON \"ConferencePeerCacheDB\" (\n" +
                                   "\t\"peer_name\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_peer_name_on_GroupPeerDB\" ON \"GroupPeerDB\" (\n" +
                                   "\t\"peer_name\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_peer_pubkey_on_ConferencePeerCacheDB\" ON \"ConferencePeerCacheDB\" (\n" +
                                   "\t\"peer_pubkey\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_port_on_BootstrapNodeEntryDB\" ON \"BootstrapNodeEntryDB\" (\n" +
                                   "\t\"port\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_privacy_state_on_GroupDB\" ON \"GroupDB\" (\n" +
                                   "\t\"privacy_state\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_private_message_on_GroupMessage\" ON \"GroupMessage\" (\n" +
                                   "\t\"private_message\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_push_url_on_FriendList\" ON \"FriendList\" (\n" +
                                   "\t\"push_url\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_raw_msgv2_bytes_on_Message\" ON \"Message\" (\n" +
                                   "\t\"raw_msgv2_bytes\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_rcvd_timestamp_ms_on_Message\" ON \"Message\" (\n" +
                                   "\t\"rcvd_timestamp_ms\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_rcvd_timestamp_on_ConferenceMessage\" ON \"ConferenceMessage\" (\n" +
                                   "\t\"rcvd_timestamp\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_rcvd_timestamp_on_GroupMessage\" ON \"GroupMessage\" (\n" +
                                   "\t\"rcvd_timestamp\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_rcvd_timestamp_on_Message\" ON \"Message\" (\n" +
                                   "\t\"rcvd_timestamp\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_resend_count_on_Message\" ON \"Message\" (\n" +
                                   "\t\"resend_count\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_send_retries_on_Message\" ON \"Message\" (\n" +
                                   "\t\"send_retries\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_sent_privately_to_tox_group_peer_pubkey_on_GroupMessage\" ON \"GroupMessage\" (\n" +
                                   "\t\"sent_privately_to_tox_group_peer_pubkey\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_sort_on_FriendList\" ON \"FriendList\" (\n" +
                                   "\t\"sort\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_state_on_Filetransfer\" ON \"Filetransfer\" (\n" +
                                   "\t\"state\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_state_on_Message\" ON \"Message\" (\n" +
                                   "\t\"state\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_storage_frame_work_on_Filetransfer\" ON \"Filetransfer\" (\n" +
                                   "\t\"storage_frame_work\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_storage_frame_work_on_GroupMessage\" ON \"GroupMessage\" (\n" +
                                   "\t\"storage_frame_work\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_storage_frame_work_on_Message\" ON \"Message\" (\n" +
                                   "\t\"storage_frame_work\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_sync_confirmations_on_GroupMessage\" ON \"GroupMessage\" (\n" +
                                   "\t\"sync_confirmations\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_text_on_Message\" ON \"Message\" (\n" +
                                   "\t\"text\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_topic_on_GroupDB\" ON \"GroupDB\" (\n" +
                                   "\t\"topic\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_tox_conference_number_on_ConferenceDB\" ON \"ConferenceDB\" (\n" +
                                   "\t\"tox_conference_number\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_tox_file_id_hex_on_Filetransfer\" ON \"Filetransfer\" (\n" +
                                   "\t\"tox_file_id_hex\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_tox_friendpubkey_on_Message\" ON \"Message\" (\n" +
                                   "\t\"tox_friendpubkey\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_tox_group_number_on_GroupDB\" ON \"GroupDB\" (\n" +
                                   "\t\"tox_group_number\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_tox_group_peer_pubkey_on_GroupMessage\" ON \"GroupMessage\" (\n" +
                                   "\t\"tox_group_peer_pubkey\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_tox_group_peer_pubkey_on_GroupPeerDB\" ON \"GroupPeerDB\" (\n" +
                                   "\t\"tox_group_peer_pubkey\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_tox_group_peer_pubkey_syncer_01_on_GroupMessage\" ON \"GroupMessage\" (\n" +
                                   "\t\"tox_group_peer_pubkey_syncer_01\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_tox_group_peer_pubkey_syncer_01_sent_timestamp_on_GroupMessage\" ON \"GroupMessage\" (\n" +
                                   "\t\"tox_group_peer_pubkey_syncer_01_sent_timestamp\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_tox_group_peer_pubkey_syncer_02_on_GroupMessage\" ON \"GroupMessage\" (\n" +
                                   "\t\"tox_group_peer_pubkey_syncer_02\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_tox_group_peer_pubkey_syncer_02_sent_timestamp_on_GroupMessage\" ON \"GroupMessage\" (\n" +
                                   "\t\"tox_group_peer_pubkey_syncer_02_sent_timestamp\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_tox_group_peer_pubkey_syncer_03_on_GroupMessage\" ON \"GroupMessage\" (\n" +
                                   "\t\"tox_group_peer_pubkey_syncer_03\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_tox_group_peer_pubkey_syncer_03_sent_timestamp_on_GroupMessage\" ON \"GroupMessage\" (\n" +
                                   "\t\"tox_group_peer_pubkey_syncer_03_sent_timestamp\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_tox_group_peer_role_on_GroupMessage\" ON \"GroupMessage\" (\n" +
                                   "\t\"tox_group_peer_role\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_tox_group_peername_on_GroupMessage\" ON \"GroupMessage\" (\n" +
                                   "\t\"tox_group_peername\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_tox_peername_on_ConferenceMessage\" ON \"ConferenceMessage\" (\n" +
                                   "\t\"tox_peername\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_tox_peerpubkey_on_ConferenceMessage\" ON \"ConferenceMessage\" (\n" +
                                   "\t\"tox_peerpubkey\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_tox_public_key_string_of_owner_on_RelayListDB\" ON \"RelayListDB\" (\n" +
                                   "\t\"tox_public_key_string_of_owner\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_tox_public_key_string_on_FileDB\" ON \"FileDB\" (\n" +
                                   "\t\"tox_public_key_string\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_tox_public_key_string_on_Filetransfer\" ON \"Filetransfer\" (\n" +
                                   "\t\"tox_public_key_string\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_udp_node_on_BootstrapNodeEntryDB\" ON \"BootstrapNodeEntryDB\" (\n" +
                                   "\t\"udp_node\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_value_on_TRIFADatabaseGlobals\" ON \"TRIFADatabaseGlobals\" (\n" +
                                   "\t\"value\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_value_on_TRIFADatabaseGlobalsNew\" ON \"TRIFADatabaseGlobalsNew\" (\n" +
                                   "\t\"value\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_was_synced_on_ConferenceMessage\" ON \"ConferenceMessage\" (\n" +
                                   "\t\"was_synced\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_was_synced_on_GroupMessage\" ON \"GroupMessage\" (\n" +
                                   "\t\"was_synced\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_who_invited__tox_public_key_string_on_ConferenceDB\" ON \"ConferenceDB\" (\n" +
                                   "\t\"who_invited__tox_public_key_string\"\n" + ");\n" +
                                   "CREATE INDEX IF NOT EXISTS \"index_who_invited__tox_public_key_string_on_GroupDB\" ON \"GroupDB\" (\n" +
                                   "\t\"who_invited__tox_public_key_string\"\n" + ");\n" + "COMMIT;\n";

        run_multi_sql(init_schema);
    }

    public OrmaDatabase getConnection()
    {
        return this;
    }

    public void execSQL(String s)
    {
        run_multi_sql(s);
    }

    public Cursor rawQuery(String s)
    {
        // TODO: !!!!!!!!write me!!!!!!!!
        Cursor c = null;
        return c;
    }

    private boolean check_db_open()
    {
        boolean ret2 = false;
        try
        {
            Statement statement = sqldb.createStatement();
            ResultSet rs = statement.executeQuery(
                    "SELECT count(*) as sqlite_master_count FROM sqlite_master");
            if (rs.next())
            {
                long ret3 = rs.getLong("sqlite_master_count");
                System.out.println(TAG + "sqlite_master_count: " + ret3);
                ret2 = true;
            }
            else
            {
                throw new RuntimeException();
            }

            try
            {
                statement.close();
            }
            catch (Exception ignored)
            {
                throw new RuntimeException();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println(TAG + "DBERR: database could not be opened!!");
            throw new RuntimeException(e);
        }
        return ret2;
    }

    private void open_db(final String db_file_path, final String password, boolean wal_mode)
    {
        try
        {
            System.out.println(TAG + "#########################");
            String class_sqlite = String.valueOf(Class.forName("org.sqlite.JDBC"));
            System.out.println(TAG + class_sqlite);
            sqldb = DriverManager.getConnection("jdbc:sqlite:" + db_file_path, null, password);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        if  (wal_mode)
        {
            // set WAL mode
            final String set_wal_mode = "PRAGMA journal_mode = WAL;";
            run_multi_sql(set_wal_mode);
        }
    }

    void close_db()
    {
        shutdown();
    }
}
