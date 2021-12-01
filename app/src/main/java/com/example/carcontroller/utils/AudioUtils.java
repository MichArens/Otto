package com.example.carcontroller.utils;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.carcontroller.callbacks.HttpCallbacks;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class AudioUtils {

//    private static SpotifyAppRemote mSpotifyAppRemote;

    private static AudioManager mAudioManager = null;

    public static void skipSong(Context context) {
        if (mAudioManager == null) mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT);
        mAudioManager.dispatchMediaKeyEvent(event);
    }

    public static void goBackSong(Context context) {
        if (mAudioManager == null) mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
        mAudioManager.dispatchMediaKeyEvent(event);
    }

    public static void  skipSpotifySong(SpotifyAppRemote spotifyAppRemote) {
        Log.d("AUDIO UTILS", "SKIP");
        spotifyAppRemote.getPlayerApi().skipNext();
    }

}
