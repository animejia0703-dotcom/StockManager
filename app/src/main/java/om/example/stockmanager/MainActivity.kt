package om.example.stockmanager

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var dao: MovimientoDao
    private lateinit var sync: SyncManager
    private lateinit var adapter: MovimientoAdapter

    private lateinit var etCodigo: EditText
    private lateinit var etCantidad: EditText
    private lateinit var rbEntrada: RadioButton
    private lateinit var btnGuardar: Button
    private lateinit var tvPendientes: TextView

    private var editando: Movimiento? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dao = AppDatabase.getInstance(this).movimientoDao()
        sync = SyncManager(dao)

        etCodigo = findViewById(R.id.etCodigo)
        etCantidad = findViewById(R.id.etCantidad)
        rbEntrada = findViewById(R.id.rbEntrada)
        btnGuardar = findViewById(R.id.btnGuardar)
        tvPendientes = findViewById(R.id.tvPendientes)

        adapter = MovimientoAdapter(
            onClick = { cargarParaEditar(it) },
            onLongClick = { confirmarEliminar(it) }
        )
        val rv = findViewById<RecyclerView>(R.id.rvMovimientos)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        btnGuardar.setOnClickListener { guardar() }
        findViewById<Button>(R.id.btnBuscar).setOnClickListener { buscar() }
        findViewById<Button>(R.id.btnTodos).setOnClickListener { refrescar() }
        findViewById<Button>(R.id.btnSync).setOnClickListener { sincronizar() }

        refrescar()
    }

    private fun guardar() {
        val codigo = etCodigo.text.toString().trim()
        val cantidad = etCantidad.text.toString().toIntOrNull()
        if (codigo.isEmpty() || cantidad == null || cantidad <= 0) {
            Toast.makeText(this, "Ingresa un código y una cantidad válida", Toast.LENGTH_SHORT).show()
            return
        }
        val tipo = if (rbEntrada.isChecked) "ENTRADA" else "SALIDA"

        lifecycleScope.launch {
            val actual = editando
            if (actual == null) {
                val fecha = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                dao.insertar(Movimiento(codigoProducto = codigo, cantidad = cantidad, tipo = tipo, fecha = fecha))
                Toast.makeText(this@MainActivity, "Movimiento guardado", Toast.LENGTH_SHORT).show()
            } else {
                // Al editar vuelve a quedar pendiente de sincronizar
                dao.actualizar(actual.copy(codigoProducto = codigo, cantidad = cantidad, tipo = tipo, sincronizado = false))
                Toast.makeText(this@MainActivity, "Movimiento actualizado", Toast.LENGTH_SHORT).show()
            }
            limpiarFormulario()
            refrescar()
        }
    }

    private fun refrescar() {
        lifecycleScope.launch {
            adapter.actualizar(dao.obtenerRecientes(100))
            tvPendientes.text = "Pendientes de sincronizar: ${dao.contarPendientes()}"
        }
    }

    private fun buscar() {
        val codigo = etCodigo.text.toString().trim()
        if (codigo.isEmpty()) {
            Toast.makeText(this, "Escribe el código a buscar", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch { adapter.actualizar(dao.buscarPorCodigo(codigo)) }
    }

    private fun cargarParaEditar(m: Movimiento) {
        editando = m
        etCodigo.setText(m.codigoProducto)
        etCantidad.setText(m.cantidad.toString())
        if (m.tipo == "ENTRADA") rbEntrada.isChecked = true
        else findViewById<RadioButton>(R.id.rbSalida).isChecked = true
        btnGuardar.text = "Actualizar"
    }

    private fun confirmarEliminar(m: Movimiento) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar movimiento")
            .setMessage("¿Eliminar el movimiento de ${m.codigoProducto}?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    dao.eliminar(m)
                    refrescar()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun sincronizar() {
        if (!sync.hayConexion(this)) {
            Toast.makeText(this, "Sin conexión: los datos siguen guardados en el dispositivo", Toast.LENGTH_LONG).show()
            return
        }
        lifecycleScope.launch {
            val n = sync.sincronizar()
            Toast.makeText(this@MainActivity, "Sincronizados: $n", Toast.LENGTH_SHORT).show()
            refrescar()
        }
    }

    private fun limpiarFormulario() {
        editando = null
        etCodigo.text.clear()
        etCantidad.text.clear()
        rbEntrada.isChecked = true
        btnGuardar.text = "Guardar"
    }
}

