package com.example.carcontroller.globals;

import java.util.ArrayList;
import java.util.List;

public class Controls {
    public static final int RED_CONTROL = 0;
    public static final int BLUE_CONTROL = 1;
    public static final int GREEN_CONTROL = 2;
    public static final int PURPLE_CONTROL = 3;

    public static final String SPOTIFY_LIKE_SONG = "spotifyLikeSong";
    public static final String SPOTIFY_TOGGLE_SHUFFLE = "spotifyToggleShuffle";
    public static final String NEXT_SONG = "nextSong";
    public static final String PREV_SONG = "prevSong";
    public static final String PAUSE_PLAY = "pausePlay";

    private static final String SPOTIFY_LIKE_SONG_DISPLAY = "Like Spotify Song";
    private static final String SPOTIFY_TOGGLE_SHUFFLE_DISPLAY = "Toggle Spotify Shuffle";
    private static final String NEXT_SONG_DISPLAY = "Next Song";
    private static final String PREV_SONG_DISPLAY = "Previous Song";
    private static final String PAUSE_PLAY_DISPLAY = "Pause/Play Song";

    public static final List<String > phoneOptions = new ArrayList<String>() {
        {
            add(NEXT_SONG);
            add(PREV_SONG);
            add(PAUSE_PLAY);
        }
    };

    public static final List<String > phoneOptionsDisplay = new ArrayList<String>() {
        {
            add(NEXT_SONG_DISPLAY);
            add(PREV_SONG_DISPLAY);
            add(PAUSE_PLAY_DISPLAY);
        }
    };

    public static final List<String > spotifyOptions = new ArrayList<String>() {
        {
         add(SPOTIFY_LIKE_SONG);
         add(SPOTIFY_TOGGLE_SHUFFLE);
        }
    };

    public static final List<String > spotifyOptionsDisplay = new ArrayList<String>() {
        {
            add(SPOTIFY_LIKE_SONG_DISPLAY);
            add(SPOTIFY_TOGGLE_SHUFFLE_DISPLAY);
        }
    };
}
