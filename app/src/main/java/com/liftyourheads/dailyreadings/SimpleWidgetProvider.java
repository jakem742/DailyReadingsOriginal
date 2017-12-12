package com.liftyourheads.dailyreadings;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static android.content.Context.MODE_PRIVATE;

public class SimpleWidgetProvider extends AppWidgetProvider {

    private static final String ACTION_SCHEDULED_UPDATE = "com.liftyourheads.dailyreadings.SCHEDULED_UPDATE";

    private String curMonth;
    private Integer curDay;
    SimpleDateFormat monthStringTemplate = new SimpleDateFormat("MMMM");
    String[] readings;


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int count = appWidgetIds.length;

        for (int i = 0; i < count; i++) {
            int widgetId = appWidgetIds[i];

            getDate();
            readings = getDailyReadings(context, curDay, curMonth);

            //Set the TV's to today's readings
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
            remoteViews.setTextViewText(R.id.reading1TextView, readings[0]);
            remoteViews.setTextViewText(R.id.reading2TextView, readings[1]);
            remoteViews.setTextViewText(R.id.reading3TextView, readings[2]);

            //Go to app on click
            Intent goToApp = new Intent(context, BibleMainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, goToApp, 0);
            remoteViews.setOnClickPendingIntent(R.id.readingWidgetLinearLayout, pendingIntent);

            //Schedule midnight update
            _scheduleNextUpdate(context);


            appWidgetManager.updateAppWidget(widgetId, remoteViews);

        }
    }

    //Retrieve readings for use in widget view
    public String[] getDailyReadings(Context context, Integer day, String month) {

        SQLiteDatabase readingsDB = context.openOrCreateDatabase("DB/DailyReadings.db", MODE_PRIVATE, null);

        Cursor reading = readingsDB.rawQuery("SELECT * FROM daily_readings WHERE month = '" + month + "\' AND date = " + day.toString(), null);
        Log.i("Readings Found", Integer.toString(reading.getCount()));

        reading.moveToFirst();
        String[] portions = new String[3];

        int portionColumn = reading.getColumnIndex("first_book");

        do {

            for (int i = 0; i < 3; i++) {

                portions[i] = reading.getString(portionColumn++) + " " + reading.getString(portionColumn++).replaceAll(",", ", ");

            }

        } while (reading.moveToNext());

        reading.close();

        return portions;

    }

    public void getDate() {

        //// GET CURRENT DATE INFO ////

        Calendar cal = new GregorianCalendar(TimeZone.getDefault());

        curMonth = monthStringTemplate.format(cal.getTime());
        curDay = cal.get(Calendar.DAY_OF_MONTH);

    }


    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(ACTION_SCHEDULED_UPDATE)) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int[] ids = manager.getAppWidgetIds(new ComponentName(context, SimpleWidgetProvider.class));
            onUpdate(context, manager, ids);
        }

        super.onReceive(context, intent);

    }

    private static void _scheduleNextUpdate(Context context) {
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // Substitute AppWidget for whatever you named your AppWidgetProvider subclass
        Intent intent = new Intent(context, SimpleWidgetProvider.class);
        intent.setAction(ACTION_SCHEDULED_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        // Get a calendar instance for midnight tomorrow.
        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        // Schedule one second after midnight, to be sure we are in the right day next time this
        // method is called.  Otherwise, we risk calling onUpdate multiple times within a few
        // milliseconds
        midnight.set(Calendar.SECOND, 1);
        midnight.set(Calendar.MILLISECOND, 0);
        midnight.add(Calendar.DAY_OF_YEAR, 1);

        // For API 19 and later, set may fire the intent a little later to save battery,
        // setExact ensures the intent goes off exactly at midnight.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, midnight.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, midnight.getTimeInMillis(), pendingIntent);
        }
    }


}



