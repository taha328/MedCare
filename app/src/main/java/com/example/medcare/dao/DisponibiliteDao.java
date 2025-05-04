package com.example.medcare.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

import com.example.medcare.model.Disponibilite;

@Dao
public interface DisponibiliteDao {

    @Insert
    void insert(Disponibilite disponibilite);

    @Update
    void update(Disponibilite disponibilite);

    @Delete
    void delete(Disponibilite disponibilite);

    @Query("SELECT * FROM Disponibilite WHERE etablissementId = :etablissementId")
    LiveData<List<Disponibilite>> getDisponibilitesForEtablissement(int etablissementId);
}
