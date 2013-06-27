
package com.blogspot.dibargatin.counterspro;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import com.blogspot.dibargatin.counterspro.util.BackupUtils;
import com.blogspot.dibargatin.counterspro.util.FileUtils;

public class RestoreActivity extends SherlockListActivity {
    // ===========================================================
    // Constants
    // ===========================================================
    private final static String ATTRIBUTE_DATE = "date";

    private final static String ATTRIBUTE_TIMESTAMP = "timestamp";

    private final static String ATTRIBUTE_FILENAME = "filename";

    // ===========================================================
    // Fields
    // ===========================================================
    SimpleAdapter mAdapter;

    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_menu_home);

        // Фон пустого списка счетчиков
        final View ev = View.inflate(this, R.layout.restore_list_empty, null);
        ev.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        ev.setVisibility(View.GONE);
        ((ViewGroup)getListView().getParent()).addView(ev);
        getListView().setEmptyView(ev);

        // Данные для списка бэкапов
        final File sd = Environment.getExternalStorageDirectory();
        File[] fl = new File(sd, FileUtils.DIRECTORY_BACKUP).listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(FileUtils.BACKUP_FILE_EXT);
            }
        });

        ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

        if (fl != null) {
            final java.text.DateFormat df = android.text.format.DateFormat.getDateFormat(this);
            final java.text.DateFormat tf = android.text.format.DateFormat.getTimeFormat(this);

            for (int i = 0; i < fl.length; i++) {
                Map<String, Object> item = new HashMap<String, Object>();

                Date d = new Date(fl[i].lastModified());
                String t = df.format(d) + " " + tf.format(d);

                item.put(ATTRIBUTE_DATE, d);
                item.put(ATTRIBUTE_TIMESTAMP, t);
                item.put(ATTRIBUTE_FILENAME, fl[i].getName());

                data.add(item);
            }
        }

        Collections.sort(data, new Comparator<Map<String, Object>>() {

            @Override
            public int compare(Map<String, Object> lhs, Map<String, Object> rhs) {
                return (((Date)lhs.get(ATTRIBUTE_DATE)).getTime() > ((Date)rhs.get(ATTRIBUTE_DATE))
                        .getTime() ? -1 : 1);
            }

        });

        String[] from = new String[] {
                ATTRIBUTE_TIMESTAMP, ATTRIBUTE_FILENAME
        };

        int[] to = new int[] {
                R.id.tvTime, R.id.tvFilename
        };

        mAdapter = new SimpleAdapter(this, data, R.layout.restore_list_item, from, to);
        getListView().setAdapter(mAdapter);

        // При нажатии на бэкап запустим восстановление
        getListView().setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
                AlertDialog.Builder alert = new AlertDialog.Builder(RestoreActivity.this);
                final int p = pos;

                alert.setIcon(R.drawable.reload);
                alert.setTitle(R.string.restore_dialog_title);
                alert.setMessage(R.string.restore_dialog_description);

                alert.setPositiveButton(getResources().getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> obj = (Map<String, Object>)mAdapter.getItem(p);

                                new BackupUtils(RestoreActivity.this)
                                        .restore(FileUtils.DIRECTORY_BACKUP + "//"
                                                + obj.get(ATTRIBUTE_FILENAME).toString());

                                setResult(RESULT_OK);
                                finish();
                            }
                        });

                alert.setNegativeButton(getResources().getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // На нет и суда нет
                            }
                        });

                alert.show();
            }
        });
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }

        return true;
    }

    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
