package kantczak.imagedrawproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * Created by pc on 2016-01-12.
 */
public class CalculateMask extends AsyncTask<Void,String,Void> {

    private String maskPath;
    private List<MyImageView.PointsToChange> AllPointsToChange;
    private Drawable image;
    private int color;
    private Bitmap mask;
    private int actualyMaskWidth;
    private int actualyMaskHeight;
    private Bitmap CopyMask;
    private Context context;
    private Mat newMask;

    public CalculateMask(String maskPath, List<MyImageView.PointsToChange> AllPointsToChange,
                         Drawable image, int color, Context context) {
        this.maskPath = maskPath;
        this.AllPointsToChange = AllPointsToChange;
        this.image = image;
        this.color = color;
        this.context = context;
    }


    @Override
    protected Void doInBackground(Void... params) {
//        android.os.Debug.waitForDebugger();
        newMask = Imgcodecs.imread(maskPath, 0);
        actualyMaskWidth = newMask.width();
        actualyMaskHeight = newMask.height();
        newMask = resizeMat(image.getIntrinsicWidth(), image.getIntrinsicHeight(), newMask);
        setNewMat(newMask);
        return null;
    }

    public float calculateScale(Drawable image, Bitmap mask) {
        int imageWidth = image.getIntrinsicWidth();
        int imageHeight = image.getIntrinsicHeight();
        int maskWidth = mask.getWidth();
        int maskHeight = mask.getHeight();

        float scaleWidth = maskWidth / imageWidth;
        float scaleHeight = maskHeight / imageHeight;
        if (scaleHeight == scaleWidth)
            return scaleWidth;
        else return 0;
    }

    public Bitmap resizeMask(Bitmap mask, int width, int height) {

        Bitmap resizedMask = Bitmap.createScaledBitmap(mask, width, height, false);
        return resizedMask;
    }
    public Mat resizeMat(int w, int h, Mat mat) {
        Size size = new Size(w, h);
        Mat sizedMat = new Mat();
        Imgproc.resize(mat, sizedMat, size);
        return sizedMat;
    }
    public void setNewMat(Mat newMask) {
        for (MyImageView.PointsToChange points : AllPointsToChange) {
            int x = (int) points.getX();
            int y = (int) points.getY();
            newMask.col(x).row(y).setTo(new Scalar(color));
        }
        int newMaskWidth = newMask.width();
        int newMaskHeight = newMask.height();
        newMask = resizeMat(actualyMaskWidth,actualyMaskHeight,newMask);
        int newMasknewWidth = newMask.width();
        int newMasknewHeight = newMask.height();
        Imgcodecs.imwrite(Environment.getExternalStorageDirectory().toString() + "/Picture&Labels/firstMask.png", newMask);
        Imgcodecs.imwrite(Environment.getExternalStorageDirectory().toString() + "/Picture&Labels/firstMask1.png",newMask);
    }
}
