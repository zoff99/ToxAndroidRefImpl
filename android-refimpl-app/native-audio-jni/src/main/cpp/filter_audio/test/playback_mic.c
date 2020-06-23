/**
 *  Licence: 3-clause BSD
 */

/* Playing and recording audio data to and from audio devices */
#include <portaudio.h>
/* Reading and writing .wav files */
#include <sndfile.h>
/* Audio filtering */
#include <filter_audio.h>

#include <stdlib.h>
#include <time.h>
#include <assert.h>
#include <string.h>
#include <stdbool.h>

Filter_Audio *filteraudio;
bool filterenabled = true;

/* Input callback. The data in 'input' buffer contains 'fcount' frames captured from the input device.
 * We will filter that data and write it to our output file. (You can consider input and output files as
 * peer audio feed).
 */
int in_cb(const void *input, void *o, unsigned long fcount, const PaStreamCallbackTimeInfo* c, PaStreamCallbackFlags f, void *user_data)
{
    (void) o;
    (void) c;
    (void) f;
    
    SNDFILE *af_handle = user_data; // af_handle_out
    
    /* We copy data to the mutable buffer */
    int16_t PCM [fcount]; /* WARNING I'm not quire sure how this works with 2 channels but you'd copy the buffer however */
    memcpy (PCM, input, sizeof(PCM));
    
    /* Now we filter it. After the process is completed you'll get buffer filled with echo-less PCM data. */
    if (filterenabled && filter_audio(filteraudio, PCM, fcount) == -1)
        puts("Filtering failed");
    
    /* Write filtered data to the file. (or send it to the peer) */
    sf_write_short(af_handle, PCM, fcount);
    
    return 0;
}
/* Output callback. The 'output' contains space for 'fcount' frames to be played by the output device.
 * We will have to feed filter_audio with that data as it's a referene what sounds should be treated as echo in the input.
 * We read the output data from the input file provided to use by the first argument (You can consider input and
 * output files as peer audio feed).
 */
int out_cb(const void *i, void *output, unsigned long fcount, const PaStreamCallbackTimeInfo* c, PaStreamCallbackFlags f, void *user_data)
{
    (void) i;
    (void) c;
    (void) f;
    
    SNDFILE *af_handle = user_data; // af_handle_in
    
    /* Read PCM from the file */
    int64_t count = sf_read_short(af_handle, output, fcount);

    /* If some frames are read, pass them to filter_audio */
    if (filterenabled && count > 0)
        pass_audio_output(filteraudio, output, count);
    
    return 0;
}

int main (int argc, char** argv)
{
    if (argc < 2) {
        puts("Required input .wav file path");
        return 1;
    }
    
    const char* output_path = "echoes_removed.wav";
    
    if (argc > 2)
        output_path = argv[2];
    
    Pa_Initialize();
    
    /* list audio IO devices */
    for (int i = 0; i < Pa_GetDeviceCount(); i ++)
        puts(Pa_GetDeviceInfo(i)->name);
    
    SNDFILE *af_handle_in, *af_handle_out;
    SF_INFO af_info_in, af_info_out;

    /* Open input audio file */
    af_handle_in = sf_open(argv[1], SFM_READ, &af_info_in);

    if (af_handle_in == NULL) {
        puts("Failed to open input file");
        return 1;
    }

    /* Open output audio file */
    af_info_out = af_info_in;
    af_handle_out = sf_open(output_path, SFM_WRITE, &af_info_out);

    if (af_handle_out == NULL) {
        puts("Failed to open output file");
        return 1;
    }

    /* Prepare filter_audio */
    filteraudio = new_filter_audio(af_info_in.samplerate);
    
    /* Prepare portaudio streams */
    PaStream *adout = NULL;
    PaStream *adin = NULL;
    
    /* Choose devices */
    int in_dev = Pa_GetDefaultInputDevice();
    int out_dev = Pa_GetDefaultOutputDevice();
    
    /* High latency works the best but low latency will also work */
    double inlat = Pa_GetDeviceInfo(in_dev)->defaultHighInputLatency;
    double outlat = Pa_GetDeviceInfo(out_dev)->defaultHighOutputLatency;
    
    PaStreamParameters output;
    output.device = out_dev;
    output.channelCount = af_info_in.channels;
    output.sampleFormat = paInt16;
    output.suggestedLatency = outlat;
    output.hostApiSpecificStreamInfo = NULL;

    PaStreamParameters input;
    input.device = in_dev;
    input.channelCount = af_info_in.channels;
    input.sampleFormat = paInt16;
    input.suggestedLatency = inlat;
    input.hostApiSpecificStreamInfo = NULL;
    
    int frame_duration = 20;
    int frame_size = (af_info_in.samplerate * frame_duration / 1000) * af_info_in.channels;
    
    PaError err = Pa_OpenStream(&adout, NULL, &output, af_info_in.samplerate, frame_size, paNoFlag, out_cb, af_handle_in);
    assert(err == paNoError);

    err = Pa_OpenStream(&adin, &input, NULL, af_info_in.samplerate, frame_size, paNoFlag, in_cb, af_handle_out);
    assert(err == paNoError);
    
    /* It's essential that echo delay is set correctly; it's the most important part of the
     * echo cancellation process. If the delay is not set to the acceptable values the AEC
     * will not be able to recover. Given that it's not that easy to figure out the exact
     * time it takes for a signal to get from Output to the Input, setting it to suggested
     * input device latency + frame duration works really good and gives the filter ability
     * to adjust it internally after some time (usually up to 6-7 seconds in my tests when
     * the error is about 20%).
     */
    set_echo_delay_ms(filteraudio, (inlat * 1000) + frame_duration);
    /*
     */
    
    /* Start the streams */
    err = Pa_StartStream(adout);
    assert(err == paNoError);
    
    err = Pa_StartStream(adin);
    assert(err == paNoError);
    
    /* In case you want to repeat set reps to > 1 */
    int reps = 1;
    for (int i = 0; i < reps; i ++)
    {
        /* Sleep until the whole file is read */
        Pa_Sleep((af_info_in.frames * 1000) / (af_info_in.samplerate + 2));
        sf_seek(af_handle_in, 0, SEEK_SET);
    }
    
    /* Clear everything */
    Pa_StopStream(adout);
    Pa_StopStream(adin);
    kill_filter_audio(filteraudio);
    sf_close(af_handle_in);
    sf_close(af_handle_out);
    Pa_Terminate();
    
    return 0;
}
