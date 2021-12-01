package com.example.carcontroller.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.carcontroller.globals.Controls;

public class SharedPerefrencesUtils {

    public static final String SET_1 = "set1";
    public static final String SET_2 = "set2";
    public static final String SET_3 = "set3";
    public static final String CALL_SET = "callSet";

    private static final String RED_CONTROL = "RED_CONTROL";
    private static final String BLUE_CONTROL = "BLUE_CONTROL";
    private static final String GREEN_CONTROL = "GREEN_CONTROL";
    private static final String PURPLE_CONTROL = "PURPLE_CONTROL";

    private static final String SELECTED_SET_ID = "selectedSetID";

    public static String[] getCurrentControlsState(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        String selectedSetId = sharedPref.getString(SELECTED_SET_ID, "");
        if (selectedSetId.equalsIgnoreCase("")) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(SELECTED_SET_ID, SET_1);
            editor.apply();
            selectedSetId = SET_1;
        }
        return new String[]{
                sharedPref.getString(selectedSetId + "_" + RED_CONTROL, ""),
                sharedPref.getString(selectedSetId + "_" + BLUE_CONTROL, ""),
                sharedPref.getString(selectedSetId + "_" + GREEN_CONTROL, ""),
                sharedPref.getString(selectedSetId + "_" + PURPLE_CONTROL, ""),
        };

    }

    public static void setSelectedSet(Context context, String selectedSetId) {
        if (!selectedSetId.equalsIgnoreCase(SET_1) &&
                !selectedSetId.equalsIgnoreCase(SET_2) &&
                 !selectedSetId.equalsIgnoreCase(SET_3) &&
                  !selectedSetId.equalsIgnoreCase(CALL_SET)) return;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(SELECTED_SET_ID, selectedSetId);
        editor.apply();
    }

    public static void setControlValue(Context context, int controlPicked, String controlValue) {
        switch (controlPicked) {
            case Controls
                    .RED_CONTROL:
                setControlValueHelper(context, RED_CONTROL, controlValue);
                break;
            case Controls
                    .BLUE_CONTROL:
                setControlValueHelper(context, BLUE_CONTROL, controlValue);
                break;
            case Controls
                    .GREEN_CONTROL:
                setControlValueHelper(context, GREEN_CONTROL, controlValue);
                break;
            case Controls
                    .PURPLE_CONTROL:
                setControlValueHelper(context, PURPLE_CONTROL, controlValue);
                break;
        }
    }

    private static void setControlValueHelper(Context context, String key , String value) {
        SharedPreferences  sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();

        String selectedSetId = sharedPref.getString(SELECTED_SET_ID, "");
        if (selectedSetId.equalsIgnoreCase(""))  {
            editor.putString(SELECTED_SET_ID, SET_1);
            selectedSetId = SET_1;
        }

        editor.putString(selectedSetId + "_" + key, value);
        editor.apply();
    }

    public static String getPickedAction(Context context, int commandPicked) {
        switch (commandPicked) {
            case Controls
                    .RED_CONTROL:
                return getPickedActionHelper(context, RED_CONTROL);
            case Controls
                    .BLUE_CONTROL:
                return getPickedActionHelper(context, BLUE_CONTROL);
            case Controls
                    .GREEN_CONTROL:
                return getPickedActionHelper(context, GREEN_CONTROL);
            case Controls
                    .PURPLE_CONTROL:
                return getPickedActionHelper(context, PURPLE_CONTROL);
        }
        return "";
    }

    private static String getPickedActionHelper(Context context, String key) {
        SharedPreferences  sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        String selectedSetId = sharedPref.getString(SELECTED_SET_ID, "");
        if (selectedSetId.equalsIgnoreCase(""))  {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(SELECTED_SET_ID, SET_1);
            editor.apply();
            selectedSetId = SET_1;
        }

        return sharedPref.getString(selectedSetId + "_" + key, "");
    }
}
