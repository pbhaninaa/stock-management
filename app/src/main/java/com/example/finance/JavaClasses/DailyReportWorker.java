package com.example.finance;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class DailyReportWorker extends Worker {
    public DailyReportWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        StockDatabaseHelper dbHelper = new StockDatabaseHelper(getApplicationContext());
        if (!dbHelper.hasUsers() || !Utils.hasTavernDetails(getApplicationContext())) {
            return Result.success();
        }

        String reportDate = Utils.getDateDaysAgo(1);
        ReportManager.SendStatus status = ReportManager.sendReportBlocking(
                getApplicationContext(),
                reportDate,
                StockDatabaseHelper.REPORT_KIND_MIDNIGHT_AUTO,
                "System scheduler",
                true
        );

        if (status == ReportManager.SendStatus.FAILED) {
            return Result.retry();
        }

        return Result.success();
    }
}
