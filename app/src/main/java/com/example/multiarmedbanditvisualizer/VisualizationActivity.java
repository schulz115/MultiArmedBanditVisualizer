package com.example.multiarmedbanditvisualizer;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TableRow;
import android.view.View;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.graphics.Typeface;

import java.util.List;

public class VisualizationActivity extends AppCompatActivity {

    private EpsilonGreedy algorithm;
    private VisualizationView visualizationView;

    private HorizontalScrollView horizontalScroll;
    private TableLayout tableLayout;
    private TextView labelInfo, labelInfo2, labelEpsilon;
    private Button toggleBtn, niceBtn;

    private Handler handler= new Handler();
    private boolean showingTable= false;

    // Intent-Parameter
    private int numBandits;
    private double epsilon;
    private int limit;
    private int intervalMs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualization);

        // 1) Intent
        numBandits= getIntent().getIntExtra("numBandits",3);
        epsilon   = getIntent().getDoubleExtra("epsilon",0.5);
        limit     = getIntent().getIntExtra("limit",30);
        intervalMs= getIntent().getIntExtra("intervalMs",10);

        // 2) Algo
        algorithm= new EpsilonGreedy(numBandits, epsilon, limit);

        // 3) Views
        visualizationView= findViewById(R.id.visualizationView);
        horizontalScroll= findViewById(R.id.horizontalScroll);
        tableLayout= findViewById(R.id.qTable);

        labelInfo= findViewById(R.id.iterationInfo);
        labelInfo2= findViewById(R.id.iterationInfo2);
        labelEpsilon= findViewById(R.id.epsilonLabel);
        toggleBtn= findViewById(R.id.toggleViewButton);
        niceBtn= findViewById(R.id.niceButton);

        niceBtn.setVisibility(View.GONE);
        labelInfo2.setVisibility(View.GONE);

        toggleBtn.setOnClickListener(v-> toggleView());
        niceBtn.setOnClickListener(v-> {
            Intent intent= new Intent(this, PostVisualizationActivity.class);
            startActivity(intent);
            finish();
        });

        // 4) Info oben => "Bandits: X    ε: Y    interval: Z ms"
        labelEpsilon.setText(
                String.format("Bandits: %d    ε: %.2f    interval: %d ms",
                        numBandits, epsilon, intervalMs)
        );

        startLoop();
    }

    private void startLoop(){
        handler.postDelayed(new Runnable(){
            @Override
            public void run(){
                if(!algorithm.canProceed()){
                    int total= algorithm.getTotalActionsDone();
                    // (3) "Finished! Total actions: W"
                    labelInfo.setText("Finished! Total actions: " + total);

                    // (2) => optimal = anzahl aktionen, die den "besten" banditen gewählt haben
                    int optCount= algorithm.getOptimalActionCount();
                    float rate= (total>0)? (optCount/(float)total) : 0f;
                    labelInfo2.setVisibility(View.VISIBLE);
                    labelInfo2.setText(
                            String.format("%% of optimal actions: %.1f%%", rate*100)
                    );

                    niceBtn.setVisibility(View.VISIBLE);
                    updateUI();
                    return;
                }
                boolean ok= algorithm.doTimeStep();
                if(!ok){
                    int total= algorithm.getTotalActionsDone();
                    labelInfo.setText("Finished! Total actions: " + total);

                    int optCount= algorithm.getOptimalActionCount();
                    float rate= (total>0)? (optCount/(float)total) : 0f;
                    labelInfo2.setVisibility(View.VISIBLE);
                    labelInfo2.setText(
                            String.format("%% of optimal actions: %.1f%%", rate*100)
                    );

                    niceBtn.setVisibility(View.VISIBLE);
                    updateUI();
                    return;
                }

                labelInfo.setText("Actions so far: "+ algorithm.getTotalActionsDone());
                labelInfo2.setVisibility(View.GONE);

                updateUI();
                handler.postDelayed(this, intervalMs);
            }
        }, intervalMs);
    }

    private void updateUI(){
        visualizationView.setAlgorithm(algorithm);
        visualizationView.invalidate();
        buildTable();
    }

    private void buildTable(){
        tableLayout.removeAllViews();
        tableLayout.setPadding(2,2,2,2);

        // Kopfzeile
        TableRow header= new TableRow(this);
        header.setBackgroundColor(0xFFC0C0C0);

        // "Time Step"
        TextView col0= makeCell("Time Step", Color.BLACK, Color.WHITE);
        header.addView(col0);

        // 6) Neue Reihenfolge => #1 Türkis, #2 Pink, #3 Braun, #4 Dunkelblau,
        //   #5 Gelb, #6 Grau, #7 Grün, #8 Rot
        int[] colorCandidates= {
                0xFF40E0D0, // TURQUOISE
                0xFFFF69B4, // PINK
                0xFF8B4513, // BROWN
                0xFF00008B, // DARKBLUE
                0xFFFFFF00, // YELLOW
                0xFF808080, // GRAY
                0xFF008000, // GREEN
                0xFFFF0000  // RED
        };

        int[] banditBg= new int[numBandits];
        for(int i=0; i<numBandits; i++){
            banditBg[i]= lightenColor(colorCandidates[i]);
        }

        // (5) => "Bandit A".."Bandit H"
        // => 'A'+ i => char c
        for(int i=0; i< numBandits; i++){
            char c= (char)('A' + i);
            String colName= "Bandit "+ c;
            TextView tv= makeCell(colName, Color.BLACK, banditBg[i]);
            header.addView(tv);
        }
        tableLayout.addView(header);

        // Logs
        List<EpsilonGreedy.LogRow> logs= algorithm.getLogRows();
        for(EpsilonGreedy.LogRow row: logs){
            TableRow tr= new TableRow(this);

            boolean isFooter= row.firstCol.startsWith("Actual probabilities");

            // Left col => evtl. normal
            TextView first= makeCell(row.firstCol, Color.BLACK, 0xFFFFFFFF);
            if(isFooter){
                // (1) => bold
                first.setTypeface(null, Typeface.BOLD);
            }
            tr.addView(first);

            for(int a=0; a< numBandits; a++){
                String text= row.values[a];
                String colorCode= row.colors[a];

                if(colorCode.equals("GREEN")){
                    TextView cell= makeCell(text, Color.WHITE, 0xFF00CC00);
                    tr.addView(cell);
                } else if(colorCode.equals("RED")){
                    TextView cell= makeCell(text, Color.WHITE, 0xFFFF4444);
                    tr.addView(cell);
                } else {
                    int bg= banditBg[a];
                    int txtCol= getContrastTextColor(bg);
                    TextView cell= makeCell(text, txtCol, bg);

                    if(isFooter){
                        // => fett
                        cell.setTypeface(null, Typeface.BOLD);
                    }

                    tr.addView(cell);
                }
            }
            tableLayout.addView(tr);
        }
    }

    private TextView makeCell(String txt, int txtColor, int bgColor){
        TextView tv= new TextView(this);
        tv.setText(txt);
        tv.setTextColor(txtColor);
        tv.setBackgroundColor(bgColor);
        tv.setPadding(12,8,12,8);
        tv.setGravity(Gravity.CENTER);
        return tv;
    }

    private int lightenColor(int baseColor){
        int r= (baseColor>>16)&0xFF;
        int g= (baseColor>>8 )&0xFF;
        int b= (baseColor    )&0xFF;
        r= (r+255)/2;
        g= (g+255)/2;
        b= (b+255)/2;
        return 0xFF000000 | (r<<16)|(g<<8)|b;
    }

    private int getContrastTextColor(int bg){
        int r= (bg>>16)&0xFF;
        int g= (bg>>8 )&0xFF;
        int b= (bg    )&0xFF;
        double brightness= 0.299*r + 0.587*g + 0.114*b;
        if(brightness<128){
            return Color.WHITE;
        } else {
            return Color.BLACK;
        }
    }

    private void toggleView(){
        if(showingTable){
            horizontalScroll.setVisibility(View.GONE);
            visualizationView.setVisibility(View.VISIBLE);
            toggleBtn.setText("View Value Table / Log");
        } else {
            horizontalScroll.setVisibility(View.VISIBLE);
            visualizationView.setVisibility(View.GONE);
            toggleBtn.setText("View Visualization");
        }
        showingTable= !showingTable;
    }
}
