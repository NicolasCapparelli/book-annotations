package cappasoftware.com.bookannotations.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import cappasoftware.com.bookannotations.R

class FragmentHome : Fragment() {

    // Used to create new instances of this fragment.
    companion object {
        fun newInstance(): FragmentHome {
            return FragmentHome()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }


}