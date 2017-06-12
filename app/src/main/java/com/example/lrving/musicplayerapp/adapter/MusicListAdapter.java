package com.example.lrving.musicplayerapp.adapter;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lrving.musicplayerapp.R;
import com.example.lrving.musicplayerapp.application.App;
import com.example.lrving.musicplayerapp.utils.ImageTools;
import com.example.lrving.musicplayerapp.utils.MusicIconLoader;
import com.example.lrving.musicplayerapp.utils.MusicUtils;

/**
 * Created by Lrving on 2017/6/8.
 */

public class MusicListAdapter extends BaseAdapter {
    private int mPlayingPosition;

    //private OnMoreClickListener mListener;

    public void setPlayingPosition(int position) {
        mPlayingPosition = position;
    }

    public int getPlayingPosition() {
        return mPlayingPosition;
    }

	/*
	public void setOnMoreClickListener(OnMoreClickListener l) {
		mListener = l;
	}
	*/

    @Override
    public int getCount() {
        return MusicUtils.sMusicList.size();
    }

    @Override
    public Object getItem(int position) {
        return MusicUtils.sMusicList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder ;

        if(convertView == null) {
            convertView = View.inflate(App.appContext, R.layout.music_list_item, null);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.tv_music_list_title);
            holder.artist = (TextView) convertView.findViewById(R.id.tv_music_list_artist);
            holder.icon = (ImageView) convertView.findViewById(R.id.music_list_icon);
            holder.mark = convertView.findViewById(R.id.music_list_selected);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        if(mPlayingPosition == position) holder.mark.setVisibility(View.VISIBLE);
        else holder.mark.setVisibility(View.INVISIBLE);

        Bitmap icon = MusicIconLoader.getInstance()
                .load(MusicUtils.sMusicList.get(position).getImage());
        holder.icon.setImageBitmap(icon == null ?
                ImageTools.scaleBitmap(R.drawable.bubble_new) : ImageTools.scaleBitmap(icon));

        holder.title.setText(MusicUtils.sMusicList.get(position).getTitle());
        holder.artist.setText(MusicUtils.sMusicList.get(position).getArtist());

        //final int pos = position;
		/*
		holder.more.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mListener != null) mListener.onMoreClick(pos);
			}
		});
		*/
        return convertView;
    }

    public interface OnMoreClickListener {
        public void onMoreClick(int position);
    }

    static class ViewHolder {
        ImageView icon;
        TextView title;
        TextView artist;
        View mark;
        //ImageView imgview;
        //ImageView more;
    }
}
