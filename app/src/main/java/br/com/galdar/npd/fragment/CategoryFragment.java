package br.com.galdar.npd.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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

public class CategoryFragment extends Fragment {

    private FloatingActionButton fabCategory;

    private RecyclerView recyclerCategories;

    private RecyclerView.Adapter recyclerCategoriesAdapter;
    private RecyclerView.LayoutManager recyclerCategoriesLayoutManager;
    private List<Category> categoriesList = new ArrayList<>();

    private FirebaseAuth auth = FirebaseConfig.getFirebaseAuth();

    private DatabaseReference dbReference = FirebaseConfig.getFirebaseDatabase();
    private DatabaseReference userRef = FirebaseConfig.getFirebaseDatabase();
    private DatabaseReference transactionsRef;

    private ValueEventListener valueEventListenerFromTransactions;

    public void onStart() {
        super.onStart();
        getCategories();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);

        fabCategory = view.findViewById( R.id.fabCategory );
        fabCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCategory(v);
            }
        });

        recyclerCategories = view.findViewById( R.id.recyclerCategories );
        recyclerCategories.setHasFixedSize( true );
        recyclerCategories.addItemDecoration( new DividerItemDecoration( getActivity().getApplicationContext(), LinearLayout.VERTICAL) );

        recyclerCategoriesLayoutManager = new LinearLayoutManager( getActivity().getApplicationContext() );
        recyclerCategories.setLayoutManager( recyclerCategoriesLayoutManager );

        recyclerCategoriesAdapter = new CategoryAdapter( categoriesList, getActivity().getApplicationContext() );
        recyclerCategories.setAdapter( recyclerCategoriesAdapter );

        return view;
    }

    public void addCategory(View view) {
        startActivity(new Intent( getActivity().getApplicationContext(), AddCategoryActivity.class));
    }

    public void getCategories () {
        String userEmail = auth.getCurrentUser().getEmail();
        String userID = Base64Custom.encodeBase64(userEmail);
        transactionsRef = dbReference.child("users").child(userID).child("categories");

        valueEventListenerFromTransactions = transactionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                categoriesList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren() ) {
                    Category category = data.getValue( Category.class );
                    category.setID(data.getKey());
                    categoriesList.add(category);
                }

                recyclerCategoriesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
