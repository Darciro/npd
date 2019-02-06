package br.com.galdar.npd.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import br.com.galdar.npd.R;
import br.com.galdar.npd.adapter.TransactionsAdapter;
import br.com.galdar.npd.config.FirebaseConfig;
import br.com.galdar.npd.fragment.CategoryFragment;
import br.com.galdar.npd.fragment.ExpenseFragment;
import br.com.galdar.npd.fragment.IncomeFragment;
import br.com.galdar.npd.fragment.MainFragment;
import br.com.galdar.npd.helper.Base64Custom;
import br.com.galdar.npd.model.Transaction;
import br.com.galdar.npd.model.User;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private MaterialCalendarView transactionsCalendar;
    private TextView loadingText, balanceValue;
    private RecyclerView recyclerTransactions;
    private TransactionsAdapter transactionsAdapter;
    private List<Transaction> transactionsList = new ArrayList<>();
    private Transaction transaction;

    private FirebaseAuth auth = FirebaseConfig.getFirebaseAuth();

    private DatabaseReference dbReference = FirebaseConfig.getFirebaseDatabase();
    private DatabaseReference userRef = FirebaseConfig.getFirebaseDatabase();
    private DatabaseReference transactionsRef;

    private ValueEventListener valueEventListenerFromUser;
    private ValueEventListener valueEventListenerFromTransactions;

    private TextView userNameText;
    private TextView userEmailText;

    private Double expensesTotal = 0.0;
    private Double incomesTotal = 0.0;
    private Double userBalance = 0.0;

    private String monthYearSelected;

    private FrameLayout frameLayout;

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        frameLayout = findViewById(R.id.frameContainer);
        MainFragment mainFragment = new MainFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameContainer, mainFragment);
        fragmentTransaction.commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Toast.makeText( this, "Em desenvolvimento", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {

            MainFragment mainFragment = new MainFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frameContainer, mainFragment);
            fragmentTransaction.commit();

        } else if (id == R.id.nav_incomes) {

            IncomeFragment incomeFragment = new IncomeFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frameContainer, incomeFragment);
            fragmentTransaction.commit();

        } else if (id == R.id.nav_expenses) {

            ExpenseFragment expenseFragment = new ExpenseFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frameContainer, expenseFragment);
            fragmentTransaction.commit();

        } else if (id == R.id.nav_categories) {

            // Toast.makeText( this, "Em desenvolvimento", Toast.LENGTH_LONG).show();
            CategoryFragment categoryFragment = new CategoryFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace( R.id.frameContainer, categoryFragment );
            fragmentTransaction.commit();

        } else if (id == R.id.nav_share) {

            shareNPD();

        } else if (id == R.id.nav_signout) {

            auth = FirebaseConfig.getFirebaseAuth();
            auth.signOut();

            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    });

            mGoogleSignInClient.revokeAccess()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    });

            startActivity(new Intent(this, IntroActivity.class));
            finish();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void addIncome(View view) {
        startActivity(new Intent(this, IncomeActivity.class));
    }

    public void addExpense(View view) {
        startActivity(new Intent(this, ExpenseActivity.class));
    }

    public void shareNPD() {
        Intent email = new Intent( Intent.ACTION_SEND );
        // email.putExtra( Intent.EXTRA_EMAIL, new String[]{ "rickmanu@gmail.com", "contato@galdar.com.br" } );
        email.putExtra( Intent.EXTRA_SUBJECT, "Checkout this new brand aap" );
        email.putExtra( Intent.EXTRA_TEXT, "Na ponta do dedo, suas contas de modo fácil! Adiquira em https://play.google.com/store/apps/details?id=br.com.galdar.npd" );
        email.setType( "message/rfc822" ); // Type for send email message
        // email.setType( "application/pdf" ); // Type for open pdf
        // email.setType( "image/png" ); // Type for open image png

        startActivity( Intent.createChooser( email, "Escolha o app para enviar o email" ) );
    }
}
