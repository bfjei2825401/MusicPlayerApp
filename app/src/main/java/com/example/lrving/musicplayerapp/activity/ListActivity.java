package com.example.lrving.musicplayerapp.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.lrving.musicplayerapp.R;
import com.example.lrving.musicplayerapp.fragment.LocalFragment;
import com.example.lrving.musicplayerapp.fragment.OnlineFragment;
import com.example.lrving.musicplayerapp.service.PlayService;

public class ListActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int TAB_LOCAL = 0;
    private static final int TAB_ONLINE = 1;
    private PlayService mPlayService;

    private TextView tv_title;

    private LinearLayout ll_local;
    private LinearLayout ll_online;
    private ImageButton ib_local;
    private ImageButton ib_online;
    private ImageButton ib_menu;

    private LocalFragment localFragment;
    private OnlineFragment onlineFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        initView();
        initEvent();
        setSelect(0);
    }

    //初始化点击事件
    private void initEvent(){
        ll_local.setOnClickListener(this);
        ll_online.setOnClickListener(this);
        ib_menu.setOnClickListener(this);
    }

    private void initView(){
        tv_title= (TextView) findViewById(R.id.tv_title);
        ll_local= (LinearLayout) findViewById(R.id.ll_local);
        ll_online= (LinearLayout) findViewById(R.id.ll_online);
        ib_local= (ImageButton) findViewById(R.id.ib_local);
        ib_online= (ImageButton) findViewById(R.id.ib_online);
        ib_menu= (ImageButton) findViewById(R.id.ib_menu);

    }

    /**
     * 切换图片至暗色
     */
    private void resetImgs(){
        ib_local.setImageResource(R.drawable.icon_user_normal);
        ib_online.setImageResource(R.drawable.icon_search_normal);
    }
    @Override
    public void onClick(View v) {
        resetImgs();
        switch (v.getId()){
            case R.id.ll_local:
                setSelect(0);
                break;
            case R.id.ll_online:
                setSelect(1);
                break;
            default:
                break;
        }

    }

    public void setSelect(int i){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        hideFragment(transaction);
        switch(i){
            case TAB_LOCAL:
                if(localFragment==null){
                    localFragment = new LocalFragment();
                    transaction.add(R.id.id_content,localFragment);
                }else{
                    transaction.show(localFragment);
                }
                ib_local.setImageResource(R.drawable.icon_user_selected);
                tv_title.setText("本地音乐");
                break;
            case TAB_ONLINE:
                if(onlineFragment==null){
                    onlineFragment = new OnlineFragment();
                    transaction.add(R.id.id_content,onlineFragment);
                }else{
                    transaction.show(onlineFragment);
                }
                ib_online.setImageResource(R.drawable.icon_search_selected);
                tv_title.setText("在线音乐");
                break;
            default:
                break;
        }
        transaction.commit();

    }

    private void hideFragment(FragmentTransaction transaction) {

        if (onlineFragment!=null){
            transaction.hide(onlineFragment);
        }

    }

    private ServiceConnection mPlayServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder service) {
            mPlayService = ((PlayService.PlayBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mPlayService = null;

        }

    };

    public PlayService getPlayService() {
        return mPlayService;
    }

    /**
     * Fragment的view加载完成后回调
     */
    public void allowBindService() {
        bindService(new Intent(this, PlayService.class), mPlayServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    /**
     * fragment的view消失后回调
     */
    public void allowUnbindService() {
        unbindService(mPlayServiceConnection);
    }

}
