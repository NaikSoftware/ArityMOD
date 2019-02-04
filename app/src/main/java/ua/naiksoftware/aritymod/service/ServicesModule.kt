package ua.naiksoftware.aritymod.service

import android.content.Context
import dagger.Module
import dagger.Provides
import org.javia.arity.Symbols
import javax.inject.Singleton

@Module
class ServicesModule(private val context: Context) {

    @Provides
    fun provideContext() : Context {
        return context
    }

    @Provides
    @Singleton
    fun provideDefinitionService(symbols: Symbols) : UserDefinitionsService {
        return UserDefinitionsService(context, symbols)
    }

    @Provides
    @Singleton
    fun provideHistoryService(context: Context) : HistoryService {
        return HistoryService(context)
    }

    @Provides
    @Singleton
    fun provideSymbols() : Symbols {
        return Symbols()
    }
}