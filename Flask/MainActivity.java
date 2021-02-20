package com.example.flask_to_apk2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
//import android.support.v4.app.ActivityCompat;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_CODE = 1;
    private static final int STORAGE_PERMISSION_CODE = 1;
    private static final int CAMERA_PIC_REQUEST = 109;
    File mFile = null;
    String postUrl = "http://192.168.43.115:5001/apk";

    final int SELECT_MULTIPLE_IMAGES = 1;
    String selectedImagesPaths; // Paths of the image(s) selected by the user.
    boolean imagesSelected = false; // Whether the user selected at least an image or not.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedImagesPaths = "";
        mFile = null;
        imagesSelected = false;
        setContentView(R.layout.activity_main);

        String[] per = {
                Manifest.permission.CAMERA,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE

        };
        checkPermission(per,1 );

    }

// This function is called when the user accepts or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when the user is prompt for permission.

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super
                .onRequestPermissionsResult(requestCode,
                        permissions,
                        grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this,
                        "Camera Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            }
            else {
                Toast.makeText(MainActivity.this,
                        "Camera Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
        else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this,
                        "Storage Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            }
            else {
                Toast.makeText(MainActivity.this,
                        "Storage Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }








    // Function to check and request permission
    public void checkPermission(String[] permission, int requestCode)
    {

        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(
                MainActivity.this,
                permission[0])
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat
                    .requestPermissions(
                            MainActivity.this,
                            permission,
                            requestCode);
        }
        else {
            Toast
                    .makeText(MainActivity.this,
                            "Permission already granted",
                            Toast.LENGTH_SHORT)
                    .show();
        }
    }








    public void CaptureImage(View V){
        File dir = new File(Environment.getExternalStorageDirectory() + "/dcim/wheatclassifier");

        if(!dir.exists() && !dir.isDirectory())
        {
            // create empty directory
            if (dir.mkdirs())
            {
                Log.i("CreateDir","App dir created");
            }
            else
            {
                Log.w("CreateDir","Unable to create app dir!");
            }
        }
        else
        {
            Log.i("CreateDir","App dir already exists");
        }

        try {
            mFile = File.createTempFile("myImage", ".png", dir);
            //Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Log.d("Steps............","Step 0 okay....................................................................");
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                // Continue only if the File was successfully created
                Log.d("Steps............","Step 0.5 okay....................................................................");
                if (mFile != null) {
                    Log.d("Steps............","Step 0.75 okay....................................................................");
                    Uri photoURIc = FileProvider.getUriForFile(this,
                            "com.example.android.fileprovider",
                            mFile);
                    Log.d("Steps............","Step 1 okay....................................................................");
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURIc);
                    Log.d("Steps............","Step 2 okay....................................................................");
                    startActivityForResult(takePictureIntent, CAMERA_PIC_REQUEST);
                    Log.d("Steps............","Step 3 okay....................................................................");
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    //Bitmap bm = BitmapFactory.decodeFile(mFile.getAbsolutePath(),options);
                    selectedImagesPaths = mFile.getAbsolutePath();
                    imagesSelected = true;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }











    public void connectServer(View v) {

        Log.d("Connect server.......", "Single Image URI : " + selectedImagesPaths);


        TextView responseText = findViewById(R.id.responseText);
        if (imagesSelected == false) { // This means no image is selected and thus nothing to upload.
            responseText.setText("No Image Selected to Upload. Select Image(s) and Try Again.");
            return;
        }
        responseText.setText("Sending the Files. Please Wait ...");


        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);


            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try {
                Log.d("Try block.......", "Single Image URI : " + selectedImagesPaths);
                // Read BitMap by file path.
                Bitmap bitmap = BitmapFactory.decodeFile(selectedImagesPaths, options);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            }catch(Exception e){
                responseText.setText("Please Make Sure the Selected File is an Image.");
                Log.d("Catch block.......", "Single Image URI : " + selectedImagesPaths);
                return;
            }
            byte[] byteArray = stream.toByteArray();


            multipartBodyBuilder.addFormDataPart("data_file" , "Image_of_wheat.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray));

        RequestBody postBodyImage = multipartBodyBuilder.build();

//        RequestBody postBodyImage = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("image", "androidFlask.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
//                .build();

        postRequest(postUrl, postBodyImage);
    }







    void postRequest(String postUrl, RequestBody postBody) {
        boolean t = false;

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(3000,TimeUnit.SECONDS)
                .writeTimeout(3000,TimeUnit.SECONDS)
                .readTimeout(3000,TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build();
        //client.setConnectTimeout(15, TimeUnit.SECONDS); // connect timeout
        //client.setReadTimeout(15, TimeUnit.SECONDS);    // socket timeout

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();




        client.newCall(request).enqueue(new Callback() {


            @Override
            public void onFailure(Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();
                Log.d("FAIL", e.getMessage());

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView responseText = findViewById(R.id.responseText);
                        responseText.setText("Failed to Connect to Server. Please Try Again.");
                    }
                });
            }



            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView responseText = findViewById(R.id.responseText);
                        try {
                            String res = response.body().string();

                            Intent intent2 = new Intent(MainActivity.this, ResultsOfImage.class);
                            intent2.putExtra("RESU_OF_JSON", res);
                            startActivity(intent2);

                            responseText.setText("Server's Response\n" + res);

                            JSONObject Jobject = new JSONObject(res);
                            int a = Jobject.getInt("maj");
                            responseText.setText("Server's  int Response\n" + a);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                );
            }
        }
        );
    }





    public void selectImage(View v) {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_MULTIPLE_IMAGES);
    }










    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == SELECT_MULTIPLE_IMAGES && resultCode == RESULT_OK && null != data) {
                // When a single image is selected.
                String currentImagePath;
                if (data.getData() != null) {
                    Uri uri = data.getData();
                    currentImagePath = getPath(getApplicationContext(), uri);
                    selectedImagesPaths = currentImagePath;

                    Log.d("ImageDetails", "Single Image URI : " + uri);
                    Log.d("ImageDetails", "Single Image Path : " + currentImagePath);
                    imagesSelected = true;
                    TextView boolofselection = findViewById(R.id.numSelectedImages);
                    boolofselection.setText("Image is selected now parse it to the server");
                } else {
                    selectedImagesPaths = "Image Not picked";
                    imagesSelected = false;
                    TextView boolofselection = findViewById(R.id.numSelectedImages);
                    boolofselection.setText("Image not selected");

                    Log.d("Code is not working","There is error while selecting path of the image");
                }
            }



            // if user wants to upload image....................

            else if(requestCode == CAMERA_PIC_REQUEST) {
                if (resultCode == RESULT_OK) {
                    if ( data != null)
                    {
                        return;
                        //createcameraarry();
                    }
                }
            }

            else {
                imagesSelected = false;
                selectedImagesPaths = "Image not picked";
                TextView boolofselection = findViewById(R.id.numSelectedImages);
                boolofselection.setText("Image not selected");
                Toast.makeText(this, "You haven't Picked any Image.", Toast.LENGTH_LONG).show();
            }
            Toast.makeText(getApplicationContext(), selectedImagesPaths + " Image(s) Selected.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Something Went Wrong.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }








    // Implementation of the getPath() method and all its requirements is taken from the StackOverflow Paul Burke's answer: https://stackoverflow.com/a/20559175/5426539
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}




