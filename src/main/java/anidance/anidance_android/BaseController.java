package anidance.anidance_android;

public abstract class BaseController {

    protected boolean runningFlag;

    protected VisualizerViewCallBack mVisualizerViewCallBack;
    protected OnControllerStartStopListener mOnControllerStartStopListener;

    protected BaseController() {
        runningFlag = false;
    }

    public final void setVisualizerViewCallBack(VisualizerViewCallBack callBack) {
        mVisualizerViewCallBack = callBack;
    }

    public final void setOnControllerStartStopListener(OnControllerStartStopListener listener) {
        mOnControllerStartStopListener = listener;
    }

    public void start() {
        runningFlag = true;
    }

    public void stop() {
        runningFlag = false;
    }

    public boolean isRunning() {
        return runningFlag;
    }
}
