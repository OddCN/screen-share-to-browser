package com.oddcn.screensharetobrowser.main.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oddcn.screensharetobrowser.R;
import com.oddcn.screensharetobrowser.databinding.ItemConnBinding;
import com.oddcn.screensharetobrowser.main.viewModel.ConnViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oddzh on 2017/11/1.
 */

public class ConnAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String> connList = new ArrayList<>();

    public void setData(List<String> connList) {
        this.connList = connList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conn, parent, false);
        return new ConnBindingHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ConnBindingHolder connBindingHolder = (ConnBindingHolder) holder;
        connBindingHolder.binding.setVm(new ConnViewModel(connList.get(position)));
    }

    @Override
    public int getItemCount() {
        return connList.size();
    }

    private class ConnBindingHolder extends RecyclerView.ViewHolder {

        public ItemConnBinding binding;

        public ConnBindingHolder(View itemView) {
            super(itemView);
            binding = ItemConnBinding.bind(itemView);
        }
    }

}
