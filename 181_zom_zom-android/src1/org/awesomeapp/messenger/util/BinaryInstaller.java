/* Copyright (c) 2009, Nathan Freitas, Orbot / The Guardian Project - http://openideals.com/guardian */
/* See LICENSE for licensing information */

package org.awesomeapp.messenger.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.os.Build;
import android.util.Log;

public class BinaryInstaller {

    
    File installFolder;
    Context context;
    
    public BinaryInstaller(Context context, File installFolder)
    {
        this.installFolder = installFolder;
        
        this.context = context;
    }
    
    public void deleteDirectory(File file) {
        if( file.exists() ) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for(int i=0; i<files.length; i++) {
                    if(files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    }
                    else {
                        files[i].delete();
                    }
                }
            }
                
            file.delete();
        }
    }
    
    private final static String COMMAND_RM_FORCE = "rm -f ";
    private final static String MP3_EXT = ".mp3";
    //        
    /*
     * Extract the resources from the APK file using ZIP
     */
    public File installResource (String basePath, String assetKey, boolean overwrite) throws IOException, FileNotFoundException, TimeoutException
    {
        
        InputStream is;
        File outFile;

        outFile = new File(installFolder, assetKey);

        if (outFile.exists() && (!overwrite))
            return outFile;

        deleteDirectory(installFolder);
        installFolder.mkdirs();

        is = context.getAssets().open(basePath + '/' + assetKey);
        streamToFile(is,outFile, false, false);
        setExecutable(outFile);

    
        return outFile;
    }

    
    private final static int FILE_WRITE_BUFFER_SIZE = 1024*8;
    /*
     * Write the inputstream contents to the file
     */
    public static boolean streamToFile(InputStream stm, File outFile, boolean append, boolean zip) throws IOException

    {
        byte[] buffer = new byte[FILE_WRITE_BUFFER_SIZE];

        int bytecount;

        OutputStream stmOut = new FileOutputStream(outFile.getAbsolutePath(), append);
        ZipInputStream zis = null;
        
        if (zip)
        {
            zis = new ZipInputStream(stm);            
            ZipEntry ze = zis.getNextEntry();
            stm = zis;
            
        }
        
        while ((bytecount = stm.read(buffer)) > 0)
        {

            stmOut.write(buffer, 0, bytecount);

        }

        stmOut.close();
        stm.close();
        
        if (zis != null)
            zis.close();
        
        
        return true;

    }
    
    //copy the file from inputstream to File output - alternative impl
    public static boolean copyFile (InputStream is, File outputFile)
    {
        
        try {
        	if (outputFile.exists())
        		outputFile.delete();
        	
            boolean newFile = outputFile.createNewFile();
            DataOutputStream out = new DataOutputStream(new FileOutputStream(outputFile));
            DataInputStream in = new DataInputStream(is);
            
            int b = -1;
            byte[] data = new byte[1024];
            
            while ((b = in.read(data)) != -1) {
                out.write(data);
            }
            
            if (b == -1); //rejoice
            
            //
            out.flush();
            out.close();
            in.close();
            // chmod?
            
            return newFile;
            
            
        } catch (IOException ex) {
            Log.e("Binaryinstaller", "error copying binary", ex);
            return false;
        }

    }
    
    

   
    /**
     * Copies a raw resource file, given its ID to the given location
     * @param ctx context
     * @param resid resource id
     * @param file destination file
     * @param mode file permissions (E.g.: "755")
     * @throws IOException on error
     * @throws InterruptedException when interrupted
     */
    public static void copyRawFile(Context ctx, int resid, File file, String mode, boolean isZipd) throws IOException, InterruptedException
    {
        final String abspath = file.getAbsolutePath();
        // Write the iptables binary
        final FileOutputStream out = new FileOutputStream(file);
        InputStream is = ctx.getResources().openRawResource(resid);
        
        if (isZipd)
        {
            ZipInputStream zis = new ZipInputStream(is);            
            ZipEntry ze = zis.getNextEntry();
            is = zis;
        }
        
        byte buf[] = new byte[1024];
        int len;
        while ((len = is.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        is.close();
        // Change the permissions
        Runtime.getRuntime().exec("chmod "+mode+" "+abspath).waitFor();
    }
    

    private void setExecutable(File fileBin) {
        fileBin.setReadable(true);
        fileBin.setExecutable(true);
        fileBin.setWritable(false);
        fileBin.setWritable(true, true);
    }

}
