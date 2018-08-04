package anidance.anidance_android.table;

public class Vector3d {

    public double x;
    public double y;
    public double z;

    public Vector3d() {
        x = 0;
        y = 0;
        z = 0;
    }

    public Vector3d(double x_, double y_, double z_) {
        x = x_;
        y = y_;
        z = z_;
    }

    @Override
    public String toString() {
        return String.format("{\"x\": %.14f, \"y\": %.14f, \"z\": %.14f}", x, y, z);
    }
}
