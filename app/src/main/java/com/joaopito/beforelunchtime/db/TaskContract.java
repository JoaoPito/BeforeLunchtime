package com.joaopito.beforelunchtime.db;

import android.provider.BaseColumns;

//This class defines the constants that will be used to access data in the DB
//TODO:Create a new column to classify tasks as DONE or not(Need to add it to the TaskDbHelper too)
public class TaskContract {

    public static final String DB_NAME = "com.joaopito.beforelunchtime.db";
    public static final int DB_VERSION = 1;

    public class TaskEntry implements BaseColumns {
        public static final String TABLE = "tasks";

        public static final String COL_TASK_TITLE = "title";
    }

}
