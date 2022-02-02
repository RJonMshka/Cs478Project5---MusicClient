// MusicCentralInterface.aidl
package com.example.musiccentralappp5;

interface MusicCentralInterface {
    List getAllSongs();
    Map getSongByIndex(int index);
    String getSongUrl(int index);
    String[] getSongTitles();
}