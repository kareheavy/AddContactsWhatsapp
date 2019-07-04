package com.jhonjimenez.myapplication;

import android.text.InputFilter;
import android.text.Spanned;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    public static final String DATE_FORMAT_ANIOMESDIA = "yyyy-MM-dd";
    public static final String PREFERENCES = "preferences";
    public static final String ID_CLIENT = "id_cliente";
    public static final String CLIENTES = "clientes";
    public static final String CONSECUTIVO = "consecutivo";
    public static final String NOMBRE_BASE = "nombre_base";

    public static String getDate() {
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_ANIOMESDIA, new Locale("es_ES"));
        return format.format(new Date());
    }

    public static class EditTextFilters {

        public static InputFilter filterAlphaNumeric25 = new InputFilter() {

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (dstart < 25) {
                    if (source.equals("")) { // for backspace
                        return source;
                    }
                    if (source.toString().matches("[a-zA-ZñÑÁáÉéÍíÓóÚú# 0-9_-]+")) {
                        return source;
                    }
                    return "";
                } else {
                    return "";
                }

            }
        };

    }

}
