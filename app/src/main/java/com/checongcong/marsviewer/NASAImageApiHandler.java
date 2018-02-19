package com.checongcong.marsviewer;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * NASAImageApiHandler fetches and parses the NASA Mars Rover Image API content.
 *
 * Created by cche on 2/18/2018.
 */

public class NASAImageApiHandler {
    private static final String TAG = NASAImageApiHandler.class.getSimpleName();

    public JSONObject fetchUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            InputStream is = new BufferedInputStream(conn.getInputStream());
            return convertStreamToJSON(is);
        } catch (MalformedURLException e) {
            Log.e(TAG, "fetchUrl MalformedURLException: " + e.getMessage());
        } catch (ProtocolException e) {
            Log.e(TAG, "fetchUrl ProtocolException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "fetchUrl IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "fetchUrl Exception: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private JSONObject convertStreamToJSON(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            Log.e(TAG, "convertStreamToJSON readLine IOException: " + e.getMessage());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "convertStreamToJSON close stream IOException: " + e.getMessage());
            }
        }

        JSONObject json = null;
        try {
            json = new JSONObject(sb.toString());
        } catch (final JSONException e) {
            Log.e(TAG, "convertStreamToJSON JSONException: " + e.getMessage());
        }
        return json;
    }
}
