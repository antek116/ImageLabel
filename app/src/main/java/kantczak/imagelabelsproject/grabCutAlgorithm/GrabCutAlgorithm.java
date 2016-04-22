package kantczak.imagelabelsproject.grabCutAlgorithm;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;

import kantczak.imagelabelsproject.Model.ImageWithTag;
import kantczak.imagelabelsproject.R;
import kantczak.imagelabelsproject.controlers.ImageCutController;
import kantczak.imagelabelsproject.fragments.AddTagsFragment;

import static org.opencv.imgproc.Imgproc.cvtColor;

public class GrabCutAlgorithm extends AsyncTask<Void, Void, Boolean> {
    private Scalar color = new Scalar(255, 0, 0, 255);
    private Context context;
    Mat dst;
    Uri bitmapUri;
    String imageName;
    float[] cutValues;
    private int imageHeight, imageWidth;
    private Double calculatedColor;
    private Point upCorner;
    private Point downCorner;
    private int actualyWidth;
    private int actualyHeight;
    private ProgressDialog pd;
    private FragmentActivity activity;

    public GrabCutAlgorithm(Context context, float[] cutValues, String bitmapName, Uri bitmapUri, int imageWidth, int imageHeight, FragmentActivity activity) {
        this.context = context;
        this.cutValues = cutValues;
        imageName = bitmapName;
        this.bitmapUri = bitmapUri;
        this.imageHeight = imageHeight;
        this.imageWidth = imageWidth;
        this.activity = activity;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        isFolderExist();
        dst = new Mat();
        Mat img = new Mat();
        Mat background = new Mat();
        upCorner = new Point();
        downCorner = new Point();
        int i = 0;
        for(float values : cutValues){
            if (values < 0) cutValues[i] = 0;
            i++;
        }
        upCorner.x = cutValues[0];
        upCorner.y = cutValues[1];
        downCorner.x = cutValues[2];
        downCorner.y = cutValues[3];

        try {
            img = getMatFromUri();
            actualyWidth = img.width();
            actualyHeight = img.height();
            img = resizeMat(imageWidth, imageHeight, img);
            background = getMatFromUri();
            background.convertTo(background, -1, 0.25, 0);
            background = resizeMat(imageWidth, imageHeight, background);
        } catch (Exception e) {
            e.printStackTrace();
        }
        backgroundSubtracting(img, background, null);
        dst = resizeMat(actualyWidth, actualyHeight, dst);
        Imgcodecs.imwrite(getPathToFile(""), dst);
        MediaScannerConnection.scanFile(context, new String[]{getPathToFile((calculatedColor.toString()))}, null, null);
        pd.dismiss();
        return true;
    }

    @Override
    protected void onPreExecute() {
        pd = ProgressDialog.show(context, "Processing",
                "Please wait, procesing ...");
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        AddTagsFragment addTagsFragment = new AddTagsFragment();
        Bundle bundle = new Bundle();
        bundle.putFloatArray(AddTagsFragment.CUT_VALUES_KEY, cutValues);
        bundle.putParcelable(AddTagsFragment.IMAGE_URI_KEY, bitmapUri);
        bundle.putString(AddTagsFragment.PATH_TO_FOLDER_KEY, Environment.getExternalStorageDirectory() + "/Picture&Labels/");
        bundle.putDouble(AddTagsFragment.CALCULATED_COLOR, calculatedColor);
        bundle.putString(AddTagsFragment.IMAGE_NAME_KEY, imageName);
        bundle.putInt(AddTagsFragment.IMAGE_WIDTH, imageWidth);
        bundle.putInt(AddTagsFragment.IMAGE_HEIGTH, imageHeight);
        addTagsFragment.setArguments(bundle);
        activity.getSupportFragmentManager().beginTransaction().
                replace(R.id.fragment_container, addTagsFragment).commit();
    }

    public Mat resizeMat(int w, int h, Mat mat) {
        Size size = new Size(w, h);
        Mat sizedMat = new Mat();
        try{
            Imgproc.resize(mat, sizedMat, size);
        }catch (Exception e){
            e.printStackTrace();
        }

        return sizedMat;
    }

    private void backgroundSubtracting(Mat img, Mat background, Mat createdMask) {
        File isMaskExist = new File(getPathToFile("_mask"));
        if (isMaskExist.exists()) {
            calculatedColor = calculateColor();
        } else {
            calculatedColor = 1.0;
        }

        Mat firstMask = new Mat();
        Mat bgModel = new Mat();
        Mat fgModel = new Mat();
        Mat mask;
        Mat source = new Mat(1, 1, CvType.CV_8U, new Scalar(3.0));
        dst = new Mat();
        Rect rect = new Rect(upCorner, downCorner);

        cvtColor(img, img, Imgproc.COLOR_RGBA2RGB);

        if (createdMask != null) {
            firstMask = createdMask;
            Imgproc.grabCut(img, firstMask, rect, bgModel, fgModel, 3, Imgproc.GC_INIT_WITH_MASK);
        } else {

            Imgproc.grabCut(img, firstMask, rect, bgModel, fgModel, 3, Imgproc.GC_INIT_WITH_RECT);
        }

        firstMask = resizeMat(actualyWidth, actualyHeight, firstMask);
        firstMask = resizeMat(imageWidth, imageHeight, firstMask);


        Core.compare(firstMask, source/* GC_PR_FGD */, firstMask, Core.CMP_EQ);
        firstMask = resizeMat(actualyWidth, actualyHeight, firstMask);
        firstMask = resizeMat(imageWidth, imageHeight, firstMask);
        Mat foreground = new Mat(img.size(), CvType.CV_8UC3, new Scalar(255,
                255, 255));
        img.copyTo(foreground, firstMask);
        firstMask = resizeMat(actualyWidth, actualyHeight, firstMask);
        firstMask = resizeMat(imageWidth, imageHeight, firstMask);
        Imgproc.rectangle(img, upCorner, downCorner, color);

        Mat tmp = new Mat();
        Imgproc.resize(background, tmp, img.size());
        background = tmp;
        mask = new Mat(foreground.size(), CvType.CV_8UC1, new Scalar(calculatedColor));

        cvtColor(foreground, mask, 6/* COLOR_BGR2GRAY */);
        Imgproc.threshold(mask, mask, 254, calculatedColor, 1 /* THRESH_BINARY_INV */);

        Mat vals = new Mat(1, 1, CvType.CV_8UC3, new Scalar(0.0));
        background.copyTo(dst);

        background.setTo(vals, mask);
        cvtColor(background, background, Imgproc.COLOR_RGBA2RGB);
        cvtColor(foreground, foreground, Imgproc.COLOR_RGBA2RGB);
        cvtColor(dst, dst, Imgproc.COLOR_RGBA2RGB);
        Core.add(background, foreground, dst, mask);
        vals.release();
        mask = resizeMat(actualyWidth, actualyHeight, mask);
        Imgcodecs.imwrite(getPathToFile("mask"), mask);
        MediaScannerConnection.scanFile(context, new String[]{getPathToFile("mask")}, null, null);
        if (calculatedColor == 1.0) {

            Imgcodecs.imwrite(getPathToFile("_mask"), mask);
            MediaScannerConnection.scanFile(context, new String[]{getPathToFile("mask")}, null, null);

        } else {
            Mat existMask = Imgcodecs.imread(getPathToFile("_mask"));
            cvtColor(existMask, existMask, Imgproc.COLOR_RGB2GRAY);
            Core.add(existMask, mask, existMask);
            Imgcodecs.imwrite(getPathToFile("_mask"), existMask);
            MediaScannerConnection.scanFile(context, new String[]{getPathToFile("_mask")}, null, null);
        }
        firstMask.release();
        source.release();
        bgModel.release();
        fgModel.release();
    }

    public boolean isFolderExist() {
        File dir = new File(Environment.getExternalStorageDirectory() + "/Picture&Labels");
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

    private Double calculateColor() {
        double color = 0;
        ArrayList arrayList = (ArrayList) ImageCutController.getInstance(context).getImageWithTagObjectList();
        for(Object item : arrayList){
            color = ((ImageWithTag)item).getColor();
        }
        return  color + 1;
    }

    private Mat getMatFromUri() throws Exception {
        Bitmap bitmapFromGallery = MediaStore.Images.Media.getBitmap(context.getContentResolver(), bitmapUri);
        Bitmap myBitmap32 = bitmapFromGallery.copy(Bitmap.Config.ARGB_8888, true);
        Mat mat = new Mat();
        Utils.bitmapToMat(myBitmap32, mat);
        return mat;
    }

    private String getPathToFile(String kind) {
        return Environment.getExternalStorageDirectory() + "/Picture&Labels/" + imageName + kind + ".png";
    }
}
