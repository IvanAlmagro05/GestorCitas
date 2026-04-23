package com.example.gestorcitas

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper (context: Context):
        SQLiteOpenHelper(context, "citas.db", null, 3){
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE citas(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nombre TEXT,
            fecha TEXT,
            hora TEXT,
            tipo TEXT,
            notas TEXT,
            telefono TEXT
            )
            """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE citas ADD COLUMN telefono TEXT")
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE citas ADD COLUMN nombre TEXT")
        }
    }

    fun insertarCita(nombre: String, fecha:String, hora: String, tipo: String, notas: String, telefono: String): Long {
        val values = ContentValues().apply {
            put("nombre", nombre)
            put("fecha",fecha)
            put("hora",hora)
            put("tipo", tipo)
            put ("notas", notas)
            put("telefono", telefono)
        }

        return writableDatabase.insert("citas",null,values)
    }

    fun obtenerCitas(): MutableList<Cita>{
        val lista = mutableListOf<Cita>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM citas",null)
        
        if (cursor.moveToFirst()) {
            val idIdx = cursor.getColumnIndex("id")
            val nombreIdx = cursor.getColumnIndex("nombre")
            val fechaIdx = cursor.getColumnIndex("fecha")
            val horaIdx = cursor.getColumnIndex("hora")
            val tipoIdx = cursor.getColumnIndex("tipo")
            val notasIdx = cursor.getColumnIndex("notas")
            val telefonoIdx = cursor.getColumnIndex("telefono")

            do {
                lista.add(
                    Cita(
                        if (idIdx != -1) cursor.getInt(idIdx) else 0,
                        if (nombreIdx != -1) cursor.getString(nombreIdx) ?: "" else "",
                        if (fechaIdx != -1) cursor.getString(fechaIdx) ?: "" else "",
                        if (horaIdx != -1) cursor.getString(horaIdx) ?: "" else "",
                        if (tipoIdx != -1) cursor.getString(tipoIdx) ?: "" else "",
                        if (notasIdx != -1) cursor.getString(notasIdx) ?: "" else "",
                        if (telefonoIdx != -1) cursor.getString(telefonoIdx) ?: "" else ""
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    fun borrarCita(id:Int){
        writableDatabase.delete("citas","id=?", arrayOf(id.toString()))
    }

    fun fechaHoraOcupada(fecha: String, hora:String): Boolean{
        val db= readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM citas WHERE fecha = ? AND hora = ?",
            arrayOf(fecha,hora)
        )

        val existe = cursor.count>0
        cursor.close()
        return existe

    }
}
