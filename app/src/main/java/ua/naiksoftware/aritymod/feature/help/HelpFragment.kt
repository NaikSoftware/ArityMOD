// Copyright (C) 2009 Mihai Preda, (C) 2019 Nickolay Savchenko

package ua.naiksoftware.aritymod.feature.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment

import ua.naiksoftware.aritymod.R
import ua.naiksoftware.aritymod.core.BaseFragment
import ua.naiksoftware.aritymod.databinding.FragmentHelpBinding

class HelpFragment : BaseFragment() {

    private lateinit var screenBinding: FragmentHelpBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        screenBinding = FragmentHelpBinding.inflate(inflater, container, false)
        return screenBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(screenBinding!!.toolbar)
        activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        screenBinding.webView.loadUrl("file:///android_asset/" + getString(R.string.help_file))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            android.R.id.home -> NavHostFragment.findNavController(this).popBackStack()

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
