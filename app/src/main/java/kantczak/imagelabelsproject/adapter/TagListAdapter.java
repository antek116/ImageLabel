package kantczak.imagelabelsproject.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import kantczak.imagelabelsproject.Model.ImageWithTag;
import kantczak.imagelabelsproject.R;
import kantczak.imagelabelsproject.controlers.ImageCutController;

public class TagListAdapter extends RecyclerView.Adapter<TagListAdapter.MyViewHolder> {
    List<ImageWithTag> imageWithTagsList = new ArrayList<>();;
    private ImageCutController imageCutController;

    public TagListAdapter(Context context) {
        imageCutController = ImageCutController.getInstance(context);
    }

    @Override
    public TagListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tag_element_layout, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TagListAdapter.MyViewHolder holder, int position) {
        if(!imageWithTagsList.isEmpty()){
            holder.mImageView.setImageBitmap(setImageBitmap(imageWithTagsList.get(position).getImageUri(), position));
            holder.mTagsTextView.setText(imageWithTagsList.get(position).getTags());
        }
    }

    public Bitmap setImageBitmap(Uri imageBitmap, int position) {
        int height = imageWithTagsList.get(position).getImageHeight();
        int width = imageWithTagsList.get(position).getImageWidth();
        return imageCutController.loadImage(imageBitmap,imageWithTagsList.get(position).getCutValues(),width,height);
    }
    public void setNewTagList(List<ImageWithTag> itemList){
        this.imageWithTagsList = itemList;
    }

    @Override
    public int getItemCount() {
        return imageWithTagsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final ImageView mImageView;
        private final TextView mTagsTextView;
        public MyViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mImageView =(ImageView) itemView.findViewById(R.id.tagImageView);
            mTagsTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
        }
    }

    private int[] calulateImageSize(int position){
        int[] imageSize = new int[2];
        imageSize[0] = imageWithTagsList.get(position).getCutValues()[2] - imageWithTagsList.get(position).getCutValues()[0];
        imageSize[1] = imageWithTagsList.get(position).getCutValues()[3] - imageWithTagsList.get(position).getCutValues()[1];
        return imageSize;
    }
}

