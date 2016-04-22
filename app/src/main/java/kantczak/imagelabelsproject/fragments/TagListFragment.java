package kantczak.imagelabelsproject.fragments;


import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import kantczak.imagelabelsproject.Model.ImageWithTag;
import kantczak.imagelabelsproject.R;
import kantczak.imagelabelsproject.adapter.TagListAdapter;
import kantczak.imagelabelsproject.controlers.ImageObjectsWithTagsListController;

public class TagListFragment extends Fragment {
    public static final String IMAGE_URI_KEY = "imageUriKey";
    private TagListAdapter adapter;
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tag_list_layout, container, false);
        Bundle arguments = getArguments();

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.allTags);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ImageObjectsWithTagsListController imageObjectsWithTagsListController =
                ImageObjectsWithTagsListController.getInstance(getContext(),(Uri)arguments.getParcelable(IMAGE_URI_KEY));
        adapter = new TagListAdapter(getContext());
        List<ImageWithTag> imageWithTagList = imageObjectsWithTagsListController.getImageWithTagList();
        if(imageWithTagList.size() > 0) {
            adapter.setNewTagList(imageObjectsWithTagsListController.getImageWithTagList());
        }
        recyclerView.setAdapter(adapter);
        return view;
    }
}
