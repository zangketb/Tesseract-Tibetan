## OpenCV && Tesseract-OCR in Android Studio
## 工具
* Android Studio 安卓开发工具，被墙了下面是国内地址
http://www.android-studio.org/
但感觉还是jetbrains的IDE比较牛逼  http://www.jetbrains.com
* Android NDK 自行度娘
* Tesseract-OCR项目地址
https://github.com/tesseract-ocr/tesseract
* 语言包地址
https://github.com/tesseract-ocr/tesseract/wiki/Data-Files

## 使用

将下载好的bod.traineddata文件放在assets/tessdata/下，不要修改目录

```java
 //location we want the file to be a
            String resOutPath = m_datapath + "/tessdata/bod.traineddata";
            //open byte streams for writing
            OutputStream outstream = new FileOutputStream(resOutPath);
            //copy the file to the location specified by filepath
            byte[] buffer = new byte[1024];
            int read;
```

## 缺点
* 识别速度比较慢
* 效果不佳，
* 需要大家进行不断的训练

##  测试
三星S7真机测试
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200703210454464.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3RpYmV0emhheGk=,size_16,color_FFFFFF,t_70#pic_center)
