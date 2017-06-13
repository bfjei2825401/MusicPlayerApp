package com.example.lrving.musicplayerapp.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.lrving.musicplayerapp.R;
import com.example.lrving.musicplayerapp.activity.ListActivity;
import com.example.lrving.musicplayerapp.activity.PlayActivity;
import com.example.lrving.musicplayerapp.adapter.MusicListAdapter;
import com.example.lrving.musicplayerapp.domain.Music;
import com.example.lrving.musicplayerapp.utils.MusicUtils;

import java.io.File;


public class LocalFragment extends Fragment implements AdapterView.OnItemClickListener {
    private ListView mListView;
    private ListActivity mActivity;
    private PlayActivity mPlayActivity;
    private static final String TAG = LocalFragment.class.getSimpleName();
    private MusicListAdapter mMusicListAdapter = new MusicListAdapter();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (ListActivity) activity;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View mView = inflater.inflate(R.layout.fragment_local, container, false);
        setupViews(mView);

        return mView;
    }
    @Override
    public void onStart() {
        super.onStart();
//        mActivity.allowBindService();
    }

    @Override
    public void onStop() {
        super.onStop();
//        mActivity.allowUnbindService();
    }


    private void setupViews(View mView) {
        mListView = (ListView) mView.findViewById(R.id.lv_music_list);
        mListView.setAdapter(mMusicListAdapter);

        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(mItemLongClickListener);
    }


    private AdapterView.OnItemLongClickListener mItemLongClickListener =
            new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view,
                                               int position, long id) {
                    final int pos = position;

                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    builder.setTitle("删除该条目");
                    builder.setMessage("确认要删除该条目吗?");
                    builder.setPositiveButton("删除",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //更新数据
                                    Music music = MusicUtils.sMusicList.remove(pos);
                                    mMusicListAdapter.notifyDataSetChanged();
                                    if (new File(music.getUri()).delete()) {
                                        Toast.makeText(mActivity,"删除成功",Toast.LENGTH_SHORT);
                                    }
                                }
                            });
                    builder.setNegativeButton("取消", null);
                    builder.create().show();
                    return true;
                }
            };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(mActivity, PlayActivity.class);
        intent.putExtra("pos", position);
        startActivity(intent);
        play(position);
        Log.e(TAG, ""+position);
    }
    private void play(int position) {
        int pos = mActivity.getPlayService().newPlay(position);
//        onPlay(pos);
    }

    private void onPlay(int pos) {
        //新启动一个线程更新通知栏，防止更新时间过长，导致界面卡顿！
        new Thread(){
            @Override
            public void run() {
                super.run();
                mActivity.getPlayService().setRemoteViews();
            }
        }.start();
    }
}
