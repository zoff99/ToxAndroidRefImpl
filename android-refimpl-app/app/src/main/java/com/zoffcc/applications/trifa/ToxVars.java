/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 Zoff <zoff@zoff.cc>
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

package com.zoffcc.applications.trifa;


public class ToxVars
{
    // ------ global defines ------
    // ------ global defines ------
    // ------ global defines ------
    public static final int sizeof_uint16_t = 2; // 2 bytes
    public static final int sizeof_uint32_t = 4; // 4 bytes
    // TODO: get these with the appropriate JNI functions!
    public static final int TOX_MAX_NAME_LENGTH = 128;
    public static final int TOX_MAX_STATUS_MESSAGE_LENGTH = 1007;
    public static final int TOX_MAX_FRIEND_REQUEST_LENGTH = 1016;
    public static final int TOX_PUBLIC_KEY_SIZE = 32; // --> 64 hex chars!!
    public static final int TOX_SECRET_KEY_SIZE = 32;
    public static final int TOX_NOSPAM_SIZE = sizeof_uint32_t;
    public static final int TOX_ADDRESS_SIZE = TOX_PUBLIC_KEY_SIZE + TOX_NOSPAM_SIZE + sizeof_uint16_t;
    // public static final int TOX_MAX_MESSAGE_LENGTH = 1372; // -> tox_max_message_length [DONE]
    public static final int TOX_HASH_LENGTH = 32;
    public static final int TOX_FILE_ID_LENGTH = 32;
    public static final int TOX_MAX_FILENAME_LENGTH = 255;
    // TODO: get these with the appropriate JNI functions!
    // ------ global defines ------
    // ------ global defines ------
    // ------ global defines ------


    // --------- TOXAV ------------
    // --------- TOXAV ------------
    // --------- TOXAV ------------

    public static enum TOXAV_ERR_NEW
    {

        /**
         * The function returned successfully.
         */
        TOXAV_ERR_NEW_OK,

        /**
         * One of the arguments to the function was NULL when it was not expected.
         */
        TOXAV_ERR_NEW_NULL,

        /**
         * Memory allocation failure while trying to allocate structures required for
         * the A/V session.
         */
        TOXAV_ERR_NEW_MALLOC,

        /**
         * Attempted to create a second session for the same Tox instance.
         */
        TOXAV_ERR_NEW_MULTIPLE,

    }


    public static enum TOXAV_ERR_CALL
    {

        /**
         * The function returned successfully.
         */
        TOXAV_ERR_CALL_OK,

        /**
         * A resource allocation error occurred while trying to create the structures
         * required for the call.
         */
        TOXAV_ERR_CALL_MALLOC,

        /**
         * Synchronization error occurred.
         */
        TOXAV_ERR_CALL_SYNC,

        /**
         * The friend number did not designate a valid friend.
         */
        TOXAV_ERR_CALL_FRIEND_NOT_FOUND,

        /**
         * The friend was valid, but not currently connected.
         */
        TOXAV_ERR_CALL_FRIEND_NOT_CONNECTED,

        /**
         * Attempted to call a friend while already in an audio or video call with
         * them.
         */
        TOXAV_ERR_CALL_FRIEND_ALREADY_IN_CALL,

        /**
         * Audio or video bit rate is invalid.
         */
        TOXAV_ERR_CALL_INVALID_BIT_RATE,

    }

    public static enum TOXAV_ERR_ANSWER
    {

        /**
         * The function returned successfully.
         */
        TOXAV_ERR_ANSWER_OK,

        /**
         * Synchronization error occurred.
         */
        TOXAV_ERR_ANSWER_SYNC,

        /**
         * Failed to initialize codecs for call session. Note that codec initiation
         * will fail if there is no receive callback registered for either audio or
         * video.
         */
        TOXAV_ERR_ANSWER_CODEC_INITIALIZATION,

        /**
         * The friend number did not designate a valid friend.
         */
        TOXAV_ERR_ANSWER_FRIEND_NOT_FOUND,

        /**
         * The friend was valid, but they are not currently trying to initiate a call.
         * This is also returned if this client is already in a call with the friend.
         */
        TOXAV_ERR_ANSWER_FRIEND_NOT_CALLING,

        /**
         * Audio or video bit rate is invalid.
         */
        TOXAV_ERR_ANSWER_INVALID_BIT_RATE,

    }


    public static enum TOXAV_FRIEND_CALL_STATE
    {

        /**
         * The empty bit mask. None of the bits specified below are set.
         */
        TOXAV_FRIEND_CALL_STATE_NONE(0),

        /**
         * Set by the AV core if an error occurred on the remote end or if friend
         * timed out. This is the final state after which no more state
         * transitions can occur for the call. This call state will never be triggered
         * in combination with other call states.
         */
        TOXAV_FRIEND_CALL_STATE_ERROR(1),

        /**
         * The call has finished. This is the final state after which no more state
         * transitions can occur for the call. This call state will never be
         * triggered in combination with other call states.
         */
        TOXAV_FRIEND_CALL_STATE_FINISHED(2),

        /**
         * The flag that marks that friend is sending audio.
         */
        TOXAV_FRIEND_CALL_STATE_SENDING_A(4),

        /**
         * The flag that marks that friend is sending video.
         */
        TOXAV_FRIEND_CALL_STATE_SENDING_V(8),

        /**
         * The flag that marks that friend is receiving audio.
         */
        TOXAV_FRIEND_CALL_STATE_ACCEPTING_A(16),

        /**
         * The flag that marks that friend is receiving video.
         */
        TOXAV_FRIEND_CALL_STATE_ACCEPTING_V(32);

        public int value;

        private TOXAV_FRIEND_CALL_STATE(int value)
        {
            this.value = value;
        }
    }


    public static enum TOXAV_CALL_CONTROL
    {

        /**
         * Resume a previously paused call. Only valid if the pause was caused by this
         * client, if not, this control is ignored. Not valid before the call is accepted.
         */
        TOXAV_CALL_CONTROL_RESUME(0),

        /**
         * Put a call on hold. Not valid before the call is accepted.
         */
        TOXAV_CALL_CONTROL_PAUSE(1),

        /**
         * Reject a call if it was not answered, yet. Cancel a call after it was
         * answered.
         */
        TOXAV_CALL_CONTROL_CANCEL(2),

        /**
         * Request that the friend stops sending audio. Regardless of the friend's
         * compliance, this will cause the audio_receive_frame event to stop being
         * triggered on receiving an audio frame from the friend.
         */
        TOXAV_CALL_CONTROL_MUTE_AUDIO(3),

        /**
         * Calling this control will notify client to start sending audio again.
         */
        TOXAV_CALL_CONTROL_UNMUTE_AUDIO(4),

        /**
         * Request that the friend stops sending video. Regardless of the friend's
         * compliance, this will cause the video_receive_frame event to stop being
         * triggered on receiving a video frame from the friend.
         */
        TOXAV_CALL_CONTROL_HIDE_VIDEO(5),

        /**
         * Calling this control will notify client to start sending video again.
         */
        TOXAV_CALL_CONTROL_SHOW_VIDEO(6);
        public int value;

        private TOXAV_CALL_CONTROL(int value)
        {
            this.value = value;
        }

    }


    public static enum TOXAV_ERR_CALL_CONTROL
    {

        /**
         * The function returned successfully.
         */
        TOXAV_ERR_CALL_CONTROL_OK,

        /**
         * Synchronization error occurred.
         */
        TOXAV_ERR_CALL_CONTROL_SYNC,

        /**
         * The friend_number passed did not designate a valid friend.
         */
        TOXAV_ERR_CALL_CONTROL_FRIEND_NOT_FOUND,

        /**
         * This client is currently not in a call with the friend. Before the call is
         * answered, only CANCEL is a valid control.
         */
        TOXAV_ERR_CALL_CONTROL_FRIEND_NOT_IN_CALL,

        /**
         * Happens if user tried to pause an already paused call or if trying to
         * resume a call that is not paused.
         */
        TOXAV_ERR_CALL_CONTROL_INVALID_TRANSITION,

    }


    public static enum TOXAV_ERR_BIT_RATE_SET
    {

        /**
         * The function returned successfully.
         */
        TOXAV_ERR_BIT_RATE_SET_OK,

        /**
         * Synchronization error occurred.
         */
        TOXAV_ERR_BIT_RATE_SET_SYNC,

        /**
         * The audio bit rate passed was not one of the supported values.
         */
        TOXAV_ERR_BIT_RATE_SET_INVALID_AUDIO_BIT_RATE,

        /**
         * The video bit rate passed was not one of the supported values.
         */
        TOXAV_ERR_BIT_RATE_SET_INVALID_VIDEO_BIT_RATE,

        /**
         * The friend_number passed did not designate a valid friend.
         */
        TOXAV_ERR_BIT_RATE_SET_FRIEND_NOT_FOUND,

        /**
         * This client is currently not in a call with the friend.
         */
        TOXAV_ERR_BIT_RATE_SET_FRIEND_NOT_IN_CALL,

    }


    public static enum TOXAV_ERR_SEND_FRAME
    {

        /**
         * The function returned successfully.
         */
        TOXAV_ERR_SEND_FRAME_OK(0),

        /**
         * In case of video, one of Y, U, or V was NULL. In case of audio, the samples
         * data pointer was NULL.
         */
        TOXAV_ERR_SEND_FRAME_NULL(1),

        /**
         * The friend_number passed did not designate a valid friend.
         */
        TOXAV_ERR_SEND_FRAME_FRIEND_NOT_FOUND(2),

        /**
         * This client is currently not in a call with the friend.
         */
        TOXAV_ERR_SEND_FRAME_FRIEND_NOT_IN_CALL(3),

        /**
         * Synchronization error occurred.
         */
        TOXAV_ERR_SEND_FRAME_SYNC(4),

        /**
         * One of the frame parameters was invalid. E.g. the resolution may be too
         * small or too large, or the audio sampling rate may be unsupported.
         */
        TOXAV_ERR_SEND_FRAME_INVALID(5),

        /**
         * Either friend turned off audio or video receiving or we turned off sending
         * for the said payload.
         */
        TOXAV_ERR_SEND_FRAME_PAYLOAD_TYPE_DISABLED(6),

        /**
         * Failed to push frame through rtp interface.
         */
        TOXAV_ERR_SEND_FRAME_RTP_FAILED(7);

        public int value;

        private TOXAV_ERR_SEND_FRAME(int value)
        {
            this.value = value;
        }

        public static String value_str(int value)
        {
            if (value == TOXAV_ERR_SEND_FRAME.TOXAV_ERR_SEND_FRAME_OK.value)
            {
                return "TOXAV_ERR_SEND_FRAME_OK";
            }
            else if (value == TOXAV_ERR_SEND_FRAME.TOXAV_ERR_SEND_FRAME_NULL.value)
            {
                return "TOXAV_ERR_SEND_FRAME_NULL";
            }
            else if (value == TOXAV_ERR_SEND_FRAME.TOXAV_ERR_SEND_FRAME_FRIEND_NOT_FOUND.value)
            {
                return "TOXAV_ERR_SEND_FRAME_FRIEND_NOT_FOUND";
            }
            else if (value == TOXAV_ERR_SEND_FRAME.TOXAV_ERR_SEND_FRAME_FRIEND_NOT_IN_CALL.value)
            {
                return "TOXAV_ERR_SEND_FRAME_FRIEND_NOT_IN_CALL";
            }

            else if (value == TOXAV_ERR_SEND_FRAME.TOXAV_ERR_SEND_FRAME_SYNC.value)
            {
                return "TOXAV_ERR_SEND_FRAME_SYNC";
            }
            else if (value == TOXAV_ERR_SEND_FRAME.TOXAV_ERR_SEND_FRAME_INVALID.value)
            {
                return "TOXAV_ERR_SEND_FRAME_INVALID";
            }
            else if (value == TOXAV_ERR_SEND_FRAME.TOXAV_ERR_SEND_FRAME_PAYLOAD_TYPE_DISABLED.value)
            {
                return "TOXAV_ERR_SEND_FRAME_PAYLOAD_TYPE_DISABLED";
            }
            else if (value == TOXAV_ERR_SEND_FRAME.TOXAV_ERR_SEND_FRAME_RTP_FAILED.value)
            {
                return "TOXAV_ERR_SEND_FRAME_RTP_FAILED";
            }
            return "UNKNOWN";
        }

    }

    // --------- TOXAV ------------
    // --------- TOXAV ------------
    // --------- TOXAV ------------


    // ---------- TOX -------------
    // ---------- TOX -------------
    // ---------- TOX -------------

    public static enum TOX_USER_STATUS
    {

        /**
         * User is online and available.
         */
        TOX_USER_STATUS_NONE(0),

        /**
         * User is away. Clients can set this e.g. after a user defined
         * inactivity time.
         */
        TOX_USER_STATUS_AWAY(1),

        /**
         * User is busy. Signals to other clients that this client does not
         * currently wish to communicate.
         */
        TOX_USER_STATUS_BUSY(2);

        public int value;

        private TOX_USER_STATUS(int value)
        {
            this.value = value;
        }
    }

    public static enum TOX_MESSAGE_TYPE
    {

        /**
         * Normal text message. Similar to PRIVMSG on IRC.
         */
        TOX_MESSAGE_TYPE_NORMAL,

        /**
         * A message describing an user action. This is similar to /me (CTCP ACTION)
         * on IRC.
         */
        TOX_MESSAGE_TYPE_ACTION,

    }


    public static enum TOX_PROXY_TYPE
    {

        /**
         * Don't use a proxy.
         */
        TOX_PROXY_TYPE_NONE,

        /**
         * HTTP proxy using CONNECT.
         */
        TOX_PROXY_TYPE_HTTP,

        /**
         * SOCKS proxy for simple socket pipes.
         */
        TOX_PROXY_TYPE_SOCKS5,

    }


    public static enum TOX_SAVEDATA_TYPE
    {

        /**
         * No savedata.
         */
        TOX_SAVEDATA_TYPE_NONE,

        /**
         * Savedata is one that was obtained from tox_get_savedata.
         */
        TOX_SAVEDATA_TYPE_TOX_SAVE,

        /**
         * Savedata is a secret key of length TOX_SECRET_KEY_SIZE.
         */
        TOX_SAVEDATA_TYPE_SECRET_KEY,

    }


    public static enum TOX_LOG_LEVEL
    {

        /**
         * Very detailed traces including all network activity.
         */
        TOX_LOG_LEVEL_TRACE(0),

        /**
         * Debug messages such as which port we bind to.
         */
        TOX_LOG_LEVEL_DEBUG(1),

        /**
         * Informational log messages such as video call status changes.
         */
        TOX_LOG_LEVEL_INFO(2),

        /**
         * Warnings about internal inconsistency or logic errors.
         */
        TOX_LOG_LEVEL_WARNING(3),

        /**
         * Severe unexpected errors caused by external or internal inconsistency.
         */
        TOX_LOG_LEVEL_ERROR(4);

        public int value;

        private TOX_LOG_LEVEL(int value)
        {
            this.value = value;
        }

        public static String value_str(int value)
        {
            if (value == TOX_LOG_LEVEL.TOX_LOG_LEVEL_TRACE.value)
            {
                return "TOX_LOG_LEVEL_TRACE";
            }
            else if (value == TOX_LOG_LEVEL.TOX_LOG_LEVEL_DEBUG.value)
            {
                return "TOX_LOG_LEVEL_DEBUG";
            }
            else if (value == TOX_LOG_LEVEL.TOX_LOG_LEVEL_INFO.value)
            {
                return "TOX_LOG_LEVEL_INFO";
            }
            else if (value == TOX_LOG_LEVEL.TOX_LOG_LEVEL_WARNING.value)
            {
                return "TOX_LOG_LEVEL_WARNING";
            }
            else if (value == TOX_LOG_LEVEL.TOX_LOG_LEVEL_ERROR.value)
            {
                return "TOX_LOG_LEVEL_ERROR";
            }
            return "UNKNOWN";
        }

    }


    public static enum TOX_ERR_OPTIONS_NEW
    {

        /**
         * The function returned successfully.
         */
        TOX_ERR_OPTIONS_NEW_OK,

        /**
         * The function failed to allocate enough memory for the options struct.
         */
        TOX_ERR_OPTIONS_NEW_MALLOC,

    }


    public static enum TOX_ERR_NEW
    {

        /**
         * The function returned successfully.
         */
        TOX_ERR_NEW_OK,

        /**
         * One of the arguments to the function was NULL when it was not expected.
         */
        TOX_ERR_NEW_NULL,

        /**
         * The function was unable to allocate enough memory to store the internal
         * structures for the Tox object.
         */
        TOX_ERR_NEW_MALLOC,

        /**
         * The function was unable to bind to a port. This may mean that all ports
         * have already been bound, e.g. by other Tox instances, or it may mean
         * a permission error. You may be able to gather more information from errno.
         */
        TOX_ERR_NEW_PORT_ALLOC,

        /**
         * proxy_type was invalid.
         */
        TOX_ERR_NEW_PROXY_BAD_TYPE,

        /**
         * proxy_type was valid but the proxy_host passed had an invalid format
         * or was NULL.
         */
        TOX_ERR_NEW_PROXY_BAD_HOST,

        /**
         * proxy_type was valid, but the proxy_port was invalid.
         */
        TOX_ERR_NEW_PROXY_BAD_PORT,

        /**
         * The proxy address passed could not be resolved.
         */
        TOX_ERR_NEW_PROXY_NOT_FOUND,

        /**
         * The byte array to be loaded contained an encrypted save.
         */
        TOX_ERR_NEW_LOAD_ENCRYPTED,

        /**
         * The data format was invalid. This can happen when loading data that was
         * saved by an older version of Tox, or when the data has been corrupted.
         * When loading from badly formatted data, some data may have been loaded,
         * and the rest is discarded. Passing an invalid length parameter also
         * causes this error.
         */
        TOX_ERR_NEW_LOAD_BAD_FORMAT,

    }

    public static enum TOX_ERR_BOOTSTRAP
    {

        /**
         * The function returned successfully.
         */
        TOX_ERR_BOOTSTRAP_OK,

        /**
         * One of the arguments to the function was NULL when it was not expected.
         */
        TOX_ERR_BOOTSTRAP_NULL,

        /**
         * The address could not be resolved to an IP address, or the IP address
         * passed was invalid.
         */
        TOX_ERR_BOOTSTRAP_BAD_HOST,

        /**
         * The port passed was invalid. The valid port range is (1, 65535).
         */
        TOX_ERR_BOOTSTRAP_BAD_PORT,

    }


    public static enum TOX_CONNECTION
    {

        /**
         * There is no connection. This instance, or the friend the state change is
         * about, is now offline.
         */
        TOX_CONNECTION_NONE(0),

        /**
         * A TCP connection has been established. For the own instance, this means it
         * is connected through a TCP relay, only. For a friend, this means that the
         * connection to that particular friend goes through a TCP relay.
         */
        TOX_CONNECTION_TCP(1),

        /**
         * A UDP connection has been established. For the own instance, this means it
         * is able to send UDP packets to DHT nodes, but may still be connected to
         * a TCP relay. For a friend, this means that the connection to that
         * particular friend was built using direct UDP packets.
         */
        TOX_CONNECTION_UDP(2);

        public int value;

        private TOX_CONNECTION(int value)
        {
            this.value = value;
        }


    }

    public static enum TOX_ERR_SET_INFO
    {

        /**
         * The function returned successfully.
         */
        TOX_ERR_SET_INFO_OK,

        /**
         * One of the arguments to the function was NULL when it was not expected.
         */
        TOX_ERR_SET_INFO_NULL,

        /**
         * Information length exceeded maximum permissible size.
         */
        TOX_ERR_SET_INFO_TOO_LONG,

    }


    public static enum TOX_ERR_FRIEND_ADD
    {

        /**
         * The function returned successfully.
         */
        TOX_ERR_FRIEND_ADD_OK,

        /**
         * One of the arguments to the function was NULL when it was not expected.
         */
        TOX_ERR_FRIEND_ADD_NULL,

        /**
         * The length of the friend request message exceeded
         * TOX_MAX_FRIEND_REQUEST_LENGTH.
         */
        TOX_ERR_FRIEND_ADD_TOO_LONG,

        /**
         * The friend request message was empty. This, and the TOO_LONG code will
         * never be returned from tox_friend_add_norequest.
         */
        TOX_ERR_FRIEND_ADD_NO_MESSAGE,

        /**
         * The friend address belongs to the sending client.
         */
        TOX_ERR_FRIEND_ADD_OWN_KEY,

        /**
         * A friend request has already been sent, or the address belongs to a friend
         * that is already on the friend list.
         */
        TOX_ERR_FRIEND_ADD_ALREADY_SENT,

        /**
         * The friend address checksum failed.
         */
        TOX_ERR_FRIEND_ADD_BAD_CHECKSUM,

        /**
         * The friend was already there, but the nospam value was different.
         */
        TOX_ERR_FRIEND_ADD_SET_NEW_NOSPAM,

        /**
         * A memory allocation failed when trying to increase the friend list size.
         */
        TOX_ERR_FRIEND_ADD_MALLOC,

    }


    public static enum TOX_ERR_FRIEND_SEND_MESSAGE
    {

        /**
         * The function returned successfully.
         */
        TOX_ERR_FRIEND_SEND_MESSAGE_OK,

        /**
         * One of the arguments to the function was NULL when it was not expected.
         */
        TOX_ERR_FRIEND_SEND_MESSAGE_NULL,

        /**
         * The friend number did not designate a valid friend.
         */
        TOX_ERR_FRIEND_SEND_MESSAGE_FRIEND_NOT_FOUND,

        /**
         * This client is currently not connected to the friend.
         */
        TOX_ERR_FRIEND_SEND_MESSAGE_FRIEND_NOT_CONNECTED,

        /**
         * An allocation error occurred while increasing the send queue size.
         */
        TOX_ERR_FRIEND_SEND_MESSAGE_SENDQ,

        /**
         * Message length exceeded TOX_MAX_MESSAGE_LENGTH.
         */
        TOX_ERR_FRIEND_SEND_MESSAGE_TOO_LONG,

        /**
         * Attempted to send a zero-length message.
         */
        TOX_ERR_FRIEND_SEND_MESSAGE_EMPTY,

    }


    public static enum TOX_FILE_KIND
    {

        /**
         * Arbitrary file data. Clients can choose to handle it based on the file name
         * or magic or any other way they choose.
         */
        TOX_FILE_KIND_DATA(0),

        /**
         * Avatar file_id. This consists of tox_hash(image).
         * Avatar data. This consists of the image data.
         * <p>
         * Avatars can be sent at any time the client wishes. Generally, a client will
         * send the avatar to a friend when that friend comes online, and to all
         * friends when the avatar changed. A client can save some traffic by
         * remembering which friend received the updated avatar already and only send
         * it if the friend has an out of date avatar.
         * <p>
         * Clients who receive avatar send requests can reject it (by sending
         * TOX_FILE_CONTROL_CANCEL before any other controls), or accept it (by
         * sending TOX_FILE_CONTROL_RESUME). The file_id of length TOX_HASH_LENGTH bytes
         * (same length as TOX_FILE_ID_LENGTH) will contain the hash. A client can compare
         * this hash with a saved hash and send TOX_FILE_CONTROL_CANCEL to terminate the avatar
         * transfer if it matches.
         * <p>
         * When file_size is set to 0 in the transfer request it means that the client
         * has no avatar.
         */
        TOX_FILE_KIND_AVATAR(1);

        public int value;

        private TOX_FILE_KIND(int value)
        {
            this.value = value;
        }


    }

    public static enum TOX_FILE_CONTROL
    {

        /**
         * Sent by the receiving side to accept a file send request. Also sent after a
         * TOX_FILE_CONTROL_PAUSE command to continue sending or receiving.
         */
        TOX_FILE_CONTROL_RESUME(0),

        /**
         * Sent by clients to pause the file transfer. The initial state of a file
         * transfer is always paused on the receiving side and running on the sending
         * side. If both the sending and receiving side pause the transfer, then both
         * need to send TOX_FILE_CONTROL_RESUME for the transfer to resume.
         */
        TOX_FILE_CONTROL_PAUSE(1),

        /**
         * Sent by the receiving side to reject a file send request before any other
         * commands are sent. Also sent by either side to terminate a file transfer.
         */
        TOX_FILE_CONTROL_CANCEL(2);

        public int value;

        private TOX_FILE_CONTROL(int value)
        {
            this.value = value;
        }
    }


    public static enum TOX_ERR_FILE_CONTROL
    {

        /**
         * The function returned successfully.
         */
        TOX_ERR_FILE_CONTROL_OK,

        /**
         * The friend_number passed did not designate a valid friend.
         */
        TOX_ERR_FILE_CONTROL_FRIEND_NOT_FOUND,

        /**
         * This client is currently not connected to the friend.
         */
        TOX_ERR_FILE_CONTROL_FRIEND_NOT_CONNECTED,

        /**
         * No file transfer with the given file number was found for the given friend.
         */
        TOX_ERR_FILE_CONTROL_NOT_FOUND,

        /**
         * A RESUME control was sent, but the file transfer is running normally.
         */
        TOX_ERR_FILE_CONTROL_NOT_PAUSED,

        /**
         * A RESUME control was sent, but the file transfer was paused by the other
         * party. Only the party that paused the transfer can resume it.
         */
        TOX_ERR_FILE_CONTROL_DENIED,

        /**
         * A PAUSE control was sent, but the file transfer was already paused.
         */
        TOX_ERR_FILE_CONTROL_ALREADY_PAUSED,

        /**
         * Packet queue is full.
         */
        TOX_ERR_FILE_CONTROL_SENDQ,

    }


    public static enum TOX_ERR_FILE_SEEK
    {

        /**
         * The function returned successfully.
         */
        TOX_ERR_FILE_SEEK_OK,

        /**
         * The friend_number passed did not designate a valid friend.
         */
        TOX_ERR_FILE_SEEK_FRIEND_NOT_FOUND,

        /**
         * This client is currently not connected to the friend.
         */
        TOX_ERR_FILE_SEEK_FRIEND_NOT_CONNECTED,

        /**
         * No file transfer with the given file number was found for the given friend.
         */
        TOX_ERR_FILE_SEEK_NOT_FOUND,

        /**
         * File was not in a state where it could be seeked.
         */
        TOX_ERR_FILE_SEEK_DENIED,

        /**
         * Seek position was invalid
         */
        TOX_ERR_FILE_SEEK_INVALID_POSITION,

        /**
         * Packet queue is full.
         */
        TOX_ERR_FILE_SEEK_SENDQ,

    }


    public static enum TOX_ERR_FILE_GET
    {

        /**
         * The function returned successfully.
         */
        TOX_ERR_FILE_GET_OK,

        /**
         * One of the arguments to the function was NULL when it was not expected.
         */
        TOX_ERR_FILE_GET_NULL,

        /**
         * The friend_number passed did not designate a valid friend.
         */
        TOX_ERR_FILE_GET_FRIEND_NOT_FOUND,

        /**
         * No file transfer with the given file number was found for the given friend.
         */
        TOX_ERR_FILE_GET_NOT_FOUND,

    }


    public static enum TOX_ERR_FILE_SEND
    {

        /**
         * The function returned successfully.
         */
        TOX_ERR_FILE_SEND_OK,

        /**
         * One of the arguments to the function was NULL when it was not expected.
         */
        TOX_ERR_FILE_SEND_NULL,

        /**
         * The friend_number passed did not designate a valid friend.
         */
        TOX_ERR_FILE_SEND_FRIEND_NOT_FOUND,

        /**
         * This client is currently not connected to the friend.
         */
        TOX_ERR_FILE_SEND_FRIEND_NOT_CONNECTED,

        /**
         * Filename length exceeded TOX_MAX_FILENAME_LENGTH bytes.
         */
        TOX_ERR_FILE_SEND_NAME_TOO_LONG,

        /**
         * Too many ongoing transfers. The maximum number of concurrent file transfers
         * is 256 per friend per direction (sending and receiving).
         */
        TOX_ERR_FILE_SEND_TOO_MANY,

    }


    public static enum TOX_ERR_FILE_SEND_CHUNK
    {

        /**
         * The function returned successfully.
         */
        TOX_ERR_FILE_SEND_CHUNK_OK,

        /**
         * The length parameter was non-zero, but data was NULL.
         */
        TOX_ERR_FILE_SEND_CHUNK_NULL,

        /**
         * The friend_number passed did not designate a valid friend.
         */
        TOX_ERR_FILE_SEND_CHUNK_FRIEND_NOT_FOUND,

        /**
         * This client is currently not connected to the friend.
         */
        TOX_ERR_FILE_SEND_CHUNK_FRIEND_NOT_CONNECTED,

        /**
         * No file transfer with the given file number was found for the given friend.
         */
        TOX_ERR_FILE_SEND_CHUNK_NOT_FOUND,

        /**
         * File transfer was found but isn't in a transferring state: (paused, done,
         * broken, etc...) (happens only when not called from the request chunk callback).
         */
        TOX_ERR_FILE_SEND_CHUNK_NOT_TRANSFERRING,

        /**
         * Attempted to send more or less data than requested. The requested data size is
         * adjusted according to maximum transmission unit and the expected end of
         * the file. Trying to send less or more than requested will return this error.
         */
        TOX_ERR_FILE_SEND_CHUNK_INVALID_LENGTH,

        /**
         * Packet queue is full.
         */
        TOX_ERR_FILE_SEND_CHUNK_SENDQ,

        /**
         * Position parameter was wrong.
         */
        TOX_ERR_FILE_SEND_CHUNK_WRONG_POSITION,

    }


    public static enum TOX_CONFERENCE_TYPE
    {
        /**
         * Text-only conferences that must be accepted with the tox_conference_join function.
         */
        TOX_CONFERENCE_TYPE_TEXT(0),

        /**
         * Video conference. The function to accept these is in toxav.
         */
        TOX_CONFERENCE_TYPE_AV(1);

        public int value;

        private TOX_CONFERENCE_TYPE(int value)
        {
            this.value = value;
        }

    }

    public static enum TOX_CONFERENCE_STATE_CHANGE
    {

        /**
         * A peer has joined the conference.
         */
        TOX_CONFERENCE_STATE_CHANGE_PEER_JOIN(0),

        /**
         * A peer has exited the conference.
         */
        TOX_CONFERENCE_STATE_CHANGE_PEER_EXIT(1),

        /**
         * A peer has changed their name.
         */
        TOX_CONFERENCE_STATE_CHANGE_PEER_NAME_CHANGE(2);

        public int value;

        private TOX_CONFERENCE_STATE_CHANGE(int value)
        {
            this.value = value;
        }
    }

    public static enum TOX_ERR_CONFERENCE_PEER_QUERY
    {

        /**
         * The function returned successfully.
         */
        TOX_ERR_CONFERENCE_PEER_QUERY_OK,

        /**
         * The conference number passed did not designate a valid conference.
         */
        TOX_ERR_CONFERENCE_PEER_QUERY_CONFERENCE_NOT_FOUND,

        /**
         * The peer number passed did not designate a valid peer.
         */
        TOX_ERR_CONFERENCE_PEER_QUERY_PEER_NOT_FOUND,

        /**
         * The client is not connected to the conference.
         */
        TOX_ERR_CONFERENCE_PEER_QUERY_NO_CONNECTION,

    }

    public static enum TOX_ERR_CONFERENCE_INVITE
    {

        /**
         * The function returned successfully.
         */
        TOX_ERR_CONFERENCE_INVITE_OK,

        /**
         * The conference number passed did not designate a valid conference.
         */
        TOX_ERR_CONFERENCE_INVITE_CONFERENCE_NOT_FOUND,

        /**
         * The invite packet failed to send.
         */
        TOX_ERR_CONFERENCE_INVITE_FAIL_SEND,

    }

    public static enum TOX_ERR_CONFERENCE_JOIN
    {

        /**
         * The function returned successfully.
         */
        TOX_ERR_CONFERENCE_JOIN_OK,

        /**
         * The cookie passed has an invalid length.
         */
        TOX_ERR_CONFERENCE_JOIN_INVALID_LENGTH,

        /**
         * The conference is not the expected type. This indicates an invalid cookie.
         */
        TOX_ERR_CONFERENCE_JOIN_WRONG_TYPE,

        /**
         * The friend number passed does not designate a valid friend.
         */
        TOX_ERR_CONFERENCE_JOIN_FRIEND_NOT_FOUND,

        /**
         * Client is already in this conference.
         */
        TOX_ERR_CONFERENCE_JOIN_DUPLICATE,

        /**
         * Conference instance failed to initialize.
         */
        TOX_ERR_CONFERENCE_JOIN_INIT_FAIL,

        /**
         * The join packet failed to send.
         */
        TOX_ERR_CONFERENCE_JOIN_FAIL_SEND,

    }

    public static enum TOX_ERR_CONFERENCE_SEND_MESSAGE
    {

        /**
         * The function returned successfully.
         */
        TOX_ERR_CONFERENCE_SEND_MESSAGE_OK,

        /**
         * The conference number passed did not designate a valid conference.
         */
        TOX_ERR_CONFERENCE_SEND_MESSAGE_CONFERENCE_NOT_FOUND,

        /**
         * The message is too long.
         */
        TOX_ERR_CONFERENCE_SEND_MESSAGE_TOO_LONG,

        /**
         * The client is not connected to the conference.
         */
        TOX_ERR_CONFERENCE_SEND_MESSAGE_NO_CONNECTION,

        /**
         * The message packet failed to send.
         */
        TOX_ERR_CONFERENCE_SEND_MESSAGE_FAIL_SEND,

    }


    public static enum TOX_ERR_CONFERENCE_TITLE
    {

        /**
         * The function returned successfully.
         */
        TOX_ERR_CONFERENCE_TITLE_OK,

        /**
         * The conference number passed did not designate a valid conference.
         */
        TOX_ERR_CONFERENCE_TITLE_CONFERENCE_NOT_FOUND,

        /**
         * The title is too long or empty.
         */
        TOX_ERR_CONFERENCE_TITLE_INVALID_LENGTH,

        /**
         * The title packet failed to send.
         */
        TOX_ERR_CONFERENCE_TITLE_FAIL_SEND,

    }


    public static enum TOX_ERR_CONFERENCE_GET_TYPE
    {

        /**
         * The function returned successfully.
         */
        TOX_ERR_CONFERENCE_GET_TYPE_OK,

        /**
         * The conference number passed did not designate a valid conference.
         */
        TOX_ERR_CONFERENCE_GET_TYPE_CONFERENCE_NOT_FOUND,

    }


    public static enum TOXAV_OPTIONS_VP8_QUALITY_VALUE
    {
        TOXAV_ENCODER_VP8_QUALITY_NORMAL(0), TOXAV_ENCODER_VP8_QUALITY_HIGH(1);
        public int value;

        private TOXAV_OPTIONS_VP8_QUALITY_VALUE(int value)
        {
            this.value = value;
        }
    }


    public static enum TOXAV_ENCODER_CODEC_USED_VALUE
    {
        TOXAV_ENCODER_CODEC_USED_VP8(0), TOXAV_ENCODER_CODEC_USED_VP9(1);
        public int value;

        private TOXAV_ENCODER_CODEC_USED_VALUE(int value)
        {
            this.value = value;
        }
    }

    public static enum TOXAV_ENCODER_KF_METHOD_VALUE
    {
        TOXAV_ENCODER_KF_METHOD_NORMAL(0), TOXAV_ENCODER_KF_METHOD_PATTERN(1);
        public int value;

        private TOXAV_ENCODER_KF_METHOD_VALUE(int value)
        {
            this.value = value;
        }
    }


    /**
     * Maximum size of MessageV2 Messagetext
     */
    public static int TOX_MESSAGEV2_MAX_TEXT_LENGTH = 4096;

    /**
     * Maximum size of MessageV2 Messagetext
     */
    public static int TOX_MESSAGEV2_MAX_HEADER_SIZE = (32 + 4 + 2 + 1);

    /**
     * Maximum size of MessageV2 Filetransfers (overall size including any overhead)
     */
    public static int TOX_MAX_FILETRANSFER_SIZE_MSGV2 = (TOX_MESSAGEV2_MAX_TEXT_LENGTH + TOX_MESSAGEV2_MAX_HEADER_SIZE);


    public static enum TOXAV_OPTIONS_OPTION
    {
        TOXAV_ENCODER_CPU_USED(0), TOXAV_ENCODER_VP8_QUALITY(1), TOXAV_ENCODER_MIN_SOFTDEADLINE(
            2), TOXAV_ENCODER_MAX_SOFTDEADLINE(3), TOXAV_DECODER_MIN_SOFTDEADLINE(4), TOXAV_DECODER_MAX_SOFTDEADLINE(
            5), TOXAV_ENCODER_RC_MAX_QUANTIZER(6), TOXAV_ENCODER_RC_MIN_QUANTIZER(8), TOXAV_DECODER_ERROR_CONCEALMENT(
            7), TOXAV_ENCODER_CODEC_USED(9), TOXAV_ENCODER_KF_METHOD(10), TOXAV_ENCODER_VIDEO_BITRATE_AUTOSET(
            11), TOXAV_ENCODER_VIDEO_MAX_BITRATE(12), TOXAV_DECODER_VIDEO_BUFFER_MS(
            13), TOXAV_CLIENT_VIDEO_CAPTURE_DELAY_MS(14);
        public int value;

        private TOXAV_OPTIONS_OPTION(int value)
        {
            this.value = value;
        }
    }

    public static enum TOXAV_CALL_COMM_INFO
    {
        TOXAV_CALL_COMM_DECODER_IN_USE_VP8(0), TOXAV_CALL_COMM_DECODER_IN_USE_H264(
            1), TOXAV_CALL_COMM_ENCODER_IN_USE_VP8(2), TOXAV_CALL_COMM_ENCODER_IN_USE_H264(
            3), TOXAV_CALL_COMM_ENCODER_IN_USE_H264_OMX_PI(6), TOXAV_CALL_COMM_DECODER_CURRENT_BITRATE(
            4), TOXAV_CALL_COMM_ENCODER_CURRENT_BITRATE(5), TOXAV_CALL_COMM_NETWORK_ROUND_TRIP_MS(
            7), TOXAV_CALL_COMM_PLAY_DELAY(8), TOXAV_CALL_COMM_PLAY_BUFFER_ENTRIES(9), TOXAV_CALL_COMM_INCOMING_FPS(
            10), TOXAV_CALL_COMM_REMOTE_RECORD_DELAY(11);;

        public int value;

        private TOXAV_CALL_COMM_INFO(int value)
        {
            this.value = value;
        }
    }
    // ---------- TOX -------------
    // ---------- TOX -------------
    // ---------- TOX -------------
}
