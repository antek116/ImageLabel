package kantczak.imagedrawproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class NewMaskActivity extends AppCompatActivity {
    private Button BlockButton;
    private Button DrawButton;
    private Button GrabCutButton;
    private Button AddButton;
    private Button DeleteButton;
    private Button CorrectButton;
    private Button NotCorrectButton;
    private Bitmap mask;
    private Bitmap actualyImg;
    private MyImageView MaskImageView;
    private Drawable ImageDrawable;

    private boolean block;
    private boolean startDraw;
    private boolean selectedColor;
    private int Black = 0;
    private int White = 255;


    private String FolderPath;
    private String ImageName;
    private String MaskName;
    private float[] cutValues;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_mask);
        BlockButton = (Button) findViewById(R.id.maskBlockButton);
        DrawButton = (Button) findViewById(R.id.maskDrawButton);
        GrabCutButton = (Button) findViewById(R.id.maskGrabCutButton);
        AddButton = (Button) findViewById(R.id.maskAddButton);
        DeleteButton = (Button) findViewById(R.id.maskDeleteButton);
        CorrectButton = (Button) findViewById(R.id.maskCorrectButton);
        NotCorrectButton = (Button) findViewById(R.id.maskNotCorrectButton);
        MaskImageView = (MyImageView) findViewById(R.id.maskImageView);

        Bundle extras = getIntent().getExtras();
        if(extras != null)
        {
            FolderPath = extras.get("ImagePath").toString();
            ImageName = extras.get("ImageName").toString();
            MaskName = extras.get("MaskName").toString();
            cutValues = extras.getFloatArray("CutValues");
            actualyImg = BitmapFactory.decodeFile(FolderPath + "/" + ImageName);
            mask = BitmapFactory.decodeFile(FolderPath + "/" + MaskName);
            ImageDrawable = Drawable.createFromPath(FolderPath + "/" + ImageName);
            MaskImageView.setDrawable(ImageDrawable);
            MaskImageView.setMaskPath(FolderPath + "/" + MaskName);

        }else
        {
            Toast.makeText(this,"Something goes Wrong",Toast.LENGTH_SHORT).show();
        }

        startDraw = false;
        block = false;
        selectedColor = false;
    }
    public void onMaskBlockButtonClicked(View v)
    {
        if(!block)
        {
            block = true;
            MaskImageView.blockPicture(block);
        }
        else
        {
            block = false;
            MaskImageView.blockPicture(block);
        }
    }
    public void onMaskDrawButtonClicked(View v)
    {
//        if(selectedColor)
//        {
            if (!startDraw) {
                startDraw = true;
                MaskImageView.startDraw(startDraw);
            }
            else{
                startDraw = false;
                MaskImageView.startDraw(startDraw);
            }
//        }
//        else {
//            Toast.makeText(this,"Please select Add or Delete Button",Toast.LENGTH_SHORT).show();
//        }
    }
    public void onMaskDeleteButtonClicked(View v)
    {
        MaskImageView.setOption("DELETE");
    }
    public void onMaskAddButtonClicked(View v)
    {
        MaskImageView.setOption("ADD");
    }
    public void onMaskGrabCutButtonClicked(View v) {
        AsyncTaskParams Params = new AsyncTaskParams("ABCD",cutValues,this,MaskImageView.getImageWidth()
                ,MaskImageView.getImageHeight(),FolderPath + "/" + "firstMask2.pmg");
        new grabCutAlgorithm(this).execute(Params);
    }

}
