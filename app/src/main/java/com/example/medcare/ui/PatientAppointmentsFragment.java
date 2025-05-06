package com.example.medcare.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog; // Use this for confirmation dialog
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.example.medcare.R;
import com.example.medcare.model.RendezVous;
import com.example.medcare.viewmodel.RendezVousViewModel;
import com.example.medcare.viewmodel.RendezVousViewModelFactory;
import com.example.medcare.ui.AppointmentAdapter;

import java.util.ArrayList;

public class PatientAppointmentsFragment extends Fragment implements AppointmentAdapter.OnAppointmentActionListener { // Implement listener

    private static final String TAG = "PatientApptsFragment";

    private RendezVousViewModel rendezVousViewModel;
    private RecyclerView recyclerViewMyAppointments;
    private AppointmentAdapter adapter;
    private ProgressBar loadingIndicatorMyAppts;
    private TextView textViewEmptyMyAppts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_appointments, container, false);

        recyclerViewMyAppointments = view.findViewById(R.id.recycler_view_my_appointments);
        loadingIndicatorMyAppts = view.findViewById(R.id.loading_indicator_my_appointments);
        textViewEmptyMyAppts = view.findViewById(R.id.text_view_my_appointments_empty);

        RendezVousViewModelFactory factory = new RendezVousViewModelFactory();
        rendezVousViewModel = new ViewModelProvider(this, factory).get(RendezVousViewModel.class);

        setupRecyclerView();
        setupObservers();
        loadPatientAppointmentsIfUserAvailable(); // Check user before loading

        return view;
    }

    private void setupRecyclerView() {
        adapter = new AppointmentAdapter(this); // Pass fragment as listener
        recyclerViewMyAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewMyAppointments.setAdapter(adapter);
        recyclerViewMyAppointments.setHasFixedSize(true);
    }

    private void loadPatientAppointmentsIfUserAvailable() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String patientId = currentUser.getUid();
            // Observation will trigger the data fetch via ViewModel and Repository stream
            // We just ensure the observer is set up below
            Log.d(TAG,"Setting up observer for patient appointments: " + patientId);
        } else {
            handleUserNotLoggedIn();
        }
    }

    private void handleUserNotLoggedIn() {
        Log.e(TAG, "No logged-in user found to load appointments.");
        Toast.makeText(getContext(), "Erreur: Utilisateur non connecté", Toast.LENGTH_SHORT).show();
        if(textViewEmptyMyAppts != null) {
            textViewEmptyMyAppts.setText("Veuillez vous reconnecter.");
            textViewEmptyMyAppts.setVisibility(View.VISIBLE);
        }
        if (recyclerViewMyAppointments != null) {
            recyclerViewMyAppointments.setVisibility(View.GONE);
        }
        if(adapter != null) adapter.submitList(new ArrayList<>()); // Clear adapter
    }


    private void setupObservers() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return; // No user, don't observe
        }
        String patientId = currentUser.getUid();

        rendezVousViewModel.getAppointmentsForPatient(patientId).observe(getViewLifecycleOwner(), appointments -> {
            if (appointments != null) {
                Log.d(TAG, "Observer received " + appointments.size() + " appointments.");
                adapter.submitList(appointments); // Update list using ListAdapter's submitList
                textViewEmptyMyAppts.setVisibility(appointments.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerViewMyAppointments.setVisibility(appointments.isEmpty() ? View.GONE : View.VISIBLE);
                textViewEmptyMyAppts.setText("Aucun rendez-vous programmé.");
            } else {
                Log.w(TAG, "Observer received null appointment list");
                adapter.submitList(new ArrayList<>());
                textViewEmptyMyAppts.setVisibility(View.VISIBLE);
                recyclerViewMyAppointments.setVisibility(View.GONE);
                textViewEmptyMyAppts.setText("Erreur de chargement des rendez-vous.");
            }
        });

        rendezVousViewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (loadingIndicatorMyAppts != null) {
                loadingIndicatorMyAppts.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        rendezVousViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Erreur: " + error, Toast.LENGTH_LONG).show();
                if(adapter == null || adapter.getItemCount() == 0){
                    textViewEmptyMyAppts.setVisibility(View.VISIBLE);
                    recyclerViewMyAppointments.setVisibility(View.GONE);
                    textViewEmptyMyAppts.setText("Erreur: " + error);
                }
                rendezVousViewModel.clearErrorMessage(); // Clear error after showing
            }
        });

        // Observer for results of actions like cancelling
        rendezVousViewModel.getOperationResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null && !result.isEmpty()) {
                Toast.makeText(getContext(), result, Toast.LENGTH_SHORT).show();
                rendezVousViewModel.clearOperationResult(); // Clear message after showing
                // List should update automatically via the main observer
            }
        });
    }

    @Override
    public void onCancelClick(RendezVous appointment) {
        if (appointment != null && appointment.getDocumentId() != null) {
            // Show confirmation dialog before cancelling
            new AlertDialog.Builder(requireContext())
                    .setTitle("Confirmer Annulation")
                    .setMessage("Êtes-vous sûr de vouloir annuler ce rendez-vous le "
                            + appointment.getAppointmentDate() + " à " + appointment.getAppointmentTime() + "?")
                    .setPositiveButton("Oui, Annuler", (dialog, which) -> {
                        // User confirmed, call ViewModel to cancel
                        rendezVousViewModel.cancelAppointment(appointment.getDocumentId());
                    })
                    .setNegativeButton("Non", null)
                    .show();
        } else {
            Toast.makeText(getContext(), "Impossible d'annuler: informations manquantes.", Toast.LENGTH_SHORT).show();
        }
    }
}