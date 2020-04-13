package com.esp.library.utilities.common;

import android.text.format.DateUtils;

import com.esp.library.exceedersesp.ESPApplication;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class DateTimeUtils {

    private DateTimeUtils() {
        throw new AssertionError();
    }

    /*
     * see http://drdobbs.com/java/184405382
     */
    private static final ThreadLocal<DateFormat> ISO8601Format = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        }
    };

    /*
     * converts a date to a relative time span ("8h", "3d", etc.) - similar to
     * DateUtils.getRelativeTimeSpanString but returns shorter result
     */
    public static String javaDateToTimeSpan(final Date date) {
        if (date == null) {
            return "";
        }

        long passedTime = date.getTime();
        long currentTime = System.currentTimeMillis();

        // return "now" if less than a minute has elapsed
        long secondsSince = (currentTime - passedTime) / 1000;
        if (secondsSince < 60) {
            return "now";
        }

        // less than an hour (ex: 12m)
        long minutesSince = secondsSince / 60;
        if (minutesSince < 60) {
            return minutesSince + "m ago";
        }

        // less than a day (ex: 17h)
        long hoursSince = minutesSince / 60;
        if (hoursSince < 24) {
            return hoursSince + "h ago";
        }

        // less than a week (ex: 5d)
        long daysSince = hoursSince / 24;
        if (daysSince < 7) {
            return daysSince + "d ago";
        }

        // less than a year old, so return day/month without year (ex: Jan 30)
        if (daysSince < 365) {
            return DateUtils.formatDateTime(ESPApplication.getInstance(), passedTime,
                    DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_ABBREV_ALL);
        }

        // date is older, so include year (ex: Jan 30, 2013)
        return DateUtils.formatDateTime(ESPApplication.getInstance(), passedTime,
                DateUtils.FORMAT_ABBREV_ALL);
    }

    /*
     * converts an ISO8601 date to a Java date
     */
    public static Date iso8601ToJavaDate(final String strDate) {
        try {
            DateFormat formatter = ISO8601Format.get();
            return formatter.parse(strDate);
        } catch (ParseException e) {
            return null;
        }
    }

    /*
     * converts a Java date to ISO8601
     */
    public static String javaDateToIso8601(Date date) {
        if (date == null) {
            return "";
        }
        DateFormat formatter = ISO8601Format.get();
        return formatter.format(date);
    }

    /*
     * returns the current UTC date
     */
    public static Date nowUTC() {
        Date dateTimeNow = new Date();
        return localDateToUTC(dateTimeNow);
    }

    public static Date localDateToUTC(Date dtLocal) {
        if (dtLocal == null) {
            return null;
        }
        TimeZone tz = TimeZone.getDefault();
        int currentOffsetFromUTC =
                tz.getRawOffset() + (tz.inDaylightTime(dtLocal) ? tz.getDSTSavings() : 0);
        return new Date(dtLocal.getTime() - currentOffsetFromUTC);
    }

    public static Date UTCDateToLocal(Date dtLocal) {
        if (dtLocal == null) {
            return null;
        }
        long ts = System.currentTimeMillis();
        Date localTime = new Date(ts);
        // Convert UTC to Local Time
        Date fromGmt = new Date(
                dtLocal.getTime() + TimeZone.getDefault().getOffset(localTime.getTime()));
        //System.out.println("UTC time:" + dtLocal.toString() + "," + dtLocal.getTime() + " --> Local:"
        //        + fromGmt.toString() + "-" + fromGmt.getTime());

        return fromGmt;
    }

    /*
     * routines to return a diff between two dates - always return a positive number
     */
    public static int minutesBetween(Date dt1, Date dt2) {
        long msDiff = millisecondsBetween(dt1, dt2);
        if (msDiff == 0) {
            return 0;
        }
        return (int) (msDiff / 60000);
    }

    public static int secondsBetween(Date dt1, Date dt2) {
        long msDiff = millisecondsBetween(dt1, dt2);
        if (msDiff == 0) {
            return 0;
        }
        return (int) (msDiff / 1000);
    }

    public static long millisecondsBetween(Date dt1, Date dt2) {
        if (dt1 == null || dt2 == null) {
            return 0;
        }
        return Math.abs(dt1.getTime() - dt2.getTime());
    }

    public static long iso8601ToTimestamp(final String strDate) {
        Date date = iso8601ToJavaDate(strDate);
        if (date == null) {
            return 0;
        }
        return (date.getTime() / 1000);
    }

    /*
     * routines involving Unix timestamps (GMT assumed)
     */
    public static Date timestampToDate(long timeStamp) {
        return new Date(timeStamp * 1000);
    }

    public static String timestampToIso8601Str(long timestamp) {
        return javaDateToIso8601(timestampToDate(timestamp));
    }
    /*public static String timestampToTimeSpan(long timeStamp) {
        Date dtGmt = timestampToDate(timeStamp);
        return javaDateToTimeSpan(dtGmt);
    }*/


    /**
     * <p>Checks if two dates are on the same day ignoring time.</p>
     *
     * @param date1 the first date, not altered, not null
     * @param date2 the second date, not altered, not null
     * @return true if they represent the same day
     * @throws IllegalArgumentException if either date is <code>null</code>
     */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDay(cal1, cal2);
    }

    /**
     * <p>Checks if two calendars represent the same day ignoring time.</p>
     *
     * @param cal1 the first calendar, not altered, not null
     * @param cal2 the second calendar, not altered, not null
     * @return true if they represent the same day
     * @throws IllegalArgumentException if either calendar is <code>null</code>
     */
    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

    /**
     * <p>Checks if a date is today.</p>
     *
     * @param date the date, not altered, not null.
     * @return true if the date is today.
     * @throws IllegalArgumentException if the date is <code>null</code>
     */
    public static boolean isToday(Date date) {
        return isSameDay(date, Calendar.getInstance().getTime());
    }

    /**
     * <p>Checks if a calendar date is today.</p>
     *
     * @param cal the calendar, not altered, not null
     * @return true if cal date is today
     * @throws IllegalArgumentException if the calendar is <code>null</code>
     */
    public static boolean isToday(Calendar cal) {
        return isSameDay(cal, Calendar.getInstance());
    }

    /**
     * <p>Checks if the first date is before the second date ignoring time.</p>
     *
     * @param date1 the first date, not altered, not null
     * @param date2 the second date, not altered, not null
     * @return true if the first date day is before the second date day.
     * @throws IllegalArgumentException if the date is <code>null</code>
     */
    public static boolean isBeforeDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isBeforeDay(cal1, cal2);
    }

    /**
     * <p>Checks if the first calendar date is before the second calendar date ignoring time.</p>
     *
     * @param cal1 the first calendar, not altered, not null.
     * @param cal2 the second calendar, not altered, not null.
     * @return true if cal1 date is before cal2 date ignoring time.
     * @throws IllegalArgumentException if either of the calendars are <code>null</code>
     */
    public static boolean isBeforeDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        if (cal1.get(Calendar.ERA) < cal2.get(Calendar.ERA)) {
            return true;
        }
        if (cal1.get(Calendar.ERA) > cal2.get(Calendar.ERA)) {
            return false;
        }
        if (cal1.get(Calendar.YEAR) < cal2.get(Calendar.YEAR)) {
            return true;
        }
        if (cal1.get(Calendar.YEAR) > cal2.get(Calendar.YEAR)) {
            return false;
        }
        return cal1.get(Calendar.DAY_OF_YEAR) < cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * <p>Checks if the first date is after the second date ignoring time.</p>
     *
     * @param date1 the first date, not altered, not null
     * @param date2 the second date, not altered, not null
     * @return true if the first date day is after the second date day.
     * @throws IllegalArgumentException if the date is <code>null</code>
     */
    public static boolean isAfterDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isAfterDay(cal1, cal2);
    }

    /**
     * <p>Checks if the first calendar date is after the second calendar date ignoring time.</p>
     *
     * @param cal1 the first calendar, not altered, not null.
     * @param cal2 the second calendar, not altered, not null.
     * @return true if cal1 date is after cal2 date ignoring time.
     * @throws IllegalArgumentException if either of the calendars are <code>null</code>
     */
    public static boolean isAfterDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        if (cal1.get(Calendar.ERA) < cal2.get(Calendar.ERA)) {
            return false;
        }
        if (cal1.get(Calendar.ERA) > cal2.get(Calendar.ERA)) {
            return true;
        }
        if (cal1.get(Calendar.YEAR) < cal2.get(Calendar.YEAR)) {
            return false;
        }
        if (cal1.get(Calendar.YEAR) > cal2.get(Calendar.YEAR)) {
            return true;
        }
        return cal1.get(Calendar.DAY_OF_YEAR) > cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * <p>Checks if a date is after today and within a number of days in the future.</p>
     *
     * @param date the date to check, not altered, not null.
     * @param days the number of days.
     * @return true if the date day is after today and within days in the future .
     * @throws IllegalArgumentException if the date is <code>null</code>
     */
    public static boolean isWithinDaysFuture(Date date, int days) {
        if (date == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return isWithinDaysFuture(cal, days);
    }

    /**
     * <p>Checks if a calendar date is after today and within a number of days in the future.</p>
     *
     * @param cal  the calendar, not altered, not null
     * @param days the number of days.
     * @return true if the calendar date day is after today and within days in the future .
     * @throws IllegalArgumentException if the calendar is <code>null</code>
     */
    public static boolean isWithinDaysFuture(Calendar cal, int days) {
        if (cal == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar today = Calendar.getInstance();
        Calendar future = Calendar.getInstance();
        future.add(Calendar.DAY_OF_YEAR, days);
        return (isAfterDay(cal, today) && !isAfterDay(cal, future));
    }

    /**
     * Returns the given date with the time set to the start of the day.
     */
    public static Date getStart(Date date) {
        return clearTime(date);
    }

    /**
     * Returns the given date with the time values cleared.
     */
    public static Date clearTime(Date date) {
        if (date == null) {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /** Determines whether or not a date has any time values (hour, minute,
     * seconds or millisecondsReturns the given date with the time values cleared. */

    /**
     * Determines whether or not a date has any time values.
     *
     * @param date The date.
     * @return true iff the date is not null and any of the date's hour, minute, seconds or
     * millisecond values are greater than zero.
     */
    public static boolean hasTime(Date date) {
        if (date == null) {
            return false;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        if (c.get(Calendar.HOUR_OF_DAY) > 0) {
            return true;
        }
        if (c.get(Calendar.MINUTE) > 0) {
            return true;
        }
        if (c.get(Calendar.SECOND) > 0) {
            return true;
        }
        return c.get(Calendar.MILLISECOND) > 0;
    }

    /**
     * Returns the given date with time set to the end of the day
     */
    public static Date getEnd(Date date) {
        if (date == null) {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTime();
    }

    public static Date getDateWithOutTime(Date date) {

        if (date == null) {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * Returns the maximum of two dates. A null date is treated as being less
     * than any non-null date.
     */
    public static Date max(Date d1, Date d2) {
        if (d1 == null && d2 == null) {
            return null;
        }
        if (d1 == null) {
            return d2;
        }
        if (d2 == null) {
            return d1;
        }
        return (d1.after(d2)) ? d1 : d2;
    }

    /**
     * Returns the minimum of two dates. A null date is treated as being greater
     * than any non-null date.
     */
    public static Date min(Date d1, Date d2) {
        if (d1 == null && d2 == null) {
            return null;
        }
        if (d1 == null) {
            return d2;
        }
        if (d2 == null) {
            return d1;
        }
        return (d1.before(d2)) ? d1 : d2;
    }

    public static boolean isYesterday(Date date) {
        return isSameDay(getYesterdayDate(), date);
    }

    private static Date getYesterdayDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }

    public static boolean isTomorrow(Date date) {
        return isSameDay(getTomorrowDate(), date);
    }

    private static Date getTomorrowDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        return cal.getTime();
    }

    /**
     * The maximum date possible.
     */
    public static Date MAX_DATE = new Date(Long.MAX_VALUE);

}
