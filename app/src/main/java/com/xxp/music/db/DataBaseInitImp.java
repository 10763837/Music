package com.xxp.music.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.xxp.music.bean.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 钟大爷 on 2016/12/29.
 */

public class DataBaseInitImp implements DataBaseInit {
    private Context mContext;
    private DataBaseHelper mDataBaseHelper;
    private SQLiteDatabase mSQLiteDatabase;
    //公开数据的key
    public final String SONGNAME = "songName";
    public final String SINGER = "singer";
    public final String ALBUM = "album";
    public final String FILEPATH = "filePath";
    public final String CURRENTPOSITION = "CurrentPosition";
    public final String DURATION = "Duration";
    public final String KEY = "key";
    private static DataBaseInitImp mDataBaseInitImp = null;
    //本地
    private final String TB_LOCALLIST ="TB_LOCALLIST";
    //播放列表
    private final String TB_PLAYLIST ="TB_PLAYLIST";
    //记录上次播放
    private final String TB_PLAYRECORD ="TB_PLAYRECORD";
    //记录收藏
    private final String TB_COLLECT ="TB_COLLECT";
    private static final String TAG = "DataBaseInitImp";


    //私有化构造方法
    private DataBaseInitImp(Context mContext) {
        this.mContext = mContext;
        mDataBaseHelper = new DataBaseHelper(mContext);
        mSQLiteDatabase = mDataBaseHelper.getWritableDatabase();
    }

    //但例
    public static DataBaseInitImp getInstance(Context mContext){
        if(mDataBaseInitImp!=null){
            return mDataBaseInitImp;
        } else {
            synchronized(DataBaseInitImp.class){
                return new DataBaseInitImp(mContext);
            }
        }
    }

    @Override
    public void add(String tbName,Song song) {
        Log.e(TAG, "add: "+song.toString() );
        ContentValues contentValues = new ContentValues();
        contentValues.put(SONGNAME,song.getSongName());
        contentValues.put(SINGER,song.getSinger());
        contentValues.put(ALBUM,song.getAlbum());
        contentValues.put(FILEPATH,song.getFilePath());
        contentValues.put(CURRENTPOSITION,song.getCurrentPosition());
        contentValues.put(DURATION,song.getDuration());
        contentValues.put(KEY,song.getKey());
        switch (tbName) {
            //移除多余的项
            case TB_PLAYLIST:
                contentValues.remove(CURRENTPOSITION);
                contentValues.remove(DURATION);
                break;
            case TB_LOCALLIST:
                contentValues.remove(DURATION);
                contentValues.remove(CURRENTPOSITION);
                break;
            case TB_PLAYRECORD:
                mSQLiteDatabase.execSQL("DELETE FROM "+TB_PLAYRECORD);
                break;
        }
        mSQLiteDatabase.insert(tbName,null,contentValues);

    }

    @Override
    public void remove(String tbName,String where,int key) {
        mSQLiteDatabase.delete(tbName,key+"",null);
    }

    @Override
    public void modified(String tbName,Song song,String where) {
        switch (tbName){
            case TB_PLAYRECORD:

                break;
        }
    }

    @Override
    public List<Song> quire(String tbName) {
        Song song = null;
        List<Song> songs = new ArrayList<>();
        Cursor cursor = mSQLiteDatabase.query(tbName,null,null,null,null,null,null);
        while (cursor.moveToNext()){
            song = new Song();
            song.setSongName(cursor.getString(1));
            song.setSinger(cursor.getString(2));
            song.setAlbum(cursor.getString(3));
            song.setFilePath(cursor.getString(4));
            song.setKey(cursor.getInt(5));
            switch (tbName){
                case TB_PLAYRECORD:
                    song.setKey(cursor.getInt(5));
                    song.setCurrentPosition(cursor.getInt(6));
                    song.setDuration(cursor.getInt(7));
                    break;
            }
            songs.add(song);
        }
        return songs;
    }
}
