
package com.blogspot.dibargatin.counterspro.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.supercsv.cellprocessor.FmtDate;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.blogspot.dibargatin.counterspro.R;
import com.blogspot.dibargatin.counterspro.database.Counter;
import com.blogspot.dibargatin.counterspro.database.Counter.RateType;
import com.blogspot.dibargatin.counterspro.database.Indication;
import com.blogspot.dibargatin.counterspro.database.IndicationsCollection;

public class CsvUtils {
    // ===========================================================
    // Constants
    // ===========================================================
    private final static int FIELD_COUNTER = 0;

    private final static int FIELD_DATE = 1;

    private final static int FIELD_TOTAL = 2;

    private final static int FIELD_VALUE = 3;

    private final static int FIELD_TARIFF = 4;

    private final static int FIELD_FORMULA = 5;

    private final static int FIELD_COST = 6;

    private final static int FIELD_NOTE = 7;

    // ===========================================================
    // Fields
    // ===========================================================
    private Context mContext;

    private String[] mTotalAliases;

    private String[] mValueAliases;

    private String[] mRateAliases;

    // ===========================================================
    // Constructors
    // ===========================================================
    public CsvUtils(Context context) {
        mContext = context;

        mValueAliases = mContext.getResources().getStringArray(R.array.formula_var_value_aliases);
        mTotalAliases = mContext.getResources().getStringArray(R.array.formula_var_total_aliases);
        mRateAliases = mContext.getResources().getStringArray(R.array.formula_var_tariff_aliases);

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
    private CellProcessor[] getProcessors() {
        final CellProcessor[] processors = new CellProcessor[8];

        processors[FIELD_COUNTER] = new Optional();
        processors[FIELD_DATE] = new FmtDate("yyyy/MM/dd HH:mm");
        processors[FIELD_TOTAL] = new Optional();
        processors[FIELD_VALUE] = new NotNull();
        processors[FIELD_TARIFF] = new NotNull();
        processors[FIELD_FORMULA] = new Optional();
        processors[FIELD_COST] = new Optional();
        processors[FIELD_NOTE] = new Optional();

        return processors;
    }

    public void export(Counter counter, IndicationsCollection indications) {
        final String[] header = mContext.getResources().getStringArray(R.array.csv_export_fields);
        String fileName = FileUtils.DIRECTORY_EXPORT + "//" + FileUtils.getNewCSVExportFileName();
        ICsvMapWriter mapWriter = null;

        try {
            File sd = Environment.getExternalStorageDirectory();
            File file = new File(sd, fileName);

            if (sd.canWrite()) {
                mapWriter = new CsvMapWriter(new FileWriter(file),
                        CsvPreference.STANDARD_PREFERENCE);

                final CellProcessor[] processors = getProcessors();

                mapWriter.writeHeader(header);

                for (Indication ind : indications) {
                    final Map<String, Object> line = new HashMap<String, Object>();

                    line.put(header[FIELD_COUNTER], ind.getCounter().getName());
                    line.put(header[FIELD_DATE], new Date(ind.getDate().getTime()));
                    line.put(header[FIELD_VALUE], ind.getValue());
                    line.put(header[FIELD_TARIFF], ind.getRateValue());
                    line.put(header[FIELD_TOTAL], ind.getTotal());

                    if (counter.getRateType() == RateType.SIMPLE) {
                        final String f = mContext.getResources().getString(R.string.formula_simple);
                        line.put(header[FIELD_FORMULA], f);
                    } else {
                        line.put(header[FIELD_FORMULA], counter.getFormula());
                    }

                    line.put(header[FIELD_COST], ind.calcCost(Indication.COST_PRECISION,
                            mTotalAliases, mValueAliases, mRateAliases));
                    line.put(header[FIELD_NOTE], ind.getNote());

                    mapWriter.write(line, header, processors);
                }

                Toast.makeText(mContext, mContext.getResources().getString(R.string.export_ok),
                        Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(mContext,
                        mContext.getResources().getString(R.string.sdcard_not_available),
                        Toast.LENGTH_LONG).show();
            }

        } catch (IOException e) {

            Toast.makeText(mContext, mContext.getResources().getString(R.string.export_trouble),
                    Toast.LENGTH_LONG).show();

        } finally {
            if (mapWriter != null) {
                try {
                    mapWriter.close();
                } catch (Exception e) {

                    // Что-то не так

                }

            }
        }
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
