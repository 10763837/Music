package com.xxp.music.utils;

/**
 * Created by 钟大爷 on 2016/12/28.
 */

public class AppUtils {
    //long的格式转换
    public static String toTime(int time){
        int minute = time / 1000 / 60;
        int s = time / 1000 % 60;
        String mm = null;
        String ss = null;
        if(minute<10)mm = "0" + minute;
        else mm = minute + "";
        if(s <10)ss = "0" + s;
        else ss = "" + s;
        return mm + ":" + ss;
    }

//    //图片的优化
//    public static Bitmap readBitmap(Context context,Bitmap bitmap){
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.RGB_565;
//        options.inPurgeable = true;
//        return  Bitmap.createBitmap(bitmap,options);
//    }

}
