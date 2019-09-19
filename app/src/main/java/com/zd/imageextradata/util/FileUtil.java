package com.zd.imageextradata.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Create by zhangdong 2019/9/19
 */
public class FileUtil {
    private static final String LOG_TAG = FileUtil.class.getSimpleName();

    public static boolean hasSdCard() {
        String status = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(status);
    }

    public static String getRootPath() {
        if (hasSdCard()) {
            return Environment.getExternalStorageDirectory().getAbsolutePath(); // sdcard/
        } else {
            return Environment.getDataDirectory().getAbsolutePath() + "/data"; // data/data
        }
    }

    /**
     * 从assets目录读取文件并保存到sdcard
     *
     * @param context 上下文
     * @param fileName 保存文件名
     * @param savePath 保存文件路径
     * @return 保存后的文件
     */
    public static File getAssetsFile(Context context, String fileName, String savePath, String saveName) {
        if (context == null || TextUtils.isEmpty(fileName) || TextUtils.isEmpty(savePath) || TextUtils.isEmpty(saveName)) {
            return null;
        }
        File destDir = new File(savePath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        } else if (destDir.isFile()) {
            destDir.delete();
            destDir.mkdirs();
        }

        File targetFile = new File(savePath + File.separator + saveName);
        AssetManager assetManager = context.getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(fileName);
            out = new FileOutputStream(targetFile);
            byte[] buffer = new byte[2048];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            Log.i(LOG_TAG, "copyFileFromAssets, success");
        } catch (IOException e) {
            targetFile.delete(); //拷贝过程中出现异常则删除
            Log.e(LOG_TAG, "copyFileFromAssets IOException :{}", e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, e.toString());
            }
        }
        return targetFile;
    }

    /**
     * file to bitmap
     *
     * @param imageFile file
     * @return bitmap
     */
    public static Bitmap getFileBitmap(File imageFile) {
        if (!imageFile.isFile() || !imageFile.exists()) {
            return null;
        }
        Bitmap bitmap = null;
        try {
            FileInputStream inputStream = new FileInputStream(imageFile);
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (OutOfMemoryError e) {
            Log.w(LOG_TAG, "OOM", e);
            System.gc();
        } catch (Exception e) {
            Log.w(LOG_TAG, "Fail to load image from disk.", e);
        }
        return bitmap;
    }

    /**
     * 文件转二进制
     *
     * @param file 文件
     * @return 二进制
     */
    public static byte[] fileToByteArray(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        int byteSize = 1024;
        byte[] byteArray = new byte[byteSize];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(byteSize);
            int length = fileInputStream.read(byteArray);
            while (length != -1) {
                outputStream.write(byteArray, 0, length);
                length = fileInputStream.read(byteArray);
            }
            fileInputStream.close();
            outputStream.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            Log.e(LOG_TAG, e.toString());
        }
        return null;
    }

    /**
     * 图片插入额外信息
     *
     * @param file         图片文件
     * @param saveFilePath 存储位置
     * @param extraData    插入的额外信息
     * @return 生成的文件
     */
    public static File insertExtraDataToFile(File file, String saveFilePath, String extraData) {
        if (file == null || !file.exists()) {
            return null;
        }
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File outputFile = null;
        try {
            outputFile = new File(saveFilePath);
            fos = new FileOutputStream(outputFile);
            bos = new BufferedOutputStream(fos);
            byte[] fileByte = fileToByteArray(file);
            if (fileByte == null) {
                return null;
            }
            //写入图片信息
            bos.write(fileToByteArray(file));
            if (!TextUtils.isEmpty(extraData)) {
                //插入信息
                bos.write(extraData.getBytes(Charset.defaultCharset()));
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.toString());
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, e.toString());
                }

            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, e.toString());
                }

            }
        }
        return outputFile;
    }
}
