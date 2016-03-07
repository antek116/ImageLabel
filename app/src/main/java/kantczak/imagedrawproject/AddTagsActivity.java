package kantczak.imagedrawproject;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddTagsActivity extends AppCompatActivity {
    private Button addButton;
    private Button finishButton;
    private ImageView addTagsImageVIew;
    private ListView tagsView;
    private File yourFile;
    private EditText Tag;
    private String FolderPath;
    private String ImageName;
    private String MaskName;
    private float[] cutValues;
    private Bitmap mask;
    private Bitmap actualyImg;
    private Drawable ImageDrawable;
    private String TagsFile;
    private List<String> TagsList;
    private StableArrayAdapter adapter;
    private String color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tags);
        addButton = (Button) findViewById(R.id.tagsAddButton);
        finishButton = (Button) findViewById(R.id.tagsFinishButton);
        addTagsImageVIew = (ImageView) findViewById(R.id.addTagsImageView);
        tagsView = (ListView) findViewById(R.id.tagsListView);
        Tag = (EditText) findViewById(R.id.editText);
        Bundle extras = getIntent().getExtras();
        if(extras != null)
        {
            FolderPath = extras.get("ImagePath").toString();
            ImageName = extras.get("ImageName").toString();
            MaskName = extras.get("MaskName").toString();
            cutValues = extras.getFloatArray("CutValues");
            TagsFile = extras.get("TagsFile").toString();
            color = extras.get("Color").toString();
            actualyImg = BitmapFactory.decodeFile(FolderPath + "/" + ImageName);
            mask = BitmapFactory.decodeFile(FolderPath + "/" + MaskName);
            ImageDrawable = Drawable.createFromPath(FolderPath + "/" + ImageName);
            addTagsImageVIew.setImageDrawable(ImageDrawable);
            TagsList = new ArrayList<String>();

        }else
        {
            Toast.makeText(this, "Something goes Wrong", Toast.LENGTH_SHORT).show();
        }
        yourFile = new File(FolderPath + "/" + TagsFile + "Tags.txt");
        boolean exist = yourFile.exists();
        if(!yourFile.exists()) {
            try {
                yourFile.createNewFile();
                writeHeader(yourFile, color);
                MediaScannerConnection.scanFile(this, new String[]{FolderPath + "/" + TagsFile + "Tags.txt"}, null, null);
                Toast.makeText(this, "File Created", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Cant Create File", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(this, "File Exist", Toast.LENGTH_SHORT).show();
            readFromFile(yourFile, true, color);
            adapter = new StableArrayAdapter(this,
                    android.R.layout.simple_list_item_1, TagsList);
            tagsView.setAdapter(adapter);
        }
    }
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void readFromFile(File file, boolean isExist,String colorCliked)
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean tagsFlag;
            tagsFlag = false;
            boolean colorFlag = false;
            if(isExist) {
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(" ");
                    if(line.equals("KOLOR: "+colorCliked))
                    {
                        tagsFlag = true;
                        continue;
                    }
                    if(tagsFlag && parts[0].equals("TAGS:"))
                    {
                            colorFlag = true;
                            for(int i=1; i< parts.length; i++) {
                                TagsList.add(parts[i]);

                            }
                    }
                    else{
                        tagsFlag = false;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void writeToFile(File file, boolean isExist,String colorCliked,String tag)
    {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                boolean tagsFlag,colorFlag;
                tagsFlag = false;
                colorFlag = false;
                if(isExist) {
                        while ((line = reader.readLine()) != null) {
                            String[] parts = line.split(" ");
                            if (line.equals("KOLOR: " + colorCliked)) {
                                tagsFlag = true;
                                colorFlag = true;
                                continue;
                            }
                            if (tagsFlag && parts[0].equals("TAGS:")) {
                                try (PrintWriter output = new PrintWriter(new FileWriter(file.getPath(), true))) {
                                    output.printf(" " + tag);
                                } catch (Exception e) {
                                }
                                TagsList.add(0, tag);
                            } else {
                                tagsFlag = false;
                            }
                        }
                    if(!colorFlag) // COLOR NOT FOUND, CREATE NEW THEN
                    {
                        writeHeader(file,colorCliked);
                        writeToFile(file, isExist,colorCliked,tag);
                    }
                    }
                reader.close();
            } catch (FileNotFoundException e) {
                writeHeader(file, color);
            } catch (IOException e) {
               writeHeader(file, color);
            }

        MediaScannerConnection.scanFile(this, new String[]{FolderPath + "/" + TagsFile + "Tags.txt"}, null, null);
    }
    public void onAddButtonClicked(View v)
    {
        String addedTag = Tag.getText().toString();
        if(addedTag.matches("") || addedTag.matches(" "))
            Toast.makeText(this,"Tag field can not by empty",Toast.LENGTH_SHORT).show();
        else
        {
            writeToFile(yourFile, true, color, addedTag);
            adapter = new StableArrayAdapter(this,
                    android.R.layout.simple_list_item_1, TagsList);
            tagsView.setAdapter(adapter);
            Tag.setText("");
        }
    }
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void writeHeader(File file,String colorCliked) {
        try
        {
            FileOutputStream fOut = new FileOutputStream(file,true);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append("\r\nKOLOR: " + colorCliked +"\r\n" +"TAGS:");
            myOutWriter.close();
            fOut.close();
            MediaScannerConnection.scanFile(this, new String[]{FolderPath + "/" + TagsFile + "Tags.txt"}, null, null);
        } catch(Exception e){}
    }
    public void onFinishButtonClicked(View v)
    {
        Intent mainScreenActivity = new Intent(this, MainScreenActivity.class);
        this.startActivity(mainScreenActivity);
    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
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
