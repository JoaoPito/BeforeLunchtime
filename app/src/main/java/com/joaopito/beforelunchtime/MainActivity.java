package com.joaopito.beforelunchtime;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.joaopito.beforelunchtime.db.TaskContract;
import com.joaopito.beforelunchtime.db.TaskDbHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //TODO:Functionality - The user needs to be able to set a timer for the lunchtime and a notification must appear
    //TODO:Functionality - Edit the tasks already created
    //TODO:Functionality - readjusting the order of tasks in the list
    //TODO:Maybe create an activity for adding new tasks, and for adding more info about the task

    //Used to know where the debug msgs come from
    private static final String TAG = "MainActivity";

    private TaskDbHelper mHelper;

    //The UI for displaying the tasks
    private ListView mTaskListView;
    private CheckboxAdapter taskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHelper = new TaskDbHelper(this);
        mTaskListView = (ListView) findViewById(R.id.list_todo);

        UpdateUI();
    }

    //Creates the main_menu.xml menu in the activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Deals with input from the user
    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch (item.getItemId()){
            case R.id.action_add_task:
                //Aqui vai abrir uma janela para adicionar uma tarefa nova
                //Essa janela tem titulo, subtitulo, input de texto, um botao para
                //adicionar e outro para cancelar
                final EditText taskEditText = new EditText(this);
                AlertDialog dialog = new AlertDialog.Builder(this)

                        .setTitle(R.string.MainAct_AddTask_Title)//Titulo da janela
                        .setMessage(R.string.MainAct_AddTask_Subtitle)//Subtitulo
                        .setView(taskEditText)
                        //Botao "Positivo" (Adicionar tarefa)
                        .setPositiveButton(R.string.Default_Create, new DialogInterface.OnClickListener() {
                            @Override//Funçao que lida quando o user clica no botao add
                            public void onClick(DialogInterface dialog, int which) {
                                String task = String.valueOf(taskEditText.getText());

                                createTask(false,task);
                                UpdateUI();
                            }
                        })
                        //Cancela a acao
                        .setNegativeButton(R.string.Default_Cancel, null)
                        .create();

                dialog.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //This is our custom ArrayAdapter that handles checkboxes and strings
    public class CheckboxAdapter extends ArrayAdapter {
        Context context;
        List<Boolean> checkboxState;
        List<String> checkboxItems;

        public CheckboxAdapter(Context context, List<String> resource, ArrayList<Boolean> checkboxResource) {
            super(context, R.layout.item_todo, resource);

            this.context = context;
            this.checkboxItems = resource;
            this.checkboxState = checkboxResource;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.item_todo, parent, false);
            TextView textView = (TextView) convertView.findViewById(R.id.task_title);
            CheckBox cb = (CheckBox) convertView.findViewById(R.id.task_checkbox);

            textView.setText(checkboxItems.get(position));
            cb.setChecked(checkboxState.get(position));
            return convertView;
        }
    }

    private void UpdateUI(){
        ArrayList<String> taskList = new ArrayList<>();
        ArrayList<Boolean> doneList = new ArrayList<>();

        SQLiteDatabase db = mHelper.getReadableDatabase();//Our DB

        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                new String[] {TaskContract.TaskEntry._ID,
                              TaskContract.TaskEntry.COL_TASK_DONE,
                              TaskContract.TaskEntry.COL_TASK_TITLE},
                null, null, null ,null, null);

        while(cursor.moveToNext()){//Goes across all entries in the DB
            int text_idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            int done_idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_DONE);

            Boolean done_bool = false;
            if(cursor.getInt(done_idx)>0) done_bool = true;

            Log.d(TAG, "Task: " + cursor.getString(text_idx) + " Done: " + cursor.getInt(done_idx));

            taskList.add(cursor.getString(text_idx));//Adds the task to the UI
            doneList.add(done_bool);
        }

        //the mAdapter is attached to the ListView and helps us to populate our UI with stuff
        if(taskAdapter == null){
            taskAdapter = new CheckboxAdapter(this, taskList, doneList);
            mTaskListView.setAdapter(taskAdapter);
        } else {
            taskAdapter.clear();
            taskAdapter.addAll(taskList);
            taskAdapter.notifyDataSetChanged();
        }

        cursor.close();
        db.close();
    }

    public String getTextfromUI(View view){
        View parent = (View) view.getParent();
        TextView taskTextView = (TextView) parent.findViewById(R.id.task_title);
        String task = String.valueOf(taskTextView.getText()); //find the task by its text

        return task;
    }

    //Function for the DONE Button
    public void createTask(Boolean done, String text){
        SQLiteDatabase db = mHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        int doneTask = 0;
        if(done) doneTask = 1;

        values.put(TaskContract.TaskEntry.COL_TASK_DONE, doneTask);
        values.put(TaskContract.TaskEntry.COL_TASK_TITLE, text);

        db.insertWithOnConflict(TaskContract.TaskEntry.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void doneTask(View view){
        Boolean done = ((CheckBox) view).isChecked();

        View parent = (View) view.getParent();
        CheckBox taskCheckbox = (CheckBox) parent.findViewById(R.id.task_checkbox);

        //Its a bit of a hacky way but it first deletes the task and creates another with the new state
        //Probably if the user has 2 identical tasks could lead to some bugs
        //Maybe a better way would be to look for the task ID but i dont want to do this now
        String task = getTextfromUI(view);
        deleteTask(task);
        createTask(done,task);
    }

    public void deleteTaskUI(View view){
        String task = getTextfromUI(view); //find the task by its text
        deleteTask(task);

        UpdateUI();
    }

    public void deleteTask(String task){
        //Deals with the DB
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.delete(TaskContract.TaskEntry.TABLE,
                TaskContract.TaskEntry.COL_TASK_TITLE + " = ?",
                new String[]{task});
        db.close();
    }
}