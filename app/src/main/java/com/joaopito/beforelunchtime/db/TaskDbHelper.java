//This class manages the access to the DB

package com.joaopito.beforelunchtime.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TaskDbHelper extends SQLiteOpenHelper {

    public TaskDbHelper (Context context){
        super (context, TaskContract.DB_NAME, null, TaskContract.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        //This string has the commands for creating a table in SQL
        //Now it only contains an ID and some Text
        String createTable = "CREATE TABLE " + TaskContract.TaskEntry.TABLE + " ( " +
                TaskContract.TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + //Column for the ID
                TaskContract.TaskEntry.COL_TASK_DONE + " INTEGER DEFAULT 0, " + //Column for done status
                TaskContract.TaskEntry.COL_TASK_TITLE + " TEXT NOT NULL);"; //Column for the text

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TaskContract.TaskEntry.TABLE);
        onCreate(db);
    }


}
