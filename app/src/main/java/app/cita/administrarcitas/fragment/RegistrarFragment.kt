package app.cita.administrarcitas

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import app.cita.administrarcitas.databinding.FragmentRegistrarBinding
import com.bumptech.glide.Glide
import com.example.appcitas.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID


class RegistrarFragment : Fragment() {

    private lateinit var binding: FragmentRegistrarBinding
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val user by lazy { User() }
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var selectedOption: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegistrarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("users")
        binding.btnRegistrar.setOnClickListener {
            registerUser()
        }
        binding.buttonseleccionarimagen.setOnClickListener { buscarImagen() }

        seleccionarOpcion()
    }


    private fun seleccionarOpcion() {
        val options = listOf("doctor", "paciente")

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
                //Toast.makeText(requireContext(), "Seleccionaste: $selectedOption", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Manejar el caso donde no se selecciona nada (opcional)
            }
        }
    }


    private fun buscarImagen() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        register.launch(intent)
    }

    val register = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val intent = it.data
            val img = intent?.data
            Glide.with(this).load(img).into(binding.circleImageView)
            user.imagen = img.toString()

        }
    }

    fun registerUser() {
        if (validarEntradaDatos() != null) {
            binding.progress.visibility = View.VISIBLE
            firebaseAuth.createUserWithEmailAndPassword(user.email, user.password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val id = firebaseAuth.currentUser?.uid ?: UUID.randomUUID().toString()

                        user.id = id
                        println(user)
                        databaseReference.child(id).setValue(user).addOnSuccessListener {
                            findNavController().navigate(R.id.action_registrarFragment_to_pacienteFragment)
                        }.addOnFailureListener {

                            Toast.makeText(
                                requireContext(),
                                "Error al crear el usuario ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }


                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Error al crear el usuario ${task.exception}",
                            Toast.LENGTH_SHORT
                        ).show()
                        println("error $task.exception")
                    }
                }.addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Error al crear el usuario${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

    }

    private fun validarEntradaDatos(): User? {
        val nombre = binding.nombreUsuario.text.toString().trim()
        val apellido = binding.apellidoUsuario.text.toString().trim()
        val email = binding.email.text.toString().trim()
        val password = binding.password.text.toString().trim()
        return if (!nombre.isNullOrEmpty() && !apellido.isNullOrEmpty() &&
            !email.isNullOrEmpty() && !password.isNullOrEmpty()
        ) {
            user.nombre = nombre
            user.apellido = apellido
            user.email = email
            user.password = password
            user.rol=Constants.PACIENTE
            user
        } else {
            null
        }

    }

}