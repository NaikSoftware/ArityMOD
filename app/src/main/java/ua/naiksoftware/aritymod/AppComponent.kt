package ua.naiksoftware.aritymod

import javax.inject.Singleton

import dagger.Component
import ua.naiksoftware.aritymod.feature.calculator.MainViewModel
import ua.naiksoftware.aritymod.feature.definitions.UserDefinitionsViewModel
import ua.naiksoftware.aritymod.service.ServicesModule

@Singleton
@Component(modules = [ServicesModule::class])
interface AppComponent {

    fun inject(userDefinitionsViewModel: UserDefinitionsViewModel)
    fun inject(mainViewModel: MainViewModel)
}
