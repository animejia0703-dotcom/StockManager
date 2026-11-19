package om.example.stockmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import om.example.stockmanager.R

class MovimientoAdapter(
    private val onClick: (Movimiento) -> Unit,
    private val onLongClick: (Movimiento) -> Unit
) : RecyclerView.Adapter<MovimientoAdapter.VH>() {

    private var lista: List<Movimiento> = emptyList()

    fun actualizar(nueva: List<Movimiento>) {
        lista = nueva
        notifyDataSetChanged()
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val titulo: TextView = v.findViewById(R.id.tvTitulo)
        val detalle: TextView = v.findViewById(R.id.tvDetalle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_movimiento, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val m = lista[position]
        holder.titulo.text = "${m.codigoProducto}  ·  ${m.tipo}  ·  ${m.cantidad} uds"
        holder.detalle.text = "${m.fecha}  —  " + if (m.sincronizado) "Sincronizado" else "Pendiente"
        holder.itemView.setOnClickListener { onClick(m) }
        holder.itemView.setOnLongClickListener { onLongClick(m); true }
    }

    override fun getItemCount() = lista.size
}