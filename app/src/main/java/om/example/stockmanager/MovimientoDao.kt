package om.example.stockmanager

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/** DAO con las operaciones CRUD y las consultas optimizadas. */
@Dao
interface MovimientoDao {

    // CREATE
    @Insert
    suspend fun insertar(movimiento: Movimiento): Long

    // READ
    @Query("SELECT * FROM movimientos ORDER BY id DESC LIMIT :limite")
    suspend fun obtenerRecientes(limite: Int): List<Movimiento>

    @Query("SELECT * FROM movimientos WHERE codigoProducto = :codigo ORDER BY id DESC")
    suspend fun buscarPorCodigo(codigo: String): List<Movimiento>

    // UPDATE
    @Update
    suspend fun actualizar(movimiento: Movimiento): Int

    // DELETE
    @Delete
    suspend fun eliminar(movimiento: Movimiento): Int

    // Consultas para la sincronización
    @Query("SELECT * FROM movimientos WHERE sincronizado = 0")
    suspend fun obtenerPendientes(): List<Movimiento>

    @Query("SELECT COUNT(*) FROM movimientos WHERE sincronizado = 0")
    suspend fun contarPendientes(): Int

    @Query("UPDATE movimientos SET sincronizado = 1 WHERE id IN (:ids)")
    suspend fun marcarSincronizados(ids: List<Long>)
}
