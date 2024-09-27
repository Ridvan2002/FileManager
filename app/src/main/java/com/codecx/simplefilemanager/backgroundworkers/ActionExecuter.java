package com.codecx.simplefilemanager.backgroundworkers;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.codecx.simplefilemanager.modelclass.FileManagerModel;
import com.codecx.simplefilemanager.callbacks.ResultCallBack;
import com.codecx.simplefilemanager.enums.ActionEnum;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class ActionExecuter extends AsyncTask<ArrayList<FileManagerModel>, Void, Integer> {
    private ActionEnum mType;
    private Context mContext;

    private ResultCallBack mResultCallBack;
    private ProgressDialog mProgressDialog;
    private String selectedPath;

    public ActionExecuter(ActionEnum mType, Context mContext, String message, ResultCallBack mResultCallBack) {
        this.mType = mType;
        this.mContext = mContext;
        this.mResultCallBack = mResultCallBack;
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(message);
    }

    public ActionExecuter(ActionEnum mType, Context mContext, String message, ResultCallBack mResultCallBack, String selectedPath) {
        this.mType = mType;
        this.mContext = mContext;
        this.mResultCallBack = mResultCallBack;
        this.selectedPath = selectedPath;
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(message);
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog.show();
    }

    @Override
    protected Integer doInBackground(ArrayList<FileManagerModel>... arrayLists) {
        try {
            if (mType == ActionEnum.Delete) {
                deleteFiles(arrayLists[0]);
            } else if (mType == ActionEnum.Copy) {
                copyFiles(selectedPath, arrayLists[0]);
            } else if (mType == ActionEnum.Move) {
                moveFiles(selectedPath, arrayLists[0]);

            }
            return 0;
        } catch (Exception exception) {
            Log.d("adsad", exception.getMessage());
            return 1;
        }
    }

    private void moveFiles(String selectedPath, ArrayList<FileManagerModel> arrayLists) {
        try {
            //create output directory if it doesn't exist
            File dir = new File(selectedPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            for (FileManagerModel mf : arrayLists) {
                InputStream in = null;
                OutputStream out = null;
                in = new FileInputStream(mf.getFile().getAbsolutePath());
                out = new FileOutputStream(selectedPath + "/" + mf.getFileName());
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                in = null;
                // write the output file
                out.flush();
                out.close();
                out = null;
                new File(mf.getFile().getAbsolutePath()).delete();
            }


        } catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }

    private void copyFiles(String selectedPath, ArrayList<FileManagerModel> arrayLists) {
        try {
            //create output directory if it doesn't exist
            File dir = new File(selectedPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            for (FileManagerModel mf : arrayLists) {
                InputStream in = null;
                OutputStream out = null;
                in = new FileInputStream(mf.getFile().getAbsolutePath());
                out = new FileOutputStream(selectedPath + "/" + mf.getFileName());
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                in = null;
                // write the output file
                out.flush();
                out.close();
                out = null;
            }


        } catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        if (result == 0) {
            mResultCallBack.onSuccess();
        } else {
            mResultCallBack.onFail();
        }
    }

    private void deleteFiles(ArrayList<FileManagerModel> arrayLists) {
        for (FileManagerModel file : arrayLists) {
            if (file.getFile().exists()) {
                if (file.isDirectory()) {
                    try {
                        FileUtils.deleteDirectory(file.getFile());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    file.getFile().delete();
                }
            }
        }
    }

    private void deleteDir(File file) {
        if (file.isDirectory()) {
            File[] listFiles = file.listFiles();
            if (listFiles != null) {
                if (listFiles.length > 0) {
                    for (File mf : listFiles) {
                        deleteDir(mf);
                    }
                }
            }
        } else {
            if (file.exists()) {
                file.delete();
            }
        }

    }

}
