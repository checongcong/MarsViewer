package com.checongcong.marsviewer;

import android.util.Log;

import org.json.JSONArray;
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
import java.util.Vector;

/**
 * NASAImageApiHandler fetches and parses the NASA Mars Rover Image API content.
 *
 * Created by cche on 2/18/2018.
 */

class NASAImageApiHandler {
    private static final String TAG = NASAImageApiHandler.class.getSimpleName();

    private JSONObject json;

    boolean fetchUrl(String urlString, Vector<String> imageUrls) {
        imageUrls.clear();
        boolean success = false;
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            InputStream is = new BufferedInputStream(conn.getInputStream());
            json = convertStreamToJSON(is);
            is.close();
            success = populateImageUrls(json, imageUrls);
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
        return success;
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
        }

        JSONObject json = null;
        try {
            json = new JSONObject(sb.toString());
        } catch (final JSONException e) {
            Log.e(TAG, "convertStreamToJSON JSONException: " + e.getMessage());
        }
        return json;
    }


    /* Format of JSONObject:

       {"photos":[
          {
            "id":102693,
            "sol":1000,
            "camera":{
                "id":20,
                "name":"FHAZ",
                "rover_id":5,
                "full_name":"Front Hazard Avoidance Camera"
            },
            "img_src":"http://mars.jpl.nasa.gov/msl-raw-images/proj/msl/redops/ods/surface/sol/01000/opgs/edr/fcam/FLB_486265257EDR_F0481570FHAZ00323M_.JPG",
            "earth_date":"2015-05-30",
            "rover":{
                "id":5,
                "name":"Curiosity",
                "landing_date":"2012-08-06",
                "launch_date":"2011-11-26",
                "status":"active",
                "max_sol":1968,
                "max_date":"2018-02-18",
                "total_photos":332219,
                "cameras":[
                    {"name":"FHAZ","full_name":"Front Hazard Avoidance Camera"},
                    {"name":"NAVCAM","full_name":"Navigation Camera"},
                    {"name":"MAST","full_name":"Mast Camera"},
                    {"name":"CHEMCAM","full_name":"Chemistry and Camera Complex"},
                ],
            },
          },
          {
              // the second photo
          },
          {
              // the third photo
          },
          ...
      ]}
    */
    private boolean populateImageUrls(JSONObject content, Vector<String> imageUrls) {
        try {
            JSONArray photos = content.getJSONArray("photos");
            for (int i = 0; i < photos.length(); i++) {
                JSONObject photo = photos.getJSONObject(i);
                String imageUrl = photo.getString("img_src");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    imageUrls.add(imageUrl);
                    Log.e(TAG, imageUrl);
                } else {
                    Log.e(TAG, "failed to fetch url");
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "populateImageUrls JSONException: " + e.getMessage());
        }
        return !imageUrls.isEmpty();
    }
}
