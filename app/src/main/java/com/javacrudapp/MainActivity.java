package com.javacrudapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton btn_add;
    private FloatingActionButton btn_clear_all; // Add clear all button
    private DatabaseReference databaseReference;
    private List<com.myapplication.TaskModel> taskList;
    private TaskAdapter taskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        btn_add = findViewById(R.id.add_button);
        btn_clear_all = findViewById(R.id.clear_all_button); // Initialize clear all button
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        taskList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("tasks");

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTaskDialog();
            }
        });

        // Set onClickListener for clear all button
        btn_clear_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllTasks();
            }
        });

        // Add ValueEventListener to fetch data from Firebase
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                taskList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    com.myapplication.TaskModel task = snapshot.getValue(com.myapplication.TaskModel.class);
                    taskList.add(task);
                }
                if (taskAdapter == null) {
                    taskAdapter = new TaskAdapter(MainActivity.this, taskList, new TaskAdapter.OnTaskClickListener() {
                        @Override
                        public void onUpdateClick(int position) {
                            com.myapplication.TaskModel updatedTask = taskList.get(position);
                            updateTask(updatedTask);
                        }

                        @Override
                        public void onDeleteClick(int position) {
                            com.myapplication.TaskModel deletedTask = taskList.get(position);
                            deleteTask(deletedTask);
                        }

                        @Override
                        public void onTaskClick(com.myapplication.TaskModel task) {
                            // Handle task click if needed
                        }
                    });
                    recyclerView.setAdapter(taskAdapter);
                } else {
                    taskAdapter.notifyDataSetChanged(); // Notify adapter of dataset changes
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to show add task dialog
    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Task");

        final EditText inputField = new EditText(this);
        inputField.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(inputField);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String taskName = inputField.getText().toString().trim();
                if (!TextUtils.isEmpty(taskName)) {
                    String id = databaseReference.push().getKey();
                    com.myapplication.TaskModel task = new com.myapplication.TaskModel(id, taskName);
                    databaseReference.child(id).setValue(task);
                    Toast.makeText(MainActivity.this, "Task added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a task", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    // Method to update task
    private void updateTask(final com.myapplication.TaskModel task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Task");

        final EditText inputField = new EditText(this);
        inputField.setInputType(InputType.TYPE_CLASS_TEXT);
        inputField.setText(task.getName());
        builder.setView(inputField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String updatedTaskName = inputField.getText().toString().trim();
                if (!TextUtils.isEmpty(updatedTaskName)) {
                    task.setName(updatedTaskName);
                    databaseReference.child(task.getId()).setValue(task);
                    Toast.makeText(MainActivity.this, "Task updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a task", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    // Method to delete task
    private void deleteTask(final com.myapplication.TaskModel task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Task");
        builder.setMessage("Are you sure you want to delete this task?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                databaseReference.child(task.getId()).removeValue();
                Toast.makeText(MainActivity.this, "Task deleted", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    // Method to clear all tasks
    private void clearAllTasks() {
        if (taskList.isEmpty()) {
            Toast.makeText(MainActivity.this, "No tasks to clear", Toast.LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Clear All Tasks");
            builder.setMessage("Are you sure you want to clear all tasks?");

            builder.setPositiveButton("Clear All", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Clear all tasks from Firebase Realtime Database
                    databaseReference.removeValue();
                    Toast.makeText(MainActivity.this, "All tasks cleared", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }
    }
}
