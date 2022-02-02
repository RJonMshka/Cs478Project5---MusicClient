package com.example.musicclientappp5;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.musiccentralappp5.MusicCentralInterface;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    protected static final String TAG = "MusicCentralServiceUser";
    // for holding the proxy for server's APIs
    private MusicCentralInterface musicCentralInterfaceService;
    private boolean isBoundToService = false;
    private Button bindServiceButton;
    private Button unbindServiceButton;
    private Button showSongsList;
    private TextView bindStatusText;
    // Spinner for selecting and playing a song
    private Spinner songSelector;
    private Button playSongButton;
    // holds the position of song that needs to play
    private Integer selectedSongPosition;

    // Service connection object
    private ServiceConnection musicCentralServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // Create the access to service's API
            musicCentralInterfaceService = MusicCentralInterface.Stub.asInterface(service);
            // set the flag true that the component is now bound to service
            isBoundToService = true;

            // Disable bind button and enable unbind button
            bindServiceButton.setEnabled(false);
            unbindServiceButton.setEnabled(true);
            // update the status of binding
            bindStatusText.setText(R.string.bind_text);
            // enable the button to access list of songs
            showSongsList.setEnabled(true);
            // enable the song selector spinner
            songSelector.setEnabled(true);
            // enable play button as well
            playSongButton.setEnabled(true);

            try {
                // if service is bound, then populate the spinner with information of songs
                showSongsSpinner();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // initialize the service's api to be null
            musicCentralInterfaceService = null;
            isBoundToService = false;
            // enable bind button and disable unbind button
            bindServiceButton.setEnabled(true);
            unbindServiceButton.setEnabled(false);
            // update the status
            bindStatusText.setText(R.string.unbind_text);
            // disable the access to song list
            showSongsList.setEnabled(false);
            // disable other features as well
            songSelector.setEnabled(false);
            playSongButton.setEnabled(false);
        }
    };

    // This method is responsible for populating spinner with song data
    private void showSongsSpinner() throws RemoteException {
        if(isBoundToService) {
            String[] songNames = musicCentralInterfaceService.getSongTitles();
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_song_item, songNames);
            songSelector.setAdapter(adapter);

            // set item select handler on spinner
            songSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedSongPosition = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
    }

    // This method binds to the music central service
    private void BindToService() {
        if(!isBoundToService) {
            Intent intent = new Intent(MusicCentralInterface.class.getName());

            ResolveInfo info = getPackageManager().resolveService(intent, 0);
            intent.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));

            boolean isConnectionSuccessful = bindService(intent, musicCentralServiceConnection, Context.BIND_AUTO_CREATE);

            // Logging whether the connection was successful or not
            if(isConnectionSuccessful) {
                Log.i(TAG, "binding successful");
            } else {
                Log.i(TAG, "binding not successful");
            }
        }
    }

    // This method unbinds from the music central service
    private void UnbindFromService() {
        if(isBoundToService) {
            unbindService(musicCentralServiceConnection);
            isBoundToService = false;
        }
    }


    // On create method
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // reference the views
        bindServiceButton = (Button) findViewById(R.id.bindToService);
        unbindServiceButton = (Button) findViewById(R.id.unbindFromService);
        unbindServiceButton.setEnabled(false);

        bindStatusText = (TextView) findViewById(R.id.bindServiceText);

        showSongsList = (Button) findViewById(R.id.showSongsList);
        showSongsList.setEnabled(false);

        songSelector = (Spinner) findViewById(R.id.songSpinner);
        playSongButton = (Button) findViewById(R.id.playSong);
        songSelector.setEnabled(false);
        playSongButton.setEnabled(false);

        // Click listener on bind button
        bindServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isBoundToService) {
                    BindToService();
                    bindServiceButton.setEnabled(false);
                }
            }
        });

        // click listener on unbind button
        unbindServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isBoundToService) {
                    UnbindFromService();

                    bindServiceButton.setEnabled(true);
                    unbindServiceButton.setEnabled(false);

                    bindStatusText.setText(R.string.unbind_text);

                    showSongsList.setEnabled(false);

                    songSelector.setEnabled(false);
                    playSongButton.setEnabled(false);

                    // if unbind from music central service, also stop the music player service if playing
                    stopMusicPlayingService();
                }
            }
        });

        // Click listener on show songs button
        showSongsList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isBoundToService) {
                    // if the music is playing with music player service, stop it
                    stopMusicPlayingService();
                    // start the second activity
                    Intent intent = new Intent(MainActivity.this, SongsListActivity.class);
                    startActivity(intent);
                }
            }
        });

        // Click event listener for play button
        playSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isBoundToService && musicCentralInterfaceService != null && selectedSongPosition != null) {
                    try {
                        // starts the media player service
                        String songUrl = musicCentralInterfaceService.getSongUrl(selectedSongPosition);

                        Intent intent = new Intent(getApplicationContext(), MusicPlayingService.class);
                        intent.putExtra("songLink", songUrl);

                        startService(intent);

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }


                }
            }
        });

        StartMusicCentralService();
    }

    // This method stops the Media Player Service
    private void stopMusicPlayingService() {
        Intent intent = new Intent(getApplicationContext(), MusicPlayingService.class);
        stopService(intent);
    }

    // This method starts the Custom Music Central Service
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void StartMusicCentralService() {
        Intent intent = new Intent(MusicCentralInterface.class.getName());

        ResolveInfo info = getPackageManager().resolveService(intent, 0);
        intent.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));

        startForegroundService(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stop the Media Player Service on OnStop lifecycle methods
        stopMusicPlayingService();
    }
}