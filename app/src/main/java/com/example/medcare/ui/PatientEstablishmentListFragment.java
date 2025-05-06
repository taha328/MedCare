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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medcare.R;
import com.example.medcare.model.Etablissement;

import com.example.medcare.viewmodel.EtablissementViewModel;
import com.example.medcare.viewmodel.EtablissementViewModelFactory;

import java.util.ArrayList;

public class PatientEstablishmentListFragment extends Fragment implements PatientEstablishmentAdapter.OnEtablissementClickListener {

    private static final String TAG = "PatientEstabListFrag";

    private RecyclerView recyclerViewEstablishments;
    private ProgressBar loadingIndicator;
    private TextView emptyTextView;
    private EtablissementViewModel etablissementVM;
    private PatientEstablishmentAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout defined below (fragment_patient_establishment_list.xml)
        View view = inflater.inflate(R.layout.fragment_patient_establishment_list, container, false);

        recyclerViewEstablishments = view.findViewById(R.id.recycler_view_patient_etab_list);
        loadingIndicator = view.findViewById(R.id.loading_indicator_patient_etab); // Make sure this ID exists in XML
        emptyTextView = view.findViewById(R.id.text_view_patient_etab_empty);   // Make sure this ID exists in XML

        setupRecyclerView();

        // Initialize ViewModel using its factory
        EtablissementViewModelFactory factory = new EtablissementViewModelFactory();
        // Use requireActivity() or 'this' depending on ViewModel scope needed
        etablissementVM = new ViewModelProvider(requireActivity(), factory).get(EtablissementViewModel.class);

        setupObservers();

        return view;
    }

    private void setupRecyclerView() {
        // Initialize adapter with empty list and 'this' fragment as the listener
        adapter = new PatientEstablishmentAdapter(this);
        recyclerViewEstablishments.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewEstablishments.setAdapter(adapter);
        recyclerViewEstablishments.setHasFixedSize(true); // Optimization
    }

    private void setupObservers() {
        // Observe the stream of establishments
        etablissementVM.getAllEtablissementsStream().observe(getViewLifecycleOwner(), etablissements -> {
            if (etablissements != null) {
                adapter.submitList(etablissements); // Update the adapter
                emptyTextView.setVisibility(etablissements.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerViewEstablishments.setVisibility(etablissements.isEmpty() ? View.GONE : View.VISIBLE);
                emptyTextView.setText("Aucun établissement disponible."); // Set default empty text
            } else {
                // Handle the case where the LiveData emits null (could indicate an error)
                adapter.submitList(new ArrayList<>());
                emptyTextView.setVisibility(View.VISIBLE);
                recyclerViewEstablishments.setVisibility(View.GONE);
                emptyTextView.setText("Erreur lors du chargement.");
                Log.w(TAG, "Received null list from Etablissement stream");
            }
        });

        // Observe loading state
        etablissementVM.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if(loadingIndicator != null) {
                loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        // Observe error messages
        etablissementVM.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Erreur: " + error, Toast.LENGTH_LONG).show();
                emptyTextView.setVisibility(View.VISIBLE);
                recyclerViewEstablishments.setVisibility(View.GONE);
                emptyTextView.setText("Erreur: " + error);
            }
        });
    }


    @Override
    public void onEtablissementClick(Etablissement etablissement) {
        if (etablissement == null || etablissement.getDocumentId() == null || etablissement.getProfessionalOwnerId() == null) {
            Toast.makeText(getContext(), "Impossible d'ouvrir la réservation pour cet établissement.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Cannot navigate to booking, invalid Etablissement data: ID=" +
                    (etablissement != null ? etablissement.getDocumentId() : "null") +
                    ", OwnerID=" + (etablissement != null ? etablissement.getProfessionalOwnerId() : "null"));
            return;
        }

        String establishmentId = etablissement.getDocumentId();
        String professionalId = etablissement.getProfessionalOwnerId(); // <<< GET THE PROFESSIONAL ID HERE

        Log.d(TAG, "Etablissement clicked: " + etablissement.getNom() + ", Navigating to Booking with ProfID: " + professionalId + ", EstabID: " + establishmentId);

        // Create the PatientBookingFragment instance with the necessary arguments
        PatientBookingFragment bookingFragment = PatientBookingFragment.newInstance(
                professionalId,
                establishmentId
        );

        // Use FragmentManager to navigate
        FragmentManager fragmentManager = getParentFragmentManager(); // Use getParentFragmentManager() from Fragment
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Replace the current fragment container content with the new fragment
        transaction.replace(R.id.fragment_container, bookingFragment); // Use the container ID from MainActivity

        // Add the transaction to the back stack so the user can navigate back
        transaction.addToBackStack(null); // Passing null adds it with an auto-generated name

        // Commit the transaction
        transaction.commit();
    }
}