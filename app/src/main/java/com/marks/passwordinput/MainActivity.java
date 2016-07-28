package com.marks.passwordinput;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import com.marks.inputlibrary.AliPasswordEditText;

public class MainActivity extends AppCompatActivity {
    private AliPasswordEditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        input = (AliPasswordEditText) findViewById(R.id.input);
        input.setInputCallBack(new AliPasswordEditText.InputCallBack() {
            @Override
            public void onInputFinish(String result) {
                Toast.makeText(MainActivity.this, "result=" + result, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
