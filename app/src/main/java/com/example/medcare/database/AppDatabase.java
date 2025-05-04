package com.example.medcare.database;

import android.app.Application;
import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.medcare.dao.DisponibiliteDao;
import com.example.medcare.dao.EtablissementDao;
import com.example.medcare.dao.FileAttenteDao;
import com.example.medcare.model.Disponibilite;
import com.example.medcare.model.Etablissement;
import com.example.medcare.model.FileAttente;

@Database(entities = {Etablissement.class, Disponibilite.class, FileAttente.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract EtablissementDao etablissementDao();
    public abstract DisponibiliteDao disponibiliteDao();
    public abstract FileAttenteDao fileAttenteDao();

    private static volatile AppDatabase INSTANCE;

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "mc_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // ✅ Ajoute cette méthode pour la compatibilité avec les Repositories
    public static AppDatabase getInstance(Application application) {
        return getDatabase(application.getApplicationContext());
    }
}
