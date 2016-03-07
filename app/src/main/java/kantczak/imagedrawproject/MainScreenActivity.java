package kantczak.imagedrawproject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

public class MainScreenActivity extends AppCompatActivity {
    private Button blockButton;
    private Button drawButton;
    private Button yButton;
    private MyImageView mainImageView;
    private boolean startDraw;
    private boolean block;
    private float values[] = {0,0,0,0};
    private String bitmapPath;
    private String filename;

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.i("OpenCV","OpenCv initialization not successful");
        }else{
            Log.i("OpenCV","OpenCv initialization successful");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        Log.i(" ", "Trying to load OpenCV library");
        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mOpenCVCallBack))
        {
            Log.e(" ", "Cannot connect to OpenCV Manager");
        }



        blockButton = (Button) findViewById(R.id.blockButton);
        drawButton = (Button) findViewById(R.id.drawButton);
        yButton = (Button) findViewById(R.id.yButton);
        mainImageView = (MyImageView) findViewById(R.id.mainImageView);
        startDraw = false;
        block = false;
        yButton.setClickable(false);
        mainImageView.setClickable(true);
        filename = "Repra1";
    }

    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("", "OpenCV loaded successfully");


                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };



    public void onDrawButtonClicked(View v) {
        values = mainImageView.cutValues();
        if (!startDraw) {
            startDraw = true;
            mainImageView.startDraw(startDraw);
        }
        else{
            startDraw = false;
            mainImageView.startDraw(startDraw);
        }
        if(values[0] != 999999999)
        {
            yButton.setClickable(true);
            Toast.makeText(getApplicationContext(),"U can grabcut now",Toast.LENGTH_SHORT).show();
        }


    }

    public void onBlockButtonClicked(View v) {
        if(!block)
        {
            block = true;
            mainImageView.blockPicture(block);
        }
        else
        {
            block = false;
            mainImageView.blockPicture(block);
        }
    }
    public void grabCutIntent(View v){
        AsyncTaskParams Params = new AsyncTaskParams("Repra1",values,this,mainImageView.getImageWidth()
        ,mainImageView.getImageHeight(),null);
        new grabCutAlgorithm(this).execute(Params);
    }
    public void onGalleryButtonClicked(View v)
    {

    }
    public void onTagsButtonClicked(View v)
    {
        float x = mainImageView.getTouchPosX();
        float y = mainImageView.getTouchPosY();
        if( x == 0.0 && y == 0.0) {
            Toast.makeText(this, "First Click on object", Toast.LENGTH_SHORT).show();
        }
        int widht = mainImageView.getImageWidth();
        int height = mainImageView.getImageHeight();
        Mat img = Imgcodecs.imread(Environment.getExternalStorageDirectory().toString() + "/Picture&Labels/Repra1mask.png");
//                + BitmapName + "mask.png");
        img = resizeMat(widht,height,img);
        img.col((int)x).row((int) y).setTo(new Scalar(Color.GREEN));
        Imgcodecs.imwrite(Environment.getExternalStorageDirectory() + "/Picture&Labels/firstMask3.png", img);
        MediaScannerConnection.scanFile(this, new String[]{Environment.getExternalStorageDirectory() +
                "/Picture&Labels/firstMask3.png"}, null, null);
        double[] imgData = img.get((int)x,(int)y);
        if(imgData[1] == 0.0)
        {
            Toast.makeText(MainScreenActivity.this, "There is no Tags yet", Toast.LENGTH_SHORT).show();
        }
        else {
            double dataImage = imgData[0];
            int color = (int) dataImage;
            Intent AddTagsActivity = new Intent(this, AddTagsActivity.class);
            AddTagsActivity.putExtra("ImagePath", Environment.getExternalStorageDirectory() + "/Picture&Labels");
            AddTagsActivity.putExtra("TagsFile", "Repra1");
            AddTagsActivity.putExtra("ImageName", "Repra1" + color +".png");
            AddTagsActivity.putExtra("MaskName", "firstMask.png");
            AddTagsActivity.putExtra("CutValues", "HEHESZKI");
            AddTagsActivity.putExtra("Color", color);
            this.startActivity(AddTagsActivity);
        }
    }

    public void onDropBoxButtonClicked(View v)
    {

    }
    public Mat resizeMat(int w, int h, Mat mat)
    {
        Size size = new Size(w,h);
        Mat sizedMat = new Mat();
        Imgproc.resize(mat, sizedMat, size);
        return sizedMat;
    }
}
