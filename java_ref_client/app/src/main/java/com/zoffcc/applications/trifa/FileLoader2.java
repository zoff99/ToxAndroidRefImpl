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

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;

import java.io.File;

import static com.zoffcc.applications.trifa.MainActivity.SD_CARD_TMP_DIR;
import static com.zoffcc.applications.trifa.MainActivity.copy_vfs_file_to_real_file;

public class FileLoader2 implements ModelLoader<info.guardianproject.iocipher.File, java.io.InputStream>
{
    private static final String TAG = "trifa.FileLoader2";

    public FileLoader2()
    {
        // Log.i(TAG, "FileLoader2");
    }

    @Override
    public LoadData<java.io.InputStream> buildLoadData(info.guardianproject.iocipher.File model, int width, int height, Options options)
    {
        Key k = new ObjectKey(model.getAbsoluteFile() + ":" + model.length());
        // Log.i(TAG, "buildLoadData:key=" + k + " model=" + model);
        return new LoadData<>(k, new MyDataFetcher(model));
    }

    @Override
    public boolean handles(info.guardianproject.iocipher.File model)
    {
        // Log.i(TAG, "handles:f=" + model);
        return true;
    }

    private class MyDataFetcher implements DataFetcher<java.io.InputStream>
    {
        info.guardianproject.iocipher.File in = null;
        String temp_file_name = null;
        long rand_num = -1L;
        long rand_num_model = -1L;
        java.io.InputStream out = null;

        public MyDataFetcher(info.guardianproject.iocipher.File model_)
        {
            // Log.i(TAG, "MyDataFetcher");
            in = model_;
            out = null;
            // rand_num_model = (long) (Math.random() * 10000d);
        }

        @Override
        public void loadData(final Priority priority, DataCallback<? super java.io.InputStream> callback)
        {
            // Log.i(TAG, "loadData:" + rand_num_model + ":" + "001");
            try
            {

                if (priority == Priority.LOW)
                {
                    // Log.i(TAG, "loadData:Priority.LOW");
                    try
                    {
                        Thread.sleep(300); // sleep 0.3s
                    }
                    catch (Exception e)
                    {
                    }
                }
                //                else if (priority == Priority.NORMAL)
                //                {
                //                    // Log.i(TAG, "loadData:Priority.NORMAL");
                //                    try
                //                    {
                //                        Thread.sleep(50); // sleep 0.05s
                //                    }
                //                    catch (Exception e)
                //                    {
                //                    }
                //                }

                // System.out.println("fileloader2:loadData:000b:data=" + in);
                // System.out.println("fileloader2:loadData:001:" + in.getAbsolutePath());

                // Log.i(TAG, "loadData:" + rand_num_model + ":" + "002");

                final Thread t = new Thread()
                {
                    @Override
                    public void run()
                    {
                        if (priority == Priority.LOW)
                        {
                            this.setPriority(Thread.MIN_PRIORITY);
                        }
                        else if (priority == Priority.NORMAL)
                        {
                            this.setPriority(Thread.NORM_PRIORITY);
                        }
                        else if (priority == Priority.HIGH)
                        {
                            this.setPriority(Thread.MAX_PRIORITY);
                        }
                        rand_num = (long) (Math.random() * 10000d);
                        temp_file_name = copy_vfs_file_to_real_file(in.getParent(), in.getName(), SD_CARD_TMP_DIR, "_glide" + "_" + rand_num);
                    }
                };
                // Log.i(TAG, "loadData:" + rand_num_model + ":" + "003");
                t.start();
                // Log.i(TAG, "loadData:" + rand_num_model + ":" + "004");
                t.join();
                // Log.i(TAG, "loadData:" + rand_num_model + ":" + "005");

                // System.out.println("fileloader2:loadData:000a:temp_file_name=" + temp_file_name);
                // new java.io.File(SD_CARD_TMP_DIR + "/" + temp_file_name);
                out = new java.io.FileInputStream(SD_CARD_TMP_DIR + "/" + temp_file_name);
                // Log.i(TAG, "loadData:" + rand_num_model + ":" + "006");
                // System.out.println("fileloader2:loadData:000a:data=" + in + " file_new=" + SD_CARD_TMP_DIR + "/" + temp_file_name);
            }
            catch (Exception e)
            {
                // System.out.println("fileloader2:EE:" + e.getMessage());
                e.printStackTrace();
            }
            // System.out.println("fileloader2:loadData:004:onDataReady=" + out);

            // Log.i(TAG, "loadData:" + rand_num_model + ":" + "007");
            callback.onDataReady(out);
            // Log.i(TAG, "loadData:" + rand_num_model + ":" + "099");
            // Log.i(TAG, "loadData:end");
        }

        @Override
        public void cleanup()
        {
            // Log.i(TAG, "cleanup:" + rand_num_model + ":" + "001");

            //            try
            //            {
            //                Thread.sleep(200); // sleep 0.2s
            //            }
            //            catch (Exception e)
            //            {
            //            }

            try
            {
                out.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "cleanup:EE1:" + e.getMessage());
            }

            try
            {
                // close stuff and remove temp files
                if (temp_file_name != null)
                {
                    new File(temp_file_name).delete();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "cleanup:EE2:" + e.getMessage());
            }

            // Log.i(TAG, "cleanup:" + rand_num_model + ":" + "099");
        }

        @Override
        public void cancel()
        {
            // this is run on the main UI thread!!
            // Log.i(TAG, "cancel");
        }

        @Override
        public Class<java.io.InputStream> getDataClass()
        {
            // Log.i(TAG, "Class:" + java.io.InputStream.class);
            return java.io.InputStream.class;
        }

        @Override
        public DataSource getDataSource()
        {
            // Log.i(TAG, "getDataSource:" + DataSource.LOCAL);
            return DataSource.LOCAL;
        }
    }

    public static class StreamFactory<Data> implements ModelLoaderFactory<info.guardianproject.iocipher.File, java.io.InputStream>
    {
        public StreamFactory()
        {
            // Log.i(TAG, "StreamFactory");
        }

        @Override
        public ModelLoader<info.guardianproject.iocipher.File, java.io.InputStream> build(MultiModelLoaderFactory multiFactory)
        {
            // Log.i(TAG, "ModelLoader");
            return new FileLoader2();
        }

        @Override
        public void teardown()
        {
            // Log.i(TAG, "teardown");
        }
    }

}