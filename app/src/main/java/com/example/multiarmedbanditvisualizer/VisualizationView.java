package com.example.multiarmedbanditvisualizer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * VisualizationView
 * -----------------
 * Zeigt das Koordinatensystem und den Graph der Q-Werte.
 *  - X-Achse: 0..algorithm.getLimit() (in ~10 Schritten)
 *    -> So wie wir es zuletzt hatten.
 *    Wenn du fest 0..30 haben willst, setze es wieder fest.
 *  - Y-Achse: 0..1
 *    * Gitter alle 0.1 => 0%,10%..100%,
 *      Beschriftung NUR bei 0%,20%,40%,60%,80%,100%
 *  - Achsen-Beschriftung X => "Iterations"
 *  - Y-Beschriftung => "Approximated Probabilities"
 *  - Die Bandit-Farben haben die Reihenfolge:
 *     A => Türkis (#40E0D0)
 *     B => Pink   (#FF69B4)
 *     C => Braun  (#8B4513)
 *     D => D-blau (#00008B)
 *     E => Gelb   (#FFFF00)
 *     F => Grau   (#808080)
 *     G => Grün   (#008000)
 *     H => Rot    (#FF0000)
 */
public class VisualizationView extends View {

    private EpsilonGreedy algorithm;
    private Paint axisPaint, textPaint, verticalTextPaint, gridPaint;

    private static final int PADDING = 100;

    public VisualizationView(Context context, AttributeSet attrs){
        super(context, attrs);

        axisPaint = new Paint();
        axisPaint.setColor(Color.BLACK);
        axisPaint.setStrokeWidth(3);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(25);

        verticalTextPaint = new Paint();
        verticalTextPaint.setColor(Color.BLACK);
        verticalTextPaint.setTextSize(25);

        gridPaint = new Paint();
        gridPaint.setColor(0xFFAAAAAA);
        gridPaint.setStrokeWidth(1);
    }

    public void setAlgorithm(EpsilonGreedy alg){
        this.algorithm = alg;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(algorithm == null) return;

        int w = getWidth();
        int h = getHeight();

        // Achsen
        canvas.drawLine(PADDING, h - PADDING, w - PADDING, h - PADDING, axisPaint);
        canvas.drawLine(PADDING, h - PADDING, PADDING, PADDING, axisPaint);

        // Y-Achse: Feingitter: 0,0.1..1 =>
        //   => Graue Linien + nur multiples of 0.2 => Beschriftung
        for(float p = 0f; p <= 1.0001f; p += 0.1f) {
            float y = (h - PADDING) - p*(h - 2f*PADDING);
            // Gitterlinie
            canvas.drawLine(PADDING, y, w - PADDING, y, gridPaint);

            int pct = Math.round(p*100);
            if(pct % 20 == 0){
                // 0%,20%,40%,60%,80%,100% => Label
                canvas.drawText(pct+"%", PADDING - 60, y, textPaint);
            }
        }

        // X-Achse dynamisch: 0..limit in ~10 Schritten
        int limit = algorithm.getLimit();
        if(limit < 1) limit = 1; // fallback

        // step => limit/10 => mind. 1
        int stepX = limit/10;
        if(stepX < 1) stepX=1;

        for(int xv = 0; xv <= limit; xv += stepX){
            float x= PADDING + (xv * (w - 2f*PADDING) / (float)limit);
            canvas.drawLine(x, PADDING, x, h - PADDING, gridPaint);
            canvas.drawText(String.valueOf(xv), x, h - PADDING + 30, textPaint);
        }

        // X-Achsen-Beschriftung
        canvas.drawText("Iterations", (w/2f)-50, h-PADDING+60, textPaint);

        // Y-Achsen-Beschriftung
        canvas.save();
        float centerY= (h/2f);
        canvas.rotate(-90, (PADDING-70), centerY+140);
        canvas.drawText("Approximated Probabilities", (PADDING-70), centerY+140, verticalTextPaint);
        canvas.restore();

        // Graphen-Daten
        float[][] data= algorithm.getGraphData();
        if(data==null) return;

        // Gewünschte Reihenfolge:
        //  A => Türkis (#40E0D0)
        //  B => Pink   (#FF69B4)
        //  C => Braun  (#8B4513)
        //  D => D-blau (#00008B)
        //  E => Gelb   (#FFFF00)
        //  F => Grau   (#808080)
        //  G => Grün   (#008000)
        //  H => Rot    (#FF0000)
        int[] defaultColors= {
                0xFF40E0D0, // turquoise
                0xFFFF69B4, // pink
                0xFF8B4513, // brown
                0xFF00008B, // darkblue
                0xFFFFFF00, // yellow
                0xFF808080, // gray
                0xFF008000, // green
                0xFFFF0000  // red
        };

        // Zeichnen
        for(int arm=0; arm<data.length; arm++){
            float[] arr= data[arm];
            if(arr == null || arr.length < 2) continue;

            // => i=1..arr.length-1 => wir verbinden arr[i-1]..arr[i]
            for(int i=1; i< arr.length; i++){
                float x1 = PADDING + (i-1)*( (w - 2f*PADDING)/(float)limit );
                float y1 = (h - PADDING) - arr[i-1]*(h - 2f*PADDING);

                float x2 = PADDING + i*( (w - 2f*PADDING)/(float)limit );
                float y2 = (h - PADDING) - arr[i]*(h - 2f*PADDING);

                Paint paint= new Paint();
                // => mod, falls user >8 Banditen
                paint.setColor(defaultColors[ arm % defaultColors.length ]);
                paint.setStrokeWidth(5);

                canvas.drawLine(x1,y1, x2,y2, paint);
            }
        }
    }
}
