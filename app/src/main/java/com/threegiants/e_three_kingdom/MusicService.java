package com.threegiants.e_three_kingdom;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

/**
 * Created by zhangjiaqiao on 17-11-26.
 */

public class MusicService extends Service {
    public static MediaPlayer mp = new MediaPlayer(); //音乐播放服务
    private final IBinder binder = new MyBinder(); //设置与主activity

    public MusicService() {
    }
    public class MyBinder extends Binder {
            @Override
            protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
                switch (code) {
                    case 101:
                        playOrPause();
                        //播放按钮，服务处理函数
                        break;
                    case 102:
                        stop();
                        //停止按钮，服务处理函数
                        break;
                    case 103:
                        onDestroy();
                        //退出按钮，服务处理函数
                        break;
                    case 104:
                        reply.writeInt(mp.getCurrentPosition());
                        //界面刷新，服务返回数据函数
                        break;
                    case 105:
                        mp.seekTo(data.readInt());
                        //拖动进度条，服务处理函数
                        break;
                    case 106:
                        reply.writeInt(mp.getDuration());
                        //返回歌曲的时间长度
                        break;
                    default:
                        System.out.println("Default");
                        break;
                }
                return super.onTransact(code, data, reply, flags);
            }
        }

    @Override
    public IBinder onBind(Intent intent) {
            return binder; //返回引用供客户端使用
        }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            mp = MediaPlayer.create(this, R.raw.bgm_4);
            mp.setLooping(true); //设置循环播放
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
            super.onDestroy();
            if (mp != null) {
                mp.stop(); //停止播放器
                mp.release(); //释放mediaplayer资源
            }
        }

    public void playOrPause() {
            if (mp.isPlaying()) {
                mp.pause();
            } else {
                mp.start();
            }
        }

    public void stop() {
        if (mp != null) {
            mp.stop();
            try {
                mp.prepare();
                mp.seekTo(0);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
