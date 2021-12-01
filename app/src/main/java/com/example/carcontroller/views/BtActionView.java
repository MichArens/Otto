package com.example.carcontroller.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.example.carcontroller.R;
import com.example.carcontroller.globals.Controls;

public class BtActionView extends LinearLayout {

    ImageView bigIcon, smallIcon;
    public BtActionView(Context context) {
        super(context);
        inflate(context, R.layout.bt_action_view, this);
        init();
    }

    public BtActionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context,R.layout.bt_action_view, this);
        init();
    }

    private void init() {
        bigIcon = findViewById(R.id.option_big_icon);
        smallIcon = findViewById(R.id.option_small_icon);
        setIcons("");
    }

    public void setIcons(String control) {
        switch (control) {
            case Controls
                    .SPOTIFY_LIKE_SONG:
                bigIcon.setImageResource(R.drawable.like_icon);
                smallIcon.setImageResource(R.drawable.spotify_icon);
                break;
            case Controls
                    .SPOTIFY_TOGGLE_SHUFFLE:
                bigIcon.setImageResource(R.drawable.shuffle_icon);
                smallIcon.setImageResource(R.drawable.spotify_icon);
                break;
            case Controls
                    .NEXT_SONG:
                bigIcon.setImageResource(R.drawable.next_song_icon);
                smallIcon.setImageResource(R.drawable.headset_icon);
                break;
            case Controls
                    .PREV_SONG:
                bigIcon.setImageResource(R.drawable.back_song_icon);
                smallIcon.setImageResource(R.drawable.headset_icon);
                break;
            case Controls
                    .PAUSE_PLAY:
                bigIcon.setImageResource(R.drawable.pause_icon);
                smallIcon.setImageResource(R.drawable.headset_icon);
                break;
            default:
                bigIcon.setImageResource(R.drawable.add_icon);
                smallIcon.setImageResource(0);
                break;
        }
    }
}
