package com.omalik.android.hockeydj

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.omalik.android.hockeydj.databinding.FragmentQuickHelpBinding


class QuickHelp : DialogFragment(), View.OnClickListener {
    val TAG = "JZ:QuickHelpFragment"

    //data binding
    lateinit var binding: FragmentQuickHelpBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_quick_help, container, false)

        //val tag = tag
        val modeHelp = tag == "help"
        var sp = Html.fromHtml(getString(R.string.quickhelp),Html.FROM_HTML_MODE_COMPACT)
        if (!modeHelp) {
            sp = Html.fromHtml(getString(R.string.acknowledgements), Html.FROM_HTML_MODE_COMPACT)
        }
        binding.helptext.text = sp
        binding.helptext.setMovementMethod(LinkMovementMethod.getInstance())
        binding.homepageLink.setOnClickListener(this)
        binding.close.setOnClickListener(this)

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)


        return dialog
    }

    override fun onClick(v: View?) {
        when(v) {
            binding.homepageLink -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.omalik.ch/hockeydj/"))
                startActivity(browserIntent)
            }
            binding.close -> {
                this.dismiss()
            }
        }
    }
}
