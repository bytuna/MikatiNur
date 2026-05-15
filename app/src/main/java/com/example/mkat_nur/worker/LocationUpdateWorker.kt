package com.example.mkat_nur.worker

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result
import com.example.mkat_nur.model.Province
import com.example.mkat_nur.model.toPrayerData
import com.example.mkat_nur.viewmodel.RetrofitClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await
import java.util.*

class LocationUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val prefs = context.getSharedPreferences("mkat_nur_prefs", Context.MODE_PRIVATE)

    override suspend fun doWork(): Result {
        Log.d("LocationUpdateWorker", "Otomatik konum güncellemesi başladı.")
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
            val location = try {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
            } catch (e: SecurityException) {
                Log.e("LocationUpdateWorker", "GPS İzni Yok: ${e.message}")
                null
            }

            if (location != null) {
                val geocoder = Geocoder(applicationContext, Locale("tr"))
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                if (!addresses.isNullOrEmpty()) {
                    val city = addresses[0].adminArea ?: ""
                    val district = addresses[0].subAdminArea ?: addresses[0].locality ?: ""

                    val sehirList = RetrofitClient.diyanetInstance.getSehirler()
                    val matchedSehir = sehirList.find { it.SehirAdi.contains(city, ignoreCase = true) }

                    if (matchedSehir != null) {
                        val ilceList = RetrofitClient.diyanetInstance.getIlceler(matchedSehir.SehirID)
                        val matchedIlce = ilceList.find { it.IlceAdi.contains(district, ignoreCase = true) }
                            ?: ilceList.find { it.IlceAdi.contains(city, ignoreCase = true) }

                        if (matchedIlce != null) {
                            val province = Province(matchedIlce.IlceAdi, matchedIlce.IlceID)
                            saveLocation(province)
                            fetchAndCachePrayerTimes(province)
                            return Result.success()
                        }
                    }
                }
            }
            return Result.failure()
        } catch (e: Exception) {
            Log.e("LocationUpdateWorker", "Hata: ${e.message}")
            return Result.retry()
        }
    }

    private fun saveLocation(province: Province) {
        prefs.edit()
            .putString("selected_city_name", province.name)
            .putString("selected_city_id", province.id)
            .apply()
    }

    private suspend fun fetchAndCachePrayerTimes(province: Province) {
        try {
            val cityIdentifier = if (province.id.isNotEmpty()) province.id else province.name
            val responseList = RetrofitClient.diyanetInstance.getVakitler(cityIdentifier)
            if (responseList.isNotEmpty()) {
                val prayerDataList = responseList.map { it.toPrayerData() }
                val gson = com.google.gson.Gson()
                val json = gson.toJson(prayerDataList)
                
                val now = System.currentTimeMillis()
                prefs.edit()
                    .putString("vakitler_cache_${province.id}", json)
                    .putLong("last_update_timestamp", now)
                    .apply()
                
                com.example.mkat_nur.receiver.QuoteWidgetProvider.updateAllWidgets(applicationContext)
                Log.d("LocationUpdateWorker", "Vakitler güncellendi ve kaydedildi.")
            }
        } catch (e: Exception) {
            Log.e("LocationUpdateWorker", "Vakit çekme hatası: ${e.message}")
        }
    }
}
