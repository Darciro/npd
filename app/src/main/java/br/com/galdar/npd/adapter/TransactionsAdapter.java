package br.com.galdar.npd.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

import br.com.galdar.npd.R;
import br.com.galdar.npd.model.Transaction;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.MyViewHolder> {

    List<Transaction> transactions;
    Context context;

    public TransactionsAdapter(List<Transaction> transactions, Context context) {
        this.transactions = transactions;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_transaction, parent, false);
        return new MyViewHolder(itemLista);
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        holder.title.setText(transaction.getDescription());
        holder.value.setText(String.valueOf(transaction.getValue()));
        holder.category.setText(transaction.getCategory());
        holder.value.setTextColor( context.getResources().getColor(R.color.colorAccentIncome) );

        if ( transaction.getType().equals("expense") ) {
            holder.value.setTextColor( context.getResources().getColor(R.color.colorAccentExpense) );
            holder.value.setText("-" + transaction.getValue());
        }
    }


    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView title, value, category;

        public MyViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.textAdapterTitle);
            value = itemView.findViewById(R.id.textAdapterValue);
            category = itemView.findViewById(R.id.textAdapterCategory);
        }

    }

}
