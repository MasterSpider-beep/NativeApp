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
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.nativeapp.roomdb.BookDatabase
import com.example.nativeapp.utils.DataStoreManager
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class BookDetailActivity : AppCompatActivity() {
    private lateinit var titleTextView: TextView
    private lateinit var authorTextView: TextView
    private lateinit var releaseDateTextView: TextView
    private lateinit var quantityTextView: TextView
    private lateinit var isRentableTextView: TextView
    private lateinit var currBook: Book
    private lateinit var currentPhotoPath: String

    companion object {
        private const val CAMERA_REQUEST_CODE = 1
    }

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

//    private fun showEditBookModal() {
//        val dialog = Dialog(this)
//        val view = LayoutInflater.from(this).inflate(R.layout.edit_book_modal, null)
//        dialog.setContentView(view)
//
//        val window = dialog.window
//        if (window != null) {
//            window.setLayout(
//                (resources.displayMetrics.widthPixels * 0.9).toInt(),
//                (resources.displayMetrics.heightPixels * 0.9).toInt()
//            )
//        }
//
//        val editTitleEditText = view.findViewById<EditText>(R.id.editTitleEditText)
//        val editAuthorEditText = view.findViewById<EditText>(R.id.editAuthorEditText)
//        val editReleaseDateEditText = view.findViewById<EditText>(R.id.editReleaseDateEditText)
//        val editQuantityEditText = view.findViewById<EditText>(R.id.editQuantityEditText)
//        val editIsRentableCheckbox = view.findViewById<CheckBox>(R.id.editIsRentableCheckbox)
//        val saveBookButton = view.findViewById<Button>(R.id.saveBookButton)
//
//        // Populate the fields with current book data
//        editTitleEditText.setText(titleTextView.text)
//        editAuthorEditText.setText(authorTextView.text.removePrefix("by "))
//        editReleaseDateEditText.setText(releaseDateTextView.text.removePrefix("Release Date: "))
//        editQuantityEditText.setText(quantityTextView.text.removePrefix("Quantity: "))
//        editIsRentableCheckbox.isChecked =
//            isRentableTextView.text.removePrefix("Is Rentable: ") == "true"
//
//        // Show Date Picker when clicking on Release Date EditText
//        editReleaseDateEditText.setOnClickListener {
//            showDatePickerDialog { selectedDate ->
//                editReleaseDateEditText.setText(selectedDate) // Set the selected date
//            }
//        }
//        saveBookButton.setOnClickListener {
//            val updatedTitle = editTitleEditText.text.toString()
//            val updatedAuthor = editAuthorEditText.text.toString()
//            val updatedReleaseDate = editReleaseDateEditText.text.toString()
//            val updatedQuantity = editQuantityEditText.text.toString().toIntOrNull()
//            val updatedIsRentable = editIsRentableCheckbox.isChecked
//
//            if (updatedTitle.isBlank() || updatedAuthor.isBlank() || updatedReleaseDate.isBlank()) {
//                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            if (updatedQuantity == null || updatedQuantity < 0) {
//                Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            // Update Book
//            val book = Book(
//                id = currBook.id,
//                title = updatedTitle,
//                author = updatedAuthor,
//                releaseDate = updatedReleaseDate,
//                quantity = updatedQuantity,
//                isRentable = updatedIsRentable,
//                owner = currBook.owner
//            )
//            updateBook(book)
//            dialog.dismiss()
//        }
//        dialog.show()
//    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        val file = File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
        currentPhotoPath = file.absolutePath
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
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
        val editBookImageView = view.findViewById<ImageView>(R.id.editBookImageView)
        val selectImageButton = view.findViewById<Button>(R.id.selectImageButton)
        val saveBookButton = view.findViewById<Button>(R.id.saveBookButton)

        // To store the selected image URI
        var selectedImageUri: Uri? = null


        // Handle image selection
        selectImageButton.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                Toast.makeText(this, "Error creating image file: ${ex.message}", Toast.LENGTH_SHORT).show()
                null
            }

            photoFile?.let {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "com.example.nativeapp", // Update with your package name
                    it
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            }
        }

        editTitleEditText.setText(titleTextView.text)
        editAuthorEditText.setText(authorTextView.text.removePrefix("by "))
        editReleaseDateEditText.setText(releaseDateTextView.text.removePrefix("Release Date: "))
        editQuantityEditText.setText(quantityTextView.text.removePrefix("Quantity: "))
        editIsRentableCheckbox.isChecked =
            isRentableTextView.text.removePrefix("Is Rentable: ") == "true"

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

            // Convert the selected image to a base64 string
            val imageBase64String = selectedImageUri?.let { convertImageToBase64(it) }

            // Update Book
            val book = Book(
                id = currBook.id,
                title = updatedTitle,
                author = updatedAuthor,
                releaseDate = updatedReleaseDate,
                quantity = updatedQuantity,
                isRentable = updatedIsRentable,
                owner = currBook.owner,
                image = imageBase64String // Send the image as a base64 string
            )

            updateBook(book)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun convertImageToBase64(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val byteArray = inputStream?.readBytes()
            inputStream?.close()

            byteArray?.let { Base64.encodeToString(it, Base64.DEFAULT) }
        } catch (e: Exception) {
            Log.e("ImageConversion", "Error converting image to base64: ${e.message}")
            null
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i("CameraDebug", "CCamera result entered requestCode: $requestCode and resultCode = $resultCode")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            // Use the image file path created in `createImageFile()` to display or process the image
            showEditBookModal()
            val imageBitmap = BitmapFactory.decodeFile(currentPhotoPath)
            findViewById<ImageView>(R.id.editBookImageView).setImageBitmap(imageBitmap)
        }else{
            Log.e("CameraDebug", "Camera result not OK or unexpected requestCode: $requestCode and resultCode = $resultCode")
        }
    }

    private fun showDatePickerDialog(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate =
                    String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                onDateSelected(formattedDate)
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    private fun updateBook(book: Book) {
        val dataStoreManager = DataStoreManager(this)
        val tokenFlow = dataStoreManager.getToken
        lifecycleScope.launch {
            tokenFlow.collect { token ->
                RetrofitClient.apiService.createBook("Bearer $token", book)
                    .enqueue(object : Callback<Book> {
                        override fun onResponse(call: Call<Book>, response: Response<Book>) {
                            if (response.isSuccessful) {
                                Toast.makeText(
                                    this@BookDetailActivity,
                                    "Book updated successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                currBook = book
                                setBookDisplay()
                            } else {
                                Toast.makeText(
                                    this@BookDetailActivity,
                                    "Book couldn't updated successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<Book>, t: Throwable) {
                            Log.e("BookDetailsPage", "${t.message}")
                        }
                    })
            }
        }

    }

    private fun setBookDisplay() {
        titleTextView.text = currBook.title
        authorTextView.text = "by ${currBook.author}"
        releaseDateTextView.text = "Release Date: ${currBook.releaseDate}"
        quantityTextView.text = "Quantity: ${currBook.quantity}"
        isRentableTextView.text = "Is Rentable: ${currBook.isRentable}"

        val imageView: ImageView = findViewById(R.id.bookImageView)

        if (!currBook.image.isNullOrEmpty()) {

            val decodedString: ByteArray = Base64.decode(currBook.image, Base64.DEFAULT)
            val decodedBitmap: Bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

            imageView.visibility= View.VISIBLE
            imageView.setImageBitmap(decodedBitmap)

        } else {
            imageView.visibility= View.INVISIBLE
        }
    }

//    private fun fetchBookDetails(bookId: Int) {
//        val dataStoreManager = DataStoreManager(this)
//        val tokenFlow = dataStoreManager.getToken
//        lifecycleScope.launch {
//            tokenFlow.collect { token ->
//                RetrofitClient.apiService.getBookById("Bearer $token", bookId.toString())
//                    .enqueue(object : Callback<Book> {
//                        @SuppressLint("SetTextI18n")
//                        override fun onResponse(call: Call<Book>, response: Response<Book>) {
//                            if (response.isSuccessful) {
//                                val book = response.body()
//                                if (book != null) {
//                                    currBook = book
//                                };
//                            } else {
//                                Toast.makeText(
//                                    this@BookDetailActivity,
//                                    "Error: ${response.code()} - ${response.message()}",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                        }
//
//                        override fun onFailure(call: Call<Book>, t: Throwable) {
//                            Toast.makeText(
//                                this@BookDetailActivity,
//                                "Network error",
//                                Toast.LENGTH_SHORT
//                            )
//                                .show()
//                        }
//                    })
//            }
//        }
//
//    }

    private fun fetchBookDetails(bookId: Int){
        lifecycleScope.launch {
            val bookDatabase = BookDatabase.getDatabase(this@BookDetailActivity)
            val book = bookDatabase.bookDao().getBookById(bookId)
            if(book == null){
                Toast.makeText(this@BookDetailActivity, "This book doesn't exist", Toast.LENGTH_SHORT).show()
            }else{
                currBook = book
                setBookDisplay()
            }
        }
    }
}