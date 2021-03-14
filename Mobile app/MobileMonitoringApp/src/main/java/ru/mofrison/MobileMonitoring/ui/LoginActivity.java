package ru.mofrison.MobileMonitoring.ui;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import ru.mofrison.MobileMonitoring.R;
import ru.mofrison.MobileMonitoring.mqtt.Connection;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    //private Context context;
    private Connection connection;
    // UI references.
    private EditText mPasswordView, mUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //context = this;

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.mc_login);

        connection = Connection.getInstance();
        if(connection.getStatus() == Connection.Status.CONNECT) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // declaring obejct of EditText control
        mUserName = (EditText) findViewById(R.id.txtUserName);
        mPasswordView = (EditText) findViewById(R.id.txtPassword);

        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String UserName = mUserName.getText().toString();
                String Pwd = mPasswordView.getText().toString();
                if((mUserName.length() != 0) && (mPasswordView.length() != 0)){
                    Intent MainIntent = new Intent(LoginActivity.this, MainActivity.class).addFlags(
                        Intent.FLAG_ACTIVITY_TASK_ON_HOME).addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                    connection.connect((Application) getApplicationContext(), getURI(), UserName, Pwd, MainIntent);
                }
            }
        });
    }

    private String getURI() {
        SharedPreferences sharedPreferences;
        sharedPreferences = getSharedPreferences("MControlPref",MODE_PRIVATE);
        String uri = "tcp://" + sharedPreferences.getString(SettingsActivity.URL, "")
                + (":" + sharedPreferences.getString(SettingsActivity.PORT, ""));
        return new String(uri);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Операции для выбранного пункта меню
        switch (item.getItemId())
        {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.settings:
                Intent intent = new Intent(LoginActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

