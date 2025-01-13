package com.example.nativeapp

import FetchBooksWorker
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.nativeapp.domain.Book
import com.example.nativeapp.roomdb.BookDatabase
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import android.os.Build
import com.example.nativeapp.utils.NetworkStatusMonitor
import com.example.nativeapp.utils.ShakeDetector
import com.example.nativeapp.utils.createNotificationChannel
import com.example.nativeapp.utils.showNotification

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter
    private lateinit var shakeDetector: ShakeDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val fetchBooksWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<FetchBooksWorker>().build()
        WorkManager.getInstance(this).enqueue(fetchBooksWorkRequest)

        reloadBooksFromDatabase()

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(fetchBooksWorkRequest.id)
            .observe(this, Observer { workInfo ->
                if (workInfo != null && workInfo.state.isFinished) {
                    reloadBooksFromDatabase()
                }
            })

        schedulePeriodicWork(this)

        createNotificationChannel(this)

        val networkStatusMonitor = NetworkStatusMonitor(this)

        // Observe network status changes
        networkStatusMonitor.observe(this, Observer { isOnline ->
            if (isOnline) {
                showNotification(this, "Online", "Your device is now online.")
            } else {
                showNotification(this, "Offline", "Your device is now offline.")
            }
        })

        shakeDetector = ShakeDetector(this) {
            runOnUiThread {
                bookAdapter.triggerShakeAnimation()
            }
        }
        shakeDetector.start()
    }

    private fun schedulePeriodicWork(context: Context) {
        val periodicWorkRequest = PeriodicWorkRequestBuilder<FetchBooksWorker>(15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "FetchBooksWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWorkRequest
        )
    }

    private fun reloadBooksFromDatabase() {
        lifecycleScope.launch {
            val bookDatabase = BookDatabase.getDatabase(this@MainActivity)
            val books = bookDatabase.bookDao().getBooks() // Fetch books from Room
            loadBooksIntoAdapter(books)
        }
    }

    private fun loadBooksIntoAdapter(books: List<Book>) {
        bookAdapter = BookAdapter(this, books) { book ->
            navigateToBookDetail(book)
        }
        recyclerView.adapter = bookAdapter
    }

    private fun navigateToBookDetail(book: Book) {
        val intent = Intent(this, BookDetailActivity::class.java).apply {
            putExtra("BOOK_ID", book.id)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        shakeDetector.stop() // Stop shake detection to avoid memory leaks
    }
}




