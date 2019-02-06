package br.com.galdar.npd.fragment;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import br.com.galdar.npd.activity.ExpenseActivity;
import br.com.galdar.npd.activity.IncomeActivity;
import br.com.galdar.npd.adapter.TransactionsAdapter;
import br.com.galdar.npd.config.FirebaseConfig;
import br.com.galdar.npd.helper.Base64Custom;
import br.com.galdar.npd.model.Transaction;
import br.com.galdar.npd.model.User;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    private MaterialCalendarView transactionsCalendar;
    private TextView balanceMonthText, balanceValue;
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
    private CharSequence meses[] = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};

    public MainFragment() {
        // Required empty public constructor
    }

    public void onStart() {
        super.onStart();
        getBalance();
        getTransacions();
    }

    public void onStop() {
        super.onStop();
        userRef.removeEventListener(valueEventListenerFromUser);
        transactionsRef.removeEventListener(valueEventListenerFromTransactions);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        NavigationView navigationView = getActivity().findViewById( R.id.nav_view );
        View headerView = navigationView.getHeaderView(0);

        userNameText = headerView.findViewById(R.id.userName);
        userEmailText = headerView.findViewById(R.id.userEmail);

        transactionsCalendar = view.findViewById(R.id.transactionsCalendar);
        // loadingText = view.findViewById(R.id.loadingText);
        balanceValue = view.findViewById(R.id.balanceValue);
        recyclerTransactions = view.findViewById(R.id.recyclerTransactions);
        balanceMonthText = view.findViewById(R.id.balanceMonthText);

        configCalendarView();
        // swipe();

        transactionsAdapter = new TransactionsAdapter(transactionsList, getActivity().getApplicationContext() );

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerTransactions.setLayoutManager(layoutManager);
        recyclerTransactions.setHasFixedSize(true);
        recyclerTransactions.addItemDecoration( new DividerItemDecoration( getActivity().getApplicationContext(), LinearLayout.VERTICAL) );
        recyclerTransactions.setAdapter(transactionsAdapter);

        return view;
    }

    public void getBalance() {

        final String userEmail = auth.getCurrentUser().getEmail();
        String userID = Base64Custom.encodeBase64(userEmail);
        transactionsRef = dbReference.child("transactions").child(userID).child(monthYearSelected);

        valueEventListenerFromUser = transactionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                transactionsList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    // Transaction transaction = data.getValue(Transaction.class);
                    if( data.child("type").getValue().toString().equals("expense") ){
                        Class cls = data.child("value").getValue().toString().getClass(); // cls.getName() To return the type for this object
                        // Log.i("XXX", data.child("value").getValue().toString() + " " + cls.getName() );
                        expensesTotal += Double.parseDouble( data.child("value").getValue().toString() );
                    } else if( data.child("type").getValue().toString().equals("income") ){
                        incomesTotal += Double.parseDouble( data.child("value").getValue().toString() );
                    }
                }

                userBalance = incomesTotal - expensesTotal;
                DecimalFormat decimalFormat = new DecimalFormat("0.##;-0.##");
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
                expensesTotal = 0.0;
                incomesTotal = 0.0;

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Transaction transaction = data.getValue(Transaction.class);
                    transaction.setID(data.getKey());
                    transactionsList.add(transaction);

                    if( data.child("type").getValue().toString().equals("expense") ){
                        expensesTotal += Double.parseDouble( data.child("value").getValue().toString() );
                    } else if( data.child("type").getValue().toString().equals("income") ){
                        incomesTotal += Double.parseDouble( data.child("value").getValue().toString() );
                    }
                }

                transactionsAdapter.notifyDataSetChanged();
                userBalance = incomesTotal - expensesTotal;
                DecimalFormat decimalFormat = new DecimalFormat("0.##;-0.##");
                balanceValue.setText("R$ " + decimalFormat.format(userBalance));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void excludeTransaction(final RecyclerView.ViewHolder viewHolder) {
        AlertDialog.Builder confirmDialog = new AlertDialog.Builder( getActivity().getApplicationContext()  );

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
        startActivity(new Intent(getActivity().getApplicationContext(), IncomeActivity.class));
    }

    public void addExpense(View view) {
        startActivity(new Intent(getActivity().getApplicationContext(), ExpenseActivity.class));
    }

    public void configCalendarView() {

        transactionsCalendar.setTitleMonths(meses);

        CalendarDay curDate = transactionsCalendar.getCurrentDate();
        String monthFormated = String.format("%02d", (curDate.getMonth()));
        monthYearSelected = String.valueOf(monthFormated + "" + curDate.getYear());
        balanceMonthText.setText( "Saldo total para o mês de " + meses[ curDate.getMonth() -1 ] );

        transactionsCalendar.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {

                String monthFormated = String.format("%02d", (date.getMonth()));
                balanceMonthText.setText( "Saldo total para o mês de " + meses[ date.getMonth() -1 ] );
                monthYearSelected = String.valueOf(monthFormated + "" + date.getYear());
                // transactionsRef.removeEventListener(valueEventListenerFromTransactions);
                getTransacions();
            }
        });
    }

    /*public void swipe() {
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
    }*/

}
