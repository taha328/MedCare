package com.example.medcare;


import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp; // Import ServerTimestamp
import java.util.Date; // Import Date


@IgnoreExtraProperties
public class UserProfile {

    // --- Fields matching your Firestore document ---
    private String uid;
    private String name;
    private String email;
    private String role;          // e.g., "patient", "pending_professional", "professional", "admin"
    private String status;        // e.g., "pending", "approved", "rejected", "active"
    private String licenseNumber; // Specific to professionals
    private String specialty;     // Specific to professionals

    @ServerTimestamp // Automatically set by Firestore on creation
    private Date createdAt;

    // --- Required: Public no-argument constructor for Firestore ---
    public UserProfile() {
        // Default constructor required for calls to DataSnapshot.getValue(UserProfile.class)
        // and for Firestore's automatic data mapping (toObject).
    }

    // --- Getters (Required by Firestore for mapping data FROM Firestore) ---
    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public String getLicenseNumber() { return licenseNumber; }
    public String getSpecialty() { return specialty; }
    public Date getCreatedAt() { return createdAt; }

    // --- Setters (Useful for creating/modifying objects BEFORE saving TO Firestore) ---
    public void setUid(String uid) { this.uid = uid; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setStatus(String status) { this.status = status; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    // Optional: toString() method for debugging
    @Override
    public String toString() {
        return "UserProfile{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", status='" + status + '\'' +
                ", licenseNumber='" + licenseNumber + '\'' +
                ", specialty='" + specialty + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}