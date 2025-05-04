package com.example.medcare.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import com.example.medcare.database.AppDatabase;
import com.example.medcare.model.FileAttente;

public class FileAttenteViewModel extends AndroidViewModel {

    private final AppDatabase db;

    public FileAttenteViewModel(Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
    }

    public void insert(FileAttente f) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.fileAttenteDao().insert(f);
        });
    }

    public LiveData<List<FileAttente>> getFileForEtablissement(int etabId) {
        return db.fileAttenteDao().getFileForEtablissement(etabId);
    }
}
