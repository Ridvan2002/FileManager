package com.codecx.simplefilemanager.backgroundworkers;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.webkit.MimeTypeMap;

import com.codecx.simplefilemanager.callbacks.FileInterface;
import com.codecx.simplefilemanager.modelclass.FileManagerModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class FileUtil extends AsyncTask<String, Void, ArrayList<FileManagerModel>> {
    private Context mContext;
    private FileInterface mFileCallBack;

    public FileUtil(Context mContext, FileInterface mFiles) {
        this.mContext = mContext;
        this.mFileCallBack = mFiles;
    }

    private String getMimeType(Uri uri) {
        String mimeType = null;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            ContentResolver cr = mContext.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(
                    uri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase(Locale.getDefault()));
        }
        return mimeType;
    }

    private ArrayList<FileManagerModel> loadFiles(String path) {
        ArrayList<FileManagerModel> myModelList = new ArrayList<>();
        File mFile = new File(path);
        File[] mFilesList = mFile.listFiles();
        if (mFilesList != null) {
            if (mFilesList.length > 0) {
                for (File file : mFilesList) {
                    FileManagerModel mModel = new FileManagerModel();
                    mModel.setDirectory(file.isDirectory());
                    mModel.setFile(file);
                    mModel.setFileName(file.getName());
                    if (file.isDirectory()) {
                        if (file.listFiles() != null) {
                            mModel.setFileCount(file.listFiles().length);
                        }
                    }
                    mModel.setMimeType(getMimeType(Uri.parse(file.getAbsolutePath())));
                    mModel.setFileImages();
                    myModelList.add(mModel);
                }
            }
        }
        return myModelList;
    }

    @Override
    protected ArrayList<FileManagerModel> doInBackground(String... strings) {
        return loadFiles(strings[0]);
    }

    @Override
    protected void onPostExecute(ArrayList<FileManagerModel> fileManagerModels) {
        super.onPostExecute(fileManagerModels);
        mFileCallBack.result(fileManagerModels);
    }
}
