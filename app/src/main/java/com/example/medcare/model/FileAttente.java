package com.example.medcare.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class FileAttente {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int etablissementId;
    public String nomPersonne;
    public String prenomPersonne;
    public String heureArrivee;
    public String motif;
    public String statut; // Ex : En attente, Servi, etc.

    public FileAttente(int etablissementId, String nomPersonne, String prenomPersonne,
                       String heureArrivee, String motif, String statut) {
        this.etablissementId = etablissementId;
        this.nomPersonne = nomPersonne;
        this.prenomPersonne = prenomPersonne;
        this.heureArrivee = heureArrivee;
        this.motif = motif;
        this.statut = statut;
    }

    public int getId() {
        return id;
    }

    public int getEtablissementId() {
        return etablissementId;
    }

    public String getNomPersonne() {
        return nomPersonne;
    }

    public String getPrenomPersonne() {
        return prenomPersonne;
    }

    public String getHeureArrivee() {
        return heureArrivee;
    }

    public String getMotif() {
        return motif;
    }

    public String getStatut() {
        return statut;
    }
}
