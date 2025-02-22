package app.cita.administrarcitas

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.cita.administrarcitas.databinding.ItemReciclerBinding
import com.example.appcitas.model.Cita

class AdapterCita : RecyclerView.Adapter<AdapterCita.CitaMedica>() {
    private val citas_lista = mutableListOf<Cita>()
    val item = object : DiffUtil.ItemCallback<Cita>() {
        override fun areItemsTheSame(oldItem: Cita, newItem: Cita): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Cita, newItem: Cita): Boolean {
            return oldItem == newItem
        }


    }

    val differ = AsyncListDiffer(this, item)

    fun setData(data: List<Cita>) {
        citas_lista.clear()
        citas_lista.addAll(data)
        notifyDataSetChanged()
    }

    var citasselecionadas: ((Cita)->Unit)? = null

    inner class CitaMedica(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemReciclerBinding.bind(itemView)
        fun render(citaMedica: Cita) {
            binding.TUBNOMBRE.text = "Nombre: ${citaMedica.nombre}"
            binding.EMAIL.text = "Email: ${citaMedica.email}"
            binding.TAPELLLIDO.text = "APELLIDO: ${citaMedica.apellido}"
            binding.telefono.text = "telefono: ${citaMedica.telefono}"
            binding.estado.text="Estado: ${citaMedica.estado}"
            binding.FECHA.text = citaMedica.fecha
            binding.HORA.text = citaMedica.hora
            val colorInt = Color.parseColor(citaMedica.color) // Convertir a entero

            binding.view.setBackgroundColor(colorInt)
            itemView.setOnClickListener {
                citasselecionadas?.invoke(citaMedica)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaMedica {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_recicler, parent, false)
        return CitaMedica(view)
    }

    override fun onBindViewHolder(holder: CitaMedica, position: Int) {
        holder.render(differ.currentList[position])
    }

    override fun getItemCount() = differ.currentList.size
}
