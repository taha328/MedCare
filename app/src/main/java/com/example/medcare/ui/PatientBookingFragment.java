package com.example.medcare.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medcare.R;
import com.example.medcare.model.TimeSlot;
import com.example.medcare.viewmodel.PatientBookingViewModel;
import com.example.medcare.viewmodel.PatientBookingViewModelFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class PatientBookingFragment extends Fragment implements TimeSlotAdapter.OnSlotClickListener {

    private static final String TAG = "PatientBookingFragment";

    private CalendarView calendarView;
    private RecyclerView recyclerViewSlots;
    private ProgressBar loadingIndicator;
    private TextView labelAvailableSlots; // To show date or message

    private PatientBookingViewModel viewModel;
    private TimeSlotAdapter adapter;

    // These IDs MUST be passed to this fragment (e.g., via arguments)
    private String professionalId;
    private String establishmentId;

    // Key for arguments
    private static final String ARG_PROFESSIONAL_ID = "professionalId";
    private static final String ARG_ESTABLISHMENT_ID = "establishmentId";

    // --- Factory method to create fragment with arguments ---
    public static PatientBookingFragment newInstance(String professionalId, String establishmentId) {
        PatientBookingFragment fragment = new PatientBookingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROFESSIONAL_ID, professionalId);
        args.putString(ARG_ESTABLISHMENT_ID, establishmentId);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve arguments
        if (getArguments() != null) {
            professionalId = getArguments().getString(ARG_PROFESSIONAL_ID);
            establishmentId = getArguments().getString(ARG_ESTABLISHMENT_ID);
        } else {
            Log.e(TAG, "Error: Required IDs not provided to fragment.");
            Toast.makeText(getContext(), "Erreur: Informations manquantes.", Toast.LENGTH_LONG).show();
            // Optionally navigate back or disable fragment
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_booking, container, false); // Use correct layout name

        calendarView = view.findViewById(R.id.calendar_view_booking);
        recyclerViewSlots = view.findViewById(R.id.recycler_view_slots);
        loadingIndicator = view.findViewById(R.id.loading_indicator_booking);
        labelAvailableSlots = view.findViewById(R.id.label_available_slots); // Assign TextView

        if(professionalId == null || establishmentId == null) {
            view.setVisibility(View.GONE); // Hide if IDs are missing
            return view;
        }

        PatientBookingViewModelFactory factory = new PatientBookingViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(PatientBookingViewModel.class);

        setupRecyclerView();
        setupCalendarView();
        setupObservers();

        // Trigger load for today initially
        viewModel.setSelectedDate(LocalDate.now(), professionalId);


        return view;
    }

    private void setupRecyclerView() {
        adapter = new TimeSlotAdapter(requireContext(), this);
        // Adjust span count based on your item_timeslot layout width
        recyclerViewSlots.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerViewSlots.setAdapter(adapter);
        recyclerViewSlots.setHasFixedSize(true); // Optimization if item size doesn't change
    }

    private void setupCalendarView() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // month is 0-indexed, LocalDate uses 1-indexed
            LocalDate selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
            viewModel.setSelectedDate(selectedDate, professionalId);
        });

        // Optional: Set min/max dates if needed
        // calendarView.setMinDate(System.currentTimeMillis() - 1000);
    }

    private void setupObservers() {
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            // Optionally disable Calendar/RecyclerView during loading
        });

        viewModel.getSelectedDate().observe(getViewLifecycleOwner(), date -> {
            // Update label or title when date changes
            labelAvailableSlots.setText("Créneaux Disponibles pour: " + date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        });


        viewModel.getAvailableSlots().observe(getViewLifecycleOwner(), slots -> {
            if(slots == null || slots.isEmpty()){
                Log.d(TAG, "No slots available or returned for selected date.");
                // Show a message in place of recycler view if needed
                // For now, adapter handles empty list state
            } else {
                Log.d(TAG, "Submitting " + slots.size() + " slots to adapter.");
            }
            adapter.submitList(slots); // Submit list to adapter
        });

        viewModel.getBookingResult().observe(getViewLifecycleOwner(), resultMessage -> {
            if (resultMessage != null && !resultMessage.isEmpty()) {
                // Show result message (success or failure)
                Toast.makeText(getContext(), resultMessage, Toast.LENGTH_LONG).show();
                // Clear the message in ViewModel after showing to avoid reshowing on rotation?
                // viewModel.clearBookingResult(); // Add method in ViewModel
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), "Erreur: " + errorMessage, Toast.LENGTH_LONG).show();
                // viewModel.clearErrorMessage(); // Optional clear
            }
        });
    }


    // Implementation of the adapter's click listener interface
    @Override
    public void onSlotClick(TimeSlot slot) {
        LocalDate selectedDate = viewModel.getSelectedDate().getValue();
        if (slot == null || selectedDate == null) return;

        // Show Confirmation Dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmer Rendez-vous")
                .setMessage("Réserver le créneau de " + slot.time + " le " + selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "?")
                .setPositiveButton("Confirmer", (dialog, which) -> {
                    // User confirmed, call ViewModel to book
                    viewModel.bookAppointment(slot.time, selectedDate, professionalId, establishmentId);
                })
                .setNegativeButton("Annuler", null) // Just dismiss
                .show();
    }
}