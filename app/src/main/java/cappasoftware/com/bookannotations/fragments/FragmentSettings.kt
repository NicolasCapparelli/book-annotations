package cappasoftware.com.bookannotations.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cappasoftware.com.bookannotations.R

class FragmentSettings : Fragment() {


    // Used to create new instances of this fragment. Not needed for this fragment but maybe later
    companion object {
        fun newInstance(): FragmentSettings {
            return FragmentSettings()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

}