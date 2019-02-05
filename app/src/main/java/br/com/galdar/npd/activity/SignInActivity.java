package br.com.galdar.npd.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import br.com.galdar.npd.R;
import br.com.galdar.npd.config.FirebaseConfig;
import br.com.galdar.npd.helper.Base64Custom;
import br.com.galdar.npd.model.User;

public class SignInActivity extends AppCompatActivity {

    private EditText signinName, signinEmail, signinPassword;
    private Button signinButton;
    private FirebaseAuth auth;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        getSupportActionBar().setTitle("Cadastro");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        signinName =  findViewById( R.id.signinName );
        signinEmail =  findViewById( R.id.signinEmail);
        signinPassword =  findViewById( R.id.signinPassword);
        signinButton = findViewById(R.id.signinButton);

        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String typedName = signinName.getText().toString();
                String typedEmail = signinEmail.getText().toString();
                String typedPassword = signinPassword.getText().toString();

                // Validate fields
                if( !typedName.isEmpty() ) {
                    if( !typedEmail.isEmpty() ) {
                        if( !typedPassword.isEmpty() ) {

                            // Everything's ok, we can continue
                            user = new User();
                            user.setName( typedName );
                            user.setEmail( typedEmail );
                            user.setPassword( typedPassword );

                            registerUser();

                        } else {
                            Toast.makeText( SignInActivity.this, "Preencha o campo senha", Toast.LENGTH_LONG ).show();
                        }
                    } else {
                        Toast.makeText( SignInActivity.this, "Preencha o campo email", Toast.LENGTH_LONG ).show();
                    }
                } else {
                    Toast.makeText( SignInActivity.this, "Preencha o campo nome", Toast.LENGTH_LONG ).show();
                }

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void registerUser () {

        auth = FirebaseConfig.getFirebaseAuth();
        auth.createUserWithEmailAndPassword( user.getEmail(), user.getPassword() ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if( task.isSuccessful() ) {

                    String userID = Base64Custom.encodeBase64( user.getEmail() );
                    user.setUserID( userID );
                    user.save();

                    Toast.makeText( SignInActivity.this, "Cadastro realizado com sucesso", Toast.LENGTH_LONG ).show();
                    finish();

                } else {

                    String exception = "";

                    try {
                        throw task.getException();
                    } catch ( FirebaseAuthWeakPasswordException e ) {
                        exception = "Senha fraca";
                    } catch ( FirebaseAuthInvalidCredentialsException e ) {
                        exception = "Digite um email válido";
                    } catch ( FirebaseAuthUserCollisionException e ) {
                        exception = "Esse email já está sendo utilizado";
                    } catch (Exception e) {
                        exception = "Erro ao realizar o cadastro: " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText( SignInActivity.this, exception, Toast.LENGTH_LONG ).show();
                }
            }
        });

    }
}
