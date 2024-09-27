package com.codecx.simplefilemanager.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class DialogUtil extends Dialog {
    private Context mContext;

    public DialogUtil(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    public void showDialog(@NonNull View view, boolean isCancelable) {
        setUpDialog(view, isCancelable);
        show();
    }

    private void setUpDialog(View view, boolean isCancelable) {
        setContentView(view);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getWindow().setBackgroundDrawable(ContextCompat.getDrawable(mContext, android.R.color.transparent));
        setCancelable(isCancelable);
    }

    public void dismissDialog() {
        if (isShowing()) {
            dismiss();
        }
    }
}
