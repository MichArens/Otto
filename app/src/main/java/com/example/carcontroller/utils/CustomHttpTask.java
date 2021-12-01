package com.example.carcontroller.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.carcontroller.callbacks.HttpCallbacks;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class CustomHttpTask extends AsyncTask<String, Integer, String> {

    private Context mContext;
    private final HttpCallbacks.HttpCallback httpCustomCallback;
    private JSONObject jsonObject;
    private String method = "POST";
    public static String POST_REQUEST = "POST";
    public static String GET_REQUEST = "GET";
    public static String PUT_REQUEST = "PUT";
    private int currentRequestStatusCode;

    public CustomHttpTask(@Nullable Context context, @NonNull String method, @Nullable JSONObject jsonObject, @Nullable final HttpCallbacks.HttpCallback httpCallback) {
        //Relevant Context should be provided to newly created components (whether application context or activity context)
        //getApplicationContext() - Returns the context for all activities running in application
        mContext = context != null ? context.getApplicationContext() : null;
        this.httpCustomCallback = httpCallback;
        this.jsonObject = jsonObject != null ? jsonObject : new JSONObject();
        this.method = method;
    }

    //Execute this before the request is made
    @Override
    protected void onPreExecute() {
        // A toast provides simple feedback about an operation as popup.
        // It takes the application Context, the text message, and the duration for the toast as arguments
//        if (mContext != null) Toast.makeText(mContext, "Going for the network call..", Toast.LENGTH_LONG).show();

    }

    //Perform the request in background
    @Override
    protected String doInBackground(String... params) {
        try {
            // Creating & connection Connection with url and required Header.
            String urlStr = params[0];
            URL url = new URL(urlStr);
            HttpURLConnection urlConnection;
            if (params[0].contains("https")) {
                urlConnection = (HttpsURLConnection) url.openConnection();
            } else {
                urlConnection = (HttpURLConnection) url.openConnection();
            }
            urlConnection.setRequestProperty("Content-Type", "application/json");
//            if (this.method.equalsIgnoreCase("POST")) urlConnection.addRequestProperty("Authorization", "Bearer " + CustomConfig.getInstance().getCustomToken());
            urlConnection.setRequestMethod(this.method);   //POST or GET
            urlConnection.connect();

            // Create JSONObject Request
            // Write Request to output stream to server.
            if (this.method.equalsIgnoreCase("POST")) {
                OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
                out.write(this.jsonObject != null ? this.jsonObject.toString() : new JSONObject().toString());
                out.close();
            }

            // Check the connection status.
            int statusCode = urlConnection.getResponseCode();
            String statusMsg = urlConnection.getResponseMessage();
            currentRequestStatusCode = statusCode;

            // Connection success. Proceed to fetch the response.
            if (statusCode == 200) {
                InputStream it = new BufferedInputStream(urlConnection.getInputStream());
                InputStreamReader read = new InputStreamReader(it);
                BufferedReader buff = new BufferedReader(read);
                StringBuilder dta = new StringBuilder();
                String chunks;
                while ((chunks = buff.readLine()) != null) {
                    dta.append(chunks);
                }
                String returndata = dta.toString();
                return returndata;
            } else {
                //Handle else case
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //Run this once the background task returns.
    @Override
    protected void onPostExecute(@Nullable String json) {
        //Print the response code as toast popup
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user != null) {
//            user.getIdToken(false).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
//                public void onComplete(@NonNull Task<GetTokenResult> task) {
//                    if (task != null && task.getResult() != null) {
//                        String idToken = task.getResult().getToken();
//                        CustomConfig.getInstance().setCustomToken(idToken);
//                    }
//                }
//            });
//        }
        if (httpCustomCallback != null) {
            if (currentRequestStatusCode != 200) {
                httpCustomCallback.onError(false);
                return;
            } else if (json == null) {
                httpCustomCallback.onResult(new JSONObject());
                return;
            }
            Object obj = null;
            try {
                obj = new JSONObject(json);
            } catch (Throwable tx) {
                Log.e("My App", "Could not parse malformed JSON: \"" + json + "\"");
            }

            httpCustomCallback.onResult(obj);
        }
    }

}
