package com.example.medcare.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.medcare.dao.EtablissementDao;
import com.example.medcare.database.AppDatabase;
import com.example.medcare.model.Etablissement;

public class EtablissementRepository {

    private EtablissementDao dao;
    private LiveData<List<Etablissement>> allEtablissements;
    private ExecutorService executorService;

    public EtablissementRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        dao = db.etablissementDao();
        allEtablissements = dao.getAllEtablissements();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Etablissement>> getAllEtablissements() {
        return allEtablissements;
    }

    public LiveData<Etablissement> getEtablissementById(int id) {
        return dao.getEtablissementById(id);
    }

    public void insert(Etablissement etablissement) {
        executorService.execute(() -> dao.insert(etablissement));
    }

    public void update(Etablissement etablissement) {
        executorService.execute(() -> dao.update(etablissement));
    }

    public void delete(Etablissement etablissement) {
        executorService.execute(() -> dao.delete(etablissement));
    }
}
