package com.zoffcc.applications.trifa;

import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TombstoneDecoder {
    private static final String TAG = "TombstoneParser";

    private static final int MAX_NUMBER_OF_FRAMES_BT = 20;

    public static String parseNativeStacktrace(InputStream is) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            byte[] rawBytes = baos.toByteArray();

            // 1. Extract the actual crashing thread ID from root field 6
            long crashingTid = findCrashingThreadId(new ByteArrayInputStream(rawBytes));
            Log.d(TAG, "Global Crashing TID: " + crashingTid);

            StringBuilder sb = new StringBuilder();
            // 2. Parse only that thread, extraction limited strictly to the n crash site lines
            parseTombstoneForThread(new ByteArrayInputStream(rawBytes), sb, crashingTid);
            return sb.toString();
        } catch (Exception e) {
            Log.e(TAG, "Failed to decode protobuf tombstone structure", e);
            return "Error decoding stacktrace: " + e.getMessage();
        }
    }

    private static long findCrashingThreadId(ByteArrayInputStream bis) throws IOException {
        while (bis.available() > 0) {
            int tag = (int) readVarint(bis);
            int fieldNum = tag >>> 3;
            int wireType = tag & 0x07;

            if (fieldNum == 6 && wireType == 0) {
                return readVarint(bis);
            } else {
                skipField(bis, wireType);
            }
        }
        return -1;
    }

    private static void parseTombstoneForThread(ByteArrayInputStream bis, StringBuilder sb, long targetTid) throws IOException {
        while (bis.available() > 0) {
            int tag = (int) readVarint(bis);
            int fieldNum = tag >>> 3;
            int wireType = tag & 0x07;

            // Root Field 16: map<uint32, Thread> threads
            if (fieldNum == 16 && wireType == 2) {
                int mapEntryLength = (int) readVarint(bis);
                byte[] mapEntryBuffer = readBytes(bis, mapEntryLength);

                ByteArrayInputStream entryBis = new ByteArrayInputStream(mapEntryBuffer);
                byte[] threadBuffer = null;

                while (entryBis.available() > 0) {
                    int entryTag = (int) readVarint(entryBis);
                    int entryFieldNum = entryTag >>> 3;
                    int entryWireType = entryTag & 0x07;

                    if (entryFieldNum == 2 && entryWireType == 2) {
                        int threadLength = (int) readVarint(entryBis);
                        threadBuffer = readBytes(entryBis, threadLength);
                    } else {
                        skipField(entryBis, entryWireType);
                    }
                }

                if (threadBuffer != null) {
                    long actualThreadId = parseThreadId(new ByteArrayInputStream(threadBuffer));

                    if (actualThreadId == targetTid || targetTid == -1) {
                        ByteArrayInputStream threadBis = new ByteArrayInputStream(threadBuffer);
                        int displayFrameIndex = 0;

                        while (threadBis.available() > 0) {
                            int tTag = (int) readVarint(threadBis);
                            int tFieldNum = tTag >>> 3;
                            int tWireType = tTag & 0x07;

                            // Thread Field 4 contains backtrace frames
                            if (tFieldNum == 4 && tWireType == 2) {
                                int frameLength = (int) readVarint(threadBis);
                                byte[] frameBuffer = readBytes(threadBis, frameLength);

                                // STOP CRITERIA: Once x frames are collected from the crash site, return immediately
                                if (displayFrameIndex < MAX_NUMBER_OF_FRAMES_BT) {
                                    ByteArrayInputStream frameBis = new ByteArrayInputStream(frameBuffer);
                                    String formattedFrame = parseSingleFrame(frameBis, displayFrameIndex);
                                    if (!formattedFrame.isEmpty()) {
                                        sb.append(formattedFrame).append("\n");
                                        displayFrameIndex++;
                                    }
                                } else {
                                    return; // x frames extracted successfully. Break out of the method.
                                }
                            } else {
                                skipField(threadBis, tWireType);
                            }
                        }
                        return;
                    }
                }
            } else {
                skipField(bis, wireType);
            }
        }
    }

    private static long parseThreadId(ByteArrayInputStream bis) throws IOException {
        while (bis.available() > 0) {
            int tag = (int) readVarint(bis);
            int fieldNum = tag >>> 3;
            int wireType = tag & 0x07;

            if (fieldNum == 1 && wireType == 0) {
                return readVarint(bis);
            } else {
                skipField(bis, wireType);
            }
        }
        return -1;
    }

    private static String parseSingleFrame(ByteArrayInputStream bis, int index) throws IOException {
        long pc = 0;
        String fileName = "";
        String functionName = "";

        while (bis.available() > 0) {
            int tag = (int) readVarint(bis);
            int fieldNum = tag >>> 3;
            int wireType = tag & 0x07;

            switch (fieldNum) {
                case 3: // uint64 pc
                    if (wireType == 0) pc = readVarint(bis);
                    else skipField(bis, wireType);
                    break;
                case 4: // string file_name
                    if (wireType == 2) fileName = new String(readBytes(bis, (int) readVarint(bis)), StandardCharsets.UTF_8);
                    else skipField(bis, wireType);
                    break;
                case 6: // string function_name
                    if (wireType == 2) functionName = new String(readBytes(bis, (int) readVarint(bis)), StandardCharsets.UTF_8);
                    else skipField(bis, wireType);
                    break;
                default:
                    skipField(bis, wireType);
                    break;
            }
        }

        if (pc != 0 || !fileName.isEmpty()) {
            if (fileName.isEmpty()) fileName = "unknown";

            StringBuilder frameLine = new StringBuilder();
            frameLine.append(String.format("#%02d pc %016x  ", index, pc));
            if (!functionName.isEmpty()) {
                frameLine.append(functionName).append(" ");
            }
            frameLine.append("[").append(fileName).append("]");
            return frameLine.toString();
        }
        return "";
    }

    private static long readVarint(ByteArrayInputStream bis) throws IOException {
        long result = 0;
        int shift = 0;
        while (shift < 64) {
            int b = bis.read();
            if (b == -1) throw new IOException("Unexpected EOF reading Varint");
            result |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0) return result;
            shift += 7;
        }
        throw new IOException("Malformed Varint signature");
    }

    private static byte[] readBytes(ByteArrayInputStream bis, int length) throws IOException {
        byte[] data = new byte[length];
        int read = bis.read(data, 0, length);
        if (read != length) throw new IOException("Truncated payload stream block target window");
        return data;
    }

    private static void skipField(ByteArrayInputStream bis, int wireType) throws IOException {
        switch (wireType) {
            case 0: readVarint(bis); break;
            case 1: bis.skip(8); break;
            case 2: bis.skip((int) readVarint(bis)); break;
            case 5: bis.skip(4); break;
            default: throw new IOException("Invalid Protobuf Wire Type Encountered: " + wireType);
        }
    }
}
