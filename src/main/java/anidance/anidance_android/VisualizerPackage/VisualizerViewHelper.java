package anidance.anidance_android.VisualizerPackage;

import android.graphics.Color;
import android.graphics.Paint;

public class VisualizerViewHelper {

    public static void setDefaultLineRender(VisualizerView view) {
        Paint linePaint = new Paint();
        linePaint.setStrokeWidth(1f);
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.argb(88, 0, 128, 255));
        Paint lineFlashPaint = new Paint();
        lineFlashPaint.setStrokeWidth(5f);
        lineFlashPaint.setAntiAlias(true);
        lineFlashPaint.setColor(Color.argb(188, 255, 255, 255));
        LineRenderer lineRenderer = new LineRenderer(linePaint, lineFlashPaint, true);
        view.addRenderer(lineRenderer);
    }
}
