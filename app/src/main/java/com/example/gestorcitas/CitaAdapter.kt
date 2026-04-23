package com.example.gestorcitas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class CitaAdapter (
    private val citas: MutableList<Cita>,
    private val db: DBHelper,
    private val refrescar: () -> Unit
): RecyclerView.Adapter<CitaAdapter.ViewHolder>(){
    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val nombre: TextView = view.findViewById(R.id.txtNombre)
        val fechaHora: TextView = view.findViewById(R.id.txtFechaHora)
        val tipo: TextView = view.findViewById(R.id.txtTipo)
        val telefono: TextView = view.findViewById(R.id.txtTelefono)
        val notas: TextView = view.findViewById(R.id.txtNotas)
        val eliminar: Button = view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cita, parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount() = citas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cita = citas[position]

        holder.nombre.text = cita.nombre
        holder.fechaHora.text = "${cita.fecha} - ${cita.hora}"
        holder.tipo.text = "Tipo: ${cita.tipo}"
        holder.telefono.text = "Tel: ${cita.telefono}"
        holder.notas.text = "Notas: ${cita.notas}"

        holder.eliminar.setOnClickListener {
            db.borrarCita (cita.id)
            refrescar()
        }
    }
}