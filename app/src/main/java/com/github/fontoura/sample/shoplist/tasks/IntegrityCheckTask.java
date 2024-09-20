package com.github.fontoura.sample.shoplist.tasks;

import android.app.Activity;
import android.content.Intent;

import com.github.fontoura.sample.shoplist.ErrorActivity;
import com.github.fontoura.sample.shoplist.plugin.BackgroundTask;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IntegrityCheckTask implements BackgroundTask<Boolean> {

    private final Activity activity;
    private final List<Supplier<Boolean>> checks = new ArrayList<>();

    @Getter
    private int count = 0;

    public IntegrityCheckTask withCheck(Supplier<Boolean> check) {
        this.checks.add(check);
        return this;
    }

    @Override
    public Boolean doInBackground() throws Throwable {
        count ++;
        return doIntegrityCheck();
    }

    @Override
    public void continueInForeground(Boolean value) {
        if (!value) {
            Intent intent = new Intent(activity, ErrorActivity.class);
            activity.startActivity(intent);
            activity.finish();
        }
    }

    @Override
    public void handleInForeground(Throwable t) {
    }

    private boolean doIntegrityCheck() {
        int successfulChecks = 0;
        int failedChecks = 0;

        for (Supplier<Boolean> check : checks) {
            if (check.get()) {
                successfulChecks++;
            } else {
                failedChecks++;
            }
        }

        if (successfulChecks > 0 && failedChecks == 0) {
            return true;
        } else {
            return false;
        }
    }
}
