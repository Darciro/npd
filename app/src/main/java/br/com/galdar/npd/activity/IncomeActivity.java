package br.com.galdar.npd.activity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.github.stephenvinouze.materialnumberpickercore.MaterialNumberPicker;
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
    private ImageView incomeRepeaterButton, incomeReminderButton, incomePhotoButton;
    private MaterialNumberPicker numberPicker;
    private AlertDialog repeaterAlert;
    private TextView repeaterSettedHeader, repeaterSetted;
    static final int REQUEST_IMAGE_CAPTURE = 1;

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
        incomeRepeaterButton = findViewById(R.id.incomeRepeaterButton);
        repeaterSettedHeader = findViewById(R.id.repeaterSettedHeader);
        repeaterSetted = findViewById(R.id.repeaterSetted);
        incomeReminderButton = findViewById(R.id.incomeReminderButton);
        incomePhotoButton = findViewById(R.id.incomePhotoButton);

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

        incomeRepeaterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incomeRepeater();
            }
        });

        incomeReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incomeReminder();
            }
        });

        incomePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incomePhoto();
            }
        });
    }

    public void incomeRepeater () {
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
                    repeaterSetted.setVisibility(View.GONE);
                    repeaterSetted.setText("");
                    repeaterSetted.setTextColor( getResources().getColor(R.color.colorTextGrey) );
                    repeaterSettedHeader.setTextColor( getResources().getColor(R.color.colorTextGrey) );
                } else {
                    repeaterSetted.setVisibility(View.VISIBLE);
                    repeaterSetted.setText( numberPicker.getValue() + " vezes" );
                    repeaterSetted.setTextColor( getResources().getColor(R.color.colorAccentIncome) );
                    repeaterSettedHeader.setTextColor( getResources().getColor(R.color.colorAccentIncome) );
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

    public void incomeReminder() {
        Calendar cal = Calendar.getInstance();
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra("beginTime", cal.getTimeInMillis());
        intent.putExtra("allDay", false);
        intent.putExtra("rule", "FREQ=DAILY");
        intent.putExtra("endTime", cal.getTimeInMillis()+60*60*1000);
        intent.putExtra("title", "Lembrete: Na ponta do dedo");
        startActivity(intent);
    }

    public void incomePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
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
