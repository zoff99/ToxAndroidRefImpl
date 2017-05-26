package com.zoffcc.applications.trifa;

import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;

import java.io.FileNotFoundException;
import java.io.IOException;

import info.guardianproject.iocipher.File;

import static com.zoffcc.applications.trifa.MainActivity.SD_CARD_TMP_DIR;
import static com.zoffcc.applications.trifa.MainActivity.copy_vfs_file_to_real_file;

public class FileLoader2<Data> implements ModelLoader<info.guardianproject.iocipher.File, Data>
{
    private static final String TAG = "trifa.FileLoader2";

    private final FileOpener<Data> fileOpener;

    public FileLoader2(FileOpener<Data> fileOpener)
    {
        Log.i(TAG, "FileLoader2");
        this.fileOpener = fileOpener;
    }

    @Override
    public LoadData<Data> buildLoadData(info.guardianproject.iocipher.File model, int width, int height, Options options)
    {
        Log.i(TAG, "buildLoadData");
        return new LoadData<>(new ObjectKey(model), new FileFetcher<>(model, fileOpener));
    }

    @Override
    public boolean handles(info.guardianproject.iocipher.File model)
    {
        Log.i(TAG, "handles:f=" + model);

        return true;
    }

    public interface FileOpener<Data>
    {
        Data open(info.guardianproject.iocipher.File file) throws FileNotFoundException;

        void close(Data data) throws IOException;

        Class<Data> getDataClass();
    }

    private static class FileFetcher<Data> implements DataFetcher<Data>
    {
        private final info.guardianproject.iocipher.File file;
        private java.io.File file2;
        private final FileOpener<Data> opener;
        private Data data;

        public FileFetcher(info.guardianproject.iocipher.File file, FileOpener<Data> opener)
        {
            Log.i(TAG, "FileFetcher");

            this.file = file;
            this.file2 = null;
            this.opener = opener;
        }

        @Override
        public void loadData(Priority priority, DataCallback<? super Data> callback)
        {
            Log.i(TAG, "loadData");

            try
            {
                // data = opener.open(file);
                System.out.println("fileloader2:loadData:000a:data=" + data);
                System.out.println("fileloader2:loadData:000b:data=" + (java.io.FileInputStream) data);
                System.out.println("fileloader2:loadData:001:" + file.getAbsolutePath());
                //                data = (Data) new byte[(int) file.length()];
                //                java.io.FileInputStream fis = new java.io.FileInputStream(file);
                //
                //                System.out.println("fileloader2:loadData:002");
                //                fis.read((byte[]) data, 0, (int) file.length());
                //                System.out.println("fileloader2:loadData:003");

                String temp_file_name = copy_vfs_file_to_real_file(file.getParent(), file.getName(), SD_CARD_TMP_DIR, "_2");
                System.out.println("fileloader2:loadData:000a:temp_file_name=" + temp_file_name);
                data = opener.open(new File(SD_CARD_TMP_DIR + "/" + temp_file_name));
                // data = (Data) new java.io.FileInputStream(SD_CARD_TMP_DIR + "/" + temp_file_name);
                System.out.println("fileloader2:loadData:000a:data=" + data + " file_new=" + SD_CARD_TMP_DIR + "/" + temp_file_name);
            }
            catch (Exception e)
            {
                System.out.println("fileloader2:EE:" + e.getMessage());
                e.printStackTrace();
            }
            System.out.println("fileloader2:loadData:004:onDataReady=" + data);

            callback.onDataReady(data);

            Log.i(TAG, "loadData:end");

        }

        @Override
        public void cleanup()
        {
            Log.i(TAG, "cleanup");

            if (data != null)
            {
                try
                {
                    opener.close(data);
                }
                catch (IOException e)
                {
                }
            }
        }

        @Override
        public void cancel()
        {
            Log.i(TAG, "cancel");
        }

        @Override
        public Class<Data> getDataClass()
        {
            Log.i(TAG, "getDataClass:" + opener.getDataClass());

            return opener.getDataClass();
        }

        @Override
        public DataSource getDataSource()
        {
            Log.i(TAG, "getDataSource:" + DataSource.LOCAL);

            return DataSource.LOCAL;
        }
    }

    public static class Factory<Data> implements ModelLoaderFactory<info.guardianproject.iocipher.File, Data>
    {
        private final FileOpener<Data> opener;

        public Factory(FileOpener<Data> opener)
        {
            Log.i(TAG, "Factory");

            this.opener = opener;
        }

        @Override
        public ModelLoader<info.guardianproject.iocipher.File, Data> build(MultiModelLoaderFactory multiFactory)
        {
            Log.i(TAG, "ModelLoader");

            return new FileLoader2<>(opener);
        }

        @Override
        public final void teardown()
        {
            Log.i(TAG, "teardown");
        }
    }

    public static class StreamFactory extends Factory<java.io.FileInputStream>
    {
        public StreamFactory()
        {
            super(new FileOpener<java.io.FileInputStream>()
            {
                @Override
                public java.io.FileInputStream open(File file) throws FileNotFoundException
                {
                    Log.i(TAG, "StreamFactory:open+f=" + file);

                    return new java.io.FileInputStream(file);
                }

                @Override
                public void close(java.io.FileInputStream inputStream) throws IOException
                {
                    Log.i(TAG, "StreamFactory:close:is=" + inputStream);

                    inputStream.close();
                }

                @Override
                public Class<java.io.FileInputStream> getDataClass()
                {
                    Log.i(TAG, "StreamFactory:getDataClass");

                    return java.io.FileInputStream.class;
                }
            });
        }
    }

    public static class FileDescriptorFactory extends Factory<ParcelFileDescriptor>
    {
        public FileDescriptorFactory()
        {
            super(new FileOpener<ParcelFileDescriptor>()
            {
                @Override
                public ParcelFileDescriptor open(File file) throws FileNotFoundException
                {
                    Log.i(TAG, "FileDescriptorFactory:open:f=" + file);

                    return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
                }

                @Override
                public void close(ParcelFileDescriptor parcelFileDescriptor) throws IOException
                {
                    Log.i(TAG, "FileDescriptorFactory:close:fd=" + parcelFileDescriptor);

                    parcelFileDescriptor.close();
                }

                @Override
                public Class<ParcelFileDescriptor> getDataClass()
                {
                    Log.i(TAG, "FileDescriptorFactory:getDataClass:class=" + ParcelFileDescriptor.class);

                    return ParcelFileDescriptor.class;
                }
            });
        }
    }
}