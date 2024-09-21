package me.jagar.chatvoiceplayerlibrary;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;

import com.zoffcc.applications.trifa.R;

import java.io.File;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class PlayerVisualizerSeekbar extends androidx.appcompat.widget.AppCompatSeekBar {

    /**
     * constant value for Height of the bar
     */
    public static final int VISUALIZER_HEIGHT = 28;

    /**
     * bytes array converted from file.
     */
    private byte[] bytes;

    /**
     * Percentage of audio sample scale
     * Should updated dynamically while audioPlayer is played
     */
    private float denseness;

    /**
     * Canvas painting for sample scale, filling played part of audio sample
     */
    private final Paint playedStatePainting = new Paint();
    /**
     * Canvas painting for sample scale, filling not played part of audio sample
     */
    private final Paint notPlayedStatePainting = new Paint();

    private int width;
    private int height;

    public PlayerVisualizerSeekbar(Context context) {
        super(context);
        init();
    }

    public PlayerVisualizerSeekbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setColors(int playedColor, int notPlayedColor){
        playedStatePainting.setColor(notPlayedColor);
        notPlayedStatePainting.setColor(playedColor);

    }
    private void init() {
        bytes = null;

        playedStatePainting.setStrokeWidth(1f);
        playedStatePainting.setAntiAlias(true);
        playedStatePainting.setColor(ContextCompat.getColor(getContext(), R.color.gray));
        notPlayedStatePainting.setStrokeWidth(1f);
        notPlayedStatePainting.setAntiAlias(true);
        notPlayedStatePainting.setColor(ContextCompat.getColor(getContext(), R.color.md_pink_200));
    }

    /**
     * update and redraw Visualizer view
     */
    public void updateVisualizer(File file) {

        FileUtils.updateVisualizer(getContext(), file, this);
    }

    public void vupdateVisualizer(info.guardianproject.iocipher.File vfile) {

        FileUtils.vupdateVisualizer(getContext(), vfile, this);
    }

    public void setBytes(byte[] bytes){
        this.bytes = bytes;
    }

    /**
     * Update player percent. 0 - file not played, 1 - full played
     *
     * @param percent
     */
    public void updatePlayerPercent(float percent) {
        denseness = (int) Math.ceil(width * percent);
        if (denseness < 0) {
            denseness = 0;
        } else if (denseness > width) {
            denseness = width;
        }
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (width == 0) {
            return;
        }

        if (bytes == null) {
            float left = 0;
            float middle_vertical_top = (height / 2) - 1;
            float middle_vertical_bottom = (height / 2) + 1;
            float right = width;
            canvas.drawRect(left, middle_vertical_top, right, middle_vertical_bottom, playedStatePainting);
            canvas.drawRect(left, middle_vertical_top, denseness, middle_vertical_bottom, notPlayedStatePainting);
            canvas.drawCircle(denseness, (height / 2), dp(6), notPlayedStatePainting);
            return;
        }

        float totalBarsCount = width / dp(3);
        if (totalBarsCount <= 0.1f) {
            return;
        }
        byte value;
        int samplesCount = (bytes.length * 8 / 5);
        float samplesPerBar = samplesCount / totalBarsCount;
        float barCounter = 0;
        int nextBarNum = 0;

        int y = (height - dp(VISUALIZER_HEIGHT));
        int barNum = 0;
        int lastBarNum;
        int drawBarCount;

        for (int a = 0; a < samplesCount; a++) {
            if (a != nextBarNum) {
                continue;
            }
            drawBarCount = 0;
            lastBarNum = nextBarNum;
            while (lastBarNum == nextBarNum) {
                barCounter += samplesPerBar;
                nextBarNum = (int) barCounter;
                drawBarCount++;
            }

            int bitPointer = a * 5;
            int byteNum = bitPointer / Byte.SIZE;
            int byteBitOffset = bitPointer - byteNum * Byte.SIZE;
            int currentByteCount = Byte.SIZE - byteBitOffset;
            int nextByteRest = 5 - currentByteCount;
            value = (byte) ((bytes[byteNum] >> byteBitOffset) & ((2 << (Math.min(5, currentByteCount) - 1)) - 1));
            if (nextByteRest > 0) {
                value <<= nextByteRest;
                value |= bytes[byteNum + 1] & ((2 << (nextByteRest - 1)) - 1);
            }

            for (int b = 0; b < drawBarCount; b++) {
                int x = barNum * dp(3);
                float left = x;
                float top = y + dp(VISUALIZER_HEIGHT - Math.max(1, VISUALIZER_HEIGHT * value / 31.0f));
                float right = x + dp(2);
                float bottom = y + dp(VISUALIZER_HEIGHT);

                if (x < denseness && x + dp(2) < denseness) {
                    canvas.drawRect(left, top, right, bottom, notPlayedStatePainting);

                } else {
                    canvas.drawRect(left, top, right, bottom, playedStatePainting);
                    if (x < denseness) {
                        canvas.drawRect(left, top, right, bottom, notPlayedStatePainting);
                    }
                }
                barNum++;
            }
        }
    }

    public int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(getContext().getResources().getDisplayMetrics().density * value);
    }
}
