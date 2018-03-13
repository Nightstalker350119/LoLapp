package com.norbertotaveras.game_companion_app;

import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Norberto on 3/12/2018.
 */

// This class keeps track of the number of outstanding operations and shows the ProgressBar
// when there are one or more outstanding, and hides when there are zero outstanding.
// All methods are thread safe and may be called from threads other than the UI thread.

public class ProgressBarManager {
    private final ProgressBar progressBar;
    private final Handler uiHandler;
    private AtomicInteger pendingOperations;

    private Runnable showProgressBar = new Runnable() {
        @Override
        public void run() {
            progressBar.setVisibility(View.VISIBLE);
        }
    };

    private Runnable hideProgressBar = new Runnable() {
        @Override
        public void run() {
            progressBar.setVisibility(View.GONE);
        }
    };

    public ProgressBarManager(Handler uiHandler, ProgressBar progressBar) {
        this.progressBar = progressBar;
        this.uiHandler = uiHandler;
        pendingOperations = new AtomicInteger(0);
    }

    public ProgressBarManager(ProgressBar progressBar) {
        this.progressBar = progressBar;
        this.uiHandler = UIHelper.createRunnableLooper();
        pendingOperations = new AtomicInteger(0);
    }

    public void started(int operations) {
        if (pendingOperations.getAndAdd(operations) == 0)
            uiHandler.post(showProgressBar);
    }

    public void completed(int operations) {
        if (pendingOperations.addAndGet(-operations) == 0)
            uiHandler.post(hideProgressBar);
    }
}
