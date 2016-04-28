package kantczak.imagelabelsproject.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kantczak.imagelabelsproject.R;
import kantczak.imagelabelsproject.controlers.TagsController;
import kantczak.imagelabelsproject.controlers.ImageCutController;


public class AddTagsFragment extends Fragment {
    View view;
    private Button addButton, finishButton;
    private ListView tagsView;
    private EditText Tag;
    private Context context;
    public static String CUT_VALUES_KEY = "CutValuesKey";
    public static String IMAGE_URI_KEY = "ImageUriKey";
    public static String PATH_TO_FOLDER_KEY = "PathToFolderKey";
    public static String IMAGE_NAME_KEY = "ImageNameKey";
    public static String CALCULATED_COLOR = "CalculatedColor";
    public static String IMAGE_WIDTH = "ImageWidthGrabCut";
    public static String IMAGE_HEIGTH ="ImageHeightGrabCut";
    float[] cutValues;
    Uri imageUri;
    String pathToFile;
    double color;
    private List<String> tagsList;
    private File tagsFile;
    private String imageName;
    private StableArrayAdapter adapter;
    private TagsController tagsController;
    private int imageWidth, imageHeight;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.add_tags_layout, container, false);
        initialView();
        tagsList = new ArrayList<>();
        onButtonClickConfiguration();
        initialTagsFile();
        return view;
    }

    private Drawable loadDrawableFromUri(){
        Drawable drawable = null;
        try {
            drawable =  Drawable.createFromStream(context.getContentResolver().openInputStream(imageUri), imageUri.toString());
        } catch (FileNotFoundException e) {
            Toast.makeText(context,"Cannot load cuted image",Toast.LENGTH_SHORT).show();
        }
        return drawable;
    }
    private void initialView() {
        addButton = (Button) view.findViewById(R.id.tagsAddButton);
        finishButton = (Button) view.findViewById(R.id.tagsFinishButton);
        ImageView addTagsImageVIew = (ImageView) view.findViewById(R.id.addTagsImageView);
        tagsView = (ListView) view.findViewById(R.id.tagsListView);
        Tag = (EditText) view.findViewById(R.id.editText);
        Bundle bundle = this.getArguments();
        imageUri = bundle.getParcelable(IMAGE_URI_KEY);
        cutValues = bundle.getFloatArray(CUT_VALUES_KEY);
        pathToFile = bundle.getString(PATH_TO_FOLDER_KEY);
        color = bundle.getDouble(CALCULATED_COLOR);
        imageName = bundle.getString(IMAGE_NAME_KEY);
        imageWidth = bundle.getInt(IMAGE_WIDTH);
        imageHeight = bundle.getInt(IMAGE_HEIGTH);
        context = getContext();
        ImageCutController cutController = ImageCutController.getInstance(context);
        Drawable drawable = loadDrawableFromUri();
        if(drawable != null){
            addTagsImageVIew.setImageBitmap(cutController.loadImage(imageUri, cutValuesToInteger(),imageWidth,imageHeight));
        }
    }

    private int[] cutValuesToInteger() {
        int i = 0;
        int[] cutValuesInt = new int[4];
        for (float cutvalue: cutValues) {
            cutValuesInt[i] = Math.round(cutvalue);
            i++;
        }
        return cutValuesInt;
    }

    private void initialTagsFile() {
        tagsController = TagsController.getInstance(context, cutValues, tagsList);
        tagsController.setCutValues(cutValues);
        tagsController.saveImageWithTagToFile(pathToFile,imageName,color,imageUri,imageHeight,imageWidth);
        tagsFile = new File(pathToFile + "/" + imageName + "_tags.txt");
        if (!tagsFile.exists()) {
                tagsController.writeHeader(tagsFile, Double.toString(color));
            adapter = new StableArrayAdapter(context,
                    android.R.layout.simple_list_item_1, tagsList);
                MediaScannerConnection.scanFile(context, new String[]{pathToFile + "/" + imageName + "_Tags.txt"}, null, null);
        } else {
            tagsController.readFromFile(tagsFile, true, Double.toString(color));
            adapter = new StableArrayAdapter(context,
                    android.R.layout.simple_list_item_1, tagsList);
        }
        tagsView.setAdapter(adapter);
    }
    public void onButtonClickConfiguration() {
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String addedTag = Tag.getText().toString();
                if (addedTag.matches("") || addedTag.matches(" "))
                    Toast.makeText(context, "Tag field can not by empty", Toast.LENGTH_SHORT).show();
                else {
                    Tag.setText("");
                    tagsList.add(addedTag);
                    tagsController.writeToFile(tagsFile, true, Double.toString(color), addedTag);
                    MediaScannerConnection.scanFile(context, new String[]{pathToFile + "/" + imageName + "_Tags.txt"}, null, null);
                    adapter.addNewTags(tagsList);
                }
            }
        });
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.support.v4.app.FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                DrawFragment drawFragment = (DrawFragment)fragmentManager.findFragmentByTag(DrawFragment.DRAW_FRAGMENT_ID);
                ImageCutController.getInstance(getContext()).loadImagesWithTagsFromFile(imageUri.toString(), imageName);
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, drawFragment).commit();
            }
        });
    }
    public class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        public void addNewTags(List<String> objects) {
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
                notifyDataSetChanged();
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }
}
