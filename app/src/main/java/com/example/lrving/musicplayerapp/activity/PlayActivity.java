package com.example.lrving.musicplayerapp.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.lrving.musicplayerapp.R;
import com.example.lrving.musicplayerapp.application.App;
import com.example.lrving.musicplayerapp.domain.Music;
import com.example.lrving.musicplayerapp.service.PlayService;
import com.example.lrving.musicplayerapp.utils.ImageTools;
import com.example.lrving.musicplayerapp.utils.MusicIconLoader;
import com.example.lrving.musicplayerapp.utils.MusicUtils;
import com.example.lrving.musicplayerapp.view.CDView;
import com.example.lrving.musicplayerapp.view.LrcView;

import java.io.Serializable;
import java.util.List;

public class PlayActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private PlayService mPlayService;
    private int pos;

    private CDView mCdView;

    private ImageView mImagPlayBack;

    private LrcView lrcView;

    private TextView mTextMusicTitle;
    private TextView mTextArtistTitle;
    private TextView mTextUsedTime;
    private TextView mTextDuration;

    private SeekBar mPlaySeekBar;

    private ImageButton mStartPlayButton;
    private ImageButton mNextButton;
    private ImageButton mPreButton;

    Handler mUpdateHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0: {
                    mTextUsedTime.setText(transformMsec(msg.getData().getInt("progress")));
                    break;
                }
                case 1: {
                    disPlay(msg.getData().getInt("position"));
                    setLrc(msg.getData().getInt("position"));
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
//        this.pos = getIntent().getIntExtra("pos", -1);
//        getIntentPos();
        initViews();
        initEvent();
//        allowBindService();
    }

    private void initEvent() {

        this.mImagPlayBack.setOnClickListener(this);
        this.mPlaySeekBar.setOnSeekBarChangeListener(this);
        this.mStartPlayButton.setOnClickListener(this);
        this.mNextButton.setOnClickListener(this);
        this.mPreButton.setOnClickListener(this);

    }

    private void initViews() {

        mImagPlayBack = (ImageView) findViewById(R.id.iv_play_back);

        mTextMusicTitle = (TextView) findViewById(R.id.tv_music_title);
        mTextArtistTitle = (TextView) findViewById(R.id.tv_play_singer);
        mTextUsedTime = (TextView) findViewById(R.id.tv_used_time);
        mTextDuration = (TextView) findViewById(R.id.tv_duration);

        mPlaySeekBar = (SeekBar) findViewById(R.id.sb_play_progress);
        mCdView = (CDView) findViewById(R.id.play_cdView);
        lrcView = (LrcView) findViewById(R.id.play_first_lrc);

        mStartPlayButton = (ImageButton) findViewById(R.id.ib_play_start);
        mPreButton = (ImageButton) findViewById(R.id.ib_play_pre);
        mNextButton = (ImageButton) findViewById(R.id.ib_play_next);

        // 动态设置seekbar的margin
//        ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) mPlaySeekBar.getLayoutParams();
//        p.leftMargin = (int) (App.appScreenWidth * 0.1);
//        p.rightMargin = (int) (App.appScreenWidth * 0.1);

    }

    private void disPlay(int position) {
        Music music = MusicUtils.sMusicList.get(position);

        mTextMusicTitle.setText(music.getTitle());
        mTextArtistTitle.setText(music.getArtist());
        mTextDuration.setText(transformMsec(music.getLength()));
        mPlaySeekBar.setMax(music.getLength());
        Bitmap bmp = MusicIconLoader.getInstance().load(music.getImage());
        if (bmp == null) bmp = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
        mCdView.setImage(ImageTools.scaleBitmap(bmp, (int) (App.appScreenWidth * 0.4)));
        mCdView.reset();
        if (mPlayService.isPlaying()) {
            mCdView.start();
            mStartPlayButton.setImageResource(R.drawable.player_btn_pause_normal);
        } else {
            mCdView.pause();
            mStartPlayButton.setImageResource(R.drawable.player_btn_play_normal);
        }
    }

    private void getIntentPos() {
        // TODO Auto-generated method stub
        Intent intent = getIntent();
        this.pos = intent.getIntExtra("pos", -1);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        allowUnbindService();
    }

    @Override
    protected void onResume() {
        allowBindService();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        allowUnbindService();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        allowBindService();
        Intent intent = getIntent();
        Intent serviceIntent = new Intent(this, PlayService.class);
        serviceIntent.setAction(intent.getAction());
        serviceIntent.putExtra("musicList", intent.getSerializableExtra("musicList"));
        this.pos = intent.getExtras().getInt("pos", -1);
        serviceIntent.putExtra("pos", this.pos);
        startService(serviceIntent);
    }

    /**
     * 上一曲
     */
    public void pre() {
        this.mCdView.pause();
//        this.mCdView.reset();
        this.mPlayService.pre(); // 上一曲
    }

    /**
     * 播放
     */
    public void play() {
//        this.mPlayService.pause(); // 暂停
//        this.mStartPlayButton.setImageResource(R.drawable.player_btn_play_normal);
//        this.mCdView.pause();
        this.mPlayService.play();
        this.mStartPlayButton.setImageResource(R.drawable.player_btn_pause_normal);
        this.mCdView.start();
//            disPlay(this.pos);
        //onPlay(mPlayService.resume()); // 播放
    }

    public void play(int pos) {
        this.mPlayService.play(pos);
        this.mStartPlayButton.setImageResource(R.drawable.player_btn_pause_normal);
        this.mCdView.start();
    }

    /**
     * 暂停
     */
    public void pause() {
        this.mPlayService.pause(); // 暂停
        this.mStartPlayButton.setImageResource(R.drawable.player_btn_play_normal);
        this.mCdView.pause();
    }

    /**
     * 上一曲
     */
    public void next() {
        this.mCdView.pause();
//        this.mCdView.reset();
        this.mPlayService.next(); // 上一曲
    }

    private void setLrc(int position) {
        Music music = MusicUtils.sMusicList.get(position);
        String lrcPath = MusicUtils.getLrcDir() + music.getTitle() + ".lrc";
        Log.d("Log", "lrcPath----->" + lrcPath);
        lrcView.setLrcPath(lrcPath);

    }

    //音乐进度同步
    public void onPublish(int progress) {
        mPlaySeekBar.setProgress(progress);
        if (lrcView.hasLrc()) lrcView.changeCurrent(progress);
        Message msg = this.mUpdateHandler.obtainMessage();
        msg.what = 0;
        Bundle bundle = new Bundle();
        bundle.putInt("progress", progress);
        msg.setData(bundle);
        this.mUpdateHandler.sendMessage(msg);
    }

    //歌曲切换界面同步
    public void onChange(int position) {
        Message msg = this.mUpdateHandler.obtainMessage();
        msg.what = 1;
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        msg.setData(bundle);
        this.mUpdateHandler.sendMessage(msg);
//        disPlay(position);
        Log.d("Log", "onChange---->Lrc");
//        setLrc(position);
    }

    private ServiceConnection mPlayServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder service) {
            mPlayService = ((PlayService.PlayBinder) service).getService();
            mPlayService.setOnMusicEventListener(mMusicEventListener);
            onChange(mPlayService.getPlayingPosition());
            mPlayService.succeed();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mPlayService = null;
        }

    };

    private void allowBindService() {
        bindService(new Intent(this, PlayService.class), mPlayServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    private void allowUnbindService() {
        unbindService(mPlayServiceConnection);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_play_back: {
                finish();
                break;
            }
            case R.id.ib_play_start: {
                if (this.mPlayService.isPlaying()) {
                    pause();
                } else {
                    play();
                }
                break;
            }
            case R.id.ib_play_pre: {
                pre();
                break;
            }
            case R.id.ib_play_next: {
                next();
                break;
            }
        }
    }
    /*
     * 音乐播放服务回调接口的实现类
	 */

    private PlayService.OnMusicEventListener mMusicEventListener =
            new PlayService.OnMusicEventListener() {
                @Override
                public void onPublish(int progress) {
                    PlayActivity.this.onPublish(progress);
                }

                @Override
                public void onChange(int position) {
                    PlayActivity.this.onChange(position);
                }
            };

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        this.mPlayService.seek(progress);
        lrcView.onDrag(progress);
    }

    public String transformMsec(int msec) {
        int second;
        int minute = 0;
        int hour = 0;
        String result = "";
        second = msec / 1000;
        if (second >= 60) {
            minute = second / 60;
            second %= 60;
        }
        if (minute >= 60) {
            hour = minute / 60;
            minute %= 60;
        }
        if (hour > 0) {
            result += hour + ":";
        }
        result += minute + ":";
        if (second < 10) {
            result += "0" + second;
        } else {
            result += second;
        }
        return result;
    }

}
