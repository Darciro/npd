package br.com.galdar.npd.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import br.com.galdar.npd.R;
import br.com.galdar.npd.model.Category;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.catsViewHolder> {

    private List<Category> categoryList;
    Context context;

    public CategoryAdapter(List<Category> categoryList, Context context) {
        this.categoryList = categoryList;
        this.context = context;
    }

    @NonNull
    @Override
    public catsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View listItem = LayoutInflater.from( parent.getContext() ).inflate(R.layout.adapter_category, parent, false);

        return new catsViewHolder( listItem );
    }

    @Override
    public void onBindViewHolder(@NonNull catsViewHolder catViewHolder, int position) {

        Category cat = categoryList.get( position );

        catViewHolder.name.setText( cat.getName() );
        catViewHolder.desc.setText( cat.getDescription() );

        if( cat.getType().equals("income") ){
            catViewHolder.type.setText( "Receita" );
            catViewHolder.type.setTextColor( context.getResources().getColor(R.color.colorAccentIncome) );
        } else {
            catViewHolder.type.setText( "Despesa" );
            catViewHolder.type.setTextColor( context.getResources().getColor(R.color.colorAccentExpense) );
        }

    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public class catsViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView type;
        TextView desc;

        public catsViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById( R.id.catName );
            type = itemView.findViewById( R.id.catType );
            desc = itemView.findViewById( R.id.catDesc );
        }
    }

}
