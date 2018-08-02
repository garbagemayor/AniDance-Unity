package anidance.anidance_android.table;

import android.content.Context;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

//import com.google.gson.*;

public class ReadGson {
    private String fileName = "ppp.json";
    private String TAG = "ReadGson";
    private Context context;
    private String dance_type;
    private Dict dict;
    private HashMap<String, HashMap<String, EdgesItem>> edges;
    private HashMap<String, MotionItem> motions;
    private HashMap<String, List<String>> startstep;
    //private Moves moves;
    private String status;
    public int beats;



    /*public String readToString(String fileName) {
        String encoding = "UTF-8";
        //File file = new File(fileName);
        File file = new File(AssetManager.open("test.json"));//("assets/" + "test.json");
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }
    }*/

    public ReadGson(String dance_type) {
        /*//String input = readToString(fileName);
        this.context = context;

        int ch;
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        AssetManager assetManager = context.getAssets();
        Reader in = null;
        try {
            in = new InputStreamReader(assetManager.open("dict.json"), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (; ; ) {
            int rsz = 0;
            try {
                rsz = in.read(buffer, 0, buffer.length);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (rsz < 0)
                break;
            out.append(buffer, 0, rsz);
        }
        String input = out.toString();
        Log.d(TAG, "input: " + input.length());
        Gson gson = new Gson();
        test t = gson.fromJson(input, test.class);
        Log.d(TAG, "dict size: " + t.getId());*/

        this.dict = new Dict();
        Edges e = new Edges();
        this.edges = e.edges.get(dance_type);
        Motions m = new Motions();
        this.motions = m.motions.get(dance_type);
        //this.moves = new Moves();
        this.dance_type = dance_type;
        this.status = "rest";
        this.beats = 0;
        this.startstep = new HashMap<>();
        startstep.put("C", Arrays.asList("C-0-1", "C-0-2", "C-0-3", "C-0-4", "C-1-1", "C-11-2", "C-25-1"));
        startstep.put("T", Arrays.asList("START"));
        startstep.put("W", Arrays.asList("W-0-2", "W-1-1", "W-2-1"));
        startstep.put("R", Arrays.asList("R-0-3", "R-0-4", "R-0-5", "R-0-6", "R-10-1", "R-11-1", "R-14-1", "R-22-3"));
    }

    public String choose_next(String step, List<Double> mfcc) {
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
                List<Double> mu = dict.dict.get(w).subList(0, 12);
                List<Double> sigma = dict.dict.get(w).subList(0, 12);
                if (mfcc == null) {
                    prob = prior;
                } else {
                    for (int i = 0; i < 13; ++i) {
                        prob += mu.get(i) - mfcc.get(i) * sigma.get(i);
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

    public String next(List<Double> mfcc) {
        String step = "";
        if (this.status.equals("rest")) {
            int random = (int) (Math.random() * startstep.get(this.dance_type).size());
            step = startstep.get(this.dance_type).get(random);
            this.status = "walking";
        } else if (this.status.equals("walking")) {
            step = choose_next(step, mfcc);

        } else if (this.status.equals("stopping")) {
            if (step.equals("END")) {
                this.status = "stopped";
            } else {
                step = motions.get(step).end_dir;
            }
        } else if (status.equals("stopped")) {
            step = "END";
        }
        if (step.equals("END")) {
            this.beats = 4;
        } else {
            this.beats = Integer.parseInt(motions.get(step).duration);
        }

        return step;


    }

    public List<String> getMoves(String step) {
        //return this.moves.moves.get(step);
        return Arrays.asList("");

    }

}
