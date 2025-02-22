package app.cita.administrarcitas

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import app.cita.administrarcitas.databinding.FragmentCitaBinding
import com.example.appcitas.model.Cita
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID


class CitaFragment : Fragment() {
    private lateinit var binding: FragmentCitaBinding
    private lateinit var selectedOption: String
    private lateinit var selectedDate: String
    private lateinit var selectedTime: String
   private lateinit var auth :FirebaseAuth
    private var selectedColor: Int = Color.BLACK
    private lateinit var reference: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCitaBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reference = FirebaseDatabase.getInstance().getReference("Citas")
        auth=FirebaseAuth.getInstance()
        // Configurar el botón para mostrar el diálogo de selección de fecha
        binding.BTNHORA.setOnClickListener {
            val dialogo = TimeDialogo()
            dialogo.show(parentFragmentManager, "timePicker")
            dialogo.listener = { hour, minute ->
                selectedTime = "$hour:$minute"
                binding.TITULOHORA.setText(selectedTime)
            }


        }
        // Configurar el botón para mostrar el diálogo de selección de fecha
        binding.BTNFECHA.setOnClickListener {
            val dialogo = DateDialogo()
            dialogo.show(parentFragmentManager, "datePicker")
            dialogo.listener = { year, month, dayOfMonth ->
                selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                binding.TITULOOFECHA.setText(selectedDate)
            }
        }
        // Configurar el botón para mostrar el diálogo de selección de sexo
        seleccionarOpcion()
        binding.btnRegistrar.setOnClickListener {
            registarCita()

        }
        binding.btnSelectColor.setOnClickListener {
            showColorPickerDialog()
        }
    }


    private fun seleccionarOpcion() {
        val options = listOf("masculino", "femenino")

        // Verificar que el binding está inicializado correctamente
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.mySpinner.adapter = adapter

        binding.mySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Obtener la opción seleccionada
                selectedOption = parent?.getItemAtPosition(position).toString()

                // Mostrar un mensaje con la opción seleccionada
                Toast.makeText(
                    requireContext(),
                    "Seleccionaste: $selectedOption",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Manejar el caso donde no se selecciona nada (opcional)
            }
        }
    }

    private fun showColorPickerDialog() {
        val colors = arrayOf("Rojo", "Verde", "Azul", "Amarillo", "Negro")
        val colorValues = arrayOf(
            Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.BLACK,
            Color.WHITE, Color.CYAN, Color.MAGENTA, Color.GRAY, Color.DKGRAY,
            Color.LTGRAY, Color.TRANSPARENT
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Selecciona un color")
            .setItems(colors) { _, which ->
                selectedColor = colorValues[which]
                Toast.makeText(requireContext(), selectedColor.toString(), Toast.LENGTH_SHORT)
                    .show()
                binding.selectedColorView.setTextColor(selectedColor)
                binding.selectedColorView.text = "Color seleccionado: ${colors[which]}"
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun registarCita() {
        val nombre = binding.nombreUsuario.text.toString()
        val apellido = binding.apellidoUsuario.text.toString()
        val email = binding.email.text.toString()
        val telefono = binding.telefono.text.toString()
        val fecha = selectedDate
        val hora = selectedTime
        val sexo: String = selectedOption


        if (nombre.isNotEmpty() && apellido.isNotEmpty() && email.isNotEmpty()
            && telefono.isNotEmpty() && fecha.isNotEmpty() && hora.isNotEmpty()
            && sexo.isNotEmpty()
        ) {
            val random = UUID.randomUUID().toString()

            val cita = Cita(
               auth.currentUser?.uid?:random, nombre, email, apellido, fecha, hora, sexo,
                String.format("#%08X", (selectedColor)), telefono,"PENDIENTE",true,random
            )
            reference.child(random).setValue(cita).addOnSuccessListener {
                binding.nombreUsuario.text?.clear()
                binding.apellidoUsuario.text?.clear()
                binding.email.text?.clear()
                binding.telefono.text?.clear()
                Toast.makeText(requireContext(), "Cita registrada exitosamente", Toast.LENGTH_SHORT)
                    .show()
                findNavController().navigateUp()


            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Error al registrar la cita", Toast.LENGTH_SHORT)
                    .show()
            }


        } else {
            Toast.makeText(
                requireContext(),
                "Por favor, completa todos los campos",
                Toast.LENGTH_SHORT
            ).show()

        }
    }


}