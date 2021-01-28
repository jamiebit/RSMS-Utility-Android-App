package com.jns.rsmsutility;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    String name;
    SharedPreferences sharedPreferences;
    TinyDB tinyDB;
    JsoupTest jsoupTest;
    TextView tvname,tvuid;
    CardView cvattendance,cvinternalmarks,cvsessionalmarks;

    //method to save the credentials to sharedPreferences to be reused
    public void saveCred()
    {
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("uid",WebHandler.user);
        editor.putString("pass",WebHandler.pass);
        editor.putString("name",name);
        editor.apply();
        tinyDB.putListString("semlist",WebHandler.listsem);
    }

    //method to obtain credentials from sharedPreferences
    public void getCred()
    {
        WebHandler.user=sharedPreferences.getString("uid","");
        WebHandler.pass=sharedPreferences.getString("pass","");

        WebHandler.listsem=tinyDB.getListString("semlist");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //to prevent the activity from going in landscape mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //to prevent the activity from going dark mode
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_main);

        tvname=findViewById(R.id.tvname);
        tvuid=findViewById(R.id.tvuid);

        cvattendance=findViewById(R.id.cvattendance);
        cvinternalmarks=findViewById(R.id.cvinternalmarks);
        cvsessionalmarks=findViewById(R.id.cvsessionalmarks);

        sharedPreferences=getSharedPreferences("login",MODE_PRIVATE);
        tinyDB=new TinyDB(this);


        if(savedInstanceState == null){
            if(!sharedPreferences.contains("pass")) {
                Intent intent=new Intent(MainActivity.this,com.jns.rsmsutility.LoginActivity.class);
                startActivityForResult(intent, 3);
            }
            else {
                //setting info in the app if previously logged in
                getCred();
                tvname.setText(sharedPreferences.getString("name","name not found"));
                tvuid.setText(WebHandler.user);
            }

        }

        //setting on click listener of Attendance CardView
        cvattendance.setOnClickListener(v -> {
            Intent intent1=new Intent(MainActivity.this, AttendanceActivity.class);
            startActivity(intent1);
        });

        cvinternalmarks.setOnClickListener(v -> {
            Intent intent=new Intent(MainActivity.this,InternalMarksActivity.class);
            startActivity(intent);
        });

        cvsessionalmarks.setOnClickListener(v -> {
            Intent intent=new Intent(MainActivity.this,SessionalMarksActivity.class);
            startActivity(intent);
        });

    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //coming back from the login activity
        if(requestCode==3)
        {
            if (resultCode==RESULT_OK)
            {
                WebHandler.user=data.getStringExtra("uid");
                WebHandler.pass=data.getStringExtra("pass");


                jsoupTest=new JsoupTest(WebHandler.user,WebHandler.pass);
                jsoupTest.execute();


            }
        }
    }



    //for fetching data (This specifically for authentication)
    @SuppressLint("StaticFieldLeak")
    public class JsoupTest extends AsyncTask<Void,Void,Void>
    {
        private String username;
        private String password;

        ProgressDialog dialog;

        public JsoupTest(String username, String password) {
            this.username = username;
            this.password = password;

        }

        @Override
        protected Void doInBackground(Void... voids) {
            //checking if the Credentials entered by the user is correct
            name=WebHandler.getAuth(username,password);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //to show the Loading dialouge box, while data is fetched
            dialog=new ProgressDialog(MainActivity.this);
            dialog.setTitle("login");
            dialog.setMessage("Please wait while loading...");
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();

            //checking the authentication of the user
            if(name.equals(" ")) {
                Toast.makeText(MainActivity.this,"Invalid Login Id/ Password",Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(MainActivity.this,com.jns.rsmsutility.LoginActivity.class);
                startActivityForResult(intent, 3);
            }
            else
            {
                saveCred();
                tvname.setText(name);
                tvuid.setText(WebHandler.user);
            }
        }
    }


}