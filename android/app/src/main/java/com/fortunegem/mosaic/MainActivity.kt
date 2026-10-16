package com.fortunegem.mosaic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fortunegem.mosaic.core.di.ServiceLocator
import com.fortunegem.mosaic.core.navigation.Navigator
import com.fortunegem.mosaic.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    val navigator: Navigator
        get() {
            val existing = navigatorRef
            if (existing != null) return existing
            val created = Navigator(supportFragmentManager, R.id.fragment_container)
            navigatorRef = created
            return created
        }

    private var navigatorRef: Navigator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        ServiceLocator.init(applicationContext)
        super.onCreate(savedInstanceState)
        val inflated = ActivityMainBinding.inflate(layoutInflater)
        binding = inflated
        setContentView(inflated.root)
        if (savedInstanceState == null) {
            navigator.showSplash()
        }
    }

    override fun onDestroy() {
        binding = null
        navigatorRef = null
        super.onDestroy()
    }
}
