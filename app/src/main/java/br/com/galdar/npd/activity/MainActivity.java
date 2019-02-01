package br.com.galdar.npd.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

    @Override
    protected void onStart() {
        super.onStart();
        getBalance();
        getTransacions();
    }

    @Override
    protected void onStop() {
        super.onStop();
        userRef.removeEventListener(valueEventListenerFromUser);
        transactionsRef.removeEventListener(valueEventListenerFromTransactions);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);

        userNameText = headerView.findViewById(R.id.userName);
        userEmailText = headerView.findViewById(R.id.userEmail);

        transactionsCalendar = findViewById(R.id.transactionsCalendar);
        loadingText = findViewById(R.id.loadingText);
        balanceValue = findViewById(R.id.balanceValue);
        recyclerTransactions = findViewById(R.id.recyclerTransactions);

        configCalendarView();
        swipe();

        transactionsAdapter = new TransactionsAdapter(transactionsList, this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerTransactions.setLayoutManager(layoutManager);
        recyclerTransactions.setHasFixedSize(true);
        recyclerTransactions.addItemDecoration( new DividerItemDecoration( this, LinearLayout.VERTICAL) );
        recyclerTransactions.setAdapter(transactionsAdapter);
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
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
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else if (id == R.id.nav_incomes) {

        } else if (id == R.id.nav_expenses) {

        } else if (id == R.id.nav_categories) {
            Toast.makeText( this, "Em desenvolvimento", Toast.LENGTH_LONG).show();
        } else if (id == R.id.nav_share) {
            shareNPD();
        } else if (id == R.id.nav_signout) {
            auth = FirebaseConfig.getFirebaseAuth();
            auth.signOut();
            startActivity(new Intent(this, IntroActivity.class));
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void getBalance() {

        final String userEmail = auth.getCurrentUser().getEmail();
        String userID = Base64Custom.encodeBase64(userEmail);
        userRef = dbReference.child("users").child(userID);

        valueEventListenerFromUser = userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                expensesTotal = user.getExpensesTotal();
                incomesTotal = user.getIncomesTotal();
                userBalance = incomesTotal - expensesTotal;

                DecimalFormat decimalFormat = new DecimalFormat("0.##");

                userNameText.setText(user.getName());
                userEmailText.setText(userEmail);
                balanceValue.setText("R$ " + decimalFormat.format(userBalance));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void getTransacions() {
        String userEmail = auth.getCurrentUser().getEmail();
        String userID = Base64Custom.encodeBase64(userEmail);
        transactionsRef = dbReference.child("transactions").child(userID).child(monthYearSelected);

        valueEventListenerFromTransactions = transactionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                transactionsList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Transaction transaction = data.getValue(Transaction.class);
                    transaction.setID(data.getKey());
                    transactionsList.add(transaction);
                }

                transactionsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void excludeTransaction(final RecyclerView.ViewHolder viewHolder) {
        AlertDialog.Builder confirmDialog = new AlertDialog.Builder(this);

        confirmDialog.setTitle("Excluir item");
        confirmDialog.setMessage("Tem certeza que deseja realemnte excluir este item?");
        confirmDialog.setCancelable(false);

        confirmDialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int pos = viewHolder.getAdapterPosition();
                transaction = transactionsList.get(pos);
                String userEmail = auth.getCurrentUser().getEmail();
                String userID = Base64Custom.encodeBase64(userEmail);
                transactionsRef = dbReference.child("transactions").child(userID).child(monthYearSelected);
                transactionsRef.child(transaction.getID()).removeValue();
                transactionsAdapter.notifyItemRemoved(pos);

                updateBalance();
            }
        });

        confirmDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                transactionsAdapter.notifyDataSetChanged();
            }
        });

        AlertDialog a = confirmDialog.create();
        a.show();

    }

    public void updateBalance() {
        String userEmail = auth.getCurrentUser().getEmail();
        String userID = Base64Custom.encodeBase64(userEmail);
        userRef = dbReference.child("users").child(userID);

        if (transaction.getType().equals("income")) {
            incomesTotal = incomesTotal - transaction.getValue();
            userRef.child("incomesTotal").setValue(incomesTotal);
        }

        if (transaction.getType().equals("expense")) {
            expensesTotal = expensesTotal - transaction.getValue();
            userRef.child("expensesTotal").setValue(expensesTotal);
        }
    }

    public void addIncome(View view) {
        startActivity(new Intent(this, IncomeActivity.class));
    }

    public void addExpense(View view) {
        startActivity(new Intent(this, ExpenseActivity.class));
    }

    public void configCalendarView() {

        CharSequence meses[] = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
        transactionsCalendar.setTitleMonths(meses);

        CalendarDay curDate = transactionsCalendar.getCurrentDate();
        String monthFormated = String.format("%02d", (curDate.getMonth()));
        monthYearSelected = String.valueOf(monthFormated + "" + curDate.getYear());

        transactionsCalendar.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                String monthFormated = String.format("%02d", (date.getMonth()));
                monthYearSelected = String.valueOf(monthFormated + "" + date.getYear());
                Log.i("XXX", monthFormated + ", " + monthYearSelected);
                transactionsRef.removeEventListener(valueEventListenerFromTransactions);
                getTransacions();
            }
        });
    }

    public void swipe() {
        ItemTouchHelper.Callback itemTouch = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int dragFlag = ItemTouchHelper.ACTION_STATE_IDLE; // Disable drag and drop
                int swipeFlag = ItemTouchHelper.END; // ItemTouchHelper.START | ItemTouchHelper.END => For swipe for both directions (left and right)
                return makeMovementFlags(dragFlag, swipeFlag);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                excludeTransaction(viewHolder);
            }
        };

        new ItemTouchHelper(itemTouch).attachToRecyclerView(recyclerTransactions);
    }

    public void shareNPD() {
        Intent email = new Intent( Intent.ACTION_SEND );
        // email.putExtra( Intent.EXTRA_EMAIL, new String[]{ "rickmanu@gmail.com", "contato@galdar.com.br" } );
        email.putExtra( Intent.EXTRA_SUBJECT, "Checkout this new brand aap" );
        email.putExtra( Intent.EXTRA_TEXT, "Na ponta do dedo, suas contas de modo fácil! Adiquira em http://galdar.com.br" );
        email.setType( "message/rfc822" ); // Type for send email message
        // email.setType( "application/pdf" ); // Type for open pdf
        // email.setType( "image/png" ); // Type for open image png

        startActivity( Intent.createChooser( email, "Escolha o app para enviar o email" ) );
    }
}
