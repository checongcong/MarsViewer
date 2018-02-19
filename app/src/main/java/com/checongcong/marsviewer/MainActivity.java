package com.checongcong.marsviewer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    // Handles to UI Elements.
    private Button prevButton;
    private Button nextButton;
    private ImageView imageView;

    // Handles Rover image download and rendering.
    private ImageHandler imageHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prevButton = (Button) findViewById(R.id.prevButton);
        nextButton = (Button) findViewById(R.id.nextButton);
        imageView = (ImageView) findViewById(R.id.imageView);

        imageHandler = new ImageHandler(imageView);
        imageHandler.start();  // Starts to download and render first image in async mode.

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageHandler.renderImage(false);
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageHandler.renderImage(true);
            }
        });
    }
}
