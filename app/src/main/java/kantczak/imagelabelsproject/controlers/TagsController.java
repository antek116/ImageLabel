package kantczak.imagelabelsproject.controlers;


import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

public class TagsController {
    private static final String LINE_SEPARATOR = "\r\n";
    private static TagsController instance =null;
    private Context context;
    float[] cutValues;
    double color;
    private List<String> tagsList;

    private TagsController(Context context, float[] cutValues, List<String> tagsList) {
        this.context = context;
        this.cutValues = cutValues;
        this.tagsList = tagsList;
        this.tagsList.clear();
    }
    public static TagsController getInstance(Context context,float[] cutValues,List<String> tagsList){
        if(instance == null){
            return instance = new TagsController(context,cutValues,tagsList);
        }else{
            return instance;
        }
    }
    public void setCutValues(float[] cutValues){
        this.cutValues = cutValues;
    }
    public List<String> getTagsList(){
        return this.tagsList;
    }
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void readFromFile(File file, boolean isExist, String colorCliked) {
        tagsList.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean tagsFlag;
            tagsFlag = false;
            if (isExist) {
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(" ");
                    if (line.equals("KOLOR: " + colorCliked)) {
                        tagsFlag = true;
                        continue;
                    }
                    if (tagsFlag && parts[0].equals("TAGS:")) {
                        for (int i = 1; i < parts.length; i++) {
                            tagsList.add(parts[i]);
                        }
                    } else {
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
    public void writeHeader(File file, String colorCliked) {
        try {
            FileOutputStream fOut = new FileOutputStream(file, true);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append("\r\nKOLOR: " + colorCliked + "\r\n" + "TAGS:");
            myOutWriter.close();
            fOut.close();
        } catch (Exception e) {
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void writeToFile(File file, boolean isExist, String colorCliked, String tag) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean tagsFlag, colorFlag;
            tagsFlag = false;
            colorFlag = false;
            if (isExist) {
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
//                        tagsList.add(0, tag);
                    } else {
                        tagsFlag = false;
                    }
                }
                if (!colorFlag) // COLOR NOT FOUND, CREATE NEW THEN
                {
                    writeHeader(file, colorCliked);
                    writeToFile(file, isExist, colorCliked, tag);
                }
            }
            reader.close();
        } catch (IOException e) {
            writeHeader(file, Double.toString(color));
        }
        MediaScannerConnection.scanFile(context, new String[]{file.getPath()}, null, null);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void saveImageWithTagToFile(String pathToFile, String imageName, double imageColor,Uri imageUri,int imageHeight,int imageWidth) {
        File imageWithTagFile = new File(pathToFile + "/" + imageName + "_Details.txt");
        if(!imageWithTagFile.exists()){
            try {
                imageWithTagFile.createNewFile();
            } catch (IOException e) {
                Toast.makeText(context,"Cannot create file",Toast.LENGTH_SHORT).show();
            }
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(imageWithTagFile))) {
            String line;
            if (imageWithTagFile.exists()) {
                do{
                    line = reader.readLine();
                    if (line != null && line.equals("KOLOR : " + imageColor)) {
                        break;
                    }
                    if (line == null) {

                        try (PrintWriter output = new PrintWriter(new FileWriter(imageWithTagFile.getPath(), true))) {
                            output.printf("KOLOR : " + imageColor + LINE_SEPARATOR);
                            output.printf(imageUri.toString() + LINE_SEPARATOR);
                            output.printf(Integer.toString(Math.round(cutValues[0])) + " "
                                    +Integer.toString(Math.round(cutValues[1])) + " "
                                    +Integer.toString(Math.round(cutValues[2])) + " "
                                    +Integer.toString(Math.round(cutValues[3])));
                            output.printf(LINE_SEPARATOR);
                            output.printf(imageWidth + " " +imageHeight);
                            output.printf(LINE_SEPARATOR);
                            break;
                        } catch (Exception e) {
                              Toast.makeText(context,"NIe dodalo wszystkiego",Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                        }
                    }
                } while (line != null);
            }
            reader.close();
        } catch (IOException e) {
            writeHeader(imageWithTagFile, Double.toString(color));
        }
        MediaScannerConnection.scanFile(context, new String[]{pathToFile + "/" + imageName +"_Details.txt"}, null, null);
    }
}

