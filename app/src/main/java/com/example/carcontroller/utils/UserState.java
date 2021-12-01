package com.example.carcontroller.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

public class UserState {

    public static void getUserState(Activity activity) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        Set<String> strings = sharedPref.getStringSet("btn1", new HashSet<>());
        Toast.makeText(activity.getApplicationContext(), strings.toString(), Toast.LENGTH_LONG).show();
    }
}
