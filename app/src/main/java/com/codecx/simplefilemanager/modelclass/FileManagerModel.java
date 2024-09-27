package com.codecx.simplefilemanager.modelclass;

import com.codecx.simplefilemanager.R;

import java.io.File;

public class FileManagerModel {
    private File file;
    private boolean isSelected = false;
    private boolean isDirectory = false;
    private Object fileImage;

    private String mimeType;


    int fileCount = 0;

    private String fileName;




    public FileManagerModel(File file, boolean isSelected, boolean isDirectory, Object fileImage) {
        this.file = file;
        this.isSelected = isSelected;
        this.isDirectory = isDirectory;
        this.fileImage = fileImage;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public int getFileCount() {
        return fileCount;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }

    public FileManagerModel() {
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public Object getFileImage() {
        return fileImage;
    }

    public void setFileImage(Object fileImage) {
        this.fileImage = fileImage;
    }

    public void setFileImages() {
        if (isDirectory) {
            setFileImage(R.drawable.baseline_folder_24);
        } else {
            setFileImage(file.getAbsolutePath());
        }
    }
}
