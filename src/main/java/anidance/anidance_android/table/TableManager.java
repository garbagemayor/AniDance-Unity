package anidance.anidance_android.table;

import android.content.Context;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TableManager {
    private String fileName = "ppp.json";
    private String TAG = "TableManager";
    private Context context;
    private String dance_type;
    private Dict dict;
    private HashMap<String, HashMap<String, EdgesItem>> edges;
    private HashMap<String, MotionItem> motions;
    private HashMap<String, List<String>> startstep;
    private Moves moves;
    private String status;
    private int beats;
    private String lastStep;

    public TableManager(String dance_type) {
        this.dict = new Dict();
        Edges e = new Edges();
        this.edges = e.edges.get(dance_type);
        Motions m = new Motions();
        this.motions = m.motions.get(dance_type);
        this.moves = new Moves();
        this.dance_type = dance_type;
        this.status = "rest";
        this.beats = 0;
        this.startstep = new HashMap<>();
        startstep.put("C", Arrays.asList("C-0-1", "C-0-2", "C-0-3", "C-0-4", "C-1-1", "C-11-2", "C-25-1"));
        startstep.put("T", Arrays.asList("START"));
        startstep.put("W", Arrays.asList("W-0-2", "W-1-1", "W-2-1"));
        startstep.put("R", Arrays.asList("R-0-3", "R-0-4", "R-0-5", "R-0-6", "R-10-1", "R-11-1", "R-14-1", "R-22-3"));
        lastStep = null;
    }

    private String choose_next(String step, List<Double> mfcc) {
        HashMap<String, EdgesItem> tmp = edges.get(step);
        if (mfcc.size() < 13) {
            return null;
        }
        String bad = "";
        String s = "";
        double max = 0;
        for (String w : tmp.keySet()) {
            if (w.equals("HOLD") || motions.get(w).is_terminal) {
                bad = w;
            } else {
                double prob = 0;
                double prior = tmp.get(w).probability;
                List<Double> mu = dict.dict.get(w).subList(0, 13);
                List<Double> sigma = dict.dict.get(w).subList(13, 26);
                if (mfcc == null) {
                    prob = prior;
                } else {
                    for (int i = 0; i < 13; ++i) {
                        prob = Math.max(prob, (mu.get(i) - mfcc.get(i)) * sigma.get(i));
                    }
                    if (max < prob) {
                        s = w;
                        max = prob;
                    }
                }
            }
        }
        if (max == 0) {
            return bad;
        } else {
            return s;
        }
    }

    public void reset() {
        status = "rest";
        lastStep = null;
    }

    public String next(List<Double> mfcc) {
        String step = "";
        if (this.status.equals("rest")) {
            int random = (int) (Math.random() * startstep.get(this.dance_type).size());
            step = startstep.get(this.dance_type).get(random);
            this.status = "walking";
        } else if (this.status.equals("walking")) {
            step = choose_next(lastStep, mfcc);
        } else if (this.status.equals("stopping")) {
            if (step.equals("END")) {
                this.status = "stopped";
            } else {
                step = motions.get(step).end_dir;
            }
        } else if (status.equals("stopped")) {
            step = "END";
        }
        lastStep = step;
        return step;
    }

    public int getStepBeats(String step) {
        if (step.equals("END")) {
            this.beats = 4;
        } else {
            try {
                this.beats = Integer.parseInt(motions.get(step).duration);
            } catch (NullPointerException e) {
                this.beats = 4;
            }
        }
        return this.beats;
    }

    public List<String> getMoves(String step) {
        return this.moves.getMoves(step);
        //return Arrays.asList("");
    }
}
