package com.example.medcare.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.medcare.dao.FileAttenteDao;
import com.example.medcare.database.AppDatabase;
import com.example.medcare.model.FileAttente;

public class FileAttenteRepository {

    private FileAttenteDao dao;
    private ExecutorService executorService;

    public FileAttenteRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        dao = db.fileAttenteDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<FileAttente>> getFileAttenteForEtablissement(int etablissementId) {
        return dao.getFileForEtablissement(etablissementId);
    }

    public void insert(FileAttente fileAttente) {
        executorService.execute(() -> dao.insert(fileAttente));
    }

    public void update(FileAttente fileAttente) {
        executorService.execute(() -> dao.update(fileAttente));
    }

    public void delete(FileAttente fileAttente) {
        executorService.execute(() -> dao.delete(fileAttente));
    }
}
