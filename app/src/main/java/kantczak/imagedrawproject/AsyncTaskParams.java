package kantczak.imagedrawproject;

import android.content.Context;

/**
 * Created by pc on 2016-01-06.
 */
public class AsyncTaskParams {
    String BitmapName;
    float[] cutValues;
    Context context;
    int imageWidht;
    int imageHeight;
    String maskPath;
    AsyncTaskParams(String bitmapName, float[] cutValues,Context context,int width,int height,String maskPath){
        this.BitmapName = bitmapName;
        this.cutValues = cutValues;
        this.context = context;
        this.imageWidht = width;
        this.imageHeight = height;
        this.maskPath = maskPath;
    }
}
