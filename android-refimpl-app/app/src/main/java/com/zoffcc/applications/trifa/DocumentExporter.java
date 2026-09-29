package com.zoffcc.applications.trifa;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.documentfile.provider.DocumentFile;

import java.util.List;
import java.util.Map;

/**
 * Reusable helper class for exporting files using the Android Storage Access Framework (SAF).
 * Handles directory selection, persisting URI permissions, and file creation.
 */
public class DocumentExporter {
    private static final String PREFS_NAME = "trifa_document_exporter_prefs";

    private final Activity activity;
    private final String prefsKey;
    private final int requestCode;

    private Uri treeUri = null;
    private boolean pendingAction = false;
    private Runnable pendingSuccessRunnable = null;
    private ErrorCallback pendingErrorCallback = null;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface ErrorCallback {
        void onError(String message);
    }

    /**
     * @param activity    The calling Activity (needed for startActivityForResult and ContentResolver)
     * @param prefsKey    A unique key to store the selected directory URI in SharedPreferences
     * @param requestCode The request code to use for the SAF folder picker Intent
     */
    public DocumentExporter(Activity activity, String prefsKey, int requestCode) {
        this.activity = activity;
        this.prefsKey = prefsKey;
        this.requestCode = requestCode;
        this.treeUri = loadSavedTreeUri();
    }

    public boolean hasDirectorySelected() {
        return hasUsableTree(treeUri);
    }

    /**
     * Ensures a writable directory is selected.
     * If already selected, runs onSuccess immediately on the UI thread.
     * If not, launches the folder picker and runs onSuccess after the user picks.
     */
    public void ensureDirectory(Runnable onSuccess, ErrorCallback onError) {
        if (hasUsableTree(treeUri)) {
            onSuccess.run();
        } else {
            pendingAction = true;
            pendingSuccessRunnable = onSuccess;
            pendingErrorCallback = onError;
            pickFolder();
        }
    }

    /**
     * Creates a new file in the currently selected directory.
     * MUST be called on a background thread to avoid NetworkOnMainThreadException / ANRs.
     *
     * @return The Uri of the created file, or null if failed.
     */
    public Uri createFileUri(String fileName, String mimeType) {
        if (!hasUsableTree(treeUri)) return null;

        try {
            DocumentFile directoryFile = DocumentFile.fromTreeUri(activity, treeUri);
            if (directoryFile == null) return null;

            // Delete existing file to prevent SAF from appending " (1)" to the filename
            DocumentFile existing = directoryFile.findFile(fileName);
            if (existing != null && existing.exists()) {
                existing.delete();
            }

            DocumentFile newFile = directoryFile.createFile(mimeType, fileName);
            return newFile != null ? newFile.getUri() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Forces the user to pick a new directory, replacing the currently saved one.
     */
    public void changeDirectory(Runnable onSuccess, ErrorCallback onError) {
        pendingAction = true;
        pendingSuccessRunnable = onSuccess;
        pendingErrorCallback = onError;
        pickFolder();
    }

    private void pickFolder() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            activity.startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            pendingAction = false;
            if (pendingErrorCallback != null) {
                pendingErrorCallback.onError("Could not open folder picker: " + e.getMessage());
            }
            pendingSuccessRunnable = null;
            pendingErrorCallback = null;
        }
    }

    /**
     * Call this from your Activity's onActivityResult.
     * @return true if the request code matched and was handled by this exporter.
     */
    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != this.requestCode) {
            return false;
        }

        if (resultCode != Activity.RESULT_OK || data == null) {
            pendingAction = false;
            if (pendingErrorCallback != null) {
                pendingErrorCallback.onError("Folder selection cancelled");
            }
            pendingSuccessRunnable = null;
            pendingErrorCallback = null;
            return true;
        }

        Uri picked = data.getData();
        if (picked == null) {
            pendingAction = false;
            if (pendingErrorCallback != null) {
                pendingErrorCallback.onError("Invalid folder selected");
            }
            pendingSuccessRunnable = null;
            pendingErrorCallback = null;
            return true;
        }

        // --- SAFE RELEASE LOGIC ---
        // Only release the old URI if no other exporter instance is still using it
        if (treeUri != null && !treeUri.equals(picked)) {
            if (!isUriUsedByOtherExporters(treeUri, prefsKey)) {
                try {
                    activity.getContentResolver().releasePersistableUriPermission(treeUri,
                                                                                  Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                } catch (Exception ignored) {}
            }
        }
        // --------------------------

        try {
            activity.getContentResolver().takePersistableUriPermission(picked,
                                                                       Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } catch (Exception ignored) {}

        treeUri = picked;
        saveTreeUri(picked);

        if (pendingAction) {
            pendingAction = false;
            Runnable success = pendingSuccessRunnable;
            pendingSuccessRunnable = null;
            pendingErrorCallback = null;

            if (success != null) {
                mainHandler.post(success);
            }
        }
        return true;
    }

    private Uri loadSavedTreeUri() {
        String s = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(prefsKey, null);
        if (s == null) return null;
        try {
            Uri u = Uri.parse(s);
            if (hasUsableTree(u)) return u;
            activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().remove(prefsKey).apply();
        } catch (Exception e) {}
        return null;
    }

    private void saveTreeUri(Uri u) {
        activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(prefsKey, u.toString()).apply();
    }

    private boolean hasUsableTree(Uri u) {
        if (u == null) return false;
        try {
            List<UriPermission> perms = activity.getContentResolver().getPersistedUriPermissions();
            if (perms == null) return false;
            for (UriPermission p : perms) {
                if (p.getUri().equals(u) && p.isWritePermission()) {
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    /**
     * Checks if the given URI is currently saved in the preferences of any OTHER DocumentExporter instance.
     * This prevents us from accidentally revoking a folder permission that is still in use by another part of the app.
     */
    private boolean isUriUsedByOtherExporters(Uri uriToCheck, String currentPrefsKey) {
        if (uriToCheck == null) return false;
        String uriString = uriToCheck.toString();

        try {
            SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            Map<String, ?> allEntries = prefs.getAll();

            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                String key = entry.getKey();
                // Skip the current exporter's key, as it's about to be overwritten
                if (key.equals(currentPrefsKey)) continue;

                Object value = entry.getValue();
                if (value instanceof String && value.equals(uriString)) {
                    return true; // Found it in another exporter's prefs
                }
            }
        } catch (Exception ignored) {}
        return false;
    }
}
