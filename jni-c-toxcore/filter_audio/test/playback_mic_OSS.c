#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <math.h>
#include <math.h>
#include <sys/ioctl.h>
#include <fcntl.h>
#include <sys/soundcard.h>
#include "../other/signal_processing_library.h"

#include "../filter_audio.h"


#define DEVICE_NAME "/dev/dsp"



int main(int argc, char *argv[])
{
    unsigned int sample_rate = 16000;  //16khz
    unsigned int format = 16;
    unsigned int channels = 1;
    unsigned int status;
    unsigned int audio_fd; //声卡
    unsigned int recoder_fid;

    unsigned int samples_perframe = sample_rate/50;
    _Bool filter = 1;

    FILE *fp_in,*fp_out;
    char *read_mic_file = "read_from_mic.pcm";
    char *result_pcm_file = "result_out_agc.pcm";
    fp_in=fopen(read_mic_file,"wb");
	fp_out=fopen(result_pcm_file,"wb");


    recoder_fid = open(DEVICE_NAME, O_RDONLY,0777);
    if (recoder_fid < 0)
    {
        perror("Cannot open /dev/dsp device");
        return 1;
    }

    status = ioctl(recoder_fid, SOUND_PCM_WRITE_BITS, &format);//设置量化位数
    if(status == -1)
    {
        perror("Cannot set SOUND_PCM_WRITE_BITS ");
        return 1;
    }

    status = ioctl(recoder_fid, SOUND_PCM_WRITE_CHANNELS, &channels);//设置声道数
    if (status == -1)
    {
        perror("Cannot set SOUND_PCM_WRITE_CHANNELS");
        return 1;
    }

    status = ioctl(recoder_fid, SOUND_PCM_WRITE_RATE, &sample_rate);//设置采样率
    if (status == -1)
    {
        perror("Cannot set SOUND_PCM_WRITE_WRITE");
        return 1;
    }


    if ((audio_fd = open(DEVICE_NAME, O_WRONLY,0777)) == -1)
    {
       printf("open error\n");
       return -1;
    }


    if (ioctl(audio_fd, SOUND_PCM_WRITE_BITS, &format) == -1)
    {
       /* fatal error */
       printf("SNDCTL_DSP_SETFMT error\n");
       return -1;
    }

    int param;
    param = ( 0x0002 << 16) + 0x0006; //参数param由两部分组成：低16位为fragment的大小，此处0x0007表示fragment大小为2^7，即128字节；
                                     //高16位为fragment的数量，此处为0x0002，即2个fragement。

    if (ioctl(audio_fd, SNDCTL_DSP_SETFRAGMENT, &param) == -1) {
        printf("SNDCTL_DSP_SETFRAGMENT error\n");
    }


    if (ioctl(audio_fd, SOUND_PCM_WRITE_CHANNELS, &channels) == -1)
    {
       /* Fatal error */
       printf("SOUND_PCM_WRITE_CHANNELS error");
       return -1;
    }

    if (ioctl(audio_fd, SOUND_PCM_WRITE_RATE, &sample_rate)==-1)
    {
       /* Fatal error */
       printf("SOUND_PCM_WRITE_RATE error\n");
       return -1;
    }

    int16_t *SigBuf;
    float   *Sigout;

    Filter_Audio *f_a = new_filter_audio(sample_rate);

    SigBuf = calloc(1, sizeof(int16_t) * samples_perframe);
    Sigout = calloc(1, sizeof(float) * samples_perframe);

    int delay_ms = 0;
    sscanf(argv[1],"%d", &delay_ms);
    set_echo_delay_ms(f_a, delay_ms);



    while (1)
    {


        read(recoder_fid, SigBuf, sizeof(SigBuf[0])*samples_perframe);
        fwrite(SigBuf,sizeof(SigBuf[0]),samples_perframe,fp_in);
        if (filter && filter_audio(f_a, SigBuf, samples_perframe) == -1)
        {
            printf("filter_audio fail\n");
            return 0;
        }


        write(audio_fd,SigBuf,sizeof(int16_t)*samples_perframe);


        fwrite(SigBuf,sizeof(SigBuf[0]),samples_perframe,fp_out);
    }


    return 0;
}

