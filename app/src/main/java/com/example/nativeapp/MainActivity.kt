package com.example.nativeapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nativeapp.apiService.RetrofitClient
import com.example.nativeapp.domain.Book
import com.example.nativeapp.roomdb.BookDatabase
import com.example.nativeapp.utils.DataStoreManager
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        Log.i("MAIN", "Fetching books")
        fetchBooksFromApi()
    }

    private fun loadBooksIntoAdapter(books: List<Book>) {
        bookAdapter = BookAdapter(books) { book ->
            navigateToBookDetail(book)
        }
        recyclerView.adapter = bookAdapter
    }

    private fun fetchBooksFromApi() {
        val dataStoreManager = DataStoreManager(this)
        val tokenFlow = dataStoreManager.getToken

        lifecycleScope.launch {
            tokenFlow.collect { token ->
                if (token.isNotEmpty()) {
                    Log.i("Fetching", "Entered with token: $token")
                    RetrofitClient.apiService.getBooks("Bearer $token")
                        .enqueue(object : Callback<List<Book>> {
                            override fun onResponse(
                                call: Call<List<Book>>,
                                response: Response<List<Book>>
                            ) {
                                if (response.isSuccessful) {
                                    val books = response.body() ?: emptyList()
                                    // Insert books into Room database
                                    lifecycleScope.launch {
                                        val bookDatabase =
                                            BookDatabase.getDatabase(this@MainActivity)
                                        bookDatabase.bookDao().insertBooks(books)
                                    }
                                    // Load books into the adapter after fetching from API
                                    loadBooksIntoAdapter(books)
                                } else {
                                    Log.e("MainPage", "onResponse: ${response.message()}")
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Error fetching books",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onFailure(call: Call<List<Book>>, t: Throwable) {
                                Log.e("MainPage", "error: ${t.message}")
                                Toast.makeText(
                                    this@MainActivity,
                                    "Network error",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        })
                }
            }
        }
    }

    private fun navigateToBookDetail(book: Book) {
        val intent = Intent(this, BookDetailActivity::class.java).apply {
            putExtra("BOOK_ID", book.id)
        }
        startActivity(intent)
    }

}




