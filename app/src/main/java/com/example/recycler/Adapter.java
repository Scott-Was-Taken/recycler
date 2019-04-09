//
// Name                 Scott Thompson
// Student ID           S1507806
// Programme of Study   Computing
//
//this class is used as an adapter for performance as loading directly onto the screen causes performance issues
//for a large dataset. Since the dataset is a reasonable size and the data is also pulled from a dynamic web source
//it is important to ensure that the application is able to pass unpredictable data to the recycler without lagging/crashing.
package com.example.recycler;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.RecViewHolder> implements Filterable {
    private List<RecyclerItem> recList;
    private List<RecyclerItem> recListFull;

    class RecViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView1;
        TextView textView2;

        RecViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            textView1 = itemView.findViewById(R.id.text_view1);
            textView2 = itemView.findViewById(R.id.text_view2);
        }
    }
    //pass the arraylist of items
    Adapter(List<RecyclerItem> recList) {
        //instantiate the variable
        this.recList = recList;
        recListFull = new ArrayList<>(recList);
    }

    @NonNull
    @Override
    public RecViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item,
                parent, false);
        //create a view holder
        return new RecViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecViewHolder holder, int position) {
        //create an instance of item
        RecyclerItem currentItem = recList.get(position);
        //set the resources
        holder.imageView.setImageResource(currentItem.getImageResource());
        holder.textView1.setText(currentItem.getText1());
        holder.textView2.setText(currentItem.getText2());
    }
    //get the amount of items
    @Override
    public int getItemCount() {
        return recList.size();
    }

    @Override
    public Filter getFilter() {
        return recFilter;
    }

    private Filter recFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<RecyclerItem> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(recListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (RecyclerItem item : recListFull) {
                    if (item.getText2().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            recList.clear();
            recList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}