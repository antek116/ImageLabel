package kantczak.imagelabelsproject.controlers;


import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class MaskController {
    private static final int MAXIMUM_COLOR = 255;

    private Context context;
    public MaskController(Context context) {
        this.context = context;
    }

    public double getNextColorFromMask(Uri imageUri){
        Mat mask = loadImageFromPath(imageUri);
        if (mask.width() > 100 || mask.height() > 100) {
            if (mask.width() > mask.height())
                mask = resizeMat(100, mask.height() / (mask.width() / 100), mask);
            else
                mask = resizeMat(mask.width() / (mask.height() / 100), 100, mask);
        }
        double color = 0;
        double[] masklast = new double[0];
        for (int i = 0; i < mask.rows(); i++)
            for (int j = 0; j < mask.cols(); j++) {
                if (i == 0 && j == 0) {
                    masklast = mask.get(0, 0);
                } else {
                    if (masklast[1] < mask.get(i, j)[1]) {
                        color = mask.get(i, j)[0];
                    }
                    masklast = mask.get(i, j);
                }
            }
        color = color + 1;
        return color;
    }
    private Mat resizeMat(int w, int h, Mat mat) {
        Size size = new Size(w, h);
        Mat sizedMat = new Mat();
        Imgproc.resize(mat, sizedMat, size);
        return sizedMat;
    }
    private Mat loadImageFromPath(Uri imagePath){
        Bitmap bitmapFromGallery = null;
        try {
            bitmapFromGallery = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imagePath);
        } catch (IOException e) {
        }
        Bitmap myBitmap32 = bitmapFromGallery.copy(Bitmap.Config.ARGB_8888, true);
        Mat mat = new Mat();
        Utils.bitmapToMat(myBitmap32, mat);
        return mat;
    }
    public Mat createMask(double color,Uri createdMaskUri, Uri existMaskUri){
        Mat createdMask = loadImageFromPath(createdMaskUri);
        Mat existMask = loadImageFromPath(existMaskUri);
        int maskwidth = createdMask.width();
        int maskheight = createdMask.height();
        Mat resizedmask = resizeMat(100, maskheight / (maskwidth / 100), createdMask);
        Mat resizedexist = resizeMat(100, existMask.height() / (existMask.width() / 100), existMask);

        for (int i = 0; i < resizedexist.rows(); i++)
            for (int j = 0; j < resizedexist.cols(); j++) {
                if (resizedexist.get(i, j)[0] < resizedmask.get(i, j)[0]) {
                    resizedexist.col(i).row(j).setTo(resizedmask.col(i).row(j));
                }
            }
        return  resizeMat(maskwidth, maskheight, resizedexist);
    }

}

