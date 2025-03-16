package com.example.multiarmedbanditvisualizer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private Button btnYes, btnExplanation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnYes = findViewById(R.id.btn_yes);
        btnExplanation = findViewById(R.id.btn_explanation);

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, ConfigurationActivity.class);
                startActivity(intent);
            }
        });

        btnExplanation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, ExplanationActivity.class);
                startActivity(intent);
            }
        });
    }
}
