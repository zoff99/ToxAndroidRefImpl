/**
 * [TRIfA], JNI part of Tox Reference Implementation for Android
 * Copyright (C) 2017 Zoff <zoff@zoff.cc>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */


#include <ctype.h>
#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <time.h>
#include <dirent.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <sys/ioctl.h>
#include <unistd.h>

#include <fcntl.h>
#include <errno.h>

#include <tox/tox.h>
#include <tox/toxav.h>

#include <sodium/utils.h>

#include <pthread.h>

#include <linux/videodev2.h>
#include <vpx/vpx_image.h>
#include <sys/mman.h>

// ------- Android/JNI stuff -------
// #include <android/log.h>
#include <jni.h>
#include "coffeecatch.h"
#include "coffeejni.h"
// ------- Android/JNI stuff -------


// ----------- version -----------
// ----------- version -----------
#define VERSION_MAJOR 0
#define VERSION_MINOR 99
#define VERSION_PATCH 10
static const char global_version_string[] = "0.99.10";
// ----------- version -----------
// ----------- version -----------



#define CLEAR(x) memset(&(x), 0, sizeof(x))
#define c_sleep(x) usleep(1000*x)

#define CURRENT_LOG_LEVEL 9 // 0 -> error, 1 -> warn, 2 -> info, 9 -> debug
#define MAX_LOG_LINE_LENGTH 1000
#define MAX_FULL_PATH_LENGTH 1000

#define DEFAULT_FPS_SLEEP_MS 160 // default video fps (sleep in msecs. !!)

typedef struct {
    bool incoming;
    uint32_t state;
	uint32_t audio_bit_rate;
	uint32_t video_bit_rate;
    // pthread_mutex_t arb_mutex[1];
} CallControl;


const char *savedata_filename = "savedata.tox";
const char *savedata_tmp_filename = "savedata.tox.tmp";
int tox_loop_running = 1;
int toxav_video_thread_stop = 0;
int toxav_iterate_thread_stop = 0;

TOX_CONNECTION my_connection_status = TOX_CONNECTION_NONE;
Tox *tox_global = NULL;
ToxAV *tox_av_global = NULL;
CallControl mytox_CC;
pthread_t tid[2]; // 0 -> toxav_iterate thread, 1 -> video send thread


// ----- JNI stuff -----
JNIEnv *jnienv;
JavaVM *cachedJVM = NULL;
jobject *android_activity;

char *app_data_dir = NULL;
jclass MainActivity = NULL;
jclass TrifaToxService_class = NULL;
jmethodID logger_method = NULL;
jmethodID safe_string_method = NULL;

uint8_t *video_buffer_1 = NULL;
uint8_t *video_buffer_1_u = NULL;
uint8_t *video_buffer_1_v = NULL;
long video_buffer_1_size = 0;
int video_buffer_1_width = 0;
int video_buffer_1_height = 0;
int video_buffer_1_y_size = 0;
int video_buffer_1_u_size = 0;
int video_buffer_1_v_size = 0;

uint8_t *video_buffer_2 = NULL;
uint8_t *video_buffer_2_u = NULL;
uint8_t *video_buffer_2_v = NULL;
long video_buffer_2_size = 0;
int video_buffer_2_y_size = 0;
int video_buffer_2_u_size = 0;
int video_buffer_2_v_size = 0;

uint8_t *audio_buffer_pcm_1 = NULL;
long audio_buffer_pcm_1_size = 0;

uint8_t *audio_buffer_pcm_2 = NULL;
long audio_buffer_pcm_2_size = 0;

// -------- _callbacks_ --------
jmethodID android_tox_callback_self_connection_status_cb_method = NULL;
jmethodID android_tox_callback_friend_name_cb_method = NULL;
jmethodID android_tox_callback_friend_status_message_cb_method = NULL;
jmethodID android_tox_callback_friend_status_cb_method = NULL;
jmethodID android_tox_callback_friend_connection_status_cb_method = NULL;
jmethodID android_tox_callback_friend_typing_cb_method = NULL;
jmethodID android_tox_callback_friend_read_receipt_cb_method = NULL;
jmethodID android_tox_callback_friend_request_cb_method = NULL;
jmethodID android_tox_callback_friend_message_cb_method = NULL;
jmethodID android_tox_callback_file_recv_control_cb_method = NULL;
jmethodID android_tox_callback_file_chunk_request_cb_method = NULL;
jmethodID android_tox_callback_file_recv_cb_method = NULL;
jmethodID android_tox_callback_file_recv_chunk_cb_method = NULL;
jmethodID android_tox_log_cb_method = NULL;
// -------- _AV-callbacks_ -----
jmethodID android_toxav_callback_call_cb_method = NULL;
jmethodID android_toxav_callback_video_receive_frame_cb_method = NULL;
jmethodID android_toxav_callback_call_state_cb_method = NULL;
jmethodID android_toxav_callback_bit_rate_status_cb_method = NULL;
jmethodID android_toxav_callback_audio_receive_frame_cb_method = NULL;
// -------- _AV-callbacks_ -----
// -------- _callbacks_ --------

// ----- JNI stuff -----



typedef struct DHT_node {
    const char *ip;
    uint16_t port;
    const char key_hex[TOX_PUBLIC_KEY_SIZE*2 + 1];
    unsigned char key_bin[TOX_PUBLIC_KEY_SIZE];
} DHT_node;




// functions -----------
// functions -----------
// functions -----------
void self_connection_status_cb(Tox *tox, TOX_CONNECTION connection_status, void *user_data);
void friend_name_cb(Tox *tox, uint32_t friend_number, const uint8_t *name, size_t length, void *user_data);
void friend_status_message_cb(Tox *tox, uint32_t friend_number, const uint8_t *message, size_t length, void *user_data);
void friend_status_cb(Tox *tox, uint32_t friend_number, TOX_USER_STATUS status, void *user_data);
void friend_connection_status_cb(Tox *tox, uint32_t friend_number, TOX_CONNECTION connection_status, void *user_data);
void friend_typing_cb(Tox *tox, uint32_t friend_number, bool is_typing, void *user_data);
void friend_read_receipt_cb(Tox *tox, uint32_t friend_number, uint32_t message_id, void *user_data);
void friend_request_cb(Tox *tox, const uint8_t *public_key, const uint8_t *message, size_t length, void *user_data);
void friend_message_cb(Tox *tox, uint32_t friend_number, TOX_MESSAGE_TYPE type, const uint8_t *message, size_t length, void *user_data);

void file_recv_control_cb(Tox *tox, uint32_t friend_number, uint32_t file_number, TOX_FILE_CONTROL control, void *user_data);
void file_chunk_request_cb(Tox *tox, uint32_t friend_number, uint32_t file_number, uint64_t position, size_t length, void *user_data);
void file_recv_cb(Tox *tox, uint32_t friend_number, uint32_t file_number, uint32_t kind, uint64_t file_size, const uint8_t *filename, size_t filename_length, void *user_data);
void file_recv_chunk_cb(Tox *tox, uint32_t friend_number, uint32_t file_number, uint64_t position, const uint8_t *data, size_t length, void *user_data);

void tox_log_cb__custom(Tox *tox, TOX_LOG_LEVEL level, const char *file, uint32_t line, const char *func, const char *message, void *user_data);

void android_logger(int level, const char* logtext);
jstring c_safe_string_from_java(char *instr, size_t len);
// functions -----------
// functions -----------
// functions -----------





void dbg(int level, const char *fmt, ...)
{
	char *level_and_format = NULL;
	char *fmt_copy = NULL;
	char *log_line_str = NULL;

	if (fmt == NULL)
	{
		return;
	}

	if (strlen(fmt) < 1)
	{
		return;
	}

	if ((level < 0) || (level > 9))
	{
		level = 0;
	}

	level_and_format = malloc(strlen(fmt) + 3);

	if (!level_and_format)
	{
		return;
	}

	fmt_copy = level_and_format + 2;
	strcpy(fmt_copy, fmt);
	level_and_format[1] = ':';
	if (level == 0)
	{
		level_and_format[0] = 'E';
	}
	else if (level == 1)
	{
		level_and_format[0] = 'W';
	}
	else if (level == 2)
	{
		level_and_format[0] = 'I';
	}
	else
	{
		level_and_format[0] = 'D';
	}

	if (level <= CURRENT_LOG_LEVEL)
	{
		log_line_str = malloc((size_t)MAX_LOG_LINE_LENGTH);
		// memset(log_line_str, 0, (size_t)MAX_LOG_LINE_LENGTH);
		va_list ap;
		va_start(ap, fmt);
		vsnprintf(log_line_str, (size_t)MAX_LOG_LINE_LENGTH, level_and_format, ap);
		// send "log_line_str" to android
		android_logger(level, log_line_str);
		va_end(ap);
		free(log_line_str);
	}

	if (level_and_format)
	{
		free(level_and_format);
	}
}


Tox *create_tox(int udp_enabled)
{
	Tox *tox = NULL;
	TOX_ERR_NEW error;
	struct Tox_Options options;
	CLEAR(options);

	dbg(9, "1006");
	tox_options_default(&options);

	uint16_t tcp_port = 33776;

	options.ipv6_enabled = true;

	if (udp_enabled == 1)
	{
		options.udp_enabled = true;
	}
	else
	{
		options.udp_enabled = false; // set TCP as default mode for android !!
	}
	options.local_discovery_enabled = true;
	options.hole_punching_enabled = true;
	// options.tcp_port = tcp_port;
    options.tcp_port = 0; // TCP relay is disabled !!
	// ------------------------------------------------------------
	// set our own handler for c-toxcore logging messages!!
	options.log_callback = tox_log_cb__custom;
	// ------------------------------------------------------------

	dbg(9, "1007");
	char *full_path_filename = malloc(MAX_FULL_PATH_LENGTH);
	dbg(9, "1008");
	snprintf(full_path_filename, (size_t)MAX_FULL_PATH_LENGTH, "%s/%s", app_data_dir, savedata_filename);
	dbg(9, "1009");


    FILE *f = fopen(full_path_filename, "rb");
    if (f)
	{
        fseek(f, 0, SEEK_END);
        long fsize = ftell(f);
        fseek(f, 0, SEEK_SET);

        uint8_t *savedata = malloc(fsize);

        size_t dummy = fread(savedata, fsize, 1, f);
		if (dummy < 1)
		{
			dbg(0, "reading savedata failed");
		}
        fclose(f);

        options.savedata_type = TOX_SAVEDATA_TYPE_TOX_SAVE;
        options.savedata_data = savedata;
        options.savedata_length = fsize;

		dbg(9, "1008");
        tox = tox_new(&options, &error);
		dbg(9, "1009 tox=%p error=%d", tox, error);

		int j = 0;
		while (error != 0)
		{
			j++;
			if (j > 100)
			{
				break;
			}
			// could not allocate network port, sleep and try again ...
			c_sleep(150);
			tox = tox_new(&options, &error);
			dbg(9, "1009 tox=%p error=%d", tox, error);
		}

        free((void *)savedata);
    }
	else
	{
		dbg(9, "1010");
        tox = tox_new(&options, NULL);
		dbg(9, "1011 tox=%p", tox);
    }

	bool local_discovery_enabled = tox_options_get_local_discovery_enabled(&options);
	dbg(9, "local discovery enabled = %d", (int)local_discovery_enabled);

	free(full_path_filename);

    return tox;
}


void update_savedata_file(const Tox *tox)
{
    size_t size = tox_get_savedata_size(tox);
    char *savedata = malloc(size);
    tox_get_savedata(tox, (uint8_t *)savedata);

	char *full_path_filename = malloc(MAX_FULL_PATH_LENGTH);
	snprintf(full_path_filename, (size_t)MAX_FULL_PATH_LENGTH, "%s/%s", app_data_dir, savedata_filename);

	char *full_path_filename_tmp = malloc(MAX_FULL_PATH_LENGTH);
	snprintf(full_path_filename_tmp, (size_t)MAX_FULL_PATH_LENGTH, "%s/%s", app_data_dir, savedata_tmp_filename);

    FILE *f = fopen(full_path_filename_tmp, "wb");
    fwrite(savedata, size, 1, f);
    fclose(f);

    rename(full_path_filename_tmp, full_path_filename);

	free(full_path_filename);
	free(full_path_filename_tmp);
    free(savedata);
}


int bin_id_to_string(const char *bin_id, size_t bin_id_size, char *output, size_t output_size)
{
    if (bin_id_size != TOX_ADDRESS_SIZE || output_size < (TOX_ADDRESS_SIZE * 2 + 1))
    {
        return -1;
    }

    size_t i;
    for (i = 0; i < TOX_ADDRESS_SIZE; ++i)
    {
        snprintf(&output[i * 2], output_size - (i * 2), "%02X", bin_id[i] & 0xff);
    }

	return 0;
}

void bootstrap_real(Tox *tox)
{

#if 1

	// these nodes seem to be faster!!
    DHT_node nodes[] =
    {
        {"178.62.250.138",             33445, "788236D34978D1D5BD822F0A5BEBD2C53C64CC31CD3149350EE27D4D9A2F9B6B", {0}},
        {"nodes.tox.chat",             33445, "6FC41E2BD381D37E9748FC0E0328CE086AF9598BECC8FEB7DDF2E440475F300E", {0}},
        {"130.133.110.14",             33445, "461FA3776EF0FA655F1A05477DF1B3B614F7D6B124F7DB1DD4FE3C08B03B640F", {0}},
        {"tox.zodiaclabs.org",         33445, "A09162D68618E742FFBCA1C2C70385E6679604B2D80EA6E84AD0996A1AC8A074", {0}},
        {"163.172.136.118",            33445, "2C289F9F37C20D09DA83565588BF496FAB3764853FA38141817A72E3F18ACA0B", {0}},
        {"217.182.143.254",             443, "7AED21F94D82B05774F697B209628CD5A9AD17E0C073D9329076A4C28ED28147", {0}},
        {"185.14.30.213",               443,  "2555763C8C460495B14157D234DD56B86300A2395554BCAE4621AC345B8C1B1B", {0}},
        {"136.243.141.187",             443,  "6EE1FADE9F55CC7938234CC07C864081FC606D8FE7B751EDA217F268F1078A39", {0}},
        {"128.199.199.197",            33445, "B05C8869DBB4EDDD308F43C1A974A20A725A36EACCA123862FDE9945BF9D3E09", {0}},
        // {"192.168.0.20",   33447, "578E5F044C98290D0368F425E0E957056B30FB995F53DEB21C3E23D7A3B4E679", {0}} ,
        // {"192.168.0.22",   33447, "578E5F044C98290D0368F425E0E957056B30FB995F53DEB21C3E23D7A3B4E679", {0}} ,
        {"biribiri.org",               33445, "F404ABAA1C99A9D37D61AB54898F56793E1DEF8BD46B1038B9D822E8460FAB67", {0}}
    };

#else

	// more nodes here, but video with TCP sucks
    DHT_node nodes[] =
    {
        {"178.62.250.138",             33445, "788236D34978D1D5BD822F0A5BEBD2C53C64CC31CD3149350EE27D4D9A2F9B6B", {0}},
        {"136.243.141.187",             443,  "6EE1FADE9F55CC7938234CC07C864081FC606D8FE7B751EDA217F268F1078A39", {0}},
        {"185.14.30.213",               443,  "2555763C8C460495B14157D234DD56B86300A2395554BCAE4621AC345B8C1B1B", {0}},
		{"biribiri.org",33445,"F404ABAA1C99A9D37D61AB54898F56793E1DEF8BD46B1038B9D822E8460FAB67", {0}},
		{"nodes.tox.chat",33445,"6FC41E2BD381D37E9748FC0E0328CE086AF9598BECC8FEB7DDF2E440475F300E", {0}},
		{"130.133.110.14",33445,"461FA3776EF0FA655F1A05477DF1B3B614F7D6B124F7DB1DD4FE3C08B03B640F", {0}},
		{"205.185.116.116",33445,"A179B09749AC826FF01F37A9613F6B57118AE014D4196A0E1105A98F93A54702", {0}},
		{"198.98.51.198",33445,"1D5A5F2F5D6233058BF0259B09622FB40B482E4FA0931EB8FD3AB8E7BF7DAF6F", {0}},
		{"108.61.165.198",33445,"8E7D0B859922EF569298B4D261A8CCB5FEA14FB91ED412A7603A585A25698832", {0}},
		{"194.249.212.109",33445,"3CEE1F054081E7A011234883BC4FC39F661A55B73637A5AC293DDF1251D9432B", {0}},
		{"185.25.116.107",33445,"DA4E4ED4B697F2E9B000EEFE3A34B554ACD3F45F5C96EAEA2516DD7FF9AF7B43", {0}},
		{"5.189.176.217",5190,"2B2137E094F743AC8BD44652C55F41DFACC502F125E99E4FE24D40537489E32F", {0}},
		{"217.182.143.254",2306,"7AED21F94D82B05774F697B209628CD5A9AD17E0C073D9329076A4C28ED28147", {0}},
		{"104.223.122.15",33445,"0FB96EEBFB1650DDB52E70CF773DDFCABE25A95CC3BB50FC251082E4B63EF82A", {0}},
		{"tox.verdict.gg",33445,"1C5293AEF2114717547B39DA8EA6F1E331E5E358B35F9B6B5F19317911C5F976", {0}},
		{"d4rk4.ru",1813,"53737F6D47FA6BD2808F378E339AF45BF86F39B64E79D6D491C53A1D522E7039", {0}},
		{"104.233.104.126",33445,"EDEE8F2E839A57820DE3DA4156D88350E53D4161447068A3457EE8F59F362414", {0}},
		{"51.254.84.212",33445,"AEC204B9A4501412D5F0BB67D9C81B5DB3EE6ADA64122D32A3E9B093D544327D", {0}},
		{"88.99.133.52",33445,"2D320F971EF2CA18004416C2AAE7BA52BF7949DB34EA8E2E21AF67BD367BE211", {0}},
		{"185.58.206.164",33445,"24156472041E5F220D1FA11D9DF32F7AD697D59845701CDD7BE7D1785EB9DB39", {0}},
		{"92.54.84.70",33445,"5625A62618CB4FCA70E147A71B29695F38CC65FF0CBD68AD46254585BE564802", {0}},
		{"195.93.190.6",33445,"FB4CE0DDEFEED45F26917053E5D24BDDA0FA0A3D83A672A9DA2375928B37023D", {0}},
		{"tox.uplinklabs.net",33445,"1A56EA3EDF5DF4C0AEABBF3C2E4E603890F87E983CAC8A0D532A335F2C6E3E1F", {0}},
		{"toxnode.nek0.net",33445,"20965721D32CE50C3E837DD75B33908B33037E6225110BFF209277AEAF3F9639", {0}},
		{"95.215.44.78",33445,"672DBE27B4ADB9D5FB105A6BB648B2F8FDB89B3323486A7A21968316E012023C", {0}},
		{"163.172.136.118",33445,"2C289F9F37C20D09DA83565588BF496FAB3764853FA38141817A72E3F18ACA0B", {0}},
		{"sorunome.de",33445,"02807CF4F8BB8FB390CC3794BDF1E8449E9A8392C5D3F2200019DA9F1E812E46", {0}},
		{"37.97.185.116",33445,"E59A0E71ADA20D35BD1B0957059D7EF7E7792B3D680AE25C6F4DBBA09114D165", {0}},
		{"193.124.186.205",5228,"9906D65F2A4751068A59D30505C5FC8AE1A95E0843AE9372EAFA3BAB6AC16C2C", {0}},
		{"80.87.193.193",33445,"B38255EE4B054924F6D79A5E6E5889EC94B6ADF6FE9906F97A3D01E3D083223A", {0}},
		{"initramfs.io",33445,"3F0A45A268367C1BEA652F258C85F4A66DA76BCAA667A49E770BCC4917AB6A25", {0}},
		{"hibiki.eve.moe",33445,"D3EB45181B343C2C222A5BCF72B760638E15ED87904625AAD351C594EEFAE03E", {0}},
		{"tox.deadteam.org",33445,"C7D284129E83877D63591F14B3F658D77FF9BA9BA7293AEB2BDFBFE1A803AF47", {0}},
		{"46.229.52.198",33445,"813C8F4187833EF0655B10F7752141A352248462A567529A38B6BBF73E979307", {0}},
		{"node.tox.ngc.network",33445,"A856243058D1DE633379508ADCAFCF944E40E1672FF402750EF712E30C42012A", {0}},
		{"144.217.86.39",33445,"7E5668E0EE09E19F320AD47902419331FFEE147BB3606769CFBE921A2A2FD34C", {0}},
		{"185.14.30.213",443,"2555763C8C460495B14157D234DD56B86300A2395554BCAE4621AC345B8C1B1B", {0}},
		{"77.37.160.178",33440,"CE678DEAFA29182EFD1B0C5B9BC6999E5A20B50A1A6EC18B91C8EBB591712416", {0}},
		{"85.21.144.224",33445,"8F738BBC8FA9394670BCAB146C67A507B9907C8E564E28C2B59BEBB2FF68711B", {0}},
		{"tox.natalenko.name",33445,"1CB6EBFD9D85448FA70D3CAE1220B76BF6FCE911B46ACDCF88054C190589650B", {0}},
		{"37.187.122.30",33445,"BEB71F97ED9C99C04B8489BB75579EB4DC6AB6F441B603D63533122F1858B51D", {0}},
		{"completelyunoriginal.moe",33445,"FBC7DED0B0B662D81094D91CC312D6CDF12A7B16C7FFB93817143116B510C13E", {0}},
		{"136.243.141.187",443,"6EE1FADE9F55CC7938234CC07C864081FC606D8FE7B751EDA217F268F1078A39", {0}},
		{"tox.abilinski.com",33445,"0E9D7FEE2AA4B42A4C18FE81C038E32FFD8D907AAA7896F05AA76C8D31A20065", {0}},
		{"95.215.46.114",33445,"5823FB947FF24CF83DDFAC3F3BAA18F96EA2018B16CC08429CB97FA502F40C23", {0}},
		{"51.15.54.207",33445,"1E64DBA45EC810C0BF3A96327DC8A9D441AB262C14E57FCE11ECBCE355305239", {0}}
    };

#endif


    for (size_t i = 0; i < sizeof(nodes)/sizeof(DHT_node); i ++)
	{
        sodium_hex2bin(nodes[i].key_bin, sizeof(nodes[i].key_bin),
                       nodes[i].key_hex, sizeof(nodes[i].key_hex)-1, NULL, NULL, NULL);
			tox_bootstrap(tox, nodes[i].ip, nodes[i].port, nodes[i].key_bin, NULL);
		tox_add_tcp_relay(tox, nodes[i].ip, nodes[i].port, nodes[i].key_bin, NULL); // also try as TCP relay
    }
}


// fill string with toxid in upper case hex.
// size of toxid_str needs to be: [TOX_ADDRESS_SIZE*2 + 1] !!
void get_my_toxid(Tox *tox, char *toxid_str)
{
    uint8_t tox_id_bin[TOX_ADDRESS_SIZE];
    tox_self_get_address(tox, tox_id_bin);

	char tox_id_hex_local[TOX_ADDRESS_SIZE*2 + 1];
    sodium_bin2hex(tox_id_hex_local, sizeof(tox_id_hex_local), tox_id_bin, sizeof(tox_id_bin));

    for (size_t i = 0; i < sizeof(tox_id_hex_local)-1; i ++)
	{
        tox_id_hex_local[i] = toupper(tox_id_hex_local[i]);
    }

	snprintf(toxid_str, (size_t)(TOX_ADDRESS_SIZE*2 + 1), "%s", (const char*)tox_id_hex_local);
}

void toxid_hex_to_bin(unsigned char * public_key, const char *toxid_str)
{
	sodium_hex2bin(public_key, TOX_ADDRESS_SIZE, toxid_str, (TOX_ADDRESS_SIZE*2), NULL, NULL, NULL);
}

void toxid_bin_to_hex(const uint8_t *public_key, char *toxid_str)
{
	char tox_id_hex_local[TOX_ADDRESS_SIZE*2 + 1];
    sodium_bin2hex(tox_id_hex_local, sizeof(tox_id_hex_local), public_key, TOX_ADDRESS_SIZE);

    for (size_t i = 0; i < sizeof(tox_id_hex_local)-1; i ++)
	{
        tox_id_hex_local[i] = toupper(tox_id_hex_local[i]);
    }

	snprintf(toxid_str, (size_t)(TOX_ADDRESS_SIZE*2 + 1), "%s", (const char*)tox_id_hex_local);
}

void print_tox_id(Tox *tox)
{
    char tox_id_hex[TOX_ADDRESS_SIZE*2 + 1];
	get_my_toxid(tox, tox_id_hex);

	dbg(2, "MyToxID:%s", tox_id_hex);
}

void bootstrap()
{
	bootstrap_real(tox_global);
}

void init_tox_callbacks()
{
	// -------- _callbacks_ --------
	tox_callback_self_connection_status(tox_global, self_connection_status_cb);
	tox_callback_friend_name(tox_global, friend_name_cb);
	tox_callback_friend_status_message(tox_global, friend_status_message_cb);
	tox_callback_friend_status(tox_global, friend_status_cb);
	tox_callback_friend_connection_status(tox_global, friend_connection_status_cb);
	tox_callback_friend_typing(tox_global, friend_typing_cb);
	tox_callback_friend_read_receipt(tox_global, friend_read_receipt_cb);
	tox_callback_friend_request(tox_global, friend_request_cb);
	tox_callback_friend_message(tox_global, friend_message_cb);

	tox_callback_file_recv_control(tox_global, file_recv_control_cb);
	tox_callback_file_chunk_request(tox_global, file_chunk_request_cb);
	tox_callback_file_recv(tox_global, file_recv_cb);
	tox_callback_file_recv_chunk(tox_global, file_recv_chunk_cb);

// tox_callback_conference_invite(tox_global, tox_conference_invite_cb *callback);
// tox_callback_conference_message(tox_global, tox_conference_message_cb *callback);
// tox_callback_conference_title(tox_global, tox_conference_title_cb *callback);
// tox_callback_conference_namelist_change(tox_global, tox_conference_namelist_change_cb *callback);
// tox_callback_friend_lossy_packet(tox_global, tox_friend_lossy_packet_cb *callback);
// tox_callback_friend_lossless_packet(tox_global, tox_friend_lossless_packet_cb *callback);
	// -------- _callbacks_ --------
}




// ------------- JNI -------------
// ------------- JNI -------------
// ------------- JNI -------------

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved)
{
	JNIEnv *env_this;
	cachedJVM = jvm;
	if ((*jvm)->GetEnv(jvm, (void**) &env_this, JNI_VERSION_1_6))
	{
		// dbg(0,"Could not get JVM");
		return JNI_ERR;
	}

	// dbg(0,"++ Found JVM ++");
	return JNI_VERSION_1_6;
}

JNIEnv* jni_getenv()
{
	JNIEnv* env_this;
	(*cachedJVM)->GetEnv(cachedJVM, (void**) &env_this, JNI_VERSION_1_6);
	return env_this;
}


JNIEnv *AttachJava()
{
	JavaVMAttachArgs args = {JNI_VERSION_1_6, 0, 0};
	JNIEnv *java;
	(*cachedJVM)->AttachCurrentThread(cachedJVM, &java, &args);
	return java;
}

int android_find_class_global(char *name, jclass *ret)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	*ret = (*jnienv2)->FindClass(jnienv2, name);
	if (!*ret)
	{
		return 0;
	}

	*ret = (*jnienv2)->NewGlobalRef(jnienv2, *ret);
	return 1;
}

int android_find_method(jclass class, char *name, char *args, jmethodID *ret)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	*ret = (*jnienv2)->GetMethodID(jnienv2, class, name, args);
	if (*ret == NULL)
	{
		return 0;
	}

	return 1;
}


int android_find_static_method(jclass class, char *name, char *args, jmethodID *ret)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	*ret = (*jnienv2)->GetStaticMethodID(jnienv2, class, name, args);
	if (*ret == NULL)
	{
		return 0;
	}

	return 1;
}

// -------- _callbacks_ --------
void android_tox_callback_self_connection_status_cb(int a_TOX_CONNECTION)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();
	(*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
          android_tox_callback_self_connection_status_cb_method, (jint)a_TOX_CONNECTION);
}

void self_connection_status_cb(Tox *tox, TOX_CONNECTION connection_status, void *user_data)
{
    switch (connection_status)
    {
        case TOX_CONNECTION_NONE:
            dbg(2, "Offline");
            my_connection_status = TOX_CONNECTION_NONE;
            android_tox_callback_self_connection_status_cb((int)my_connection_status);
            break;
        case TOX_CONNECTION_TCP:
            dbg(2, "Online, using TCP");
            my_connection_status = TOX_CONNECTION_TCP;
            android_tox_callback_self_connection_status_cb((int)my_connection_status);
            break;
        case TOX_CONNECTION_UDP:
            dbg(2, "Online, using UDP");
            my_connection_status = TOX_CONNECTION_UDP;
            android_tox_callback_self_connection_status_cb((int)my_connection_status);
            break;
    }
}

void android_tox_callback_friend_name_cb(uint32_t friend_number, const uint8_t *name, size_t length)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	jstring js1 = (*jnienv2)->NewStringUTF(jnienv2, (char *)name);

	(*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
          android_tox_callback_friend_name_cb_method, (jlong)(unsigned long long)friend_number, js1, (jlong)(unsigned long long)length);

	(*jnienv2)->DeleteLocalRef(jnienv2, js1);
}

void friend_name_cb(Tox *tox, uint32_t friend_number, const uint8_t *name, size_t length, void *user_data)
{
	android_tox_callback_friend_name_cb(friend_number, name, length);
}

void android_tox_callback_friend_status_message_cb(uint32_t friend_number, const uint8_t *message, size_t length)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	jstring js1 = (*jnienv2)->NewStringUTF(jnienv2, (char *)message);

	(*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
          android_tox_callback_friend_status_message_cb_method, (jlong)(unsigned long long)friend_number, js1, (jlong)(unsigned long long)length);

	(*jnienv2)->DeleteLocalRef(jnienv2, js1);
}

void friend_status_message_cb(Tox *tox, uint32_t friend_number, const uint8_t *message, size_t length, void *user_data)
{
	android_tox_callback_friend_status_message_cb(friend_number, message, length);
}

void android_tox_callback_friend_status_cb(uint32_t friend_number, TOX_USER_STATUS status)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	(*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
          android_tox_callback_friend_status_cb_method, (jlong)(unsigned long long)friend_number, (jint)status);
}

void friend_status_cb(Tox *tox, uint32_t friend_number, TOX_USER_STATUS status, void *user_data)
{
	android_tox_callback_friend_status_cb(friend_number, status);
}

void android_tox_callback_friend_connection_status_cb(uint32_t friend_number, TOX_CONNECTION connection_status)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

    switch (connection_status)
    {
        case TOX_CONNECTION_NONE:
            dbg(2, "friend# %d Offline", (int)friend_number);
			(*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
				android_tox_callback_friend_connection_status_cb_method, (jlong)(unsigned long long)friend_number, (jint)connection_status);
            break;
        case TOX_CONNECTION_TCP:
            dbg(2, "friend# %d Online, using TCP", (int)friend_number);
			(*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
				android_tox_callback_friend_connection_status_cb_method, (jlong)(unsigned long long)friend_number, (jint)connection_status);
            break;
        case TOX_CONNECTION_UDP:
            dbg(2, "friend# %d Online, using UDP", (int)friend_number);
			(*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
				android_tox_callback_friend_connection_status_cb_method, (jlong)(unsigned long long)friend_number, (jint)connection_status);
            break;
    }
}

void friend_connection_status_cb(Tox *tox, uint32_t friend_number, TOX_CONNECTION connection_status, void *user_data)
{
	android_tox_callback_friend_connection_status_cb(friend_number, connection_status);
}

void android_tox_callback_friend_typing_cb(uint32_t friend_number, bool is_typing)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	(*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
		android_tox_callback_friend_typing_cb_method, (jlong)(unsigned long long)friend_number, (jlong)is_typing);
}

void friend_typing_cb(Tox *tox, uint32_t friend_number, bool is_typing, void *user_data)
{
	android_tox_callback_friend_typing_cb(friend_number, is_typing);
}

void android_tox_callback_friend_read_receipt_cb(uint32_t friend_number, uint32_t message_id)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	(*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
		android_tox_callback_friend_read_receipt_cb_method, (jlong)(unsigned long long)friend_number, (jlong)(unsigned long long)message_id);
}

void friend_read_receipt_cb(Tox *tox, uint32_t friend_number, uint32_t message_id, void *user_data)
{
	android_tox_callback_friend_read_receipt_cb(friend_number, message_id);
}

void android_tox_callback_friend_request_cb(const uint8_t *public_key, const uint8_t *message, size_t length)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

    char tox_id_hex[TOX_ADDRESS_SIZE*2 + 1];
	CLEAR(tox_id_hex);
	toxid_bin_to_hex(public_key, tox_id_hex);
	tox_id_hex[TOX_PUBLIC_KEY_SIZE * 2] = '\0'; // fix to correct size of public key

	dbg(9, "pubkey string=%s", tox_id_hex);

	jstring js1 = (*jnienv2)->NewStringUTF(jnienv2, tox_id_hex);
	jstring js2 = (*jnienv2)->NewStringUTF(jnienv2, message);

	(*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
		android_tox_callback_friend_request_cb_method, js1, js2, (jlong)(unsigned long long)length);

	(*jnienv2)->DeleteLocalRef(jnienv2, js1);
	(*jnienv2)->DeleteLocalRef(jnienv2, js2);

}

void friend_request_cb(Tox *tox, const uint8_t *public_key, const uint8_t *message, size_t length, void *user_data)
{
	android_tox_callback_friend_request_cb(public_key, message, length);
}

void android_tox_callback_friend_message_cb(uint32_t friend_number, TOX_MESSAGE_TYPE type, const uint8_t *message, size_t length)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	jstring js1 = (*jnienv2)->NewStringUTF(jnienv2, (char *)message);

	(*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
          android_tox_callback_friend_message_cb_method, (jlong)(unsigned long long)friend_number, (jint) type, js1, (jlong)(unsigned long long)length);

	(*jnienv2)->DeleteLocalRef(jnienv2, js1);
}

void friend_message_cb(Tox *tox, uint32_t friend_number, TOX_MESSAGE_TYPE type, const uint8_t *message, size_t length, void *user_data)
{
	android_tox_callback_friend_message_cb(friend_number, type, message, length);
}

void android_tox_callback_file_recv_control_cb(uint32_t friend_number, uint32_t file_number, TOX_FILE_CONTROL control)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	(*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
          android_tox_callback_file_recv_control_cb_method, (jlong)(unsigned long long)friend_number, (jlong)(unsigned long long)file_number, (jint)control);
}

void file_recv_control_cb(Tox *tox, uint32_t friend_number, uint32_t file_number, TOX_FILE_CONTROL control, void *user_data)
{
	android_tox_callback_file_recv_control_cb(friend_number, file_number, control);
}

void android_tox_callback_file_chunk_request_cb(uint32_t friend_number, uint32_t file_number, uint64_t position, size_t length)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	(*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
          android_tox_callback_file_chunk_request_cb_method, (jlong)(unsigned long long)friend_number, (jlong)(unsigned long long)file_number, (jlong)(unsigned long long)position, (jlong)(unsigned long long)length);

}

void file_chunk_request_cb(Tox *tox, uint32_t friend_number, uint32_t file_number, uint64_t position, size_t length, void *user_data)
{
	android_tox_callback_file_chunk_request_cb(friend_number, file_number, position, length);
}

void android_tox_callback_file_recv_cb(uint32_t friend_number, uint32_t file_number, uint32_t kind, uint64_t file_size, const uint8_t *filename, size_t filename_length)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();
	char filename_unknown[] = "unknown.png";
	size_t len = filename_length;
	char *mystr = NULL;

	dbg(9, "file_recv_cb:001:file_number=%d", (int)file_number);
	dbg(9, "file_recv_cb:filename=%p filename_length=%d", filename, (int)filename_length);

	dbg(9, "file_recv_cb:002");

	if ((! filename)||(filename_length == 0))
	{
		dbg(9, "file_recv_cb:003");
		dbg(9, "file_recv_cb:004");
		len = strlen(filename_unknown);
		mystr = filename_unknown;
		dbg(9, "file_recv_cb:005");
	}
	else
	{
		dbg(9, "file_recv_cb:006");
		mystr = (char *)filename;
		dbg(9, "file_recv_cb:007");
	}

	jstring js1 = c_safe_string_from_java(mystr, len);

	(*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
          android_tox_callback_file_recv_cb_method, (jlong)(unsigned long long)friend_number, (jlong)(unsigned long long)file_number, (jint)kind, (jlong)(unsigned long long)file_size,
				js1, (jlong)(unsigned long long)len);

	(*jnienv2)->DeleteLocalRef(jnienv2, js1);

	dbg(9, "file_recv_cb:009");
}

jstring c_safe_string_from_java(char *instr, size_t len)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	jbyteArray data = (*jnienv2)->NewByteArray(jnienv2, (int)len);
	(*jnienv2)->SetByteArrayRegion(jnienv2, data, 0, (int)len, (const jbyte*)instr);

	jstring js1 = (jstring)(*jnienv2)->CallStaticObjectMethod(jnienv2, TrifaToxService_class, safe_string_method, data);
	(*jnienv2)->DeleteLocalRef(jnienv2, data);

	return js1;
}

void file_recv_cb(Tox *tox, uint32_t friend_number, uint32_t file_number, uint32_t kind, uint64_t file_size, const uint8_t *filename, size_t filename_length, void *user_data)
{
	android_tox_callback_file_recv_cb(friend_number, file_number, kind, file_size, filename, filename_length);
}

void android_tox_callback_file_recv_chunk_cb(uint32_t friend_number, uint32_t file_number, uint64_t position, const uint8_t *data, size_t length)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

    jbyteArray data2 = (*jnienv2)->NewByteArray(jnienv2, (int)length);
    if (data2 == NULL)
	{
        // return NULL; // out of memory error thrown
    }

	// TODO: !! assuming sizeof(jbyte) == sizeof(uint8_t) !!
	// TODO: !! assuming sizeof(jbyte) == sizeof(uint8_t) !!
    (*jnienv2)->SetByteArrayRegion(jnienv2, data2, 0, (int)length, (const jbyte*)data);
	// TODO: !! assuming sizeof(jbyte) == sizeof(uint8_t) !!
	// TODO: !! assuming sizeof(jbyte) == sizeof(uint8_t) !!

	(*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
          android_tox_callback_file_recv_chunk_cb_method, (jlong)(unsigned long long)friend_number, (jlong)(unsigned long long)file_number,
				(jlong)(unsigned long long)position, data2, (jlong)(unsigned long long)length);

}

void file_recv_chunk_cb(Tox *tox, uint32_t friend_number, uint32_t file_number, uint64_t position, const uint8_t *data, size_t length, void *user_data)
{
	android_tox_callback_file_recv_chunk_cb(friend_number, file_number, position, data, length);
}

void android_tox_log_cb(TOX_LOG_LEVEL level, const char *file, uint32_t line, const char *func, const char *message)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	jstring js1 = (*jnienv2)->NewStringUTF(jnienv2, file);
	jstring js2 = (*jnienv2)->NewStringUTF(jnienv2, func);
	jstring js3 = (*jnienv2)->NewStringUTF(jnienv2, message);

	(*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity, android_tox_log_cb_method, (int)level, js1, (jlong)(unsigned long long)line, js2, js3);

	(*jnienv2)->DeleteLocalRef(jnienv2, js1);
	(*jnienv2)->DeleteLocalRef(jnienv2, js2);
	(*jnienv2)->DeleteLocalRef(jnienv2, js3);
}

void tox_log_cb__custom(Tox *tox, TOX_LOG_LEVEL level, const char *file, uint32_t line, const char *func, const char *message, void *user_data)
{
	android_tox_log_cb(level, file, line, func, message);
}


// ------------- AV ------------
// ------------- AV ------------
void android_toxav_callback_call_state_cb(uint32_t friend_number, uint32_t state)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	(*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
          android_toxav_callback_call_state_cb_method, (jlong)(unsigned long long)friend_number, (jint)state);

}
void toxav_call_state_cb_(ToxAV *av, uint32_t friend_number, uint32_t state, void *user_data)
{
	android_toxav_callback_call_state_cb(friend_number, state);
}

void android_toxav_callback_bit_rate_status_cb(uint32_t friend_number, uint32_t audio_bit_rate, uint32_t video_bit_rate)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	(*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
          android_toxav_callback_bit_rate_status_cb_method, (jlong)(unsigned long long)friend_number, (jlong)(unsigned long long)audio_bit_rate, (jlong)(unsigned long long)video_bit_rate);

}

void toxav_bit_rate_status_cb_(ToxAV *av, uint32_t friend_number, uint32_t audio_bit_rate, uint32_t video_bit_rate, void *user_data)
{
	android_toxav_callback_bit_rate_status_cb(friend_number, audio_bit_rate, video_bit_rate);
}

void android_toxav_callback_audio_receive_frame_cb(uint32_t friend_number, size_t sample_count, uint8_t channels, uint32_t sampling_rate)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	(*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
          android_toxav_callback_audio_receive_frame_cb_method, (jlong)(unsigned long long)friend_number,
		(jlong)(unsigned long long)sample_count, (jint)channels,
		(jlong)(unsigned long long)sampling_rate
		);
}

/**
 * The function type for the audio_receive_frame callback. The callback can be
 * called multiple times per single iteration depending on the amount of queued
 * frames in the buffer. The received format is the same as in send function.
 *
 * @param friend_number The friend number of the friend who sent an audio frame.
 * @param pcm An array of audio samples (sample_count * channels elements).
 * @param sample_count The number of audio samples per channel in the PCM array.
 * @param channels Number of audio channels.
 * @param sampling_rate Sampling rate used in this frame.
 *
 */
void toxav_audio_receive_frame_cb_(ToxAV *av, uint32_t friend_number, const int16_t *pcm, size_t sample_count, uint8_t channels, uint32_t sampling_rate, void *user_data)
{
	if ((audio_buffer_pcm_2 != NULL) && (pcm != NULL))
	{
		memcpy((void*)audio_buffer_pcm_2, (void*)pcm, (size_t)(sample_count * channels * 2));
	}

	android_toxav_callback_audio_receive_frame_cb(friend_number, sample_count, channels, sampling_rate);
}

void android_toxav_callback_video_receive_frame_cb(uint32_t friend_number, uint16_t width, uint16_t height, int32_t ystride, int32_t ustride, int32_t vstride)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	(*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
          android_toxav_callback_video_receive_frame_cb_method, (jlong)(unsigned long long)friend_number,
		(jlong)(unsigned long long)width, (jlong)(unsigned long long)height,
		(jlong)(unsigned long long)ystride,
		(jlong)(unsigned long long)ustride, (jlong)(unsigned long long)vstride
		);
}

// ----- get video buffer from Java -----
// ----- get video buffer from Java -----
// ----- get video buffer from Java -----
JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_set_1JNI_1video_1buffer(JNIEnv* env, jobject thiz, jobject buffer, jint frame_width_px, jint frame_height_px)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

    // jclass cls = (*jnienv2)->GetObjectClass(jnienv2, buffer);
    // jmethodID mid = (*jnienv2)->GetMethodID(jnienv2, cls, "limit", "(I)Ljava/nio/Buffer;");

    video_buffer_1 = (uint8_t*)(*jnienv2)->GetDirectBufferAddress(jnienv2, buffer);
    jlong capacity = (*jnienv2)->GetDirectBufferCapacity(jnienv2, buffer);
	video_buffer_1_size = (long)capacity;

	video_buffer_1_width = (int)frame_width_px;
	video_buffer_1_height = (int)frame_height_px;
	video_buffer_1_y_size = (int)(frame_width_px * frame_height_px);
	video_buffer_1_u_size = (int)(video_buffer_1_y_size / 4);
	video_buffer_1_v_size = (int)(video_buffer_1_y_size / 4);
	video_buffer_1_u = (uint8_t*)(video_buffer_1 + video_buffer_1_y_size);
	video_buffer_1_v = (uint8_t*)(video_buffer_1 + video_buffer_1_y_size + video_buffer_1_u_size);

    int written = 0;

    // (*jnienv2)->CallObjectMethod(jnienv2, buffer, mid, written);
    return written;
}
// ----- get video buffer from Java -----
// ----- get video buffer from Java -----
// ----- get video buffer from Java -----





// ----- get video buffer 2 from Java -----
// ----- get video buffer 2 from Java -----
// ----- get video buffer 2 from Java -----
JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_set_1JNI_1video_1buffer2(JNIEnv* env, jobject thiz, jobject buffer2, jint frame_width_px, jint frame_height_px)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

    video_buffer_2 = (uint8_t*)(*jnienv2)->GetDirectBufferAddress(jnienv2, buffer2);
	dbg(9, "video_buffer_2=(call.a)%p buffer2=%p", video_buffer_2, buffer2);
    jlong capacity = (*jnienv2)->GetDirectBufferCapacity(jnienv2, buffer2);
	dbg(9, "video_buffer_2=(call.b)capacity");
	video_buffer_2_size = (long)capacity;
	dbg(9, "video_buffer_2=(call.b)capacity=%d", (int)video_buffer_2_size);
}
// ----- get video buffer 2 from Java -----
// ----- get video buffer 2 from Java -----
// ----- get video buffer 2 from Java -----



// ----- get audio buffer from Java -----
// ----- get audio buffer from Java -----
// ----- get audio buffer from Java -----
JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_set_1JNI_1audio_1buffer(JNIEnv* env, jobject thiz, jobject audio_buffer)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

    audio_buffer_pcm_1 = (uint8_t*)(*jnienv2)->GetDirectBufferAddress(jnienv2, audio_buffer);

	dbg(9, "audio_buffer_1=(call)%p audio_buffer=%p", audio_buffer_pcm_1, audio_buffer);

    jlong capacity = (*jnienv2)->GetDirectBufferCapacity(jnienv2, audio_buffer);
	audio_buffer_pcm_1_size = (long)capacity;
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_set_1JNI_1audio_1buffer2(JNIEnv* env, jobject thiz, jobject audio_buffer2)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

    audio_buffer_pcm_2 = (uint8_t*)(*jnienv2)->GetDirectBufferAddress(jnienv2, audio_buffer2);

    jlong capacity = (*jnienv2)->GetDirectBufferCapacity(jnienv2, audio_buffer2);
	audio_buffer_pcm_2_size = (long)capacity;

	dbg(9, "audio_buffer_2_=================================");
	dbg(9, "audio_buffer_2_=================================");
	dbg(9, "audio_buffer_2_=(call)%p audio_buffer2=%p size in bytes=%d", audio_buffer_pcm_2, audio_buffer2, (int)audio_buffer_pcm_2_size);
	dbg(9, "audio_buffer_2_=================================");
	dbg(9, "audio_buffer_2_=================================");
}

// ----- get audio buffer from Java -----
// ----- get audio buffer from Java -----
// ----- get audio buffer from Java -----




/*
 * @param y Luminosity plane. Size = MAX(width, abs(ystride)) * height.
 * @param u U chroma plane. Size = MAX(width/2, abs(ustride)) * (height/2).
 * @param v V chroma plane. Size = MAX(width/2, abs(vstride)) * (height/2).
 */
void toxav_video_receive_frame_cb_(ToxAV *av, uint32_t friend_number, uint16_t width, uint16_t height,
     const uint8_t *y, const uint8_t *u, const uint8_t *v, int32_t ystride, int32_t ustride, int32_t vstride, void *user_data)
{
	if (video_buffer_1 != NULL)
	{
		if ((y) && (u) && (v))
		{
			// copy the Y layer into the buffer
			// dbg(9, "[V1]video_buffer_1=%p,y=%p,u=%p,v=%p", video_buffer_1, y, u, v);
			memcpy(video_buffer_1, y, (size_t)(video_buffer_1_y_size));
			// copy the U layer into the buffer
			// dbg(9, "[V2]video_buffer_1=%p,y=%p,u=%p,v=%p", video_buffer_1, y, u, v);
			memcpy(video_buffer_1_u, u, (size_t)(video_buffer_1_u_size));
			// copy the V layer into the buffer
			// dbg(9, "[V3]video_buffer_1=%p,y=%p,u=%p,v=%p", video_buffer_1, y, u, v);
			memcpy(video_buffer_1_v, v, (size_t)(video_buffer_1_v_size));
			// dbg(9, "[V4]video_buffer_1=%p,y=%p,u=%p,v=%p", video_buffer_1, y, u, v);
		}
	}

	android_toxav_callback_video_receive_frame_cb(friend_number, width, height, ystride, ustride, vstride);
}

void android_toxav_callback_call_cb(uint32_t friend_number, bool audio_enabled, bool video_enabled)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	(*jnienv2)->CallStaticVoidMethod(jnienv2, MainActivity,
          android_toxav_callback_call_cb_method, (jlong)(unsigned long long)friend_number, (jint)audio_enabled, (jint)video_enabled);
}

void toxav_call_cb_(ToxAV *av, uint32_t friend_number, bool audio_enabled, bool video_enabled, void *user_data)
{
	android_toxav_callback_call_cb(friend_number, audio_enabled, video_enabled);
}
// ------------- AV ------------
// ------------- AV ------------

// -------- _callbacks_ --------






void android_logger(int level, const char* logtext)
{
	if ((TrifaToxService_class) && (logger_method) && (logtext))
	{
		if (strlen(logtext) > 0)
		{
			JNIEnv *jnienv2;
			jnienv2 = jni_getenv();

			jstring js2 = (*jnienv2)->NewStringUTF(jnienv2, logtext);
			(*jnienv2)->CallStaticVoidMethod(jnienv2, TrifaToxService_class, logger_method, level, js2);
			(*jnienv2)->DeleteLocalRef(jnienv2, js2);
		}
	}
}

void yieldcpu(uint32_t ms)
{
    usleep(1000 * ms);
}

void *thread_av(void *data)
{
	JavaVMAttachArgs args = {JNI_VERSION_1_6, 0, 0};
	JNIEnv *env;
	(*cachedJVM)->AttachCurrentThread(cachedJVM, &env, &args);

	dbg(9, "2001");
	ToxAV *av = (ToxAV *) data;
	dbg(9, "2002");

	pthread_t id = pthread_self();
	dbg(9, "2003");
	pthread_mutex_t av_thread_lock;
	dbg(9, "2004");

	if (pthread_mutex_init(&av_thread_lock, NULL) != 0)
	{
		dbg(0, "Error creating av_thread_lock");
	}
	else
	{
		dbg(2, "av_thread_lock created successfully");
	}

	dbg(2, "AV Thread #%d: starting", (int) id);

	while (toxav_iterate_thread_stop != 1)
	{
		usleep(toxav_iteration_interval(av) * 1000);
		// yieldcpu(10);
	}

	dbg(2, "ToxVideo:Clean thread exit!\n");

	(*cachedJVM)->DetachCurrentThread(cachedJVM);
	env = NULL;
}


void *thread_video_av(void *data)
{
	JavaVMAttachArgs args = {JNI_VERSION_1_6, 0, 0};
	JNIEnv *env;
	(*cachedJVM)->AttachCurrentThread(cachedJVM, &env, &args);

	dbg(9, "2001");
	ToxAV *av = (ToxAV *) data;
	dbg(9, "2002");

	pthread_t id = pthread_self();
	dbg(9, "2003");
	pthread_mutex_t av_thread_lock;
	dbg(9, "2004");

	if (pthread_mutex_init(&av_thread_lock, NULL) != 0)
	{
		dbg(0, "Error creating video av_thread_lock");
	}
	else
	{
		dbg(2, "av_thread_lock video created successfully");
	}

	dbg(2, "AV video Thread #%d: starting", (int) id);

	while (toxav_video_thread_stop != 1)
	{
		pthread_mutex_lock(&av_thread_lock);
		toxav_iterate(av);
		// dbg(9, "AV video Thread #%d running ...", (int) id);
		pthread_mutex_unlock(&av_thread_lock);

		usleep(toxav_iteration_interval(av) * 1000);
	}

	dbg(2, "ToxVideo:Clean video thread exit!\n");

	(*cachedJVM)->DetachCurrentThread(cachedJVM);
	env = NULL;
}


void Java_com_zoffcc_applications_trifa_MainActivity_init__real(JNIEnv* env, jobject thiz, jobject datadir, jint udp_enabled)
{
	const char *s = NULL;

	// SET GLOBAL JNIENV here, this is bad!!
	// SET GLOBAL JNIENV here, this is bad!!
	// SET GLOBAL JNIENV here, this is bad!!
	// jnienv = env;
	// dbg(0,"jnienv=%p", env);
	// SET GLOBAL JNIENV here, this is bad!!
	// SET GLOBAL JNIENV here, this is bad!!
	// SET GLOBAL JNIENV here, this is bad!!

	// ------------------- *********** -------------------
	// ------------------- *********** -------------------
	// ------------------- *********** -------------------
	TrifaToxService_class = NULL;
	android_find_class_global("com/zoffcc/applications/trifa/TrifaToxService", &TrifaToxService_class);
	logger_method = (*env)->GetStaticMethodID(env, TrifaToxService_class, "logger", "(ILjava/lang/String;)V");
	safe_string_method = (*env)->GetStaticMethodID(env, TrifaToxService_class, "safe_string", "([B)Ljava/lang/String;");

	dbg(9, "TrifaToxService=%p", TrifaToxService_class);
	dbg(9, "safe_string_method=%p", safe_string_method);
	dbg(9, "logger_method=%p", logger_method);
	// ------------------- *********** -------------------
	// ------------------- *********** -------------------
	// ------------------- *********** -------------------


	jclass cls_local = (*env)->GetObjectClass(env, thiz);
	MainActivity = (*env)->NewGlobalRef(env, cls_local);
	// logger_method = (*env)->GetStaticMethodID(env, MainActivity, "logger", "(ILjava/lang/String;)V");
	dbg(9, "cls_local=%p", cls_local);
	dbg(9, "MainActivity=%p", MainActivity);

	dbg(9, "Logging test ---***---");

	int thread_id = gettid();
	dbg(9, "THREAD ID=%d", thread_id);

	s =  (*env)->GetStringUTFChars(env, datadir, NULL);
	app_data_dir = strdup(s);
	dbg(9, "app_data_dir=%s", app_data_dir);
	(*env)->ReleaseStringUTFChars(env, datadir, s);

	// jclass class2 = NULL;
	// android_find_class_global("com/zoffcc/applications/trifa/MainActivity", &class2);
	// dbg(9, "class2=%p", class2);
	// safe_string_method = (*env)->GetStaticMethodID(env, MainActivity, "safe_string", "([B)Ljava/lang/String;");


	// jmethodID test_method = NULL;
	// android_find_method(class2, "test", "(I)V", &test_method);
	// dbg(9, "test_method=%p", test_method);
	// (*env)->CallVoidMethod(env, thiz, test_method, 79);

	// -------- _callbacks_ --------
	dbg(9, "linking callbacks ... START");
	android_tox_callback_self_connection_status_cb_method = (*env)->GetStaticMethodID(env, MainActivity, "android_tox_callback_self_connection_status_cb_method", "(I)V");
	android_tox_callback_friend_name_cb_method = (*env)->GetStaticMethodID(env, MainActivity, "android_tox_callback_friend_name_cb_method", "(JLjava/lang/String;J)V");
	android_tox_callback_friend_status_message_cb_method = (*env)->GetStaticMethodID(env, MainActivity, "android_tox_callback_friend_status_message_cb_method", "(JLjava/lang/String;J)V");
	android_tox_callback_friend_status_cb_method = (*env)->GetStaticMethodID(env, MainActivity, "android_tox_callback_friend_status_cb_method", "(JI)V");
	android_tox_callback_friend_connection_status_cb_method = (*env)->GetStaticMethodID(env, MainActivity, "android_tox_callback_friend_connection_status_cb_method", "(JI)V");
	android_tox_callback_friend_typing_cb_method = (*env)->GetStaticMethodID(env, MainActivity, "android_tox_callback_friend_typing_cb_method", "(JI)V");
	android_tox_callback_friend_read_receipt_cb_method = (*env)->GetStaticMethodID(env, MainActivity, "android_tox_callback_friend_read_receipt_cb_method", "(JJ)V");
	android_tox_callback_friend_request_cb_method = (*env)->GetStaticMethodID(env, MainActivity, "android_tox_callback_friend_request_cb_method", "(Ljava/lang/String;Ljava/lang/String;J)V");
	android_tox_callback_friend_message_cb_method = (*env)->GetStaticMethodID(env, MainActivity, "android_tox_callback_friend_message_cb_method", "(JILjava/lang/String;J)V");

	android_tox_callback_file_recv_control_cb_method = (*env)->GetStaticMethodID(env, MainActivity, "android_tox_callback_file_recv_control_cb_method", "(JJI)V");
	android_tox_callback_file_chunk_request_cb_method = (*env)->GetStaticMethodID(env, MainActivity, "android_tox_callback_file_chunk_request_cb_method", "(JJJJ)V");
	android_tox_callback_file_recv_cb_method = (*env)->GetStaticMethodID(env, MainActivity, "android_tox_callback_file_recv_cb_method", "(JJIJLjava/lang/String;J)V");
	android_tox_callback_file_recv_chunk_cb_method = (*env)->GetStaticMethodID(env, MainActivity, "android_tox_callback_file_recv_chunk_cb_method", "(JJJ[BJ)V");

	android_tox_log_cb_method = (*env)->GetStaticMethodID(env, MainActivity, "android_tox_log_cb_method", "(ILjava/lang/String;JLjava/lang/String;Ljava/lang/String;)V");
	dbg(9, "linking callbacks ... READY");
	// -------- _callbacks_ --------


	// ----------- create Tox instance -----------
	tox_global = create_tox((int)udp_enabled);
	dbg(9, "tox_global=%p", tox_global);
	// ----------- create Tox instance -----------

	// dbg(9, "1001");
	// const char *name = "TRIfA";
	// dbg(9, "1002");
	// tox_self_set_name(tox_global, (uint8_t *)name, strlen(name), NULL);
	// dbg(9, "1003");
	// const char *status_message = "This is TRIfA";
	// dbg(9, "1004");
	// tox_self_set_status_message(tox_global, (uint8_t *)status_message, strlen(status_message), NULL);
	// dbg(9, "1005");


	// ----------- create Tox AV instance --------
    TOXAV_ERR_NEW rc;
	dbg(2, "new Tox AV");
    tox_av_global = toxav_new(tox_global, &rc);
	if (rc != TOXAV_ERR_NEW_OK)
	{
		dbg(0, "Error at toxav_new: %d", rc);
	}

	memset(&mytox_CC, 0, sizeof(CallControl));
	// ----------- create Tox AV instance --------

    // init AV callbacks -------------------------------
	dbg(9, "linking AV callbacks ... START");
	android_toxav_callback_call_cb_method = (*env)->GetStaticMethodID(env, MainActivity, "android_toxav_callback_call_cb_method", "(JII)V");
    toxav_callback_call(tox_av_global, toxav_call_cb_, &mytox_CC);
	android_toxav_callback_video_receive_frame_cb_method = (*env)->GetStaticMethodID(env, MainActivity, "android_toxav_callback_video_receive_frame_cb_method", "(JJJJJJ)V");
    toxav_callback_video_receive_frame(tox_av_global, toxav_video_receive_frame_cb_, &mytox_CC);
	android_toxav_callback_call_state_cb_method = (*env)->GetStaticMethodID(env, MainActivity, "android_toxav_callback_call_state_cb_method", "(JI)V");
    toxav_callback_call_state(tox_av_global, toxav_call_state_cb_, &mytox_CC);
	android_toxav_callback_bit_rate_status_cb_method = (*env)->GetStaticMethodID(env, MainActivity, "android_toxav_callback_bit_rate_status_cb_method", "(JJJ)V");
    toxav_callback_bit_rate_status(tox_av_global, toxav_bit_rate_status_cb_, &mytox_CC);
	android_toxav_callback_audio_receive_frame_cb_method = (*env)->GetStaticMethodID(env, MainActivity, "android_toxav_callback_audio_receive_frame_cb_method", "(JJIJ)V");
    toxav_callback_audio_receive_frame(tox_av_global, toxav_audio_receive_frame_cb_, &mytox_CC);
	dbg(9, "linking AV callbacks ... READY");
	// init AV callbacks -------------------------------

	// start toxav thread ------------------------------
	toxav_iterate_thread_stop = 0;
    if (pthread_create(&(tid[0]), NULL, thread_av, (void *)tox_av_global) != 0)
	{
        dbg(0, "AV iterate Thread create failed");
	}
	else
	{
        dbg(2, "AV iterate Thread successfully created");
	}

	toxav_video_thread_stop = 0;
    if (pthread_create(&(tid[1]), NULL, thread_video_av, (void *)tox_av_global) != 0)
	{
        dbg(0, "AV video Thread create failed");
	}
	else
	{
        dbg(2, "AV video Thread successfully created");
	}
	// start toxav thread ------------------------------
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_init(JNIEnv* env, jobject thiz, jobject datadir, jint udp_enabled)
{
	COFFEE_TRY_JNI(env, Java_com_zoffcc_applications_trifa_MainActivity_init__real(env, thiz, datadir, udp_enabled));
}


// --------------- _toxfuncs_ ---------------
// --------------- _toxfuncs_ ---------------
// --------------- _toxfuncs_ ---------------
void Java_com_zoffcc_applications_trifa_MainActivity_update_1savedata_1file__real(JNIEnv* env, jobject thiz)
{
	dbg(9, "update_savedata_file");
	update_savedata_file(tox_global);
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_update_1savedata_1file(JNIEnv* env, jobject thiz)
{
	COFFEE_TRY_JNI(env, Java_com_zoffcc_applications_trifa_MainActivity_update_1savedata_1file__real(env, thiz));
}

// -----------------
// -----------------
// -----------------
int add_tcp_relay_single(Tox *tox, const char *ip, uint16_t port, const char *key_hex)
{
    unsigned char key_bin[TOX_PUBLIC_KEY_SIZE];
	toxid_hex_to_bin(key_bin, key_hex);
	int res1 = sodium_hex2bin(key_bin, sizeof(key_bin), key_hex, sizeof(key_hex)-1, NULL, NULL, NULL);
	dbg(9, "sodium_hex2bin:res=%d", res1);
	TOX_ERR_BOOTSTRAP error;
	bool res = tox_add_tcp_relay(tox, ip, port, key_bin, &error); // also try as TCP relay

	if (res != true)
	{
		if (error == TOX_ERR_BOOTSTRAP_OK)
		{
			return 0;
		}
		else if (error == TOX_ERR_BOOTSTRAP_NULL)
		{
			return 1;
		}
		else if (error == TOX_ERR_BOOTSTRAP_BAD_HOST)
		{
			return 2;
		}
		else if (error == TOX_ERR_BOOTSTRAP_BAD_PORT)
		{
			return 3;
		}
	}
	else
	{
		return 0;
	}
}

int Java_com_zoffcc_applications_trifa_MainActivity_add_1tcp_1relay_1single__real(JNIEnv* env, jobject thiz, jstring ip, jstring key_hex, long port)
{
	dbg(9, "add_tcp_relay_single1");

	const char *key_hex_str = NULL;
	const char *ip_str = NULL;

	key_hex_str =  (*env)->GetStringUTFChars(env, key_hex, NULL);
	char *key_hex_str2 = strdup(key_hex_str);

	ip_str =  (*env)->GetStringUTFChars(env, ip, NULL);
	char *ip_str2 = strdup(ip_str);

	int res = add_tcp_relay_single(tox_global, ip_str2, (uint16_t)port, key_hex_str2);

	(*env)->ReleaseStringUTFChars(env, ip, ip_str);
	(*env)->ReleaseStringUTFChars(env, key_hex, key_hex_str);

	if (ip_str2)
	{
		free(ip_str2);
	}

	if (key_hex_str2)
	{
		free(key_hex_str2);
	}

	return res;
}

JNIEXPORT int JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_add_1tcp_1relay_1single(JNIEnv* env, jobject thiz, jstring ip, jstring key_hex, long port)
{
	jint retcode = 0;
	COFFEE_TRY_JNI(env, retcode = Java_com_zoffcc_applications_trifa_MainActivity_add_1tcp_1relay_1single__real(env, thiz, ip, key_hex, port));
	return retcode;
}

int bootstrap_single(Tox *tox, const char *ip, uint16_t port, const char *key_hex)
{
    unsigned char key_bin[TOX_PUBLIC_KEY_SIZE];
	toxid_hex_to_bin(key_bin, key_hex);
	int res1 = sodium_hex2bin(key_bin, sizeof(key_bin), key_hex, sizeof(key_hex)-1, NULL, NULL, NULL);
	dbg(9, "sodium_hex2bin:res=%d", res1);
	TOX_ERR_BOOTSTRAP error;
	bool res = tox_bootstrap(tox, ip, port, key_bin, &error);
	if (res != true)
	{
		if (error == TOX_ERR_BOOTSTRAP_OK)
		{
			return 0;
		}
		else if (error == TOX_ERR_BOOTSTRAP_NULL)
		{
			return 1;
		}
		else if (error == TOX_ERR_BOOTSTRAP_BAD_HOST)
		{
			return 2;
		}
		else if (error == TOX_ERR_BOOTSTRAP_BAD_PORT)
		{
			return 3;
		}
	}
	else
	{
		return 0;
	}
}

int Java_com_zoffcc_applications_trifa_MainActivity_bootstrap_1single__real(JNIEnv* env, jobject thiz, jobject ip, jobject key_hex, long port)
{
	dbg(9, "bootstrap_single");
	const char *ip_str =  (*env)->GetStringUTFChars(env, ip, NULL);
	const char *key_hex_str =  (*env)->GetStringUTFChars(env, key_hex, NULL);
	int res = bootstrap_single(tox_global, ip_str, (uint16_t)port, key_hex_str);
	(*env)->ReleaseStringUTFChars(env, key_hex, key_hex_str);
	(*env)->ReleaseStringUTFChars(env, ip, ip_str);

	return res;
}

JNIEXPORT int JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_bootstrap_1single(JNIEnv* env, jobject thiz, jobject ip, jobject key_hex, long port)
{
	jint retcode = 0;
	COFFEE_TRY_JNI(env, retcode = Java_com_zoffcc_applications_trifa_MainActivity_bootstrap_1single__real(env, thiz, ip, key_hex, port));
	return retcode;
}
// -----------------
// -----------------
// -----------------






JNIEXPORT jstring JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_get_1my_1toxid(JNIEnv* env, jobject thiz)
{
	jstring result;

	dbg(9, "get_my_toxid");
    char tox_id_hex[TOX_ADDRESS_SIZE*2 + 1];
	get_my_toxid(tox_global, tox_id_hex);
	dbg(2, "MyToxID:%s", tox_id_hex);

	result = (*env)->NewStringUTF(env, tox_id_hex); // C style string to Java String
	return result;
}


void Java_com_zoffcc_applications_trifa_MainActivity_bootstrap__real(JNIEnv* env, jobject thiz)
{
	dbg(9, "bootstrap");
	bootstrap();
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_bootstrap(JNIEnv* env, jobject thiz)
{
	COFFEE_TRY_JNI(env, Java_com_zoffcc_applications_trifa_MainActivity_bootstrap__real(env, thiz));
}


void Java_com_zoffcc_applications_trifa_MainActivity_init_1tox_1callbacks__real(JNIEnv* env, jobject thiz)
{
	dbg(9, "init_tox_callbacks");
	init_tox_callbacks();
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_init_1tox_1callbacks(JNIEnv* env, jobject thiz)
{
	COFFEE_TRY_JNI(env, Java_com_zoffcc_applications_trifa_MainActivity_init_1tox_1callbacks__real(env, thiz));
}



void Java_com_zoffcc_applications_trifa_MainActivity_tox_1iterate__real(JNIEnv* env, jobject thiz)
{
	// dbg(9, "tox_iterate ... START");
	tox_iterate(tox_global, NULL);
	// dbg(9, "tox_iterate ... READY");
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1iterate(JNIEnv* env, jobject thiz)
{
	COFFEE_TRY_JNI(env, Java_com_zoffcc_applications_trifa_MainActivity_tox_1iterate__real(env, thiz));
}

JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1get_1friend_1list_1size(JNIEnv* env, jobject thiz)
{
	size_t numfriends = tox_self_get_friend_list_size(tox_global);
	return (jlong)(unsigned long long)numfriends;
}

JNIEXPORT jstring JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1get_1public_1key(JNIEnv* env, jobject thiz, jlong friend_number)
{
	jstring result;

	uint8_t public_key[TOX_PUBLIC_KEY_SIZE];
	TOX_ERR_FRIEND_GET_PUBLIC_KEY error;
    bool res = tox_friend_get_public_key(tox_global, (uint32_t)friend_number, public_key, &error);

	if (res == false)
	{
		result = (*env)->NewStringUTF(env, "-1"); // C style string to Java String
	}
	else
	{
		char tox_id_hex[TOX_ADDRESS_SIZE*2 + 1]; // need this wrong size for next call
		CLEAR(tox_id_hex);
		toxid_bin_to_hex(public_key, tox_id_hex);
		tox_id_hex[TOX_PUBLIC_KEY_SIZE * 2] = '\0'; // fix to correct size of public key
		result = (*env)->NewStringUTF(env, tox_id_hex); // C style string to Java String
	}

	return result;
}

JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1by_1public_1key(JNIEnv* env, jobject thiz, jobject public_key_str)
{
	unsigned char public_key_bin[TOX_PUBLIC_KEY_SIZE];
	char *public_key_str2 = NULL;
	const char *s = NULL;

	s =  (*env)->GetStringUTFChars(env, public_key_str, NULL);
	public_key_str2 = strdup(s);
	(*env)->ReleaseStringUTFChars(env, public_key_str, s);

	toxid_hex_to_bin(public_key_bin, public_key_str2);

	TOX_ERR_FRIEND_BY_PUBLIC_KEY error;
	uint32_t friendnum = tox_friend_by_public_key(tox_global, (uint8_t *)public_key_bin, &error);

	if (public_key_str2)
	{
		free(public_key_str2);
	}

	if (error != TOX_ERR_FRIEND_BY_PUBLIC_KEY_OK)
	{
		return (jlong)(unsigned long long)-1;
	}
	else
	{
		return (jlong)(unsigned long long)friendnum;
	}
}

JNIEXPORT jlongArray JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1get_1friend_1list(JNIEnv* env, jobject thiz)
{
	size_t numfriends = tox_self_get_friend_list_size(tox_global);
	size_t memsize = (numfriends * sizeof(uint32_t));
	uint32_t *friend_list = malloc(memsize);
	uint32_t *friend_list_iter = friend_list;
	jlongArray result;

	tox_self_get_friend_list(tox_global, friend_list);

	result = (*env)->NewLongArray(env, numfriends);

	if (result == NULL)
	{
		// TODO this would be bad!!
	}

	jlong buffer[numfriends];
	int i = 0;
	for (i=0;i<numfriends;i++)
	{
		buffer[i] = (long)friend_list_iter[i];
	}

	(*env)->SetLongArrayRegion(env, result, 0, numfriends, buffer);

	if (friend_list)
	{
		free(friend_list);
	}

	return result;
}


void Java_com_zoffcc_applications_trifa_MainActivity_tox_1kill__real(JNIEnv* env, jobject thiz)
{
	dbg(9, "tox_kill ... START");
	toxav_iterate_thread_stop = 1;
	pthread_join(tid[0], NULL); // wait for toxav iterate thread to end

	toxav_video_thread_stop = 1;
	pthread_join(tid[1], NULL); // wait for toxav video thread to end

	toxav_kill(tox_av_global);
	tox_kill(tox_global);
	tox_av_global = NULL;
	tox_global = NULL;
	dbg(9, "tox_kill ... READY");
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1kill(JNIEnv* env, jobject thiz)
{
	COFFEE_TRY_JNI(env, Java_com_zoffcc_applications_trifa_MainActivity_tox_1kill__real(env, thiz));
}


void Java_com_zoffcc_applications_trifa_MainActivity_exit__real(JNIEnv* env, jobject thiz)
{
	dbg(9, "Exit Program");
	exit(0);
}


JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_exit(JNIEnv* env, jobject thiz)
{
	COFFEE_TRY_JNI(env, Java_com_zoffcc_applications_trifa_MainActivity_exit__real(env, thiz));
}




JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1iteration_1interval(JNIEnv* env, jobject thiz)
{
	long long l = (long long)tox_iteration_interval(tox_global);
	dbg(9, "tox_iteration_interval=%lld", (long long)l);
	return (jlong)(unsigned long long)l;
}


JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1max_1message_1length(JNIEnv* env, jobject thiz)
{
	long long l = (long long)tox_max_message_length();
	return (jlong)(unsigned long long)l;
}


JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1file_1id_1length(JNIEnv* env, jobject thiz)
{
	long long l = (long long)tox_file_id_length();
	return (jlong)(unsigned long long)l;
}

JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1max_1filename_1length(JNIEnv* env, jobject thiz)
{
	long long l = (long long)tox_max_filename_length();
	return (jlong)(unsigned long long)l;
}

JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1version_1major(JNIEnv* env, jobject thiz)
{
	long long l = (long long)tox_version_major();
	return (jlong)(unsigned long long)l;
}

JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1version_1minor(JNIEnv* env, jobject thiz)
{
	long long l = (long long)tox_version_minor();
	return (jlong)(unsigned long long)l;
}

JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1version_1patch(JNIEnv* env, jobject thiz)
{
	long long l = (long long)tox_version_patch();
	return (jlong)(unsigned long long)l;
}

JNIEXPORT jstring JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_jnictoxcore_1version(JNIEnv* env, jobject thiz)
{
	return (*env)->NewStringUTF(env, global_version_string);
}


JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1send_1message(JNIEnv* env, jobject thiz, jlong friend_number, jint type, jobject message)
{
	const char *message_str = NULL;
	message_str = (*env)->GetStringUTFChars(env, message, NULL);

	TOX_ERR_FRIEND_SEND_MESSAGE error;
	uint32_t res = tox_friend_send_message(tox_global, (uint32_t)friend_number, (int)type, (uint8_t *)message_str, (size_t)strlen(message_str), &error);

	(*env)->ReleaseStringUTFChars(env, message, message_str);

	if (error != 0)
	{
		if (error == TOX_ERR_FRIEND_SEND_MESSAGE_NULL)
		{
			dbg(9, "tox_friend_send_message:ERROR:TOX_ERR_FRIEND_SEND_MESSAGE_NULL");
			return (jlong)-1;
		}
		else if (error == TOX_ERR_FRIEND_SEND_MESSAGE_FRIEND_NOT_FOUND)
		{
			dbg(9, "tox_friend_send_message:ERROR:TOX_ERR_FRIEND_SEND_MESSAGE_FRIEND_NOT_FOUND");
			return (jlong)-2;
		}
		else if (error == TOX_ERR_FRIEND_SEND_MESSAGE_FRIEND_NOT_CONNECTED)
		{
			dbg(9, "tox_friend_send_message:ERROR:TOX_ERR_FRIEND_SEND_MESSAGE_FRIEND_NOT_CONNECTED");
			return (jlong)-3;
		}
		else if (error == TOX_ERR_FRIEND_SEND_MESSAGE_SENDQ)
		{
			dbg(9, "tox_friend_send_message:ERROR:TOX_ERR_FRIEND_SEND_MESSAGE_SENDQ");
			return (jlong)-4;
		}
		else if (error == TOX_ERR_FRIEND_SEND_MESSAGE_TOO_LONG)
		{
			dbg(9, "tox_friend_send_message:ERROR:TOX_ERR_FRIEND_SEND_MESSAGE_TOO_LONG");
			return (jlong)-5;
		}
		else if (error == TOX_ERR_FRIEND_SEND_MESSAGE_EMPTY)
		{
			dbg(9, "tox_friend_send_message:ERROR:TOX_ERR_FRIEND_SEND_MESSAGE_EMPTY");
			return (jlong)-6;
		}
		else
		{
			dbg(9, "tox_friend_send_message:ERROR:%d", (int)error);
			return (jlong)-99;
		}
	}
	else
	{
		dbg(9, "tox_friend_send_message");
		return (jlong)(unsigned long long)res;
	}
}

JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1add(JNIEnv* env, jobject thiz, jobject toxid_str, jobject message)
{
	unsigned char public_key_bin[TOX_ADDRESS_SIZE];
	char *public_key_str2 = NULL;
	const char *s = NULL;
	const char *message_str = NULL;

	s =  (*env)->GetStringUTFChars(env, toxid_str, NULL);
	dbg(9, "add friend:s=%p", s);
	public_key_str2 = strdup(s);
	dbg(9, "add friend:public_key_str2=%p", public_key_str2);
	dbg(9, "add friend:TOX_PUBLIC_KEY_SIZE len=%d", (int)TOX_ADDRESS_SIZE);
	dbg(9, "add friend:public_key_str2 len=%d", strlen(public_key_str2));

	message_str = (*env)->GetStringUTFChars(env, message, NULL);

	TOX_ERR_FRIEND_ADD error;

	toxid_hex_to_bin(public_key_bin, public_key_str2);

	dbg(9, "add friend:public_key_bin=%p", public_key_bin);
	dbg(9, "add friend:public_key_bin len=%d", strlen(public_key_bin));

	dbg(9, "add friend:message_str=%p", message_str);

    uint32_t friendnum = tox_friend_add(tox_global, (uint8_t *)public_key_bin, (uint8_t *)message_str, (size_t)strlen(message_str), &error);


	if (public_key_str2)
	{
		free(public_key_str2);
	}

	(*env)->ReleaseStringUTFChars(env, message, message_str);
	(*env)->ReleaseStringUTFChars(env, toxid_str, s);

	if (error != 0)
	{
		if (error == TOX_ERR_FRIEND_ADD_ALREADY_SENT)
		{
			dbg(9, "add friend:ERROR:TOX_ERR_FRIEND_ADD_ALREADY_SENT");
			return (jlong)-1;
		}
		else
		{
			dbg(9, "add friend:ERROR:%d", (int)error);
			return (jlong)-2;
		}
	}
	else
	{
		dbg(9, "add friend");
		return (jlong)(unsigned long long)friendnum;
	}
}


JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1add_1norequest(JNIEnv* env, jobject thiz, jobject public_key_str)
{
	unsigned char public_key_bin[TOX_PUBLIC_KEY_SIZE];
	char *public_key_str2 = NULL;
	const char *s = NULL;

	s =  (*env)->GetStringUTFChars(env, public_key_str, NULL);
	public_key_str2 = strdup(s);
	(*env)->ReleaseStringUTFChars(env, public_key_str, s);

	toxid_hex_to_bin(public_key_bin, public_key_str2);
    uint32_t friendnum = tox_friend_add_norequest(tox_global, (uint8_t *)public_key_bin, NULL);

	if (public_key_str2)
	{
		free(public_key_str2);
	}

    dbg(9, "add friend norequest");
	return (jlong)(unsigned long long)friendnum;
}



JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1set_1name(JNIEnv* env, jobject thiz, jobject name)
{
	const char *s = NULL;
	s =  (*env)->GetStringUTFChars(env, name, NULL);
	TOX_ERR_SET_INFO error;
	bool res = tox_self_set_name(tox_global, (uint8_t *)s, (size_t)strlen(s), &error);
	(*env)->ReleaseStringUTFChars(env, name, s);
	return (jint)res;
}

JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1set_1status_1message(JNIEnv* env, jobject thiz, jobject status_message)
{
	const char *s = NULL;
	s =  (*env)->GetStringUTFChars(env, status_message, NULL);
	TOX_ERR_SET_INFO error;
	bool res = tox_self_set_status_message(tox_global, (uint8_t *)s, (size_t)strlen(s), &error);
	(*env)->ReleaseStringUTFChars(env, status_message, s);
	return (jint)res;
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1set_1status(JNIEnv* env, jobject thiz, jint status)
{
	tox_self_set_status(tox_global, (TOX_USER_STATUS)status);
}


JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1set_1typing(JNIEnv* env, jobject thiz, jlong friend_number, jint typing)
{
	TOX_ERR_SET_TYPING error;
	bool res = tox_self_set_typing(tox_global, (uint32_t)friend_number, (bool)typing, &error);
	return (jint)res;
}

JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1get_1connection_1status(JNIEnv* env, jobject thiz, jlong friend_number)
{
	TOX_ERR_FRIEND_QUERY error;
	TOX_CONNECTION res = tox_friend_get_connection_status(tox_global, (uint32_t)friend_number, &error);
	return (jint)res;
}

JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1friend_1delete(JNIEnv* env, jobject thiz, jlong friend_number)
{
	TOX_ERR_FRIEND_DELETE error;
	bool res = tox_friend_delete(tox_global, (uint32_t)friend_number, &error);
	return (jint)res;
}

JNIEXPORT jstring JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1get_1name(JNIEnv* env, jobject thiz)
{
	size_t length = tox_self_get_name_size(tox_global);
    char name[length + 1];
	CLEAR(name);
	// dbg(9, "name len=%d", (int)length);
	tox_self_get_name(tox_global, name);
	// dbg(9, "name=%s", (char *)name);
	return (*env)->NewStringUTF(env, (uint8_t *)name);
}

JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1get_1name_1size(JNIEnv* env, jobject thiz)
{
	long long l = (long long)tox_self_get_name_size(tox_global);
	return (jlong)(unsigned long long)l;
}

JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1get_1status_1message_1size(JNIEnv* env, jobject thiz)
{
	long long l = (long long)tox_self_get_status_message_size(tox_global);
	return (jlong)(unsigned long long)l;
}

JNIEXPORT jstring JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1self_1get_1status_1message(JNIEnv* env, jobject thiz)
{
	size_t length = tox_self_get_status_message_size(tox_global);
    char message[length + 1];
	CLEAR(message);
	tox_self_get_status_message(tox_global, message);
	return (*env)->NewStringUTF(env, (uint8_t *)message);
}

JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1file_1control(JNIEnv* env, jobject thiz, jlong friend_number, jlong file_number, jint control)
{
	TOX_ERR_FILE_CONTROL error;
	bool res = tox_file_control(tox_global, (uint32_t)friend_number, (uint32_t)file_number, (TOX_FILE_CONTROL)control, &error);

	if (res == true)
	{
		return (jint)0;
	}
	else
	{
		return (jint)-1;
	}
}

JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1hash(JNIEnv* env, jobject thiz, jobject hash_buffer, jobject data_buffer, jlong data_length)
{
	uint8_t *hash_buffer_c = NULL;
	long capacity_hash = 0;
	uint8_t *data_buffer_c = NULL;
	long capacity_data = 0;

    hash_buffer_c = (uint8_t*)(*env)->GetDirectBufferAddress(env, hash_buffer);
    capacity_hash = (*env)->GetDirectBufferCapacity(env, hash_buffer);

	if (capacity_hash < TOX_HASH_LENGTH)
	{
		return -2;
	}

	if (data_buffer != NULL)
	{
		data_buffer_c = (uint8_t*)(*env)->GetDirectBufferAddress(env, data_buffer);
		capacity_data = (*env)->GetDirectBufferCapacity(env, data_buffer);
	}

	if (capacity_data < data_length)
	{
		return -3;
	}

	bool res = tox_hash(hash_buffer_c, data_buffer_c, (size_t)data_length);

	if (res != true)
	{
		return -1;
	}
	else
	{
		return 0;
	}
}


JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1file_1seek(JNIEnv* env, jobject thiz, jlong friend_number, jlong file_number, jlong position)
{

	TOX_ERR_FILE_SEEK error;
	bool res = tox_file_seek(tox_global, (uint32_t)friend_number, (uint32_t)file_number, (uint64_t)position, &error);

	if (res != true)
	{
		if (error == TOX_ERR_FILE_GET_NULL)
		{
			dbg(9, "tox_file_seek:ERROR:TOX_ERR_FILE_GET_NULL");
			return (jint)-1;
		}
		else if (error == TOX_ERR_FILE_GET_FRIEND_NOT_FOUND)
		{
			dbg(9, "tox_file_seek:ERROR:TOX_ERR_FILE_GET_FRIEND_NOT_FOUND");
			return (jint)-2;
		}
		else if (error == TOX_ERR_FILE_GET_NOT_FOUND)
		{
			dbg(9, "tox_file_seek:ERROR:TOX_ERR_FILE_GET_NOT_FOUND");
			return (jint)-3;
		}
		else
		{
			dbg(9, "tox_file_seek:ERROR:%d", (int)error);
			return (jint)-99;
		}
	}
	else
	{
		dbg(9, "tox_file_seek");
		return (jint)res;
	}
}


JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1file_1get_1file_1id(JNIEnv* env, jobject thiz, jlong friend_number, jlong file_number, jobject file_id_buffer)
{
	uint8_t *file_id_buffer_c = NULL;
	long capacity = 0;

	if (file_id_buffer == NULL)
	{
		return -3;
	}

	file_id_buffer_c = (uint8_t*)(*env)->GetDirectBufferAddress(env, file_id_buffer);
	capacity = (*env)->GetDirectBufferCapacity(env, file_id_buffer);

	if (capacity < TOX_FILE_ID_LENGTH)
	{
		return -2;
	}

	TOX_ERR_FILE_GET error;
	bool res = tox_file_get_file_id(tox_global, (uint32_t)friend_number, (uint32_t)file_number, file_id_buffer_c, &error);

	if (res != true)
	{
		return -1;
	}
	else
	{
		return 0;
	}
}

JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1file_1send(JNIEnv* env, jobject thiz, jlong friend_number, jlong kind, jlong file_size, jobject file_id_buffer, jstring file_name, jlong filename_length)
{
	uint8_t *file_id_buffer_c = NULL;
	long capacity = 0;

	if (file_id_buffer == NULL)
	{
		return -21;
	}

	file_id_buffer_c = (uint8_t*)(*env)->GetDirectBufferAddress(env, file_id_buffer);
	capacity = (*env)->GetDirectBufferCapacity(env, file_id_buffer);

	if (capacity < TOX_FILE_ID_LENGTH)
	{
		return -22;
	}

	const char *filename_str = NULL;
	filename_str =  (*env)->GetStringUTFChars(env, file_name, NULL);

	TOX_ERR_FILE_SEND error;
	uint32_t res = tox_file_send(tox_global, (uint32_t)friend_number, (uint32_t)kind, (uint64_t)file_size, file_id_buffer_c,
                       (uint8_t *)filename_str, (size_t)filename_length, &error);

	(*env)->ReleaseStringUTFChars(env, file_name, filename_str);

	if (error == TOX_ERR_FILE_SEND_NULL)
	{
		dbg(0, "tox_file_send:TOX_ERR_FILE_SEND_NULL");
		return (jlong)-1;
	}
	else if (error == TOX_ERR_FILE_SEND_FRIEND_NOT_FOUND)
	{
		dbg(0, "tox_file_send:TOX_ERR_FILE_SEND_FRIEND_NOT_FOUND");
		return (jlong)-2;
	}
	else if (error == TOX_ERR_FILE_SEND_FRIEND_NOT_CONNECTED)
	{
		dbg(0, "tox_file_send:TOX_ERR_FILE_SEND_FRIEND_NOT_CONNECTED");
		return (jlong)-3;
	}
	else if (error == TOX_ERR_FILE_SEND_NAME_TOO_LONG)
	{
		dbg(0, "tox_file_send:TOX_ERR_FILE_SEND_NAME_TOO_LONG");
		return (jlong)-4;
	}
	else if (error == TOX_ERR_FILE_SEND_TOO_MANY)
	{
		dbg(0, "tox_file_send:TOX_ERR_FILE_SEND_TOO_MANY");
		return (jlong)-5;
	}
	else
	{
		return (jlong)res;
	}
}


JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_tox_1file_1send_1chunk(JNIEnv* env, jobject thiz, jlong friend_number, jlong file_number, jlong position, jobject data_buffer, jlong data_length)
{
	uint8_t *data_buffer_c = NULL;
	long capacity = 0;

	if (data_buffer == NULL)
	{
		return -21;
	}

	data_buffer_c = (uint8_t*)(*env)->GetDirectBufferAddress(env, data_buffer);
	capacity = (*env)->GetDirectBufferCapacity(env, data_buffer);

	TOX_ERR_FILE_SEND_CHUNK error;
	bool res = tox_file_send_chunk(tox_global, (uint32_t)friend_number, (uint32_t)file_number, (uint64_t)position, data_buffer_c,
                       (size_t)data_length, &error);

	if (res != true)
	{
		if (error == TOX_ERR_FILE_SEND_CHUNK_NULL)
		{
			dbg(0, "tox_file_send:TOX_ERR_FILE_SEND_CHUNK_NULL");
			return (jint)-1;
		}
		else if (error == TOX_ERR_FILE_SEND_CHUNK_FRIEND_NOT_FOUND)
		{
			dbg(0, "tox_file_send:TOX_ERR_FILE_SEND_CHUNK_FRIEND_NOT_FOUND");
			return (jint)-2;
		}
		else if (error == TOX_ERR_FILE_SEND_CHUNK_FRIEND_NOT_CONNECTED)
		{
			dbg(0, "tox_file_send:TOX_ERR_FILE_SEND_CHUNK_FRIEND_NOT_CONNECTED");
			return (jint)-3;
		}
		else if (error == TOX_ERR_FILE_SEND_CHUNK_NOT_FOUND)
		{
			dbg(0, "tox_file_send:TOX_ERR_FILE_SEND_CHUNK_NOT_FOUND");
			return (jint)-4;
		}
		else if (error == TOX_ERR_FILE_SEND_CHUNK_NOT_TRANSFERRING)
		{
			dbg(0, "tox_file_send:TOX_ERR_FILE_SEND_CHUNK_NOT_TRANSFERRING");
			return (jint)-5;
		}
		else if (error == TOX_ERR_FILE_SEND_CHUNK_INVALID_LENGTH)
		{
			dbg(0, "tox_file_send:TOX_ERR_FILE_SEND_CHUNK_INVALID_LENGTH");
			return (jint)-6;
		}
		else if (error == TOX_ERR_FILE_SEND_CHUNK_SENDQ)
		{
			dbg(0, "tox_file_send:TOX_ERR_FILE_SEND_CHUNK_SENDQ");
			return (jint)-7;
		}
		else if (error == TOX_ERR_FILE_SEND_CHUNK_WRONG_POSITION)
		{
			dbg(0, "tox_file_send:TOX_ERR_FILE_SEND_CHUNK_WRONG_POSITION");
			return (jint)-8;
		}
		else
		{
			return (jint)-99;
		}
	}

	return (jint)0;
}

// -----------------------
// TODO
// -----------------------
/*
void tox_self_get_public_key(const Tox *tox, uint8_t *public_key);
void tox_self_get_secret_key(const Tox *tox, uint8_t *secret_key);
uint32_t tox_self_get_nospam(const Tox *tox);
bool tox_friend_exists(const Tox *tox, uint32_t friend_number);
uint64_t tox_friend_get_last_online(const Tox *tox, uint32_t friend_number, TOX_ERR_FRIEND_GET_LAST_ONLINE *error);
TOX_USER_STATUS tox_friend_get_status(const Tox *tox, uint32_t friend_number, TOX_ERR_FRIEND_QUERY *error);

void tox_self_set_nospam(Tox *tox, uint32_t nospam);
*/
// -----------------------
// TODO
// -----------------------


// ------------------- AV -------------------
// ------------------- AV -------------------
// ------------------- AV -------------------
JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_toxav_1answer(JNIEnv* env, jobject thiz, jlong friend_number, jlong audio_bit_rate, jlong video_bit_rate)
{
	TOXAV_ERR_ANSWER error;
	bool res = toxav_answer(tox_av_global, (uint32_t)friend_number, (uint32_t)audio_bit_rate, (uint32_t)video_bit_rate, &error);
	return (jint)res;
}


JNIEXPORT jlong JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_toxav_1iteration_1interval(JNIEnv* env, jobject thiz)
{
	long long l = (long long)toxav_iteration_interval(tox_av_global);
	dbg(9, "toxav_iteration_interval=%lld", (long long)l);
	return (jlong)(unsigned long long)l;
}


JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_toxav_1call(JNIEnv* env, jobject thiz, jlong friend_number, jlong audio_bit_rate, jlong video_bit_rate)
{
	TOXAV_ERR_CALL error;
	bool res = toxav_call(tox_av_global, (uint32_t)friend_number, (uint32_t)audio_bit_rate, (uint32_t)video_bit_rate, &error);
	return (jint)res;
}


JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_toxav_1bit_1rate_1set(JNIEnv* env, jobject thiz, jlong friend_number, jlong audio_bit_rate, jlong video_bit_rate)
{
	TOXAV_ERR_BIT_RATE_SET error;
	bool res = toxav_bit_rate_set(tox_av_global, (uint32_t)friend_number, (uint32_t)audio_bit_rate, (uint32_t)video_bit_rate, &error);
	return (jint)res;
}


JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_toxav_1call_1control(JNIEnv* env, jobject thiz, jlong friend_number, jint control)
{
	TOXAV_ERR_CALL_CONTROL error;
	bool res = toxav_call_control(tox_av_global, (uint32_t)friend_number, (TOXAV_CALL_CONTROL)control, &error);
	return (jint)res;
}


// reverse the order of the U and V planes ---------------
JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_toxav_1video_1send_1frame_1uv_1reversed(JNIEnv* env, jobject thiz, jlong friend_number, jint frame_width_px, jint frame_height_px)
{
	TOXAV_ERR_SEND_FRAME error;

	video_buffer_2_y_size = (int)(frame_width_px * frame_height_px);
	video_buffer_2_u_size = (int)(video_buffer_2_y_size / 4);
	video_buffer_2_v_size = (int)(video_buffer_2_y_size / 4);
	// reversed -----------
	// reversed -----------
	video_buffer_2_v = (uint8_t*)(video_buffer_2 + video_buffer_2_y_size);
	video_buffer_2_u = (uint8_t*)(video_buffer_2 + video_buffer_2_y_size + video_buffer_2_u_size);
	// reversed -----------
	// reversed -----------
	bool res = toxav_video_send_frame(tox_av_global, (uint32_t)friend_number, (uint16_t)frame_width_px, (uint16_t)frame_height_px,
		(uint8_t*)video_buffer_2, video_buffer_2_u, video_buffer_2_v, &error);

	return (jint)error;
}
// reverse the order of the U and V planes ---------------


/**
 * Send a video frame to a friend.
 *
 * Y - plane should be of size: height * width
 * U - plane should be of size: (height/2) * (width/2)
 * V - plane should be of size: (height/2) * (width/2)
 *
 * @param friend_number The friend number of the friend to which to send a video
 * frame.
 * @param width Width of the frame in pixels.
 * @param height Height of the frame in pixels.
 * @param y Y (Luminance) plane data.
 * @param u U (Chroma) plane data.
 * @param v V (Chroma) plane data.
 */
JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_toxav_1video_1send_1frame(JNIEnv* env, jobject thiz, jlong friend_number, jint frame_width_px, jint frame_height_px)
{
	TOXAV_ERR_SEND_FRAME error;

	video_buffer_2_y_size = (int)(frame_width_px * frame_height_px);
	video_buffer_2_u_size = (int)(video_buffer_2_y_size / 4);
	video_buffer_2_v_size = (int)(video_buffer_2_y_size / 4);
	video_buffer_2_u = (uint8_t*)(video_buffer_2 + video_buffer_2_y_size);
	video_buffer_2_v = (uint8_t*)(video_buffer_2 + video_buffer_2_y_size + video_buffer_2_u_size);

	// dbg(9, "toxav_video_send_frame:fn=%d,video_buffer_2=%p,w=%d,h=%d", (int)friend_number, video_buffer_2, (int)frame_width_px, (int)frame_height_px);

	bool res = toxav_video_send_frame(tox_av_global, (uint32_t)friend_number, (uint16_t)frame_width_px, (uint16_t)frame_height_px,
		(uint8_t*)video_buffer_2, video_buffer_2_u, video_buffer_2_v, &error);

	// dbg(9, "toxav_video_send_frame:res=%d,error=%d", (int)res, (int)error);

	return (jint)error;
}


/**
 * Send an audio frame to a friend.
 *
 * The expected format of the PCM data is: [s1c1][s1c2][...][s2c1][s2c2][...]...
 * Meaning: sample 1 for channel 1, sample 1 for channel 2, ...
 * For mono audio, this has no meaning, every sample is subsequent. For stereo,
 * this means the expected format is LRLRLR... with samples for left and right
 * alternating.
 *
 * @param friend_number The friend number of the friend to which to send an
 * audio frame.
 * @param pcm An array of audio samples. The size of this array must be
 * sample_count * channels.
 * @param sample_count Number of samples in this frame. Valid numbers here are
 * ((sample rate) * (audio length) / 1000), where audio length can be
 * 2.5, 5, 10, 20, 40 or 60 millseconds.
 * @param channels Number of audio channels. Supported values are 1 and 2.
 * @param sampling_rate Audio sampling rate used in this frame. Valid sampling
 * rates are 8000, 12000, 16000, 24000, or 48000.
 */
JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_toxav_1audio_1send_1frame(JNIEnv* env, jobject thiz, jlong friend_number, jlong sample_count, jint channels, jlong sampling_rate)
{
	TOXAV_ERR_SEND_FRAME error;

	if (audio_buffer_pcm_1)
	{
		uint16_t *pcm = (uint16_t *)audio_buffer_pcm_1;

		bool res = toxav_audio_send_frame(tox_av_global, (uint32_t)friend_number, pcm, (size_t)sample_count,
			(uint8_t)channels, (uint32_t)sampling_rate, &error);
	}

	return (jint)error;
}
// ------------------- AV -------------------
// ------------------- AV -------------------
// ------------------- AV -------------------



// --------------- _toxfuncs_ ---------------
// --------------- _toxfuncs_ ---------------
// --------------- _toxfuncs_ ---------------




// JNIEXPORT void JNICALL
// Java_com_zoffcc_applications_trifa_MainActivity_toxloop(JNIEnv* env, jobject thiz)
// {
//	_main_();
// }

// ------------------------------------------------------------------------------------------------
// taken from:
// https://github.com/googlesamples/android-ndk/blob/master/hello-jni/app/src/main/cpp/hello-jni.c
// ------------------------------------------------------------------------------------------------

JNIEXPORT jstring JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_getNativeLibAPI(JNIEnv* env, jobject thiz)
{
#if defined(__arm__)
    #if defined(__ARM_ARCH_7A__)
    #if defined(__ARM_NEON__)
      #if defined(__ARM_PCS_VFP)
        #define ABI "armeabi-v7a/NEON (hard-float)"
      #else
        #define ABI "armeabi-v7a/NEON"
      #endif
    #else
      #if defined(__ARM_PCS_VFP)
        #define ABI "armeabi-v7a (hard-float)"
      #else
        #define ABI "armeabi-v7a"
      #endif
    #endif
  #else
   #define ABI "armeabi"
  #endif
#elif defined(__i386__)
#define ABI "x86"
#elif defined(__x86_64__)
#define ABI "x86_64"
#elif defined(__mips64)  /* mips64el-* toolchain defines __mips__ too */
#define ABI "mips64"
#elif defined(__mips__)
#define ABI "mips"
#elif defined(__aarch64__)
#define ABI "arm64-v8a"
#else
#define ABI "unknown"
#endif

    return (*env)->NewStringUTF(env, "Native Code Compiled with ABI:" ABI "");
}


// ------------- JNI -------------
// ------------- JNI -------------
// ------------- JNI -------------


// ----------- produce a Crash to test Crash Detector -----------
// ----------- produce a Crash to test Crash Detector -----------
// ----------- produce a Crash to test Crash Detector -----------
// void Java_com_zoffcc_applications_trifa_MainActivity_AppCrashC__XX_real(JNIEnv* env, jobject thiz) __attribute__((optimize("-O0")));
// void Java_com_zoffcc_applications_trifa_MainActivity_AppCrashC__XX_real(JNIEnv* env, jobject thiz) __attribute__((optimize("O0")));
void Java_com_zoffcc_applications_trifa_MainActivity_AppCrashC__XX_real(JNIEnv* env, jobject thiz)
{
        // int i = 3;
        // i = (1 / 0);

        char* name = NULL;
        name = (char*)0;
        name = "ekrpowekrp";

        int *pi;
        int c;
        pi = NULL;
        c = *pi;

        int *x = NULL;
        int y = *x;
        y = y + 1;

        *(long*)0 = 0xDEADBEEF;
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_trifa_MainActivity_AppCrashC(JNIEnv* env, jobject thiz)
{
        COFFEE_TRY_JNI(env, Java_com_zoffcc_applications_trifa_MainActivity_AppCrashC__XX_real(env, thiz));
}
// ----------- produce a Crash to test Crash Detector -----------
// ----------- produce a Crash to test Crash Detector -----------
// ----------- produce a Crash to test Crash Detector -----------





