package kantczak.imagelabelsproject.controlers;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.LruCache;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kantczak.imagelabelsproject.Model.ImageWithTag;

public class ImageCutController {

    private int[] cutValues;
    private Context context;
    private LruCache<String, Bitmap> mLruCache;
    private static ImageCutController instance = null;
    private File imageWithTagFile;
    private ArrayList<ImageWithTag> imageWithTagArrayList;
    private ImageCutController(Context context) {
        this.context = context;
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        this.imageWithTagArrayList = new ArrayList<>();
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mLruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }


    public static ImageCutController getInstance(Context context) {
        if (instance == null) {
            return instance = new ImageCutController(context);
        } else {
            return instance;
        }
    }


    public Bitmap loadImage(Uri imageUri, int[] imageCut, int imageWidth, int imageHeight) {
        String imagePath = imageUri.toString() + imageCut[0];
        this.cutValues = imageCut;
        Bitmap bitmap = null;
        if ((bitmap = mLruCache.get(imagePath)) == null) {
            return addImageToLruCache(imageUri, imageWidth, imageHeight);
        } else {
            return bitmap;
        }
    }

    private Bitmap addImageToLruCache(Uri imageUri, int imageWidth, int imageHeight) {
        Bitmap bitmap;
        String imageName = getImageName(imageUri);
        Mat bitmap_mask = Imgcodecs.imread(Environment.getExternalStorageDirectory() + "/Picture&Labels/" + imageName + "_mask" + ".png");
        Size matSize = new Size(imageWidth,imageHeight);
        Imgproc.resize(bitmap_mask,bitmap_mask,matSize);
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
            bitmap = resizeBitmapToSpecificSize(bitmap, imageWidth, imageHeight);
            bitmap = multipleMaskAndBitmap(bitmap, bitmap_mask);
        } catch (IOException e) {
            bitmap = null;
        }
        if (bitmap != null) {
            bitmap = cutAndResizeBitmap(bitmap);
            mLruCache.put(imageUri.toString() + cutValues[0], bitmap);
        }
        return bitmap;
    }

    private Bitmap multipleMaskAndBitmap(Bitmap bitmap, Mat bitmap_mask) {
        Mat imgMAT = new Mat();
        Utils.bitmapToMat(bitmap, imgMAT);
        Imgproc.cvtColor(imgMAT,imgMAT,Imgproc.COLOR_BGRA2BGR);
        Mat bitmapAndMask = bitmap_mask.mul(imgMAT);
        Utils.matToBitmap(bitmapAndMask,bitmap);
        imgMAT.release();
        bitmapAndMask.release();
        return bitmap;
    }

    private String getImageName(Uri imageUri) {
        String pathToImage = imageUri.toString();
        String[] imagePath = pathToImage.split("/");
        return imagePath[imagePath.length - 1];
    }

    private Bitmap resizeBitmapToSpecificSize(Bitmap bitmap, int imageWidth, int imageHeight) {
        return Bitmap.createScaledBitmap(bitmap, imageWidth, imageHeight, false);
    }

    private Bitmap cutAndResizeBitmap(Bitmap bitmap) {
        int bitmapWidth = calculateWidthFromCutValues();
        int bitmapHeight = calculateHeightFromCutValues();
        Bitmap cutBitmap = Bitmap.createBitmap(bitmapWidth,
                bitmapHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(cutBitmap);
        Rect srcRect = new Rect(cutValues[0], cutValues[1], cutValues[2],
                cutValues[3]);
        Rect desRect = new Rect(0, 0, bitmapWidth, bitmapHeight);
        canvas.drawBitmap(bitmap, srcRect, desRect, null);
        return cutBitmap;
    }

    private int calculateWidthFromCutValues() {
        return cutValues[2] - cutValues[0];
    }

    private int calculateHeightFromCutValues() {
        return cutValues[3] - cutValues[1];
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void loadImagesWithTagsFromFile(String pathToFile,String imageName) {
        Uri imageUri = null;
        double color = 0.0;
        int i=0;
        imageWithTagArrayList.clear();
        imageWithTagFile = new File(Environment.getExternalStorageDirectory() + "/Picture&Labels/" + imageName +"_Details.txt");
        MediaScannerConnection.scanFile(context, new String[]{Environment.getExternalStorageDirectory() + "/Picture&Labels/" + imageName +"_Details.txt"}, null, null);
        int[] cutValues = new int[0];
        try (BufferedReader reader = new BufferedReader(new FileReader(imageWithTagFile))) {
            String line;
            if (imageWithTagFile.exists()) {
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(" ");
                    if (parts[0].equals("KOLOR")) {
                        color = Double.parseDouble(parts[2]);
                        i++;
                        continue;
                    }
                    if(i==1){
                        imageUri = Uri.parse(line);
                        i++;
                        continue;
                    }
                    if(i==2){
                        cutValues = new int[4];
                        String stringCutValues[] = line.split(" ");
                        cutValues[0] = Integer.parseInt(stringCutValues[0]);
                        cutValues[1] = Integer.parseInt(stringCutValues[1]);
                        cutValues[2] = Integer.parseInt(stringCutValues[2]);
                        cutValues[3] = Integer.parseInt(stringCutValues[3]);
                        i++;
                        continue;
                    }
                    if(i==3){
                        String stringSize[] = line.split(" ");
                        int[] imageSize = new int[2];
                        imageSize[0] = Integer.parseInt(stringSize[0]);
                        imageSize[1] = Integer.parseInt(stringSize[1]);
                        imageWithTagArrayList.add(new ImageWithTag(imageUri,cutValues,color,imageSize[0],imageSize[1]));
                        i=0;
                    }
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public List<ImageWithTag> getImageWithTagObjectList(){
        return this.imageWithTagArrayList;
    }
}
