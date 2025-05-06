
package com.example.medcare.ui; // Adjust if needed

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil; // For better list updates
import androidx.recyclerview.widget.ListAdapter; // Use ListAdapter for DiffUtil
import androidx.recyclerview.widget.RecyclerView;

import com.example.medcare.R; // Ensure R exists
import com.example.medcare.model.Etablissement;

import java.util.ArrayList;
import java.util.Objects;

// Use ListAdapter for automatic diffing and smoother updates
public class PatientEstablishmentAdapter extends ListAdapter<Etablissement, PatientEstablishmentAdapter.EtablissementViewHolder> {

    private final OnEtablissementClickListener clickListener;

    // Interface for click events
    public interface OnEtablissementClickListener {
        void onEtablissementClick(Etablissement etablissement);
    }

    // Constructor
    public PatientEstablishmentAdapter(@NonNull OnEtablissementClickListener listener) {
        super(DIFF_CALLBACK); // Pass the DiffUtil callback to the superclass
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public EtablissementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout (item_patient_establishment.xml)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patient_establishment, parent, false);
        return new EtablissementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EtablissementViewHolder holder, int position) {
        Etablissement current = getItem(position); // Get item from ListAdapter
        if (current != null) {
            holder.bind(current, clickListener);
        }
    }

    // ViewHolder class
    static class EtablissementViewHolder extends RecyclerView.ViewHolder {
        TextView textNom, textType, textAdresse;

        EtablissementViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find views by ID from item_patient_establishment.xml
            textNom = itemView.findViewById(R.id.item_etab_patient_name);
            textType = itemView.findViewById(R.id.item_etab_patient_type);
            textAdresse = itemView.findViewById(R.id.item_etab_patient_address);
        }

        void bind(final Etablissement etablissement, final OnEtablissementClickListener listener) {
            // Set text safely, providing defaults if null
            textNom.setText(Objects.toString(etablissement.getNom(), "Nom non disponible"));
            textType.setText(Objects.toString(etablissement.getType(), "Type non spécifié"));
            textAdresse.setText(Objects.toString(etablissement.getAdresse(), "Adresse non disponible"));

            // Set the click listener on the whole item view
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEtablissementClick(etablissement);
                }
            });
        }
    }

    // DiffUtil Callback for efficient list updates
    private static final DiffUtil.ItemCallback<Etablissement> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Etablissement>() {
                @Override
                public boolean areItemsTheSame(@NonNull Etablissement oldItem, @NonNull Etablissement newItem) {
                    // Check if items represent the same object (using unique ID)
                    return Objects.equals(oldItem.getDocumentId(), newItem.getDocumentId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Etablissement oldItem, @NonNull Etablissement newItem) {
                    // Check if item content has changed
                    return Objects.equals(oldItem.getNom(), newItem.getNom()) &&
                            Objects.equals(oldItem.getType(), newItem.getType()) &&
                            Objects.equals(oldItem.getAdresse(), newItem.getAdresse());
                    // Add other fields if their changes should trigger a rebind
                }
            };
}