package com.xxp.music.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xxp.music.R;
import com.xxp.music.adapter.SongListAdapter;
import com.xxp.music.bean.Song;
import com.xxp.music.db.DataBaseInitImp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FileActivity extends AppCompatActivity {

    @BindView(R.id.iv_open_file)
    ImageView ivOpenFile;
    @BindView(R.id.iv_search)
    ImageView ivSearch;
    @BindView(R.id.btn_search)
    Button btnSearch;
    @BindView(R.id.tv_search)
    TextView tvSearch;
    @BindView(R.id.activity_play)
    RelativeLayout activityPlay;
    @BindView(R.id.tv_name)
    TextView tvName;

    //歌曲列表
    List<Song> songs;
    private DataBaseInitImp mDbinit;

    //本地列表
    private final String TB_LOCALLIST = "TB_LOCALLIST";

    private static final String TAG = "FileActivity";
    @BindView(R.id.rv_songlist)
    RecyclerView rvSonglist;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.obj != null) {
                songs = (List<Song>) msg.obj;
                tvSearch.setVisibility(View.INVISIBLE);
                ivSearch.setVisibility(View.INVISIBLE);
                btnSearch.setText("添加到本地播放列表");
                mSongListAdapter.notifyDataSetChanged();
                onClickType = 1;
            } else {
                tvSearch.setText("出错鸟!!!!");
            }

        }
    };
    private SongListAdapter mSongListAdapter;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        ButterKnife.bind(this);
        mContext = getApplicationContext();
        initStart();
    }
    //初始化数据库
    private void initDataBase() {
        mDbinit = DataBaseInitImp.getInstance(mContext);

    }

    private void initStart() {
        //改变标题栏字
        tvName.setText("扫描更多....");
        ivOpenFile.setVisibility(View.INVISIBLE);
        initRecylerView();
        initDataBase();
    }

    private void initRecylerView() {
        songs = new ArrayList<>();
        //歌曲列表的适配器
        mSongListAdapter = new SongListAdapter(mContext, songs);
        mSongListAdapter.setType(1);
        //设置RecyclerView的布局
        rvSonglist.setLayoutManager(new LinearLayoutManager(mContext,
                LinearLayoutManager.VERTICAL, false));
        //设置其的适配器
        rvSonglist.setAdapter(mSongListAdapter);

        mSongListAdapter.setAddOnClick(new SongListAdapter.AddOnClick() {
            @Override
            public void onClick(int position) {
                songs.remove(position);
                mSongListAdapter.notifyDataSetChanged();
            }
        });

    }

    private int onClickType = 0;
    //点击扫描
    @OnClick(R.id.btn_search)
    void onSearch() {
        if (onClickType!=1){
            getMp3Files();
        } else {
            for (int i = 0; i < songs.size(); i++) {
                mDbinit.add(TB_LOCALLIST, songs.get(i));
            }
        }

    }

    private void getMp3Files() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
                File[] files = file.listFiles();
                filterMp3File(files);
                Message msg = mHandler.obtainMessage();
                msg.obj = songs;
                mHandler.sendMessage(msg);
            }
        }).start();

    }

    private void filterMp3File(File[] files) {
        for (final File f : files) {
            if (f.isDirectory()) {
                File[] fs = f.listFiles();
                filterMp3File(fs);
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvSearch.setText("共扫描到了" + songs.size() + "首音乐" + "\n" + f.getName());
                    }
                });

                if (f.getName().contains("mp3") && f.length() > 200 * 1024) {
                    Song song = new Song();
                    song.setSongName(f.getName());
                    song.setFilePath(f.getAbsolutePath());
                    songs.add(song);

                    Log.e(TAG, "run: " + f.getName());
                }
            }
        }
    }
}
