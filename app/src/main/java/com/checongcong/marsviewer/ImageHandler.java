package com.checongcong.marsviewer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

/**
 * ImageHandler handles the image download and rendering.
 *
 * Created by cche on 2/19/2018.
 */

public class ImageHandler {
    private static final String TAG = MainActivity.class.getSimpleName();

    private ImageView imageView;

    private Bitmap bitmap = null;
    private static final String API_URL = "https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos?sol=1000&api_key=y2mPJ9QhVhrPyu6psN44Zzt5dgvgQuSzTCvDxoaN";
    private Vector<String> imageUrls = new Vector<>();
    private int imageIndex = -1;

    public ImageHandler(ImageView imageView) {
        this.imageView = imageView;
    }

    public void start() {
        new PopulateImageUrlTask().execute();
    }

    public void renderImage(boolean isNext) {
        if (imageUrls.isEmpty()) {
            Log.e(TAG, "renderImage error: empty imageUrls");
            return;
        }
        if (isNext) {
            imageIndex = (imageIndex + 1) % imageUrls.size();
        } else {
            imageIndex = (imageIndex - 1 + imageUrls.size()) % imageUrls.size();
        }
        downloadImageAndRender(imageUrls.get(imageIndex));
    }

    private void downloadImageAndRender(String imageUrl) {
        final String url = imageUrl;

        new Thread() {
            public void run() {
                Message msg = Message.obtain();
                try {
                    InputStream is = openHttpConnection(url);
                    bitmap = BitmapFactory.decodeStream(is);
                    Bundle b = new Bundle();
                    b.putParcelable("bitmap", bitmap);
                    msg.setData(b);
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, "downloadImageAndRender IOException: " + e.getMessage());
                }
                messageHandler.sendMessage(msg);
            }
        }.start();
    }

    private InputStream openHttpConnection(String urlStr) {
        InputStream in = null;
        int resCode = -1;

        try {
            URL url = new URL(urlStr);
            URLConnection urlConn = url.openConnection();

            if (!(urlConn instanceof HttpURLConnection)) {
                throw new IOException("URL is not an Http URL");
            }

            HttpURLConnection httpConn = (HttpURLConnection) urlConn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            resCode = httpConn.getResponseCode();

            if (resCode == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();
            }
        }catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return in;
    }

    private Handler messageHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            imageView.setImageBitmap((Bitmap) (msg.getData().getParcelable("bitmap")));
        }
    };

    // An async task that fetches the NASA Mars Rover Image API, and populates the images URLs.
    private class PopulateImageUrlTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            NASAImageApiHandler handler = new NASAImageApiHandler();
            JSONObject content = handler.fetchUrl(API_URL);
            if (content == null) {
                Log.e(TAG, "Failed to fetch API");
                return null;
            }
            populateImageUrls(content);  // content is guaranteed to be non-null.
            return null;
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
        private void populateImageUrls(JSONObject content) {
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
                ImageHandler.this.renderImage(true);
            } catch (JSONException e) {
                Log.e(TAG, "populateImageUrls JSONException: " + e.getMessage());
            }
        }
    }
}
