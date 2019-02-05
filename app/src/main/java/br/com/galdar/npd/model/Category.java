package br.com.galdar.npd.model;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import br.com.galdar.npd.config.FirebaseConfig;
import br.com.galdar.npd.helper.Base64Custom;
import br.com.galdar.npd.helper.DateCustom;

public class Category {

    private String ID;
    private String type;
    private String name;
    private String description;

    public Category() {}

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void save() {

        FirebaseAuth auth = FirebaseConfig.getFirebaseAuth();
        String userID = Base64Custom.encodeBase64( auth.getCurrentUser().getEmail() );
        // String monthYearFormat = DateCustom.dateFormatedToDatabase( type );

        DatabaseReference dbReference = FirebaseConfig.getFirebaseDatabase();
        String exception = "";
        try {
            dbReference.child( "users" ).child ( userID ).child( "categories" ).push().setValue( this );
            // Toast.makeText("", "Preencha o campo senha", Toast.LENGTH_LONG ).show();

        } catch (Exception e) {
            exception = "Erro ao executar a transação: " + e.getMessage();
            e.printStackTrace();
        }

    }
}
