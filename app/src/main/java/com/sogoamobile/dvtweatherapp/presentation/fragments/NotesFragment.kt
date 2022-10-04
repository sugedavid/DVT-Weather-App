package com.sogoamobile.dvtweatherapp.presentation.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.sogoamobile.dvtweatherapp.R
import com.sogoamobile.dvtweatherapp.adapter.NotesAdapter
import com.sogoamobile.dvtweatherapp.data.notes.Notes
import com.sogoamobile.dvtweatherapp.data.notes.NotesViewModel
import com.sogoamobile.dvtweatherapp.databinding.FragmentNotesBinding
import java.text.SimpleDateFormat
import java.util.*


class NotesFragment : Fragment() {
    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!

    private lateinit var notesViewModel: NotesViewModel

    private var dbLat = "0.0"
    private var dbLng = "0.0"
    private val notesText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            dbLat = it.getString(WeatherInfoFragment.lat).toString()
            dbLng = it.getString(WeatherInfoFragment.long).toString()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNotesBinding.inflate(inflater, container, false)

        // RecyclerView
        val adapter = NotesAdapter(requireContext())
        val recyclerView = binding.recyclerNotes
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        val divider = DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
        divider.setDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.custom_divider
            )!!
        )
        recyclerView.addItemDecoration(divider)

        // NotesViewModel
        notesViewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
        notesViewModel.readAllData.observe(viewLifecycleOwner, androidx.lifecycle.Observer { notes ->
            adapter.setData(notes)
        })

        // home tab
        val homeTab = binding.notesAppbar.cdvHome
        homeTab.setOnClickListener {
            val action = NotesFragmentDirections.actionNotesFragmentToHomeFragment()
            findNavController().navigate(action)
        }

        // weather info tab
        val weatherInfoTab = binding.notesAppbar.cdvWeatherInfo
        weatherInfoTab.setOnClickListener {
            val action = NotesFragmentDirections.actionNotesFragmentToWeatherFragment(lat = dbLat, long = dbLng)
            findNavController().navigate(action)
        }

        // add notes
        val addNotesBtn = binding.imgAddNotes
        addNotesBtn.setOnClickListener {

            //dialog
            val builder = AlertDialog.Builder(requireActivity())
            val input = EditText(requireActivity())
            input.setHint(R.string.hint_note)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)
//            builder.setView(inflater.inflate(R.layout.dialog_notes, null))
                .setMessage(R.string.dialog_message)
                .setPositiveButton(
                    R.string.save
                ) { dialog, _ ->
                    val notesText = input.text.toString()
                    // save note
                    insertDataToDatabase(notesText)
                    dialog.cancel()
                }
                .setNegativeButton(
                    R.string.cancel
                ) { dialog, _ ->
                    // User cancelled the dialog
                    dialog.cancel()
                }
            // Create the AlertDialog object and return it
            builder.show()
        }

        return binding.root
    }

    private fun insertDataToDatabase(notesText: String) {
        val sdf = SimpleDateFormat("MMM dd, yyyy hh:mm")
        val currentDate = sdf.format(Date())

        if(notesText.isNotEmpty()){
            // create Notes Object
            val notes = Notes(0, notesText, currentDate)
            // add data to database
            notesViewModel.addNote(notes)
            Snackbar.make(requireActivity().findViewById(android.R.id.content), R.string.note_added, Snackbar.LENGTH_SHORT).show()

        }else{
            Snackbar.make(requireActivity().findViewById(android.R.id.content), R.string.write_note, Snackbar.LENGTH_SHORT).show()
        }
    }

}