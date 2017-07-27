package com.proba.drawoncamera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class MainActivity extends Activity{

    private static final String TAG = "cip";
    private SurfaceView mSurface;
    private static DrawingThread mThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);



        clearItems();

        mSurface = (SurfaceView) findViewById(R.id.surface);
        mSurface.setZOrderOnTop(true);
        mSurface.getHolder().setFormat(PixelFormat.TRANSPARENT);
        mSurface.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    int posX = (int) event.getX();
                    int posY = (int) event.getY();
                    mThread.addItem(posX, posY);
                    Log.i(TAG, "click!");
                }
                return true;
            }
        });
        mSurface.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                mThread = new DrawingThread(holder, icon);
                mThread.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                mThread.updateSize(width, height);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mThread.quit();
                mThread = null;
            }
        });
    }

    private void clearItems() {
        findViewById(R.id.button_erase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mThread.clearItems();
            }
        });
    }


}
