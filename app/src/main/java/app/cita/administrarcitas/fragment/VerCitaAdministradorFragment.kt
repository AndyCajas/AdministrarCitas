package app.cita.administrarcitas

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import app.cita.administrarcitas.databinding.FragmentVerCitaAdministradorBinding

import com.example.appcitas.model.Cita
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener


class VerCitaAdministradorFragment : Fragment() {
    private lateinit var binding: FragmentVerCitaAdministradorBinding
    private val args by navArgs<VerCitaAdministradorFragmentArgs>()
    private lateinit var reference: DatabaseReference
    private lateinit var madapter: AdapterCita
    private lateinit var query: Query

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVerCitaAdministradorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val database = FirebaseDatabase.getInstance()
        reference = database.getReference("Citas")
        val cita = args.fecha
        Toast.makeText(requireContext(), cita, Toast.LENGTH_SHORT).show()
        initRecycler()
        obtenerCita()
        // modificarCitaAdministrador()
        modificarCita()
    }

    private fun initRecycler() {
        madapter = AdapterCita()
        binding.RECYCLERPACIENTE.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = madapter
            setHasFixedSize(true)
        }
    }

    fun modificarCita() {
        madapter.citasselecionadas = { cita ->
            reference.addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (e in snapshot.children){
                        val citaLista = e.getValue(Cita::class.java)
                        if(cita.idAdministrador == citaLista?.idAdministrador){
                            
                        }

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
    }

        private fun modificarCitaAdministrador() {
            madapter.citasselecionadas = { cita ->
                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle("Modificar Estado")
                    .setMessage("Seleccione el estado de la cita")
                    .setPositiveButton("Aceptar Cita") { _, _ ->
                        obtenerIdCitaYActualizarEstado(cita.telefono, Constants.ACEPTADO)
                    }
                    .setNegativeButton("Cancelar Cita") { _, _ ->
                        obtenerIdCitaYActualizarEstado(cita.telefono, Constants.CANCELADO)
                    }
                    .setNeutralButton("Cerrar", null) // Botón para cerrar sin cambios
                    .create()

                dialog.show()
            }
        }

        // 🔥 Busca el ID real de la cita en Firebase antes de modificar el estado
        private fun obtenerIdCitaYActualizarEstado(telefono: String, nuevoEstado: String) {
            reference.orderByChild("telefono").equalTo(telefono)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (childSnapshot in snapshot.children) {
                                val citaId =
                                    childSnapshot.key  // ✅ Aquí obtenemos el ID real del nodo
                                if (citaId != null) {
                                    actualizarEstadoCita(citaId, nuevoEstado)
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Error: ID de cita no encontrado",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                break // Solo necesitamos el primer resultado
                            }
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "No se encontró la cita con este teléfono",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            requireContext(),
                            "Error en Firebase: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }

        // 🔄 Ahora sí actualizamos la cita con el ID correcto
        private fun actualizarEstadoCita(idCita: String, nuevoEstado: String) {
            reference.child(idCita).child("estado").setValue(nuevoEstado)
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "Estado actualizado a $nuevoEstado",
                        Toast.LENGTH_SHORT
                    ).show()
                    madapter.notifyDataSetChanged()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Error al actualizar el estado",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }


        private fun obtenerCita() {

            query = reference.orderByChild("fecha").equalTo(args.fecha)
            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val listaCita = mutableListOf<Cita>()
                    if (snapshot.exists()) {
                        for (e in snapshot.children) {
                            val cita = e.getValue(Cita::class.java)
                            cita?.let {
                                listaCita.add(it)
                            }
                        }
                        madapter.differ.submitList(listaCita)
                    } else {
                        Toast.makeText(requireContext(), "error: no encontrado", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })


        }


    }