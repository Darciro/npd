package br.com.galdar.npd.activity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.stephenvinouze.materialnumberpickercore.MaterialNumberPicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Locale;

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
    private boolean calOpen = false;
    private ImageView incomeRepeaterButtonExpense;
    private MaterialNumberPicker numberPicker;
    private AlertDialog repeaterAlert;
    private TextView repeaterSettedHeaderExpense, repeaterSettedExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        getSupportActionBar().setTitle("Nova despesa");
        getSupportActionBar().setBackgroundDrawable( new ColorDrawable( Color.parseColor( "#88363C" ) ));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        expenseDate = findViewById(R.id.expenseDate);
        expenseCategory = findViewById(R.id.expenseCategory);
        expenseDesc = findViewById(R.id.expenseDesc);
        expenseValue = findViewById(R.id.expenseValue);
        incomeRepeaterButtonExpense = findViewById(R.id.incomeRepeaterButtonExpense);
        repeaterSettedHeaderExpense = findViewById(R.id.repeaterSettedHeaderExpense);
        repeaterSettedExpense = findViewById(R.id.repeaterSettedExpense);

        expenseDate.setText( DateCustom.currentDateFormated() );
        getExpensesTotal();

        expenseDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if( v.hasFocus() ){
                    showDateCalendar(v);
                }
            }
        });

        incomeRepeaterButtonExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expenseRepeater();
            }
        });
    }

    public void expenseRepeater () {
        final MaterialNumberPicker numberPicker = new MaterialNumberPicker(this);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(360);
        numberPicker.setValue(0);
        numberPicker.setEditable(true);
        numberPicker.setWrapSelectorWheel(true);
        numberPicker.setTextSize(60);
        numberPicker.setSeparatorColor( ContextCompat.getColor(this, R.color.colorAccent) );

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Repetir");
        builder.setView( numberPicker );
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                // Toast.makeText(IncomeActivity.this, "positivo=" + arg1 + ", " + numberPicker.getValue(), Toast.LENGTH_SHORT).show();
                if( numberPicker.getValue() == 0 ){
                    repeaterSettedExpense.setVisibility(View.GONE);
                    repeaterSettedExpense.setText("");
                    repeaterSettedExpense.setTextColor( getResources().getColor(R.color.colorTextGrey) );
                    repeaterSettedHeaderExpense.setTextColor( getResources().getColor(R.color.colorTextGrey) );
                } else {
                    repeaterSettedExpense.setVisibility(View.VISIBLE);
                    repeaterSettedExpense.setText( numberPicker.getValue() + " vezes" );
                    repeaterSettedExpense.setTextColor( getResources().getColor(R.color.colorAccentExpense) );
                    repeaterSettedHeaderExpense.setTextColor( getResources().getColor(R.color.colorAccentExpense) );
                }
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                // Toast.makeText(IncomeActivity.this, "negativo=" + arg1 + ", " + numberPicker.getValue(), Toast.LENGTH_SHORT).show();
            }
        });
        repeaterAlert = builder.create();
        repeaterAlert.show();
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
                    expenseDate.setText( dayFormated + "/" + monthFormated + "/" + year );
                    calOpen = false;
                }

            };

            DatePickerDialog dialog = new DatePickerDialog( ExpenseActivity.this,  date, cal.get( Calendar.YEAR ), cal.get( Calendar.MONTH ), cal.get( Calendar.DAY_OF_MONTH ) );
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
