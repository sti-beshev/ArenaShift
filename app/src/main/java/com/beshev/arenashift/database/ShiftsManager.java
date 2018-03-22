package com.beshev.arenashift.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.beshev.arenashift.beans.Shift;

public class ShiftsManager extends BaseManager {

    public ShiftsManager(Context context) {
        super(context);
    }

    public void addShift(Shift shift) {

        openDataBase();

        createNewTableIfNeeded(shift.getYear());

        ContentValues values = new ContentValues();
        values.put(DataBaseManager.COLUMN_MONTH, shift.getMonth());
        values.put(DataBaseManager.COLUMN_DAY, shift.getDay());
        values.put(DataBaseManager.COLUMN_PAN_MEHANIK, shift.getPanMehanik());
        values.put(DataBaseManager.COLUMN_PAN_KASA_ONE, shift.getPanKasaOne());
        values.put(DataBaseManager.COLUMN_PAN_KASA_TWO, shift.getPanKasaTwo());
        values.put(DataBaseManager.COLUMN_PAN_KASA_THREE, shift.getPanKasaThree());
        values.put(DataBaseManager.COLUMN_RAZPOREDITEL_ONE, shift.getRazporeditelOne());
        values.put(DataBaseManager.COLUMN_RAZPOREDITEL_TWO, shift.getRazporeditelTwo());

        Cursor cursor = getShiftWithCursor(shift.getYear(), shift.getMonth(), shift.getDay());

		// If there is a shift with this date - update it.
        if(cursor.moveToFirst()) {
            values.put(DataBaseManager.COLUMN_ID, cursor.getInt(0));
        }

        cursor.close();

        sqliteDataBase.insertWithOnConflict(tableName(shift.getYear()), null, values,
                SQLiteDatabase.CONFLICT_REPLACE);

    }

    public Shift getShift(int year, int month, int day) {

        openDataBase();
        Shift shift = null;

        if(checkIfTableExist(year)) {

            Cursor cursor = getShiftWithCursor(year, month, day);

            if(cursor.moveToFirst()) {

                shift = new Shift();
                shift.setPanMehanik(cursor.getString(3));
                shift.setPanKasaOne(cursor.getString(4));
                shift.setPanKasaTwo(cursor.getString(5));
                shift.setPanKasaThree(cursor.getString(6));
                shift.setRazporeditelOne(cursor.getString(7));
                shift.setRazporeditelTwo(cursor.getString(8));
            }

            cursor.close();
        }

        return shift;
    }

    public Cursor getShiftWithCursor(int year, int month, int day) {

        openDataBase();

        String tableName = tableName(year);

        return sqliteDataBase.query(tableName,
                null,
                DataBaseManager.COLUMN_MONTH + " = ? AND " + DataBaseManager.COLUMN_DAY + " =? ",
                new String[] {Integer.toString(month), Integer.toString(day)},
                null, null, null);
    }

    // The name of a table can't start with a number
    private String tableName(int year) {

        return "a" + Integer.toString(year);
    }

    private void createNewTableIfNeeded(int year) {

        openDataBase();

        String tableName = tableName(year);

        if(!(checkIfTableExist(year))) {

            String CREATE_TABLE_YEAR = "CREATE TABLE " + tableName + "("
                    + DataBaseManager.COLUMN_ID + " INTEGER PRIMARY KEY,"
                    + DataBaseManager.COLUMN_MONTH + " INTEGER,"
                    + DataBaseManager.COLUMN_DAY + " INTEGER,"
                    + DataBaseManager.COLUMN_PAN_MEHANIK + " TEXT,"
                    + DataBaseManager.COLUMN_PAN_KASA_ONE + " TEXT,"
                    + DataBaseManager.COLUMN_PAN_KASA_TWO + " TEXT,"
                    + DataBaseManager.COLUMN_PAN_KASA_THREE + " TEXT,"
                    + DataBaseManager.COLUMN_RAZPOREDITEL_ONE + " TEXT,"
                    + DataBaseManager.COLUMN_RAZPOREDITEL_TWO + " TEXT,"
                    + DataBaseManager.COLUMN_CEN_MEHANIK + " TEXT,"
                    + DataBaseManager.COLUMN_CEN_KASA + " TEXT);";

            sqliteDataBase.execSQL(CREATE_TABLE_YEAR);
        }
    }

    private Boolean checkIfTableExist(int year) {

        openDataBase();

        String tableName = tableName(year);

        Cursor cursor = sqliteDataBase.query("sqlite_master",
                new String[]{"name"},
                "type=? AND name=?",
                new String[]{"table", tableName},
                null, null, null);

        if(cursor.moveToFirst()) {

            cursor.close();
            return true;
        }

        cursor.close();

        return false;
    }
}
