package com.ispumap.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ispumap.R


class MyFrament : Fragment() {
    companion object {
        fun newInstance(message: String): MyFrament {
            val f = MyFrament()
            val bdl = Bundle(1)
            bdl.putString(EXTRA_MESSAGE, message)
            f.setArguments(bdl)
            return f
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view: View? = inflater.inflate(R.layout.fragment_cities, container, false);
        val message = arguments!!.getString(EXTRA_MESSAGE)
        var textView: TextView = view!!.findViewById(R.id.text)
        textView!!.text = message
        return view
    }


}

