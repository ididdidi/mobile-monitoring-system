package ru.mofrison.MobileMonitoring.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;

import ru.mofrison.MobileMonitoring.R;

public class SettingsActivity extends AppCompatActivity {

    static final String URL = "url_address";
    static final String PORT = "port_number";

    private EditText url_address, port_number;

    private LinkedList<EditText> activeEdit = null;                     // number of active EditText

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ActionBar actionBar =getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.title_Settings);

        // Text boxes for entering name attributes
        url_address = (EditText) findViewById(R.id.editURL);
        port_number = (EditText) findViewById(R.id.editPort);

        setOnFocusChangeListener(url_address);
        setOnFocusChangeListener(port_number);
        setSettingsToEditTexts();
    }

    private void setSettingsToEditTexts() {
        SharedPreferences sharedPreferences;
        sharedPreferences = getSharedPreferences("MControlPref",MODE_PRIVATE);
        url_address.setText(sharedPreferences.getString(URL, ""));
        port_number.setText(sharedPreferences.getString(PORT, ""));
    }

    private void saveSettings() {
        SharedPreferences sharedPreferences;
        sharedPreferences = getSharedPreferences("MControlPref",MODE_PRIVATE);
        SharedPreferences.Editor spEditor = sharedPreferences.edit();
        spEditor.putString(URL, url_address.getText().toString());
        spEditor.putString(PORT, port_number.getText().toString());
        spEditor.commit();

        Toast.makeText(this,
                "Save settings",
                Toast.LENGTH_SHORT).show();
        Intent LoginIntent = new Intent(this, LoginActivity.class);
        startActivity(LoginIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        SharedPreferences preferences = getSharedPreferences("MControlPref",MODE_PRIVATE);

        if(preferences.getString(MainActivity.USEFLASH, "OFF").equals("ON")) {
            menu.getItem(0).setIcon(R.drawable.ic_highlight_white_off_32dp);
        }
        if(preferences.getString(MainActivity.AUTOFOCUS, "OFF").equals("ON")) {
            menu.getItem(1).setIcon(R.drawable.ic_center_focus_weak_white_32dp);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences preferences = getSharedPreferences("MControlPref",MODE_PRIVATE);
        SharedPreferences.Editor spEditor = preferences.edit();
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.save:
                saveSettings();
                return true;
            case R.id.light:
                if(preferences.getString(MainActivity.USEFLASH, "OFF").equals("ON")) {
                    item.setIcon(R.drawable.ic_highlight_white_on_32dp);
                    spEditor.putString(MainActivity.USEFLASH, "OFF");
                    spEditor.commit();
                } else{
                    item.setIcon(R.drawable.ic_highlight_white_off_32dp);
                    spEditor.putString(MainActivity.USEFLASH, "ON");
                    spEditor.commit();
                }
                return true;
            case R.id.autofocus:
                if(preferences.getString(MainActivity.AUTOFOCUS, "OFF").equals("ON")) {
                    item.setIcon(R.drawable.ic_center_focus_strong_white_32dp);
                    spEditor.putString(MainActivity.AUTOFOCUS, "OFF");
                    spEditor.commit();
                } else{
                    item.setIcon(R.drawable.ic_center_focus_weak_white_32dp);
                    spEditor.putString(MainActivity.AUTOFOCUS, "ON");
                    spEditor.commit();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setOnFocusChangeListener(@NonNull final EditText editText) {

        if(activeEdit == null) { activeEdit = new LinkedList<>(); }

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    startEditText((EditText)v);
                } else {
                    ((EditText)v).setCursorVisible(false);
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void startEditText(@NonNull final EditText editText) {
        editText.setCursorVisible(true);

        if(activeEdit.contains(editText)) {
            for(EditText id : activeEdit) {
                if(id.getId() == editText.getId()) {
                    EditText tmp = id;
                    activeEdit.remove(id);
                    activeEdit.addLast(tmp);
                    break;
                }
            }
        } else {
            activeEdit.addLast(editText);
            editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        String str = v.getText().toString();

                        if(str.length() != 0) {
                            for(EditText id : activeEdit) {
                                if(id.getId() == v.getId()) {
                                    activeEdit.remove(id);
                                    break;
                                }
                            }
                            if(activeEdit.isEmpty()){
                                v.clearFocus();
                                hideKeyboard(v);
                            } else {
                                activeEdit.peekLast().requestFocus();
                            }
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    public void hideKeyboard(@NonNull View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
