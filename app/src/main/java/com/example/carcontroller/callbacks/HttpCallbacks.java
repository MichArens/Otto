package com.example.carcontroller.callbacks;

import androidx.annotation.Nullable;

public class HttpCallbacks {

    public interface HttpCallback<JSONObject> {
        void onResult(@Nullable JSONObject obj);
        void onError(@Nullable JSONObject obj);
    }

}
