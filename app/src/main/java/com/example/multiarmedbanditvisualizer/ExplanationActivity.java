package com.example.multiarmedbanditvisualizer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ExplanationActivity extends AppCompatActivity {

    private Button btnBackToHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explanation);

        btnBackToHome = findViewById(R.id.btn_back_to_home);

        btnBackToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExplanationActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
