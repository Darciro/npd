package br.com.galdar.npd.model;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import br.com.galdar.npd.config.FirebaseConfig;
import br.com.galdar.npd.helper.Base64Custom;
import br.com.galdar.npd.helper.DateCustom;

public class Transaction {

    private String ID;
    private String date;
    private String category;
    private String description;
    private String type;
    private Double value;

    public Transaction() { }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public void save( String date ) {

        FirebaseAuth auth = FirebaseConfig.getFirebaseAuth();
        String userID = Base64Custom.encodeBase64( auth.getCurrentUser().getEmail() );
        String monthYearFormat = DateCustom.dateFormatedToDatabase( date );

        DatabaseReference dbReference = FirebaseConfig.getFirebaseDatabase();
        String exception = "";
        try {
            dbReference.child( "transactions" ).child ( userID ).child( monthYearFormat ).push().setValue( this );
            // Toast.makeText("", "Preencha o campo senha", Toast.LENGTH_LONG ).show();

        } catch (Exception e) {
            exception = "Erro ao executar a transação: " + e.getMessage();
            e.printStackTrace();
        }

    }

}
