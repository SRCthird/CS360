package me.frigidambiance.projecttwo.net;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

public final class DateIso {
    private static final ThreadLocal<SimpleDateFormat> ISO = ThreadLocal.withInitial(() -> {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf;
    });

    private DateIso() {}

    public static String toIsoUtc(long epochMillis) {
        return Objects.requireNonNull(ISO.get()).format(new Date(epochMillis));
    }

    public static long parseIsoUtc(String iso) {
        if (iso == null) return System.currentTimeMillis();
        try {
            return Objects.requireNonNull(Objects.requireNonNull(ISO.get()).parse(iso)).getTime();
        } catch (ParseException e) {
            return System.currentTimeMillis();
        }
    }
}
