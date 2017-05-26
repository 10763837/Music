package com.xxp.music;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xxp.music.activity.FileActivity;
import com.xxp.music.adapter.SongListAdapter;
import com.xxp.music.bean.Song;
import com.xxp.music.db.DataBaseInitImp;
import com.xxp.music.provider.LocalSongProvider;
import com.xxp.music.servers.MusicPlayServers;
import com.xxp.music.utils.AppUtils;
import com.xxp.music.utils.BlurImageview;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.os.Build.VERSION_CODES.M;


@RequiresApi(api = M)
public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, RecyclerView.OnScrollChangeListener {

    //本地歌曲列表
    @BindView(R.id.rv_locallist)
    RecyclerView rvLocalList;
    //播放列表
    @BindView(R.id.rv_playlist)
    RecyclerView rvPlayList;
    //当前的时间
    @BindView(R.id.id_curr_time)
    TextView currTime;

    @BindView(R.id.id_seekBar)
    SeekBar seekBar;
    //歌曲的最大的时间
    @BindView(R.id.id_total_time)
    TextView totalTime;
    //头像
    @BindView(R.id.iv_up)
    ImageView photo;
    //歌曲的名字
    @BindView(R.id.tv_title)
    TextView tvtitle;
    //演唱者
    @BindView(R.id.tv_slrc)
    TextView tvlrc;
    //播放
    @BindView(R.id.iv_play)
    ImageView play;

    //播放列表
    @BindView(R.id.id_drawerlayout)
    DrawerLayout drawerLayout;
    @BindView(R.id.fl)
    FrameLayout frameLayout;
    @BindView(R.id.iv_go_where)
    FloatingActionButton ivGoWhere;
    private static final String TAG = "MainActivity";


    //用来装本地的歌曲
    private List<Song> localList;
    //current的posi
    private int mPosition;
    private static final String TAG = "MainActivity";
    private DataBaseInitImp mDbinit;

    //本地列表
    private final String TB_LOCALLIST = "TB_LOCALLIST";
    //播放列表
    private final String TB_PLAYLIST = "TB_PLAYLIST";
    //记录上次播放
    private final String TB_PLAYRECORD = "TB_PLAYRECORD";
    //记录收藏
    private final String TB_COLLECT = "TB_COLLECT";
    private MyReceiver mMyReceiver;
    private String mSongName;
    private String mSinger;
    private String mAlbum;
    private int mDuration;
    private int mCurrentPosition;
    private Song mCurrentSong;
    private Context mContext = null;

    //播放列表
    private List<Song> playList;

    //第一次进
    private boolean isFrist;
    private SongListAdapter mPlayListAdapter;
    private SongListAdapter mLocalListAdapter;

    //当前正在播放的音乐

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawable_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        ButterKnife.bind(this);
        mContext = getApplicationContext();

        //数据库的初始化
        initDataBase();
        //本地歌曲列表的加载
        initLocalList();
        //配置文件的加载
        loadConfig();
        //播放列表的加载
        initPlayList();
        //服务的初始化
        initServers();

        //注册我们的广播
        registerMyReceiver();

        //对控件的事件监听
        forListener();

        //对定位按钮进行隐藏
        hideGoWhere();


    }

    private void hideGoWhere() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ivGoWhere.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }, 0, 8000);
    }

    private void initPlayList() {

        //默认情况下,将本地的歌曲全部加入播放列表
        if (localList == null) {
            playList = new ArrayList<>();
        } else {
            playList = localList;
        }

        //歌曲列表的适配器
        mPlayListAdapter = new SongListAdapter(mContext, playList);
        mPlayListAdapter.setType(1);
        //设置RecyclerView的布局
        rvPlayList.setLayoutManager(new LinearLayoutManager(mContext,
                LinearLayoutManager.VERTICAL, false));
        //设置其的适配器
        rvPlayList.setAdapter(mPlayListAdapter);

        //点击播放
        mPlayListAdapter.setItemOnClick(new SongListAdapter.ItemOnClick() {
            @Override
            public void onClick(int position) {
                mPosition = position;
                Intent intent = new Intent(mContext, MusicPlayServers.class);
                intent.putExtra(MusicPlayServers.SONGKEY, mPosition);
                intent.setAction(MusicPlayServers.ACTION_ITEMONCLICK);
                startService(intent);
            }
        });

        //此处为remove操作
        mPlayListAdapter.setAddOnClick(new SongListAdapter.AddOnClick() {
            @Override
            public void onClick(int position) {
                Log.e(TAG, "onClick: " + playList.get(position).toString());
                syncSongData(position);
                Toast.makeText(mContext, "移除成功!", Toast.LENGTH_SHORT).show();

            }
        });


    }

    //对上次退出时的正在播放的歌曲进行记录的恢复
    private Song recoverSong() {

        List<Song> recoverSongs = mDbinit.quire(TB_PLAYRECORD);
        if (recoverSongs.size() > 0) {

            mCurrentSong = recoverSongs.get(0);
            //设置歌曲的名字
            tvtitle.setText(mCurrentSong.getSongName());
            //设置歌曲的singer
            tvlrc.setText(mCurrentSong.getSinger());
            //当前进度
            mCurrentPosition = mCurrentSong.getCurrentPosition();
            //获得歌曲在列表中的位置
            mPosition = mCurrentSong.getKey();

            currTime.setText(AppUtils.toTime(mCurrentPosition));
            totalTime.setText(AppUtils.toTime(mDuration));
            seekBar.setMax(mDuration);
            seekBar.setProgress(mCurrentPosition);

            mPosition = mCurrentSong.getKey();

            rvLocalList.scrollToPosition(mPosition > 3 ? mPosition - 3 : mPosition);
            rvPlayList.scrollToPosition(mPosition > 3 ? mPosition - 3 : mPosition);
            Log.e(TAG, "recoverSong: " + mCurrentSong.toString());
        }
        return mCurrentSong;
    }

    private void forListener() {
        seekBar.setOnSeekBarChangeListener(this);
        rvLocalList.setOnScrollChangeListener(this);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                Log.e(TAG, "onDrawerSlide: "+slideOffset);

            }

            @Override
            public void onDrawerOpened(View drawerView) {

                Log.e(TAG, "onDrawerOpened");
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                Log.e(TAG, "onDrawerClosed");
                frameLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                Log.e(TAG, "onDrawerStateChanged");
            }
        });

    }

    private void loadConfig() {
        SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (sharedPreferences.getBoolean("isFirst", true)) {
            List<Song> initSong = LocalSongProvider.getLocalSongs(mContext);
            editor.putBoolean("isFirst", false);
            editor.commit();
            isFrist = false;
            if (initSong.size() > 0) {
                for (int i = 0; i < initSong.size(); i++) {
                    mDbinit.add(TB_LOCALLIST, initSong.get(i));
                }
                localList.addAll(mDbinit.quire(TB_LOCALLIST));
                mLocalListAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(mContext, "空空如也,没有歌曲呢!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //初始化数据库
    private void initDataBase() {
        mDbinit = DataBaseInitImp.getInstance(mContext);

    }

    //注册服务
    private void registerMyReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicPlayServers.ACTION_SEND);
        mMyReceiver = new MyReceiver();
        registerReceiver(mMyReceiver, intentFilter);
    }

    private void initServers() {
        //进入软件直接开启服务
        Intent intent = new Intent(mContext, MusicPlayServers.class);
        intent.setAction(MusicPlayServers.ACTION_INIT);
        Log.e(TAG, "initServers: " + localList.size());
        intent.putExtra(MusicPlayServers.INITLIST, (Serializable) localList);
        intent.putExtra(MusicPlayServers.RECOVERSONG, recoverSong());
        startService(intent);
    }

    //播放按钮
    @OnClick(R.id.iv_play)
    void onPlay() {
        play();

    }

    //播放上一曲
    @OnClick(R.id.iv_up)
    void onUp(){
        Intent intent = new Intent(mContext, MusicPlayServers.class);
        intent.setAction(MusicPlayServers.ACTION_UP);
        startService(intent);
    }

    //定位的按钮
    @OnClick(R.id.iv_go_where)
    void onGoWhere() {
        rvLocalList.scrollToPosition(mPosition > 3 ? mPosition - 3 : mPosition);
    }

    //播放下一曲
    @OnClick(R.id.iv_next)
    void onNext() {
        Intent intent = new Intent(mContext, MusicPlayServers.class);
        intent.setAction(MusicPlayServers.ACTION_NEXT);
        startService(intent);
    }

    //打开播放菜单
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @OnClick(R.id.iv_openlist)
    void onOpenPlayList() {

        blurBitmap();
        drawerLayout.openDrawer(Gravity.RIGHT);

        //歌曲定位
        //注-3是为了尽量的居中,便于用户的观察
        rvPlayList.scrollToPosition(mPosition > 3 ? mPosition - 3 : mPosition);


    }

    //打开本地文件()
    @OnClick(R.id.iv_open_file)
    void onOpenFile(){
        startActivity(new Intent(mContext, FileActivity.class));
    }

    private void blurBitmap() {
        //背景的模糊
        View view = getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = view.getDrawingCache();
        Matrix matrix = new Matrix();
        matrix.postScale(0.5f,0.5f);
        bitmap = Bitmap.createBitmap(bitmap,0,0,720,1280,matrix,false);
        frameLayout.setVisibility(View.INVISIBLE);
        drawerLayout.setBackground(BlurImageview.BlurImages(bitmap, mContext));
    }

    //播放音乐
    private void play() {
        switch (MusicPlayServers.MUSICPLAYER_STATE) {
            case MusicPlayServers.MUSICPLAYER_STOP:
                Intent intentPlay = new Intent(mContext, MusicPlayServers.class);
                intentPlay.setAction(MusicPlayServers.ACTION_PLAY);
                startService(intentPlay);
                break;
            case MusicPlayServers.MUSICPLAYER_PAUSED:
                Intent intentPlay_ = new Intent(mContext, MusicPlayServers.class);
                intentPlay_.setAction(MusicPlayServers.ACTION_PLAY);
                startService(intentPlay_);
                break;
            case MusicPlayServers.MUSICPLAYER_PLAYING:
                Intent intentPause = new Intent(mContext, MusicPlayServers.class);
                intentPause.setAction(MusicPlayServers.ACTION_PAUSE);
                startService(intentPause);
                break;
        }
    }

    //暂停
    private void pause() {

    }

    //上一首
    private void up() {

    }


    //开始加载本地的歌曲列表
    private void initLocalList() {

        //获得本地歌曲的数据
        //localList = LocalSongProvider.getLocalSongs(mContext);
        localList = mDbinit.quire(TB_LOCALLIST);
        //歌曲列表的适配器
        mLocalListAdapter = new SongListAdapter(mContext, localList);
        //设置RecyclerView的布局
        rvLocalList.setLayoutManager(new LinearLayoutManager(mContext,
                LinearLayoutManager.VERTICAL, false));
        //设置其的适配器
        rvLocalList.setAdapter(mLocalListAdapter);

        //点击播放
        mLocalListAdapter.setItemOnClick(new SongListAdapter.ItemOnClick() {
            @Override
            public void onClick(int position) {
                mPosition = position;
                Intent intent = new Intent(mContext, MusicPlayServers.class);
                intent.putExtra(MusicPlayServers.SONGKEY, mPosition);
                intent.setAction(MusicPlayServers.ACTION_ITEMONCLICK);
                startService(intent);
            }
        });


        //添加到播放列表
        mLocalListAdapter.setAddOnClick(new SongListAdapter.AddOnClick() {
            @Override
            public void onClick(int position) {
                Log.e(TAG, "onClick: "+localList.size() );
                Toast.makeText(MainActivity.this, "添加到下一曲成功!", Toast.LENGTH_SHORT).show();
                Song song = localList.get(position);
                playList.add(mPosition + 1,song );
                Log.e(TAG, "onClick: "+mPosition );
                Log.e(TAG, "onClick: "+localList.size() );
                mPlayListAdapter.notifyDataSetChanged();

                Intent intent = new Intent();
                intent.setAction(MusicPlayServers.ACTION_REMOVEED);
                intent.putExtra(MusicPlayServers.REMOVE, (Serializable) playList);
                sendBroadcast(intent);
            }
        });
        //移除给歌曲
        mLocalListAdapter.removeItem(new SongListAdapter.remove() {
            @Override
            public void removeSong(final int position) {
                final AlertDialog.Builder aBuilder = new AlertDialog.Builder(MainActivity.this);
                aBuilder.setTitle("警告")
                        .setMessage("你确定要删除??\n不会删除本地文件!")
                        .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                syncSongData(position);

                            }
                        })
                        .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        })
                        .show();

            }
        });
    }


    //删除时同步本地的音乐列表
    private void syncSongData(final int position) {
        localList.remove(position);
        mLocalListAdapter.notifyDataSetChanged();
        playList.remove(position);
        mPlayListAdapter.notifyDataSetChanged();
        new Thread(new Runnable() {
            @Override
            public void run() {
                mDbinit.remove(TB_LOCALLIST, null, playList.get(position).getKey());
//                //每次删除完后,将key和本地的歌曲列表的position保持一致
//                keepPositionAndKey();
                Intent intent = new Intent();
                intent.setAction(MusicPlayServers.ACTION_REMOVEED);
                intent.putExtra(MusicPlayServers.REMOVE, (Serializable) playList);
                sendBroadcast(intent);
            }
        }).start();
    }

    private void keepPositionAndKey() {
        mDbinit.remove(TB_LOCALLIST, null, 0);
        for (int i = 0; i < localList.size(); i++) {
            Song song = localList.get(i);
            song.setKey(i);
            mDbinit.add(TB_LOCALLIST, song);
        }


    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    //歌曲进度的调节
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Intent intent = new Intent(mContext, MusicPlayServers.class);
        intent.putExtra(MusicPlayServers.SEEKBAR_CURRENT, seekBar.getProgress());
        intent.setAction(MusicPlayServers.ACTION_SEEK);
        startService(intent);
    }

    //在RecyclerView进行滑动的时候,将定位按钮显示出来,
    @Override
    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        ivGoWhere.setVisibility(View.VISIBLE);
    }


    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            mCurrentSong = (Song) intent.getSerializableExtra(MusicPlayServers.SONGKEY);
            //当前歌曲在列表中的位置(播放列表)
            mPosition = mCurrentSong.getKey();
            //歌曲名字
            mSongName = mCurrentSong.getSongName();
            //歌手
            mSinger = mCurrentSong.getSinger();
            //专辑
            mAlbum = mCurrentSong.getAlbum();
            //长度
            mDuration = mCurrentSong.getDuration();
            //当前进度
            mCurrentPosition = mCurrentSong.getCurrentPosition();

            tvtitle.setText(mCurrentSong.getSongName());
            tvlrc.setText(mCurrentSong.getSinger());
            currTime.setText(AppUtils.toTime(mCurrentPosition));
            totalTime.setText(AppUtils.toTime(mDuration));
            seekBar.setMax(mDuration);
            seekBar.setProgress(mCurrentPosition);

            //对于播放键的图片的自动调整
            switch (MusicPlayServers.MUSICPLAYER_STATE) {
                case MusicPlayServers.MUSICPLAYER_STOP:
                    play.setImageResource(R.drawable.ic_play_circle);
                    break;
                case MusicPlayServers.MUSICPLAYER_PAUSED:
                    play.setImageResource(R.drawable.ic_play_circle);
                    break;
                case MusicPlayServers.MUSICPLAYER_PLAYING:
                    play.setImageResource(R.drawable.ic_pause);

                    break;
            }

        }
    }

    //销毁时解除注册
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mMyReceiver);
    }

    //当前时间
    private long currentime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - currentime) > 2000) {
                Toast.makeText(this, "再按一次退出!", Toast.LENGTH_SHORT).show();
                currentime = System.currentTimeMillis();

            } else {
                mDbinit.add(TB_PLAYRECORD, mCurrentSong);
                //退出时解除广播的注册
                unregisterReceiver(mMyReceiver);

                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
