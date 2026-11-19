package om.example.stockmanager

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.delay

/**
 * Sincroniza los movimientos pendientes con el servidor central.
 * El servidor se simula con una espera; en un sistema real aquí iría la llamada a la API.
 */
class SyncManager(private val dao: MovimientoDao) {

    fun hayConexion(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /** Devuelve cuántos registros se sincronizaron. */
    suspend fun sincronizar(): Int {
        val pendientes = dao.obtenerPendientes()
        if (pendientes.isEmpty()) return 0
        delay(1500) // simulación del envío al servidor central
        // chunked evita superar el límite de variables de SQLite en el IN (...)
        pendientes.map { it.id }.chunked(500).forEach { dao.marcarSincronizados(it) }
        return pendientes.size
    }
}