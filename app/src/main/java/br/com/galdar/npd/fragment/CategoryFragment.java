package br.com.galdar.npd.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import br.com.galdar.npd.R;
import br.com.galdar.npd.activity.AddCategoryActivity;
import br.com.galdar.npd.activity.IncomeActivity;

public class CategoryFragment extends Fragment {

    private FloatingActionButton fabCategory;

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

        return view;
    }

    public void addCategory(View view) {
        startActivity(new Intent( getActivity().getApplicationContext(), AddCategoryActivity.class));
    }

}
