#include <inttypes.h>
#include <math.h>

typedef struct {
    double x[3];
    double y[3];
    double a[3];
    double b[3];
} FilterStateZam;

void init_highpass_filter_zam(FilterStateZam *hpf, float fc, float fs);
void init_lowpass_filter_zam(FilterStateZam *lpf, float fc, float fs);
int run_filter_zam(FilterStateZam* fil, float* data, int length);
int run_saturator_zam(float *data, int length);
double sanitize_denormal(double v);
