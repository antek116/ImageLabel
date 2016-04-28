package kantczak.imagelabelsproject.controlers;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kantczak.imagelabelsproject.Model.ImageWithTag;


public class ImageObjectsWithTagsListController {
    ImageCutController imageCutController;
    TagsController tagsController;
    List<ImageWithTag> list;
    List<String> tagsList = new ArrayList<>();
    Context context;
    private static ImageObjectsWithTagsListController instance;
    private Uri imageUri;

    private ImageObjectsWithTagsListController(Context context,Uri imageUri) {
        this.context = context;
        this.imageUri = imageUri;
        imageCutController = ImageCutController.getInstance(context);
        list = imageCutController.getImageWithTagObjectList();
        tagsController = TagsController.getInstance(context, getFloatCutValues(), tagsList);
    }

    public static ImageObjectsWithTagsListController getInstance(Context context,Uri imageUri) {
        if (instance == null) {
            return instance = new ImageObjectsWithTagsListController(context,imageUri);
        }
        instance.imageUri = imageUri;
        return instance;
    }

    private String getImageName() {

        String uriSplit[] = imageUri.toString().split("/");
        return uriSplit[uriSplit.length - 1];
    }

    public List<ImageWithTag> getImageWithTagList() {
        File tagsFile = new File(Environment.getExternalStorageDirectory() + "/Picture&Labels/" + getImageName() + "_tags.txt");
        for (ImageWithTag imageWithTag : list) {
            tagsController.readFromFile(tagsFile, true, Double.toString(imageWithTag.getColor()));
            imageWithTag.setTags(setTagsAsString());
        }
        return list;
    }

    private String setTagsAsString() {
        tagsList = tagsController.getTagsList();
        String tagsInOneString = "";
        for (String tag : tagsList) {
            tagsInOneString += "#" + tag + " ";
        }
        return tagsInOneString;
    }

    public float[] getFloatCutValues() {
        if(list.size() > 0) {
            int[] cutValuesInt = list.get(0).getCutValues();
            float[] cutValues = new float[4];
            cutValues[0] = Float.intBitsToFloat(cutValuesInt[0]);
            cutValues[1] = Float.intBitsToFloat(cutValuesInt[1]);
            cutValues[2] = Float.intBitsToFloat(cutValuesInt[2]);
            cutValues[3] = Float.intBitsToFloat(cutValuesInt[3]);
            return cutValues;
        }
        return null;
    }
}
