package br.com.galdar.npd.activity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import br.com.galdar.npd.R;
import br.com.galdar.npd.config.FirebaseConfig;
import br.com.galdar.npd.helper.Base64Custom;
import br.com.galdar.npd.helper.DateCustom;
import br.com.galdar.npd.model.Transaction;
import br.com.galdar.npd.model.User;

public class IncomeActivity extends AppCompatActivity {

    private TextInputEditText incomeDate, incomeCategory, incomeDesc;
    private EditText incomeValue, parcelsNums;
    private CheckBox parcelsCheck;
    private Transaction transaction;
    private DatabaseReference dbReference = FirebaseConfig.getFirebaseDatabase();
    private FirebaseAuth auth = FirebaseConfig.getFirebaseAuth();
    private Double incomesTotal;
    private boolean calOpen = false;

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
        /*parcelsNums = findViewById(R.id.parcelsNums);
        parcelsCheck = findViewById(R.id.parcelsCheck);*/

        incomeDate.setText( DateCustom.currentDateFormated() );
        getIncomesTotal();

        incomeDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if( v.hasFocus() ){
                    showDateCalendar(v);
                }
            }
        });

        /*if( parcelsCheck.isChecked() ){
            parcelsNums.setEnabled( true );
        } else {
            parcelsNums.setEnabled( false );
        }

        parcelsCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if( parcelsCheck.isChecked() ){
                    parcelsNums.setEnabled( true );
                } else {
                    parcelsNums.setEnabled( false );
                }
            }
        });*/
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void showDateCalendar (View view) {
        if( calOpen == false ) {
            calOpen = true;

            Locale locale = getResources().getConfiguration().locale;
            Locale.setDefault(locale);

            Calendar cal = Calendar.getInstance();
            DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    String dayFormated = String.format("%02d", ( ( dayOfMonth ) ));
                    String monthFormated = String.format("%02d", ( ( month + 1 ) ));
                    incomeDate.setText( dayFormated + "/" + monthFormated + "/" + year );
                    calOpen = false;
                }

            };

            DatePickerDialog dialog = new DatePickerDialog( IncomeActivity.this,  date, cal.get( Calendar.YEAR ), cal.get( Calendar.MONTH ), cal.get( Calendar.DAY_OF_MONTH ) );
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_NEGATIVE) {
                        calOpen = false;
                    }
                }
            });
            dialog.show();
        }
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
