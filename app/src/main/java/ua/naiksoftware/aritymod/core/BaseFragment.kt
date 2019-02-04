package ua.naiksoftware.aritymod.core

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class BaseFragment : Fragment() {

    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

    protected fun manage(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    override fun onStop() {
        compositeDisposable.clear()
        super.onStop()
    }

    protected fun showMessage(@StringRes idRes: Int) {
        Toast.makeText(context, idRes, Toast.LENGTH_LONG).show()
    }

    protected fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}