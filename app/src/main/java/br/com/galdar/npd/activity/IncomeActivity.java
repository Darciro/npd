package br.com.galdar.npd.activity;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.List;

import br.com.galdar.npd.R;
import br.com.galdar.npd.config.FirebaseConfig;
import br.com.galdar.npd.helper.Base64Custom;
import br.com.galdar.npd.helper.DateCustom;
import br.com.galdar.npd.model.Transaction;
import br.com.galdar.npd.model.User;

public class IncomeActivity extends AppCompatActivity {

    private TextInputEditText incomeDate, incomeCategory, incomeDesc;
    private EditText incomeValue;
    private Transaction transaction;
    private DatabaseReference dbReference = FirebaseConfig.getFirebaseDatabase();
    private FirebaseAuth auth = FirebaseConfig.getFirebaseAuth();
    private Double incomesTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);
        getSupportActionBar().setTitle("Nova receita");
        getSupportActionBar().setBackgroundDrawable( new ColorDrawable( Color.parseColor( "#00574B" ) ));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        incomeDate = findViewById(R.id.incomeDate);
        incomeCategory = findViewById(R.id.incomeCategory);
        incomeDesc = findViewById(R.id.incomeDesc);
        incomeValue = findViewById(R.id.incomeValue);

        incomeDate.setText( DateCustom.currentDateFormated() );
        getIncomesTotal();

        incomeDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xxx();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void xxx () {
        Log.i( "XXX", "Focus aqui!");
        Calendar cal = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Log.i( "XXX", "Date set: " + dayOfMonth);
            }
        };

        new DatePickerDialog( IncomeActivity.this,  date, cal.get( Calendar.YEAR ), cal.get( Calendar.MONTH ), cal.get( Calendar.DAY_OF_MONTH ) ).show();
    }

    public void saveIncome(View view) {

        if( validateIncomeFields() ) {
            String curDate = incomeDate.getText().toString();
            Double incomeRegistered = Double.parseDouble( incomeValue.getText().toString() );

            transaction = new Transaction();
            transaction.setCategory( incomeCategory.getText().toString() );
            transaction.setDate( curDate );
            transaction.setDescription( incomeDesc.getText().toString() );
            transaction.setType( "income" );
            transaction.setValue( incomeRegistered );

            Double incomesUpdated = incomesTotal + incomeRegistered;
            updateIncomesTotal( incomesUpdated );

            transaction.save( curDate );
            finish();
        }

    }

    public Boolean validateIncomeFields () {

        incomeDate = findViewById(R.id.incomeDate);
        incomeCategory = findViewById(R.id.incomeCategory);
        incomeDesc = findViewById(R.id.incomeDesc);
        incomeValue = findViewById(R.id.incomeValue);

        String typedIncomeDate = incomeDate.getText().toString();
        String typedIncomeCategory = incomeCategory.getText().toString();
        String typedIncomeDesc = incomeDesc.getText().toString();
        String typedIncomeValue = incomeValue.getText().toString();

        // Validate fields
        if( !typedIncomeValue.isEmpty() ) {
            if( !typedIncomeCategory.isEmpty() ) {
                if( !typedIncomeDesc.isEmpty() ) {
                    if( !typedIncomeDate.isEmpty() ) {
                        return true;
                    } else {
                        Toast.makeText( IncomeActivity.this, "Preencha a data da receita", Toast.LENGTH_LONG ).show();
                        return false;
                    }
                } else {
                    Toast.makeText( IncomeActivity.this, "Preencha a descrição da receita", Toast.LENGTH_LONG ).show();
                    return false;
                }
            } else {
                Toast.makeText( IncomeActivity.this, "Preencha a categoria da receita", Toast.LENGTH_LONG ).show();
                return false;
            }
        } else {
            Toast.makeText( IncomeActivity.this, "Preencha o valor da receita", Toast.LENGTH_LONG ).show();
            return false;
        }
    }

    public void getIncomesTotal() {

        String userEmail = auth.getCurrentUser().getEmail();
        String userID = Base64Custom.encodeBase64( userEmail );
        DatabaseReference userRef = dbReference.child("users").child(userID);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue( User.class );
                incomesTotal = user.getIncomesTotal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void updateIncomesTotal (Double income) {
        String userEmail = auth.getCurrentUser().getEmail();
        String userID = Base64Custom.encodeBase64( userEmail );
        DatabaseReference userRef = dbReference.child("users").child(userID);

        userRef.child("incomesTotal").setValue( income );
    }
}
