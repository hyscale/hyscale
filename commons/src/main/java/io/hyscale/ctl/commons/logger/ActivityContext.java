package io.hyscale.ctl.commons.logger;

import io.hyscale.ctl.commons.models.Activity;


public class ActivityContext {

    private Activity startActivity;
    private long startTime;

    private int remaining;

    public ActivityContext(Activity startActivity) {
        this.startActivity = startActivity;
        this.remaining = WorkflowLogger.LEFT_ALIGNED_PADDING - startActivity.getActivityMessage().length();
    }

    public Activity getStartActivity() {
        return startActivity;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }

}
