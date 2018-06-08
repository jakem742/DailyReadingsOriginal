package com.liftyourheads.dailyreadings;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.TimeZone;

import static android.content.Context.MODE_PRIVATE;

public class CommandmentsWidgetProvider extends AppWidgetProvider {

    private static final String ACTION_SCHEDULED_UPDATE = "com.liftyourheads.dailyreadings.SCHEDULED_UPDATE";
    private static final String QUOTES_DB = "CommandmentsOfChrist.db";
    private static final String QUOTES_TABLE = "Commandments_Of_Christ";

    String[] text;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int count = appWidgetIds.length;


        for (int i = 0; i < count; i++) {
            int widgetId = appWidgetIds[i];

            text = getQuote(context);

            //Set the TV's to today's quote
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_commandments);
            remoteViews.setTextViewText(R.id.quoteTextView, text[0]);
            remoteViews.setTextViewText(R.id.refTextView, text[1]);

            //Go to app on click
            /*Intent goToApp = new Intent(context, activity_date.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, goToApp, 0);
            remoteViews.setOnClickPendingIntent(R.id.quotesWidgetLinearLayout, pendingIntent);*/

            Intent updateIntent = new Intent(context, CommandmentsWidgetProvider.class);
            updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

            //Update the current widget instance only, by creating an array that contains the widget’s unique ID//

            int[] idArray = new int[]{widgetId};
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray);


            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, widgetId, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.quotesWidgetLinearLayout, pendingIntent);

            //Schedule midnight update
            _scheduleNextUpdate(context);


            appWidgetManager.updateAppWidget(widgetId, remoteViews);

        }
    }

    //Retrieve readings for use in widget view
    public String[] getQuote(Context context) {

        activity_date.checkDatabase(QUOTES_DB);

        SQLiteDatabase commandmentsDB = context.openOrCreateDatabase(QUOTES_DB, MODE_PRIVATE, null);


        Cursor quotes = commandmentsDB.rawQuery("SELECT * FROM " + QUOTES_TABLE + " WHERE Used = 'no'", null);
        Log.i("Quotes Found", Integer.toString(quotes.getCount()));

        Random rand = new Random();
        int num = rand.nextInt(quotes.getCount());

        Log.i("Random number chosen",Integer.toString(num));

        quotes.moveToPosition(num);
        String[] quote = new String[2];

        //Quote content
        quote[0] = quotes.getString(1);
        //Quote references
        quote[1] = quotes.getString(2);

        //Update comment timestampe
        //ContentValues timeStamp = new ContentValues();
        //timeStamp.put("Used",System.nanoTime());
        //commandmentsDB.update(QUOTES_TABLE,timeStamp,"LIMIT ?,1", new String[] {Integer.toString(num-1)});

        Log.i("Quote",quote[0] + " " + quote[1]);

        quotes.close();

        return quote;

    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(ACTION_SCHEDULED_UPDATE)) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int[] ids = manager.getAppWidgetIds(new ComponentName(context, CommandmentsWidgetProvider.class));
            onUpdate(context, manager, ids);
        }

        super.onReceive(context, intent);

    }

    private static void _scheduleNextUpdate(Context context) {
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // Substitute AppWidget for whatever you named your AppWidgetProvider subclass
        Intent intent = new Intent(context, CommandmentsWidgetProvider.class);
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



