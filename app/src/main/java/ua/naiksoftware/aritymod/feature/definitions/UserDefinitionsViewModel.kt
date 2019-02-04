package ua.naiksoftware.aritymod.feature.definitions

import androidx.databinding.ObservableField
import ua.naiksoftware.aritymod.Injector
import ua.naiksoftware.aritymod.core.BaseViewModel
import ua.naiksoftware.aritymod.service.UserDefinitionsService
import javax.inject.Inject

class UserDefinitionsViewModel : BaseViewModel() {

    val definitions : ObservableField<List<String>> = ObservableField(ArrayList())

    @Inject
    lateinit var userDefinitionsService: UserDefinitionsService

    init {
        Injector.appComponent.inject(this)

        manage(userDefinitionsService.linesSubject.subscribe(definitions::set))
    }

    fun clearDefinitions() {
        userDefinitionsService.clear()
        userDefinitionsService.save()
    }
}
