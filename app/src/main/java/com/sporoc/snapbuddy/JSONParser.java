package com.sporoc.snapbuddy;

/**
 * Created by rriskhh on 22/09/16.
 */

import android.content.ContentValues;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class JSONParser {

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";

    // constructor
    public JSONParser() {

    }

    public JSONObject makeHttpRequest(String urlOfWebService, String method,
                                      String json) {

        // Making HTTP request
        try {

            // check for request method
            URL url = new URL(urlOfWebService);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(20000);
            urlConnection.setConnectTimeout(20000);
            if (method.equals("POST")) {
                urlConnection.setRequestMethod("POST");
            } else if (method.equals("GET")) {
                urlConnection.setRequestMethod("GET");
            }
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            bw.write(json);
            bw.flush();
            bw.close();
            os.close();
            urlConnection.connect();
            is = (InputStream) urlConnection.getInputStream();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jObj;

    }

    public JSONObject makeHttpRequest(String urlOfWebService, String method,
                                      ContentValues params) {

        // Making HTTP request
        try {

            // check for request method
            URL url = new URL(urlOfWebService);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(20000);
            urlConnection.setConnectTimeout(20000);
            if (method.equals("POST")) {
                urlConnection.setRequestMethod("POST");
            } else if (method.equals("GET")) {
                urlConnection.setRequestMethod("GET");
            }
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            bw.write(getQueryString(params));
            bw.flush();
            bw.close();
            os.close();
            urlConnection.connect();
            is = (InputStream) urlConnection.getInputStream();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jObj;

    }


    private String getQueryString(ContentValues contentValues) {

        //Log.d("Enthusia",contentValues.size() + "");
        StringBuilder result = new StringBuilder();
        List<String> key = new ArrayList<>();
        List<String> values = new ArrayList<>();

        if(contentValues == null || contentValues.size() == 0)
            return "";
        try {
            //get all the parameters
            for (Map.Entry<String, Object> entry : contentValues.valueSet()) {
                key.add(entry.getKey());
                values.add(entry.getValue().toString());
            }
            result.append(URLEncoder.encode(key.get(0), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(values.get(0), "UTF-8"));

            for (int i = 1; i < key.size(); ++i) {
                result.append("&");
                result.append(URLEncoder.encode(key.get(i), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(values.get(i), "UTF-8"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //Log.d("Enthusia", result.toString());
        return result.toString();
    }


}