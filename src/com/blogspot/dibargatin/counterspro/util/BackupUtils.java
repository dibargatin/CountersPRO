
package com.blogspot.dibargatin.counterspro.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.blogspot.dibargatin.counterspro.R;
import com.blogspot.dibargatin.counterspro.database.DBHelper;

public class BackupUtils {
    // ===========================================================
    // Constants
    // ===========================================================
    
    // ===========================================================
    // Fields
    // ===========================================================
    Context mContext;

    // ===========================================================
    // Constructors
    // ===========================================================
    public BackupUtils(Context context) {
        mContext = context;

        FileUtils.mkdir(FileUtils.DIRECTORY_BACKUP);
        FileUtils.mkdir(FileUtils.DIRECTORY_EXPORT);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================
    
    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================    
    public boolean backup() {
        boolean result = false;

        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                FileUtils.copyFile(new FileInputStream(new File(data, DBHelper.DB_NAME_FULL)),
                        new FileOutputStream(new File(sd, FileUtils.DIRECTORY_BACKUP + "//"
                                + FileUtils.getNewBackupFileName())));

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
                FileUtils.copyFile(new FileInputStream(new File(sd, sourceFileName)),
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
