package com.example.multiarmedbanditvisualizer;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;

public class PostVisualizationActivity extends AppCompatActivity {

    private Button btnGoHome, btnGenerateAlgorithm, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_visualization);

        // (1) "Go Home"-Button
        btnGoHome           = findViewById(R.id.btn_go_home);
        // (2) "Generate New Algorithm"-Button
        btnGenerateAlgorithm= findViewById(R.id.btn_generate_algorithm);
        // (3) Neuer "Log out"-Button
        btnLogout           = findViewById(R.id.btn_logout);

        // (1) "Go Home" -> z.B. HomeActivity
        btnGoHome.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent= new Intent(PostVisualizationActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // (2) "Generate New Algorithm" -> ConfigurationActivity
        btnGenerateAlgorithm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent= new Intent(PostVisualizationActivity.this, ConfigurationActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // (3) "Log out" -> z.B. WelcomeActivity
        btnLogout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                // Hier ggf. richtigen Logout durchführen (FirebaseAuth.signOut, etc.)
                // Dann zum Welcome-Bildschirm
                Intent intent= new Intent(PostVisualizationActivity.this, WelcomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
