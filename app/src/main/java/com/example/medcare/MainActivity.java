package com.example.medcare;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.medcare.admin.AdminDashboardActivity;
import com.example.medcare.admin.PendingVerificationActivity;
import com.example.medcare.auth.SignInActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.medcare.ui.DisponibiliteFragment;
import com.example.medcare.ui.EtablissementFragment;
import com.example.medcare.ui.FileAttenteFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private BottomNavigationView bottomNavigationView;
    private FrameLayout fragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fragmentContainer = findViewById(R.id.fragment_container);

        if (bottomNavigationView == null || fragmentContainer == null) {
            Log.e(TAG, "FATAL: Core UI elements (BottomNav or FragmentContainer) not found in layout activity_main.xml!");
            Toast.makeText(this, "Error loading main UI.", Toast.LENGTH_LONG).show();
            if (mAuth.getCurrentUser() != null) mAuth.signOut();
            redirectToSignIn();
            return;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            redirectToSignIn();
        } else {
            verifyUserAccessAndSetupUI(currentUser.getUid());
        }
    }

    private void redirectToSignIn() {
        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @SuppressLint("SetTextI18n")
    private void verifyUserAccessAndSetupUI(String userId) {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                DocumentSnapshot document = task.getResult();
                String userRole = document.getString("role");
                String userStatus = document.getString("status");
                Log.d(TAG, "Verification check: role=" + userRole + ", status=" + userStatus);

                boolean showBottomNavUI = false;
                Intent redirectIntent = null;

                if ("admin".equals(userRole)) {
                    redirectIntent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                } else if ("professional".equals(userRole) && "approved".equals(userStatus)) {
                    showBottomNavUI = true;
                } else if ("pending_professional".equals(userRole)) {
                    if ("pending".equals(userStatus)) {
                        redirectIntent = new Intent(MainActivity.this, PendingVerificationActivity.class);
                    } else if ("rejected".equals(userStatus)) {
                        Log.w(TAG, "Rejected professional attempted access. Signing out.");
                        Toast.makeText(this, "Your professional application was rejected.", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        redirectToSignIn();
                        return;
                    } else {
                        Log.e(TAG,"Unhandled status '"+userStatus+"' for pending_professional. Signing out.");
                        Toast.makeText(this, "Account status error.", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        redirectToSignIn();
                        return;
                    }
                } else if ("patient".equals(userRole)) {
                    showBottomNavUI = true;
                }

                if (showBottomNavUI) {
                    Log.d(TAG,"Setting up Bottom Navigation for role: " + userRole);
                    setupBottomNavigation();
                } else if (redirectIntent != null) {
                    Log.d(TAG, "Redirecting signed-in user to: " + redirectIntent.getComponent().getShortClassName());
                    redirectIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(redirectIntent);
                    finish();
                } else {
                    Log.e(TAG, "Access Denied/Unhandled State for role=" + userRole + ", status=" + userStatus + ". Signing out.");
                    Toast.makeText(this, "Account access error.", Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                    redirectToSignIn();
                }

            } else {
                Log.e(TAG, "Firestore get failed or user doc not found for UID: " + userId, task.getException());
                Toast.makeText(this, "Error loading profile. Please log in again.", Toast.LENGTH_LONG).show();
                mAuth.signOut();
                redirectToSignIn();
            }
        });
    }

    private void setupBottomNavigation() {
        if (bottomNavigationView == null) return;

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment selectedFragment = null;

            if (id == R.id.nav_etablissement) {
                selectedFragment = new EtablissementFragment();
            } else if (id == R.id.nav_disponibilite) {
                selectedFragment = new DisponibiliteFragment();
            } else if (id == R.id.nav_file) {
                selectedFragment = new FileAttenteFragment();
            }

            if (selectedFragment != null) {
                replaceFragment(selectedFragment);
                return true;
            }
            return false;
        });

        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            Log.d(TAG,"Setting initial fragment (EtablissementFragment)");
            if(bottomNavigationView.getMenu().findItem(R.id.nav_etablissement) != null) {
                bottomNavigationView.setSelectedItemId(R.id.nav_etablissement);
            } else {
                Log.e(TAG,"Default menu item R.id.nav_etablissement not found!");
            }
        }
    }

    private void replaceFragment(Fragment fragment) {
        if (fragmentContainer == null || fragment == null) {
            Log.e(TAG,"Cannot replace fragment - container or fragment is null!");
            return;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
        Log.d(TAG,"Replacing fragment with: " + fragment.getClass().getSimpleName());
    }
}