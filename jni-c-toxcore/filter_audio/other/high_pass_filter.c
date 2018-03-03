
#include "signal_processing_library.h"

const int16_t kFilterCoefficients8kHz[5] =
    {3798, -7596, 3798, 7807, -3733};

const int16_t kFilterCoefficients[5] =
    {4012, -8024, 4012, 8002, -3913};


void init_highpass_filter(FilterState *hpf, uint32_t fs)
{
    if (fs == 8000) {
        hpf->ba = kFilterCoefficients8kHz;
    } else {
        hpf->ba = kFilterCoefficients;
    }
}

int highpass_filter(FilterState* hpf, int16_t* data, int length)
{
  if (!hpf)
      return -1;

  int32_t tmp_int32 = 0;
  int16_t* y = hpf->y;
  int16_t* x = hpf->x;
  const int16_t* ba = hpf->ba;

  int i;
  for (i = 0; i < length; i++) {
    //  y[i] = b[0] * x[i] + b[1] * x[i-1] + b[2] * x[i-2]
    //         + -a[1] * y[i-1] + -a[2] * y[i-2];

    tmp_int32 =
        WEBRTC_SPL_MUL_16_16(y[1], ba[3]); // -a[1] * y[i-1] (low part)
    tmp_int32 +=
        WEBRTC_SPL_MUL_16_16(y[3], ba[4]); // -a[2] * y[i-2] (low part)
    tmp_int32 = (tmp_int32 >> 15);
    tmp_int32 +=
        WEBRTC_SPL_MUL_16_16(y[0], ba[3]); // -a[1] * y[i-1] (high part)
    tmp_int32 +=
        WEBRTC_SPL_MUL_16_16(y[2], ba[4]); // -a[2] * y[i-2] (high part)
    tmp_int32 = (tmp_int32 << 1);

    tmp_int32 += WEBRTC_SPL_MUL_16_16(data[i], ba[0]); // b[0]*x[0]
    tmp_int32 += WEBRTC_SPL_MUL_16_16(x[0], ba[1]);    // b[1]*x[i-1]
    tmp_int32 += WEBRTC_SPL_MUL_16_16(x[1], ba[2]);    // b[2]*x[i-2]

    // Update state (input part)
    x[1] = x[0];
    x[0] = data[i];

    // Update state (filtered part)
    y[2] = y[0];
    y[3] = y[1];
    y[0] = tmp_int32 >> 13;
    y[1] = (tmp_int32 - (((int32_t)y[0]) << 13)) << 2;

    // Rounding in Q12, i.e. add 2^11
    tmp_int32 += 2048;

    // Saturate (to 2^27) so that the HP filtered signal does not overflow
    tmp_int32 = WEBRTC_SPL_SAT((int32_t)(134217727), tmp_int32, (int32_t)(-134217728));

    // Convert back to Q0 and use rounding.
    data[i] = (int16_t)(tmp_int32 >> 12);
  }

  return 0;
}
