package ua.naiksoftware.aritymod.core

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

abstract class BaseViewModel : ViewModel() {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

     val publisher: Subject<Action> = PublishSubject.create()

    fun manage(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    protected fun publish(action: Action) {
        publisher.onNext(action)
    }

    override fun onCleared() {
        compositeDisposable.clear()
    }
}