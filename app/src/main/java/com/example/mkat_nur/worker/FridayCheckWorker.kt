package com.example.mkat_nur.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mkat_nur.util.FridayScheduler

/** Günde iki kez sıradaki Cuma kartı alarmının kurulu olduğundan emin olur. */
class FridayCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        FridayScheduler.scheduleNext(applicationContext)
        return Result.success()
    }
}
