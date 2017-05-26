package com.xxp.music.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xxp.music.R;
import com.xxp.music.bean.Song;

import java.util.List;

/**
 * Created by 钟大爷 on 2016/12/28.
 */

//歌曲列表的适配器
public class SongListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    //上下文
    private Context mContext;
    //数据
    private List<Song> songs;
    //布局加载
    private LayoutInflater mInflater;
    //列表布局的类型(默认不设置就是本地歌曲列表的Item样式),
    //因为本软件就两种类型,为了方便,咳咳咳 ,暂时提供set方法,不在构造方法进行赋值操作;
    private int type;


    public SongListAdapter(Context mContext, List<Song> songs) {
        this.mContext = mContext;
        this.songs = songs;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //暂时不处理
//        if(type == 1){
//            return new
//        }
        return new MyViewHolder(mInflater.inflate(R.layout.item_song, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        MyViewHolder holder = (MyViewHolder) viewHolder;
        if (type == 1) {
            holder.iv_add.setImageResource(R.drawable.ic_remove);
        }
        //为了美观   对前面的id进行填补
        if (position + 1 < 100) {
            if (position + 1 < 10) {
                holder.id.setText(0 + "0" + (position + 1));
            } else {
                holder.id.setText(0 + "" + (position + 1));
            }

        } else {
            holder.id.setText("" + (position + 1));
        }


        holder.title.setText(songs.get(position).getSongName());
        holder.art.setText(songs.get(position).getSinger() + " | " + songs.get(position).getAlbum());
        //
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemOnClick != null) {
                    mItemOnClick.onClick(position);
                }

            }
        });

        //添加
        holder.iv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAddItemOnClick != null) {
                    mAddItemOnClick.onClick(position);
                }

            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //防止在播放列表长按的情况
                if (mRemove != null) {
                    mRemove.removeSong(position);
                }

                return true;
            }
        });


    }

    //类型的指定
    public void setType(int type) {
        this.type = type;
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    private ItemOnClick mItemOnClick;

    public interface ItemOnClick {
        void onClick(int position);
    }

    public void setItemOnClick(ItemOnClick mItemOnClick) {
        this.mItemOnClick = mItemOnClick;
    }

    private AddOnClick mAddItemOnClick;

    public interface AddOnClick {
        void onClick(int position);
    }

    public void setAddOnClick(AddOnClick mAddItemOnClick) {
        this.mAddItemOnClick = mAddItemOnClick;
    }

    //这个是本地歌曲列表的移除接口,,,咳咳咳 播放列表为了方便暂时使用add接口进行移除
    private remove mRemove;

    public interface remove {
        void removeSong(int position);
    }

    public void removeItem(remove mRemove) {
        this.mRemove = mRemove;
    }
}
