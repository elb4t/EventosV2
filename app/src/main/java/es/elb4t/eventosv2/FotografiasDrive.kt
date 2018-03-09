package es.elb4t.eventosv2

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView


/**
 * Created by eloy on 9/3/18.
 */
class FotografiasDrive : AppCompatActivity() {
    var mDisplay: TextView? = null
    internal var evento: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fotografias_drive)
        val extras = intent.extras
        evento = extras!!.getString("evento")
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = FotografiasDrive::class.java.simpleName
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_drive, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val vista = findViewById<View>(android.R.id.content) as View
        val id = item.getItemId()
        when (id) {
            R.id.action_camara -> {
            }
            R.id.action_galeria -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }
}