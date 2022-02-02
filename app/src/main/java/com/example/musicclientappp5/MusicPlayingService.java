package com.example.musicclientappp5;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class MusicPlayingService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    private final String TAG = "MusicPlayingService";
    private MediaPlayer myMediaPlayer;
    private String songUrl;

    public MusicPlayingService() {
    }

    // Returns null as it is a started service
    @Override
    public IBinder onBind(Intent intent) {
        // not a bound service
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Create a media player and set event listeners
        myMediaPlayer = new MediaPlayer();
        myMediaPlayer.setOnCompletionListener(this);
        myMediaPlayer.setOnPreparedListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        songUrl = intent.getStringExtra("songLink");
        // Reset the media player on every start command
        myMediaPlayer.reset();
        if(!myMediaPlayer.isPlaying()) {
            try {
                // Set the data source
                myMediaPlayer.setDataSource(songUrl);
                // prepare async to avoid blocking
                myMediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(myMediaPlayer != null) {
            // stop if playing
            if(myMediaPlayer.isPlaying()) {
                myMediaPlayer.stop();
            }
            // Release the resources helds by media player
            myMediaPlayer.release();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(mp.isPlaying()) {
            mp.stop();
        }
        // Stop the service when song has completely played
        stopSelf();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // Once prepared, start playing the song
        if(!mp.isPlaying()) {
            mp.start();
        }
    }
}