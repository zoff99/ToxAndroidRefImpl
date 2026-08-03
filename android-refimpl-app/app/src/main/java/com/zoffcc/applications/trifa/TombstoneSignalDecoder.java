package com.zoffcc.applications.trifa;

import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TombstoneSignalDecoder {
    private static final String TAG = "SignalDecoder";

    public static String parseCrashReason(InputStream is) {
        Log.d(TAG, "===> Starting Robust Header Context Signal Scan <===");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            // Load the first blocks where header metrics live
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
                if (baos.size() > 16384) break; // Clamp data window to keep processing instant
            }
            byte[] headerBytes = baos.toByteArray();
            Log.d(TAG, "Loaded header snapshot cache size: " + headerBytes.length + " bytes.");

            return extractSignalFromHeaderBytes(headerBytes);
        } catch (Exception e) {
            Log.e(TAG, "CRITICAL: Signal context parsing failed", e);
            return "Signal: Unknown (Context Error)";
        }
    }

    private static String extractSignalFromHeaderBytes(byte[] data) {
        // Step 1: Recover printable characters into a clean text presentation sequence
        StringBuilder textBuilder = new StringBuilder();
        for (byte b : data) {
            if ((b >= 'A' && b <= 'Z') || (b >= 'a' && b <= 'z') || (b >= '0' && b <= '9') || b == '_' || b == '-') {
                textBuilder.append((char) b);
            } else {
                if (textBuilder.length() > 0 && textBuilder.charAt(textBuilder.length() - 1) != ' ') {
                    textBuilder.append(' ');
                }
            }
        }

        String flatText = textBuilder.toString().trim();
        Log.d(TAG, "Dumped Header Strings: \"" + (flatText.length() > 200 ? flatText.substring(0, 200) + "..." : flatText) + "\"");

        String signalName = "UNKNOWN";
        String codeName = "";

        // Step 2: Search tokens for valid standard Android kernel crash metrics
        String[] tokens = flatText.split("\\s+");
        for (String token : tokens) {
            if (token.equals("SIGSEGV") || token.equals("SIGABRT") || token.equals("SIGILL") ||
                token.equals("SIGBUS") || token.equals("SIGFPE") || token.equals("SIGTRAP")) {
                signalName = token;
            } else if (token.equals("SEGV_MAPERR") || token.equals("SEGV_ACCERR") ||
                       token.equals("BUS_ADRALN") || token.equals("BUS_ADRERR") ||
                       token.equals("ILL_ILLOPC") || token.equals("SI_USER")) {
                codeName = token;
            }
        }

        // Step 3: Handle a common fallback if strings match signatures but aren't standard tokens
        if (signalName.equals("UNKNOWN")) {
            for (String token : tokens) {
                if (token.startsWith("SIG") && token.length() > 3 && token.length() < 10) {
                    signalName = token;
                    break;
                }
            }
        }

        StringBuilder reason = new StringBuilder("Signal: ").append(signalName);
        if (!codeName.isEmpty()) {
            reason.append(" (").append(codeName).append(")");
        }

        Log.d(TAG, "Final Extracted Crash Output: " + reason.toString());
        return reason.toString();
    }
}
