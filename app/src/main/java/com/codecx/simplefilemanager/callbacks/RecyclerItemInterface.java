package com.codecx.simplefilemanager.callbacks;

import android.view.View;

import com.codecx.simplefilemanager.modelclass.FileManagerModel;

public interface RecyclerItemInterface {
    void onItemClick(FileManagerModel model, int position);

    void onItemLongClick(FileManagerModel model, int position);

    void onMenuClick(FileManagerModel model, int position, View mView);
}
