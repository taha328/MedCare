package com.example.medcare.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.medcare.dao.DisponibiliteDao;
import com.example.medcare.database.AppDatabase;
import com.example.medcare.model.Disponibilite;

public class DisponibiliteRepository {

    private final DisponibiliteDao dao;
    private final ExecutorService executorService;

    public DisponibiliteRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        dao = db.disponibiliteDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Disponibilite>> getDisponibilitesForEtablissement(int etablissementId) {
        return dao.getDisponibilitesForEtablissement(etablissementId);
    }

    public void insert(Disponibilite disponibilite) {
        executorService.execute(() -> dao.insert(disponibilite));
    }

    public void update(Disponibilite disponibilite) {
        executorService.execute(() -> dao.update(disponibilite));
    }

    public void delete(Disponibilite disponibilite) {
        executorService.execute(() -> dao.delete(disponibilite));
    }
}
