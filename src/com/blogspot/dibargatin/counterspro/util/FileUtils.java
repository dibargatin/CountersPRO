
package com.blogspot.dibargatin.counterspro.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.os.Environment;

public class FileUtils {
    // ===========================================================
    // Constants
    // ===========================================================
    public final static String DIRECTORY = "CountersPRO";

    public final static String DIRECTORY_BACKUP = DIRECTORY + "//Backup";

    public final static String DIRECTORY_EXPORT = DIRECTORY + "//Export";

    public final static String BACKUP_FILE_EXT = ".cpro";

    // ===========================================================
    // Fields
    // ===========================================================

    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================
    public static String getNewBackupFileName() {
        return new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(Calendar.getInstance().getTime())
                + BACKUP_FILE_EXT;
    }
    
    public static String getNewCSVExportFileName() {
        return new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(Calendar.getInstance().getTime())
                + ".csv";
    }

    public static boolean mkdir(String dirName) {
        boolean result = false;
        File dir = new File(Environment.getExternalStorageDirectory(), dirName);

        if (!dir.exists()) {
            if (!dir.mkdirs()) {

            }
        }

        return result;
    }

    public static void copyFile(FileInputStream fromFile, FileOutputStream toFile)
            throws IOException {
        FileChannel fromChannel = null;
        FileChannel toChannel = null;
        try {
            fromChannel = fromFile.getChannel();
            toChannel = toFile.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                }
            } finally {
                if (toChannel != null) {
                    toChannel.close();
                }
            }
        }
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
