package com.example.medcare.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import com.example.medcare.model.Disponibilite;
import com.example.medcare.repository.DisponibiliteRepository;

public class DisponibiliteViewModel extends AndroidViewModel {

    private DisponibiliteRepository repository;

    public DisponibiliteViewModel(@NonNull Application application) {
        super(application);
        repository = new DisponibiliteRepository(application);
    }

    public LiveData<List<Disponibilite>> getDisponibilitesForEtablissement(int etablissementId) {
        return repository.getDisponibilitesForEtablissement(etablissementId);
    }

    public void insert(Disponibilite disponibilite) {
        repository.insert(disponibilite);
    }

    public void update(Disponibilite disponibilite) {
        repository.update(disponibilite);
    }

    public void delete(Disponibilite disponibilite) {
        repository.delete(disponibilite);
    }
}
