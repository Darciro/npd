package br.com.galdar.npd.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import br.com.galdar.npd.R;
import br.com.galdar.npd.model.Category;
import br.com.galdar.npd.model.Transaction;

public class AddCategoryActivity extends AppCompatActivity {

    private TextInputEditText categoryName, categoryDesc;
    private Spinner categoryType;
    private FloatingActionButton fabAddCategory;
    private Category category;
    private String categoryTypeSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        fabAddCategory = findViewById(R.id.fabAddCategory);
        categoryType = findViewById(R.id.categoryType);
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

        categoryType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Log.i( "XXX", parent.getItemAtPosition(position).toString() );
                Log.i("XXX", categoryType.getSelectedItem().toString() );
                categoryTypeSelected = categoryType.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void addListenerOnSpinnerItemSelection() {
        // categoryType.setOnItemSelectedListener(new CustomOnItemSelectedListener());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void addCategory () {
        if( validateCategoryFields() ) {

            String type = "";
            if( categoryTypeSelected.equals("Receita") ){
                type = "income";
            } else {
                type = "expense";
            }

            category = new Category();
            category.setName( categoryName.getText().toString() );
            category.setType( type );
            category.setDescription( categoryDesc.getText().toString() );
            category.save();

            finish();
        }
    }

    public Boolean validateCategoryFields () {

        String typedCategoryName = categoryName.getText().toString();
        String typedCategoryDesc = categoryDesc.getText().toString();

        // Validate fields
        if( !typedCategoryName.isEmpty() ) {
            return true;
            /*if( !typedCategoryDesc.isEmpty() ) {
               return true;
            } else {
                Toast.makeText( AddCategoryActivity.this, "Preencha a descrição para a categoria", Toast.LENGTH_LONG ).show();
                return false;
            }*/
        } else {
            Toast.makeText( AddCategoryActivity.this, "Preencha um nome para a categoria", Toast.LENGTH_LONG ).show();
            return false;
        }
    }
}
