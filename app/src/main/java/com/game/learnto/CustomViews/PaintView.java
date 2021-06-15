package com.game.learnto.CustomViews;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;

import androidx.annotation.Nullable;

import com.game.learnto.Frame.CharsDetector;
import com.game.learnto.Frame.ClassifierManager;
import com.game.learnto.ImageUtils;
import com.game.learnto.Model;
import com.game.learnto.R;
import com.game.learnto.classes.Manual;

import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.util.ArrayList;


public class PaintView extends View {
    public static int BRUSH_SIZE = 50;
    public static final int DEFAULT_COLOR = Color.BLACK;
    public static final int DEFAULT_BG_COLOR =  Color.WHITE;;
    private static final float TOUCH_TOLERANCE = 4;
    private float mX, mY;
    private Path mPath;
    private Paint mPaint;
    private ArrayList<FingerPath> paths = new ArrayList<>();
    private int currentColor;
    private int backgroundColor = DEFAULT_BG_COLOR;
    private int strokewidth = 20;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    private int count = 0;
    public static Handler handler = new Handler();
    public static Runnable runnable;
    public static Handler handlerLoadImage = new Handler();
    public static Runnable runnableLoadImage;
    int delay = 1000;
    private Context ctx;
    private static final int PIXEL_WIDTH = 32;
    private static  TensorBuffer  probabilityBuffer = null;
    Model model;
    ClassifierManager classifierManager= null;
    CharsDetector charsDetector = null;
    Manual manual;
    public PaintView(Context context) {
        super(context, null);
        this.ctx = context;
    }

    public PaintView(Context context, @Nullable AttributeSet attrs) throws IOException {
        super(context, attrs);
        this.ctx = context;
        mPaint = new Paint();
        mPaint.setStrokeWidth(strokewidth);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(DEFAULT_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xff);
        model = new Model(context);
        classifierManager = ClassifierManager.getInstance(context);
        charsDetector =  new CharsDetector((Activity) context);
        manual = new Manual((Activity) context);


    }

    public void Init(DisplayMetrics matrics) {
        int height = matrics.heightPixels;
        int width = matrics.widthPixels;
        mBitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        currentColor = DEFAULT_COLOR;
        strokewidth = BRUSH_SIZE;



    }

    public void Clear() {
        backgroundColor = DEFAULT_BG_COLOR;
        paths.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        mCanvas.drawColor(backgroundColor);
        for (FingerPath fp : paths) {
            mPaint.setColor(fp.color);
            mPaint.setStrokeWidth(fp.strokewidth);
            mPaint.setMaskFilter(null);
            mCanvas.drawPath(fp.path, mPaint);
        }
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.restore();
    }

    private void touchStart(float x, float y) {
        mPath = new Path();
        FingerPath fp = new FingerPath(currentColor, strokewidth, mPath);
        paths.add(fp);
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;

    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touchUp() {
        mPath.lineTo(mX, mY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handler.removeCallbacks(runnable);
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                handler.removeCallbacks(runnable);
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                handler.postDelayed(runnable = () -> {
                    handler.postDelayed(runnable, delay);
                    classifierManager.predicImagePaint(mBitmap);
                    this.Clear();
                    handler.removeCallbacks(runnable);
                }, delay);
                touchUp();
                invalidate();
                break;
        }
        return true;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void stopCoounter() {
        handler.removeCallbacks(runnable);

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = 1000;
        int desiredHeight = 1000;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;


        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);
    }

public  void LoadImage( Bitmap bm){
    handler.postDelayed(runnable = () -> {
        handler.postDelayed(runnable, delay);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bm, 700, 700, true);
        mCanvas.drawBitmap(ImageUtils.doGreyscale(scaledBitmap), 0, 0, mBitmapPaint);

        handler.removeCallbacks(runnable);
    }, delay);

}
    private int getScreenOrientation() {
        int orientation = ctx.getResources().getConfiguration().orientation;
        switch (orientation) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }



}
