// Copyright (C) 2009 Mihai Preda, (C) 2019 Nickolay Savchenko
package ua.naiksoftware.aritymod.feature.calculator.history

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.databinding.DataBindingUtil
import ua.naiksoftware.aritymod.databinding.HistoryLineBinding
import ua.naiksoftware.aritymod.service.HistoryService
import ua.naiksoftware.aritymod.service.model.HistoryEntry

class HistoryAdapter internal constructor(context: Context, private val entries: ArrayList<HistoryEntry>) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    fun update(entries: List<HistoryEntry>) {
        this.entries.clear()
        this.entries.addAll(entries)
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return entries.size
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(pos: Int, view: View?, parent: ViewGroup): View {
        val binding = if (view == null) {
            HistoryLineBinding.inflate(inflater, parent, false)
        } else {
            DataBindingUtil.getBinding(view)!!
        }

        val revPos = entries.size - pos - 1
        val entry = entries[revPos]

        binding.input.text = entry.line
        binding.result.text = entry.result
        binding.equalsSeparator.visibility = if (TextUtils.isEmpty(entry.result)) View.GONE else View.VISIBLE

        return binding.root
    }
}
