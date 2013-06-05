
package com.blogspot.dibargatin.counterspro.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.blogspot.dibargatin.counterspro.R;
import com.blogspot.dibargatin.counterspro.database.DBHelper;

public class ExportImport {
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
    Context mContext;

    // ===========================================================
    // Constructors
    // ===========================================================
    public ExportImport(Context context) {
        mContext = context;

        mkdir(DIRECTORY_BACKUP);
        mkdir(DIRECTORY_EXPORT);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================
    public static String getNewBackupFileName() {
        return new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(Calendar.getInstance().getTime())
                + BACKUP_FILE_EXT;
    }

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================
    private static boolean mkdir(String dirName) {
        boolean result = false;
        File dir = new File(Environment.getExternalStorageDirectory(), dirName);

        if (!dir.exists()) {
            if (!dir.mkdirs()) {

            }
        }

        return result;
    }

    private static void copyFile(FileInputStream fromFile, FileOutputStream toFile)
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

    public boolean backup() {
        boolean result = false;

        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                ExportImport.copyFile(new FileInputStream(new File(data, DBHelper.DB_NAME_FULL)),
                        new FileOutputStream(new File(sd, DIRECTORY_BACKUP + "//"
                                + getNewBackupFileName())));

                result = true;
                Toast.makeText(mContext, mContext.getResources().getString(R.string.backup_ok),
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(mContext,
                        mContext.getResources().getString(R.string.sdcard_not_available),
                        Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.backup_trouble),
                    Toast.LENGTH_LONG).show();
        }

        return result;
    }

    public boolean restore(String sourceFileName) {
        boolean result = false;

        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                ExportImport.copyFile(new FileInputStream(new File(sd, sourceFileName)),
                        new FileOutputStream(new File(data, DBHelper.DB_NAME_FULL)));

                result = true;
                Toast.makeText(mContext, mContext.getResources().getString(R.string.restore_ok),
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(mContext,
                        mContext.getResources().getString(R.string.sdcard_not_available),
                        Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.restore_trouble),
                    Toast.LENGTH_LONG).show();
        }

        return result;
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
