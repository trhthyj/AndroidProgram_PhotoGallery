package com.mi.www.photogallary;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wm on 2018/1/16.
 */

public class FlickrFetchr {
    private static final String API_KEY_FLICKR = "a530343992926b24e24126fd6478f6e7";
    private static final String TAG = "FlickrFetchr";
    private static final String METHOD_GET_RECENT= "flickr.photos.getRecent";
    private static final String METHOD_SEARCH= "flickr.photos.search";
    private static final Uri BASE_URI = Uri
            .parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY_FLICKR)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .build();


    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            InputStream inputStream = connection.getInputStream();
            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage() + ": with" + urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = inputStream.read(buffer)) > 0){
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            return outputStream.toByteArray();
        }finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }

    /**
     * 获取最近的photo
     * @return
     */
    public List<GalleryItem> fetchRecentPhotos(){
        String url = buildUrl(METHOD_GET_RECENT, null);
        return downloadGalleryItems(url);
    }

    /**
     * 获取搜索结果photo
     * @param query
     * @return
     */
    public List<GalleryItem> searchPhotos(String query){
        String url = buildUrl(METHOD_SEARCH, query);
        return downloadGalleryItems(url);
    }

    private List<GalleryItem> downloadGalleryItems(String url) {
        List<GalleryItem> items = new ArrayList<>();
        try {
            /*String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY_FLICKR)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")
                    .build().toString();*/

            String jsonString = getUrlString(url);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return items;
    }

    private String buildUrl(String method, String query){
        Uri.Builder uriBuilder = BASE_URI.buildUpon()
                .appendQueryParameter("method", method);
        if (method.equals(METHOD_SEARCH)) {
            uriBuilder.appendQueryParameter("text", query);
        }
        return uriBuilder.build().toString();
    }

    public void parseItems(List<GalleryItem> items, JSONObject jsonObject)
            throws IOException, JSONException{
        JSONObject photosJsonObject = jsonObject.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");
        for (int i = 0; i < photoJsonArray.length() ; i++) {
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);
            GalleryItem item = new GalleryItem();
            item.setCaption(photoJsonObject.getString("title"));
            item.setId(photoJsonObject.getString("id"));
            if(!photoJsonObject.has("url_s")){
                continue;
            }
            item.setUrl(photoJsonObject.getString("url_s"));
            items.add(item);
        }
       /* Gson gson =new Gson();
        Type galleryItemType = new TypeToken<ArrayList<GalleryItem>>(){}.getType();
        String jsonPhotoString = photoJsonArray.toString();
        List<GalleryItem> galleyItemList = gson.fromJson(jsonPhotoString, galleryItemType);
        items.addAll(galleyItemList);*/
    }
}
