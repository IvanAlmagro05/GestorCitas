package com.example.gestorcitas

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var db: DBHelper
    private lateinit var recycler: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = DBHelper(this)
        recycler = findViewById(R.id.recyclerCitas)
        recycler.layoutManager = LinearLayoutManager(this)

        val edtNombre: EditText = findViewById(R.id.edtNombre)
        val edtTelefono: EditText = findViewById(R.id.edtTelefono)
        val edtFecha: EditText = findViewById(R.id.edtFecha)
        val edtHora: EditText = findViewById(R.id.edtHora)
        val edtNotas: EditText = findViewById(R.id.edtNotas)
        val spinner: Spinner = findViewById(R.id.spinnerTipo)
        val btnGuardar: Button = findViewById(R.id.btnGuardar)
        val btnLlamar: Button = findViewById(R.id.btnLlamar)

        btnLlamar.setOnClickListener {
            val numero = "953953953"
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$numero"))
            startActivity(intent)
        }

        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.tipos_cita)
        )

        edtFecha.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, y, m, d -> edtFecha.setText("$d/${m + 1}/$y") },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        edtHora.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, h, m -> edtHora.setText(String.format("%02d:%02d", h, m)) },
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
            ).show()
        }

        btnGuardar.setOnClickListener {
            val nombre = edtNombre.text.toString()
            val telefono = edtTelefono.text.toString()
            val fecha = edtFecha.text.toString()
            val hora = edtHora.text.toString()
            val tipo = spinner.selectedItem.toString()
            val notas = edtNotas.text.toString()

            if (nombre.isEmpty() || fecha.isEmpty() || hora.isEmpty() || telefono.isEmpty()) {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (db.fechaHoraOcupada(fecha, hora)) {
                AlertDialog.Builder(this)
                    .setTitle("Fecha ocupada")
                    .setMessage("La fecha y hora seleccionadas ya están ocupadas.")
                    .setPositiveButton("Aceptar") { dialog, _ -> dialog.dismiss() }
                    .show()
            } else {
                if (checkSmsPermission()) {
                    val id = db.insertarCita(nombre, fecha, hora, tipo, notas, telefono)
                    if (id != -1L) {
                        programarRecordatorio(id.toInt(), nombre, fecha, hora, telefono, tipo)
                        cargarCitas()
                        limpiarCampos(edtNombre, edtTelefono, edtFecha, edtHora, edtNotas)
                        Toast.makeText(this, "Cita Guardada y Recordatorio Automático Programado", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    requestSmsPermission()
                }
            }
        }
        cargarCitas()

        requestInitialPermissions()
    }

    private fun checkSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestSmsPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 102)
    }

    private fun requestInitialPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
             if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.SEND_SMS)
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 101)
        }
    }

    private fun limpiarCampos(vararg fields: EditText) {
        fields.forEach { it.text.clear() }
    }

    private fun cargarCitas() {
        recycler.adapter = CitaAdapter(db.obtenerCitas(), db) { cargarCitas() }
    }

    private fun programarRecordatorio(id: Int, nombre: String, fecha: String, hora: String, telefono: String, tipo: String) {
        val sdf = SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault())
        val date = sdf.parse("$fecha $hora") ?: return
        
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.HOUR_OF_DAY, -24)

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            return
        }

        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("citaId", id)
            putExtra("nombre", nombre)
            putExtra("fecha", fecha)
            putExtra("hora", hora)
            putExtra("telefono", telefono)
            putExtra("tipo", tipo)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }
}
