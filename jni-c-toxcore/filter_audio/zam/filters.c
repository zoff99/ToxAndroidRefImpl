#include "filters.h"

double sanitize_denormal(double v)
{
    if(!isnormal(v))
        return 0.f;
    return v;
}

void init_highpass_filter_zam(FilterStateZam *hpf, float fc, float fs)
{
    double w0;
    double q;
    double alpha;
    int i;

    q = 1.f / sqrt(2.f);
    w0 = 2.f * M_PI * fc / fs;
    alpha = sin(w0) / (2.f * q);

    hpf->b[0] = (1.f + cos(w0)) / 2.f;
    hpf->b[1] = -(1.f + cos(w0));
    hpf->b[2] = (1.f + cos(w0)) / 2.f;
    hpf->a[0] = 1.f + alpha;
    hpf->a[1] = -2.f * cos(w0);
    hpf->a[2] = 1.f - alpha;

    for (i = 0; i < 3; i++) {
        hpf->x[i] = 0.f;
        hpf->y[i] = 0.f;
    }
}

void init_lowpass_filter_zam(FilterStateZam *lpf, float fc, float fs)
{
    double w0;
    double q;
    double alpha;
    int i;

    q = 1.f / sqrt(2.f);
    w0 = 2.f * M_PI * fc / fs;
    alpha = sin(w0) / (2.f * q);

    lpf->b[0] = (1.f - cos(w0)) / 2.f;
    lpf->b[1] = 1.f - cos(w0);
    lpf->b[2] = (1.f - cos(w0)) / 2.f;
    lpf->a[0] = 1.f + alpha;
    lpf->a[1] = -2.f * cos(w0);
    lpf->a[2] = 1.f - alpha;

    for (i = 0; i < 3; i++) {
        lpf->x[i] = 0.f;
        lpf->y[i] = 0.f;
    }
}

int run_filter_zam(FilterStateZam* fil, float* data, int length)
{
    int i;
    double a0;
    double out;
    double in;

    if (!fil)
        return -1;

    a0 = sanitize_denormal(fil->a[0]);

    for (i = 0; i < length; i++) {
        //  y[i] = b[0]/a[0] * x[i] + b[1]/a[0] * x[i-1] + b[2]/a[0] * x[i-2]
        //      + -a[1]/a[0] * y[i-1] + -a[2]/a[0] * y[i-2];
        in = sanitize_denormal(data[i]);
        out = fil->b[0]/a0 * in
                + fil->b[1]/a0 * fil->x[1]
                + fil->b[2]/a0 * fil->x[2]
                - fil->a[1]/a0 * fil->y[1]
                - fil->a[2]/a0 * fil->y[2] + 1e-20f;
        out = sanitize_denormal(out);
        fil->x[2] = sanitize_denormal(fil->x[1]);
        fil->y[2] = sanitize_denormal(fil->y[1]);
        fil->x[1] = in;
        fil->y[1] = out;

        data[i] = (float) out;
    }
    return 0;
}

int run_saturator_zam(float* data, int length)
{
    int i;
    double x;

    for (i = 0; i < length; i++) {
        x = data[i];
        data[i] = 2.f * x * (1.f - fabsf(x) * 0.5f);
    }
    return 0;
}
