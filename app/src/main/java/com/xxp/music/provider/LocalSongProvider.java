package com.xxp.music.provider;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.xxp.music.bean.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 钟大爷 on 2016/12/28.
 */

public class LocalSongProvider {
    // 获取专辑封面的Uri
    private static final String TAG = "LocalSongProvider";

    /**
     * 用于从数据库中查询歌曲的信息，保存在List当中
     *
     * @return
     *///
    public static List<Song> getLocalSongs(Context context) {
        Cursor cursor = context.getContentResolver()
                .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        int mId = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
        int mTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int mArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int mAlbum = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
        int mAlbumID = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
        int mDuration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
        int mSize = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE);
        int mUrl = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        int mIsMusic = cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC);

        List<Song> songs = new ArrayList<>();
        for (int i = 0, p = cursor.getCount(); i < p; i++) {
            cursor.moveToNext();

            Song song = new Song();


            long id = cursor.getLong(mId); // 音乐id
            String title = cursor.getString(mTitle); // 音乐标题
            String artist = cursor.getString(mArtist); // 艺术家
            String album = cursor.getString(mAlbum); // 专辑
            long duration = cursor.getLong(mDuration); // 时长
            long size = cursor.getLong(mSize); // 文件大小
            String url = cursor.getString(mUrl); // 文件路径
            int isMusic = cursor.getInt(mIsMusic); // 是否为音乐
            if (isMusic != 0 && url.matches(".*\\.mp3$")) { // 只把音乐添加到集合当中
                songs.add(song);
                song.setSongName(title);
                song.setAlbum(album);
                song.setSinger(artist);
                song.setFilePath(url);
                song.setKey(i);

                Log.e(TAG, "getLocalSongs: "+song.toString());
            }
        }
        cursor.close();
        return songs;
    }

}
