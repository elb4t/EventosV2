package es.elb4t.eventosv2

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.android.synthetic.main.splash_screen.*


/**
 * Created by eloy on 30/3/18.
 */
class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        Comun.mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings: FirebaseRemoteConfigSettings = FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(BuildConfig.DEBUG).build()
        Comun.mFirebaseRemoteConfig.setConfigSettings(configSettings)
        Comun.mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_default)
        val cacheExpiration: Long = 30
        Comun.mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener {
                    Comun.mFirebaseRemoteConfig.activateFetched()
                    if (Comun.mFirebaseRemoteConfig.getBoolean("mensajeSplash"))
                        textSplash.text = getString(R.string.splash_esp)
                    else
                        textSplash.text = getString(R.string.splash_no_esp)
                    initApp()
                }
                .addOnFailureListener {
                    if (Comun.mFirebaseRemoteConfig.getBoolean("mensajeSplash"))
                        textSplash.text = getString(R.string.splash_esp)
                    else
                        textSplash.text = getString(R.string.splash_no_esp)
                    initApp()
                }
    }

    fun initApp() {
        Handler().postDelayed(Runnable {
            val intent = Intent(this, ActividadPrincipal::class.java)
            startActivity(intent)
        }, 5000)
    }

    override fun onBackPressed() {
        return
    }
}