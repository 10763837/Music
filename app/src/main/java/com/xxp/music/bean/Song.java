package com.xxp.music.bean;

import java.io.Serializable;

/**
 * 本地歌曲
 */
public class Song implements Serializable{
	private String songName;
	private String singer;
	private String album;
	private String imgUrl;
	private int songnumber;
	private String filePath;
	private String isAddedToPlayList;
	private boolean isAdded;
	private int isStoragedByUser;	//是否已被用户收藏
	private int isHaveMV;
	private int key;

	public void setKey(int key) {
		this.key = key;
	}

	//用于长度的保存 非初始化
	//用于seekbar进度 的保存 ,,非初始化
	private int currentPosition;
	private int duration;

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(int currentPosition) {
		this.currentPosition = currentPosition;
	}



	public Song(){
		
	}

	@Override
	public String toString() {
		return "Song{" +
				"songName='" + songName + '\'' +
				", singer='" + singer + '\'' +
				", album='" + album + '\'' +
				", filePath='" + filePath + '\'' +
				", duration=" + duration +
				", currentPosition=" + currentPosition +
				", lenTime='" +  + '\'' +
				'}';
	}

	public int getIsHaveMV() {
		return isHaveMV;
	}
	public void setIsHaveMV(int isHaveMV) {
		this.isHaveMV = isHaveMV;
	}
	public int getIsStoragedByUser() {
		return isStoragedByUser;
	}
	public void setIsStoragedByUser(int isStoragedByUser) {
		this.isStoragedByUser = isStoragedByUser;
	}
	public int getKey() {
		return key;
	}
	public boolean isAdded() {
		return isAdded;
	}
	public void setAdded(boolean isAdded) {
		this.isAdded = isAdded;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getImgUrl() {
		return imgUrl;
	}
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public int getSongnumber() {
		return songnumber;
	}
	public void setSongnumber(int songnumber) {
		this.songnumber = songnumber;
	}
	public String getSongName() {
		return songName;
	}
	public void setSongName(String songName) {
		this.songName = songName;
	}
	public String getSinger() {
		return singer;
	}
	public void setSinger(String singer) {
		this.singer = singer;
	}
	public String getAlbum() {
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}
	public String getIsAddedToPlayList() {
		return isAddedToPlayList;
	}
	public void setIsAddedToPlayList(String isAddedToPlayList) {
		this.isAddedToPlayList = isAddedToPlayList;
	}
	}
