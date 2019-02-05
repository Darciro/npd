package br.com.galdar.npd.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import br.com.galdar.npd.R;
import br.com.galdar.npd.config.FirebaseConfig;
import br.com.galdar.npd.model.User;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private Button loginButton;
    private User user;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        loginEmail = findViewById( R.id.loginEmail );
        loginPassword = findViewById( R.id.loginPassword );
        loginButton = findViewById( R.id.loginButton );

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String typedEmail = loginEmail.getText().toString();
                String typedPassword = loginPassword.getText().toString();

                if( !typedEmail.isEmpty() ) {
                    if( !typedPassword.isEmpty() ) {

                        // Everything's ok, we can continue
                        user = new User();
                        user.setEmail( typedEmail );
                        user.setPassword( typedPassword );

                        validateLogin();

                    } else {
                        Toast.makeText( LoginActivity.this, "Preencha o campo senha", Toast.LENGTH_LONG ).show();
                    }
                } else {
                    Toast.makeText( LoginActivity.this, "Preencha o campo email", Toast.LENGTH_LONG ).show();
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void validateLogin () {

        auth = FirebaseConfig.getFirebaseAuth();
        auth.signInWithEmailAndPassword( user.getEmail(), user.getPassword() ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if( task.isSuccessful() ) {

                    Toast.makeText( LoginActivity.this, "Login realizado com sucesso", Toast.LENGTH_LONG ).show();
                    openMainActivity();

                } else {

                    String exception = "";

                    try {
                        throw task.getException();
                    } catch ( FirebaseAuthInvalidCredentialsException e ) {
                        exception = "Email e senha não correspondem.";
                    } catch ( FirebaseAuthInvalidUserException e ) {
                        exception = "Esse email não foi encontrado";
                    } catch (Exception e) {
                        exception = "Erro ao realizar o login: " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText( LoginActivity.this, "Erro ao realizar o login", Toast.LENGTH_LONG ).show();
                }
            }
        });

    }

    public void openMainActivity () {
        startActivity( new Intent( this, MainActivity.class ));
        finish();
    }
}
