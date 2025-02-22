package app.cita.administrarcitas

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.cita.administrarcitas.databinding.FragmentPacienteBinding

import com.example.appcitas.model.Cita
import com.example.appcitas.model.Delete

import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class PacienteFragment : Fragment() ,NavigationView.OnNavigationItemSelectedListener{

private lateinit var binding: FragmentPacienteBinding
private lateinit var madapter: AdapterCita
    private lateinit var reference: DatabaseReference
    private lateinit var auth:FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPacienteBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reference = FirebaseDatabase.getInstance().getReference("Citas")
        auth=FirebaseAuth.getInstance()
        binding.AGREGARCITAS.setOnClickListener {
            findNavController().navigate(R.id.action_pacienteFragment_to_citaFragment)

        }
        resycler()
        obtenerCitas()
        eliminar()
        editarCita()
        (activity as AppCompatActivity).setSupportActionBar(binding.toolBar)
        binding.navigationView.setNavigationItemSelectedListener (this)
        val toggle =ActionBarDrawerToggle(requireActivity(),binding.drawerLayout,binding.toolBar,R.string.abrir,
            R.string.cerrar)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        obtenerInformacionUsuario()

    }
    @SuppressLint("ScheduleExactAlarm")
    private fun programarNotificacion(estado: String, citaId: String) {

        val intent = Intent(requireContext(), CitaNotificationReceiver::class.java).apply {
            putExtra("estado", estado)
            putExtra("citaId", citaId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            citaId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val tiempoNotificacion = System.currentTimeMillis() + 2000 // Notificación en 2 segundos

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, tiempoNotificacion, pendingIntent)
    }

    private fun obtenerInformacionUsuario(){
        val headerView = binding.navigationView.getHeaderView(0) // Obtiene el primer header
        val profileImage = headerView.findViewById<ImageView>(R.id.image_profile)
        val headerUsername = headerView.findViewById<TextView>(R.id.user)
        val headerEmail = headerView.findViewById<TextView>(R.id.email)
        val currentUser=auth.currentUser
        currentUser?.let {
            profileImage.setImageURI(it.photoUrl)
            headerUsername.setText(it.displayName)
            headerEmail.setText(it.email)
        }
    }
    private fun editarCita() {
        madapter.citasselecionadas = {
            val bundle = Bundle().apply {
                putParcelable("cita", it)
            }
            findNavController().navigate(R.id.action_pacienteFragment_to_editarCItaFragment, bundle)
        }
    }
    private fun resycler() {
        madapter = AdapterCita()
        binding.RECYCLERPACIENTE.apply {
            adapter = madapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }
    private val estadosPrevios = mutableMapOf<String, String>()

    private fun obtenerCitas() {


        val query =reference.orderByChild("id").equalTo(auth.currentUser?.uid)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val citalis = mutableListOf<Cita>()
                if (snapshot.exists()) {
                    for (citaSnapshot in snapshot.children) {
                        val cita = citaSnapshot.getValue(Cita::class.java)
                        cita?.let {
                            citalis.add(it)

                            // Verificar si el estado ha cambiado y si es "ACEPTADO" o "CANCELADO"
                            val estadoAnterior = estadosPrevios[it.id]
                            if (estadoAnterior != it.estado && (it.estado == Constants.ACEPTADO|| it.estado == Constants.CANCELADO) ) {
                                estadosPrevios[it.id] = it.estado // Guardar nuevo estado
                                programarNotificacion(it.estado, it.id)

                                reference.child(it.id).setValue(it)
                            }
                        }
                    }
                    madapter.differ.submitList(citalis)

                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }



    private fun eliminar(){
        val delete = object : Delete(){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val cita = madapter.differ.currentList[position]
                reference.child(cita.id).removeValue()
                madapter.notifyItemRemoved(position)
            }
        }
        ItemTouchHelper(delete).attachToRecyclerView(binding.RECYCLERPACIENTE)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
       return when(item.itemId){
            else -> super.onOptionsItemSelected(item)
        }


    }
}