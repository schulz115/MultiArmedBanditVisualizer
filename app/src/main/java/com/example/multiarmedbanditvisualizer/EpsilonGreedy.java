package com.example.multiarmedbanditvisualizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EpsilonGreedy {

    private int numArms;
    private double epsilon;
    private int limit;

    private double[] trueRates;
    private int[] armCounts;     // wie oft jeder Arm gewählt wurde
    private int[] armSuccesses;  // wie oft jeder Arm Erfolg hatte

    private boolean experimentFinished=false;
    private boolean finalRowAppended=false;

    private Random random= new Random();

    public static class LogRow {
        public String firstCol;
        public String[] values;
        public String[] colors;
        public LogRow(int arms){
            values= new String[arms];
            colors= new String[arms];
        }
    }
    private List<LogRow> logRows= new ArrayList<>();
    private int lastChosenArm= -1;

    public EpsilonGreedy(int numArms, double epsilon, int limit){
        this.numArms= numArms;
        this.epsilon= epsilon;
        this.limit  = limit;

        trueRates= new double[numArms];
        armCounts= new int[numArms];
        armSuccesses= new int[numArms];

        for(int i=0; i<numArms; i++){
            double r= 0.1 + 0.8* random.nextDouble();
            trueRates[i]= r;
        }
        doInitialization();
    }

    private void doInitialization(){
        int initRound=1;
        while(true){
            for(int arm=0; arm<numArms; arm++){
                if(armCounts[arm]>=limit){
                    experimentFinished=true;
                    appendFinalProbRow();
                    return;
                }
            }
            LogRow row= new LogRow(numArms);
            row.firstCol= "Initialization "+ initRound;
            boolean anySuccess=false;

            for(int arm=0; arm<numArms; arm++){
                boolean success= (random.nextDouble()< trueRates[arm]);
                armCounts[arm]++;
                if(success){
                    armSuccesses[arm]++;
                    anySuccess=true;
                }
                row.values[arm]= formatValue(arm);
                row.colors[arm]= success?"GREEN":"RED";

                if(armCounts[arm]>=limit){
                    logRows.add(row);
                    experimentFinished=true;
                    appendFinalProbRow();
                    return;
                }
            }
            logRows.add(row);

            if(anySuccess) {
                break;
            } else {
                initRound++;
            }
        }
    }

    public boolean doTimeStep(){
        if(experimentFinished) return false;

        for(int arm=0; arm<numArms; arm++){
            if(armCounts[arm]>=limit){
                experimentFinished=true;
                appendFinalProbRow();
                return false;
            }
        }

        double r= random.nextDouble();
        boolean explore= (r<= epsilon);
        int chosen= (explore)? random.nextInt(numArms): bestArm();

        lastChosenArm= chosen;
        boolean success= (random.nextDouble()< trueRates[chosen]);
        armCounts[chosen]++;
        if(success) armSuccesses[chosen]++;

        int stepIndex= getTotalSteps()+1;
        String firstC= String.format("%d) %.2f %s ε", stepIndex, r, (explore?"≤":">"));

        LogRow row= new LogRow(numArms);
        row.firstCol= firstC;
        for(int a=0; a<numArms; a++){
            row.values[a]= formatValue(a);
            if(a== chosen){
                row.colors[a]= success?"GREEN":"RED";
            } else {
                row.colors[a]= "BLACK";
            }
        }
        logRows.add(row);

        if(armCounts[chosen]>=limit){
            experimentFinished= true;
            appendFinalProbRow();
            return false;
        }
        return true;
    }

    private int bestArm(){
        double best=-1;
        List<Integer> ties= new ArrayList<>();
        for(int arm=0; arm<numArms; arm++){
            double ratio= (armCounts[arm]==0)?0:
                    (double)armSuccesses[arm]/armCounts[arm];
            if(ratio>best){
                best= ratio;
                ties.clear();
                ties.add(arm);
            } else if(ratio==best){
                ties.add(arm);
            }
        }
        return ties.get(random.nextInt(ties.size()));
    }

    /** "X/Y = Z%" mit Leerzeichen um '='. */
    private String formatValue(int arm){
        double ratio= (armCounts[arm]==0)?0:
                (double)armSuccesses[arm]/armCounts[arm];
        return String.format("%d/%d = %.0f%%",
                armSuccesses[arm], armCounts[arm], ratio*100);
    }

    public boolean canProceed(){
        return !experimentFinished;
    }

    public int getTotalActionsDone(){
        int s=0;
        for(int c:armCounts) s+= c;
        return s;
    }

    public int getTotalSuccesses(){
        int sum=0;
        for(int s: armSuccesses){
            sum+= s;
        }
        return sum;
    }

    /**
     * (2) Optimal actions => wir prüfen, welche Bandits
     * die höchste trueRate haben => sum(armCounts[..])
     */
    public int getOptimalActionCount(){
        // 1) Finde highest rate
        double best= -1;
        for(double r: trueRates){
            if(r>best) best= r;
        }
        // 2) Summe counts von allen Armen mit rate=best
        int sum=0;
        for(int i=0; i<numArms; i++){
            if(Math.abs(trueRates[i]- best)<1e-9){
                sum+= armCounts[i];
            }
        }
        return sum;
    }

    public int getTotalSteps(){
        int stepCount=0;
        for(LogRow row: logRows){
            if(row.firstCol.startsWith("Initialization") ||
                    row.firstCol.startsWith("Actual")){
                // skip
            } else {
                stepCount++;
            }
        }
        return stepCount;
    }

    public int getLastChosenArm(){
        return lastChosenArm;
    }

    public List<LogRow> getLogRows(){
        return logRows;
    }

    public int getNumArms(){
        return numArms;
    }

    public int getLimit(){
        return limit;
    }

    private void appendFinalProbRow(){
        if(finalRowAppended) return;
        finalRowAppended= true;

        LogRow row= new LogRow(numArms);
        row.firstCol= "Actual probabilities:";
        for(int i=0; i<numArms; i++){
            double p= trueRates[i]*100;
            row.values[i]= String.format("%.0f%%", p);
            row.colors[i]="BLACK";
        }
        logRows.add(row);
    }

    public float[][] getGraphData(){
        List<Float>[] lists= new ArrayList[numArms];
        for(int i=0; i<numArms; i++){
            lists[i]= new ArrayList<>();
        }

        int betCount=0;
        for(LogRow row: logRows){
            if(row.firstCol.startsWith("Actual")) continue;
            for(int arm=0; arm<numArms; arm++){
                if(!row.colors[arm].equals("BLACK")){
                    float val= parseRate(row.values[arm]);
                    lists[arm].add(val);
                }
            }
            betCount++;
        }

        float[][] data= new float[numArms][];
        for(int i=0; i<numArms; i++){
            data[i]= new float[ lists[i].size() ];
            for(int k=0; k<lists[i].size(); k++){
                data[i][k]= lists[i].get(k);
            }
        }
        return data;
    }

    private float parseRate(String txt){
        int eq= txt.indexOf('=');
        int pc= txt.indexOf('%');
        if(eq<0||pc<0) return 0f;
        String sub= txt.substring(eq+1, pc).trim();
        try{
            return Float.parseFloat(sub)/100f;
        } catch(Exception e){
            return 0f;
        }
    }
}
