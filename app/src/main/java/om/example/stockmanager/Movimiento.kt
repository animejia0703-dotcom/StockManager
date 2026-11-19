package om.example.stockmanager

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "movimientos",
    indices = [Index(value = ["codigoProducto"]), Index(value = ["sincronizado"])]
)
data class Movimiento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codigoProducto: String,
    val cantidad: Int,
    val tipo: String,              // "ENTRADA" o "SALIDA"
    val fecha: String,             // formato yyyy-MM-dd HH:mm
    val sincronizado: Boolean = false
)
