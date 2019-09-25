package com.baidu.rsaencrydemo;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.baidu.rsaencrydemo.constant.NetConstant;


public class MainActivity extends AppCompatActivity {

    EditText editText1;
    EditText editText2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText1 = findViewById(R.id.editText1);
        editText2 = findViewById(R.id.editText2);

        editText1.setText(NetConstant.RES_PRIVATE_KEY);
        editText2.setText(NetConstant.RES_PUBLIC_KEY);
    }

    public void onClick(View view) {
        Intent intent = new Intent();
        ComponentName component = new ComponentName(getApplicationContext(), TestActivity.class);
        intent.setComponent(component);
        startActivity(intent);
    }
}
