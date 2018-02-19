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

class ImageHandler {
    private static final String TAG = ImageHandler.class.getSimpleName();

    private ImageView imageView;

    private Bitmap bitmap = null;
    private static final String API_URL = "https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos?sol=1000&api_key=y2mPJ9QhVhrPyu6psN44Zzt5dgvgQuSzTCvDxoaN";
    private Vector<String> imageUrls = new Vector<>();
    private int imageIndex = -1;

    ImageHandler(ImageView imageView) {
        this.imageView = imageView;
    }

    void start() {
        new PopulateImageUrlTask().execute();
    }

    void renderImage(boolean isNext) {
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
            int resCode = httpConn.getResponseCode();

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
            if (!handler.fetchUrl(API_URL, imageUrls)) {
                Log.e(TAG, "Failed to fetch API");
                return null;
            }
            ImageHandler.this.renderImage(true);
            return null;
        }
    }
}
