package com.example.lan.mymusicapp.playMode;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lan.mymusicapp.R;
import com.example.lan.mymusicapp.musicItem.MusicItem;
import com.example.lan.mymusicapp.myMusicThread.MyMusicThread;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;


public class Play {
    private static MyMusicThread myMusicThread;
    private static MediaPlayer mediap;
    public static String url2Save = null;
    private static String musicName = null;

    public static Boolean setPlayMode(List<MusicItem> musicItemList, int position,
                                      String url, String lastUrl, Handler handler,
                                      Boolean isDefault, ImageView control) {

        url2Save = url;
        musicName = musicItemList.get(position).getTitle();

        try {
            if (mediap != null) {
                if (url.equals(lastUrl)) {
                    if (mediap.isPlaying()) {//暂停
                        control.setImageResource(R.drawable.ic_play_btn_play);

                        mediap.pause();
                        myMusicThread.setSuspend(true);
                        return false;
                    } else {
                        control.setImageResource(R.drawable.ic_play_btn_pause);

                        mediap.start();
                        myMusicThread.setSuspend(false);
                        return true;
                    }
                } else {//切换歌曲
                    mediap.stop();
                    mediap.release();
                    mediap = new MediaPlayer();
                    mediap.setDataSource(url);//指定音频文件路径
                    mediap.prepare();//进入准备状态
                    mediap.start();
                    myMusicThread.exitThread();
                    myMusicThread = new MyMusicThread(mediap, handler);
                    myMusicThread.start();
                    control.setImageResource(R.drawable.ic_play_btn_pause);
                    return true;
                }

            } else {//第一次播放
                mediap = new MediaPlayer();
                mediap.setLooping(true);
                mediap.setDataSource(url);//指定音频文件路径
                mediap.prepare();//进入准备状态
                mediap.start();
                myMusicThread = new MyMusicThread(mediap, handler);
                myMusicThread.start();
                control.setImageResource(R.drawable.ic_play_btn_pause);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    public static void reloadMusic(Context context, Handler handler, TextView musicNameView) {
        FileInputStream in ;
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();
        String[] saved = null;
        try {
            in = context.openFileInput("data");
            reader = new BufferedReader(new InputStreamReader(in));
            String line ;
            while ((line = reader.readLine()) != null) {
                content.append(line);
                content.append('|');
            }
            String tmp = content.toString();
            saved = tmp.split("\\|");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                return;
            }
        }
        mediap = new MediaPlayer();
        mediap.setLooping(true);
        assert saved != null;
        url2Save = saved[0];
        musicName = saved[1];
        try {

            mediap.setDataSource(saved[0]);//指定音频文件路径
            musicNameView.setText(saved[1]);
            mediap.prepare();//进入准备状态


        } catch (IOException e) {
            e.printStackTrace();
        }
        myMusicThread = new MyMusicThread(mediap, handler);
        myMusicThread.start();
        myMusicThread.setSuspend(true);


    }


    public static void tPause() {
        mediap.pause();
        myMusicThread.setSuspend(true);
    }

    public static void toPlay() {
        mediap.start();
        myMusicThread.setSuspend(false);

    }

    public static void savePlayInfo(Context context) {

        FileOutputStream out ;
        BufferedWriter writer = null;
        if (url2Save == null) {
            return;
        }
        try {
            out = context.openFileOutput("data", Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(url2Save);
            writer.newLine();
            writer.write(musicName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }


}
