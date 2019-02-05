package br.com.galdar.npd.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

import br.com.galdar.npd.R;
import br.com.galdar.npd.config.FirebaseConfig;

public class IntroActivity extends com.heinrichreimersoftware.materialintro.app.IntroActivity {

    private static FirebaseAuth auth;
    private static DatabaseReference dbReference;

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
        Toast.makeText( view.getContext(), "Acessar com o Google está em desenvolvimento, tente na próxima versão!", Toast.LENGTH_LONG).show();
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
}
