package br.com.galdar.npd.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

import br.com.galdar.npd.R;
import br.com.galdar.npd.config.FirebaseConfig;
import br.com.galdar.npd.helper.Base64Custom;
import br.com.galdar.npd.model.User;

public class IntroActivity extends com.heinrichreimersoftware.materialintro.app.IntroActivity {

    private static FirebaseAuth auth;
    private User user;
    private static DatabaseReference dbReference;
    // private SignInButton signInButton;
    private GoogleSignInClient mGoogleSignInClient;
    private static int RC_SIGN_IN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is logged and skip sliders
        checkLoggedUser();

        // Configure the slides for intro
        setButtonBackVisible(false);
        addSlide(new SimpleSlide.Builder()
                .title("Na ponta do dedo")
                .description("Gerencie suas contas com facilidade!")
                .image(R.drawable.ic_content_paste_128dp)
                .background(R.color.colorPrimary)
                // .backgroundDark(android.R.color.holo_orange_dark)
                .scrollable(false)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title("Receitas")
                .description("Adicione todos os seus ganhos para ajuda-lo a saber quanto pode gastar!")
                .image(R.drawable.ic_account_balance_wallet_128dp)
                .background(R.color.colorPrimaryIncome)
                // .backgroundDark(android.R.color.holo_orange_dark)
                .scrollable(false)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title("Despesas")
                .description("Acompanhe e crie notificações para as suas despesas")
                .image(R.drawable.ic_trending_down_128dp)
                .background(R.color.colorPrimaryExpense)
                // .backgroundDark(android.R.color.holo_orange_dark)
                .scrollable(false)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title("Comece agora mesmo!")
                .description("Crie uma conta grátis, ou faça login para aproveitar todas as vantagens de ter suas contas, na ponta do dedo!")
                .image(R.drawable.ic_mood_128dp)
                .background(R.color.colorPrimary)
                // .backgroundDark(android.R.color.holo_orange_dark)
                .scrollable(false)
                .build());

        addSlide(new FragmentSlide.Builder()
                .background(R.color.colorPrimary)
                // .backgroundDark(android.R.color.holo_orange_dark)
                .fragment((R.layout.slider_auth))
                .canGoForward(false)
                .build());

        // signInButton = findViewById(R.id.signInWithGoogle);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.npd_default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkLoggedUser();
    }

    public void login (View view) {
        startActivity( new Intent( this, LoginActivity.class ));
    }

    public void signin (View view) {
        startActivity( new Intent( this, SignInActivity.class ));
    }

    public void signinWithFacebook (View view) {
        Toast.makeText( view.getContext(), "Acessar com o Facebook está em desenvolvimento, tente na próxima versão!", Toast.LENGTH_LONG).show();
    }

    public void signinWithGoogle (View view) {
        // Toast.makeText( view.getContext(), "Acessar com o Google está em desenvolvimento, tente na próxima versão!", Toast.LENGTH_LONG).show();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            registerUserFromGoogleAccount( account );

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.i("ERROR", "signInResult:failed code=" + e.getStatusCode() );
        }
    }

    public void checkLoggedUser() {
        auth = FirebaseConfig.getFirebaseAuth();
        // auth.signOut();
        if( auth.getCurrentUser() != null ) {
            startMainActivity();
        }
    }

    public void startMainActivity () {
        startActivity( new Intent( this, MainActivity.class ));
        finish();
    }

    public void registerUserFromGoogleAccount ( final GoogleSignInAccount account ) {
        auth = FirebaseConfig.getFirebaseAuth();
        user = new User();
        user.setName( account.getDisplayName() );
        user.setEmail( account.getEmail() );
        user.setPassword( account.getIdToken() );

        auth.createUserWithEmailAndPassword( user.getEmail(), user.getPassword() ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if( task.isSuccessful() ) {

                    String userID = Base64Custom.encodeBase64( user.getEmail() );
                    user.setUserID( userID );
                    user.save();

                    Toast.makeText( IntroActivity.this, "Cadastro realizado com sucesso", Toast.LENGTH_LONG ).show();
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
                        loginUserFromGoogleAccount( account );
                    } catch (Exception e) {
                        exception = "Erro ao realizar o cadastro: " + e.getMessage();
                        e.printStackTrace();
                    }

                    Log.i("Debug NPD", exception);
                    // Toast.makeText( IntroActivity.this, exception, Toast.LENGTH_LONG ).show();
                }
            }
        });

    }

    public void loginUserFromGoogleAccount( GoogleSignInAccount account ) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText( IntroActivity.this, "Login realizado com sucesso", Toast.LENGTH_LONG ).show();
                    startMainActivity();
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Debug NPD", "signInWithCredential:failure", task.getException());
                    // Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                    // updateUI(null);
                }
            }
        });
    }
}
