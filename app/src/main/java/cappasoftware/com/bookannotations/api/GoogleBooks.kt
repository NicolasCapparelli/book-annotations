package cappasoftware.com.bookannotations.api
import android.os.Handler
import android.os.Looper
import android.util.Log
import cappasoftware.com.bookannotations.dataclasses.Book
import cappasoftware.com.bookannotations.fragments.BookDataListener
import com.squareup.okhttp.Callback
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException



object GoogleBooks {

    private val okClient: OkHttpClient = OkHttpClient()

    fun queryBooksForSearch(query: String, myBooksInterface: BookDataListener){

        val handler = Handler(Looper.getMainLooper())

        // Creating the network request
        val request: Request = Request.Builder()
                .get()
                .url("https://www.googleapis.com/books/v1/volumes?q=$query")
                .build()

        okClient.newCall(request).enqueue(object: Callback {

            override fun onResponse(response: Response?) {

                lateinit var respArray: JSONArray

                // A JSONException will be raised if there are no values for the search
                try {
                    respArray= JSONObject(response?.body()?.string()).getJSONArray("items")
                }

                catch (e: JSONException) {

                    // Give the interface an empty list
                    handler.post { myBooksInterface.onDataReady(mutableListOf())}
                    return
                }

                val suggestionList: MutableList<Book> = mutableListOf()

                for (i in 0 until respArray.length() ) {

                    // The response for the particular book currently being indexed
                    val book = respArray.getJSONObject(i).getJSONObject("volumeInfo")

                    // These two variables have a change of not appearing in the response, so they are assigned later
                    val categoryList: MutableList<String> = mutableListOf()
                    var imageLink = ""
                    var author = ""

                    // The next three if statements are to check if the following keys are in the response because Google decides to completely omit them if they don't contain values
                    if (!book.isNull("categories")) {

                        // Grab all categories from the response array and put them in categoryList
                        for (index in 0 until book.optJSONArray("categories").length()) {
                            categoryList.add(book.getJSONArray("categories").optString(index, ""))
                        }
                    }

                    if (!book.isNull("authors")) {
                        author = book.getJSONArray("authors").optString(0, "Author Not Found")
                    }

                    if (!book.isNull("imageLinks")){
                        imageLink = book.getJSONObject("imageLinks").optString("thumbnail", "")
                    }

                    val bookObj = Book(book.getString("title"), author,
                            imageLink, book.optString("pageCount", "0"),
                            categoryList, book.optString("publishedDate", ""))

                    bookObj.isSuggestion = true
                    suggestionList.add(bookObj)
                }

                // Run the interface call back on the main thread
                handler.post { myBooksInterface.onDataReady(suggestionList) }
            }

            override fun onFailure(request: Request?, e: IOException?) {
                handler.post{ myBooksInterface.onDataFailed()}
            }

        })

    }
}