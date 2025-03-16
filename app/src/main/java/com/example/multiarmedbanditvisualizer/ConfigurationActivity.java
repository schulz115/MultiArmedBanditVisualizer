package com.example.multiarmedbanditvisualizer;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Button;
import android.content.Intent;
import android.view.View;

public class ConfigurationActivity extends AppCompatActivity {

    private TextView labelBandits, labelEpsilon, labelLimit, labelInterval;
    private SeekBar seekBarBandits, seekBarEpsilon, seekBarLimit, seekBarInterval;
    private Button buttonStart;

    private int bandits   = 4;     // default
    private double epsilon = 0.1;
    private int limit      = 100;
    private int intervalMs = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        // Labels
        labelBandits   = findViewById(R.id.labelBandits);
        labelEpsilon   = findViewById(R.id.labelEpsilon);
        labelLimit     = findViewById(R.id.labelLimit);
        labelInterval  = findViewById(R.id.labelInterval);

        // Sliders
        seekBarBandits   = findViewById(R.id.seekBarBandits);
        seekBarEpsilon   = findViewById(R.id.seekBarEpsilon);
        seekBarLimit     = findViewById(R.id.seekBarLimit);
        seekBarInterval  = findViewById(R.id.seekBarInterval);

        buttonStart = findViewById(R.id.buttonStartVisualization);

        // Max etc.
        seekBarBandits.setMax(5);   // 3..8
        seekBarEpsilon.setMax(98);  // 0.01..0.99
        seekBarLimit.setMax(18);    // 20..200
        seekBarInterval.setMax(39); // 1..40

        // Default slider positions
        // (1) Bandits=4 => => progress=1
        seekBarBandits.setProgress(1);
        updateBandits(1);

        // (2) Epsilon=0.1 => => raw=10 => progress=9
        seekBarEpsilon.setProgress(9);
        updateEpsilon(9);

        // (3) limit=100 => => 20+10*8 => progress=8
        seekBarLimit.setProgress(8);
        updateLimit(8);

        // (4) interval=10 => => 1+9 => progress=9
        seekBarInterval.setProgress(9);
        updateInterval(9);

        // Listener
        seekBarBandits.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser){
                updateBandits(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar sb){}
            @Override public void onStopTrackingTouch(SeekBar sb){}
        });

        seekBarEpsilon.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser){
                updateEpsilon(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar sb){}
            @Override public void onStopTrackingTouch(SeekBar sb){}
        });

        seekBarLimit.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser){
                updateLimit(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar sb){}
            @Override public void onStopTrackingTouch(SeekBar sb){}
        });

        seekBarInterval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser){
                updateInterval(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar sb){}
            @Override public void onStopTrackingTouch(SeekBar sb){}
        });

        buttonStart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent= new Intent(ConfigurationActivity.this, VisualizationActivity.class);
                intent.putExtra("numBandits",   bandits);
                intent.putExtra("epsilon",      epsilon);
                intent.putExtra("limit",        limit);
                intent.putExtra("intervalMs",   intervalMs);
                startActivity(intent);
            }
        });
    }

    private void updateBandits(int progress){
        // 0..5 => => 3..8
        bandits= 3 + progress;
        labelBandits.setText( String.format("Set number of bandits (3 - 8): %d", bandits) );
    }

    private void updateEpsilon(int progress){
        // 0..98 => => 1..99 => => /100 => 0.01..0.99
        int raw= progress+1;
        epsilon= raw/100.0;
        labelEpsilon.setText(
                String.format("Set epsilon (0.01 - 0.99): %.2f", epsilon)
        );
    }

    private void updateLimit(int progress){
        // 0..18 => => 20 + 10*progress => => 20..200
        limit= 20 + 10*progress;
        labelLimit.setText(
                String.format("Set iterations limit (20 - 200): %d", limit)
        );
    }

    private void updateInterval(int progress){
        // 0..39 => => 1..40
        intervalMs= 1 + progress;
        labelInterval.setText(
                String.format("Set iteration interval (1 ms - 40 ms): %d ms", intervalMs)
        );
    }
}
