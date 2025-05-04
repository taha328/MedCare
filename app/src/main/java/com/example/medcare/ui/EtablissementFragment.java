package com.example.medcare.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.medcare.R;

import java.util.List;

import com.example.medcare.model.Etablissement;
import com.example.medcare.viewmodel.EtablissementViewModel;

public class EtablissementFragment extends Fragment {

    private EditText nomInput, adresseInput, contactInput, emailInput,
            responsableInput, typeInput, siteWebInput, imageUrlInput;
    private Button ajouterBtn;
    private LinearLayout listeContainer;

    private EtablissementViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_etablissement_fragment, container, false);

        nomInput = view.findViewById(R.id.input_nom);
        adresseInput = view.findViewById(R.id.input_adresse);
        contactInput = view.findViewById(R.id.input_contact);
        emailInput = view.findViewById(R.id.input_email);
        responsableInput = view.findViewById(R.id.input_responsable);
        typeInput = view.findViewById(R.id.input_type);
        siteWebInput = view.findViewById(R.id.input_siteweb);
        imageUrlInput = view.findViewById(R.id.input_imageurl);
        ajouterBtn = view.findViewById(R.id.btn_ajouter);
        listeContainer = view.findViewById(R.id.liste_etablissements);

        viewModel = new ViewModelProvider(this).get(EtablissementViewModel.class);

        ajouterBtn.setOnClickListener(v -> ajouterEtablissement());

        viewModel.getAllEtablissements().observe(getViewLifecycleOwner(), this::afficherListe);

        return view;
    }

    private void ajouterEtablissement() {
        String nom = nomInput.getText().toString().trim();
        String adresse = adresseInput.getText().toString().trim();
        String telephone = contactInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String responsable = responsableInput.getText().toString().trim();
        String type = typeInput.getText().toString().trim();
        String siteWeb = siteWebInput.getText().toString().trim();
        String imageUrl = imageUrlInput.getText().toString().trim();

        if (TextUtils.isEmpty(nom) || TextUtils.isEmpty(adresse) || TextUtils.isEmpty(telephone)
                || TextUtils.isEmpty(email) || TextUtils.isEmpty(responsable)
                || TextUtils.isEmpty(type) || TextUtils.isEmpty(siteWeb) || TextUtils.isEmpty(imageUrl)) {
            Toast.makeText(getContext(), "Tous les champs sont obligatoires", Toast.LENGTH_SHORT).show();
            return;
        }

        Etablissement e = new Etablissement(nom, adresse, telephone, email, responsable, type, siteWeb, imageUrl);
        viewModel.insert(e);
        viderChamps();
    }

    private void viderChamps() {
        nomInput.setText("");
        adresseInput.setText("");
        contactInput.setText("");
        emailInput.setText("");
        responsableInput.setText("");
        typeInput.setText("");
        siteWebInput.setText("");
        imageUrlInput.setText("");
    }

    private void afficherListe(List<Etablissement> etablissements) {
        listeContainer.removeAllViews();
        for (Etablissement e : etablissements) {
            TextView tv = new TextView(getContext());
            tv.setText("• " + e.getNom() + " | " + e.getType() + " | " + e.getAdresse() + " | " + e.getTelephone());
            tv.setPadding(16, 8, 16, 8);
            listeContainer.addView(tv);
        }
    }
}
