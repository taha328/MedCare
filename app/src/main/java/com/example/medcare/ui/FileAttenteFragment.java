package com.example.medcare.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.medcare.R;

import java.util.List;

import com.example.medcare.model.Etablissement;
import com.example.medcare.model.FileAttente;
import com.example.medcare.viewmodel.EtablissementViewModel;
import com.example.medcare.viewmodel.FileAttenteViewModel;

public class FileAttenteFragment extends Fragment {

    private Spinner spinnerEtab;
    private EditText nomInput, prenomInput, heureInput, motifInput, statutInput;
    private Button btnAjouter;
    private LinearLayout listeContainer;

    private EtablissementViewModel etablissementVM;
    private FileAttenteViewModel fileVM;

    private List<Etablissement> etablissements;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_file_attente_fragment, container, false);

        spinnerEtab = view.findViewById(R.id.spinner_etab_file);
        nomInput = view.findViewById(R.id.input_nom_client);
        prenomInput = view.findViewById(R.id.input_prenom_client);
        heureInput = view.findViewById(R.id.input_heure_arrivee);
        motifInput = view.findViewById(R.id.input_motif);
        statutInput = view.findViewById(R.id.input_statut);
        btnAjouter = view.findViewById(R.id.btn_ajouter_client);
        listeContainer = view.findViewById(R.id.liste_file_attente);

        etablissementVM = new ViewModelProvider(this).get(EtablissementViewModel.class);
        fileVM = new ViewModelProvider(this).get(FileAttenteViewModel.class);

        etablissementVM.getAllEtablissements().observe(getViewLifecycleOwner(), etabs -> {
            etablissements = etabs;
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item,
                    getNomsEtablissements(etabs));
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerEtab.setAdapter(adapter);
        });

        btnAjouter.setOnClickListener(v -> ajouterClient());

        spinnerEtab.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                afficherFile(etablissements.get(position).getId());
            }

            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return view;
    }

    private void ajouterClient() {
        int index = spinnerEtab.getSelectedItemPosition();
        if (index == -1 || etablissements == null || etablissements.isEmpty()) return;

        int etabId = etablissements.get(index).getId();
        String nom = nomInput.getText().toString().trim();
        String prenom = prenomInput.getText().toString().trim();
        String heure = heureInput.getText().toString().trim();
        String motif = motifInput.getText().toString().trim();
        String statut = statutInput.getText().toString().trim();

        if (TextUtils.isEmpty(nom) || TextUtils.isEmpty(prenom) ||
                TextUtils.isEmpty(heure) || TextUtils.isEmpty(motif) || TextUtils.isEmpty(statut)) {
            Toast.makeText(getContext(), "Tous les champs sont requis", Toast.LENGTH_SHORT).show();
            return;
        }

        FileAttente client = new FileAttente(etabId, nom, prenom, heure, motif, statut);
        fileVM.insert(client);
        nomInput.setText("");
        prenomInput.setText("");
        heureInput.setText("");
        motifInput.setText("");
        statutInput.setText("");
        afficherFile(etabId);
    }

    private void afficherFile(int etabId) {
        fileVM.getFileForEtablissement(etabId)
                .observe(getViewLifecycleOwner(), list -> {
                    listeContainer.removeAllViews();
                    int pos = 1;
                    for (FileAttente f : list) {
                        TextView tv = new TextView(getContext());
                        tv.setText(pos++ + ". " + f.getNomPersonne() + " " + f.getPrenomPersonne()
                                + " - " + f.getMotif() + " (" + f.getStatut() + ")");
                        tv.setPadding(16, 8, 16, 8);
                        listeContainer.addView(tv);
                    }
                });
    }

    private String[] getNomsEtablissements(List<Etablissement> etabs) {
        String[] noms = new String[etabs.size()];
        for (int i = 0; i < etabs.size(); i++) {
            noms[i] = etabs.get(i).getNom();
        }
        return noms;
    }
}
