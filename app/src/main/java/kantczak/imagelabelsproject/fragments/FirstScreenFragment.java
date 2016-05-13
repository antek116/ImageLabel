package kantczak.imagelabelsproject.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;

import kantczak.imagelabelsproject.R;
import kantczak.imagelabelsproject.controlers.ImageCutController;

public class FirstScreenFragment extends Fragment implements View.OnClickListener {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private int PICK_IMAGE_REQUEST = 1;
    ImageButton galleryButton, newPhoto;
    private int RESULT_OK = -1;
    private View view;

    private String mCameraFileName;
    private String TAG = "CameraTag";
    private final String PHOTO_DIR = "/Photos/";
    private static final int NEW_PICTURE = 1;
    private String outPath;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return view = inflater.inflate(R.layout.first_screen_layout, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        galleryButton = (ImageButton) view.findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(this);
        newPhoto = (ImageButton) view.findViewById(R.id.newPhotoButton);
        newPhoto.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.galleryButton:
                onGalleryButtonClick(v);
                break;
            case R.id.newPhotoButton:
                onNewPhotoButtonClick(v);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle bundle = null;
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
          bundle =   onGalleryPickImage(data);
        } else if (requestCode == NEW_PICTURE) {
            // return from file upload
           bundle =  onCameraImageCreate(resultCode, data);
        } else {
            Toast.makeText(view.getContext(), "Ups something goes wrong", Toast.LENGTH_SHORT).show();
        }
        if(bundle != null){
            DrawFragment drawFragment = new DrawFragment();
            drawFragment.setArguments(bundle);
            android.support.v4.app.FragmentTransaction fragmentManager = getFragmentManager().beginTransaction();
            fragmentManager.replace(R.id.fragment_container, drawFragment, DrawFragment.DRAW_FRAGMENT_ID);
            fragmentManager.addToBackStack("APP");
            fragmentManager.commit();
        }
    }

    private Bundle onCameraImageCreate(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
            }
            if (uri == null && mCameraFileName != null) {
                uri = Uri.fromFile(new File(mCameraFileName));
            }
            Bundle bundle = new Bundle();
            bundle.putParcelable(DrawFragment.BUNDLE_URI_KEY,uri);
            return bundle;
        } else {
            Log.w(TAG, "Unknown Activity Result from mediaImport: "
                    + resultCode);
        }
        return null;
    }

    private Bundle onGalleryPickImage(Intent data) {
        Uri uri = data.getData();
        ImageCutController.getInstance(getContext()).loadImagesWithTagsFromFile(uri.toString(), getImageName(uri));
        Bundle bundle = new Bundle();
        bundle.putParcelable(DrawFragment.BUNDLE_URI_KEY, uri);
        return bundle;
    }

    public void onNewPhotoButtonClick(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    public void onGalleryButtonClick(View v) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
    private String getImageName(Uri uri) {
        String uriSplit[] = uri.toString().split("/");
        return uriSplit[uriSplit.length - 1];
    }
}
