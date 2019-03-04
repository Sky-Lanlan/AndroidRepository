package com.example.lan.mymusicapp.myMusicThread;

import android.app.Notification;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class MyMusicThread extends Thread {

    private MediaPlayer mediaPlayer;
    private Handler handler;
    private String msg;
    private Boolean isStop = false;
    private Boolean isPause = false;
    private String lock = "";
    private int total;

    @Override
    public void run() {

        total = mediaPlayer.getDuration();
        for (int i = 0; i < (total/1000+1) && !isStop; i++) {
            try {
                Thread.currentThread().sleep(995);
                synchronized (lock) {
                    if (isPause) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }

                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Log.d("MyThread", "第" + (i+ 1) + "秒");

            Message message = new Message();
            msg = i + ":" + total;
            message.obj = msg;

            handler.sendMessage(message);

        }

    }

    public MyMusicThread(MediaPlayer mediaPlayer, Handler handler) {
        this.handler = handler;
        this.mediaPlayer = mediaPlayer;
    }

    public void exitThread() {
        this.isStop = true;

    }

    public void setSuspend(Boolean isPause) {
        if (!isPause) {
            synchronized (lock) {
                lock.notifyAll();
            }
        }
        this.isPause = isPause;

    }
}
