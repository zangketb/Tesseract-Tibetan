package com.zangke.tesseract;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.zangke.mylibrary.mylibrary;

import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.OpenCVLoader;


public class MainActivity extends AppCompatActivity {
    public static final String IMAGE_UNSPECIFIED = "image/*";
    public static final int PHOTOALBUM = 1;   // 相册
    Button photo_album = null;                // 相册
    ImageView imageView = null;               // 截取图像
    EditText textView = null;                 // OCR 识别结果
    private ProgressBar progressBar;
    Bitmap m_phone;                           // Bitmap图像
    String m_ocrOfBitmap;                     // Bitmap图像OCR识别结果
    InputStream m_instream;

    //
//    private static final int REQUEST_EXTERNAL_STORAGE = 1;
//    private static String[] PERMISSIONS_STORAGE = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        //
//        public static void verifyStoragePermissions (Activity activity){
//            try {
//                //检测是否有写的权限
//                int permission = ActivityCompat.checkSelfPermission(activity, "android.permission.WRITE_EXTERNAL_STORAGE");
//                if (permission != PackageManager.PERMISSION_GRANTED) {
//                    // 没有写的权限，去申请写的权限，会弹出对话框
//                    ActivityCompat.requestPermissions(MainActivity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
        //
        imageView = findViewById(R.id.imageID);
        photo_album = findViewById(R.id.photo_album);
        textView = findViewById(R.id.OCRTextView);
        progressBar = findViewById(R.id.progressbr);
        progressBar.setVisibility(View.GONE);
        photo_album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_UNSPECIFIED);
                startActivityForResult(intent, PHOTOALBUM);
            }
        });
        //get access to AssetManager
        AssetManager assetManager = getAssets();
        //open byte streams for reading/writing
        sdcard();
        try {
            m_instream = assetManager.open("tessdata/bod.traineddata");
        } catch (IOException e) {
            e.printStackTrace();
        }
        transfrom();
    }

    public void transfrom() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 0 || data == null) {
            return;
        }
        // 相册
        if (requestCode == PHOTOALBUM) {
            Uri image = data.getData();
            try {
                progressBar.setVisibility(View.VISIBLE);

//                m_phone = BitmapFactory.decodeResource(getResources(), R.drawable.a);
                m_phone = MediaStore.Images.Media.getBitmap(getContentResolver(), image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 处理结果
        imageView.setImageBitmap(m_phone);
        if (OpenCVLoader.initDebug()) {
            // do some opencv stuff
            mylibrary jmi = new mylibrary(m_phone, m_instream);
            m_ocrOfBitmap = jmi.getOcrOfBitmap();
        }
        textView.setText(m_ocrOfBitmap);
        progressBar.setVisibility(View.GONE);
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void sdcard() {


    }


}