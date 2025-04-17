package com.example.medcare;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button; // Import Button
import android.widget.TextView; // Import TextView
import android.widget.Toast; // Import Toast
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
// Import Firestore classes
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity"; // Tag for logging
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // Firestore instance needed here

    // Declare UI elements used in this activity's layout (activity_dashboard_main.xml)
    private TextView textViewWelcomeMain;
    private Button buttonLogoutMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ensure this inflates your combined dashboard layout
        setContentView(R.layout.activity_dashboard_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        // Find the UI elements from the layout
        textViewWelcomeMain = findViewById(R.id.textViewWelcomeMain);
        buttonLogoutMain = findViewById(R.id.buttonLogoutMain);

        // Setup the logout button listener
        if (buttonLogoutMain != null) {
            buttonLogoutMain.setOnClickListener(v -> {
                Log.d(TAG, "Logout button clicked.");
                mAuth.signOut();
                redirectToSignIn(); // Go back to login screen
            });
        } else {
            Log.e(TAG, "Logout button (buttonLogoutMain) not found in layout!");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Not signed in, go to Sign In screen
            Log.d(TAG, "User not signed in. Redirecting to SignInActivity.");
            redirectToSignIn();
        } else {
            // User IS signed in. Verify their role/status allows them here.
            Log.d(TAG, "User signed in (" + currentUser.getUid() + "). Verifying access...");
            verifyUserAccessAndLoadUI(currentUser.getUid());
        }
    }

    // Helper method to navigate to SignInActivity and finish the current one
    private void redirectToSignIn() {
        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Finish MainActivity
    }

    // Checks Firestore for the user's role and status, then either redirects or loads UI
    @SuppressLint("SetTextI18n")
    private void verifyUserAccessAndLoadUI(String userId) {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                DocumentSnapshot document = task.getResult();
                String userRole = document.getString("role");
                String userStatus = document.getString("status");
                Log.d(TAG, "Verification check: role=" + userRole + ", status=" + userStatus);

                boolean stayInMainActivity = false;
                Intent redirectIntent = null; // Intent for redirection if needed

                // Determine routing based on role and status
                if ("admin".equals(userRole)) {
                    Log.d(TAG, "User is Admin. Redirecting...");
                    redirectIntent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                } else if ("professional".equals(userRole) && "approved".equals(userStatus)) {
                    Log.d(TAG, "User is Approved Professional. Staying in MainActivity.");
                    stayInMainActivity = true; // Approved professionals stay
                } else if ("pending_professional".equals(userRole)) {
                    if ("pending".equals(userStatus)) {
                        Log.d(TAG, "User is Pending Professional. Redirecting...");
                        redirectIntent = new Intent(MainActivity.this, PendingVerificationActivity.class);
                    } else if ("rejected".equals(userStatus)) {
                        Log.w(TAG, "Rejected professional attempting access. Signing out.");
                        Toast.makeText(this, "Your professional application was rejected.", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        redirectToSignIn(); // Redirect after logging out
                        return; // Stop processing here
                    } else {
                        // Unexpected status for pending_professional
                        stayInMainActivity = false; // Don't allow stay
                    }
                } else if ("patient".equals(userRole)) {
                    Log.d(TAG, "User is Patient. Staying in MainActivity.");
                    stayInMainActivity = true; // Patients stay
                }

                // Execute action based on determination
                if (stayInMainActivity) {
                    // User is allowed here, load the UI elements for them
                    FirebaseUser currentUser = mAuth.getCurrentUser(); // Re-get current user info
                    if (currentUser != null && textViewWelcomeMain != null) {
                        String email = currentUser.getEmail();
                        // Set welcome text, potentially indicating role for clarity during testing
                        textViewWelcomeMain.setText("Welcome, " + (email != null ? email : "User") + "!\n(Role: " + userRole + ")");
                    }
                    // ** Add code here later to show/hide specific UI elements based on userRole if needed **

                } else if (redirectIntent != null) {
                    // Redirect to the determined Activity (Admin or Pending)
                    Log.d(TAG, "Redirecting signed-in user to: " + redirectIntent.getComponent().getShortClassName());
                    redirectIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(redirectIntent);
                    finish(); // Finish MainActivity AFTER starting the new one
                } else {
                    // Fell through - Unhandled role/status combination
                    Log.e(TAG, "Access Denied or Unhandled State for role: " + userRole + ", status: " + userStatus + ". Signing out.");
                    Toast.makeText(this, "Account access error. Please contact support.", Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                    redirectToSignIn();
                }

            } else {
                // Error fetching Firestore document OR document doesn't exist
                if(task.getException() != null){
                    Log.e(TAG, "Firestore get failed for user: " + userId, task.getException());
                } else {
                    Log.e(TAG, "Firestore profile not found for authenticated user: " + userId + ". Possible data inconsistency.");
                }
                Toast.makeText(this, "Error loading your profile. Please log in again.", Toast.LENGTH_LONG).show();
                mAuth.signOut();
                redirectToSignIn();
            }
        });
    }
}