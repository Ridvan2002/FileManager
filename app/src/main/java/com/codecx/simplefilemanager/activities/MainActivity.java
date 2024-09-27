package com.codecx.simplefilemanager.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.codecx.simplefilemanager.R;
import com.codecx.simplefilemanager.adaptors.RecyclerAdaptor;
import com.codecx.simplefilemanager.callbacks.RecyclerItemInterface;
import com.codecx.simplefilemanager.callbacks.ResultCallBack;
import com.codecx.simplefilemanager.backgroundworkers.ActionExecuter;
import com.codecx.simplefilemanager.backgroundworkers.FileUtil;
import com.codecx.simplefilemanager.databinding.ActivityMainBinding;
import com.codecx.simplefilemanager.databinding.RenameDialogBinding;
import com.codecx.simplefilemanager.enums.ActionEnum;
import com.codecx.simplefilemanager.modelclass.FileManagerModel;
import com.codecx.simplefilemanager.utils.DialogUtil;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements RecyclerItemInterface {
    private ActivityMainBinding binding;
    private RecyclerAdaptor mAdaptor;

    private boolean isMultiSelectionOn = false;
    private boolean isCopiedEnable = false;
    private boolean isMoveEnable = false;
    private ArrayList<FileManagerModel> selectedItemList;
    private ArrayList<String> filePathList;


    private DialogUtil mDialogUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        selectedItemList = new ArrayList<>();
        mDialogUtil = new DialogUtil(this);
        initRecycler();
        if (isPermissionGranted()) {
            loadRootFiles();
        } else {
            requestForPermissions();
        }
        binding.btnDelete.setOnClickListener(view -> {
            dismissSelection();
            new ActionExecuter(ActionEnum.Delete, this, "Deleting file...", new ResultCallBack() {
                @Override
                public void onSuccess() {
                    Toast.makeText(MainActivity.this, "Files Delete successful", Toast.LENGTH_SHORT).show();
                    loadCurrentFolder();
                    disableActions();
                }

                @Override
                public void onFail() {
                    Toast.makeText(MainActivity.this, "Fail to delete files", Toast.LENGTH_SHORT).show();
                    disableActions();
                }
            }).execute(new ArrayList[]{selectedItemList});
        });
        binding.btnCopy.setOnClickListener(view -> {
            dismissSelection();
            enableCopy();
        });
        binding.btnMove.setOnClickListener(v -> {
            dismissSelection();
            enableMove();
        });
        binding.btnCancel.setOnClickListener(v -> {
            dismissSelection();
            disableActions();
        });
        binding.btnPaste.setOnClickListener(v -> {
            if (isCopiedEnable) {
                doCopy();
            } else if (isMoveEnable) {
                doMove();
            }

        });
    }

    private void loadRootFiles() {
        filePathList.add(Environment.getExternalStorageDirectory().getAbsolutePath());
        new FileUtil(this, mFiles -> mAdaptor.submitList(mFiles)).execute(new String[]{Environment.getExternalStorageDirectory().getAbsolutePath()});

    }

    private void requestForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                startActivityForResult(intent, 222);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 222);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 111) {
            if (isPermissionGranted()) {
                loadRootFiles();
            } else {
                finishAffinity();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 222) {
            if (isPermissionGranted()) {
                loadRootFiles();
            } else {
                finishAffinity();
            }
        }
    }

    private Boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void initRecycler() {
        filePathList = new ArrayList<>();
        mAdaptor = new RecyclerAdaptor(this, this);
        binding.fileRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.fileRecycler.setAdapter(mAdaptor);
    }

    @Override
    public void onItemClick(FileManagerModel model, int position) {
        if (!isMultiSelectionOn) {
            if (model.isDirectory()) {
                filePathList.add(model.getFile().getAbsolutePath());
                new FileUtil(this, mFiles -> {
                    mAdaptor.submitList(mFiles);
                }).execute(new String[]{model.getFile().getAbsolutePath()});
            } else {
                openFile(model);
            }
        } else {
            if (selectedItemList.contains(model)) {
                selectedItemList.remove(model);
            } else {
                selectedItemList.add(model);
            }
            mAdaptor.selectUnSelectItem(model, position);
        }
    }

    private void loadCurrentFolder() {
        new FileUtil(this, mFiles -> {
            mAdaptor.submitList(mFiles);
        }).execute(new String[]{filePathList.get(filePathList.size() - 1)});
    }

    @Override
    public void onItemLongClick(FileManagerModel model, int position) {
        if (!isMultiSelectionOn) {
            isMultiSelectionOn = true;

            binding.menuLayout.setVisibility(View.VISIBLE);
        }
        mAdaptor.selectUnSelectItem(model, position);
        if (selectedItemList.contains(model)) {
            selectedItemList.remove(model);
        } else {
            selectedItemList.add(model);
        }

    }

    @Override
    public void onMenuClick(FileManagerModel model, int position, View mView) {
        if (!isMultiSelectionOn) {
            PopupMenu menu = new PopupMenu(this, mView);
            menu.getMenuInflater().inflate(R.menu.item_menu, menu.getMenu());
            menu.show();
            menuItemClick(menu, model, position);
        }
    }

    private void menuItemClick(PopupMenu menu, FileManagerModel model, int position) {
        menu.setOnMenuItemClickListener(menuItem -> {
            int menuId = menuItem.getItemId();
            if (menuId == R.id.menuDelete) {
                deleteItem(model, position);
            } else if (menuId == R.id.menuCopy) {
                copyItem(model, position);
            } else if (menuId == R.id.menuMove) {
                moveItem(model, position);
            } else if (menuId == R.id.menuRename) {
                renameItem(model, position);
            }
            return false;
        });
    }

    private void renameItem(FileManagerModel model, int position) {
        RenameDialogBinding renameDialogBinding = RenameDialogBinding.inflate(getLayoutInflater());
        renameDialogBinding.txtFileName.setHint(model.getFile().getName());
        mDialogUtil.showDialog(renameDialogBinding.getRoot(), true);
        renameDialogBinding.btnRename.setOnClickListener(v -> {
            String fileName = renameDialogBinding.txtFileName.getEditText().getText().toString();
            if (fileName.isEmpty()) {
                renameDialogBinding.txtFileName.setError("Required...");
            } else {
                mDialogUtil.dismissDialog();
                if (model.getFile().exists()) {
                    String fileNameWithEx = fileName;
                    if (model.isDirectory()) {
                        fileNameWithEx = fileName;
                    } else {
                        fileNameWithEx = fileName + "." + getFileExtension(model.getFileName());
                    }
                    File to = new File(model.getFile().getParent(), fileNameWithEx);
                    if (model.getFile().renameTo(to)) {
                        loadCurrentFolder();
                        Toast.makeText(this, "Rename successful", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Fail to rename", Toast.LENGTH_SHORT).show();

                    }
                } else {
                    Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String getFileExtension(String fileName) {
        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }
        return extension;
    }

    private void moveItem(FileManagerModel model, int position) {
        if (!selectedItemList.contains(model)) {
            selectedItemList.add(model);
        }
        enableMove();
        selectedItemList.add(model);
    }

    private void doMove() {
        new ActionExecuter(ActionEnum.Move, this, "Moving files...", new ResultCallBack() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Moved successful", Toast.LENGTH_SHORT).show();
                loadCurrentFolder();
                disableActions();
            }

            @Override
            public void onFail() {
                Toast.makeText(MainActivity.this, "Fail to move", Toast.LENGTH_SHORT).show();
                disableActions();
            }
        }, filePathList.get(filePathList.size() - 1)).execute(new ArrayList[]{selectedItemList});
    }

    private void copyItem(FileManagerModel model, int position) {
        if (!selectedItemList.contains(model)) {
            selectedItemList.add(model);
        }
        enableCopy();
        selectedItemList.add(model);
    }

    private void doCopy() {
        new ActionExecuter(ActionEnum.Copy, this, "Coping file...", new ResultCallBack() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Copied successful", Toast.LENGTH_SHORT).show();
                loadCurrentFolder();
                disableActions();
            }

            @Override
            public void onFail() {
                Toast.makeText(MainActivity.this, "Fail to copy", Toast.LENGTH_SHORT).show();
                disableActions();
            }
        }, filePathList.get(filePathList.size() - 1)).execute(new ArrayList[]{selectedItemList});
    }

    private void deleteItem(FileManagerModel model, int position) {
        disableActions();
        selectedItemList.add(model);
        new ActionExecuter(ActionEnum.Delete, this, "Deleting file...", new ResultCallBack() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Delete successful", Toast.LENGTH_SHORT).show();
                mAdaptor.mList.remove(position);
                mAdaptor.notifyItemRemoved(position);
                mAdaptor.notifyItemRangeChanged(position, mAdaptor.mList.size());
                disableActions();
            }

            @Override
            public void onFail() {
                Toast.makeText(MainActivity.this, "Fail to delete", Toast.LENGTH_SHORT).show();
                disableActions();
            }
        }).execute(new ArrayList[]{selectedItemList});
    }

    private void enableMove() {

        binding.floatingLayout.setVisibility(View.VISIBLE);
        binding.tvMessage.setVisibility(View.VISIBLE);
        binding.tvMessage.setText("Select directory to move files");
        isMoveEnable = true;
        isCopiedEnable = false;

    }

    private void enableCopy() {

        binding.floatingLayout.setVisibility(View.VISIBLE);
        binding.tvMessage.setVisibility(View.VISIBLE);
        binding.tvMessage.setText("Select directory to copy files");
        isMoveEnable = false;
        isCopiedEnable = true;
    }

    private void disableActions() {
        binding.floatingLayout.setVisibility(View.GONE);
        binding.tvMessage.setVisibility(View.GONE);
        isMoveEnable = false;
        isCopiedEnable = false;
        if (!selectedItemList.isEmpty()) {
            selectedItemList.clear();
        }
    }

    @Override
    public void onBackPressed() {
        if (isMultiSelectionOn) {
            if (!selectedItemList.isEmpty()) {
                selectedItemList.clear();
            }
            dismissSelection();
            return;
        }
        if (filePathList.size() == 1) {
            super.onBackPressed();
        } else {
            goBack();
        }

    }

    private void dismissSelection() {
        isMultiSelectionOn = false;
        mAdaptor.dismissSelection();
        binding.menuLayout.setVisibility(View.GONE);
    }

    private void goBack() {
        String path = filePathList.get(filePathList.size() - 2);
        new FileUtil(this, mFiles -> {
            filePathList.remove(filePathList.size() - 1);
            mAdaptor.submitList(mFiles);
        }).execute(new String[]{path});
    }

    private void openFile(FileManagerModel mFile) {
        Intent fileIntent = new Intent(Intent.ACTION_VIEW);
        Uri uri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                mFile.getFile()
        );
        fileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        fileIntent.setDataAndType(uri, mFile.getMimeType());
        try {
            startActivity(fileIntent);
        } catch (ActivityNotFoundException ex) {
        }
    }
}