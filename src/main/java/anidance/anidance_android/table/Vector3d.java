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

    public Vector3d(Vector3d v) {
        x = v.x;
        y = v.y;
        z = v.z;
    }

    @Override
    public String toString() {
        return String.format("{\"x\": %.14f, \"y\": %.14f, \"z\": %.14f}", x, y, z);
    }

    public void addToStringBuilder(StringBuilder builder) {
        //return String.format("{\"x\": %.14f, \"y\": %.14f, \"z\": %.14f}", x, y, z);
        builder.append("{\"x\": ");
        builder.append(x);
        builder.append(", \"y\": ");
        builder.append(y);
        builder.append(", \"z\": ");
        builder.append(z);
        builder.append("}");
    }

    public int toByteBuffer(byte[] buffer, int offset) {
        int pos = offset;
        buffer[pos ++] = '{';
        buffer[pos ++] = '"';
        buffer[pos ++] = 'x';
        buffer[pos ++] = '"';
        buffer[pos ++] = ':';
        buffer[pos ++] = ' ';
        pos += doubleToByteBuffer(x, buffer, pos);
        buffer[pos ++] = ',';
        buffer[pos ++] = ' ';
        buffer[pos ++] = '"';
        buffer[pos ++] = 'y';
        buffer[pos ++] = '"';
        buffer[pos ++] = ':';
        buffer[pos ++] = ' ';
        pos += doubleToByteBuffer(y, buffer, pos);
        buffer[pos ++] = ',';
        buffer[pos ++] = ' ';
        buffer[pos ++] = '"';
        buffer[pos ++] = 'z';
        buffer[pos ++] = '"';
        buffer[pos ++] = ':';
        buffer[pos ++] = ' ';
        pos += doubleToByteBuffer(z, buffer, pos);
        buffer[pos ++] = '}';
        return pos - offset;
    }

    private int doubleToByteBuffer(double v, byte[] buffer, int offset) {
        String str = String.valueOf(v);
        int pos = offset;
        for (int i = 0; i < str.length(); i ++) {
            buffer[pos ++] = (byte) str.charAt(i);
        }
        return pos - offset;
    }
}
