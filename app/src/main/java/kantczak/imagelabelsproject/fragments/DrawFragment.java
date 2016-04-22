package kantczak.imagelabelsproject.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;

import java.io.File;
import java.io.IOException;

import kantczak.imagelabelsproject.MainActivity;
import kantczak.imagelabelsproject.R;
import kantczak.imagelabelsproject.grabCutAlgorithm.GrabCutAlgorithm;
import kantczak.imagelabelsproject.myDrawView.MyDrawView;
import kantczak.imagelabelsproject.uploadOnDropBox.UploadPicture;

public class DrawFragment extends Fragment {
    public final static String BUNDLE_URI_KEY = "ImageUri";
    public final static String DRAW_FRAGMENT_ID = "DRAWFragmentId";
    View view;
    MyDrawView myDrawView;
    ImageButton drawButton, grabCutButton, tagsButton,dropboxButton;
    private boolean startDraw = false;
    private float[] values;
    private Uri imageUri;


    final static private String APP_KEY = "pr26tr7zpoiekdr";
    final static private String APP_SECRET = "us6kqpncbja7iud";
    final static private String APP_KEY_NAME = "APP_KEY_NAME";
    final static private String APP_SECRET_NAME = "APP_SECRET_NAME";
    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private DropboxAPI<AndroidAuthSession> mDBApi;
    private AndroidAuthSession session;
    private String maskFileName;
    private String tagsFileName;
    private boolean setLogIn;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        session = buildSession();
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
        setLoggedIn(mDBApi.getSession().isLinked());
        view = inflater.inflate(R.layout.draw_fragment_layout, container, false);

        Bundle bundle = this.getArguments();
        imageUri = bundle.getParcelable(BUNDLE_URI_KEY);
        buttonsSetUp();
        maskFileName = getImageName() + "_mask.png";
        tagsFileName = getImageName() + "_tags.txt";
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Drawable drawableFromGallery = loadImageFromUri();
        myDrawView = (MyDrawView) view.findViewById(R.id.mainImageView);
        if (drawableFromGallery != null) {
            myDrawView.setDrawable(drawableFromGallery, MainActivity.displayWidth);
        }
        myDrawView.invalidate();
    }

    private void buttonsSetUp() {
        drawButton = (ImageButton) view.findViewById(R.id.drawButton);
        grabCutButton = (ImageButton) view.findViewById(R.id.grabcutButton);
        tagsButton = (ImageButton) view.findViewById(R.id.tagsButton);
        dropboxButton = (ImageButton) view.findViewById(R.id.dropBoxButton);
        grabCutButton.setClickable(false);
        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                values = myDrawView.cutValues();
                if (!startDraw) {
                    myDrawView.blockPicture(true);
                    startDraw = true;
                    myDrawView.startDraw(true);
                } else {
                    startDraw = false;
                    myDrawView.startDraw(false);
                    myDrawView.blockPicture(false);
                }
                if (values[0] != Integer.MAX_VALUE) {
                    grabCutButton.setClickable(true);
                    Toast.makeText(getContext(), "U can grabcut now", Toast.LENGTH_SHORT).show();
                }
            }
        });
        grabCutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GrabCutAlgorithm(getContext(), values, getImageName(), imageUri
                        , myDrawView.getImageWidth(), myDrawView.getImageHeight(), getActivity()).execute();
            }


        });
        tagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(TagListFragment.IMAGE_URI_KEY, imageUri);
                TagListFragment listFragment = new TagListFragment();
                listFragment.setArguments(bundle);
                getFragmentManager().beginTransaction().
                        replace(R.id.fragment_container, listFragment)
                        .addToBackStack("APP").commit();
            }
        });
        dropboxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (!setLogIn) {
                    mDBApi.getSession().startOAuth2Authentication(getContext());
                }
                setLoggedIn(true);

                if (mDBApi.getSession().getOAuth2AccessToken() != null) {
                    if(isOnline()) {
                        File maskFile = new File(Environment.getExternalStorageDirectory().toString() + "/Picture&Labels/"
                                + maskFileName);
                        File tagsFile = new File(Environment.getExternalStorageDirectory().toString() + "/Picture&Labels/"
                                + tagsFileName);
                        UploadPicture uploadPicture = new UploadPicture(getContext(), mDBApi,
                                "/", tagsFile, maskFile);
                        uploadPicture.execute();
                    }else
                    {
                        Toast.makeText(getContext(), "Upload Failed : Check Internet Connection", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(getContext(), "First log in to Dropbox account", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private Drawable loadImageFromUri() {
        Drawable drawable = null;
        try {
            if (imageUri != null) {
                drawable = Drawable.createFromStream(view.getContext().getContentResolver().openInputStream(imageUri), imageUri.toString());
            }
        } catch (IOException e) {
            Toast.makeText(getContext(), "Somethig goes wrong... Try again", Toast.LENGTH_SHORT).show();
        }
        return drawable;
    }

    private String getImageName() {
        String pathToImage = imageUri.toString();
        String[] imagePath = pathToImage.split("/");
        return imagePath[imagePath.length - 1];
    }

    @Override
    public void onResume() {
        super.onResume();
        AndroidAuthSession session = mDBApi.getSession();

        // The next part must be inserted in the onResume() method of the
        // activity from which session.startAuthentication() was called, so
        // that Dropbox authentication completes properly.
        if (session.authenticationSuccessful()) {
            try {
                // Mandatory call to complete the auth
                session.finishAuthentication();

                // Store it locally in our app for later use
                storeAuth(session);
            } catch (IllegalStateException e) {
                Toast.makeText(getContext(),"Couldn't authenticate with Dropbox:", Toast.LENGTH_SHORT).show();
                Log.i("auth error", "Error authenticating", e);
            }
        }
    }
    private void storeAuth(AndroidAuthSession session) {
        String oauth2AccessToken = session.getOAuth2AccessToken();
        if (oauth2AccessToken != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(APP_KEY_NAME, "oauth2:");
            edit.putString(APP_SECRET_NAME, oauth2AccessToken);
            edit.commit();
            return;
        }
    }
    private void loadAuth(AndroidAuthSession session) {
        SharedPreferences prefs = getActivity().getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(APP_KEY_NAME, null);
        String secret = prefs.getString(APP_SECRET_NAME, null);
        if (key == null || secret == null || key.length() == 0 || secret.length() == 0) return;

        if (key.equals("oauth2:")) {
            // If the key is set to "oauth2:", then we can assume the token is for OAuth 2.
            session.setOAuth2AccessToken(secret);
        } else {
            // Still support using old OAuth 1 tokens.
            session.setAccessTokenPair(new AccessTokenPair(key, secret));
        }
    }
    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);

        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        loadAuth(session);
        return session;
    }
    public void setLoggedIn(boolean operation)
    {
        this.setLogIn = operation;
    }
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}
