package com.example.medcare.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

import com.example.medcare.model.FileAttente;

@Dao
public interface FileAttenteDao {

    @Insert
    void insert(FileAttente fileAttente);

    @Update
    void update(FileAttente fileAttente);

    @Delete
    void delete(FileAttente fileAttente);

    @Query("SELECT * FROM FileAttente WHERE etablissementId = :etablissementId ORDER BY heureArrivee ASC")
    LiveData<List<FileAttente>> getFileForEtablissement(int etablissementId);

}
