## OpenCV && Tesseract-OCR in Android Studio
## 工具
* Android Studio 安卓开发工具，被墙了下面是国内地址
http://www.android-studio.org/
但感觉还是jetbrains的IDE比较牛逼  http://www.jetbrains.com
* Android NDK 不知道的自行度娘
* Tesseract-OCR项目地址
https://github.com/tesseract-ocr/tesseract
* 语言包地址
https://github.com/tesseract-ocr/tesseract/wiki/Data-Files

创建Android工程，不多做解释
将下载好的bod.traineddata文件放在assets/tessdata/下，不要修改目录
创建一个安卓库，代码如下

```java
package com.zangke.mylibrary;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import com.googlecode.tesseract.android.TessBaseAPI;
import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
/**
 * Created by Zangke on 2018/1/2.
 */
public class mylibrary {
    private final static String TAG = "TessCV";
    private Bitmap m_phone;                      // The path of phone image
    private TessBaseAPI m_tessApi;               // Tesseract API reference
    private String m_datapath;                   // The path to folder containing language data file
    private final static String m_lang = "bod";  // The default language of tesseract
    private InputStream m_instream;
    public mylibrary(Bitmap phone, InputStream instream) {
        m_phone = phone;
        m_instream = instream;

        /// initial tesseract-ocr
        m_datapath = Environment.getExternalStorageDirectory().toString() + "/MyLibApp/mylibrary/tesseract";
        // make sure training data has been copied
        checkFile(new File(m_datapath + "/tessdata"));

        m_tessApi = new TessBaseAPI();
        m_tessApi.init(m_datapath, m_lang);
        // 设置psm模式
        //m_tessApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK);
        // 设置白名单
        //m_tessApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
        //m_tessApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "0123456789");
    }


    private void saveTmpImage(String name, Mat image) {
        Mat img = image.clone();
        if (img.channels() ==3 ) {
            Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2RGBA);
        }

        Bitmap bmp = null;
        try {
            bmp = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(img, bmp);
        } catch (CvException e) {
            Log.d("mat2bitmap", e.getMessage());
        }
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "MyLibApp/mylibrary");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("saveTmpImage", "failed to create directory");
                return;
            }
        }
        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //File dest = new File(mediaStorageDir.getPath() + File.separator + name + timeStamp + ".png");
        File dest = new File(mediaStorageDir.getPath() + File.separator + name + ".png");
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(dest);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public String getOcrOfBitmap() {
        if (m_phone == null) {
            return "";
        }

        Mat imgBgra = new Mat(m_phone.getHeight(), m_phone.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(m_phone, imgBgra);
        Mat imgBgr = new Mat();
        Imgproc.cvtColor(imgBgra, imgBgr, Imgproc.COLOR_RGBA2BGR);
        Mat img = imgBgr;
        saveTmpImage("srcInputBitmap", img);
        if (img.empty()) {
            return "";
        }
        if (img.channels()==3) {
            Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
        }
        return getResOfTesseractReg(img);
    }


    private String getResOfTesseractReg(Mat img) {
        String res;
        if (img.empty()) {
            return "";
        }
        byte[] bytes = new byte[(int)(img.total()*img.channels())];
        img.get(0, 0, bytes);
        m_tessApi.setImage(bytes, img.cols(), img.rows(), 1, img.cols());
        res = m_tessApi.getUTF8Text();
        return res;
    }
    private void checkFile(File dir) {
        //directory does not exist, but we can successfully create it
        if (!dir.exists() && dir.mkdirs()){
            copyFiles();
        }
        //The directory exists, but there is no data file in it
        if(dir.exists()) {
            String datafilepath = dir.toString() + "/bod.traineddata";
            File datafile = new File(datafilepath);
            if (!datafile.exists()) {
                copyFiles();
            }
        }
    }
    private void copyFiles() {
        try {
            if (m_instream == null) {
                //TODO
                String resInPath = "/tessdata/bod.traineddata";
                //Log.d(TAG, "copyFiles: resInPath " + resInPath);
                m_instream = new FileInputStream(resInPath);
            }
            //location we want the file to be a
            String resOutPath = m_datapath + "/tessdata/bod.traineddata";
            //open byte streams for writing
            OutputStream outstream = new FileOutputStream(resOutPath);
            //copy the file to the location specified by filepath
            byte[] buffer = new byte[1024];
            int read;
            while ((read = m_instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            m_instream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

```
库文件配置
```java
apply plugin: 'com.android.library'
android {
    compileSdkVersion 26
    defaultConfig {
        minSdkVersion 14
        targetSdkVersion 26
        versionCode 1
        versionName "1.0"
        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }

}

dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'com.android.support:appcompat-v7:26.1.0'
    testImplementation 'junit:junit:4.12'
    androidTestImplementation 'com.android.support.test:runner:1.0.1'
    androidTestImplementation 'com.android.support.test.espresso:espresso-core:3.0.1'
    compile 'com.rmtheis:tess-two:6.1.1'
    compile 'org.opencv:OpenCV-Android:3.1.0'
}
```
MainActivity代码

```java
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
/**
 * Created by Zangke on 2018/1/2.
 */
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView =  findViewById(R.id.imageID);
        photo_album = findViewById(R.id.photo_album);
        textView =  findViewById(R.id.OCRTextView);
        progressBar = findViewById(R.id.progressbr);
        progressBar.setVisibility(View.GONE);
        photo_album.setOnClickListener(new View.OnClickListener(){
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
        try {
            m_instream = assetManager.open("tessdata/bod.traineddata");
        } catch (IOException e) {
            e.printStackTrace();
        }
        transfrom();
    }
    public  void  transfrom(){
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
}
```
项目仓库配置
```java
// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.0.1'
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}
allprojects {
    repositories {
        google()
        jcenter()
        maven {
            url  "http://dl.bintray.com/steveliles/maven"
        }
    }
}
task clean(type: Delete) {
    delete rootProject.buildDir
}
```
在manifest中配置权限

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```
layout布局文件

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity"
    android:weightSum="1">

    <Button
        android:id="@+id/photo_album"
        android:text="GetPhoto"
        android:layout_height="wrap_content"
        android:layout_width="match_parent" />


    <ImageView
        android:id="@+id/imageID"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />

    <ProgressBar
        android:id="@+id/progressbr"
        style="?android:attr/progressBarStyle"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />

    <EditText
        android:id="@+id/OCRTextView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="OCR Text will appear here..."
        android:textSize="20dp"
        android:gravity="center"
        android:textColor="@android:color/black" />
    <!--android:background="#dedede"-->
</LinearLayout>

```
大功告成，接下来测试，识别速度比较慢，而且效果不佳，需要大家进行不断的训练
三星S7真机测试
