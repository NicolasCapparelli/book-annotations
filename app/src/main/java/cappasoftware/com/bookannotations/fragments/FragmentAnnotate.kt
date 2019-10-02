package cappasoftware.com.bookannotations.fragments

import android.support.v4.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cappasoftware.com.bookannotations.R

/**
 * Created By Nico on 7/9/2018
 */
class FragmentAnnotate: Fragment() {

    // Used to create new instances of this fragment.
    companion object {
        fun newInstance(): FragmentAnnotate {
            return FragmentAnnotate()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_annotate, container, false)

        return view
    }

}