package br.com.galdar.npd.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import br.com.galdar.npd.R;
import br.com.galdar.npd.config.FirebaseConfig;
import br.com.galdar.npd.helper.Base64Custom;
import br.com.galdar.npd.helper.DateCustom;
import br.com.galdar.npd.model.Transaction;
import br.com.galdar.npd.model.User;

public class ExpenseActivity extends AppCompatActivity {

    private TextInputEditText expenseDate, expenseCategory, expenseDesc;
    private EditText expenseValue;
    private Transaction transaction;
    private DatabaseReference dbReference = FirebaseConfig.getFirebaseDatabase();
    private FirebaseAuth auth = FirebaseConfig.getFirebaseAuth();
    private Double expensesTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Nova despesa");
        getSupportActionBar().setBackgroundDrawable( new ColorDrawable( Color.parseColor( "#88363C" ) ));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        expenseDate = findViewById(R.id.expenseDate);
        expenseCategory = findViewById(R.id.expenseCategory);
        expenseDesc = findViewById(R.id.expenseDesc);
        expenseValue = findViewById(R.id.expenseValue);

        expenseDate.setText( DateCustom.currentDateFormated() );
        getExpensesTotal();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void saveExpense(View view) {

        if( validateExpenseFields() ){
            String curDate = expenseDate.getText().toString();
            Double expenseRegistered = Double.parseDouble( expenseValue.getText().toString() );

            transaction = new Transaction();
            transaction.setCategory( expenseCategory.getText().toString() );
            transaction.setDate( curDate );
            transaction.setDescription( expenseDesc.getText().toString() );
            transaction.setType( "expense" );
            transaction.setValue( expenseRegistered );

            Double expensesUpdated = expensesTotal + expenseRegistered;
            updateExpensesTotal( expensesUpdated );

            transaction.save( curDate );
            finish();
        }

    }

    public Boolean validateExpenseFields () {

        expenseDate = findViewById(R.id.expenseDate);
        expenseCategory = findViewById(R.id.expenseCategory);
        expenseDesc = findViewById(R.id.expenseDesc);
        expenseValue = findViewById(R.id.expenseValue);

        String typedExpenseDate = expenseDate.getText().toString();
        String typedExpenseCategory = expenseCategory.getText().toString();
        String typedExpenseDesc = expenseDesc.getText().toString();
        String typedExpenseValue = expenseValue.getText().toString();

        // Validate fields
        if( !typedExpenseValue.isEmpty() ) {
            if( !typedExpenseCategory.isEmpty() ) {
                if( !typedExpenseDesc.isEmpty() ) {
                    if( !typedExpenseDate.isEmpty() ) {
                        return true;
                    } else {
                        Toast.makeText( ExpenseActivity.this, "Preencha a data da despesa", Toast.LENGTH_LONG ).show();
                        return false;
                    }
                } else {
                    Toast.makeText( ExpenseActivity.this, "Preencha a descrição da despesa", Toast.LENGTH_LONG ).show();
                    return false;
                }
            } else {
                Toast.makeText( ExpenseActivity.this, "Preencha a categoria da despesa", Toast.LENGTH_LONG ).show();
                return false;
            }
        } else {
            Toast.makeText( ExpenseActivity.this, "Preencha o valor da despesa", Toast.LENGTH_LONG ).show();
            return false;
        }
    }

    public void getExpensesTotal() {

        String userEmail = auth.getCurrentUser().getEmail();
        String userID = Base64Custom.encodeBase64( userEmail );
        DatabaseReference userRef = dbReference.child("users").child(userID);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue( User.class );
                expensesTotal = user.getExpensesTotal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void updateExpensesTotal (Double expense) {
        String userEmail = auth.getCurrentUser().getEmail();
        String userID = Base64Custom.encodeBase64( userEmail );
        DatabaseReference userRef = dbReference.child("users").child(userID);

        userRef.child("expensesTotal").setValue( expense );
    }
}
