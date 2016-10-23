package com.android.musicplayer.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import com.android.musicplayer.R;
import com.android.musicplayer.bean.Mp3Info;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016/10/18.
 */
public class MediaUtils {

    private static final String TAG = "MediaUtils";

    //获取专辑封面的Uri
    private static final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");

    /**
     * 根据歌曲ID查询歌曲信息
     *
     * @param context
     * @param _id
     * @return
     */
    private static Mp3Info getMp3Info(Context context, long _id) {
        Log.v(TAG, "" + MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media._ID + "=" + _id, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        Mp3Info mp3Info = null;

        if (cursor.moveToNext()) {
            mp3Info = new Mp3Info();
            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));//音乐ID
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));//音乐标题
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));//艺术家
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));//专辑
            long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));//专辑ID
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));//时长
            long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));//大小
            String uri = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));//音乐路径
            int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));//是否为音乐

            if (isMusic != 0) {
                mp3Info.setId(id);
                mp3Info.setTitle(title);
                mp3Info.setArtist(artist);
                mp3Info.setAlbum(album);
                mp3Info.setAlbumId(albumId);
                mp3Info.setDuration(duration);
                mp3Info.setSize(size);
                mp3Info.setUrl(uri);
            }
        }
        cursor.close();
        return mp3Info;
    }

    /**
     * 用于从数据库中查询歌曲的id信息，保存在数组中
     *
     * @param context
     * @return
     */
    public static long[] getMp3InfoIds(Context context) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Media._ID}, MediaStore.Audio.Media.DURATION + ">=180000", null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        long[] ids = null;
        if (cursor != null) {
            ids = new long[cursor.getCount()];
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                ids[i] = cursor.getLong(0);
            }
        }
        cursor.close();
        return ids;
    }

    /**
     * 用于从数据库中查询歌曲的信息，保存在ArrayList中
     *
     * @param context
     * @return
     */
    public static ArrayList<Mp3Info> getMp3Infos(Context context) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.DURATION + ">=180000", null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        ArrayList<Mp3Info> mp3Infos = new ArrayList<Mp3Info>();
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToNext();
            Mp3Info mp3Info = new Mp3Info();
            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));//专辑
            long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));//专辑ID
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));//时长
            long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));//大小
            String uri = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));//音乐路径
            int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));//是否为音乐

            if (isMusic != 0) {
                mp3Info.setId(id);
                mp3Info.setTitle(title);
                mp3Info.setArtist(artist);
                mp3Info.setAlbum(album);
                mp3Info.setAlbumId(albumId);
                mp3Info.setDuration(duration);
                mp3Info.setSize(size);
                mp3Info.setUrl(uri);
                mp3Infos.add(mp3Info);
            }
        }
        cursor.close();
        return mp3Infos;
    }


    /**
     * 时间格式化工具：将毫秒变为min
     *
     * @param time
     * @return
     */
    public static String formatTime(long time) {
        String min = time / (1000 * 60) + "";
        String sec = time % (1000 * 60) + "";
        if (min.length() < 2) {
            min = "0" + time / (1000 * 60) + "";
        } else {
            min = time / (1000 * 60) + "";
        }
        if (sec.length() == 4) {
            sec = "0" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 3) {
            sec = "00" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 2) {
            sec = "000" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 1) {
            sec = "0000" + (time % (1000 * 60)) + "";
        }
        return min + ":" + sec.trim().substring(0, 2);
    }

    /**
     * 获取默认专辑图片
     *
     * @param context
     * @param small
     * @return
     */
    public static Bitmap getDefaultArtwork(Context context, boolean small) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        if (small) {//返回小图片
            return BitmapFactory.decodeStream(context.getResources().openRawResource(R.raw.app_logo2), null, opts);
        }

        return BitmapFactory.decodeStream(context.getResources().openRawResource(R.raw.app_logo2), null, opts);

    }

    /**
     * 从文件中获取专辑封面位图
     * @param context
     * @param songId
     * @param albumId
     * @return
     */
    private static Bitmap getArtworkFromFile(Context context,long songId,long albumId){
        Bitmap bm=null;
        if (albumId<0 && songId<0){
            throw new IllegalArgumentException("Must specify an album or a song id");
        }
        try {
            BitmapFactory.Options options=new BitmapFactory.Options();
            FileDescriptor fd=null;
            if (albumId<0){
                Uri uri=Uri.parse("content://meida/external/audio/media"+songId+"/albumart");
                ParcelFileDescriptor pfd=context.getContentResolver().openFileDescriptor(uri,"r");
                if (pfd!=null){
                    fd=pfd.getFileDescriptor();
                }
            }else {
                Uri uri= ContentUris.withAppendedId(albumArtUri,albumId);
                ParcelFileDescriptor pfd=context.getContentResolver().openFileDescriptor(uri,"r");
                if (pfd!=null){
                    fd=pfd.getFileDescriptor();
                }
            }
            options.inSampleSize=1;
            //只进行大小判断
            options.inJustDecodeBounds=true;
            //调用此方法得到options得到图片大小
            BitmapFactory.decodeFileDescriptor(fd,null,options);
            //目标是在800pixel的画面上显示，所以需要调用computeSampleSize得到图片缩放的比例
            options.inSampleSize=100;
            //我们得到了缩放的比例，现在开始正式读入Bitmap数据
            options.inJustDecodeBounds=false;
            options.inDither=false;
            options.inPreferredConfig= Bitmap.Config.ARGB_8888;

            //根据options参数，减少所需要的内存
            bm=BitmapFactory.decodeFileDescriptor(fd,null,options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bm;
    }


    /**
     * 获取专辑封面位图对象
     * @param context
     * @param songId
     * @param albumId
     * @param allowDefault
     * @param small
     * @return
     */
    public static Bitmap getArtwork(Context context,long songId,long albumId,boolean allowDefault,boolean small){
        if (albumId < 0){
            if (songId <0){
                Bitmap bm=getArtworkFromFile(context,songId,-1);
                if (bm!=null){
                    return  bm;
                }
            }
            if (allowDefault) {
                return getDefaultArtwork(context, small);
            }
            return null;
        }
        ContentResolver res=context.getContentResolver();
        Uri uri=ContentUris.withAppendedId(albumArtUri,albumId);
        if (uri!=null){
            InputStream in =null;
            try{
                in=res.openInputStream(uri);
                BitmapFactory.Options options =new BitmapFactory.Options();
                //先制定原始大小
                options.inSampleSize=1;
                //只进行大小判断
                options.inJustDecodeBounds=true;
                //调用此方法得到options得到图片大小
                BitmapFactory.decodeStream(in,null,options);
                if (small){
                    options.inSampleSize=computeSampleSize(options,40);

                }else {
                    options.inSampleSize=computeSampleSize(options,600);
                }
                //得到了缩放比例，现在正式开始读入Bitmap数据
                options.inJustDecodeBounds=false;
                options.inDither=false;
                options.inPreferredConfig= Bitmap.Config.ARGB_8888;
                in=res.openInputStream(uri);
                return BitmapFactory.decodeStream(in,null,options);
            } catch (FileNotFoundException e) {
                Bitmap bm = getArtworkFromFile(context,songId,albumId);
                if (bm!=null){
                    if (bm.getConfig()==null){
                        bm=bm.copy(Bitmap.Config.RGB_565,false);
                        if (bm==null && allowDefault){
                            return getDefaultArtwork(context,small);
                        }
                    }
                } else if (allowDefault) {
                    bm = getDefaultArtwork(context,small);
                }
                return bm;
            }finally {
                try {
                    if (in!=null){
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    /**
     * 对图片进行合理的缩放
     * @param options
     * @param target
     * @return
     */
    public static int computeSampleSize(BitmapFactory.Options options,int target){
        int w=options.outWidth;
        int h=options.outHeight;
        int candidateW=w/target;
        int candidateH=h/target;
        int candidate=Math.max(candidateW,candidateH);
        if (candidate==0){
            return 1;
        }
        if (candidate>1){
            if ((w>target) && (w/candidate)<target){
                candidate=-1;
            }

        }
        if (candidate>1){
            if ((h>target) && (h/candidate)<target){
                candidate=-1;
            }

        }
        return candidate;

    }



}
