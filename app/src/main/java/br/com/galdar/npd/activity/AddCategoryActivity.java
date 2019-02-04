package br.com.galdar.npd.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import br.com.galdar.npd.R;
import br.com.galdar.npd.model.Category;
import br.com.galdar.npd.model.Transaction;

public class AddCategoryActivity extends AppCompatActivity {

    private TextInputEditText categoryName, categoryDesc;

    private FloatingActionButton fabAddCategory;

    private Category category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        fabAddCategory = findViewById(R.id.fabAddCategory);
        // categoryType = findViewById(R.id.categoryType);
        categoryName = findViewById(R.id.categoryName);
        categoryDesc = findViewById(R.id.categoryDesc);

        getSupportActionBar().setTitle("Nova categoria");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        fabAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCategory();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void addCategory () {
        if( validateCategoryFields() ) {
            /*String curDate = incomeDate.getText().toString();
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
            finish();*/

            category = new Category();
            // category.setType( "income" );
            category.setName( categoryName.getText().toString() );
            category.setDescription( categoryDesc.getText().toString() );
            category.save("income");
        }
    }

    public Boolean validateCategoryFields () {

        String typedCategoryName = categoryName.getText().toString();
        String typedCategoryDesc = categoryDesc.getText().toString();

        // Validate fields
        if( !typedCategoryName.isEmpty() ) {
            if( !typedCategoryDesc.isEmpty() ) {
               return true;
            } else {
                Toast.makeText( AddCategoryActivity.this, "Preencha a descrição para a categoria", Toast.LENGTH_LONG ).show();
                return false;
            }
        } else {
            Toast.makeText( AddCategoryActivity.this, "Preencha um nome para a categoria", Toast.LENGTH_LONG ).show();
            return false;
        }
    }
}
