package com.xxp.music.servers;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.xxp.music.bean.Song;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 钟大爷 on 2016/12/28.
 */

public class MusicPlayServers extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {
    //播放方式
    public static String PLAYTYPE;
    //播放方式(列表循环)
    public static final String PLAYTYPE_LBXH = "LBXH";
    //初始化
    public static final String ACTION_INIT = "ACTION_INIT";
    //广播
    public static final String ACTION_SEND = "ACTION_SEND";
    //列表Item点击时传入
    public static final String ACTION_ITEMONCLICK = "ACTION_ITEMONCLICK";
    //播放
    public static final String ACTION_PLAY = "ACTION_PLAY";
    //暂停
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    //下一艘
    public static final String ACTION_NEXT = "ACTION_NEXT";
    //上一搜
    public static final String ACTION_UP = "ACTION_UP";
    //位置
    public static final String ACTION_SEEK = "ACTION_SEEK";
    //收到列表更改的消息
    public static final String ACTION_REMOVEED = "ACTION_REMOVEED";
    //播放器的状态
    public static int MUSICPLAYER_STATE;
    //停止
    public static final int MUSICPLAYER_STOP = 0;
    //播放中
    public static final int MUSICPLAYER_PLAYING = 1;
    //暂停中
    public static final int MUSICPLAYER_PAUSED = 2;
    private static final String TAG = "MusicPlayServers";

    //当前播放的歌曲的
    private Song currentSong;
    //上一首
    private Song upSong;
    //下一艘
    private Song nextSong;
    private static final MediaPlayer mMediaPlayer = new MediaPlayer();

    //刚开始进入软件传入的歌曲列表的KEy
    public static final String INITLIST = "INITLIST";
    //广播中正在播放的歌曲在列表中的位置
    public static final String CURRENTSONGPOSITION = "CURRENTSONGPOSITION";
    //删除歌曲后同步传入的歌曲key
    public static final String REMOVE = "REMOVE";
    //歌曲key
    public static final String SONGKEY = "SONGKEY";
    //恢复的歌曲
    public static final String RECOVERSONG = "RECOVERSONG";
    //当前进度(time)
    public static final String CURRENT_TIME = "CURRENT_TIME";
    //seek)
    public static final String SEEKBAR_MAX = "SEEKBAR_MAX";
    //seek)
    public static final String SEEKBAR_CURRENT = "SEEKBAR_CURRENT";


    //当前的Action
    private String currentAction;


    //用来装播放列表的歌曲
    private List<Song> songs;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initMediaPlayer();
        sendMyBroadcast();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_REMOVEED);
        MyReceiver myReceiver = new MyReceiver();
        registerReceiver(myReceiver, intentFilter);

    }

    //开始发广播了
    private void sendMyBroadcast() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (currentSong != null) {
                    Intent intent = new Intent();
                    intent.setAction(ACTION_SEND);
                    if (!ACTION_INIT.equals(currentAction)) {
                        currentSong.setCurrentPosition(mMediaPlayer.getCurrentPosition());
                    }
                    currentSong.setDuration(mMediaPlayer.getDuration());
                    currentSong.setKey(position);
                    intent.putExtra(SONGKEY, currentSong);
                    //intent.putExtra(CURRENTSONGPOSITION,position);
                    sendBroadcast(intent);

                }

            }
        }, 0, 500);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            //赋给当前的Action
            currentAction = intent.getAction();
            //播放的类型(当前还没有制定)
            PLAYTYPE = PLAYTYPE_LBXH;
            switch (currentAction) {
                case ACTION_INIT:
                    //获取开始传进来的歌曲列表
                    songs = (List<Song>) intent.getSerializableExtra(INITLIST);
                    currentSong = (Song) intent.getSerializableExtra(RECOVERSONG);
                    if (currentSong != null) {
                        //获得上次退出时播放的音乐在本地列表中的位置(注:刚开始默认将本歌曲加入播放列表,所以同时也是在播放列表中的位置)
                        position = currentSong.getKey();

                        Log.e(TAG, "onStartCommand: " + currentSong.toString());
                        play(currentSong);
                    }

                    Log.e(TAG, "onStartCommand: ACTION_INIT");
                    break;
                case ACTION_ITEMONCLICK:
                    position = intent.getIntExtra(SONGKEY, 0);
                    MUSICPLAYER_STATE = MUSICPLAYER_PLAYING;
                    play(songs.get(position));
                    Log.e(TAG, "onStartCommand: ACTION_ITEMONCLICK");
                    break;
                case ACTION_PLAY:
                    mMediaPlayer.start();
                    MUSICPLAYER_STATE = MUSICPLAYER_PLAYING;
                    Log.e(TAG, "onStartCommand: ACTION_PLAY");
                    break;
                case ACTION_PAUSE:
                    Log.e(TAG, "onStartCommand: ACTION_PAUSE");
                    MUSICPLAYER_STATE = MUSICPLAYER_PAUSED;
                    mMediaPlayer.pause();
                    break;
                case ACTION_NEXT:
                    MUSICPLAYER_STATE = MUSICPLAYER_PLAYING;
                    //将索引+1实现下一曲的播放
                    position++;
                    play(songs.get(position));
                    Log.e(TAG, "onStartCommand: ACTION_NEXT");
                    break;
                case ACTION_UP:
                    MUSICPLAYER_STATE = MUSICPLAYER_PLAYING;
                    //将索引-1实现下一曲的播放
                    position--;
                    play(songs.get(position));
                    Log.e(TAG, "onStartCommand: ACTION_UP");
                    break;
                case ACTION_SEEK:
                    Log.e(TAG, "onStartCommand: ACTION_SEEK");
                    mMediaPlayer.seekTo(intent.getIntExtra(SEEKBAR_CURRENT, 0));
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }


    /*
    播放列表应由MainActivity传入,
    播放列表将会在退出软件后丢失,(可在此处决定是否播放)
     */
//    private void loadPlayList() {
//        songs = mDataBaseInitImp.quire(TB_PLAYLIST);
//    }

    //准备开始播放(异步,准备完后依据currentAction判断是不是该播放)
    private void play(Song song) {
        currentSong = song;
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(currentSong.getFilePath());
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //初始化mediaplayer
    private void initMediaPlayer() {
        MUSICPLAYER_STATE = MUSICPLAYER_STOP;
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnPreparedListener(this);
    }

    //记录歌曲当前歌曲在播放列表的索引
    private int position;

    //在播放完成后 自动下一曲:
    @Override
    public void onCompletion(MediaPlayer mp) {
        nextSong = songs.get(++position);
        Log.e(TAG, "onCompletion: " + position + nextSong.toString());
        play(nextSong);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //调整进度
        mMediaPlayer.seekTo(currentSong.getCurrentPosition());
        //
        if (!ACTION_INIT.equals(currentAction)) {
            mMediaPlayer.start();

        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        Log.e(TAG, "onDestroy: ");
    }

    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            songs = (List<Song>) intent.getSerializableExtra(REMOVE);
            Log.e(TAG, "onReceive: 收到了" + songs.size());
        }
    }
}
