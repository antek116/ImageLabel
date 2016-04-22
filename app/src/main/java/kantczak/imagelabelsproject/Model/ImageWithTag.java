package kantczak.imagelabelsproject.Model;


import android.net.Uri;

public class ImageWithTag {

    private Uri imageUri;
    private String tags;
    private double color;
    private int[] cutValues;
    private int imageWidth;
    private int imageHeight;
    public ImageWithTag(Uri imageUri, int[] cutValues, double color,int width,int height) {
        this.imageUri = imageUri;
        this.cutValues = cutValues;
        this.color = color;
        this.imageWidth = width;
        this.imageHeight = height;
    }
    public Uri getImageUri() {
        return imageUri;
    }
    public String getTags(){
        return tags;
    }
    public double getColor(){
        return this.color;
    }
    public int[] getCutValues(){
        return this.cutValues;
    }
    public void setTags(String tags){
        this.tags = tags;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }
}
