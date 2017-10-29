package com.sporoc.snapbuddy;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.squareup.picasso.Picasso;
import com.twilio.client.Twilio;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ImageResults extends AppCompatActivity {

    private final String  CLOUD_VISION_API_KEY = "AIzaSyDevG_HxSHFLZwOrF5QKOL7mXRNUiaIFN8";
    //private String json = null;
    private JSONObject jsonObject;
    private ListView resultList;
    private ResultListAdapter adapter;
    private ProgressDialog progressDialog;
    private Map<String, Double> rank;
    private Map<String, Double> rankText;
    private Map<String,String> imageMaping;
    private Map<String,String> description;
    private TextView error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_results);

        String json = getIntent().getStringExtra("json");
        resultList = (ListView) findViewById(R.id.resultList);
        error = (TextView) findViewById(R.id.error_result);

        resultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String image = view.getTag().toString();
                String url = imageMaping.get(image);
                popupImage(url);
            }
        });


        try{
            jsonObject = new JSONObject(json);
            progressDialog = new ProgressDialog(ImageResults.this);
            progressDialog.setMessage("Analyzing Image..");
            progressDialog.setCancelable(false);
            progressDialog.show();
            new ConnectAPI().execute();
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private Bitmap getImage(String url){
        try {
            Uri selectedImage = Uri.fromFile(new File(url));
            InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(selectedImage);
            Bitmap imageBitmap = BitmapFactory.decodeStream(inputStream);
            return imageBitmap;
        }catch(Exception e){
            return null;
        }
    }

    private void popupImage(String url){
        Dialog settingsDialog = new Dialog(this);
        settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = getLayoutInflater().inflate(R.layout.image_layout, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.popup_image);
        try {
            Uri selectedImage = Uri.fromFile(new File(url));
            Picasso.with(getApplicationContext())
                    .load(selectedImage)
                    .into(imageView);
        }catch(Exception e) {
            Bitmap bitmap = getImage(url);
            if(bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
        settingsDialog.setContentView(view);
        settingsDialog.show();
    }

    class ConnectAPI extends AsyncTask<Void, Void, String>{

        private JSONParser parser = new JSONParser();
        private boolean isAdult;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isAdult = false;
            description = new HashMap<>();
        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);
            progressDialog.dismiss();
            if(rank.size() == 0){
                error.setVisibility(View.VISIBLE);
                resultList.setVisibility(View.GONE);
                return;
            }
            List<String> list = sortMapByValue(rank);
            adapter = new ResultListAdapter(ImageResults.this, imageMaping, list, description);
            resultList.setAdapter(adapter);
        }

        public List<String> sortMapByValue(final Map<String, Double> map){
            Map<String, Double> result = new TreeMap<>(new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    return map.get(s1).compareTo(map.get(s2));
                }
            });
            result.putAll(map);
            List<String> list = new ArrayList<>();
            for(String key : result.keySet()){
                list.add(key);
            }
            return list;
        }

        private void checkAdultContent(JSONObject jsonObject) throws JSONException{
            if(isAdult){
                return;
            }
            JSONObject safeSearch = (JSONObject)jsonObject.get("safeSearchAnnotation");
            String temp;

            temp = (String) safeSearch.get("adult");
            if(temp.equals("VERY_LIKELY") || temp.equals("LIKELY") || temp.equals("POSSIBLE")){
                isAdult = true;
                sendSMS();
            }

            if(isAdult){
                return;
            }

            temp = (String) safeSearch.get("violence");
            if(temp.equals("VERY_LIKELY") || temp.equals("LIKELY") || temp.equals("POSSIBLE")){
                isAdult = true;
                sendSMS();
            }

        }

        private void sendSMS(){
            String url = "https://etrial01.000webhostapp.com/twilio.php";
            ContentValues contentValues = new ContentValues();
            contentValues.put("to","+18572726423");
            contentValues.put("from","+18572147792");
            contentValues.put("body","Inappropriate content detected in your child's device.");
            parser.makeHttpRequest(url, "POST", contentValues);
        }


        @Override
        protected String doInBackground(Void... params) {
            try{
                JSONArray array = (JSONArray)jsonObject.get("images");
                Map<String, Boolean> keywords = getKeywords((JSONArray) jsonObject.get("keywords"));
                imageMaping = getImageMapping(array);

                rank = new HashMap<>();
                rankText = new HashMap<>();

                for(String name : imageMaping.keySet()){
                    String image = (String)imageMaping.get(name);
                    String imageScore = getImageScores(image);

                    if(imageScore != null){
                        JSONObject scores = new JSONObject(imageScore);
                        JSONObject responses ;

                        responses = (JSONObject) ((JSONArray) scores.get("responses")).get(0);


                        /* check adult content */
                        checkAdultContent(responses);
                        /* get description  */
                        description.put(name, getImageDescription(responses));

                        if(!MainActivity.option){
                            /* get Label Rank */
                            JSONArray tempArray = (JSONArray)responses.get("labelAnnotations");
                            for(int j = 0;j<tempArray.length();++j){
                                JSONObject tempObject = (JSONObject) tempArray.get(j);
                                String desc = (String) tempObject.get("description");

                                double score = (Double) tempObject.get("score");
                                if(keywords.get(desc.toLowerCase()) != null){
                                    if(rank.get(name) != null){
                                        rank.put(name, rank.get(name) + score);
                                    }else{
                                        rank.put(name, score);
                                    }
                                }
                            }
                        }else {

                            /* get Text Rank */
                            try {
                                JSONArray tempArray = (JSONArray) responses.get("textAnnotations");

                                for (int j = 0; j < tempArray.length(); ++j) {
                                    JSONObject tempObject = (JSONObject) tempArray.get(j);
                                    String desc = (String) tempObject.get("description");
                                    double score = 1;
                                    if (keywords.get(desc.toLowerCase()) != null) {
                                        if (rank.get(name) != null) {
                                            rank.put(name, rank.get(name) + score);
                                        } else {
                                            rank.put(name, score);
                                        }
                                    }
                                }
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                }

            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        private String getImageDescription(JSONObject jsonObject) throws Exception{
            String desc = "";
            JSONArray array = (JSONArray)((JSONObject)jsonObject.get("webDetection")).get("webEntities");
            for(int i = 0;i<array.length();++i){
                JSONObject object = (JSONObject) array.get(i);
                return (String)object.get("description");
            }
            return desc;
        }

        //getImageMapping
        private Map getImageMapping(JSONArray array){
            Map<String, String> images = new HashMap<>();
            for(int i = 0;i<array.length();++i){
                try {
                    JSONObject jsonObject = (JSONObject) array.get(i);
                    String name = (String) jsonObject.get("name");
                    String image = (String) jsonObject.get("image");
                    images.put(name, image);
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
            return images;
        }

        //getkeywords
        private Map getKeywords(JSONArray array){
            Map<String, Boolean> keywords = new HashMap<>();
            for(int i = 0;i<array.length();++i){
                try {
                    String keyword = (String) array.get(i);
                    if(MainActivity.option){
                        String[] kewordArray = keyword.split(" ");
                        for(String s : kewordArray){
                            keywords.put(s, true);
                        }
                    }else{

                        keywords.put(keyword, true);
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
            return keywords;
        }

        private List getFeatures(){
            List<Feature> list = new ArrayList<>();
            Feature feature = new Feature();

            if(MainActivity.option){
                feature.setType("TEXT_DETECTION");
            }else{
                feature.setType("LABEL_DETECTION");
            }
            feature.setMaxResults(25);
            list.add(feature);
            feature = new Feature();
            feature.setType("WEB_DETECTION");
            feature.setMaxResults(10);
            list.add(feature);
            feature = new Feature();
            feature.setType("SAFE_SEARCH_DETECTION");
            feature.setMaxResults(10);
            list.add(feature);
            feature = new Feature();
            return list;
        }

        //getImageScore
        private String getImageScores(String json){
            try {
                List<Feature> list = getFeatures();
                AnnotateImageRequest annotateImageReq = new AnnotateImageRequest();
                annotateImageReq.setFeatures(list);
                annotateImageReq.setImage(getEncodedImage(json));
                List<AnnotateImageRequest> annotateList = new ArrayList<>();
                annotateList.add(annotateImageReq);
                HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
                VisionRequestInitializer requestInitializer = new VisionRequestInitializer(CLOUD_VISION_API_KEY);

                Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                builder.setVisionRequestInitializer(requestInitializer);

                Vision vision = builder.build();

                BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
                batchAnnotateImagesRequest.setRequests(annotateList);

                Vision.Images.Annotate annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);
                annotateRequest.setDisableGZipContent(true);
                BatchAnnotateImagesResponse response = annotateRequest.execute();
                return response.toString();
            }catch(Exception e){
                return null;
            }
        }
    }

    private Image getEncodedImage(String url){
        Image image = new Image();
        try {
            Uri selectedImage = Uri.fromFile(new File(url));
            InputStream inputStream = getContentResolver().openInputStream(selectedImage);
            Bitmap imageBitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            image.encodeContent(imageBytes);
        }catch(Exception e){
            e.printStackTrace();
        }
        return image;
    }

}
