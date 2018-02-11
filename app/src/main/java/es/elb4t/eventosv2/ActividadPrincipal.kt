package es.elb4t.eventosv2

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import es.elb4t.eventosv2.EventosAplicacion.Companion.PLAY_SERVICES_RESOLUTION_REQUEST
import es.elb4t.eventosv2.EventosAplicacion.Companion.mostrarDialogo
import es.elb4t.eventosv2.adapter.AdaptadorEventos
import es.elb4t.eventosv2.model.Evento
import es.elb4t.eventosv2.utils.EventosFirestore.EVENTOS
import es.elb4t.eventosv2.utils.EventosFirestore.crearEventos
import kotlinx.android.synthetic.main.activity_actividad_principal.*


class ActividadPrincipal : AppCompatActivity() {
    private var adapter: AdaptadorEventos? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actividad_principal)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.app_name)

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
        reciclerViewEventos.layoutManager = LinearLayoutManager(this)
        reciclerViewEventos.adapter = adapter
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
            mostrarDialogo(applicationContext, evento)
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
        val id = item.itemId
        if (id == R.id.action_temas) {
            val intent = Intent(baseContext, Temas::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
