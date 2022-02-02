package com.example.musicclientappp5;

import android.os.RemoteException;

public interface SongClickListener {
    public void onClick(int songNumber) throws RemoteException;
}
