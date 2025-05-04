package com.example.medcare.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

import com.example.medcare.model.Etablissement;

@Dao
public interface EtablissementDao {

    @Insert
    void insert(Etablissement etablissement);

    @Update
    void update(Etablissement etablissement);

    @Delete
    void delete(Etablissement etablissement);

    @Query("SELECT * FROM Etablissement ORDER BY nom ASC")
    LiveData<List<Etablissement>> getAllEtablissements();

    @Query("SELECT * FROM Etablissement WHERE id = :id LIMIT 1")
    LiveData<Etablissement> getEtablissementById(int id);
}
