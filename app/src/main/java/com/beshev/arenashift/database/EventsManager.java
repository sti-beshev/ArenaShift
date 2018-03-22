package com.beshev.arenashift.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.beshev.arenashift.beans.ArenaShiftEvent;
import com.beshev.arenashift.beans.ArenaShiftUserEvent;
import com.beshev.arenashift.user.UserManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class EventsManager extends BaseManager {

    public EventsManager(Context context) {
        super(context);

        this.context = context;
    }

    public void logDayEvent(String userName, String dayToCheck, Date whenIsChekedDate) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss.SSS", Locale.US);
        String date = sdf.format(whenIsChekedDate);

        openDataBase();

        ContentValues values = new ContentValues();
        values.put(DataBaseManager.COLUMN_DATE, date);
        values.put(DataBaseManager.COLUMN_EVENT, userName + " отвори: " + dayToCheck);

        sqliteDataBase.insertWithOnConflict(DataBaseManager.TABLE_EVENTS, null, values,
                SQLiteDatabase.CONFLICT_IGNORE);

    }

    public void logException(String userName, String exceptionAsString, Date dateItHappend) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss.SSS", Locale.US);
        String date = sdf.format(dateItHappend);

        openDataBase();

        ContentValues values = new ContentValues();
        values.put(DataBaseManager.COLUMN_DATE, date);
        values.put(DataBaseManager.COLUMN_EVENT, userName + " грешка: " + exceptionAsString);

        sqliteDataBase.insertWithOnConflict(DataBaseManager.TABLE_EVENTS, null, values,
                SQLiteDatabase.CONFLICT_IGNORE);
    }

    public ArrayList<ArenaShiftEvent> getEvents() {

        ArrayList<ArenaShiftEvent> eventsList = null;
        DateFormat format = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss.SSS", Locale.US);

        openDataBase();

        Cursor cursor = sqliteDataBase.query(DataBaseManager.TABLE_EVENTS, null,
                null, null, null, null, null);

        if(cursor != null && cursor.getCount() > 0) {

            eventsList = new ArrayList<>();

            while (cursor.moveToNext()) {

                Date date = null;
                try {
                    date = format.parse(cursor.getString(1));
                } catch (ParseException e) {
                    logException(UserManager.getUserName(context), e.toString(), new Date());
                }
                eventsList.add(new ArenaShiftEvent(date, cursor.getString(2)));
            }
        }

        cursor.close();

        return eventsList;
    }

    // The key for the hashmap is the event ID in the database.
    public HashMap<Integer, ArenaShiftEvent> getEventsAsHashMap() {

        HashMap<Integer, ArenaShiftEvent> eventsHashMap = null;

        DateFormat format = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss.SSS", Locale.US);

        openDataBase();

        Cursor cursor = sqliteDataBase.query(DataBaseManager.TABLE_EVENTS, null, null,
                null, null, null, null);

        if(cursor != null && cursor.getCount() > 0) {

            eventsHashMap = new HashMap<Integer, ArenaShiftEvent>();

            while (cursor.moveToNext()) {

                Date date = null;
                try {
                    date = format.parse(cursor.getString(1));
                } catch (ParseException e) {
                    logException(UserManager.getUserName(context), e.toString(), new Date());
                }

                eventsHashMap.put(cursor.getInt(0), new ArenaShiftEvent(date, cursor.getString(2)));
            }
        }

        cursor.close();

        return eventsHashMap;
    }

    // The key for the hashmap is the event ID in the database.
    public HashMap<Integer, ArenaShiftUserEvent> getEventsAsHashMapWithTimeAsString() {

        HashMap<Integer, ArenaShiftUserEvent> eventsHashMap = null;

        openDataBase();

        Cursor cursor = sqliteDataBase.query(DataBaseManager.TABLE_EVENTS, null, null,
                null, null, null, null);

        if(cursor != null && cursor.getCount() > 0) {

            eventsHashMap = new HashMap<Integer, ArenaShiftUserEvent>();

            while (cursor.moveToNext()) {

                eventsHashMap.put(cursor.getInt(0), new ArenaShiftUserEvent(cursor.getString(1),
                        cursor.getString(2)));
            }
        }

        cursor.close();

        return eventsHashMap;
    }

    public void deleteEvents(HashMap<Integer, ArenaShiftEvent> eventsHashMap) {

        for(Integer key: eventsHashMap.keySet()) {

            String whereClause = "_id=?";
            String[] whereArgs = new String[] { Integer.toString(key) };
            sqliteDataBase.delete(DataBaseManager.TABLE_EVENTS, whereClause, whereArgs);
        }
    }

    public void deleteUserEvents(HashMap<Integer, ArenaShiftUserEvent> eventsHashMap) {

        for(Integer key: eventsHashMap.keySet()) {

            String whereClause = "_id=?";
            String[] whereArgs = new String[] { Integer.toString(key) };
            sqliteDataBase.delete(DataBaseManager.TABLE_EVENTS, whereClause, whereArgs);
        }
    }

    public void emptyTableEvents() {

        openDataBase();

        sqliteDataBase.delete(DataBaseManager.TABLE_EVENTS, null, null);
    }
}