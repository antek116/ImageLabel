package kantczak.imagedrawproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc on 2016-01-05.
 */
public class MyImageView extends View {
    private Drawable image;
    private Mat image1;
    private Bitmap UserDraw;
    private Canvas UserCanvas;
    private Paint mPaint;
    private Path mPath;
    private Paint mBitmapPaint;
    private Paint circlePaint;
    private Path circlePath;
    private Bitmap UserBitmap;

    private boolean isDraw;
    private boolean isBlock;

    private float cutValues[] = {999999999,999999999,0,0};
    private float imageWidth;
    private float imageHeight;
    private float scaleMaskImage;

    private float mPosX;
    private float mPosY;
    private float mLastTouchX;
    private float mLastTouchY;

    private float touchPosX;
    private float touchPosY;

    private static final int INVALID_POINTER_ID = -1;
    // The ‘active pointer’ is the one currently moving our object.
    private int mActivePointerId = INVALID_POINTER_ID;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    public class PointsToChange
    {
        private float x;
        private float y;
        private int color;
        public PointsToChange(){};

        public void addX(float x)
        {
            this.x = x;
        }
        public void addY(float y)
        {
            this.y = y;
        }
        public void setColor(int color){this.color = color;}
        public float getX(){return this.x;}
        public float getY(){return this.y;}
        public int getColor(){return this.color;}

    }
    private PointsToChange PointsToChange;
    private List<PointsToChange> AllPointsToChange;

    private int color;
    private String option;
    private String maskPath = null;
    private Bitmap mask;


    public MyImageView(Context context) {
        super(context);
    }

    public MyImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        circlePaint = new Paint();
        circlePath = new Path();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.BLUE);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeJoin(Paint.Join.MITER);
        circlePaint.setStrokeWidth(4f);

        isDraw= false;
        isBlock = false;
    }

    public MyImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        image = context.getResources().getDrawable(R.drawable.repra);
        image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        BitmapDrawable bitmapDrawable = (BitmapDrawable) image;

        UserBitmap = bitmapDrawable.getBitmap();
        UserDraw = Bitmap.createBitmap(image.getIntrinsicWidth(), image.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        UserCanvas = new Canvas(UserDraw);
        imageWidth = image.getIntrinsicWidth();
        imageHeight = image.getIntrinsicHeight();
        AllPointsToChange = new ArrayList<PointsToChange>();
        option = null;
        mask = null;
    }


    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(mPosX, mPosY);
        canvas.scale(mScaleFactor, mScaleFactor);
        canvas.drawBitmap(UserDraw, 0, 0, mPaint);
        canvas.drawPath(mPath, mPaint);
        canvas.drawPath(circlePath, circlePaint);
        image.draw(UserCanvas);
        canvas.restore();
    }

    private void touch_start(float x, float y) {
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }
    private void touch_move(float x, float y) {

            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
                circlePath.reset();
                circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
        }
    }
    private void touch_up() {
            mPath.lineTo(mX, mY);
            circlePath.reset();
            // commit the path to our offscreen
            UserCanvas.drawPath(mPath, mPaint);
            // kill this so we don't double draw
//
        if(maskPath !=null) {
            CalculateMask newMask = new CalculateMask(maskPath, AllPointsToChange, image, color,this.getContext());
            newMask.execute();
        }
    }
    public void myDraw(MotionEvent event, float mPosX, float mPosY)
    {

        float x = (event.getX() + Math.abs(mPosX))/mScaleFactor;
        float y = (event.getY() + Math.abs(mPosY))/mScaleFactor;
        if(option != null) {
            if(option == "ADD")
                color = 1;
            if(option == "DELETE")
                color = 0;
            if(((int)x > 1 && (int)y > 1) && ((int)x <= getImageWidth() && (int)y <= getImageHeight())) {
                PointsToChange = new PointsToChange();
                PointsToChange.addX(x);
                PointsToChange.addY(y);
                PointsToChange.setColor(color);
                AllPointsToChange.add(PointsToChange);
            }
        }
        getCutValues(x, y);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;

        }
    }



    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(!isBlock) {
            mScaleDetector.onTouchEvent(ev);

            final int action = ev.getAction();
            switch (action & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN: {
                    final float x = ev.getX();
                    final float y = ev.getY();

                    mLastTouchX = x;
                    mLastTouchY = y;
                    mActivePointerId = ev.getPointerId(0);
                    break;
                }

                case MotionEvent.ACTION_MOVE: {
                    final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                    final float x = ev.getX(pointerIndex);
                    final float y = ev.getY(pointerIndex);
                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;
                    int width = getWidth();
                    int measureWidth = image.getIntrinsicWidth();

                    if (mScaleDetector.isInProgress()) {

                        if (Math.abs(mPosX) > ((image.getIntrinsicWidth() * mScaleFactor) - getWidth()))
                            mPosX = -((image.getIntrinsicWidth() * mScaleFactor) - getWidth());
                        if (Math.abs(mPosY) >= getHeight() / 2)
                            mPosY = -((image.getIntrinsicHeight() * mScaleFactor) - getHeight());
                        else if (mPosY < 0)
                            mPosY = 0;

                    }
                    // Only move if the ScaleGestureDetector isn't processing a gesture.
                    if (!mScaleDetector.isInProgress()) {

                        if (dx < 0) {
                            if ((Math.abs(mPosX) + Math.abs(dx)) < (image.getIntrinsicWidth() * mScaleFactor) - getWidth())
                                mPosX += dx;
                            else
                                mPosX = -((image.getIntrinsicWidth() * mScaleFactor) - getWidth());
                        } else {
                            if (Math.abs(mPosX) - dx > 0)
                                mPosX += dx;
                            else
                                mPosX = 0;
                        }
                        if (getHeight() < (image.getIntrinsicHeight() * mScaleFactor))
                            if (dy > 0) {
                                if (mPosY == 0 || mPosY + dy >= 0)
                                    mPosY = 0;
                                else
                                    mPosY += dy;
                            } else {
                                if ((Math.abs(mPosY + dy) <= ((image.getIntrinsicHeight() * mScaleFactor) - getHeight())))
                                    mPosY += dy;
                                else
                                    mPosY = -((image.getIntrinsicHeight() * mScaleFactor) - getHeight());
                            }

                        invalidate();
                    }
                    mLastTouchX = x;
                    mLastTouchY = y;
                    break;
                }

                case MotionEvent.ACTION_UP: {
                    mActivePointerId = INVALID_POINTER_ID;
                    break;
                }

                case MotionEvent.ACTION_CANCEL: {
                    mActivePointerId = INVALID_POINTER_ID;
                    break;
                }

                case MotionEvent.ACTION_POINTER_UP: {
                    final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                            >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                    final int pointerId = ev.getPointerId(pointerIndex);
                    if (pointerId == mActivePointerId) {
                        // This was our active pointer going up. Choose a new
                        // active pointer and adjust accordingly.
                        final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                        mLastTouchX = ev.getX(newPointerIndex);
                        mLastTouchY = ev.getY(newPointerIndex);
                        mActivePointerId = ev.getPointerId(newPointerIndex);
                    }
                    break;
                }
            }
        }
        if(isDraw)
            myDraw(ev,mPosX,mPosY);
            return true;

    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(0.25f, Math.min(mScaleFactor, 5.0f));

            invalidate();
            return true;
        }
    }

    public void setDrawable(Drawable drawable)
    {
        this.image = drawable;
        image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
        BitmapDrawable bitmapDrawable = (BitmapDrawable) image;

        UserBitmap = bitmapDrawable.getBitmap();
        UserDraw = Bitmap.createBitmap(image.getIntrinsicWidth(), image.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        UserCanvas = new Canvas(UserDraw);
        imageWidth = image.getIntrinsicWidth();
        imageHeight = image.getIntrinsicHeight();
    }
    public void startDraw(boolean start) {this.isDraw = start;}
    public void blockPicture(boolean block){this.isBlock = block;}
    public void mPathReset(){mPath.reset();}
    public void getCutValues(float x, float y){
        if(x <= imageWidth && y <=imageHeight) {//[lowest x, lowest y, highest x;highest y];
            if (this.cutValues[0] > x)
                this.cutValues[0] = x;
            if (this.cutValues[2] < x)
                this.cutValues[2] = x;
            if (this.cutValues[1] > y)
                this.cutValues[1] = y;
            if (this.cutValues[3] < y)
                this.cutValues[3] = y;
        }
    }
    public float[] cutValues(){return cutValues;}
    public Bitmap getUserBitmap(){return UserBitmap;}
    public int getImageWidth() {return image.getIntrinsicWidth();}
    public int getImageHeight(){return image.getIntrinsicHeight();}
    public void setBitmap(Bitmap bitmap){image = new BitmapDrawable(getResources(),bitmap);}

    public void setOption(String option){
      this.option = option;
    }
    public void setMaskPath(String maskPath)
    {
        this.maskPath = maskPath;
    }
    public float getTouchPosX(){return mLastTouchX;}
    public float getTouchPosY(){return mLastTouchY;}
}
