package anidance.anidance_android;

public abstract class BaseController {

    protected VisualizerViewCallBack mVisualizerViewCallBack;
    protected OnControllerStartStopListener mOnControllerStartStopListener;

    public final void setVisualizerViewCallBack(VisualizerViewCallBack callBack) {
        mVisualizerViewCallBack = callBack;
    }

    public final void setOnControllerStartStopListener(OnControllerStartStopListener listener) {
        mOnControllerStartStopListener = listener;
    }

    public abstract void start();

    public abstract void stop();
}
