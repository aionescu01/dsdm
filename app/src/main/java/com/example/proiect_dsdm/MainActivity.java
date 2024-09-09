package com.example.proiect_dsdm;

import static androidx.navigation.ui.NavigationUI.setupActionBarWithNavController;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.room.Room;

import com.example.proiect_dsdm.login.LoginActivity;
import com.example.proiect_dsdm.login.RegisterActivity;
import com.example.proiect_dsdm.task.Task;
import com.example.proiect_dsdm.task.TaskDatabase;
import com.example.proiect_dsdm.task.TaskEntity;
import com.example.proiect_dsdm.task.TaskListFragment;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private Button logoutButton;
    public static TaskDatabase database;
    private FirebaseAuth mAuth;
    private static final String PREF_NAME = "AuthPrefs";
    private static final String KEY_USER_ID = "userId";
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        database = Room.databaseBuilder(getApplicationContext(),
                TaskDatabase.class, "task_database").build();


        if (getUserId() == null || getUserId().isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new TaskListFragment())
                    .commit();
        }

        logoutButton = findViewById(R.id.logout);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove(KEY_USER_ID);
                editor.apply();

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });


    }

    private String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }


}
