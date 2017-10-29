package com.sporoc.snapbuddy;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import droidninja.filepicker.FilePickerActivity;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

public class MainActivity extends AppCompatActivity {

    private  Button capture, upload;
    private  ImageView image;
    private RelativeLayout mainLayout;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_UPLOAD = 2;
    private boolean imageSelected;
    private List<Bitmap> images;
    private int currentImage;
    private List<String> imageURL;
    private ArrayList<String> imagePath;
    public static List<String> keywords;
    public static PopupAdapter adapter;
    public static boolean option;
    public Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialisation();
    }

    private void initialisation(){
        capture = (Button) findViewById(R.id.capture);
        upload  = (Button) findViewById(R.id.upload);
        image = (ImageView) findViewById(R.id.image);
        mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
        images = new ArrayList<>();
        imagePath = new ArrayList<>();
        keywords = new ArrayList<>();
        imageURL = new ArrayList<>();
        currentImage = -1;
        option = false;
    }

    public void dispatchTakePictureIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

        /*ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "New Picture");
        imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);*/
    }

    public void uploadPicture(View view){
        FilePickerBuilder.getInstance().setMaxCount(10)
                .setSelectedFiles(imagePath)
                .setActivityTheme(R.style.AppTheme)
                .pickPhoto(MainActivity.this);
    }

    public void incorrect(View view){
        if(images.size() > 0){
            images.remove(currentImage);
            if(images.size() > 0){
                if(currentImage >= images.size())
                    --currentImage;
                image.setImageBitmap(images.get(currentImage));
            }else{
                image.setImageDrawable(getResources().getDrawable(R.drawable.default_image));
                imageSelected = false;
                currentImage = 0;
            }
        }
    }



    public void correct(View view){

        if(imageSelected){
            if(keywords.size() == 0){
                Toast.makeText(getApplicationContext(), "No keyword entered.", Toast.LENGTH_LONG)
                        .show();
                return;
            }
            String json = getJSONContent();
            if(json == null){
                Toast.makeText(getApplicationContext(), "Server Issues, Please try again!", Toast.LENGTH_LONG).show();
                return;
            }
            Intent intent = new Intent(getApplicationContext(), ImageResults.class);
            intent.putExtra("json", getJSONContent());
            startActivity(intent);
        }else{
            Toast.makeText(getApplicationContext(), "No picture clicked or uploaded.", Toast.LENGTH_LONG).show();
        }
    }

    public void settings(View view){
        Dialog settingsDialog = new Dialog(this);
        settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View popupView = getLayoutInflater().inflate(R.layout.settings_popup, null);
        RadioGroup radioGroup = (RadioGroup) popupView.findViewById(R.id.radioGroup);
        final RadioButton genre = (RadioButton) popupView.findViewById(R.id.rGenre);
        final RadioButton text = (RadioButton) popupView.findViewById(R.id.rText);
        if(option){
            text.setChecked(true);
        }else{
            genre.setChecked(true);
        }
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                option = false;
                if (text.isChecked()) {
                    option = true;
                }
        }
    });
        settingsDialog.setContentView(popupView);
        settingsDialog.show();
    }

    private String getJSONContent(){
        String json = null;
        try{
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            int i = 0;

            for(String url : imageURL){
                JSONObject image = new JSONObject();
                image.accumulate("name", "image" + i++);
                image.accumulate("image", url);
                jsonArray.put(image);
            }
            jsonObject.accumulate("images", jsonArray);
            jsonArray = new JSONArray();
            for(String keyword : keywords){
                jsonArray.put(keyword);
            }
            jsonObject.accumulate("keywords", jsonArray);
            json = jsonObject.toString();
        }catch(Exception e){

        }
        return json;
    }

    public void next_image(View view){
        if(images.size() == 0){
            Toast.makeText(getApplicationContext(), "No image uploaded.",Toast.LENGTH_LONG).show();
            return;
        }
        int size = images.size();
        currentImage = (currentImage + 1)%size;
        image.setImageBitmap(images.get(currentImage));
    }

    public void contact(View view){
        Intent intent = new Intent(getApplicationContext(), Contacts.class);
        startActivity(intent);
    }

    public void imageClicked(View view){
        if(imageSelected){

        }else{
            uploadPicture(view);
        }
    }

    public void keywords(View view){
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customView = inflater.inflate(R.layout.keyword,null);
        final PopupWindow mPopupWindow = new PopupWindow(
                customView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        mPopupWindow.setHeight(600);
        mPopupWindow.showAtLocation(mainLayout, Gravity.NO_GRAVITY, 0, 200);
        mPopupWindow.setFocusable(true);
        mPopupWindow.update();

        final ListView keywordsList = (ListView) customView.findViewById(R.id.keywordList);
        adapter = new PopupAdapter(MainActivity.this, keywords);
        keywordsList.setAdapter(adapter);
        TextView closePopup = (TextView) customView.findViewById(R.id.closePopup);
        closePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
            }
        });

        Button addKeyword = (Button) customView.findViewById(R.id.addKeyword);
        addKeyword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText keywordText = (EditText)customView.findViewById(R.id.keywordText);

                if(keywordText.getText().toString() == null || keywordText.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Enter a keyword.", Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                keywordsList.setAdapter(null);
                keywords.add(keywordText.getText().toString().toLowerCase());
                adapter.notifyDataSetChanged();
                keywordsList.setAdapter(adapter);
                keywordText.setText("");
            }
        });
    }

    private String saveToDirectory(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            return null;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
        return pictureFile.getPath();
    }

    /** Create a File for saving an image or video */
    private  File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Snapify/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="MI_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try{
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            String imageText = saveToDirectory(imageBitmap);
            if(imageText  == null){
                return;
            }
            imageURL.add(imageText);
            images.add(imageBitmap);
            //image.setImageBitmap(imageBitmap);


                Uri selectedImage = Uri.fromFile(new File(imageText));
                Picasso.with(getApplicationContext())
                        .load(selectedImage)
                        .into(image);
                imageSelected = true;
                ++currentImage;

            }catch(Exception e){
                e.printStackTrace();
            }
        }

        if(requestCode == FilePickerConst.REQUEST_CODE && resultCode == RESULT_OK){
            imagePath = data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_PHOTOS);
            if(imagePath.size() > 0){
                for(String imageText : imagePath){
                    try {
                        Uri selectedImage = Uri.fromFile(new File(imageText));
                        InputStream inputStream = getContentResolver().openInputStream(selectedImage);
                        Bitmap imageBitmap = BitmapFactory.decodeStream(inputStream);
                        imageURL.add(imageText);
                        images.add(imageBitmap);
                        Picasso.with(getApplicationContext())
                                .load(selectedImage)
                                .into(image);
                        ++currentImage;
                        imageSelected = true;
                    }catch(Exception e){
                        Toast.makeText(getApplicationContext(), "Unable to read an image.", Toast.LENGTH_LONG).show();
                    }
                }
                /*if(images.size() > 0){
                    image.setImageBitmap(images.get(currentImage));
                    imageSelected = true;
                }*/
            }
            imagePath.clear();
        }
    }
}
