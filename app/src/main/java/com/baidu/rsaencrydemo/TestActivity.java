package com.baidu.rsaencrydemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.rsaencrydemo.constant.NetConstant;
import com.baidu.rsaencrydemo.encryptUtil.MoToolingEncryptClient;

public class TestActivity extends AppCompatActivity {

    private EditText textViewData;
    private TextView textViewCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        textViewData = findViewById(R.id.date);
        textViewCode = findViewById(R.id.codeData);
    }

    public void encodeClick(View view) {
        String s = textViewData.getText().toString().trim();
        if (TextUtils.isEmpty(s)) {
            Toast.makeText(getApplicationContext(), "加密内容不能为空", Toast.LENGTH_LONG).show();
        } else {
            String s1 = MoToolingEncryptClient.encryptionResult(s, NetConstant.RES_PUBLIC_KEY);
            if (s1 != null) {
                textViewCode.setText(s1);
            }
        }
    }

    public void decodeClick(View view) {
        String s = textViewData.getText().toString().trim();
        if (TextUtils.isEmpty(s)) {
            Toast.makeText(getApplicationContext(), "解密内容不能为空", Toast.LENGTH_LONG).show();
        } else {
            try {
                String s1 = MoToolingEncryptClient.decryptionData(s);
                if (s1 != null) {
                    textViewCode.setText(s1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void copyClick(View view) {
        textViewData.setText(textViewCode.getText().toString());
        textViewCode.setText("");
    }
}
