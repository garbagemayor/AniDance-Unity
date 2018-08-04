package anidance.anidance_android.table;

public class MovesDoubleFrame {

    public Vector3d center;
    public Vector3d[] skeletons;

    public MovesDoubleFrame(Vector3d center, Vector3d[] skeletons) {
        this.center = center;
        this.skeletons = skeletons;
    }

    @Override
    public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("{");
            builder.append("\"center\": ");
            builder.append(center.toString());
            builder.append(", ");
            builder.append("\"skeletons\": ");
            builder.append("[");
            for (int i = 0; i < skeletons.length; i ++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(skeletons[i].toString());
            }
            builder.append("]");
            builder.append("}");
            return builder.toString();

    }
}
