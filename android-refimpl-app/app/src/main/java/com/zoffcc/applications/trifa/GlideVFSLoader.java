/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 Zoff <zoff@zoff.cc>
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

package com.zoffcc.applications.trifa;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Glide ModelLoader for IOCipher (encrypted VFS) files.
 *
 * KEY DESIGN DECISION: This loader accepts {@link info.guardianproject.iocipher.File}
 * (not FileInputStream) so that:
 *   1. Cache keys are based on stable file path + length (not ephemeral FD identity)
 *   2. Stream creation happens inside loadData() on Glide's worker thread (no race conditions)
 *   3. Disk cache correctly differentiates between files via updateDiskCacheKey()
 */
public class GlideVFSLoader implements ModelLoader<info.guardianproject.iocipher.File, java.io.InputStream> {

    private static final String TAG = "GlideVFSLoader";

    public GlideVFSLoader() {
    }

    @Nullable
    @Override
    public LoadData<java.io.InputStream> buildLoadData(@NonNull info.guardianproject.iocipher.File model, int width, int height, @NonNull Options options) {
        return new LoadData<>(new VFSCacheKey(model), new VFSDataFetcher(model));
    }

    @Override
    public boolean handles(@NonNull info.guardianproject.iocipher.File model) {
        return true;
    }

    // ────────────────────────────────────────────────────────────────
    // Factory
    // ────────────────────────────────────────────────────────────────

    public static class Factory implements ModelLoaderFactory<info.guardianproject.iocipher.File, java.io.InputStream> {
        @NonNull
        @Override
        public ModelLoader<info.guardianproject.iocipher.File, java.io.InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new GlideVFSLoader();
        }

        @Override
        public void teardown() {
            // Nothing to clean up
        }
    }

    // ────────────────────────────────────────────────────────────────
    // Cache Key - based on stable file identity, NOT FileDescriptor
    // ────────────────────────────────────────────────────────────────

    static class VFSCacheKey implements Key {
        private final String filePath;
        private final long fileLength;

        VFSCacheKey(@NonNull info.guardianproject.iocipher.File vfsFile) {
            this.filePath = vfsFile.getPath();
            this.fileLength = vfsFile.length();
        }

        @Override
        public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
            // Log.i(TAG, "updateDiskCacheKey for: " + filePath);
            try {
                messageDigest.update(filePath.getBytes(StandardCharsets.UTF_8));
                // Include length so overwrites with same path get new cache entries
                messageDigest.update(Long.toString(fileLength).getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                Log.e(TAG, "updateDiskCacheKey failed for: " + filePath, e);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof VFSCacheKey)) return false;
            VFSCacheKey that = (VFSCacheKey) o;
            return fileLength == that.fileLength && filePath.equals(that.filePath);
        }

        @Override
        public int hashCode() {
            int result = filePath.hashCode();
            result = 31 * result + Long.hashCode(fileLength);
            return result;
        }

        @NonNull
        @Override
        public String toString() {
            return "VFSCacheKey{path='" + filePath + "', len=" + fileLength + "}";
        }
    }

    // ────────────────────────────────────────────────────────────────
    // DataFetcher - opens stream on Glide's worker thread, safe lifecycle
    // ────────────────────────────────────────────────────────────────

    static class VFSDataFetcher implements DataFetcher<java.io.InputStream> {
        private final info.guardianproject.iocipher.File vfsFile;
        private volatile info.guardianproject.iocipher.FileInputStream vfsFileStream;

        VFSDataFetcher(@NonNull info.guardianproject.iocipher.File vfsFile) {
            this.vfsFile = vfsFile;
        }

        @Override
        public void loadData(@NonNull Priority priority,
                             @NonNull DataCallback<? super java.io.InputStream> callback) {
            // Log.i(TAG, "loadData for: " + vfsFile.getPath());

            try {
                // Open stream HERE on Glide's background thread, not in the holder
                vfsFileStream = new info.guardianproject.iocipher.FileInputStream(vfsFile);
                callback.onDataReady(vfsFileStream);
            } catch (Exception e) {
                Log.e(TAG, "loadData failed for: " + vfsFile.getPath(), e);
                callback.onLoadFailed(e);
            }
        }

        @Override
        public synchronized void cleanup() {
            closeStreamQuietly();
        }

        @Override
        public synchronized void cancel() {
            closeStreamQuietly();
        }

        private void closeStreamQuietly() {
            try {
                if (vfsFileStream != null) {
                    vfsFileStream.close();
                    vfsFileStream = null;
                }
            } catch (java.io.IOException e) {
                Log.w(TAG, "Stream close failed during cleanup/cancel", e);
            }
        }

        @NonNull
        @Override
        public Class<java.io.InputStream> getDataClass() {
            return java.io.InputStream.class;
        }

        @NonNull
        @Override
        public DataSource getDataSource() {
            return DataSource.LOCAL;
        }
    }
}