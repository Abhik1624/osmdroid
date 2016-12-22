package org.osmdroid.debug.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.text.format.DateFormat;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * from http://stackoverflow.com/a/38024962/1203182
 */

public class FileDateUtil {

    public static String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String getModifiedDate(long modified) {
        return getModifiedDate(Locale.getDefault(), modified);
    }

    public static String getModifiedDate(Locale locale, long modified) {
        SimpleDateFormat dateFormat = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            dateFormat = new SimpleDateFormat(getDateFormat(locale));
        } else {
            dateFormat = new SimpleDateFormat("MMM/dd/yyyy hh:mm:ss aa");
        }

        return dateFormat.format(new Date(modified));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static String getDateFormat(Locale locale) {
        return DateFormat.getBestDateTimePattern(locale, "MM/dd/yyyy hh:mm:ss aa");
    }
}
