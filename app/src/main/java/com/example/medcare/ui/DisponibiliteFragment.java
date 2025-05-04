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

import com.example.medcare.model.Disponibilite;
import com.example.medcare.model.Etablissement;
import com.example.medcare.viewmodel.DisponibiliteViewModel;
import com.example.medcare.viewmodel.EtablissementViewModel;

public class DisponibiliteFragment extends Fragment {

    private Spinner spinnerEtab;
    private EditText jourInput, debutInput, finInput;
    private Button btnAjouter;
    private LinearLayout listeContainer;

    private DisponibiliteViewModel disponibiliteVM;
    private EtablissementViewModel etablissementVM;

    private List<Etablissement> etablissements;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_disponibilite_fragment, container, false);

        spinnerEtab = view.findViewById(R.id.spinner_etab);
        jourInput = view.findViewById(R.id.input_jour);
        debutInput = view.findViewById(R.id.input_debut);
        finInput = view.findViewById(R.id.input_fin);
        btnAjouter = view.findViewById(R.id.btn_ajouter_dispo);
        listeContainer = view.findViewById(R.id.liste_dispos);

        disponibiliteVM = new ViewModelProvider(this).get(DisponibiliteViewModel.class);
        etablissementVM = new ViewModelProvider(this).get(EtablissementViewModel.class);

        etablissementVM.getAllEtablissements().observe(getViewLifecycleOwner(), etabs -> {
            etablissements = etabs;
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item,
                    getNomsEtablissements(etabs));
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerEtab.setAdapter(adapter);
        });

        btnAjouter.setOnClickListener(v -> ajouterDisponibilite());

        spinnerEtab.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                afficherDisponibilites(etablissements.get(position).getId());
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return view;
    }

    private void ajouterDisponibilite() {
        int index = spinnerEtab.getSelectedItemPosition();
        if (index == -1 || etablissements == null || etablissements.isEmpty()) return;

        int etabId = etablissements.get(index).getId();
        String jour = jourInput.getText().toString().trim();
        String debut = debutInput.getText().toString().trim();
        String fin = finInput.getText().toString().trim();

        if (TextUtils.isEmpty(jour) || TextUtils.isEmpty(debut) || TextUtils.isEmpty(fin)) {
            Toast.makeText(getContext(), "Tous les champs sont requis", Toast.LENGTH_SHORT).show();
            return;
        }

        Disponibilite d = new Disponibilite(etabId, jour, debut, fin, true); // ou false
        disponibiliteVM.insert(d);
        jourInput.setText("");
        debutInput.setText("");
        finInput.setText("");
        afficherDisponibilites(etabId);
    }

    private void afficherDisponibilites(int etabId) {
        disponibiliteVM.getDisponibilitesForEtablissement(etabId)
                .observe(getViewLifecycleOwner(), list -> {
                    listeContainer.removeAllViews();
                    for (Disponibilite d : list) {
                        TextView tv = new TextView(getContext());
                        tv.setText("• " + d.getJour() + " : " + d.getHeureDebut() + " - " + d.getHeureFin());
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
