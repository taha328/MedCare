package com.example.medcare.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // Example for a Cancel button
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medcare.R; // Use your R file
import com.example.medcare.model.RendezVous; // Use your RendezVous model

import java.util.Objects;

public class AppointmentAdapter extends ListAdapter<RendezVous, AppointmentAdapter.AppointmentViewHolder> {

    private final OnAppointmentActionListener listener;

    // Interface for actions (e.g., cancel)
    public interface OnAppointmentActionListener {
        void onCancelClick(RendezVous rendezVous);
        // Add more actions if needed (e.g., view details)
        // void onItemClick(RendezVous rendezVous);
    }

    // Constructor
    public AppointmentAdapter(@NonNull OnAppointmentActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false); // Create this layout
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        RendezVous current = getItem(position);
        if (current != null) {
            holder.bind(current, listener);
        }
    }

    // ViewHolder
    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView textDate, textTime, textEstablishment, textStatus;
        Button buttonCancel; // Example cancel button

        AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find views by ID from item_appointment.xml
            textDate = itemView.findViewById(R.id.item_appt_date);
            textTime = itemView.findViewById(R.id.item_appt_time);
            textEstablishment = itemView.findViewById(R.id.item_appt_establishment); // Need way to get Estab Name
            textStatus = itemView.findViewById(R.id.item_appt_status);
            buttonCancel = itemView.findViewById(R.id.item_appt_button_cancel); // Example
        }

        void bind(final RendezVous rdv, final OnAppointmentActionListener listener) {
            textDate.setText(Objects.toString(rdv.getAppointmentDate(), "N/A"));
            textTime.setText(Objects.toString(rdv.getAppointmentTime(), "N/A"));
            // Displaying establishment name requires fetching Etablissement data
            // based on rdv.getEstablishmentId() - This might be done in the Fragment/ViewModel
            // or you might store the name denormalized on the RendezVous document itself.
            textEstablishment.setText("Etab. ID: " + Objects.toString(rdv.getEstablishmentId(), "N/A")); // Placeholder
            textStatus.setText("Statut: " + Objects.toString(rdv.getStatus(), "Inconnu"));

            // Handle Cancel Button visibility and action
            if ("CANCELLED".equalsIgnoreCase(rdv.getStatus())) {
                buttonCancel.setVisibility(View.GONE); // Hide if already cancelled
            } else {
                buttonCancel.setVisibility(View.VISIBLE);
                buttonCancel.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onCancelClick(rdv);
                    }
                });
            }

            // Add general click listener if needed
            // itemView.setOnClickListener(v -> listener.onItemClick(rdv));

        }
    }

    // DiffUtil Callback
    private static final DiffUtil.ItemCallback<RendezVous> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<RendezVous>() {
                @Override
                public boolean areItemsTheSame(@NonNull RendezVous oldItem, @NonNull RendezVous newItem) {
                    return Objects.equals(oldItem.getDocumentId(), newItem.getDocumentId()); // Use Document ID
                }

                @Override
                public boolean areContentsTheSame(@NonNull RendezVous oldItem, @NonNull RendezVous newItem) {
                    // Compare relevant fields that determine if UI should update
                    return Objects.equals(oldItem.getAppointmentDate(), newItem.getAppointmentDate()) &&
                            Objects.equals(oldItem.getAppointmentTime(), newItem.getAppointmentTime()) &&
                            Objects.equals(oldItem.getStatus(), newItem.getStatus()) &&
                            Objects.equals(oldItem.getEstablishmentId(), newItem.getEstablishmentId());
                }
            };
}