# Gestor de Citas 📅

Una aplicación Android nativa desarrollada en Kotlin para la gestión eficiente de citas y recordatorios. La aplicación permite registrar citas, evitar conflictos de horario y programar recordatorios automáticos mediante notificaciones y SMS.

## ✨ Características

- **Registro de Citas:** Almacena nombre del cliente, teléfono, fecha, hora, tipo de cita y notas adicionales.
- **Validación de Disponibilidad:** El sistema verifica automáticamente si la fecha y hora seleccionadas ya están ocupadas antes de guardar.
- **Recordatorios Automáticos:** Programa una alarma 24 horas antes de la cita para notificar al usuario.
- **Integración de Comunicaciones:** 
    - Botón de llamada rápida para contactar a los clientes.
    - Envío automático de SMS de recordatorio (requiere permisos).
- **Persistencia de Datos:** Utiliza SQLite para el almacenamiento local de toda la información.
- **Interfaz Moderna:** Uso de `RecyclerView` para listar las citas y diálogos nativos (`DatePicker`, `TimePicker`) para una mejor experiencia de usuario.

## 🛠️ Tecnologías Utilizadas

- **Lenguaje:** [Kotlin](https://kotlinlang.org/)
- **Base de Datos:** SQLite (vía `SQLiteOpenHelper`)
- **Arquitectura:** Patrón Command/Listener para eventos y gestión de UI.
- **Componentes de Android:**
    - `AlarmManager` para la programación de recordatorios.
    - `BroadcastReceiver` para gestionar las notificaciones en segundo plano.
    - `RecyclerView` con adaptadores personalizados.
    - `Material Design` para los componentes de la interfaz.

## 📱 Permisos Necesarios

La aplicación solicita los siguientes permisos para su correcto funcionamiento:
- `SEND_SMS`: Para enviar el recordatorio al cliente.
- `POST_NOTIFICATIONS`: Para mostrar alertas en dispositivos con Android 13+.
- `CALL_PHONE` / `DIAL`: Para la funcionalidad de contacto rápido.

## 🚀 Instalación y Uso

1. **Clonar el repositorio** (o descargar el código).
2. **Abrir en Android Studio:** Importar el proyecto y esperar a que Gradle sincronice las dependencias.
3. **Ejecutar:** Conectar un dispositivo físico o emulador con nivel de API 24 o superior.

### Instrucciones de uso:
1. Rellena los campos de texto con la información de la cita.
2. Selecciona la fecha y hora usando los selectores emergentes.
3. Presiona **Guardar**. Si el horario está disponible, la cita aparecerá en la lista inferior y se programará un recordatorio automático.
4. Puedes usar el botón de llamada para contactar rápidamente al número predefinido o gestionar las citas existentes desde la lista.

## 📂 Estructura del Proyecto

- `MainActivity.kt`: Lógica principal y manejo de la UI.
- `DBHelper.kt`: Gestión de la base de datos SQLite.
- `CitaAdapter.kt`: Adaptador para la visualización de la lista de citas.
- `ReminderReceiver.kt`: Receptor que activa las notificaciones y SMS de recordatorio.
- `Cita.kt`: Modelo de datos.

---
Desarrollado como proyecto de gestión de servicios.
