package cappasoftware.com.bookannotations.dataclasses

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URL

data class Book(val title: String, var author: String,
                val coverImageURL: String,val pageCount: String = "",
                val categories: List<String> = emptyList(), val publishDate:String = "") {

    // Vars to be initialized in the init function
    lateinit var coverImage: ByteArray
    lateinit var annotationCreator: String
    // lateinit var annotations: Map<Int,String>

    // Runs when this object is created
    init {
    }

    // Variables to be modified depending on user action
    var pagesAnnotated: Int = 0

    // Whether or not this book is a suggestion to the user or an actual book he created
    var isSuggestion: Boolean = false


    // Gets the coverImage from the link passed in through the constructor and converts it to a byte Array so that it may be serialized and saved with PaperDB
    fun saveImageFromSource(){
        val inputStream: InputStream = URL(coverImageURL).content as InputStream
        val out = ByteArrayOutputStream()
        val buf = ByteArray(1024)

        var n = 0
        do {

            n = inputStream.read(buf)

            if (n==-1)
                break

            out.write(buf, 0, n)
        }

        while (-1 != n)

        out.close()
        inputStream.close()

        coverImage = out.toByteArray()
    }

    fun getCoverAsBitmap(): Bitmap{
        return BitmapFactory.decodeByteArray(coverImage, 0, coverImage.size)
    }
}