package com.craftaro.epiclevels.utils;

import com.craftaro.epiclevels.settings.Settings;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class Methods {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###,###.###");
    private static final Map<String, DecimalFormat> FORMATS = new HashMap<>();

    public static String formatDecimal(double decimal) {
        return DECIMAL_FORMAT.format(decimal);
    }

    public static String formatDecimal(double decimal, String format) {
        if (format == null) {
            return formatDecimal(decimal);
        }
        DecimalFormat decimalFormat = FORMATS.get(format);
        if (decimalFormat == null) {
            decimalFormat = new DecimalFormat(format);
            FORMATS.put(format, decimalFormat);
        }
        return decimalFormat.format(decimal);
    }

    public static String generateProgressBar(double exp, double nextLevel, boolean placeholder) {
        double length = placeholder ?
                Settings.PROGRESS_BAR_LENGTH_PLACEHOLDER.getInt()
                : Settings.PROGRESS_BAR_LENGTH.getInt();
        double progress = (exp / nextLevel) * length;

        StringBuilder progressBar = new StringBuilder();
        for (int j = 0; j < length; ++j) {
            progressBar
                    .append("&")
                    .append(j > progress ? "c" : "a")
                    .append("|");
        }
        return progressBar.toString();
    }
}
