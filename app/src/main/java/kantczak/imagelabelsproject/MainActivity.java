package kantczak.imagelabelsproject;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import kantczak.imagelabelsproject.fragments.FirstScreenFragment;

public class MainActivity extends AppCompatActivity {
    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("TAG", "OpenCV loaded successfully");
                    // Create and set View
                    FirstScreenFragment firstScreenFragment = new FirstScreenFragment();
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.fragment_container,firstScreenFragment).commit();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    static {
        if (!OpenCVLoader.initDebug()) {
            Log.i("OpenCV", "OpenCv initialization not successful");
        }else{
            Log.i("OpenCV","OpenCv initialization successful");
        }
    }

    public static int displayWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_view);
        displayWidth= getWindowManager().getDefaultDisplay().getWidth();
        Log.i(" ", "Trying to load OpenCV library");
        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mOpenCVCallBack))
        {
            Log.e(" ", "Cannot connect to OpenCV Manager");
        }
    }

}
