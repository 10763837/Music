package com.xxp.music.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 钟大爷 on 2016/12/29.
 */

public class DataBaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "music";
    private static final int VERSION = 1;
    //语句头
    private final String SQL_HEARD = "CREATE TABLE IF NOT EXISTS ";
    //主键设置
    private final String SQL_PRIMARY = "_ID INTEGER PRIMARY KEY,";
    //本地列表
    private final String TB_LOCALLIST = "TB_LOCALLIST";
    //播放列表
    private final String TB_PLAYLIST = "TB_PLAYLIST";
    //记录上次播放
    private final String TB_PLAYRECORD = "TB_PLAYRECORD";
    //记录收藏
    private final String TB_COLLECT = "TB_COLLECT";


    public DataBaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override

    public void onCreate(SQLiteDatabase db) {
        //创建表(本地列表)
        db.execSQL(SQL_HEARD + TB_LOCALLIST + "(" + SQL_PRIMARY + "songName varchar(20)," +
                "singer varchar(20),album varchar(20),filePath varchar(50),key integer" + ")");
        //创建表(播放列表)
        db.execSQL(SQL_HEARD + TB_PLAYLIST + "(" + SQL_PRIMARY + "songName varchar(20)," +
                "singer varchar(20),album varchar(20),filePath varchar(50),key integer" + ")");

        //记录播放
        db.execSQL(SQL_HEARD + TB_PLAYRECORD + "(" + SQL_PRIMARY + "songName varchar(20)," +
                "singer varchar(20),album varchar(20),filePath varchar(50)," +
                "key integer,currentPosition integer,duration integer" + ")");

        //记录收藏
        db.execSQL(SQL_HEARD + TB_COLLECT + "(" + SQL_PRIMARY + "songName varchar(20)," +
                "singer varchar(20),album varchar(20),fileuri varchar(50)" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
