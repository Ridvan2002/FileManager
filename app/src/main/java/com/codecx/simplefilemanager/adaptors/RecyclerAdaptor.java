package com.codecx.simplefilemanager.adaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codecx.simplefilemanager.R;
import com.codecx.simplefilemanager.callbacks.RecyclerItemInterface;
import com.codecx.simplefilemanager.databinding.ItemLayoutBinding;
import com.codecx.simplefilemanager.modelclass.FileManagerModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RecyclerAdaptor extends RecyclerView.Adapter<RecyclerAdaptor.MyViewHolder> {
    private Context mContext;
    private RecyclerItemInterface recyclerItemInterface;

    public ArrayList<FileManagerModel> mList;

    public RecyclerAdaptor(Context mContext, RecyclerItemInterface recyclerItemInterface) {
        this.mContext = mContext;
        this.recyclerItemInterface = recyclerItemInterface;
        mList = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerAdaptor.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdaptor.MyViewHolder holder, int position) {
        FileManagerModel model = mList.get(position);
        holder.binding.fileName.setText(model.getFileName());
        if (model.isSelected()) {
            holder.binding.mItem.setBackgroundColor(ContextCompat.getColor(mContext, R.color.purple_200));
        } else {
            holder.binding.mItem.setBackgroundColor(ContextCompat.getColor(mContext, android.R.color.transparent));
        }

        if (model.isDirectory()) {
            holder.binding.tvFileCountPath.setText(model.getFileCount() + " Files");
        } else {
            holder.binding.tvFileCountPath.setText(model.getFile().getAbsolutePath());
        }
        Glide.with(mContext).load(model.getFileImage()).placeholder(R.drawable.loading).error(R.drawable.baseline_insert_drive_file_24).into(holder.binding.mIcon);
        holder.binding.mItem.setOnClickListener(v -> {
            recyclerItemInterface.onItemClick(model, position);
        });
        holder.binding.mItem.setOnLongClickListener(v -> {
            recyclerItemInterface.onItemLongClick(model, position);
            return true;
        });
        holder.binding.btnMore.setOnClickListener(v -> {
            recyclerItemInterface.onMenuClick(model, position, v);
        });
    }

    public ArrayList<FileManagerModel> getList() {
        return mList;
    }

    public List<FileManagerModel> getSelectedList() {
        return mList.stream().filter(FileManagerModel::isSelected).collect(Collectors.toList());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void submitList(ArrayList<FileManagerModel> newList) {
        mList = newList;
        notifyDataSetChanged();
    }

    public void selectUnSelectItem(FileManagerModel model, int position) {
        mList.get(position).setSelected(!model.isSelected());
        notifyItemChanged(position);
    }

    public void dismissSelection() {
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).isSelected()) {
                mList.get(i).setSelected(false);
                notifyItemChanged(i);
            }
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ItemLayoutBinding binding;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemLayoutBinding.bind(itemView);
        }
    }
}
