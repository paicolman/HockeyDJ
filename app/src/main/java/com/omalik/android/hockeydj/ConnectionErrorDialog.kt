package com.omalik.android.hockeydj

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class ConnectionErrorDialog: DialogFragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_connection_error, container, false)
        val closeButton = view.findViewById<Button>(R.id.closeButton)
        val errMsg = view.findViewById<TextView>(R.id.errorMessage)
        errMsg.text = arguments?.getString("error")
        closeButton.setOnClickListener(this)
        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onClick(v: View?) {
        this.activity?.finish()
    }


}