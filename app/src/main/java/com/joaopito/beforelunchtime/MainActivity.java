package com.joaopito.beforelunchtime;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.joaopito.beforelunchtime.db.TaskContract;
import com.joaopito.beforelunchtime.db.TaskDbHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //Used to know where the debug msgs come from
    private static final String TAG = "MainActivity";

    private TaskDbHelper mHelper;

    //The UI for displaying the tasks
    private ListView mTaskListView;
    private ArrayAdapter<String> mAdapter;

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
                Log.d(TAG, "Add new task");

                //Aqui vai abrir uma janela para adicionar uma tarefa nova
                //Essa janela tem titulo, subtitulo, input de texto, um botao para
                //adicionar e outro para cancelar
                final EditText taskEditText = new EditText(this);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        //Titulo da janela
                        .setTitle("Add a new task")
                        //Subtitulo
                        .setMessage("What are you going to do?")
                        .setView(taskEditText)
                        //Botao "Positivo" (Adicionar tarefa)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {

                            //Funçao que lida quando o user clica no botao
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String task = String.valueOf(taskEditText.getText());

                                SQLiteDatabase db = mHelper.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put(TaskContract.TaskEntry.COL_TASK_TITLE, task);
                                db.insertWithOnConflict(TaskContract.TaskEntry.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                                db.close();

                                UpdateUI();
                            }
                        })
                        //Cancela a acao
                        .setNegativeButton("Cancel", null)
                        .create();

                dialog.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void UpdateUI(){
        ArrayList<String> taskList = new ArrayList<>();

        SQLiteDatabase db = mHelper.getReadableDatabase();//Our DB

        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                new String[] {TaskContract.TaskEntry._ID, TaskContract.TaskEntry.COL_TASK_TITLE},
                null, null, null ,null, null);

        while(cursor.moveToNext()){//Goes across all entries in the DB
            int idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            Log.d(TAG, "Task: " + cursor.getString(idx));
            taskList.add(cursor.getString(idx));//Adds the task to the UI
        }

        //the mAdapter is attached to the ListView and helps us to populate our UI with stuff
        if(mAdapter == null){
            mAdapter = new ArrayAdapter<>(this,
                    R.layout.item_todo,
                    R.id.task_title, //The task string go here
                    taskList);
            mTaskListView.setAdapter(mAdapter);
        } else {
            mAdapter.clear();
            mAdapter.addAll(taskList);
            mAdapter.notifyDataSetChanged();
        }


        cursor.close();
        db.close();
    }

    //Function for the DONE Button
    public void deleteTask(View view){
        //Deals with the UI
        View parent = (View) view.getParent();
        TextView taskTextView = (TextView) parent.findViewById(R.id.task_title);
        String task = String.valueOf(taskTextView.getText());

        //Deals with the DB
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.delete(TaskContract.TaskEntry.TABLE, TaskContract.TaskEntry.COL_TASK_TITLE + " = ?",
                new String[]{task});
        db.close();

        UpdateUI();
    }
}