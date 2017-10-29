package com.sporoc.snapbuddy;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by rriskhh on 29/10/17.
 */
public class ResultListAdapter extends BaseAdapter {

    private Activity activity;
    private static LayoutInflater inflater=null;
    Map<String, String> images;
    List<String> rankedImages;
    Map<String, String> description;

    public ResultListAdapter(Activity activity, Map<String,String> images, List<String> rankedImages, Map<String, String> description) {

        /********** Take passed values **********/
        this.activity = activity;
        this.images = images;
        this.rankedImages = rankedImages;
        this.description = description;
        /***********  Layout inflator to call external xml layout () ***********/
        inflater = ( LayoutInflater )activity.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        if(rankedImages == null){
            return 0;
        }
        return rankedImages.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder{

        public ImageView image;
        public TextView desc;
    }

    /****** Depends upon data size called for each row , Create each ListView row *****/
    public View getView(int position, View convertView, ViewGroup parent) {

        View vi = convertView;
        ViewHolder holder;

        if(convertView == null){

            /****** Inflate tabitem.xml file for each row ( Defined below ) *******/
            vi = inflater.inflate(R.layout.result_list, null);

            /****** View Holder Object to contain tabitem.xml file elements ******/

            holder = new ViewHolder();
            holder.image = (ImageView) vi.findViewById(R.id.result_image);
            holder.desc = (TextView) vi.findViewById(R.id.image_desc);

            vi.setTag(rankedImages.get(position));
            /************  Set holder with LayoutInflater ************/
            try {
                Uri selectedImage = Uri.fromFile(new File(images.get(rankedImages.get(position))));
                InputStream inputStream = activity.getContentResolver().openInputStream(selectedImage);
                Bitmap imageBitmap = BitmapFactory.decodeStream(inputStream);
                holder.image.setImageBitmap(CircularImage.getCircleBitmap(imageBitmap));
            }catch(Exception e){
                e.printStackTrace();
            }
            holder.desc.setText(description.get(rankedImages.get(position)));

        }
        return vi;
    }
}
