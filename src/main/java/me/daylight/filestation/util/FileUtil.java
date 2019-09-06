package me.daylight.filestation.util;

import org.springframework.lang.Nullable;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Daylight
 * @date 2018/12/5 10:56
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class FileUtil {

    /** 绝对路径 **/
    public static String absolutePath = "";

    /** 静态目录 **/
    public static String staticDir="upload/";

    private static final int BUFFER = 8192;

    public static void upload(MultipartFile file,String targetPath,String fileName) throws IOException {
        //第一次会创建文件夹
        createDirIfNotExists(targetPath);

        //存文件
        File uploadFile = new File(absolutePath, staticDir + targetPath+fileName);
        file.transferTo(uploadFile);
    }

    public static void copyFile(String sourcePath,String destPath,String destName) {
        createDirIfNotExists(destPath);
        File dest=new File(absolutePath,staticDir+destPath+destName);
        File source=new File(absolutePath,staticDir+sourcePath);
        try {
            Files.copy(source.toPath(),dest.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void moveFile(String sourcePath,String destPath,String destName) {
        createDirIfNotExists(destPath);
        File dest=new File(absolutePath,staticDir+destPath+destName);
        File source=new File(absolutePath,staticDir+sourcePath);
        try {
            Files.move(source.toPath(),dest.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void renameFile(String srcPath,String oldName,String name){
        createDirIfNotExists("");
        File file=new File(absolutePath,staticDir+srcPath+oldName);
        if (file.exists())
            file.renameTo(new File(absolutePath,staticDir+srcPath+name));
    }

    public static File previewFile(File file,String name) throws IOException {
        createDirIfNotExists("preview/");
        File destFile=new File(absolutePath,staticDir+"preview/"+name);
        new XDocService().to(file,destFile);
        return destFile;
    }

    /**
     * 创建文件夹路径
     */
    public static void createDirIfNotExists(@Nullable String path) {
        if (absolutePath.isEmpty()) {

            //获取跟目录
            File file;
            try {
                file = new File(ResourceUtils.getURL("classpath:").getPath());
            }catch (FileNotFoundException e){
                e.printStackTrace();
                file = new File("/home/FileStation");
            }

//            if (!file.exists()) {
//                try {
//                    file = new File(ResourceUtils.getURL("classpath:").getPath());
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//            }

            absolutePath = file.getAbsolutePath();
        }

        if (path!=null) {
            File upload = new File(absolutePath, staticDir + path);
            if (!upload.exists()) {
                upload.mkdirs();
            }
        }
    }

    public static boolean delete(String sourcePath) {
        createDirIfNotExists(null);
        File file = new File(absolutePath, staticDir+sourcePath);
        return file.exists() && file.delete();
    }

    public static String getSuffix(String fileName) {
        if(!fileName.contains(".")) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf(".");
        String suffix = fileName.substring(dotIndex);
        if ("gz".equals(suffix.toLowerCase()))
            suffix=".tar.gz";
        return suffix;
    }

    public static void delFolder(String folderPath) {
        try {
            createDirIfNotExists(null);
            delAllFile(folderPath); //删除完里面所有内容
            File myFilePath = new File(absolutePath,staticDir+ folderPath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void delAllFile(String path) {
        File file = new File(absolutePath,staticDir+path);
        if (!file.exists()) {
            return;
        }
        if (!file.isDirectory()) {
            return;
        }
        String[] tempList = file.list();
        File temp;
        for (String aTempList : Objects.requireNonNull(tempList)) {
            if (path.endsWith(File.separator)) {
                temp = new File(absolutePath,staticDir+path + aTempList);
            } else {
                temp = new File(absolutePath,staticDir+path + File.separator + aTempList);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + aTempList);//先删除文件夹里面的文件
                delFolder(path + "/" + aTempList);//再删除空文件夹
            }
        }
    }

    public static void compress(String srcPath , String dstPath) throws IOException{
        createDirIfNotExists(srcPath);
        createDirIfNotExists("compress/");
        File srcFile = new File(absolutePath,staticDir+srcPath);
        File dstFile = new File(absolutePath,staticDir+dstPath);
        if (!srcFile.exists()) {
            throw new FileNotFoundException(srcPath + "不存在！");
        }

        FileOutputStream out = null;
        ZipOutputStream zipOut = null;
        try {
            out = new FileOutputStream(dstFile);
            zipOut = new ZipOutputStream(out);
            String baseDir = "";
            compress(srcFile, zipOut, baseDir);
        }
        finally {
            if(null != zipOut){
                zipOut.close();
                out = null;
            }

            if(null != out){
                out.close();
            }
        }
    }

    private static void compress(File file, ZipOutputStream zipOut, String baseDir) throws IOException{
        if (file.isDirectory()) {
            compressDirectory(file, zipOut, baseDir);
        } else {
            compressFile(file, zipOut, baseDir);
        }
    }

    /** 压缩一个目录 */
    private static void compressDirectory(File dir, ZipOutputStream zipOut, String baseDir) throws IOException{
        File[] files = dir.listFiles();
        for (File file : Objects.requireNonNull(files)) {
            compress(file, zipOut, baseDir + dir.getName() + "/");
        }
    }

    /** 压缩一个文件 */
    private static void compressFile(File file, ZipOutputStream zipOut, String baseDir)  throws IOException {
        if (!file.exists()){
            return;
        }

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            ZipEntry entry = new ZipEntry(baseDir + file.getName());
            zipOut.putNextEntry(entry);
            int count;
            byte[] data = new byte[BUFFER];
            while ((count = bis.read(data, 0, BUFFER)) != -1) {
                zipOut.write(data, 0, count);
            }

        }
    }
}
