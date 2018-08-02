package anidance.anidance_android.table;

import java.util.List;

public class DictItem {
    private String id;
    private List<Double> list;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setList(List<Double> list) {
        this.list = list;
    }

    public List<Double> getList() {
        return list;
    }
}
