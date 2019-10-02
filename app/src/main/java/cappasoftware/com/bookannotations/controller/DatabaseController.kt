package cappasoftware.com.bookannotations.controller

import android.content.Context
import android.widget.Toast
import cappasoftware.com.bookannotations.dataclasses.Book
import cappasoftware.com.bookannotations.dataclasses.ConstantStrings
import io.paperdb.Paper
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * Created By Nico on 7/8/2018
 */
object DatabaseController {

    // Adds a new Book The user's main booklist
    fun addToUserBooks(newBook: Book, context: Context) {

        doAsync {

            // Grab the current book list
            val userBooks: MutableList<Book> = Paper.book(ConstantStrings.PAPERBOOK_USERBOOKLIST).read(ConstantStrings.PAPERKEY_ALLBOOKS)

            // Save the coverImage so that the user can see it even when offline
            newBook.saveImageFromSource()

            // Add the new book to it
            userBooks.add(newBook)

            // Write the new list to the database
            Paper.book(ConstantStrings.PAPERBOOK_USERBOOKLIST).write(ConstantStrings.PAPERKEY_ALLBOOKS, userBooks)

            uiThread {

                // Tell the user the book was added
                Toast.makeText(context, "Added To Your Books!" + newBook.title, Toast.LENGTH_SHORT).show()
            }

        }

    }
}