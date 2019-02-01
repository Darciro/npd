package br.com.galdar.npd.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseConfig {

    private static FirebaseAuth auth;
    private static DatabaseReference dbReference;

    // Return an instance of Firebase Database
    public static DatabaseReference getFirebaseDatabase () {
        if ( dbReference == null ) {
            dbReference = FirebaseDatabase.getInstance().getReference();
        }

        return dbReference;
    }

    // Return an instance of Firebase authentication
    public static FirebaseAuth getFirebaseAuth() {

        if( auth == null ) {
            auth = FirebaseAuth.getInstance();
        }

        return auth;
    }

}
