package es.elb4t.eventosv2

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.appinvite.AppInvite
import com.google.android.gms.appinvite.AppInviteInvitation
import com.google.android.gms.appinvite.AppInviteReferral
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import es.elb4t.eventosv2.Comun.Companion.PLAY_SERVICES_RESOLUTION_REQUEST
import es.elb4t.eventosv2.Comun.Companion.acercaDe
import es.elb4t.eventosv2.Comun.Companion.colorFondo
import es.elb4t.eventosv2.Comun.Companion.mFirebaseAnalytics
import es.elb4t.eventosv2.Comun.Companion.mFirebaseRemoteConfig
import es.elb4t.eventosv2.Comun.Companion.mostrarDialogo
import es.elb4t.eventosv2.adapter.AdaptadorEventos
import es.elb4t.eventosv2.model.Evento
import es.elb4t.eventosv2.utils.EventosFirestore.EVENTOS
import es.elb4t.eventosv2.utils.EventosFirestore.crearEventos
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_actividad_principal.*


class ActividadPrincipal : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    private var adapter: AdaptadorEventos? = null
    val PERMISOS = arrayOf(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.GET_ACCOUNTS,
            android.Manifest.permission.CAMERA
    )
    private lateinit var mGoogleApiClient: GoogleApiClient
    private val REQUEST_INVITE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actividad_principal)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.app_name)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        if (!comprobarGooglePlayServices()) {
            Toast.makeText(this, "Error Google Play Services: no está instalado o no es válido.", Toast.LENGTH_LONG)
            finish()
        }
        crearEventos()
        var query: Query = FirebaseFirestore.getInstance()
                .collection(EVENTOS)
                .limit(50)

        val options = FirestoreRecyclerOptions.Builder<Evento>()
                .setQuery(query, Evento::class.java)
                .build()
        adapter = AdaptadorEventos(this, options)
        reciclerViewEventos.layoutManager = LinearLayoutManager(this) as LinearLayoutManager
        reciclerViewEventos.adapter = adapter

        val preferencias = applicationContext.getSharedPreferences("Temas", Context.MODE_PRIVATE)
        if (!preferencias.getBoolean("Inicializado", false)) {
            val prefs = applicationContext.getSharedPreferences(
                    "Temas", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putBoolean("Inicializado", true)
            editor.commit()
            FirebaseMessaging.getInstance().subscribeToTopic("Todos")
        }

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(AppInvite.API)
                .enableAutoManage(this, this)
                .build()

        ActivityCompat.requestPermissions(this, PERMISOS, 1)

        val autoLaunchDeepLink = true
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, autoLaunchDeepLink).setResultCallback { result ->
            if (result.status.isSuccess) {
                val intent = result.invitationIntent
                val deepLink = AppInviteReferral.getDeepLink(intent)
                val invitationId = AppInviteReferral.getInvitationId(intent)
                val url = Uri.parse(deepLink)
                val descuento = url.getQueryParameter("descuento")
                mostrarDialogo(applicationContext, "Tienes un descuento del $descuento% gracias a la invitación: $invitationId", "")
            }
        }

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings: FirebaseRemoteConfigSettings = FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(BuildConfig.DEBUG).build()
        mFirebaseRemoteConfig.setConfigSettings(configSettings)
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_default)
        val cacheExpiration: Long = 30
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener {
                    mFirebaseRemoteConfig.activateFetched()
                    getColorFondo()
                    getAcercaDe()
                }
                .addOnFailureListener {
                    colorFondo = mFirebaseRemoteConfig.getString("color_fondo")
                    acercaDe = mFirebaseRemoteConfig.getBoolean("acerca_de")
                }
        Fabric.with(this, Crashlytics())
    }


    private fun comprobarGooglePlayServices(): Boolean {
        val resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)

        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show()
            else
                finish()

            return false
        }
        return true
    }

    override fun onStart() {
        super.onStart()
        adapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter!!.stopListening()
    }

    override fun onResume() {
        super.onResume()
        var extras = intent.extras
        if (extras != null && extras.keySet().size > 4) {
            var evento = ""
            evento = "Evento: " + extras.getString("evento") + "\n"
            evento = evento + "Día: " + extras.getString("dia") + "\n"
            evento = evento + "Ciudad: " + extras.getString("ciudad") + "\n"
            evento = evento + "Comentario: " + extras.getString("comentario")
            mostrarDialogo(applicationContext, evento, extras.getString("evento") ?: "")
            for (key in extras.keySet()) {
                intent.removeExtra(key)
            }
            extras.clear()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_principal, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_temas -> {
                var bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "suscripciones")
                mFirebaseAnalytics!!.logEvent("menus", bundle)
                val intent = Intent(baseContext, Temas::class.java)
                startActivity(intent)
                return true
            }
            R.id.enviarEvento -> {
                val intent = Intent(baseContext, EnviarEvento::class.java)
                startActivity(intent)
                return true
            }
            R.id.action_invitar -> invitar()
            R.id.action_error -> {
                Crashlytics.getInstance().crash()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun getAppContext(): Context {
        return ActividadPrincipal().getAppContext()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                grantResults.forEach { result ->
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        Snackbar.make(reciclerViewEventos,
                                "Hay permisos necesarios para la aplicación sin activar", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Activar", {
                                    ActivityCompat.requestPermissions(this, PERMISOS, 1)
                                }).show()

                        return
                    }
                }
                return
            }
        }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Toast.makeText(this, "Error al enviar la invitación", Toast.LENGTH_LONG)
    }

    fun invitar() {
        val intent = AppInviteInvitation
                .IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setDeepLink(Uri.parse(getString(R.string.invitation_deep_link)))
                .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build()
        startActivityForResult(intent, REQUEST_INVITE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_INVITE) {
            if (resultCode == Activity.RESULT_OK) {
                val ids = AppInviteInvitation.getInvitationIds(resultCode, data)
            } else {
                Toast.makeText(this, "Error al enviar la invitación", Toast.LENGTH_LONG)
            }
        }
    }

    private fun getColorFondo() {
        colorFondo = mFirebaseRemoteConfig.getString("color_fondo")
    }

    private fun getAcercaDe() {
        acercaDe = mFirebaseRemoteConfig.getBoolean("acerca_de")
    }
}
