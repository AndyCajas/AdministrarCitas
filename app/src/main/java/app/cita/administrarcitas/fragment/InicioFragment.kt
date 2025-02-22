package app.cita.administrarcitas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import app.cita.administrarcitas.databinding.FragmentInicioBinding
import com.example.appcitas.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class InicioFragment : Fragment() {

    private lateinit var binding: FragmentInicioBinding
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    var email = ""
    var password = ""

    // Firebase Auth instance
    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference

    // Google Sign-In client
    private lateinit var googleSignInClient: GoogleSignInClient
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reference = FirebaseDatabase.getInstance().getReference("users")

        binding.tvNoCuenta.setOnClickListener {
            findNavController().navigate(R.id.action_inicioFragment_to_registrarFragment)
        }
        binding.btnIniciarSesion.setOnClickListener {
            loginUser()
        }

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Configurar Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("https://console.cloud.google.com/apis/credentials?project=citas-c7622")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        binding.btnGoogle.setOnClickListener { signInWithGoogle() }

        usuarioactual()

    }

    private fun usuarioactual() {
        val currentUser = auth.currentUser?.uid
        val query = reference.orderByChild("id").equalTo(currentUser)

        if (currentUser != null) {
            // Aquí suponemos que en la base de datos cada usuario tiene un campo 'role' que puede ser "paciente" o "administrador"
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (childSnapshot in snapshot.children) {
                            val role = childSnapshot.child("rol").getValue(String::class.java)
                            Log.d("Firebase", "Rol encontrado: $role")
                            when (role.toString()) {
                                Constants.PACIENTE -> {
                                    // Redirige al fragment de paciente
                                    findNavController().navigate(R.id.action_inicioFragment_to_pacienteFragment)
                                }

                                Constants.ADMINISTRADOR -> {
                                    // Redirige al fragment de administrador
                                    findNavController().navigate(R.id.action_inicioFragment_to_doctorFragment)
                                }

                                else -> {
                                    // En caso de que no haya rol definido, podrías manejarlo con un mensaje o redirigir a otro lugar
                                    Toast.makeText(context, "Rol no encontrado", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        }
                        // Redirige según el rol

                    } else {
                        Toast.makeText(
                            requireContext(),
                            "usuario no encontrado",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejo de error si ocurre un fallo en la base de datos
                    Toast.makeText(
                        context,
                        "Error al obtener el rol del usuario",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }


    private val RC_SIGN_IN = 9001 // Código para identificar la actividad

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Log.w("GoogleSignIn", "Google sign-in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = User()
                    val currentUser = auth.currentUser
                    currentUser?.let {
                        user.id = it.uid
                        user.nombre = it.displayName ?: ""
                        user.email = it.email ?: ""
                        user.imagen = it.photoUrl?.toString() ?: ""
                    }

                    reference.child(user.id).setValue(user).addOnSuccessListener {
                        findNavController().navigate(R.id.action_inicioFragment_to_pacienteFragment)

                    }

                } else {
                    Log.w("GoogleSignIn", "Sign-in failed", task.exception)
                }
            }
    }


    fun loginUser() {
        if (getCredentials()) {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        findNavController().navigate(R.id.action_inicioFragment_to_pacienteFragment)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Error al inciar sesión",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            Toast.makeText(requireContext(), "Error complete los campos", Toast.LENGTH_SHORT).show()
        }

    }

    private fun getCredentials(): Boolean {
        email = binding.nombreUsuarioCuenta.text.toString().trim()
        password = binding.password.text.toString().trim()
        return if (!email.isNullOrEmpty() && !password.isNullOrEmpty()) {
            true
        } else {
            false
        }
    }

}