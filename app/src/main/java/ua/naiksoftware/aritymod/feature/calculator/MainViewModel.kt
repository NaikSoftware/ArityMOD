package ua.naiksoftware.aritymod.feature.calculator

import android.text.TextUtils
import android.view.View
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import org.javia.arity.Complex
import org.javia.arity.Function
import org.javia.arity.Symbols
import org.javia.arity.SyntaxException
import org.javia.arity.Util
import timber.log.Timber
import ua.naiksoftware.aritymod.Injector
import ua.naiksoftware.aritymod.core.BaseViewModel
import ua.naiksoftware.aritymod.service.HistoryService
import ua.naiksoftware.aritymod.service.UserDefinitionsService
import ua.naiksoftware.aritymod.service.model.HistoryEntry
import java.util.ArrayList
import javax.inject.Inject

class MainViewModel : BaseViewModel() {

    companion object {
        private const val INFINITY = "Infinity"
        private const val INFINITY_UNICODE = "\u221e"
    }

    val result = ObservableField<String>("")
    val input = ObservableField<String>("")
    val history = ObservableField<List<HistoryEntry>>(ArrayList())

    @Inject
    lateinit var symbols: Symbols

    @Inject
    lateinit var userDefinitionsService: UserDefinitionsService

    @Inject
    lateinit var historyService: HistoryService

    private val functions = ObservableField<ArrayList<Function>>(ArrayList())

    private val graphFunction = ObservableField<Function>()

    var resultLength: Int = 0

    init {
        Injector.appComponent.inject(this)
        input.set(historyService.text)
        manage(historyService.historySubject.subscribe(history::set))
        input.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                evaluate(input.get())
            }
        })
    }

    fun onEnter() {
        val text = input.get()
        try {
            val func = symbols.compileWithName(text)
            if (func.name != null) {
                symbols.define(func)
                userDefinitionsService.add(text)
            }
            val f = func.function
            val arity = f.arity()
            var value: Complex? = null
            if (arity == 0) {
                value = f.evalComplex()
                symbols.define("ans", value)
                historyService.onEnter(text, formatResult(value))
            } else {
                historyService.onEnter(text, null)
            }

        } catch (e: SyntaxException) {
             historyService.onEnter(text, null)
        }

        showGraph(null)

        if (TextUtils.isEmpty(text)) {
            result.set("")
        }
        input.set(historyService.text)
    }

    private fun evaluate(textParam: String?) {
        var text = textParam
        Timber.d("Evaluate %s", text)
        if (TextUtils.isEmpty(text)) {
            return
        }

        val functionsList = functions.get()!!
        functionsList.clear()
        var end = -1
        do {
            text = text.substring(end + 1)
            end = text.indexOf(';')
            val slice = if (end == -1) text else text.substring(0, end)
            try {
                val f = symbols.compile(slice)
                functionsList.add(f)
            } catch (e: SyntaxException) {
                // Ignored
            }

        } while (end != -1)

        val size = functionsList.size
        if (size == 0) {
//            result.setEnabled(false)
        } else if (size == 1) {
            val f = functionsList[0]
            val arity = f.arity()
            if (arity == 1 || arity == 2) {
                result.set("")
                graphFunction.set(f)
            } else if (arity == 0) {
                result.set(formatResult(f.evalComplex()))
                graphFunction.set(null)
            } else {
                result.set("function")
//                result.setEnabled(true)
                graphFunction.set(null)
            }
        } else {
            graphView.setFunctions(functionsList)
            if (graphView.getVisibility() != View.VISIBLE) {
                if (isAlphaVisible) {
                    isAlphaVisible = false
                    updateAlpha()
                }
                result.setVisibility(View.GONE)
                historyView.setVisibility(View.GONE)
                graph3dView.setVisibility(View.GONE)
                graph3dView.onPause()
                graphView.setVisibility(View.VISIBLE)
            }
        }
        functions.notifyChange()
    }

    private fun formatResult(value: Complex): String {
        val res = Util.complexToString(value, resultLength, 2)
        return res.replace(INFINITY, INFINITY_UNICODE)
    }

    fun onPause() {
        historyService.updateEdited(input.get())
        historyService.save()
        userDefinitionsService.save()
    }

    fun clearHistory() {
        historyService.clear()
        historyService.save()
    }

    fun clearUserDefinitions() {
        userDefinitionsService.clear()
        userDefinitionsService.save()
    }

    fun moveToHistory(pos: Int) {
        historyService.moveToPos(pos, input.get())
    }
}
