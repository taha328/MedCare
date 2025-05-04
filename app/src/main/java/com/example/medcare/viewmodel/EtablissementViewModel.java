package com.example.medcare.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import com.example.medcare.model.Etablissement;
import com.example.medcare.repository.EtablissementRepository;

public class EtablissementViewModel extends AndroidViewModel {

    private EtablissementRepository repository;
    private LiveData<List<Etablissement>> allEtablissements;

    public EtablissementViewModel(@NonNull Application application) {
        super(application);
        repository = new EtablissementRepository(application);
        allEtablissements = repository.getAllEtablissements();
    }

    public LiveData<List<Etablissement>> getAllEtablissements() {
        return allEtablissements;
    }

    public LiveData<Etablissement> getEtablissementById(int id) {
        return repository.getEtablissementById(id);
    }

    public void insert(Etablissement etablissement) {
        repository.insert(etablissement);
    }

    public void update(Etablissement etablissement) {
        repository.update(etablissement);
    }

    public void delete(Etablissement etablissement) {
        repository.delete(etablissement);
    }
}
