import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nativeapp.apiService.RetrofitClient
import com.example.nativeapp.domain.Book
import com.example.nativeapp.roomdb.BookDatabase
import com.example.nativeapp.utils.DataStoreManager
import kotlinx.coroutines.flow.first
import retrofit2.Response

class FetchBooksWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val dataStoreManager = DataStoreManager(applicationContext)
        val token = dataStoreManager.getToken.first() // Retrieve token synchronously

        if (token.isNotEmpty()) {
            return try {
                val response: Response<List<Book>> =
                    RetrofitClient.apiService.getBooks("Bearer $token").execute()
                if (response.isSuccessful) {
                    val books = response.body() ?: emptyList()
                    Log.i("Fetching", "Fetched books: $books")

                    // Insert books into the Room database
                    val bookDatabase = BookDatabase.getDatabase(applicationContext)
                    bookDatabase.bookDao().insertBooks(books)

                    // Notify success
                    Result.success()
                } else {
                    Log.e("Fetching", "Failed to fetch books: ${response.message()}")
                    Result.failure()
                }
            } catch (e: Exception) {
                Log.e("Fetching", "Error fetching books: ${e.message}")
                Result.retry()
            }
        }
        return Result.failure()
    }
}
