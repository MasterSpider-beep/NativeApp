package com.example.nativeapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nativeapp.apiService.RetrofitClient
import com.example.nativeapp.domain.Book
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.app.Dialog
import android.util.Log
import android.view.LayoutInflater
import java.util.Calendar

class BookDetailActivity : AppCompatActivity() {
    private lateinit var titleTextView: TextView
    private lateinit var authorTextView: TextView
    private lateinit var releaseDateTextView: TextView
    private lateinit var quantityTextView: TextView
    private lateinit var isRentableTextView: TextView
    private lateinit var currBook: Book

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_detail)

        titleTextView = findViewById(R.id.titleTextView)
        authorTextView = findViewById(R.id.authorTextView)
        releaseDateTextView = findViewById(R.id.releaseDateTextView)
        quantityTextView = findViewById(R.id.quantityTextView)
        isRentableTextView = findViewById(R.id.isRentableTextView)

        val bookId = intent.getIntExtra("BOOK_ID", -1)
        fetchBookDetails(bookId)

        val editBookButton: Button = findViewById(R.id.editBookButton)
        editBookButton.setOnClickListener {
            showEditBookModal()
        }
    }

    private fun showEditBookModal() {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.edit_book_modal, null)
        dialog.setContentView(view)

        val window = dialog.window
        if (window != null) {
            window.setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                (resources.displayMetrics.heightPixels * 0.9).toInt()
            )
        }

        val editTitleEditText = view.findViewById<EditText>(R.id.editTitleEditText)
        val editAuthorEditText = view.findViewById<EditText>(R.id.editAuthorEditText)
        val editReleaseDateEditText = view.findViewById<EditText>(R.id.editReleaseDateEditText)
        val editQuantityEditText = view.findViewById<EditText>(R.id.editQuantityEditText)
        val editIsRentableCheckbox = view.findViewById<CheckBox>(R.id.editIsRentableCheckbox)
        val saveBookButton = view.findViewById<Button>(R.id.saveBookButton)

        // Populate the fields with current book data
        editTitleEditText.setText(titleTextView.text)
        editAuthorEditText.setText(authorTextView.text.removePrefix("by "))
        editReleaseDateEditText.setText(releaseDateTextView.text.removePrefix("Release Date: "))
        editQuantityEditText.setText(quantityTextView.text.removePrefix("Quantity: "))
        editIsRentableCheckbox.isChecked =
            isRentableTextView.text.removePrefix("Is Rentable: ") == "true"

        // Show Date Picker when clicking on Release Date EditText
        editReleaseDateEditText.setOnClickListener {
            showDatePickerDialog { selectedDate ->
                editReleaseDateEditText.setText(selectedDate) // Set the selected date
            }
        }
        saveBookButton.setOnClickListener {
            val updatedTitle = editTitleEditText.text.toString()
            val updatedAuthor = editAuthorEditText.text.toString()
            val updatedReleaseDate = editReleaseDateEditText.text.toString()
            val updatedQuantity = editQuantityEditText.text.toString().toIntOrNull()
            val updatedIsRentable = editIsRentableCheckbox.isChecked

            if (updatedTitle.isBlank() || updatedAuthor.isBlank() || updatedReleaseDate.isBlank()) {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (updatedQuantity == null || updatedQuantity < 0) {
                Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Update Book
            val book = Book(
                id = currBook.id,
                title = updatedTitle,
                author = updatedAuthor,
                releaseDate = updatedReleaseDate,
                quantity = updatedQuantity,
                isRentable = updatedIsRentable,
                owner = currBook.owner
            )
            updateBook(book)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showDatePickerDialog(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                onDateSelected(formattedDate)
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    private fun getToken(): String? {
        val sharedPreferences = getSharedPreferences("NativeAppPreferences", MODE_PRIVATE)
        return sharedPreferences.getString("AUTH_TOKEN", null)
    }

    private fun updateBook(book: Book){
        val token = getToken()
        RetrofitClient.apiService.createBook("Bearer $token", book).
        enqueue(object : Callback<Book> {
            override fun onResponse(call: Call<Book>, response: Response<Book>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@BookDetailActivity, "Book updated successfully!", Toast.LENGTH_SHORT).show()
                    currBook = book
                    setBookDisplay()
                } else {
                    Toast.makeText(this@BookDetailActivity, "Book couldn't updated successfully!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Book>, t: Throwable) {
                Log.e("BookDetailsPage", "${t.message}" )
            }
        })
    }

    private fun setBookDisplay(){
        titleTextView.text = currBook.title
        authorTextView.text = "by ${currBook.author}"
        releaseDateTextView.text = "Release Date: ${currBook.releaseDate}"
        quantityTextView.text = "Quantity: ${currBook.quantity}"
        isRentableTextView.text = "Is Rentable: ${currBook.isRentable}"
    }

    private fun fetchBookDetails(bookId: Int) {
        val token = getToken();
        RetrofitClient.apiService.getBookById("Bearer $token", bookId.toString())
            .enqueue(object : Callback<Book> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(call: Call<Book>, response: Response<Book>) {
                    if (response.isSuccessful) {
                        val book = response.body()
                        if (book != null) {
                            currBook = book
                            setBookDisplay();
                        };
                    } else {
                        Toast.makeText(
                            this@BookDetailActivity,
                            "Error: ${response.code()} - ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Book>, t: Throwable) {
                    Toast.makeText(this@BookDetailActivity, "Network error", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }
}