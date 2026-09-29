package com.zoffcc.applications.trifa;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Reusable helper class for importing files using the Android Storage Access Framework (SAF).
 * Handles file selection and copying the contents to a specified internal destination path.
 */
public class DocumentImporter {
    private final Activity activity;
    private final int requestCode;

    private boolean pendingAction = false;
    private String pendingDestPath = null;
    private String pendingDestFileName = null;
    private ImportCallback pendingCallback = null;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface ImportCallback {
        void onSuccess();
        void onError(String message);
    }

    /**
     * @param activity    The calling Activity (needed for startActivityForResult and ContentResolver)
     * @param requestCode The request code to use for the SAF file picker Intent
     */
    public DocumentImporter(Activity activity, int requestCode) {
        this.activity = activity;
        this.requestCode = requestCode;
    }

    /**
     * Launches the file picker to select a file for import.
     *
     * @param destPath      The internal directory path where the file should be saved.
     * @param destFileName  The filename to use when saving the imported file.
     * @param mimeType      The MIME type to filter the file picker (e.g., "application/octet-stream" or "" or null for any file type)
     * @param callback      The callback to notify of success or failure.
     */
    public void importFile(String destPath, String destFileName, String mimeType, ImportCallback callback) {
        pendingAction = true;
        pendingDestPath = destPath;
        pendingDestFileName = destFileName;
        pendingCallback = callback;

        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            // Fallback to all files if mimeType is null or empty
            intent.setType((mimeType != null && !mimeType.isEmpty()) ? mimeType : "*/*");
            activity.startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            pendingAction = false;
            if (pendingCallback != null) {
                pendingCallback.onError("Could not open file picker: " + e.getMessage());
            }
            pendingCallback = null;
        }
    }

    /**
     * Call this from your Activity's onActivityResult.
     * @return true if the request code matched and was handled by this importer.
     */
    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != this.requestCode || !pendingAction) {
            return false;
        }

        if (resultCode != Activity.RESULT_OK || data == null) {
            pendingAction = false;
            if (pendingCallback != null) {
                pendingCallback.onError("File selection cancelled");
            }
            pendingCallback = null;
            return true;
        }

        final Uri pickedUri = data.getData();
        if (pickedUri == null) {
            pendingAction = false;
            if (pendingCallback != null) {
                pendingCallback.onError("Invalid file selected");
            }
            pendingCallback = null;
            return true;
        }

        // Capture variables for the background thread
        final String finalDestPath = pendingDestPath;
        final String finalDestFileName = pendingDestFileName;
        final ImportCallback finalCallback = pendingCallback;

        // Reset pending state immediately to prevent memory leaks
        pendingAction = false;
        pendingDestPath = null;
        pendingDestFileName = null;
        pendingCallback = null;

        // Perform the file copy on a background thread to avoid ANRs
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream in = null;
                OutputStream out = null;
                try {
                    // Ensure destination directory exists
                    File destDir = new File(finalDestPath);
                    if (!destDir.exists()) {
                        destDir.mkdirs();
                    }

                    File destFile = new File(destDir, finalDestFileName);

                    in = activity.getContentResolver().openInputStream(pickedUri);
                    if (in == null) {
                        throw new Exception("Could not open input stream from selected file");
                    }

                    out = new FileOutputStream(destFile);

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    out.flush();

                    // Success
                    if (finalCallback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                finalCallback.onSuccess();
                            }
                        });
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    if (finalCallback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                finalCallback.onError("Import failed: " + e.getMessage());
                            }
                        });
                    }
                } finally {
                    // Ensure streams are closed even if an exception occurs
                    if (in != null) {
                        try { in.close(); } catch (Exception ignored) {}
                    }
                    if (out != null) {
                        try { out.close(); } catch (Exception ignored) {}
                    }
                }
            }
        }).start();

        return true;
    }
}
