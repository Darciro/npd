package br.com.galdar.npd.model;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import br.com.galdar.npd.config.FirebaseConfig;

public class User {

    private String userID;
    private String name;
    private String email;
    private String password;
    private Double incomesTotal = 0.00;
    private Double expensesTotal = 0.00;

    public User() {}

    public void save () {
        DatabaseReference dbReference = FirebaseConfig.getFirebaseDatabase();

        String exception = "";
        try {
            dbReference.child( "users" ).child( this.userID ).setValue( this );
        } catch (Exception e) {
            exception = "Erro com a transação do DB: " + e.getMessage();
            e.printStackTrace();
        }
    }

    @Exclude
    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Double getIncomesTotal() {
        return incomesTotal;
    }

    public void setIncomesTotal(Double incomesTotal) {
        this.incomesTotal = incomesTotal;
    }

    public Double getExpensesTotal() {
        return expensesTotal;
    }

    public void setExpensesTotal(Double expensesTotal) {
        this.expensesTotal = expensesTotal;
    }

}
