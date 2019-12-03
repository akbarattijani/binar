package com.app.binar.component.listener;

import android.view.View;

import java.util.List;

/**
 * @author AKBAR <dicky.akbar@dwidasa.com>
 */

public interface DynamicListener {
    public void onBindView(com.pac.merchant.ottopay_adapter.DynamicAdapter.DynamicViewHolder holder, int position, int id);
    public List<View> onViewHolder(View view);
    public void onItemClick(Object... packet);
}
