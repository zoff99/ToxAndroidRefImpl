package com.zoffcc.applications.trifa;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.zoffcc.applications.trifa.TrifaToxService.battery_sleep_start_ms;
import static com.zoffcc.applications.trifa.TrifaToxService.stats_time_tox_not_iterating_ms;
import static com.zoffcc.applications.trifa.TrifaToxService.tox_startup_timestamp;

/** @noinspection ALL*/
public class NetProfiler extends AppCompatActivity {
    private static final String TAG = "trifa.NetProfiler";

    // UI Elements
    private TextView tvSentBytes, tvSentRate, tvSentPkts;
    private TextView tvSentTcp, tvSentUdp;
    private TextView tvRecvBytes, tvRecvRate, tvRecvPkts;
    private TextView tvRecvTcp, tvRecvUdp;
    private TextView tvUptimeValue;
    private TextView tvSleepValue;
    private TextView tvDeepSleepValue;
    private View viewSentHeat, viewRecvHeat, viewCpuHeat;
    private TextView tvSentHeatRate, tvRecvHeatRate, tvCpuHeatRate;
    private RecyclerView rvPackets;
    private TextView tvWakeupHistory;

    private PacketAdapter adapter;
    private final List<PacketStat> packetStats = new ArrayList<>();

    // Executor for background polling
    private ScheduledExecutorService executor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Previous stats for rate calculation
    private long prevSentBytes = 0;
    private long prevRecvBytes = 0;
    private long prevMidBytes = 0;
    private long prevCpuCycles = 0;
    private final Map<String, Long> prevPacketBytes = new HashMap<>();
    private long prevTimestamp = System.currentTimeMillis();
    private boolean isFirstSample = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_netprofiler);

        // Bind views
        tvSentBytes = findViewById(R.id.tv_sent_bytes);
        tvSentRate = findViewById(R.id.tv_sent_rate);
        tvSentPkts = findViewById(R.id.tv_sent_pkts);
        tvSentTcp = findViewById(R.id.tv_sent_tcp);
        tvSentUdp = findViewById(R.id.tv_sent_udp);

        tvRecvBytes = findViewById(R.id.tv_recv_bytes);
        tvRecvRate = findViewById(R.id.tv_recv_rate);
        tvRecvPkts = findViewById(R.id.tv_recv_pkts);
        tvRecvTcp = findViewById(R.id.tv_recv_tcp);
        tvRecvUdp = findViewById(R.id.tv_recv_udp);

        tvUptimeValue = findViewById(R.id.tv_uptime_value);
        tvSleepValue = findViewById(R.id.tv_sleep_value);
        tvDeepSleepValue = findViewById(R.id.tv_deepsleep_value);

        viewSentHeat = findViewById(R.id.view_sent_heat);
        viewRecvHeat = findViewById(R.id.view_recv_heat);
        viewCpuHeat = findViewById(R.id.view_cpu_heat);

        tvSentHeatRate = findViewById(R.id.tv_sent_heat_rate);
        tvRecvHeatRate = findViewById(R.id.tv_recv_heat_rate);
        tvCpuHeatRate = findViewById(R.id.tv_cpu_heat_rate);

        tvWakeupHistory = findViewById(R.id.tv_wakeup_history);
        tvWakeupHistory.setOnClickListener(v -> showWakeupDetailsDialog());
        try
        {
            if (tvWakeupHistory.getParent() instanceof android.view.View)
            {
                ((android.view.View) tvWakeupHistory.getParent()).setOnClickListener(v -> showWakeupDetailsDialog());
            }
        }
        catch(Exception e)
        {
        }

        tvSleepValue.setOnClickListener(v -> showSleepLogDialog());
        try
        {
            if (tvSleepValue.getParent() instanceof android.view.View) {
                ((android.view.View) tvSleepValue.getParent()).setOnClickListener(v -> showSleepLogDialog());
            }
        }
        catch(Exception e)
        {
        }

        rvPackets = findViewById(R.id.rv_packets);

        // Setup RecyclerView (Auto-fit columns based on screen width, min 120dp per box)
        int spanCount = getResources().getDisplayMetrics().widthPixels / dpToPx(120);
        if (spanCount < 2) spanCount = 2;
        rvPackets.setLayoutManager(new GridLayoutManager(this, spanCount));
        adapter = new PacketAdapter(packetStats);
        rvPackets.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startPolling();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPolling();
    }

    private void startPolling() {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleWithFixedDelay(this::pollNetworkStats, 0, 1000, TimeUnit.MILLISECONDS);
        }
    }

    private void stopPolling() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    private void showSleepLogDialog() {
        // Fetch the dump from HelperGeneric
        String logDump = HelperGeneric.battery_sleep_log_dump();

        // Create a ScrollView and TextView for the dialog content
        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        android.widget.TextView textView = new android.widget.TextView(this);

        textView.setText(logDump);
        textView.setTypeface(android.graphics.Typeface.MONOSPACE);
        textView.setTextSize(12); // Smaller text for better log readability
        textView.setPadding(30, 30, 30, 30);
        textView.setTextIsSelectable(true); // Allow user to long-press and copy text

        scrollView.addView(textView);

        // Build and show the dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Tox Sleep Prevention Log")
                .setView(scrollView)
                .setPositiveButton("Close", null)
                .show();
        /*
                .setNeutralButton("Clear Log", (dialog, which) -> {
            // Reset the ring buffer when user clicks "Clear"
            for (int i = 0; i < HelperGeneric.BATTERY_SLEEP_LOG_MAX_ENTRIES; i++) {
                HelperGeneric.battery_sleep_log_ring[i] = null;
            }
            HelperGeneric.battery_sleep_log_ring_index = 0;
            HelperGeneric.battery_sleep_log_ring_count = 0;
            android.widget.Toast.makeText(this, "Log cleared", android.widget.Toast.LENGTH_SHORT).show();
        })
         */
    }

    private void pollNetworkStats() {
        if (tox_startup_timestamp == -1L)
        {
            return;
        }

        long currentTime = System.currentTimeMillis();
        double deltaTimeSec = (currentTime - prevTimestamp) / 1000.0;

        int typeTcp = ToxVars.TOX_NETPROF_PACKET_TYPE.TOX_NETPROF_PACKET_TYPE_TCP.value;
        int typeUdp = ToxVars.TOX_NETPROF_PACKET_TYPE.TOX_NETPROF_PACKET_TYPE_UDP.value;
        int dirSent = ToxVars.TOX_NETPROF_DIRECTION.TOX_NETPROF_DIRECTION_SENT.value;
        int dirRecv = ToxVars.TOX_NETPROF_DIRECTION.TOX_NETPROF_DIRECTION_RECV.value;

        long tcpSentCount = MainActivity.tox_netprof_get_packet_total_count(typeTcp, dirSent);
        long tcpRecvCount = MainActivity.tox_netprof_get_packet_total_count(typeTcp, dirRecv);
        long udpSentCount = MainActivity.tox_netprof_get_packet_total_count(typeUdp, dirSent);
        long udpRecvCount = MainActivity.tox_netprof_get_packet_total_count(typeUdp, dirRecv);

        long tcpSentBytes = MainActivity.tox_netprof_get_packet_total_bytes(typeTcp, dirSent);
        long tcpRecvBytes = MainActivity.tox_netprof_get_packet_total_bytes(typeTcp, dirRecv);
        long udpSentBytes = MainActivity.tox_netprof_get_packet_total_bytes(typeUdp, dirSent);
        long udpRecvBytes = MainActivity.tox_netprof_get_packet_total_bytes(typeUdp, dirRecv);

        long totalSentCount = tcpSentCount + udpSentCount;
        long totalRecvCount = tcpRecvCount + udpRecvCount;
        long totalSentBytes = tcpSentBytes + udpSentBytes;
        long totalRecvBytes = tcpRecvBytes + udpRecvBytes;

        long[] midStats = MainActivity.tox_group_mid_get_network_stats();
        long midSentBytes = (midStats != null && midStats.length > 0) ? midStats[0] : 0;
        long midRecvBytes = (midStats != null && midStats.length > 1) ? midStats[1] : 0;
        long midTotalBytes = midSentBytes + midRecvBytes;

        long cpuCyclesRaw = MainActivity.tox_get_estimated_cpu_cycles();
        long cpuCycles = Math.max(cpuCyclesRaw, 0);

        long sentBps = calculateRate(totalSentBytes, prevSentBytes, deltaTimeSec);
        long recvBps = calculateRate(totalRecvBytes, prevRecvBytes, deltaTimeSec);
        long midBps = calculateRate(midTotalBytes, prevMidBytes, deltaTimeSec);
        long cpuCyclesPerSec = calculateRate(cpuCycles, prevCpuCycles, deltaTimeSec);

        List<PacketStat> newStats = new ArrayList<>();

        // Add Middleware stat as a special entry at the top
        PacketStat midStat = new PacketStat();
        midStat.id = -1;
        midStat.name = "MIDDLEWARE";
        midStat.transport = "NGC";
        midStat.sentBytes = midSentBytes;
        midStat.recvBytes = midRecvBytes;
        midStat.bytesPerSec = midBps;
        newStats.add(midStat);

        for (ToxVars.TOX_NETPROF_PACKET_ID id : ToxVars.TOX_NETPROF_PACKET_ID.values()) {
            addPacketStat(newStats, id, "TCP", typeTcp, dirSent, dirRecv, deltaTimeSec);
            addPacketStat(newStats, id, "UDP", typeUdp, dirSent, dirRecv, deltaTimeSec);
        }

        // Update previous stats
        prevSentBytes = totalSentBytes;
        prevRecvBytes = totalRecvBytes;
        prevMidBytes = midTotalBytes;
        prevCpuCycles = cpuCycles;
        prevTimestamp = currentTime;
        isFirstSample = false;

        // Post to UI thread
        final long fTotalSentBytes = totalSentBytes;
        final long fTotalRecvBytes = totalRecvBytes;
        final long fTotalSentCount = totalSentCount;
        final long fTotalRecvCount = totalRecvCount;
        final long fSentBps = sentBps;
        final long fRecvBps = recvBps;
        final long fCpuCps = cpuCyclesPerSec;
        final long fTcpSentBytes = tcpSentBytes;
        final long fUdpSentBytes = udpSentBytes;
        final long fTcpRecvBytes = tcpRecvBytes;
        final long fUdpRecvBytes = udpRecvBytes;

        mainHandler.post(() -> {
            updateSummaryCards(fTotalSentBytes, fTotalSentCount, fSentBps, fTotalRecvBytes, fTotalRecvCount, fRecvBps,
                               fTcpSentBytes, fUdpSentBytes, fTcpRecvBytes, fUdpRecvBytes);
            updateHeatBars(fSentBps, fRecvBps, fCpuCps);

            packetStats.clear();
            packetStats.addAll(newStats);
            adapter.notifyDataSetChanged();
        });
    }

    private void addPacketStat(List<PacketStat> list, ToxVars.TOX_NETPROF_PACKET_ID id, String transport, int type, int dirSent, int dirRecv, double deltaTimeSec) {
        long sC = MainActivity.tox_netprof_get_packet_id_count(type, id.value, dirSent);
        long rC = MainActivity.tox_netprof_get_packet_id_count(type, id.value, dirRecv);
        long sB = MainActivity.tox_netprof_get_packet_id_bytes(type, id.value, dirSent);
        long rB = MainActivity.tox_netprof_get_packet_id_bytes(type, id.value, dirRecv);
        long totalBytesNow = sB + rB;

        String key = id.name() + "@" + transport;
        long prevB = prevPacketBytes.containsKey(key) ? prevPacketBytes.get(key) : 0L;
        long bps = calculateRate(totalBytesNow, prevB, deltaTimeSec);
        prevPacketBytes.put(key, totalBytesNow);

        PacketStat stat = new PacketStat();
        stat.id = id.value;
        stat.name = id.name().replace("TOX_NETPROF_PACKET_ID_", "");
        stat.transport = transport;
        stat.sentCount = sC;
        stat.recvCount = rC;
        stat.sentBytes = sB;
        stat.recvBytes = rB;
        stat.bytesPerSec = bps;

        list.add(stat);
    }

    private long calculateRate(long currentBytes, long previousBytes, double deltaTimeSec) {
        if (isFirstSample || deltaTimeSec <= 0.0) return 0;
        long delta = currentBytes - previousBytes;
        if (delta < 0) delta = 0; // Handle counter wrap-around gracefully
        return (long) (delta / deltaTimeSec);
    }

    private void updateSummaryCards(long sentBytes, long sentPkts, long sentBps, long recvBytes, long recvPkts, long recvBps,
                                    long tcpSentBytes, long udpSentBytes, long tcpRecvBytes, long udpRecvBytes) {
        tvSentBytes.setText(formatBytes(sentBytes));
        tvSentRate.setText(formatRate(sentBps));
        tvSentPkts.setText(sentPkts + " pkts");
        tvSentTcp.setText("TCP: " + formatBytes(tcpSentBytes));
        tvSentUdp.setText("UDP: " + formatBytes(udpSentBytes));

        tvRecvBytes.setText(formatBytes(recvBytes));
        tvRecvRate.setText(formatRate(recvBps));
        tvRecvPkts.setText(recvPkts + " pkts");
        tvRecvTcp.setText("TCP: " + formatBytes(tcpRecvBytes));
        tvRecvUdp.setText("UDP: " + formatBytes(udpRecvBytes));

        long uptimeMillis = System.currentTimeMillis() - tox_startup_timestamp;
        tvUptimeValue.setText(formatUptime(uptimeMillis));

        long currently_sleeping_ms = 0;
        if (battery_sleep_start_ms > 0)
        {
            currently_sleeping_ms = System.currentTimeMillis() - battery_sleep_start_ms;
        }
        long totalSleepMs = stats_time_tox_not_iterating_ms + currently_sleeping_ms;

        // Use the new formatter that always shows seconds and includes the percentage
        tvSleepValue.setText(formatSleepWithPercent(totalSleepMs, uptimeMillis));

        // --- DEEP SLEEP CALCULATION (cpuspy logic) ---
        // elapsedRealtime() includes deep sleep. uptimeMillis() does not.
        // The difference is the exact amount of time the CPU has been suspended since device boot.
        long currentElapsed = SystemClock.elapsedRealtime();
        long currentUptime = SystemClock.uptimeMillis();

        long totalDeepSleepMs = currentElapsed - currentUptime;
        long totalBootMs = currentElapsed;

        double sleepPct = (totalBootMs > 0) ? (totalDeepSleepMs * 100.0) / totalBootMs : 0.0;
        String humanDeepSleep = formatDuration(totalDeepSleepMs);

        tvDeepSleepValue.setText(humanDeepSleep + " (" + String.format(Locale.US, "%.1f%%", sleepPct) + ")");

        updateWakeupHistory();
    }

    /** Compact one-line view: specific emoji codes + relative age. */
    private void updateWakeupHistory() {
        long now = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        int idx = TrifaToxService.wakeup_ring_index;
        int maxHistory = TrifaToxService.MAX_WAKEUP_HISTORY;
        boolean hasHistory = false;

        for (int i = 0; i < maxHistory; i++) {
            int readIdx = (idx - 1 - i + maxHistory) % maxHistory;
            String reason = TrifaToxService.last_wakeup_reasons[readIdx];
            long time = TrifaToxService.last_wakeup_times[readIdx];
            long sleepDur = TrifaToxService.last_sleep_durations[readIdx];
            long awakeDur = TrifaToxService.last_awake_durations[readIdx];

            if (reason != null && time > 0) {
                if (hasHistory) sb.append("\n");

                String code = wakeupCode(reason);
                String ago = formatTimeAgo(now - time);

                // Format: 🔔push(2m ago) 💤20m ⚡5m
                sb.append(code).append("(").append(ago).append(" ago)");
                if (sleepDur > 0) sb.append(" 💤").append(formatDurationShort(sleepDur));
                if (awakeDur > 0) sb.append(" ⚡").append(formatDurationShort(awakeDur));

                hasHistory = true;
            }
        }

        tvWakeupHistory.setText(hasHistory ? sb.toString() : "No recent wakeups");
    }

    /** Compact but specific code for the one-line view. */
    private String wakeupCode(String reason) {
        if (reason == null || reason.isEmpty()) return "?";

        if (reason.startsWith("INT:")) {
            String exc = reason.substring(4);
            int colon = exc.indexOf(':');
            if (colon > 0) exc = exc.substring(0, colon);
            if (exc.length() > 10) exc = exc.substring(0, 10);
            return "⚠️" + exc;
        }
        if (reason.startsWith("PUSH_NTFY")) return "🔔push";
        if (reason.startsWith("PUSH_")) return "🔔" + reason.substring(5).toLowerCase(Locale.US);

        switch (reason) {
            case "SCHED_FULL":   return "⏰full";
            case "UI_MSGVIEW":   return "📱msgV";
            case "UI_GROUPVIEW": return "📱grpV";
            case "MSG_IN":       return "💬msg";
            case "MSG_V2_IN":    return "💬msgv2";
            case "MSG_V3_IN":    return "💬msgv3";
            case "CALL_IN":      return "📞call";
            case "FT_IN":        return "📁ft";
            case "GC_INVITE":    return "👥ginv";
            case "CONF_INVITE":  return "👥cinv";
            case "ALARM":        return "⏰alarm";
            case "CALL_ACTIVE":     return "📞call";
            case "AUDIO_GROUP":     return "🔊agrpa";
            case "NGC_AUDIO_GROUP": return "🔊ngca";
            case "RECENT_ACTIVITY": return "✋actvy";
            case "RECENTLY_ONLINE": return "🟢onln";
            case "ONLINE_TS_UNSET": return "❓onTs";
            case "TRIG_UNKNOWN": return "❓trig";
            default:
                String r = reason.toLowerCase(Locale.US);
                if (r.length() > 12) r = r.substring(0, 12);
                return r;
        }
    }

    /** Tap target: exact reasons with exact wall-clock timestamps. */
    private void showWakeupDetailsDialog() {
        long now = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        int idx = TrifaToxService.wakeup_ring_index;
        int maxHistory = TrifaToxService.MAX_WAKEUP_HISTORY;
        boolean hasHistory = false;

        for (int i = 0; i < maxHistory; i++) {
            int readIdx = (idx - 1 - i + maxHistory) % maxHistory;
            String reason = TrifaToxService.last_wakeup_reasons[readIdx];
            long time = TrifaToxService.last_wakeup_times[readIdx];
            long sleepDur = TrifaToxService.last_sleep_durations[readIdx];
            long awakeDur = TrifaToxService.last_awake_durations[readIdx];

            if (reason != null && time > 0) {
                String ts = new java.text.SimpleDateFormat("HH:mm:ss", Locale.US).format(new java.util.Date(time));

                sb.append("⏰ ").append(ts).append("  (").append(formatTimeAgo(now - time)).append(" ago)\n");
                sb.append("   Reason: ").append(reason).append("\n");
                if (sleepDur > 0) sb.append("   💤 Sleep duration: ").append(formatDuration(sleepDur)).append("\n");
                if (awakeDur > 0) sb.append("   ⚡ Awake before sleep: ").append(formatDuration(awakeDur)).append("\n");
                sb.append("\n");
                hasHistory = true;
            }
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Sleep / Wake Cycle History")
                .setMessage(hasHistory ? sb.toString() : "No wakeups recorded yet")
                .setPositiveButton("OK", null)
                .show();
    }

    private String formatDurationShort(long ms) {
        if (ms < 0) ms = 0;
        long sec = ms / 1000;
        if (sec < 60) return sec + "s";
        long min = sec / 60;
        if (min < 60) return min + "m";
        long hr = min / 60;
        if (hr < 24) return hr + "h " + (min % 60) + "m";
        long days = hr / 24;
        return days + "d " + (hr % 24) + "h";
    }

    private String formatTimeAgo(long ms) {
        if (ms < 0) ms = 0;
        long sec = ms / 1000;
        if (sec < 60) return sec + "s";
        long min = sec / 60;
        if (min < 60) return min + "m";
        long hr = min / 60;
        if (hr < 24) return hr + "h";
        long days = hr / 24;
        return days + "d";
    }

    /**
     * Formats milliseconds into a human-readable duration string that ALWAYS includes seconds,
     * and appends the percentage relative to the total uptime.
     * Example output: "02h 15m 30s (45.2%)"
     */
    private String formatSleepWithPercent(long sleepMs, long uptimeMs) {
        if (sleepMs < 0) sleepMs = 0;
        long seconds = sleepMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        seconds = seconds % 60;
        minutes = minutes % 60;
        hours = hours % 24;

        String timeStr;
        if (days > 0) {
            timeStr = String.format(Locale.US, "%dd %02dh %02dm %02ds", days, hours, minutes, seconds);
        } else if (hours > 0) {
            timeStr = String.format(Locale.US, "%02dh %02dm %02ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            timeStr = String.format(Locale.US, "%02dm %02ds", minutes, seconds);
        } else {
            timeStr = String.format(Locale.US, "%02ds", seconds);
        }

        double pct = (uptimeMs > 0) ? (sleepMs * 100.0) / uptimeMs : 0.0;
        return timeStr + " (" + String.format(Locale.US, "%.1f%%", pct) + ")";
    }

    private void updateHeatBars(long sentBps, long recvBps, long cpuCps) {
        float sentRatio = rateToHeatRatio(sentBps);
        float recvRatio = rateToHeatRatio(recvBps);
        float cpuRatio = cpuToHeatRatio(cpuCps);

        int sentColor = getHeatColor(sentRatio);
        int recvColor = getHeatColor(recvRatio);
        int cpuColor = getHeatColor(cpuRatio);

        // Linear width based on 500KB/s max for network
        double maxBpsLinear = 500.0 * 1024.0;
        float sentWidthRatio = (float) Math.min(Math.max(sentBps / maxBpsLinear, 0.02), 1.0);
        float recvWidthRatio = (float) Math.min(Math.max(recvBps / maxBpsLinear, 0.02), 1.0);
        if (sentBps == 0) sentWidthRatio = 0;
        if (recvBps == 0) recvWidthRatio = 0;

        // Linear width based on 3 Gc/s max for CPU
        double maxCpsLinear = 3_000_000_000.0;
        float cpuWidthRatio = (float) Math.min(Math.max(cpuCps / maxCpsLinear, 0.02), 1.0);
        if (cpuCps == 0) cpuWidthRatio = 0;

        viewSentHeat.setBackgroundColor(sentColor);
        viewRecvHeat.setBackgroundColor(recvColor);
        viewCpuHeat.setBackgroundColor(cpuColor);

        viewSentHeat.setPivotX(0);
        viewSentHeat.setScaleX(sentWidthRatio);

        viewRecvHeat.setPivotX(0);
        viewRecvHeat.setScaleX(recvWidthRatio);

        viewCpuHeat.setPivotX(0);
        viewCpuHeat.setScaleX(cpuWidthRatio);

        tvSentHeatRate.setText(formatRate(sentBps));
        tvRecvHeatRate.setText(formatRate(recvBps));
        tvCpuHeatRate.setText(formatCycles(cpuCps));
    }

    /**
     * Shows a detailed popup dialog for a specific packet or the middleware box.
     */
    public static void showPacketDetailsDialog(android.content.Context context, PacketStat stat) {
        StringBuilder sb = new StringBuilder();
        String title;

        if (stat.id == -1) {
            // Middleware details
            title = "Middleware Details";
            sb.append("Middleware Custom Packets (NGC)\n");
            sb.append("\n");
            sb.append("Sent: ").append(formatBytes(stat.sentBytes)).append("\n");
            sb.append("Recv: ").append(formatBytes(stat.recvBytes)).append("\n");
            sb.append("Rate: ").append(formatRate(stat.bytesPerSec)).append("\n");
            sb.append("\n");
            sb.append("Total: ").append(formatBytes(stat.sentBytes + stat.recvBytes)).append("\n");
        } else {
            // Standard Packet Details
            String hexId = String.format(Locale.US, "0x%02X", stat.id);
            title = stat.name + " (" + stat.transport + ")";

            sb.append("Transport: ").append(stat.transport).append("\n");
            sb.append("Packet ID: ").append(hexId).append(" (").append(stat.id).append(")\n");
            sb.append("\n");
            sb.append("Sent: ").append(stat.sentCount).append(" pkts (").append(formatBytes(stat.sentBytes)).append(")\n");
            sb.append("Recv: ").append(stat.recvCount).append(" pkts (").append(formatBytes(stat.recvBytes)).append(")\n");
            sb.append("Rate: ").append(formatRate(stat.bytesPerSec)).append("\n");
            sb.append("\n");
            long totalBytes = stat.sentBytes + stat.recvBytes;
            long totalPkts = stat.sentCount + stat.recvCount;
            sb.append("Total: ").append(totalPkts).append(" pkts (").append(formatBytes(totalBytes)).append(")\n");
        }

        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(sb.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    // --- Formatting & Math Helpers ---

    public static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double kb = bytes / 1024.0;
        if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb);
        double mb = kb / 1024.0;
        if (mb < 1024) return String.format(Locale.US, "%.2f MB", mb);
        double gb = mb / 1024.0;
        return String.format(Locale.US, "%.2f GB", gb);
    }

    public static String formatRate(long bytesPerSec) {
        if (bytesPerSec <= 0) return "0 B/s";
        if (bytesPerSec < 1024) return bytesPerSec + " B/s";
        double kb = bytesPerSec / 1024.0;
        if (kb < 1024) return String.format(Locale.US, "%.1f KB/s", kb);
        double mb = kb / 1024.0;
        if (mb < 1024) return String.format(Locale.US, "%.2f MB/s", mb);
        double gb = mb / 1024.0;
        return String.format(Locale.US, "%.2f GB/s", gb);
    }

    public static String formatCycles(long cps) {
        if (cps <= 0) return "0 c/s";
        if (cps < 1000) return cps + " c/s";
        double k = cps / 1000.0;
        if (k < 1000) return String.format(Locale.US, "%.1f Kc/s", k);
        double m = k / 1000.0;
        if (m < 1000) return String.format(Locale.US, "%.1f Mc/s", m);
        double g = m / 1000.0;
        return String.format(Locale.US, "%.2f Gc/s", g);
    }

    public static String formatUptime(long millis) {
        if (millis < 0) return "00:00:00";
        long totalSeconds = millis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * Formats milliseconds into a human-readable duration string (e.g., "1d 02h 30m 15s").
     */
    private String formatDuration(long ms) {
        if (ms < 0) ms = 0;
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        seconds = seconds % 60;
        minutes = minutes % 60;
        hours = hours % 24;

        if (days > 0) {
            return String.format(Locale.US, "%dd %02dh", days, hours);
        } else if (hours > 0) {
            return String.format(Locale.US, "%02dh %02dm", hours, minutes);
        } else if (minutes > 0) {
            return String.format(Locale.US, "%02dm %02ds", minutes, seconds);
        } else {
            return String.format(Locale.US, "%02ds", seconds);
        }
    }

    public static float rateToHeatRatio(long bytesPerSec) {
        if (bytesPerSec <= 0) return 0f;
        double maxBps = 500.0 * 1024.0;
        double logMax = Math.log10(maxBps);
        double logVal = Math.log10(Math.max(bytesPerSec, 1.0));
        float ratio = (float) Math.min(Math.max(logVal / logMax, 0.0), 1.0);
        return Math.max(ratio, 0.05f);
    }

    /**
     * Map CPU cycles per second to a 0..1 heat ratio.
     * Uses a shifted log scale from 10 Mc/s to 10 Gc/s.
     */
    public static float cpuToHeatRatio(long cps) {
        if (cps <= 10_000_000L) return 0f; // Below 10 Mc/s is essentially idle
        double logMax = 3.0; // 10 Mc/s to 10 Gc/s is 3 decades
        double logVal = Math.log10(cps / 10_000_000.0);
        float ratio = (float) Math.min(Math.max(logVal / logMax, 0.0), 1.0);
        return Math.max(ratio, 0.05f);
    }

    public static int lerpColor(int c1, int c2, float t) {
        float f = Math.min(Math.max(t, 0f), 1f);
        int a = (int) (Color.alpha(c1) + (Color.alpha(c2) - Color.alpha(c1)) * f);
        int r = (int) (Color.red(c1) + (Color.red(c2) - Color.red(c1)) * f);
        int g = (int) (Color.green(c1) + (Color.green(c2) - Color.green(c1)) * f);
        int b = (int) (Color.blue(c1) + (Color.blue(c2) - Color.blue(c1)) * f);
        return Color.argb(a, r, g, b);
    }

    public static int getHeatColor(float ratio) {
        float r = Math.min(Math.max(ratio, 0f), 1f);
        int C_NONE = Color.parseColor("#2C2C2E");
        int C_LOW1 = Color.parseColor("#1A237E");
        int C_LOW2 = Color.parseColor("#0288D1");
        int C_MED  = Color.parseColor("#4CAF50");
        int C_HIGH = Color.parseColor("#FFEB3B");
        int C_MAX  = Color.parseColor("#F44336");

        if (r == 0f) return C_NONE;
        if (r < 0.25f) return lerpColor(C_LOW1, C_LOW2, r / 0.25f);
        if (r < 0.5f) return lerpColor(C_LOW2, C_MED, (r - 0.25f) / 0.25f);
        if (r < 0.75f) return lerpColor(C_MED, C_HIGH, (r - 0.5f) / 0.25f);
        return lerpColor(C_HIGH, C_MAX, (r - 0.75f) / 0.25f);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    // --- Data Model & Adapter ---

    private static class PacketStat {
        int id;
        String name;
        String transport;
        long sentCount;
        long recvCount;
        long sentBytes;
        long recvBytes;
        long bytesPerSec;
    }

    private static class PacketAdapter extends RecyclerView.Adapter<PacketAdapter.ViewHolder> {
        private final List<PacketStat> stats;

        public PacketAdapter(List<PacketStat> stats) {
            this.stats = stats;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_netprof_packet, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PacketStat stat = stats.get(position);
            holder.bind(stat);
            holder.itemView.setOnClickListener(v -> showPacketDetailsDialog(v.getContext(), stat));
        }

        @Override
        public int getItemCount() {
            return stats.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            LinearLayout layoutPacket;
            TextView tvName, tvTransport, tvBytes, tvRatePkts;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                layoutPacket = itemView.findViewById(R.id.layout_packet);
                tvName = itemView.findViewById(R.id.tv_packet_name);
                tvTransport = itemView.findViewById(R.id.tv_packet_transport);
                tvBytes = itemView.findViewById(R.id.tv_packet_bytes);
                tvRatePkts = itemView.findViewById(R.id.tv_packet_rate_pkts);
            }

            public void bind(PacketStat stat) {
                if (stat.id == -1) {
                    // Middleware
                    tvName.setText("MIDDLEWARE");
                    tvTransport.setText("NGC");
                    tvBytes.setText(formatBytes(stat.sentBytes + stat.recvBytes));
                    tvRatePkts.setText("S: " + formatBytes(stat.sentBytes) + "\n" + "R: " + formatBytes(stat.recvBytes));
                    layoutPacket.setBackgroundColor(Color.parseColor("#9C27B0"));
                    tvName.setTextColor(Color.WHITE);
                    tvTransport.setTextColor(Color.argb(180, 255, 255, 255));
                    tvBytes.setTextColor(Color.WHITE);
                    tvRatePkts.setTextColor(Color.argb(200, 255, 255, 255));
                } else {
                    tvName.setText(stat.name);
                    tvTransport.setText(stat.transport);
                    long totalBytes = stat.sentBytes + stat.recvBytes;
                    long totalPkts = stat.sentCount + stat.recvCount;
                    tvBytes.setText(formatBytes(totalBytes));
                    tvRatePkts.setText(formatRate(stat.bytesPerSec) + "\n" + totalPkts + " pkts");

                    float heatRatio = rateToHeatRatio(stat.bytesPerSec);
                    int bgColor = getHeatColor(heatRatio);
                    layoutPacket.setBackgroundColor(bgColor);

                    int textColor = heatRatio > 0.6f ? Color.BLACK : Color.WHITE;
                    int subTextColor = heatRatio > 0.6f ? Color.argb(200, 0, 0, 0) : Color.argb(200, 255, 255, 255);

                    tvName.setTextColor(textColor);
                    tvTransport.setTextColor(subTextColor);
                    tvBytes.setTextColor(textColor);
                    tvRatePkts.setTextColor(subTextColor);
                }
            }
        }
    }
}
