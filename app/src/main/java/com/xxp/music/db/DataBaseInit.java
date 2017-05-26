package com.xxp.music.db;

/**
 * Created by 钟大爷 on 2016/12/29.
 */

import com.xxp.music.bean.Song;

import java.util.List;

/**
 * 数据库操作的接口类
 */

public interface DataBaseInit {

    //增加
    void add(String tbName,Song song);


    //删除
    void remove(String tbName,String where,int key);


    //修改
    void modified(String tbName,Song song,String where);


    //查找
    List<Song> quire(String tbName);
}
