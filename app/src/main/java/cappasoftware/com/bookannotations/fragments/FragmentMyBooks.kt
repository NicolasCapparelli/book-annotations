package cappasoftware.com.bookannotations.fragments

import android.graphics.PorterDuff
import android.graphics.drawable.ClipDrawable.HORIZONTAL
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import cappasoftware.com.bookannotations.R
import cappasoftware.com.bookannotations.api.GoogleBooks
import cappasoftware.com.bookannotations.controller.DatabaseController
import cappasoftware.com.bookannotations.dataclasses.Book
import cappasoftware.com.bookannotations.dataclasses.ConstantStrings
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import io.paperdb.Paper
import kotlinx.android.synthetic.main.fragment_my_books.*
import kotlinx.android.synthetic.main.fragment_my_books.view.*
import kotlinx.android.synthetic.main.recview_booklist_cell.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.lang.Exception


/**
 * Created by Nico on 7/5/2018.
 */

interface BookDataListener{

    fun onDataReady(newBookList: MutableList<Book>)
    fun onDataFailed()
}

class FragmentMyBooks : Fragment(), BookDataListener{

    // INTERFACE IMPLEMENTATION

    override fun onDataReady(newBookList: MutableList<Book>) {

        if (isAddingNewBook) {

            // Setting the recycler view's viewable data as the passed in data. If passed in data is empty recycler view will be empty
            (view?.recView_booklist?.adapter as BooklistAdapter).setViewableDataList(newBookList)

            // Remove loading screen and show recycler view
            view?.progress_bar?.visibility = GONE
            view?.recView_booklist?.visibility = VISIBLE
        }

        isSearching = false
    }

    override fun onDataFailed() {
        isSearching = false
    }

    // Used to create new instances of this fragment. Not needed for this fragment but maybe later
    companion object {
        fun newInstance(): FragmentMyBooks {
            return FragmentMyBooks()
        }
    }

    // GLOBAL
    var isAddingNewBook: Boolean = false
    var searchCharactersTyped: Int = 0
    var isSearching = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Allowing options on the toolbar
        setHasOptionsMenu(true)

        // Setting up the recycler view
        val view = inflater.inflate(R.layout.fragment_my_books, container, false)
        view.recView_booklist.layoutManager = LinearLayoutManager(this.context)
        view.recView_booklist.adapter = BooklistAdapter()

        // Adding a gray bottom border as a divider between recycler view cells
        view.recView_booklist.addItemDecoration(DividerItemDecoration(context, HORIZONTAL))

        view.progress_bar.indeterminateDrawable.setColorFilter(ContextCompat.getColor(context!!, R.color.colorPrimaryDark), PorterDuff.Mode.SRC_IN )

        return view
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_actionbar_booklist, menu)

        // Search My Books SearchView
        val searchItem : MenuItem = menu.findItem(R.id.toolbar_search)
        val searchView: android.support.v7.widget.SearchView = searchItem.actionView as android.support.v7.widget.SearchView

        // Add books floating action button
        view?.fab_addBook?.setOnClickListener {
            // Set the recycler view's viewable data to nothing (Empty)
            (view?.recView_booklist?.adapter as BooklistAdapter).setViewableDataList(mutableListOf())
            isAddingNewBook = true
            searchItem.expandActionView()
            view?.txt_createOwnBook?.visibility = VISIBLE
        }


        // Search My Books EventListener
        searchView.setOnQueryTextListener(object : android.support.v7.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {

                // This will be true if the user hits the add new book FAB and thus is searching for a new book
                if (isAddingNewBook) {

                    // Increment the amount of searchCharacters typed.
                    searchCharactersTyped++

                    // Once it reaches 4, send a query request to Google Books API and reset searchCharactersTyped to 0
                    if (searchCharactersTyped >= 4 && !isSearching) {

                        view?.progress_bar?.visibility = VISIBLE
                        view?.recView_booklist?.visibility = GONE

                        isSearching = true
                        GoogleBooks.queryBooksForSearch(newText, this@FragmentMyBooks)

                        searchCharactersTyped = 0

                    } else { }
                }

                // Else the user just wants to search through his own books
                else {
                    val newBookList = mutableListOf<Book>()

                    // Search the user's booklist to see if the book they are searching for is in their list
                    for (book in (recView_booklist.adapter as BooklistAdapter).getMainBookList()) {

                        // If it is, added to the newBookList which will be displayed by the recycler view
                        if (newText.toLowerCase() in book.title.toLowerCase() || newText.toLowerCase() in book.author.toLowerCase()) {
                            newBookList.add(book)
                        }
                    }

                    (recView_booklist.adapter as BooklistAdapter).setViewableDataList(newBookList)
                }


                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                //Task HERE
                return false
            }

        })

        // What happens when the search is open/closed
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {

            override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {

                if (isAddingNewBook) {
                    searchView.queryHint = "Find a New Book..."
                }

                else {
                    searchView.queryHint = "Search My Books..."
                }

                view?.fab_addBook?.visibility = GONE
                return true
            }

            // Changes the recycler view data back to its original state and the isAddingNewBook is set to false
            override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {

                isAddingNewBook = false

                (recView_booklist.adapter as BooklistAdapter).initMainBookList()

                view?.txt_createOwnBook?.visibility = GONE
                view?.fab_addBook?.visibility = VISIBLE

                return true
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }
}


// TODO: Error handling for literally everything
// TODO: Fix issue where bookCover images are not showing up on mobile data


// Custom Adapter For Recycler View
class BooklistAdapter : RecyclerView.Adapter<BooklistViewHolder>() {

    private var mainBookList: MutableList<Book> = mutableListOf()

    private var viewableBookList: MutableList<Book> = mainBookList


    init {
        // Call method to grab user's books
        initMainBookList()
    }

    // OVERRIDES

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BooklistViewHolder {
        val layoutInflater = LayoutInflater.from(parent?.context)
        val booklistCell = layoutInflater.inflate(R.layout.recview_booklist_cell, parent, false)


        return BooklistViewHolder(booklistCell)
    }


    override fun onBindViewHolder(holder: BooklistViewHolder, position: Int) {

        holder.itemView.txt_bookTitle.text = viewableBookList[position].title
        holder.itemView.txt_bookAuthor.text = viewableBookList[position].author

        // If the Book is a suggestion, fetch the image from the url
        if (viewableBookList[position].isSuggestion) {

            // Start a loading bar above the book cover ImageView
            holder.itemView.progress_bar_bookCover.visibility = VISIBLE

            if (viewableBookList[position].coverImageURL != ""){

                // Using picasso to fetch the image
                Picasso.get().load(viewableBookList[position].coverImageURL).into(holder.itemView.img_bookCover, object: Callback {

                    override fun onSuccess() {
                        holder.itemView.progress_bar_bookCover.visibility = GONE
                    }

                    override fun onError(e: Exception?) {
                        holder.itemView.progress_bar_bookCover.visibility = GONE
                        holder.itemView.img_bookCover.setImageDrawable(holder.itemView.context.getDrawable(R.drawable.ic_missing_image_24dp))
                        Log.e("IMAGE ERROR>>", e.toString(), e)
                    }


                })
            }

            else {
                holder.itemView.img_bookCover.setImageDrawable(holder.itemView.context.getDrawable(R.drawable.ic_missing_image_24dp))
            }
        }

        // Otherwise the book cover is saved in the object since it was saved by the user
        else {
            holder.itemView.img_bookCover.setImageBitmap(viewableBookList[position].getCoverAsBitmap())
        }


        // If currently showing suggestions for new books
        if (viewableBookList[position].isSuggestion) {

            // Show the add book button and show the cover image
            holder.itemView.btn_addToMyBook.visibility = VISIBLE

            // Adding functionality to the Add New Book Button
            holder.itemView.btn_addToMyBook.setOnClickListener {

                // Set isSuggestion to False because it will now reside in the user's book collection
                viewableBookList[position].isSuggestion = false

                // Add to user's book database
                DatabaseController.addToUserBooks(viewableBookList[position], holder.itemView.context)

                // Remove the item from the list and tell the adapter
                viewableBookList.removeAt(position)
                notifyItemRemoved(position)
            }
        }

        else {
            holder.itemView.btn_addToMyBook.visibility = GONE
        }
    }

    // Number of items in recycler view
    override fun getItemCount(): Int {
        return viewableBookList.size
    }


    // PERSONAL FUNCTIONS

    fun getMainBookList(): List<Book>{
        return mainBookList
    }

    // Sets the main book list to the passed in value, used to update mainBookList
    private fun setMainBookList(usersBooklist: MutableList<Book>) {
        mainBookList = usersBooklist
    }

    fun setViewableDataList(newBookList: MutableList<Book>){
        viewableBookList = newBookList
        notifyDataSetChanged()
    }

    fun revertToMainList() {
        viewableBookList = mainBookList
        notifyDataSetChanged()
    }

    // Reads from local PaperDB to get user's books and sets it as this adapter's data set
    fun initMainBookList() {

        doAsync {

            // Read from PaperDB
            val usersBooklist: MutableList<Book> = Paper.book(ConstantStrings.PAPERBOOK_USERBOOKLIST).read(ConstantStrings.PAPERKEY_ALLBOOKS)

            uiThread {
                setMainBookList(usersBooklist)
                revertToMainList()
            }
        }
    }
}

// Custom Adapter For Recycler View
class BooklistViewHolder(v: View) : RecyclerView.ViewHolder(v)