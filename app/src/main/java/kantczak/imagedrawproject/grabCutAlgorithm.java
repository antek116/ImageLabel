package kantczak.imagedrawproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;

import org.opencv.core.Core;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;

import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


/**
 * Created by pc on 2016-01-06.
 */
public class grabCutAlgorithm extends  AsyncTask<AsyncTaskParams, Void, Boolean> {
    private Scalar color = new Scalar(255, 0, 0, 255);
    private Point tl, br;
    private Bitmap bitmapResult, bitmapBackground;
    private String pathToImage;
    private Activity activity;
    private ProgressDialog pd;
    private String BitmapName;
    private float[] cutValues;
    private Point upCorner,downCorner;
    private Context context;
    private int imageWidth;
    private int imageHeight;
    private String maskPath;
    private double calculatedColor;

    private int actualyWidth;
    private int actualyHeight;


    Mat dst;

    public grabCutAlgorithm(){}

    public grabCutAlgorithm(Activity activity)
    {
        this.activity = activity;
    }


    @Override
    protected void onPreExecute() {
        pd = ProgressDialog.show(activity, "Processing",
                "Please wait, procesing ...");
    }

    @Override
    protected Boolean doInBackground(AsyncTaskParams... params) {
        this.BitmapName = params[0].BitmapName;
        this.cutValues = params[0].cutValues;
        this.context=params[0].context;
        upCorner = new Point();
        downCorner = new Point();
        upCorner.x = cutValues[0];
        upCorner.y = cutValues[1];
        downCorner.x = cutValues[2];
        downCorner.y = cutValues[3];
        imageWidth = params[0].imageWidht;
        imageHeight = params[0].imageHeight;
        maskPath = params[0].maskPath;


            dst = new Mat();
            Mat img = new Mat();
            Mat background = new Mat();
            Mat background1 = new Mat();
//            android.os.Debug.waitForDebugger();
//        img = Imgcodecs.imread(BitmapPath);
            isFolderExist();
            try {
                img = Utils.loadResource(context, R.drawable.repra);
                actualyWidth = img.width();
                actualyHeight = img.height();
                img = resizeMat(imageWidth,imageHeight,img);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
               background = Utils.loadResource(context, R.drawable.repra);
//                background.setTo(new Scalar(1,1,1));
                background.convertTo(background,-1,0.25,0);
                background = resizeMat(imageWidth,imageHeight,background);
                Log.i("GRAY","Gray size " + background.width() + " x " + background.height());
                Log.i("GRAY", "img size " + img.width() + " x " + img.height());


            } catch (IOException e) {
                e.printStackTrace();
            }
            if(maskPath != null)
            {
//                android.os.Debug.waitForDebugger();
//                Mat createdMask = Imgcodecs.imread(maskPath,0);
                Mat createdMask = Imgcodecs.imread(Environment.getExternalStorageDirectory().toString() + "/Picture&Labels/firstMask2.png");
                createdMask = resizeMat(imageWidth,imageHeight,createdMask);
                backgroundSubtracting(img, background, createdMask);
            }else
            {
                backgroundSubtracting(img, background,null);
            }
            dst = resizeMat(actualyWidth,actualyHeight,dst);
            Imgcodecs.imwrite(setPathToImage() + "/Repra1" + (int)calculatedColor +".png", dst);
            MediaScannerConnection.scanFile(context, new String[]{setPathToImage() + "/Repra1"+ (int)calculatedColor +".png"}, null, null);

        pd.dismiss();
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        Toast.makeText(activity, "please add labels", Toast.LENGTH_LONG).show();
        Intent AddTagsActivity = new Intent(context, AddTagsActivity.class);
        AddTagsActivity.putExtra("ImagePath", setPathToImage());
        AddTagsActivity.putExtra("TagsFile", "Repra1");
        AddTagsActivity.putExtra("ImageName", "Repra1"+(int)calculatedColor+".png");
        AddTagsActivity.putExtra("MaskName", "firstMask.png");
        AddTagsActivity.putExtra("CutValues", cutValues);
        AddTagsActivity.putExtra("Color", (int)calculatedColor);
        activity.startActivity(AddTagsActivity);

    }
    private void backgroundSubtracting(Mat img, Mat background,Mat createdMask) {

        File isMaskExist = new File(Environment.getExternalStorageDirectory().toString() + "/Picture&Labels/" + BitmapName + "mask.png");
        if(isMaskExist.exists())
        {
//            android.os.Debug.waitForDebugger();
            calculatedColor = calculateColor(isMaskExist);
        }
        else //Pierwsza maska wgl!
        {
            calculatedColor = 1;
        }

        Mat firstMask = new Mat();
        Mat bgModel = new Mat();
        Mat fgModel = new Mat();
        Mat mask;
        Mat source = new Mat(1, 1, CvType.CV_8U, new Scalar(3.0));
        dst = new Mat();
        Rect rect = new Rect(upCorner, downCorner);


        if(createdMask != null) {
            firstMask = createdMask;
            Imgproc.grabCut(img, firstMask, rect, bgModel, fgModel, 3, Imgproc.GC_INIT_WITH_MASK);
        }
        else {

            Imgproc.grabCut(img, firstMask, rect, bgModel, fgModel, 3, Imgproc.GC_INIT_WITH_RECT);
        }

        firstMask = resizeMat(actualyWidth,actualyHeight,firstMask);
        Imgcodecs.imwrite(setPathToImage() + "/firstMask.png", firstMask);
        MediaScannerConnection.scanFile(context, new String[]{setPathToImage() + "/firstMask.png"}, null, null);
        firstMask = resizeMat(imageWidth, imageHeight, firstMask);


        Core.compare(firstMask, source/* GC_PR_FGD */, firstMask, Core.CMP_EQ);
        firstMask = resizeMat(actualyWidth,actualyHeight,firstMask);
        Imgcodecs.imwrite(setPathToImage() + "/firstMask1.png", firstMask);
        MediaScannerConnection.scanFile(context, new String[]{setPathToImage() + "/firstMask1.png"}, null, null);
        firstMask = resizeMat(imageWidth, imageHeight, firstMask);
            Mat foreground = new Mat(img.size(), CvType.CV_8UC3, new Scalar(255,
                    255, 255));
            img.copyTo(foreground, firstMask);
        firstMask = resizeMat(actualyWidth,actualyHeight,firstMask);
        Imgcodecs.imwrite(setPathToImage() + "/firstMask2.png", firstMask);
        MediaScannerConnection.scanFile(context, new String[]{setPathToImage() + "/firstMask2.png"}, null, null);
        firstMask = resizeMat(imageWidth, imageHeight, firstMask);
        Imgproc.rectangle(img, upCorner, downCorner, color);

            Mat tmp = new Mat();
            Imgproc.resize(background, tmp, img.size());
            background = tmp;
            mask = new Mat(foreground.size(), CvType.CV_8UC1, new Scalar(calculatedColor));//255/255/255

        Imgproc.cvtColor(foreground, mask, 6/* COLOR_BGR2GRAY */);
        Imgproc.threshold(mask, mask, 254, calculatedColor, 1 /* THRESH_BINARY_INV */);//254/255/1

            Mat vals = new Mat(1, 1, CvType.CV_8UC3, new Scalar(0.0));
        background.copyTo(dst);

        background.setTo(vals, mask);
            Core.add(background, foreground, dst, mask);
            vals.release();
        mask = resizeMat(actualyWidth,actualyHeight,mask);
        Imgcodecs.imwrite(setPathToImage() + "/mask.png", mask);
        MediaScannerConnection.scanFile(context, new String[]{setPathToImage() + "/mask.png"}, null, null);
        if(calculatedColor == 1)
        {
            Imgcodecs.imwrite(Environment.getExternalStorageDirectory().toString() + "/Picture&Labels/"
                            + BitmapName + "mask.png", mask);
            MediaScannerConnection.scanFile(context, new String[]{Environment
                    .getExternalStorageDirectory().toString() + "/Picture&Labels/" + BitmapName +
                                                                "mask.png"}, null, null);
        }
        else
        {
//                       android.os.Debug.waitForDebugger();

           int maskwidth =  mask.width();
           int maskheight =  mask.height();
            Mat existMask = Imgcodecs.imread(Environment.getExternalStorageDirectory().toString() + "/Picture&Labels/"
                    + BitmapName + "mask.png");
            Mat resizedmask = resizeMat(100, maskheight / (maskwidth / 100), mask);
            Mat resizedexist = resizeMat(100,existMask.height()/(existMask.width()/100),existMask);

            Mat newMask = new Mat();
            for(int i=0;i<resizedexist.rows();i++)
                for(int j=0;j<resizedexist.cols();j++){
                    if(resizedexist.get(i,j)[0] < resizedmask.get(i,j)[0]) {
                        resizedexist.col(i).row(j).setTo(resizedmask.col(i).row(j));
                    }
                }
            newMask = resizeMat(maskwidth,maskheight,resizedexist);
            Imgcodecs.imwrite(Environment.getExternalStorageDirectory().toString() + "/Picture&Labels/"
                    + BitmapName + "mask.png", newMask);
            MediaScannerConnection.scanFile(context, new String[]{Environment
                    .getExternalStorageDirectory().toString() + "/Picture&Labels/" + BitmapName +
                    "mask.png"}, null, null);
        }


        firstMask.release();
        source.release();
        bgModel.release();
        fgModel.release();

    }
    public boolean isFolderExist()
    {
        File dir = new File(Environment.getExternalStorageDirectory() + "/Picture&Labels");
        String filename = dir.getName();
        String path = dir.getPath();
        Log.i("DirPath", "DirPath : " + path);
        if (!dir.exists()) {
            Log.i("tag", "Directory Does Not Exist, Create It");
           boolean success = dir.mkdirs();
            MediaScannerConnection.scanFile(context, new String[]{path}, null, null);
            if (success) {
                Log.i("tag", "Directory Created");
                return true;
            } else {
                Log.i("tag", "Failed - Error");
                 return false;
            }
        }
        return true;
    }
    public String setPathToImage(){
        String path = Environment.getExternalStorageDirectory() + "/Picture&Labels";
        return path;
    }
    public Mat resizeMat(int w, int h, Mat mat)
    {
        Size size = new Size(w,h);
        Mat sizedMat = new Mat();
        Imgproc.resize(mat,sizedMat,size);
        return sizedMat;
    }
    public double calculateColor(File file)
    {
//        android.os.Debug.waitForDebugger();
        Mat mask = Imgcodecs.imread(file.getPath());

            if(mask.width() > 100 || mask.height() > 100)
            {
                if(mask.width()> mask.height())
                    mask= resizeMat(100,mask.height()/(mask.width()/100),mask);
                else
                    mask = resizeMat(mask.width()/ (mask.height()/100),100,mask);
            }
        Imgcodecs.imwrite(Environment.getExternalStorageDirectory().toString() + "/Picture&Labels/"
                + BitmapName + "mask1.png", mask);
        MediaScannerConnection.scanFile(context, new String[]{Environment
                .getExternalStorageDirectory().toString() + "/Picture&Labels/" + BitmapName +
                "mask1.png"}, null, null);
        double color = 0;
        double[] masklast = new double[0];
        for(int i=0; i<mask.rows();i++)
            for(int j=0;j<mask.cols();j++) {
                double[] masks = mask.get(i,j);
                if (i == 0 && j == 0) {
                    masklast = mask.get(0, 0);
                } else {
                    if (masklast[1] < mask.get(i,j)[1]) {
                        color = mask.get(i, j)[0];
                    }
                    masklast = mask.get(i, j);
                }

            }
//        android.os.Debug.waitForDebugger();
        color = color + 1;
       return color;
    }
}
