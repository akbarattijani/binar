package com.pac.merchant.ottopay_adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.binar.component.listener.DynamicListener;

import java.util.List;

/**
 * @author AKBAR <akbar.attijani@gmail.com>
 */
public class DynamicAdapter extends RecyclerView.Adapter<DynamicAdapter.DynamicViewHolder> {
    private Context context;
    private int layout = -1;
    private int size = 0;
    private DynamicListener listener;
    private int id = -1;

    public DynamicAdapter(Context context, int size, DynamicListener listener, int layout, int id) {
        this.context = context;
        this.size = size;
        this.listener = listener;
        this.layout = layout;
        this.id = id;
    }

    public void update() {
        notifyDataSetChanged();
    }

    public void update(int size) {
        this.size = size;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public DynamicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(layout, parent, false);
        return new DynamicViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final DynamicViewHolder holder, final int position) {
        listener.onBindView(holder, position, id);
    }

    @Override
    public int getItemCount() {
        return size;
    }

    public class DynamicViewHolder extends RecyclerView.ViewHolder {
        public List<View> views;

        public DynamicViewHolder(View itemView) {
            super(itemView);
            views = listener.onViewHolder(itemView);
        }
    }
}

