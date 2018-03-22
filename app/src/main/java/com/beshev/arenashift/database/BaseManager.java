package com.beshev.arenashift.database;


import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public abstract class BaseManager {

    protected DataBaseManager dataBaseManager;
    protected SQLiteDatabase sqliteDataBase;
    protected Context context;

    public BaseManager(Context context) {

        this.context = context;
        dataBaseManager = new DataBaseManager(context);
    }

    public void closeDataBase() {

        if(sqliteDataBase != null && sqliteDataBase.isOpen()) {

            sqliteDataBase.close();
        }
    }

    protected void openDataBase() {

        if(sqliteDataBase == null || !(sqliteDataBase.isOpen())) {

            try {

                sqliteDataBase = dataBaseManager.getReadableDatabase();

            } catch (SQLException e) {
                Log.e("ARENA_SHIFT", e.toString());
            }
        }
    }
}
