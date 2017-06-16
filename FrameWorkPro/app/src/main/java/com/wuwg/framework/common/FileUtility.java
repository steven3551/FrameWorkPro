/*
 * Copyright (C) 2013 TD Tech<br>
 * All Rights Reserved.<br>
 * 
 */
package com.wuwg.framework.common;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**文件操作的工具类
 * Create Date: 2014年5月30日<br>
 * Create Author: cWX19923<br>
 * Description :
 * @hide
 */
public class FileUtility
{

    private static final int BUFFER = 8192;
    private static final int TOOBIG = 0x6400000; // max size of unzipped data, 100MB
    private static final int TOOMANY = 1024; // max number of files

    private static final String TAG = "FileUtility";

    /**
     * 文件复制
     * @param srcFile 复制的原文件
     * @param destFile 复制的目标文件
     * @return 成功返回true，失败返回false
     */
    public static boolean copyFile(File srcFile, File destFile)
    {
       // Log.d(TAG, "copy file:srcFile=" + srcFile.getAbsolutePath() + ",destFile=" + destFile.getAbsolutePath());
        boolean result;
        try
        {
            InputStream in = new FileInputStream(srcFile);
            try
            {
                result = copyToFile(in, destFile);
            }
            finally
            {
                in.close();
            }
        }
        catch(IOException e)
        {
            Log.e(TAG, "copyFile occur an exception:" + e.toString());
            result = false;
        }
        return result;
    }

    /**
     * 将流中内容复制到一个文件中
     * @param inputStream 复制的流
     * @param destFile 复制的目标文件
     * @return 成功返回true，失败返回false
     */
    public static boolean copyToFile(InputStream inputStream, File destFile)
    {
        if(inputStream == null)
        {
            Log.w(TAG, "inputStream == null");
            return false;
        }
        try
        {
            if(destFile.exists() && !destFile.delete())
            {
                Log.w(TAG, "delete file failed : filepath " );
            }
            FileOutputStream out = null;
            try
            {
                out = new FileOutputStream(destFile);
                byte[] buffer = new byte[BUFFER];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) >= 0)
                {
                    out.write(buffer, 0, bytesRead);
                }
            }
            finally
            {
                
            	if(out != null)
            	{
	                try
	                {
	            		out.flush();
	            		out.getFD().sync();
	                }
	                catch(IOException e)
	                {
	                    Log.e(TAG, "out.flush exception:" + e.toString());
	                }
	                
	                try
	                {
                		out.close();
	                }
	                catch(IOException e)
	                {
	                    Log.e(TAG, "out.close exception:" + e.toString());
	                }
            	}

            }
            return true;
        }
        catch(IOException e)
        {
            Log.e(TAG, "copyToFile occur an exception:" + e.toString());
            return false;
        }
    }

    /**
     * 创建文件目录，如果已经存在，则不重复创建
     * @param path 要创建的目录
     */
    public static void mkdirs(String path)
    {
        File dir = new File(path);
        if(!dir.exists() && !dir.mkdirs())
        {
        	//add by chenjie:防止在日志文件创建失败时，循环调用Log日志打印
            Log.w(TAG, "failed to make directory:dir=" + Utils.toSafeText(path));
        }
    }

    /**
     * 删除整个文件目录
     * @param filePath 文件夹或文件路径
     */
    public static boolean delFiles(String filePath)
    {
        if(filePath == null){
            Log.i(TAG, "delFiles filePath is null.");
            return true;
        }
        Log.d(TAG, "the file path  will be deleted");
        File myFile = new File(filePath);
        if(myFile.exists())
        {
            // 如果是文件，直接删除
            if(myFile.isFile() && !myFile.delete())
            {
                Log.e(TAG, "delete file failed:filePath " );
		   return false;
            }
            // 如果是文件夹，递归删除
            else if(myFile.isDirectory() && null != myFile.listFiles())
            {
                for (File tmpFile : myFile.listFiles())
                {
                    if(tmpFile!=null)//coverity check ---yzf
                    {
                        try
                        {
                            if(!delFiles(tmpFile.getCanonicalPath()))
                            	{
                            	     Log.e(TAG, "delete file failed:filePath " );
		                        return false;
                            	}	
                        }
                        catch(IOException e)
                        {
                            Log.e(TAG, "An exception occurred: "+e.toString());
                            return false;
                        }
                    }
                    
                }
                // 文件删除后，把空文件夹删掉
                if(!myFile.delete())
                {
                    Log.e(TAG, "delete file failed:filePath ");
			 return false;
                }
            }
        }
        Log.i(TAG, "delete files successfully.");
	 return true;
    }
    
    /** 在文件夹下查找指定文件
     * @param fileName 待查找的文件名
     * @param searchDir 指定查找的文件目录
     * @return 如果找到，返回文件的全路径，否则返回为NULL
     */
    public static String searchFile(String fileName,String searchDir)
    {
        String strRetFile = null;
        File dstFileDir = new File(searchDir);
        if(!dstFileDir.exists() || !dstFileDir.isDirectory())
        {
            Log.w(TAG, "the searchDir does not exist or is not directory");
        }
        else if( null != dstFileDir.listFiles())
        {
            for(File f :dstFileDir.listFiles())
            {
                if(f!=null)//coverity check---yzf
                {
                    if(f.exists() && f.isFile())
                    {
                        //Log.d(TAG, "fileName ="+fileName+",f.getName ="+f.getAbsolutePath());
                        if(fileName.equalsIgnoreCase(f.getName()))
                        {
                            Log.d(TAG, "file has been found,the file is ");
                            try
                            {
                                return f.getCanonicalPath();
                            }
                            catch(IOException e)
                            {
                                Log.w(TAG, "An exception occurred: "+e.toString());
                                return null;
                            }
                        }
                    }
                    else if(f.exists() && f.isDirectory())
                    {
                        try
                        {
                            strRetFile = searchFile(fileName, f.getCanonicalPath());
                        }
                        catch(IOException e)
                        {
                            Log.w(TAG, "An exception occurred: "+e.toString());
                            return null;
                        }
                    }
                }
                
                
            }
        }
        return strRetFile;
    }

    /**
     * 压缩一个文件或文件夹成zip文件
     * @param srcPathName 源文件路径，可以是一个文件，也可以是一个文件目录
     * @param dstZipFileName 压缩包名称
     */
    public static void compress(String srcPathName, String dstZipFileName)
    {
        Log.d(TAG, "compress file:the srcPathName=" + srcPathName + ",the dstZipFileName=" + dstZipFileName);
        File file = new File(srcPathName);
        if(!file.exists())
        {
            Log.w(TAG, "compress file:the srcPathName " + srcPathName + "does not exist!!!");
            return;
        }
        try
        {
            File zipFile = new File(dstZipFileName);
            FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
            CheckedOutputStream cos = new CheckedOutputStream(fileOutputStream, new CRC32());
            ZipOutputStream out = new ZipOutputStream(cos);
            String basedir = "";
            compress(file, out, basedir);
            out.close();
            fileOutputStream.close();
            cos.close();
        }
        catch(IOException e)
        {
            Log.e(TAG, "compress occur an exception:", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 文件/目录压缩
     * @param file 要压缩的文件/目录
     * @param out   压缩后的流
     * @param basedir 压缩文件中Entry的根路径
     */
    public static void compress(File file, ZipOutputStream out, String basedir)
    {
        /* 判断是目录还是文件 */
        if(file.isDirectory())
        {
            // System.out.println("压缩：" + basedir + file.getName());
            compressDirectory(file, out, basedir);
        }
        else
        {
            // System.out.println("压缩：" + basedir + file.getName());
            compressFile(file, out, basedir);
        }
    }
    
    /** 压缩一个目录 */
    private static void compressDirectory(File dir, ZipOutputStream out, String basedir)
    {
        if(!dir.exists())
            return;
       // File[] files =new File[]{};//coverity check ---yzf
        File[] files = dir.listFiles();
        if(files == null)
        {
        	return;
        }
        for (int i = 0; i < files.length; i++)
        {
            /* 递归 */
            compress(files[i], out, basedir + dir.getName() + "/");
        }
    }

    /** 压缩一个文件 */
    private static void compressFile(File file, ZipOutputStream out, String basedir)
    {
        if(!file.exists())
        {
            Log.w(TAG, "the file  does not exist!!!");
            return;
        }
        BufferedInputStream bis = null;
        try
        {
            bis = new BufferedInputStream(new FileInputStream(file));
            ZipEntry entry = new ZipEntry(basedir + file.getName());
            out.putNextEntry(entry);
            int count;
            byte data[] = new byte[BUFFER];
            while ((count = bis.read(data, 0, BUFFER)) != -1)
            {
                out.write(data, 0, count);
            }
        }
        catch(IOException e)
        {
            Log.e(TAG, "compressFile occur an exception:" + e.toString());
            throw new RuntimeException(e);
        }
        finally
        {
            try
            {
                if(bis != null)
                {
                    bis.close();
                    bis = null;
                }
            }
            catch(IOException e)
            {
                Log.e(TAG, "close stream occur an exception:" + e.toString());
            }
        }
    }

    /* 检查解压路径是否正确 */
    private static String sanitzeFileName(String entryName, String intendedDir) throws IOException
    {
        File f = new File(intendedDir, entryName);
        String canonicalPath = f.getCanonicalPath();
        
        File iD = new File(intendedDir);
        String canonicalID = iD.getCanonicalPath();
        
        if (canonicalPath.startsWith(canonicalID))
        {
            return canonicalPath;
        }
        else
        {
            throw new IllegalStateException(
                    "File is outside extraction target directory.");
        }
    }
    
    /**
     * 解压到指定目录
     * @param zipPath 要解压的压缩文件
     * @param destDir 解压输出的目录
     */
    public static void decompress(String zipPath, String destDir)
    {
        try
        {
            //如果文件夾路徑不以分隔符為結尾，則自動加上
            if(!destDir.endsWith(File.separator))
                destDir = destDir + File.separator;
            decompress(new File(zipPath), destDir);
        }
        catch(IOException e)
        {
            Log.e(TAG, "decompress occur an exception:" + e.toString());
        }
        Log.i(TAG, "the file decompress successful");
    }

    /**
     * 解压文件到指定目录
     * @param zipFile
     * @param destDir
     */
    @SuppressWarnings("rawtypes")
    private static void decompress(File zipFile, String destDir) throws IOException
    {
        //如果文件夾路徑不以分隔符為結尾，則自動加上
        if(!destDir.endsWith(File.separator))
            destDir = destDir + File.separator;
        File pathFile = new File(destDir);
        if(!pathFile.exists() && !pathFile.mkdirs())
        {
            Log.w(TAG, "failed to make directory:dir=" + destDir);
        }
        ZipFile zip = new ZipFile(zipFile);
        try
        {
            int total = 0;
            int entriesNo = 0;
            for (Enumeration entries = zip.entries(); entries.hasMoreElements();)
            {
                InputStream in = null;
                OutputStream out = null;
                try
                {
                    ZipEntry entry = (ZipEntry) entries.nextElement();
                    String zipEntryName = sanitzeFileName(entry.getName(), destDir);                  
                    in = zip.getInputStream(entry);
                    String outPath = zipEntryName.replaceAll("\\*", "/");
                    // 判断路径是否存在,不存在则创建文件路径
                    File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
                    if(!file.exists() && !file.mkdirs())
                    {
                        Log.w(TAG, "failed to make directory:dir=");
                    }
                    // 判断文件全路径是否为文件夹,如果是上面已经创建,不需要解压
                    if(new File(outPath).isDirectory())
                    {
                        continue;
                    }
                    out = new FileOutputStream(outPath);
                    byte[] buf1 = new byte[1024];
                    int len;
                    if((in != null)&&(out != null))
                    {
	                    while (total + BUFFER <= TOOBIG && (len = in.read(buf1)) > 0)
	                    {
	                        out.write(buf1, 0, len);
	                        total += len;
	                    }
                    }
                    entriesNo++;
                    if (entriesNo > TOOMANY)
                    {
                        throw new IllegalStateException("Too many files to unzip.");
                    }
                    if (total > TOOBIG)
                    {
                        throw new IllegalStateException(
                                "File being unzipped is too big.");
                    }

                }
                catch(FileNotFoundException e)
                {
                    Log.e(TAG, "occur an FileNotFoundException :" + e.toString());
                }
                catch(IOException e)
                {
                    Log.e(TAG, "occur an IOException :" + e.toString());
                }
                catch(RuntimeException e)
                {
                    Log.e(TAG, "occur an RuntimeException :" + e.toString());
                }
                finally
                {
                    if(in != null)
                    {
                    	try
                    	{
                    		in.close();
                    	}
                        catch(IOException e)
                        {
                            Log.e(TAG, "in.close IOException :" + e.toString());
                        }
                    }
                    if(out != null)
                    {
                    	try
                    	{
                    		out.close();
                    	}
                        catch(IOException e)
                        {
                            Log.e(TAG, "out.close IOException :" + e.toString());
                        }
                    }
                }
            }
        }
        finally
        {
            zip.close();
        }
    }
}
