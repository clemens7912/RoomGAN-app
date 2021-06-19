package com.example.roomgan;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CreateActivity extends AppCompatActivity implements Listener{

    private final int GET_FROM_CAMERA = 0;
    private final int GET_FROM_GALLERY = 1;

    private String[] options;
    private String chosenView;

    private int[] imagesIds = new int[]{R.id.img1,R.id.img2,R.id.img3,
            R.id.img4,R.id.img5,R.id.img6,R.id.img7,R.id.img8,R.id.img9};

    private int[] drawables = new int[]{R.drawable.image0, R.drawable.image1,R.drawable.image2,
            R.drawable.image3,R.drawable.image4,R.drawable.image5,
            R.drawable.image6,R.drawable.image7,R.drawable.image8};

    private ImageView sourceImage;
    private Bitmap sourceBitmap;
    private ImageView targetImage;
    private Bitmap targetBitmap;
    private ImageView generatedImage;
    private Bitmap generatedBitmap;
    private ImageButton saveImageButton;
    private Button sendImagesButton;
    private AlertDialog progressDialog;

    private File mImageFolder;
    private String mImageFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        sourceImage = findViewById(R.id.sourceImage);
        targetImage = findViewById(R.id.targetImage);
        generatedImage = findViewById(R.id.generatedImage);
        sendImagesButton = findViewById(R.id.sendImages);
        saveImageButton = findViewById(R.id.saveImageButton);

        options = new String[]{getResources().getString(R.string.fromCamera),
                getResources().getString(R.string.fromGallery), getResources().getString(R.string.cancel)};

        sourceImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenView = "source";
                chooseImage();
            }
        });

        targetImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenView = "target";
                chooseImage();
            }
        });

        sendImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendImages();
            }
        });

        saveImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storeImage(generatedBitmap);
            }
        });

        createImageFolder();


    }

    private void storeImage(Bitmap bitmap) {
        new Thread(new Runnable() {

            private Activity activity;

            public Runnable init(Activity activity){
                this.activity = activity;
                return this;
            }

            @Override
            public void run() {
                //Bitmap bitmap = loadBitmapFromView();

                FileOutputStream outputStream = null;

                if(!mImageFolder.exists()){
                    mImageFolder.mkdirs();
                }
                createFileName();
                try {
                    outputStream = new FileOutputStream(mImageFileName);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, "Image Stored", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.init(CreateActivity.this)).start();
    }

    private void createImageFolder(){
        File imageFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        mImageFolder = new File(imageFile, "RoomGan");
        if(!mImageFolder.exists()){
            mImageFolder.mkdirs();
        }
    }

    private File createFileName(){
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String prepend = "IMAGE_" + timestamp;
        File imageFile = new File(mImageFolder, prepend+".jpeg");
        mImageFileName = imageFile.getAbsolutePath();
        return imageFile;
    }

    private void chooseImage(){
        View view = LayoutInflater.from(CreateActivity.this).inflate(R.layout.image_picker, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CreateActivity.this,R.style.DialogTheme);
        alertDialogBuilder.setView(view);

        ImageButton cameraButton = view.findViewById(R.id.cameraButton);
        ImageButton galleryButton = view.findViewById(R.id.galleryButton);

        AlertDialog alertDialog = alertDialogBuilder.create();

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
                alertDialog.dismiss();
            }
        });

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
                alertDialog.dismiss();
            }
        });

        for(int i=0; i<9; i++){
            final int d = i;
            view.findViewById(imagesIds[i]).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bitmap bitmap;
                    switch(chosenView){
                        case "source":
                            //sourceBitmap = loadBitmapFromView((ImageView)v);
                            bitmap = BitmapFactory.decodeResource(getResources(), drawables[d]);
                            sourceBitmap = Bitmap.createScaledBitmap(bitmap, 256,256, true);
                            sourceImage.setImageBitmap(sourceBitmap);
                            alertDialog.dismiss();
                            break;
                        case "target":
                            //targetBitmap = loadBitmapFromView((ImageView)v);
                            bitmap = BitmapFactory.decodeResource(getResources(), drawables[d]);
                            targetBitmap = Bitmap.createScaledBitmap(bitmap, 256,256, true);
                            targetImage.setImageBitmap(targetBitmap);
                            alertDialog.dismiss();
                            break;
                    }
                }
            });
        }


        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();
        Window window = alertDialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private void choosePhoto(){
        new AlertDialog.Builder(CreateActivity.this).setTitle(getResources().getString(R.string.chooseImageTitle))
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(options[which].equals(getResources().getString(R.string.fromCamera))){
                            takePhoto();
                        } else if(options[which].equals(getResources().getString(R.string.fromGallery))){
                            uploadImage();
                        }else {
                            dialog.dismiss();
                        }
                    }
                }).create().show();
    }

    private void takePhoto(){
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, GET_FROM_CAMERA);
    }

    private void uploadImage(){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GET_FROM_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap bitmap = getBitmapFromUri(selectedImage);

            switch (chosenView){
                case "source":
                    sourceBitmap = resizeBitmap(bitmap);
                    sourceImage.setImageBitmap(sourceBitmap);
                    break;
                case "target":
                    targetBitmap = resizeBitmap(bitmap);
                    targetImage.setImageBitmap(targetBitmap);
                    break;
            }
        }

        if (requestCode == GET_FROM_CAMERA && resultCode == Activity.RESULT_OK)
        {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            switch (chosenView){
                case "source":
                    sourceBitmap = resizeBitmap(bitmap);
                    sourceImage.setImageBitmap(sourceBitmap);
                    break;
                case "target":
                    targetBitmap = resizeBitmap(bitmap);
                    targetImage.setImageBitmap(targetBitmap);
                    break;
            }
        }
    }

    private void sendImages(){
        String encodedSourceImage = encodeImage(sourceBitmap);
        String encodedTargetImage = encodeImage(targetBitmap);

        JSONObject data = new JSONObject();
        try {
            data.put("sourceImage", encodedSourceImage);
            data.put("targetImage", encodedTargetImage);
            data.put("timestamp", createTimestamp());
            new ServerConnection().post(CreateActivity.this, data, "generate-room",
                    ((GlobalState)getApplication()).getToken());
            showProgressDialog();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Bitmap loadBitmapFromView(ImageView imageView){
        Bitmap b = Bitmap.createBitmap(imageView.getLayoutParams().width, imageView.getLayoutParams().height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        imageView.layout(0,0, imageView.getLayoutParams().width,imageView.getLayoutParams().height);
        imageView.draw(c);
        return b;
    }

    private String encodeImage(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    private Bitmap getBitmapFromUri(Uri uri){
        Bitmap bitmap = null;

        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

            InputStream in = getContentResolver().openInputStream(uri);
            ExifInterface exif = new ExifInterface(in);

            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
            int rotationInDegrees = exifToDegrees(orientation);

            Matrix matrix = new Matrix();
            if(orientation != 0f){
                matrix.preRotate(rotationInDegrees);
                bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    private int exifToDegrees(int exifOrientation){
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }

    private Bitmap resizeBitmap(Bitmap image){
        Bitmap bitmap = null;

        if(image.getWidth() >= image.getHeight()){
            bitmap = Bitmap.createBitmap(image, image.getWidth()/2-image.getHeight()/2,
                    0, image.getHeight(), image.getHeight());
        }else {
            bitmap = Bitmap.createBitmap(image, 0, image.getHeight()/2 - image.getWidth()/2,
                    image.getWidth(), image.getWidth());
        }

        return Bitmap.createScaledBitmap(bitmap, 256,256, true);
    }

    private String createTimestamp(){
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }

    private void showProgressDialog(){
        LayoutInflater inflater = LayoutInflater.from(CreateActivity.this);
        View v = inflater.inflate(R.layout.progress_dialog, null, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setView(v);

        progressDialog = builder.create();
        progressDialog.show();
        Window window = progressDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(progressDialog.getWindow().getAttributes());
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            progressDialog.getWindow().setAttributes(layoutParams);
        }
    }

    /*
    private Bitmap resizeBitmap(Bitmap image, int maxWidth, int maxHeight){
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > ratioBitmap) {
                finalWidth = (int) ((float)maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float)maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }

     */

    @Override
    public void receiveMessage(JSONObject data) {
        progressDialog.dismiss();
        try {
            switch (data.getInt("status")){
                case 0:
                    byte[] imageBytes = Base64.decode(data.getString("generatedImage"), Base64.DEFAULT);
                    generatedBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            generatedImage.setImageBitmap(generatedBitmap);
                            ((RelativeLayout)findViewById(R.id.generatedImageLayout)).setVisibility(View.VISIBLE);
                        }
                    });
                    break;
                default:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CreateActivity.this, "Internal error generating image", Toast.LENGTH_LONG).show();
                        }
                    });
                    break;
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

    }
}