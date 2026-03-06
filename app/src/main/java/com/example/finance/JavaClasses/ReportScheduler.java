package com.example.finance;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class ReportScheduler {
    private static final String DAILY_REPORT_WORK_NAME = "daily_report_worker";

    public static void scheduleDailyReport(Context context) {
        long initialDelay = getInitialDelayToNextRun();

        PeriodicWorkRequest workRequest =
                new PeriodicWorkRequest.Builder(DailyReportWorker.class, 24, TimeUnit.HOURS)
                        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                        .build();

        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                        DAILY_REPORT_WORK_NAME,
                        ExistingPeriodicWorkPolicy.UPDATE,
                        workRequest
                );
    }

    private static long getInitialDelayToNextRun() {
        Calendar now = Calendar.getInstance();
        Calendar nextRun = Calendar.getInstance();
        nextRun.set(Calendar.HOUR_OF_DAY, 0);
        nextRun.set(Calendar.MINUTE, 5);
        nextRun.set(Calendar.SECOND, 0);
        nextRun.set(Calendar.MILLISECOND, 0);

        if (!nextRun.after(now)) {
            nextRun.add(Calendar.DAY_OF_YEAR, 1);
        }

        return nextRun.getTimeInMillis() - now.getTimeInMillis();
    }
}
