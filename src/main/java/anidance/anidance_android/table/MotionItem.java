package anidance.anidance_android.table;

public class MotionItem {

    public String duration;
    public String end_dir;
    public String end_dis;
    public boolean is_start;
    public boolean is_terminal;

    public MotionItem(String duration, String end_dir, String end_dis, boolean is_start, boolean is_terminal) {
        this.duration = duration;
        this.end_dir = end_dir;
        this.end_dis = end_dis;
        this.is_start = is_start;
        this.is_terminal = is_terminal;
    }


}
