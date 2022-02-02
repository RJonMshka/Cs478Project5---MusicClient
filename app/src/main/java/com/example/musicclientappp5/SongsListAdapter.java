package com.example.musicclientappp5;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class SongsListAdapter extends RecyclerView.Adapter<SongsListAdapter.ViewHolder> {

    private List<Map> songsList;
    private SongClickListener clickListener;

    public SongsListAdapter(List<Map> songsList, SongClickListener clickListener) {
        this.songsList = songsList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View songView = inflater.inflate(R.layout.song_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(songView, this.clickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.songTitle.setText((String) songsList.get(position).get("songTitle"));
        holder.artistName.setText((String) songsList.get(position).get("songArtist"));
        holder.songImage.setImageBitmap((Bitmap) songsList.get(position).get("songImage"));
        holder.songNumber = position;
    }

    @Override
    public int getItemCount() {
        return this.songsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private SongClickListener clickListener;
        public int songNumber;
        public ImageView songImage;
        public TextView songTitle;
        public TextView artistName;

        public ViewHolder(@NonNull View itemView, SongClickListener clickListener) {
            super(itemView);
            this.clickListener = clickListener;
            songImage = (ImageView) itemView.findViewById(R.id.songImage);
            songTitle = (TextView) itemView.findViewById(R.id.songTitle);
            artistName = (TextView) itemView.findViewById(R.id.artistName);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            try {
                this.clickListener.onClick(songNumber);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
