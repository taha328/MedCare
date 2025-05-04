package com.example.medcare.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Etablissement {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String nom;
    private String adresse;
    private String telephone;
    private String email;
    private String responsable;
    private String type;     // Ex: Clinique, Banque, Administration, etc.
    private String siteWeb;
    private String imageUrl;

    // Constructeur
    public Etablissement(String nom, String adresse, String telephone, String email,
                         String responsable, String type, String siteWeb, String imageUrl) {
        this.nom = nom;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.responsable = responsable;
        this.type = type;
        this.siteWeb = siteWeb;
        this.imageUrl = imageUrl;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getEmail() {
        return email;
    }

    public String getResponsable() {
        return responsable;
    }

    public String getType() {
        return type;
    }

    public String getSiteWeb() {
        return siteWeb;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // Setter pour Room (optionnels sauf pour `id`)
    public void setId(int id) {
        this.id = id;
    }
}
