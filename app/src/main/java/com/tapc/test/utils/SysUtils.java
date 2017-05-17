/**
 * SysUtils.java[v 1.0.0]
 * classes:com.jht.tapc.platform.utils.SysUtils
 * fch Create of at 2015年4月23日 下午3:03:32
 */
package com.tapc.test.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.tapc.test.interfaces.CopyListenerInterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SysUtils {
    public static long COPY_FILE_SIZE = 0;

    /***
     * 获取文件大小
     ***/
    public long getFileSizes(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
            fis.close();
        } else {
            System.out.println("文件不存在");
        }
        return size;
    }

    /***
     * 获取文件夹大小
     ***/
    static public long getFileSize(File file) throws Exception {
        long size = 0;
        File flist[] = file.listFiles();
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFileSize(flist[i]);
            } else {
                size = size + flist[i].length();
            }
        }
        return size;
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    static public boolean testCopyFile(Context context, String file, String newPath) {
        try {
            int byteread = 0;
            String newFilePath = newPath + file;
            InputStream inStream = context.getResources().getAssets().open(file);
            FileOutputStream fs = new FileOutputStream(newFilePath);
            byte[] buffer = new byte[1444];
            while ((byteread = inStream.read(buffer)) < 0) {
                fs.write(buffer, 0, byteread);
            }
            inStream.close();
            fs.close();
            File newFile = new File(newFilePath);
            if (newFile.exists()) {
                newFile.delete();
                return true;
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    static public boolean copyFile(Context context, String file, String newPath, CopyListenerInterface copyListener) {
        try {
            int byteread = 0;
            long index = 0;
            String newFilePath = newPath + file;
            InputStream inStream = context.getResources().getAssets().open(file);
            FileOutputStream fs = new FileOutputStream(newFilePath);
            byte[] buffer = new byte[1024];
            while ((byteread = inStream.read(buffer)) < 0) {
                fs.write(buffer, 0, byteread);
                index = index + byteread;
                if (copyListener != null) {
                    copyListener.progress(index);
                }
            }
            inStream.close();
            fs.close();
            return true;
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错 ：" + file);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 复制整个文件夹内容
     *
     * @param oldPath String 原文件路径 如：c:/fqf
     * @param newPath String 复制后路径 如：f:/fqf/ff
     * @return boolean
     */
    static public boolean copyFolder(String oldPath, String newPath) {
        try {
            (new File(newPath)).mkdirs();
            File a = new File(oldPath);
            String[] file = a.list();
            File temp = null;
            for (int i = 0; i < file.length; i++) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file[i]);
                } else {
                    temp = new File(oldPath + File.separator + file[i]);
                }

                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + "/" + (temp.getName()).toString());
                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                        COPY_FILE_SIZE = COPY_FILE_SIZE + len;
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if (temp.isDirectory()) {
                    boolean copyResult = copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
                    if (!copyResult) {
                        return copyResult;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("复制整个文件夹内容操作出错");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 递归删除文件和文件夹
     *
     * @param file 要删除的根目录
     */
    public static void RecursionDeleteFile(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                file.delete();
                return;
            }
            for (File f : childFile) {
                RecursionDeleteFile(f);
            }
            file.delete();
        }
    }

    public static String getLocalVersionCode(Context context) {
        String versionName = "";
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (Exception e) {
        }
        return versionName;
    }

    public static String execCommand(String command) throws IOException {

        Runtime runtime = Runtime.getRuntime();
        Process proc = runtime.exec(command);

        InputStream inputstream = proc.getInputStream();
        InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
        BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

        String line = "";
        StringBuilder sb = new StringBuilder(line);
        while ((line = bufferedreader.readLine()) != null) {
            sb.append(line);
            sb.append('\n');
        }
        // tv.setText(sb.toString());
        // 使用exec执行不会等执行成功以后才返回,它会立即返回
        // 所以在某些情况下是很要命的(比如复制文件的时候)
        // 使用wairFor()可以等待命令执行完成以后才返回
        try {
            if (proc.waitFor() != 0) {
                System.err.println("exit value = " + proc.exitValue());
            }
        } catch (InterruptedException e) {
            System.err.println(e);
        }
        return sb.toString();
    }
}
