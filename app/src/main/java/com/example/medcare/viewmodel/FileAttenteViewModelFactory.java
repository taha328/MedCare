package com.example.medcare.viewmodel; // Or your viewmodel package

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.medcare.repository.FileAttenteRepository;

public class FileAttenteViewModelFactory implements ViewModelProvider.Factory {

    public FileAttenteViewModelFactory() {
        // No dependencies needed for the factory/repository itself
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(FileAttenteViewModel.class)) {
            FileAttenteRepository repository = new FileAttenteRepository();
            return (T) new FileAttenteViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}