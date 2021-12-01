package com.example.carcontroller.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.carcontroller.globals.AppConfig;
import com.example.carcontroller.globals.Controls;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;

public class SpotifyHandler {

//    private static SpotifyUtils instance;
    private Context context;
    private SpotifyAppRemote spotifyAppRemote;
    private Track currentTrack;

    public SpotifyHandler(Context context) {
        this.context = context;
    }

    public void initSpotifyConnection() {
        /**
         * Spotify connection Params
         */
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(AppConfig.SPOTIFY_CLIENT_ID)
                        .setRedirectUri(AppConfig.SPOTIFY_REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        /**
         * Spotify Connection
         */
        SpotifyAppRemote.connect(this.context, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
//                        AudioUtils.setmSpotifyAppRemote(spotifyAppRemote);
                        setSpotifyAppRemote(spotifyAppRemote);
                        Log.d("MainActivity", "Connected! Yay!");
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("MainActivity", throwable.getMessage(), throwable);
                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
    }

    public void disconnectSpotifyConnection() {
        SpotifyAppRemote.disconnect(getSpotifyAppRemote());
        setSpotifyAppRemote(null);
    }

    public SpotifyAppRemote getSpotifyAppRemote() {
        return spotifyAppRemote;
    }

    public void setSpotifyAppRemote(SpotifyAppRemote spotifyAppRemote) {
        this.spotifyAppRemote = spotifyAppRemote;
        if (this.spotifyAppRemote != null) {
            spotifyAppRemote.getPlayerApi()
                    .subscribeToPlayerState()
                    .setEventCallback(playerState -> {
                        final Track track = playerState.track;
                        if (track != null) {
                            Log.d("MainActivity", track.name + " by " + track.artist.name);
                        }
                    });
        }
    }

    public void likeSong(){
        Log.d("SpotifyHelper", "Like Song");
        if (currentTrack == null) {
//            Toast.makeText(context, "No Track Playing", Toast.LENGTH_LONG).show();
            Log.d("SpotifyHelper", "Like Song No Track");
            return;
        }

        spotifyAppRemote.getUserApi().addToLibrary(currentTrack.uri);
    }

    public void toggleShuffle() {
        spotifyAppRemote.getPlayerApi().toggleShuffle();
    }
}
