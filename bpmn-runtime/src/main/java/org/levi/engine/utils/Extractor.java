package org.levi.engine.utils;

import org.levi.engine.Constants;
import org.levi.engine.LeviException;
import org.levi.engine.impl.StorageServiceImpl;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Ishan Jayawardena
 */
public final class Extractor {
    private final static int BUFSIZE = 2048;

    public static ExtractData extract(String larPath)
            throws IOException {
        check(larPath);
        ExtractData exData = new ExtractData();
        FileInputStream larFile = new FileInputStream(larPath);
        ZipInputStream lar = new ZipInputStream(new BufferedInputStream(larFile));
        ZipEntry entry;
        BufferedOutputStream dest;
        entry = lar.getNextEntry();
        String destDir;
        if (!entry.isDirectory()) {
            throw new RuntimeException("Lar: " + larPath + " does not have a top level directory");
        } else {
            destDir = makePath(entry.getName());
            //TODO in the storage service, after checking for duplicates, these dirs must be deleted
            boolean result = (new File(destDir)).mkdir();
            if (!result) {
                throw new LeviException("Could not extract 1 " + larPath + ". " + destDir + " already exists.");
            }
            String topLevelDir = destDir + entry.getName();
            result = (new File(topLevelDir)).mkdir();
            if (!result) {
                throw new LeviException("Could not extract 2 " + larPath + ". " + topLevelDir + " already exists.");
            }
            exData.setExtractPath(topLevelDir);
        }
        String file;
        while ((entry = lar.getNextEntry()) != null) {
            if (entry.isDirectory()) {
                boolean result = (new File(destDir + entry.getName())).mkdir();
                if (!result) {
                    return null;
                }
                continue;
            }
            int count;
            byte data[] = new byte[BUFSIZE];
            file = destDir + entry.getName();
            if (file.endsWith(Constants.BPMN_FILE_EXTENSION)) {
                exData.addBPMNFile(file);
            } else {
                if (file.endsWith(Constants.VELOCITY_EXTENSION)) {
                    file = Constants.VELOCITY_TEMPLATES + LeviUtils.getFileName(entry.getName());
                    System.out.println("file: " + file);
                }
                exData.addOtherFile(file);
            }
            FileOutputStream fos = new FileOutputStream(file);
            dest = new BufferedOutputStream(fos, BUFSIZE);
            while ((count = lar.read(data, 0, BUFSIZE)) != -1) {
                dest.write(data, 0, count);
            }
            dest.flush();
            dest.close();
        }
        lar.close();
        if (!exData.validate()) {
            StorageServiceImpl.delete(destDir, true);
        }
        return exData;
    }

    private static void check(String larPath) {
        if (larPath == null) {
            throw new IllegalArgumentException("Lar path is null");
        }
        if (larPath.endsWith(Constants.LAR_EXTENSION)) {
            return;
        }
        throw new RuntimeException("Cannot deploy the archive " + larPath + ". Not a Levi archive(lar)");
    }

    private static String makePath(String entryName) {
        String path;
        path = "";//LeviUtils.getyyyyMMddHHmmss();
        String name = LeviUtils.trimAndReplace(entryName);
        return Constants.LAR_EXTRACT_PATH + name + "_" + path + "/";
    }


}
