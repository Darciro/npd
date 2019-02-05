package br.com.galdar.npd.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import br.com.galdar.npd.R;
import br.com.galdar.npd.activity.AddCategoryActivity;
import br.com.galdar.npd.activity.IncomeActivity;
import br.com.galdar.npd.adapter.CategoryAdapter;
import br.com.galdar.npd.adapter.TransactionsAdapter;
import br.com.galdar.npd.config.FirebaseConfig;
import br.com.galdar.npd.helper.Base64Custom;
import br.com.galdar.npd.model.Category;
import br.com.galdar.npd.model.Transaction;

/**
 * A simple {@link Fragment} subclass.
 */
public class IncomeFragment extends Fragment {


    private FloatingActionButton fabCategory;

    private RecyclerView recyclerIncomes;
    private TransactionsAdapter transactionsAdapter;
    private List<Transaction> transactionsList = new ArrayList<>();
    private Transaction transaction;

    private FirebaseAuth auth = FirebaseConfig.getFirebaseAuth();

    private DatabaseReference dbReference = FirebaseConfig.getFirebaseDatabase();
    private DatabaseReference userRef = FirebaseConfig.getFirebaseDatabase();
    private DatabaseReference transactionsRef;

    private ValueEventListener valueEventListenerFromTransactions;

    public void onStart() {
        super.onStart();
        getIncomes();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Receitas");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_income, container, false);

        fabCategory = view.findViewById( R.id.fabAddIncome );
        fabCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addIncome(v);
            }
        });

        recyclerIncomes = view.findViewById(R.id.recyclerIncomes);
        transactionsAdapter = new TransactionsAdapter(transactionsList, getActivity().getApplicationContext() );

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerIncomes.setLayoutManager(layoutManager);
        recyclerIncomes.setHasFixedSize(true);
        recyclerIncomes.addItemDecoration( new DividerItemDecoration( getActivity().getApplicationContext(), LinearLayout.VERTICAL) );
        recyclerIncomes.setAdapter(transactionsAdapter);

        return view;
    }

    public void addIncome(View view) {
        startActivity(new Intent( getActivity().getApplicationContext(), IncomeActivity.class));
    }

    public void getIncomes () {
        String userEmail = auth.getCurrentUser().getEmail();
        String userID = Base64Custom.encodeBase64(userEmail);
        transactionsRef = dbReference.child("transactions").child( userID );

        valueEventListenerFromTransactions = transactionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                transactionsList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren() ) {

                    for( DataSnapshot d: data.getChildren() ){

                        if( d.child("type").getValue().equals( "income" ) ){
                            Transaction transaction = d.getValue(Transaction.class);
                            transaction.setID( d.getKey() );
                            transactionsList.add(transaction);
                        }
                    }
                }

                transactionsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
