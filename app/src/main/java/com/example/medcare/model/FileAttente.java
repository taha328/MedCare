package com.example.medcare.model;

import com.google.firebase.Timestamp; // Optional: For using Firestore Timestamp

public class FileAttente {

    private String documentId; // To hold Firestore document ID

    private String etablissementId; // Assuming this is the Firestore ID of the establishment
    private String nomPersonne;
    private String prenomPersonne;
    private String heureArrivee; // Consider using Timestamp for better sorting/querying
    // private Timestamp timestampArrivee; // Alternative using Timestamp
    private String motif;
    private String statut;

    // No-argument constructor required by Firestore
    public FileAttente() {}

    // Parameterized constructor
    public FileAttente(String etablissementId, String nomPersonne, String prenomPersonne,
                       String heureArrivee, String motif, String statut) {
        this.etablissementId = etablissementId;
        this.nomPersonne = nomPersonne;
        this.prenomPersonne = prenomPersonne;
        this.heureArrivee = heureArrivee;
        // this.timestampArrivee = timestampArrivee; // If using Timestamp
        this.motif = motif;
        this.statut = statut;
    }

    // Getters
    public String getDocumentId() { return documentId; }
    public String getEtablissementId() { return etablissementId; }
    public String getNomPersonne() { return nomPersonne; }
    public String getPrenomPersonne() { return prenomPersonne; }
    public String getHeureArrivee() { return heureArrivee; }
    // public Timestamp getTimestampArrivee() { return timestampArrivee; } // If using Timestamp
    public String getMotif() { return motif; }
    public String getStatut() { return statut; }


    // Setters
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public void setEtablissementId(String etablissementId) { this.etablissementId = etablissementId; }
    public void setNomPersonne(String nomPersonne) { this.nomPersonne = nomPersonne; }
    public void setPrenomPersonne(String prenomPersonne) { this.prenomPersonne = prenomPersonne; }
    public void setHeureArrivee(String heureArrivee) { this.heureArrivee = heureArrivee; }
    // public void setTimestampArrivee(Timestamp timestampArrivee) { this.timestampArrivee = timestampArrivee; } // If using Timestamp
    public void setMotif(String motif) { this.motif = motif; }
    public void setStatut(String statut) { this.statut = statut; }
}