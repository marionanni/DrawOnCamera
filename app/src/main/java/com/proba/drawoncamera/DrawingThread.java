package com.proba.drawoncamera;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.SurfaceHolder;

import java.util.ArrayList;


public class DrawingThread extends HandlerThread implements Handler.Callback {
    private static final int MSG_ADD = 100;
    private static final int MSG_MOVE = 101;
    private static final int MSG_CLEAR = 102;

    private int mDrawingWidth, mDrawingHeight;
    private boolean mRunning = false;

    private SurfaceHolder mDrawingSurface;
    private Paint mPaint;
    private Handler mReceiver;
    private Bitmap mIcon;
    private ArrayList<DrawingItem> mLocations;

    private class DrawingItem {
        int x, y;
        boolean horizontal, vertical;

        public DrawingItem(int x, int y, boolean horizontal, boolean vertical) {
            this.x = x;
            this.y = y;

            this.horizontal = horizontal;
            this.vertical = vertical;
        }

        public DrawingItem(int x, int y) {
            this.x = x;
            this.y = y;

            this.horizontal = Math.round(Math.random()) == 0;
            this.vertical = Math.round(Math.random()) == 0;
        }
    }

    public DrawingThread(SurfaceHolder holder, Bitmap icon) {
        super("DrawingThread");
        mDrawingSurface = holder;
        mLocations = new ArrayList<DrawingItem>();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIcon = icon;
    }

    @Override
    protected void onLooperPrepared() {
        mReceiver = new Handler(getLooper(), this);
        //Start the rendering
        mRunning = true;
        mReceiver.sendEmptyMessage(MSG_MOVE);
    }

    @Override
    public boolean quit() {
        // Clear all messages before dying
        mRunning = false;
        mReceiver.removeCallbacksAndMessages(null);

        return super.quit();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_ADD:
                DrawingItem newItem = new DrawingItem(msg.arg1, msg.arg2);
                mLocations.add(newItem);
                break;
            case MSG_CLEAR:
                //Remove all objects
                mLocations.clear();
                break;
            case MSG_MOVE:
                if (!mRunning) return true;

                //Render a frame
                Canvas c = mDrawingSurface.lockCanvas();

                if (c == null) {
                    break;
                }
                //Clear canvas first
                c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                //Draw each item
                for (DrawingItem item : mLocations) {
                    //Update location
                    item.x += (item.horizontal ? 5 : -5);
                    if (item.x >= (mDrawingWidth - mIcon.getWidth()) ) item.horizontal = false;
                    if (item.x <= 0) item.horizontal = true;
                    item.y += (item.vertical ? 5 : -5);
                    if (item.y >= (mDrawingHeight - mIcon.getHeight()) ) item.vertical = false;
                    if (item.y <= 0) item.vertical = true;

                    c.drawBitmap(mIcon, item.x, item.y, mPaint);
                }
                mDrawingSurface.unlockCanvasAndPost(c);
                break;
        }

        //Post the next frame
        if (mRunning) {
            mReceiver.sendEmptyMessage(MSG_MOVE);
        }
        return true;
    }

    public void updateSize(int width, int height) {
        mDrawingWidth = width;
        mDrawingHeight = height;
    }

    public void addItem(int x, int y) {
        //Pass the location into the Handler using Message arguments
        Message msg = Message.obtain(mReceiver, MSG_ADD, x, y);
        mReceiver.sendMessage(msg);
    }

    public void clearItems() {
        mReceiver.sendEmptyMessage(MSG_CLEAR);
    }
}
