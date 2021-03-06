package anidance.anidance_android.table;

import java.util.HashMap;

public class Motions {
    public HashMap<String, HashMap<String, MotionItem>> motions;

    public Motions() {
        motions = new HashMap<String, HashMap<String, MotionItem>>();

        motions.put("C", new HashMap<String, MotionItem>());

        motions.get("C").put("C-0-1", new MotionItem("8", "C-1-3", "20", true, false));
        motions.get("C").put("C-0-2", new MotionItem("12", "C-1-3", "24", true, false));
        motions.get("C").put("C-0-3", new MotionItem("8", "C-1-3", "20", true, false));
        motions.get("C").put("C-0-4", new MotionItem("8", "C-1-3", "20", true, false));
        motions.get("C").put("C-0-5", new MotionItem("14", "END", "0", false, true));
        motions.get("C").put("C-0-6", new MotionItem("16", "END", "0", false, true));
        motions.get("C").put("C-1-1", new MotionItem("8", "C-14-1", "12", true, false));
        motions.get("C").put("C-1-2", new MotionItem("8", "C-1-1", "20", false, false));
        motions.get("C").put("C-1-3", new MotionItem("4", "C-15-1", "12", false, false));
        motions.get("C").put("C-10-1", new MotionItem("4", "C-16-1", "12", false, false));
        motions.get("C").put("C-10-2", new MotionItem("4", "C-14-1", "8", false, false));
        motions.get("C").put("C-10-3", new MotionItem("4", "C-14-1", "8", false, false));
        motions.get("C").put("C-11-1", new MotionItem("4", "C-10-2", "12", false, false));
        motions.get("C").put("C-11-2", new MotionItem("4", "C-10-1", "16", true, false));
        motions.get("C").put("C-11-3", new MotionItem("4", "C-10-3", "12", false, false));
        motions.get("C").put("C-12-1", new MotionItem("4", "C-14-1", "8", false, false));
        motions.get("C").put("C-13-1", new MotionItem("4", "C-11-2", "20", false, false));
        motions.get("C").put("C-14-1", new MotionItem("4", "C-0-6", "4", false, false));
        motions.get("C").put("C-15-1", new MotionItem("4", "C-14-1", "8", false, false));
        motions.get("C").put("C-16-1", new MotionItem("4", "C-14-1", "8", false, false));
        motions.get("C").put("C-17-1", new MotionItem("4", "C-0-6", "4", false, false));
        motions.get("C").put("C-18-1", new MotionItem("8", "C-15-1", "16", false, false));
        motions.get("C").put("C-19-1", new MotionItem("8", "C-18-1", "24", false, false));
        motions.get("C").put("C-2-1", new MotionItem("8", "C-14-1", "12", false, false));
        motions.get("C").put("C-2-2", new MotionItem("8", "C-14-1", "12", false, false));
        motions.get("C").put("C-20-1", new MotionItem("8", "C-14-1", "12", false, false));
        motions.get("C").put("C-21-1", new MotionItem("8", "C-15-1", "16", false, false));
        motions.get("C").put("C-22-1", new MotionItem("12", "C-0-6", "12", false, false));
        motions.get("C").put("C-23-1", new MotionItem("4", "C-17-1", "8", false, false));
        motions.get("C").put("C-24-1", new MotionItem("4", "C-27-1", "8", false, false));
        motions.get("C").put("C-25-1", new MotionItem("4", "C-26-1", "16", true, false));
        motions.get("C").put("C-26-1", new MotionItem("12", "C-0-6", "12", false, false));
        motions.get("C").put("C-27-1", new MotionItem("4", "C-0-5", "4", false, false));
        motions.get("C").put("C-3-1", new MotionItem("2", "C-4-1", "12", false, false));
        motions.get("C").put("C-4-1", new MotionItem("2", "C-24-1", "10", false, false));
        motions.get("C").put("C-5-1", new MotionItem("2", "C-6-1", "16", false, false));
        motions.get("C").put("C-6-1", new MotionItem("2", "C-1-1", "14", false, false));
        motions.get("C").put("C-7-1", new MotionItem("2", "C-8-1", "8", false, false));
        motions.get("C").put("C-8-1", new MotionItem("2", "C-17-1", "6", false, false));
        motions.get("C").put("C-9-1", new MotionItem("4", "C-2-2", "16", false, false));
        motions.get("C").put("HOLD", new MotionItem("4", "C-26-1", "16", false, false));

        motions.put("T", new HashMap<String, MotionItem>());

        motions.get("T").put("HOLD", new MotionItem("4", "END", "0", false, true));
        motions.get("T").put("START", new MotionItem("16", "T-1-1", "16", true, false));
        motions.get("T").put("T-1-1", new MotionItem("4", "END", "0", false, true));
        motions.get("T").put("T-1-2", new MotionItem("4", "T-2-2", "18", false, false));
        motions.get("T").put("T-1-3", new MotionItem("4", "HOLD", "4", false, false));
        motions.get("T").put("T-10-1", new MotionItem("4", "T-11-1", "14", false, false));
        motions.get("T").put("T-10-2", new MotionItem("4", "T-14-3", "14", false, false));
        motions.get("T").put("T-11-1", new MotionItem("2", "T-5-1", "10", false, false));
        motions.get("T").put("T-11-2", new MotionItem("4", "T-19-1", "10", false, false));
        motions.get("T").put("T-12-1", new MotionItem("8", "T-13-1", "22", false, false));
        motions.get("T").put("T-13-1", new MotionItem("6", "T-14-1", "14", false, false));
        motions.get("T").put("T-14-1", new MotionItem("4", "T-14-2", "8", false, false));
        motions.get("T").put("T-14-2", new MotionItem("4", "T-15-1", "4", false, false));
        motions.get("T").put("T-14-3", new MotionItem("2", "T-15-2", "10", false, false));
        motions.get("T").put("T-15-1", new MotionItem("4", "END", "0", false, true));
        motions.get("T").put("T-15-2", new MotionItem("2", "T-16-2", "8", false, false));
        motions.get("T").put("T-16-1", new MotionItem("6", "HOLD", "6", false, false));
        motions.get("T").put("T-16-2", new MotionItem("6", "HOLD", "6", false, false));
        motions.get("T").put("T-17-1", new MotionItem("4", "T-17-2", "16", false, false));
        motions.get("T").put("T-17-2", new MotionItem("4", "T-18-1", "12", false, false));
        motions.get("T").put("T-18-1", new MotionItem("4", "T-1-3", "8", false, false));
        motions.get("T").put("T-19-1", new MotionItem("6", "HOLD", "6", false, false));
        motions.get("T").put("T-2-1", new MotionItem("2", "HOLD", "2", false, false));
        motions.get("T").put("T-2-2", new MotionItem("2", "T-3-4", "14", false, false));
        motions.get("T").put("T-2-3", new MotionItem("2", "T-3-6", "8", false, false));
        motions.get("T").put("T-3-1", new MotionItem("6", "T-4-1", "6", false, false));
        motions.get("T").put("T-3-2", new MotionItem("6", "T-4-1", "6", false, false));
        motions.get("T").put("T-3-3", new MotionItem("6", "END", "0", false, true));
        motions.get("T").put("T-3-4", new MotionItem("6", "T-4-2", "12", false, false));
        motions.get("T").put("T-3-5", new MotionItem("6", "T-11-1", "16", false, false));
        motions.get("T").put("T-3-6", new MotionItem("6", "T-4-3", "6", false, false));
        motions.get("T").put("T-3-7", new MotionItem("6", "T-1-3", "10", false, false));
        motions.get("T").put("T-4-1", new MotionItem("6", "END", "0", false, true));
        motions.get("T").put("T-4-2", new MotionItem("6", "T-3-3", "6", false, false));
        motions.get("T").put("T-4-3", new MotionItem("6", "END", "0", false, true));
        motions.get("T").put("T-5-1", new MotionItem("2", "T-3-2", "8", false, false));
        motions.get("T").put("T-5-2", new MotionItem("2", "T-6-2", "34", false, false));
        motions.get("T").put("T-6-1", new MotionItem("4", "T-7-1", "32", false, false));
        motions.get("T").put("T-6-2", new MotionItem("4", "T-7-2", "32", false, false));
        motions.get("T").put("T-7-1", new MotionItem("4", "T-8-1", "28", false, false));
        motions.get("T").put("T-7-2", new MotionItem("4", "T-8-2", "28", false, false));
        motions.get("T").put("T-8-1", new MotionItem("6", "T-9-1", "24", false, false));
        motions.get("T").put("T-8-2", new MotionItem("6", "T-9-2", "24", false, false));
        motions.get("T").put("T-9-1", new MotionItem("4", "T-10-1", "18", false, false));
        motions.get("T").put("T-9-2", new MotionItem("4", "T-10-2", "18", false, false));

        motions.put("R", new HashMap<String, MotionItem>());

        motions.get("R").put("HOLD", new MotionItem("4", "R-22-1", "24", false, false));
        motions.get("R").put("R-0-3", new MotionItem("8", "R-19-1", "20", true, false));
        motions.get("R").put("R-0-4", new MotionItem("12", "R-19-1", "24", true, false));
        motions.get("R").put("R-0-5", new MotionItem("20", "R-19-1", "32", true, false));
        motions.get("R").put("R-0-6", new MotionItem("20", "R-19-1", "32", true, false));
        motions.get("R").put("R-0-7", new MotionItem("16", "END", "0", false, true));
        motions.get("R").put("R-1-1", new MotionItem("8", "R-14-1", "12", false, false));
        motions.get("R").put("R-1-2", new MotionItem("8", "R-1-1", "20", false, false));
        motions.get("R").put("R-10-1", new MotionItem("4", "R-1-1", "16", true, false));
        motions.get("R").put("R-11-1", new MotionItem("4", "R-1-1", "16", true, false));
        motions.get("R").put("R-12-1", new MotionItem("4", "R-0-7", "4", false, false));
        motions.get("R").put("R-13-1", new MotionItem("4", "R-12-1", "8", false, false));
        motions.get("R").put("R-13-2", new MotionItem("4", "R-12-1", "8", false, false));
        motions.get("R").put("R-14-1", new MotionItem("4", "R-0-7", "4", true, false));
        motions.get("R").put("R-14-2", new MotionItem("4", "R-0-7", "4", false, false));
        motions.get("R").put("R-15-1", new MotionItem("4", "R-0-7", "4", false, false));
        motions.get("R").put("R-16-1", new MotionItem("4", "R-13-1", "12", false, false));
        motions.get("R").put("R-16-2", new MotionItem("4", "R-15-1", "8", false, false));
        motions.get("R").put("R-17-1", new MotionItem("2", "R-18-2", "12", false, false));
        motions.get("R").put("R-18-2", new MotionItem("2", "R-16-2", "10", false, false));
        motions.get("R").put("R-19-1", new MotionItem("12", "R-0-7", "12", false, false));
        motions.get("R").put("R-19-2", new MotionItem("12", "R-0-7", "12", false, false));
        motions.get("R").put("R-2-1", new MotionItem("2", "R-3-1", "56", false, false));
        motions.get("R").put("R-2-2", new MotionItem("2", "R-3-2", "4", false, false));
        motions.get("R").put("R-20-1", new MotionItem("8", "R-21-1", "16", false, false));
        motions.get("R").put("R-21-1", new MotionItem("8", "R-0-7", "8", false, false));
        motions.get("R").put("R-22-1", new MotionItem("8", "R-19-1", "20", false, false));
        motions.get("R").put("R-22-2", new MotionItem("8", "R-21-1", "16", false, false));
        motions.get("R").put("R-22-3", new MotionItem("32", "R-22-1", "52", true, false));
        motions.get("R").put("R-22-4", new MotionItem("16", "R-22-2", "32", false, false));
        motions.get("R").put("R-23-1", new MotionItem("8", "R-0-7", "8", false, false));
        motions.get("R").put("R-23-2", new MotionItem("8", "R-16-1", "20", false, false));
        motions.get("R").put("R-24-1", new MotionItem("8", "R-23-1", "16", false, false));
        motions.get("R").put("R-3-1", new MotionItem("2", "R-22-3", "54", false, false));
        motions.get("R").put("R-3-2", new MotionItem("2", "R-0-7", "2", false, false));
        motions.get("R").put("R-3-3", new MotionItem("2", "R-22-4", "34", false, false));
        motions.get("R").put("R-4-1", new MotionItem("2", "R-5-1", "8", false, false));
        motions.get("R").put("R-5-1", new MotionItem("2", "R-14-1", "6", false, false));
        motions.get("R").put("R-6-1", new MotionItem("2", "R-5-1", "8", false, false));
        motions.get("R").put("R-7-1", new MotionItem("2", "HOLD", "26", false, false));
        motions.get("R").put("R-8-1", new MotionItem("2", "R-24-1", "18", false, false));
        motions.get("R").put("R-9-1", new MotionItem("4", "R-19-2", "16", false, false));

        motions.put("W", new HashMap<String, MotionItem>());

        motions.get("W").put("W-0-1", new MotionItem("12", "END", "0", false, true));
        motions.get("W").put("W-0-2", new MotionItem("12", "W-2-1", "15", true, false));
        motions.get("W").put("W-1-1", new MotionItem("6", "W-2-1", "9", true, false));
        motions.get("W").put("W-10-1", new MotionItem("6", "W-15-1", "9", false, false));
        motions.get("W").put("W-11-1", new MotionItem("3", "W-16-1", "9", false, false));
        motions.get("W").put("W-12-1", new MotionItem("3", "W-10-1", "12", false, false));
        motions.get("W").put("W-13-1", new MotionItem("3", "W-0-1", "3", false, false));
        motions.get("W").put("W-14-1", new MotionItem("3", "W-21-1", "3", false, false));
        motions.get("W").put("W-15-1", new MotionItem("3", "W-0-1", "3", false, false));
        motions.get("W").put("W-16-1", new MotionItem("3", "W-15-1", "6", false, false));
        motions.get("W").put("W-17-1", new MotionItem("3", "W-15-1", "6", false, false));
        motions.get("W").put("W-18-1", new MotionItem("3", "W-13-1", "6", false, false));
        motions.get("W").put("W-19-1", new MotionItem("6", "END", "0", false, true));
        motions.get("W").put("W-2-1", new MotionItem("3", "W-0-1", "3", true, false));
        motions.get("W").put("W-20-1", new MotionItem("3", "W-21-1", "3", false, false));
        motions.get("W").put("W-21-1", new MotionItem("3", "END", "0", false, true));
        motions.get("W").put("W-22-1", new MotionItem("3", "W-0-1", "3", false, false));
        motions.get("W").put("W-3-1", new MotionItem("3", "W-15-1", "6", false, false));
        motions.get("W").put("W-4-1", new MotionItem("3", "W-0-1", "3", false, false));
        motions.get("W").put("W-5-1", new MotionItem("3", "W-14-1", "6", false, false));
        motions.get("W").put("W-5-2", new MotionItem("3", "W-14-1", "6", false, false));
        motions.get("W").put("W-6-1", new MotionItem("3", "W-16-1", "9", false, false));
        motions.get("W").put("W-6-2", new MotionItem("3", "W-16-1", "9", false, false));
        motions.get("W").put("W-6-3", new MotionItem("3", "W-0-1", "3", false, false));
        motions.get("W").put("W-7-1", new MotionItem("3", "W-2-1", "6", false, false));
        motions.get("W").put("W-8-1", new MotionItem("3", "W-13-1", "6", false, false));
        motions.get("W").put("W-8-2", new MotionItem("3", "W-13-1", "6", false, false));
        motions.get("W").put("W-8-3", new MotionItem("3", "W-13-1", "6", false, false));
        motions.get("W").put("W-8-4", new MotionItem("3", "W-0-1", "3", false, false));
        motions.get("W").put("W-9-1", new MotionItem("3", "W-0-1", "3", false, false));
        motions.get("W").put("W-9-2", new MotionItem("3", "W-18-1", "9", false, false));
        motions.get("W").put("W-9-3", new MotionItem("3", "W-18-1", "9", false, false));


    }
}
