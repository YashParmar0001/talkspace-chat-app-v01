package com.example.talkspace

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class StatusWorker(
    context: Context,
    workerParams: WorkerParameters,
): CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        Log.d("StatusWorker", "Worker is running")
        return Result.success()
    }

}