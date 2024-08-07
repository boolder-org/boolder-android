package com.boolder.boolder

import android.content.Context
import com.boolder.boolder.data.databaseModule
import com.boolder.boolder.view.viewModelModule
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.koinApplication
import org.koin.test.KoinTest
import org.koin.test.check.checkModules
import org.koin.test.mock.MockProviderRule
import org.mockito.Mockito

@Ignore("This test is broken and has to be fixed")
class KoinModuleInjectionTest : KoinTest {

    @get:Rule
    val mockProvider = MockProviderRule.create { clazz ->
        Mockito.mock(clazz.java)
    }

    @Test
    fun `test that all modules of koin can be injected at runtime`() {

        koinApplication {
            modules(databaseModule, viewModelModule)
            checkModules {
                withInstance<Context>()
            }
        }
    }
}
