package com.example.medcare.model; // Or your model package

// Removed Room annotations
import com.google.firebase.firestore.DocumentId; // Optional

public class RendezVous {

    @DocumentId // Optional: Firestore populates this with the document's ID
    private String documentId; // Field to hold Firestore Document ID if needed

    // Required fields
    private String patientId;
    private String patientName;
    private String establishmentId; // Links to the establishment
    private String appointmentDate; // "YYYY-MM-DD"
    private String appointmentTime; // "HH:MM"
    private String status;          // "CONFIRMED", "CANCELLED", etc.

    // *** Firestore Required: Default no-argument constructor ***
    public RendezVous() {}

    // --- CORRECTED Parameterized Constructor (6 arguments) ---
    public RendezVous(String patientId, String patientName, String establishmentId, String appointmentDate, String appointmentTime, String status) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.establishmentId = establishmentId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }

    // --- Getters ---
    public String getDocumentId() { return documentId; }
    public String getPatientId() { return patientId; }
    public String getPatientName() { return patientName; }
    public String getEstablishmentId() { return establishmentId; }
    public String getAppointmentDate() { return appointmentDate; }
    public String getAppointmentTime() { return appointmentTime; }
    public String getStatus() { return status; }

    // --- Setters ---
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public void setEstablishmentId(String establishmentId) { this.establishmentId = establishmentId; }
    public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }
    public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }
    public void setStatus(String status) { this.status = status; }

}