package com.example.musicclientappp5;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.example.musiccentralappp5.MusicCentralInterface;

import java.util.List;
import java.util.Map;

public class SongsListActivity extends AppCompatActivity {

    private final String TAG = "SongsListActivity";
    // Recycler view variable
    private RecyclerView songsRecyclerView;
    private SongClickListener clickListener;

    private boolean isBoundToService = false;
    private MusicCentralInterface musicCentralInterfaceService;

    // Service Connection object
    private final ServiceConnection musicCentralServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // Service's API
            musicCentralInterfaceService = MusicCentralInterface.Stub.asInterface(service);
            isBoundToService = true;

            try {
                // show a list of songs in a recycler view
                showSongsList();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicCentralInterfaceService = null;
            isBoundToService = false;
        }
    };

    // This method shows the list of songs in a recyler view
    private void showSongsList() throws RemoteException {
        if(isBoundToService) {
            List<Map> songsList = musicCentralInterfaceService.getAllSongs();

            // On click of list item, play the song associated
            clickListener = (songNumber) -> {
                if(isBoundToService && musicCentralInterfaceService != null) {
                    String url = musicCentralInterfaceService.getSongUrl(songNumber);
                    Intent intent = new Intent(getApplicationContext(), MusicPlayingService.class);
                    intent.putExtra("songLink", url);
                    // Start the Media Player Service
                    startService(intent);
                }
            };
            // Custom Recycler View Adapter for list view
            SongsListAdapter adapter = new SongsListAdapter(songsList, clickListener);
            songsRecyclerView.setAdapter(adapter);
            songsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    // This method binds the activity to Music Central Service
    private void BindToService() {
        if(!isBoundToService) {
            Intent intent = new Intent(MusicCentralInterface.class.getName());

            ResolveInfo info = getPackageManager().resolveService(intent, 0);
            intent.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));

            boolean isConnectionSuccessful = bindService(intent, musicCentralServiceConnection, Context.BIND_AUTO_CREATE);

            if(isConnectionSuccessful) {
                Log.i(TAG, "binding successful");
            } else {
                Log.i(TAG, "binding not successful");
            }
        }
    }


    // This method unbinds this activity from Music Central Service
    private void UnbindFromService() {
        if(isBoundToService) {
            unbindService(musicCentralServiceConnection);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs_list);
        // reference the recycler view
        songsRecyclerView = (RecyclerView) findViewById(R.id.songsListRecyclerView);
        // Bind to Music Central Service
        BindToService();
    }

    // Stop the Media Player service when back pressed, also unbind from Music Central Service
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        UnbindFromService();

        Intent intent = new Intent(getApplicationContext(), MusicPlayingService.class);
        stopService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}