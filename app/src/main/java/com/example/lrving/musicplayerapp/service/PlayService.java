package com.example.lrving.musicplayerapp.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.lrving.musicplayerapp.R;
import com.example.lrving.musicplayerapp.activity.PlayActivity;
import com.example.lrving.musicplayerapp.utils.ImageTools;
import com.example.lrving.musicplayerapp.utils.MusicIconLoader;
import com.example.lrving.musicplayerapp.utils.MusicUtils;
import com.example.lrving.musicplayerapp.utils.SpUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayService extends Service implements MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener {
    private static final String TAG =
            PlayService.class.getSimpleName();                  //类标签，用于Log();

    private MediaPlayer mPlayer;                                //媒体播放类
    private AudioManager mManager;                              //音频管理类
    private int mPlayingPosition;                               //当前播放的歌曲位置
    private RemoteViews remoteViews;                            //通知栏布局
    private Notification notification;                          //通知栏
    private OnMusicEventListener mListener;                     //音乐事件
    private NotificationManager notificationManager;            //通知管理类

    private Boolean readyNotification = false;
    private boolean isPrepared;

    private MyBroadCastReceiver receiver;
    private ExecutorService mProgressUpdatedListener = Executors.newSingleThreadExecutor();

    @Override
    public void onPrepared(MediaPlayer mp) {
        mManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        this.isPrepared = true;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        mManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    public class PlayBinder extends Binder {
        public PlayService getService() {
            return PlayService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化MediaPlayer
        mPlayer = new MediaPlayer();
        this.mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setOnCompletionListener(this);
        this.mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        //初始化AudioManager
        mManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //获取当前播放歌曲的位置
//        this.mPlayingPosition = 0;
        this.mPlayingPosition = (Integer) SpUtils.get(this, "position", 0);
        // 开始更新进度的线程
        mProgressUpdatedListener.execute(mPublishProgressRunnable);

        this.isPrepared = false;
        if (MusicUtils.sMusicList.size() != 0) {
            startNotification();
            readyNotification = true;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return new PlayBinder();
    }

    public void succeed() {
        Log.e("log", "连接服务成功");
    }

    /**
     * 更新进度的线程
     */
    private Runnable mPublishProgressRunnable = new Runnable() {
        @Override
        public void run() {
            for (; ; ) {
                if (mPlayer != null && mPlayer.isPlaying() &&
                        mListener != null) {
                    //
                    mListener.onPublish(mPlayer.getCurrentPosition());
                }

                SystemClock.sleep(200);
            }
        }
    };

    private void startNotification() {
        /**
         * 该方法虽然被抛弃过时，但是通用！
         */
        PendingIntent pendingIntent = PendingIntent
                .getActivity(PlayService.this,
                        0, new Intent(PlayService.this, PlayActivity.class), 0);
        remoteViews = new RemoteViews(getPackageName(),
                R.layout.play_notification);
        notification = new Notification(R.drawable.icon,
                "歌曲正在播放", System.currentTimeMillis());
        notification.contentIntent = pendingIntent;
        notification.contentView = remoteViews;
        //标记位，设置通知栏一直存在
        notification.flags = Notification.FLAG_ONGOING_EVENT;

        Intent intent = new Intent(PlayService.class.getSimpleName());
        intent.putExtra("BUTTON_NOTI", 1);
        PendingIntent preIntent = PendingIntent.getBroadcast(
                PlayService.this,
                1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(
                R.id.music_play_pre, preIntent);

        intent.putExtra("BUTTON_NOTI", 2);
        PendingIntent pauseIntent = PendingIntent.getBroadcast(
                PlayService.this,
                2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(
                R.id.music_play_pause, pauseIntent);

        intent.putExtra("BUTTON_NOTI", 3);
        PendingIntent nextIntent = PendingIntent.getBroadcast
                (PlayService.this,
                        3, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(
                R.id.music_play_next, nextIntent);

        intent.putExtra("BUTTON_NOTI", 4);
        PendingIntent exit = PendingIntent.getBroadcast(PlayService.this,
                4, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(
                R.id.music_play_notifi_exit, exit);

        notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        setRemoteViews();

        /**
         * 注册广播接收者
         * 功能：
         * 监听通知栏按钮点击事件
         */
        IntentFilter filter = new IntentFilter(
                PlayService.class.getSimpleName());
        this.receiver = new MyBroadCastReceiver();
        registerReceiver(this.receiver, filter);
    }

    public int play() {
        return play(this.mPlayingPosition);
    }

    /**
     * 播放
     *
     * @param position
     * @return 当前播放的位置
     */
    public int play(int position) {

        if (position < 0) position = 0;
        if (position >= MusicUtils.sMusicList.size())
            position = MusicUtils.sMusicList.size() - 1;

        if (isPlaying()) {
            pause();
        } else if (this.isPrepared) {
            this.mPlayer.start();
        } else {
            try {
                this.mPlayer.stop();
                this.mPlayer.reset();
                this.mPlayer.setDataSource(MusicUtils.sMusicList.get(position).getUri());
                this.mPlayer.prepare();
                this.mPlayingPosition = position;
                start();
                this.isPrepared = true;
                //
                if (mListener != null) mListener.onChange(this.mPlayingPosition);
            } catch (Exception e) {
                e.printStackTrace();
            }

            SpUtils.put("position", this.mPlayingPosition);

            if (!readyNotification) {
                startNotification();
            } else {
                setRemoteViews();
            }
        }
        return this.mPlayingPosition;
    }

    private void start() {
        mPlayer.start();
    }

    /**
     * 是否正在播放
     *
     * @return
     */
    public boolean isPlaying() {
        return mPlayer != null && mPlayer.isPlaying();
    }

//    /**
//     * 继续播放
//     *
//     * @return 当前播放的位置 默认为0
//     */
//    public int resume() {
//        if (isPlaying()) {
//            return -1;
//        } else if (mPlayingPosition <= 0 || mPlayingPosition >= MusicUtils.sMusicList.size()) {
//            mPlayingPosition = 0;
//            play();
//            setRemoteViews();
//            return mPlayingPosition;
//        } else {
//            mPlayer.start();
//            setRemoteViews();
//            return mPlayingPosition;
//        }
//    }

    /**
     * 暂停播放
     *
     * @return 当前播放的位置
     */
    public int pause() {
        if (isPlaying()) {
            mPlayer.pause();
            setRemoteViews();
            return mPlayingPosition;
        }
        return -1;
    }

    /**
     * 下一曲
     *
     * @return 当前播放的位置
     */
    public int next() {
        this.mPlayingPosition++;
        if (this.mPlayingPosition > MusicUtils.sMusicList.size() - 1) {
            this.mPlayingPosition = 0;
        }
        if (this.mPlayer != null) {
            this.mPlayer.stop();
            this.isPrepared = false;
        }
        setRemoteViews();
        return play(this.mPlayingPosition);
    }

    /**
     * 上一曲
     *
     * @return 当前播放的位置
     */
    public int pre() {
        this.mPlayingPosition--;
        if (this.mPlayingPosition < 0) {
            this.mPlayingPosition = MusicUtils.sMusicList.size() - 1;
        }
        if (this.mPlayer != null) {
            this.mPlayer.stop();
            this.isPrepared = false;
        }
        setRemoteViews();
        this.isPrepared = false;
        return play(this.mPlayingPosition);
    }

    /**
     * 获取正在播放的位置
     *
     * @return
     */
    public int getPlayingPosition() {
        return this.mPlayingPosition;
    }

    /**
     * 设置回调
     *
     * @param l
     */
    public void setOnMusicEventListener(OnMusicEventListener l) {
        mListener = l;
    }

    @Override
    public void onCompletion(MediaPlayer mPlayer) {
        next();
    }

    @Override
    public void onDestroy() {
        this.mPlayer.release();
        this.mManager.abandonAudioFocus(this);
        unregisterReceiver(this.receiver);
        super.onDestroy();
    }

    /**
     * 音乐播放回调接口
     */
    public interface OnMusicEventListener {
        void onPublish(int percent);
        void onChange(int position);
    }

    public void seek(int progress) {
        if (this.mPlayer != null) {
            if (!this.isPrepared) {
                play(this.mPlayingPosition);
            }
            this.mPlayer.seekTo(progress);
        }
    }

    public void setRemoteViews() {
        remoteViews.setTextViewText(R.id.music_name,
                MusicUtils.sMusicList.get(
                        getPlayingPosition()).getTitle());
        remoteViews.setTextViewText(R.id.music_author,
                MusicUtils.sMusicList.get(
                        getPlayingPosition()).getArtist());
        Bitmap icon = MusicIconLoader.getInstance().load(
                MusicUtils.sMusicList.get(
                        getPlayingPosition()).getImage());
        remoteViews.setImageViewBitmap(R.id.music_icon, icon == null
                ? ImageTools.scaleBitmap(R.drawable.icon)
                : ImageTools
                .scaleBitmap(icon));
        if (isPlaying()) {
            remoteViews.setImageViewResource(R.id.music_play_pause,
                    R.drawable.btn_notification_player_stop_normal);
        } else {
            remoteViews.setImageViewResource(R.id.music_play_pause,
                    R.drawable.btn_notification_player_play_normal);
        }
        //通知栏更新
        notificationManager.notify(5, notification);
    }

    private class MyBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    PlayService.class.getSimpleName())) {
                switch (intent.getIntExtra("BUTTON_NOTI", 0)) {
                    case 1:
                        pre();
                        break;
                    case 2:
                        play();
                        break;
                    case 3:
                        next();
                        break;
                    case 4:
                        if (isPlaying()) {
                            pause();
                        }
                        //取消通知栏
                        notificationManager.cancel(5);
                        break;
                    default:
                        break;
                }
            }
            if (mListener != null) {
                mListener.onChange(getPlayingPosition());
            }
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (isPlaying()) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                    this.mPlayer.pause();
                    break;
                }
                case AudioManager.AUDIOFOCUS_GAIN: {
                    this.mPlayer.start();
                    break;
                }
                case AudioManager.AUDIOFOCUS_LOSS: {
                    this.mPlayer.stop();
                    break;
                }
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    // 失去音频焦点，无需停止播放，降低声音即可
                    this.mPlayer.setVolume(0.1f, 0.1f);
                    break;
            }
        }
    }

    public int getUsedTime() {
        if (this.isPrepared) {
            return this.mPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if (this.isPrepared) {
            return MusicUtils.sMusicList.get(this.mPlayingPosition).getLength();
        }
        return 0;
    }

}
