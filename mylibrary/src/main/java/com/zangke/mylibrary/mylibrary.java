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
