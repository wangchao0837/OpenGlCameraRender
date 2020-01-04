package com.example.cameraglrender.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Method;

/**
 * 文件辅助工具，提供注视点标定数据的保存、读取和BMP文件流的封装。
 */
public class FileUtil {

    private static final String TAG = FileUtil.class.getSimpleName();

    /**
     * 无外部存储卡访问权限
     */
    public static class NoExternalStoragePermissionException extends Exception {
        public NoExternalStoragePermissionException(String message) {
            super(message);
        }
    }

    /**
     * 无存储卡挂载异常
     */
    public static class NoExternalStorageMountedException extends Exception {
        public NoExternalStorageMountedException(String message) {
            super(message);
        }
    }

    /**
     * 文件夹无足够空闲空间异常
     */
    public static class DirHasNoFreeSpaceException extends Exception {
        public DirHasNoFreeSpaceException(String message) {
            super(message);
        }
    }

    /**
     * 创建内部私有目录
     *
     * @param context
     * @param dirPathName
     * @return
     */
    public static File createPrivateDir(Context context, String dirPathName) {
        File dir = context.getFilesDir();
        File privateDir = new File(dir, dirPathName);
        if (privateDir.exists()) {
            if (!privateDir.isDirectory()) {
                privateDir.delete();
            }
        }
        privateDir.mkdirs();
        return privateDir;
    }

    /**
     * 在外部存储空间或内部私有空间创建文件夹
     *
     * @param context
     * @param dirName 目录名
     * @return 创建的文件夹
     * @throws NoExternalStoragePermissionException 未能获取外部存储卡读写权限异常
     * @throws NoExternalStorageMountedException    外部存储卡未挂载异常
     */
    public static File createExternalDir(Context context, String dirName) throws NoExternalStoragePermissionException, NoExternalStorageMountedException {
        if (((ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))) {
            String absolutePath = null;

            absolutePath = getExternalPath(context);

            File dir = new File(absolutePath, dirName);
                if (dir.exists()) {
                    if (!dir.isDirectory()) {
                        dir.delete();
                    }
                }
            boolean mkdirs = dir.mkdirs();

                return dir;

        } else {
            throw new NoExternalStoragePermissionException("无法获取对外部存储空间的读写权限，请在Activity中加入获取权限的代码并在AndroidManifest中声明权限");
        }

    }

    public static String getExternalPath(Context context) {
        String absolutePath = null;

        File[] externalFilesDirs = context.getExternalFilesDirs("");
        if (externalFilesDirs.length > 1) {
            absolutePath = externalFilesDirs[1].getAbsolutePath();
        }

//        absolutePath = getStoragePath(context, true);
        if (TextUtils.isEmpty(absolutePath)) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            } else {
                try {
                    throw new NoExternalStorageMountedException("外部存储卡未挂载");
                } catch (NoExternalStorageMountedException e) {
                    e.printStackTrace();
                }
            }
        }
        return absolutePath;
    }


    /**
     * 在外部存储空间或内部私有空间新建指定文件夹中的指定文件
     *
     * @param context
     * @param dirPath       目录路径名称
     * @param fileName      文件名
     * @param needFreeSpace 需要的空间空间，以字节计
     * @return 创建的文件
     * @throws NoExternalStoragePermissionException 未能获取外部存储卡读写权限异常
     * @throws NoExternalStorageMountedException    外部存储卡未挂载异常
     * @throws DirHasNoFreeSpaceException           目录缺少足够的存储空间异常
     * @throws IOException                          文件读写IO异常
     */
    public static File createFile(Context context, boolean isPrivate, String dirPath, String fileName, long needFreeSpace) throws NoExternalStoragePermissionException, NoExternalStorageMountedException, DirHasNoFreeSpaceException, IOException {
        File dir = null;
        if (isPrivate) {
            dir = createPrivateDir(context, dirPath);
        } else {
            dir = createExternalDir(context, dirPath);
        }
        long freeSpace = dir.getFreeSpace();

        Log.e(TAG, "freeSpace:" + freeSpace);
        //需要保留50MB空间
//        if (freeSpace > needFreeSpace) {
            File targetFile = new File(dir, fileName);
            if (targetFile.exists()) {
                targetFile.delete();
            }
        Log.e(TAG, "targetFile:" + targetFile.getAbsolutePath());
        targetFile.createNewFile();
            Log.e(TAG, "createFile:" + targetFile.getAbsolutePath());
            return targetFile;
//        } else {
//            throw new DirHasNoFreeSpaceException("目录" + dir.getAbsolutePath() + "缺少足够的空间以保存文件");
//        }
    }


    /**
     * 通过反射调用获取内置存储和外置sd卡根路径(通用)
     *
     * @param mContext    上下文
     * @param is_removale 是否可移除，false返回内部存储路径，true返回外置SD卡路径
     * @return
     */
    public static String getStoragePath(Context mContext, boolean is_removale) {
        String path = "";
        //使用getSystemService(String)检索一个StorageManager用于访问系统存储功能。
        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);

            for (int i = 0; i < Array.getLength(result); i++) {
                Object storageVolumeElement = Array.get(result, i);
                path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removale == removable) {
                    return path;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }





    /**
     * 将输入流写入到指定文件
     *
     * @param context
     * @param isPrivate      是否私有
     * @param dirPath        目录路径
     * @param targetFileName 目标文件名
     * @param inputStream    输入流
     * @return 写入完成的文件
     * @throws IOException                          文件读写IO异常
     * @throws NoExternalStoragePermissionException 未能获取外部存储卡读写权限异常
     * @throws NoExternalStorageMountedException    外部存储卡未挂载异常
     * @throws DirHasNoFreeSpaceException           目录缺少足够的存储空间异常
     */
    private static File writeInputStreamToFile(Context context, boolean isPrivate, String dirPath, String targetFileName, InputStream inputStream) throws IOException, NoExternalStoragePermissionException, DirHasNoFreeSpaceException, NoExternalStorageMountedException {
        File targetFile = createFile(context, isPrivate, dirPath, targetFileName, inputStream.available());
        byte[] buffer = new byte[2048];
        int byteCount = 0;
        FileOutputStream out = new FileOutputStream(targetFile);
        while ((byteCount = inputStream.read(buffer, 0, buffer.length)) != -1) {
            out.write(buffer, 0, byteCount);
        }
        out.flush();
        out.close();
        inputStream.close();
        return targetFile;
    }


    /**
     * 写入字节流到指定目录的指定文件
     *
     * @param context
     * @param isPrivate      是否私有
     * @param dirPath        目录相对当前类型的根目录路径
     * @param targetFileName 目标文件名
     * @return 写入完成的文件
     * @throws NoExternalStoragePermissionException 未能获取外部存储卡读写权限异常
     * @throws NoExternalStorageMountedException    外部存储卡未挂载异常
     * @throws DirHasNoFreeSpaceException           目录缺少足够的存储空间异常
     * @throws IOException                          文件读写IO异常
     */
    public static File writeByteDataToTargetFile(Context context, byte[] data, boolean isPrivate, String dirPath, String targetFileName) throws NoExternalStoragePermissionException, DirHasNoFreeSpaceException, NoExternalStorageMountedException, IOException {

        InputStream inputStream = new ByteArrayInputStream(data);
        return writeInputStreamToFile(context, isPrivate, dirPath, targetFileName, inputStream);
    }

    /**
     * 读取指定目录的指定文件到字节数组
     *
     * @param context
     * @param isPrivate      是否私有
     * @param dirPath        目录相对当前类型的根目录路径
     * @param targetFileName 目标文件名
     * @return 读取的字节数组
     * @throws IOException           IO异常
     * @throws FileNotFoundException 目标文件未找到
     */
    public static byte[] readByteDateFromTargetFile(Context context, boolean isPrivate, String dirPath, String targetFileName) throws IOException, NoExternalStoragePermissionException, NoExternalStorageMountedException {
        File dir = null;
        if (isPrivate) {
            dir = createPrivateDir(context, dirPath);
        } else {
            dir = createExternalDir(context, dirPath);
        }
        File file = new File(dir, targetFileName);
        if (file.exists()) {
            FileInputStream inputStream = new FileInputStream(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int byteCount = 0;
            while ((byteCount = inputStream.read(buffer, 0, buffer.length)) != -1) {
                out.write(buffer, 0, byteCount);
            }
            out.flush();
            out.close();
            inputStream.close();
            byte[] result = out.toByteArray();
            return result;
        } else {
            throw new FileNotFoundException("未找到指定文件");
        }
    }

    public static String readSdcardFile(String fileName) throws NoExternalStorageMountedException, IOException {
        //如果手机插入了SD卡，而且应用程序具有访问SD的权限
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //获取SD卡对应的存储目录
            File sdCardDir = Environment.getExternalStorageDirectory();
            //获取指定文件对应的输入流
            File file = new File(sdCardDir, fileName);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(sdCardDir.getCanonicalPath() + "/" + fileName);
                //将指定输入流包装成BufferedReader
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                StringBuilder sb = new StringBuilder("");
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            } else {
                throw new FileNotFoundException("未在存储卡根目录找到7invensun许可文件,即将退出应用");
            }

        } else {
            throw new NoExternalStorageMountedException("外部存储卡未挂载");
        }
    }


//    /**
//     * 删除已存储的文件
//     */
//    public static boolean deletefile(String fileName) {
//        try {
//            // 找到文件所在的路径并删除该文件
//            Log.e("AAAAA", "fileName:" + fileName);
//            File file = new File(Environment.getExternalStorageDirectory(), fileName);
//            if (!file.exists()) {
//                Log.e(TAG, "FileNotFound when delete VideoInfoFile");
//                Log.e("AAAAA", "1111111");
//                return false;
//            }
//            file.delete();
//            Log.e("AAAAA", "2222");
//            return true;
//        } catch (Exception e) {
//            Log.e("AAAAA", "33333");
//            Log.e("AAAA", e.getMessage().toLowerCase().toString());
//            e.printStackTrace();
//            return false;
//        }
//    }


    //删除文件夹和文件夹里面的文件
    public static boolean deletefile(Context mContext, final String fileName) {
        Log.e(TAG, "fileName:" + fileName);
        File dir = new File(getExternalPath(mContext), fileName);
        return deleteDirWihtFile(dir);
    }

    public static boolean deleteDirWihtFile(File dir) {
        Log.e(TAG, "dir:" + dir);

        Log.e(TAG, "dir.exists():" + dir.exists());

        Log.e(TAG, "dir.isDirectory():" + dir.isDirectory());

        if (dir == null || !dir.exists() || !dir.isDirectory()) return false;

        for (File file : dir.listFiles()) {
            if (file.isFile()) file.delete(); // 删除所有文件
            else if (file.isDirectory()) deleteDirWihtFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
        return true;
    }

}
