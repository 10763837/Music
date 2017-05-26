package com.xxp.music.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xxp.music.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by 钟大爷 on 2016/12/28.
 */
public class MyViewHolder  extends RecyclerView.ViewHolder {

    //标题
    @BindView(R.id.item_title)
    public TextView title;
    //演唱者
    @BindView(R.id.item_art)
    public TextView art;
    //序号
    @BindView(R.id.item_id)
    public TextView id;
    @BindView(R.id.iv_add)
    ImageView iv_add;


    public MyViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }
}
