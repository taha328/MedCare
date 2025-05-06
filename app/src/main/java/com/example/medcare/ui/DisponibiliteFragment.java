package com.example.medcare.ui; // Adjust package if needed

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner; // <<< IMPORT Spinner
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.medcare.R; // Ensure R exists

import java.util.ArrayList;
import java.util.Arrays; // <<< IMPORT Arrays
import java.util.List;

import com.example.medcare.model.Disponibilite;
import com.example.medcare.model.Etablissement;
import com.example.medcare.viewmodel.DisponibiliteViewModel;
import com.example.medcare.viewmodel.DisponibiliteViewModelFactory;
import com.example.medcare.viewmodel.EtablissementViewModel;
import com.example.medcare.viewmodel.EtablissementViewModelFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DisponibiliteFragment extends Fragment {

    private static final String TAG = "DisponibiliteFragment";

    private Spinner spinnerEtab;
    // private EditText jourInput; // <<< REMOVE this
    private Spinner spinnerJour;   // <<< ADD this
    private EditText debutInput, finInput;
    private Button btnAjouter;
    private LinearLayout listeContainer;
    private ProgressBar loadingIndicator;

    private DisponibiliteViewModel disponibiliteVM;
    private EtablissementViewModel etablissementVM;

    private List<Etablissement> etablissementsList = new ArrayList<>();
    private String currentProfessionalId;

    // <<< Define consistent Day names (MUST match PatientBookingViewModel calculation) >>>
    private static final List<String> DAYS_OF_WEEK = Arrays.asList(
            "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
            // OR Use French if Patient side calculates French:
            // "LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI", "SAMEDI", "DIMANCHE"
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_disponibilite_fragment, container, false);

        spinnerEtab = view.findViewById(R.id.spinner_etab);
        spinnerJour = view.findViewById(R.id.spinner_jour); // <<< GET Spinner
        // jourInput = view.findViewById(R.id.input_jour); // <<< REMOVE reference to EditText
        debutInput = view.findViewById(R.id.input_debut);
        finInput = view.findViewById(R.id.input_fin);
        btnAjouter = view.findViewById(R.id.btn_ajouter_dispo);
        listeContainer = view.findViewById(R.id.liste_dispos);
        loadingIndicator = view.findViewById(R.id.loading_indicator_dispo);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentProfessionalId = currentUser.getUid();
        } else {
            Log.e(TAG, "Error: Current professional user ID is null.");
            Toast.makeText(getContext(), "Erreur: Utilisateur non connecté.", Toast.LENGTH_LONG).show();
        }

        EtablissementViewModelFactory etabFactory = new EtablissementViewModelFactory();
        etablissementVM = new ViewModelProvider(requireActivity(), etabFactory).get(EtablissementViewModel.class); // Use Activity scope

        DisponibiliteViewModelFactory dispoFactory = new DisponibiliteViewModelFactory();
        disponibiliteVM = new ViewModelProvider(this, dispoFactory).get(DisponibiliteViewModel.class); // Use Fragment scope

        setupDaySpinner(); // <<< Setup the day spinner
        setupObservers();

        btnAjouter.setOnClickListener(v -> ajouterOuModifierDisponibilite()); // Changed method name slightly

        spinnerEtab.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (!etablissementsList.isEmpty() && position >= 0 && position < etablissementsList.size()) {
                    if (currentProfessionalId != null) {
                        afficherDisponibilites(currentProfessionalId);
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return view;
    }

    // <<< Method to setup the Day Spinner >>>
    private void setupDaySpinner() {
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, DAYS_OF_WEEK);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerJour.setAdapter(dayAdapter);
    }

    private void setupObservers() {
        etablissementVM.getAllEtablissementsStream().observe(getViewLifecycleOwner(), etabs -> {
            if (etabs != null) {
                // Maybe filter establishments to show only those owned by this professional?
                List<Etablissement> ownedEtablissements = new ArrayList<>();
                if (currentProfessionalId != null) {
                    for (Etablissement e : etabs) {
                        if (currentProfessionalId.equals(e.getProfessionalOwnerId())) {
                            ownedEtablissements.add(e);
                        }
                    }
                } else {
                    ownedEtablissements.addAll(etabs); // Show all if owner check fails
                }
                etablissementsList = ownedEtablissements;

                if(etablissementsList.isEmpty()) {
                    // Handle case where professional owns no establishments
                    Toast.makeText(getContext(), "Vous n'avez pas encore d'établissement.", Toast.LENGTH_LONG).show();
                    spinnerEtab.setAdapter(null);
                    listeContainer.removeAllViews(); // Clear displayed availability
                } else {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_item,
                            getNomsEtablissements(etablissementsList));
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerEtab.setAdapter(adapter);
                    // Trigger display for initially selected item
                    if(spinnerEtab.getCount() > 0) {
                        spinnerEtab.setSelection(0); // Select first one initially
                        // Re-trigger listener call to load initial availability list
                        if(currentProfessionalId != null){
                            afficherDisponibilites(currentProfessionalId);
                        }
                    }
                }


            } else {
                etablissementsList.clear();
                spinnerEtab.setAdapter(null);
                listeContainer.removeAllViews();
                Toast.makeText(getContext(), "Erreur chargement établissements.", Toast.LENGTH_SHORT).show();
            }
        });

        disponibiliteVM.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnAjouter.setEnabled(!isLoading);
            spinnerEtab.setEnabled(!isLoading);
            spinnerJour.setEnabled(!isLoading);
        });

        disponibiliteVM.getOperationSuccess().observe(getViewLifecycleOwner(), success -> { // Now observes Boolean
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(getContext(), "Disponibilité enregistrée!", Toast.LENGTH_SHORT).show();
                // Refresh list after successful operation
                if(currentProfessionalId != null) {
                    afficherDisponibilites(currentProfessionalId);
                }
            }
        });

        disponibiliteVM.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Erreur: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void ajouterOuModifierDisponibilite() { // Renamed method
        if (currentProfessionalId == null) {
            Toast.makeText(getContext(), "Erreur: Utilisateur professionnel non identifié.", Toast.LENGTH_SHORT).show();
            return;
        }

        int etabIndex = spinnerEtab.getSelectedItemPosition();
        int jourIndex = spinnerJour.getSelectedItemPosition();

        if (etabIndex == AdapterView.INVALID_POSITION || etablissementsList.isEmpty() || etabIndex >= etablissementsList.size()) {
            Toast.makeText(getContext(), "Veuillez sélectionner un établissement.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (jourIndex == AdapterView.INVALID_POSITION ) {
            Toast.makeText(getContext(), "Veuillez sélectionner un jour.", Toast.LENGTH_SHORT).show();
            return;
        }


        String etabDocumentId = etablissementsList.get(etabIndex).getDocumentId();
        if (etabDocumentId == null || etabDocumentId.isEmpty()){
            Toast.makeText(getContext(), "Erreur: ID établissement invalide.", Toast.LENGTH_SHORT).show();
            return;
        }

        // <<< GET DAY FROM SPINNER >>>
        String jour = (String) spinnerJour.getSelectedItem();
        String debut = debutInput.getText().toString().trim();
        String fin = finInput.getText().toString().trim();
        boolean ouvert = true; // Assuming adding/editing means it's open. Need a Checkbox/Switch to set this to false.

        // Basic validation (improve time validation)
        if (TextUtils.isEmpty(debut) || TextUtils.isEmpty(fin) || !debut.matches("\\d{2}:\\d{2}") || !fin.matches("\\d{2}:\\d{2}")) {
            Toast.makeText(getContext(), "Format Heure Début/Fin invalide (HH:mm).", Toast.LENGTH_SHORT).show();
            return;
        }

        // Construct Firestore object
        Disponibilite newDispo = new Disponibilite(etabDocumentId, currentProfessionalId, jour, debut, fin, ouvert);

        // Construct the specific Firestore Document ID using the *standardized* day name
        String availabilityDocId = currentProfessionalId + "_" + jour; // e.g., USER123_MONDAY

        Log.d(TAG, "Upserting Availability - DocID: " + availabilityDocId + ", EstabID: " + etabDocumentId + ", Jour: " + jour);
        disponibiliteVM.upsert(newDispo, availabilityDocId); // Upsert will create or update
    }


    // This method likely remains the same, it reads based on professional ID now
    private void afficherDisponibilites(String professionalId) {
        if (professionalId == null) return;
        disponibiliteVM.getDisponibilitesForProfessionalStream(professionalId)
                .observe(getViewLifecycleOwner(), list -> {
                    listeContainer.removeAllViews();
                    if (list != null) {
                        if(list.isEmpty()){
                            TextView tv = new TextView(getContext());
                            tv.setText("Aucune disponibilité définie.");
                            tv.setPadding(16, 8, 16, 8);
                            listeContainer.addView(tv);
                        } else {
                            // Sort list by day maybe? (Requires mapping DAYS_OF_WEEK to order)
                            for (Disponibilite d : list) {
                                TextView tv = new TextView(getContext());
                                // Find establishment name for better display
                                String etabName = findEtabNameById(d.getEtablissementId());
                                String etabInfo = (etabName != null) ? " ("+ etabName +")" : "";

                                String displayText = "• " + d.getJour() + etabInfo + " : " + d.getHeureDebut() + " - " + d.getHeureFin() + (d.isOuvert() ? "" : " (Fermé)");
                                tv.setText(displayText);
                                tv.setTextSize(16f);
                                tv.setPadding(16, 8, 16, 8);
                                // Add delete button here maybe?
                                listeContainer.addView(tv);
                            }
                        }
                    } else {
                        TextView tv = new TextView(getContext());
                        tv.setText("Erreur au chargement des disponibilités.");
                        listeContainer.addView(tv);
                    }
                });
    }

    private List<String> getNomsEtablissements(List<Etablissement> etabs) {
        List<String> noms = new ArrayList<>();
        if(etabs == null) return noms;
        for (Etablissement etab : etabs) {
            if(etab != null && etab.getNom() != null) {
                noms.add(etab.getNom());
            }
        }
        return noms;
    }

    private String findEtabNameById(String docId) {
        if(docId == null || docId.isEmpty() || etablissementsList == null) return null;
        for (Etablissement etab : etablissementsList) {
            if (docId.equals(etab.getDocumentId())) {
                return etab.getNom();
            }
        }
        return null; // Not found in the current list
    }

    // Removed clearInputFields as we keep spinner selections mostly
}