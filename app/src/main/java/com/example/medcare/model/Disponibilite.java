package com.example.medcare.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Disponibilite {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int etablissementId;
    private String jour;
    private String heureDebut;
    private String heureFin;
    private boolean ouvert; // true = ouvert ce jour-là

    public Disponibilite(int etablissementId, String jour, String heureDebut,
                         String heureFin, boolean ouvert) {
        this.etablissementId = etablissementId;
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.ouvert = ouvert;
    }

    public int getId() {
        return id;
    }

    public int getEtablissementId() {
        return etablissementId;
    }

    public String getJour() {
        return jour;
    }

    public String getHeureDebut() {
        return heureDebut;
    }

    public String getHeureFin() {
        return heureFin;
    }

    public boolean isOuvert() {
        return ouvert;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setEtablissementId(int etablissementId) {
        this.etablissementId = etablissementId;
    }

    public void setJour(String jour) {
        this.jour = jour;
    }

    public void setHeureDebut(String heureDebut) {
        this.heureDebut = heureDebut;
    }

    public void setHeureFin(String heureFin) {
        this.heureFin = heureFin;
    }

    public void setOuvert(boolean ouvert) {
        this.ouvert = ouvert;
    }
}
