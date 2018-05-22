package ru.prog_edu.movies;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class TrailersAdapter extends RecyclerView.Adapter<TrailersAdapter.TrailersHolder> {

    private final ArrayList<Trailer> trailerArrayList;

    public interface OnSelectedTrailerListener{
        void onListItemClick(int selectedTrailer);
    }

    private final OnSelectedTrailerListener mClickHandler;

    TrailersAdapter(ArrayList<Trailer> trailerArrayList, OnSelectedTrailerListener mClickHandler) {
        this.trailerArrayList = trailerArrayList;
        this.mClickHandler = mClickHandler;
    }

    @NonNull
    @Override
    public TrailersHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context;
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.trailer_item, parent, false);
        return new TrailersHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TrailersHolder holder, int position) {

    holder.getTrailerNameTextView().setText(trailerArrayList.get(position).getName());
    holder.getTrailerTypeTextView().setText(trailerArrayList.get(position).getType());
    holder.getTrailerSizeTextView().setText(Integer.toString(trailerArrayList.get(position).getSize()));

    }

    @Override
    public int getItemCount() {
        return trailerArrayList.size();
    }

    public class TrailersHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private final TextView trailerNameTextView;
        private final TextView trailerTypeTextView;
        private final TextView trailerSizeTextView;

        TextView getTrailerNameTextView() {
            return trailerNameTextView;
        }

        TextView getTrailerTypeTextView() {
            return trailerTypeTextView;
        }

        TextView getTrailerSizeTextView() {
            return trailerSizeTextView;
        }

        TrailersHolder(View itemView) {
            super(itemView);

            trailerNameTextView = itemView.findViewById(R.id.trailer_name);
            trailerTypeTextView = itemView.findViewById(R.id.trailer_type);
            trailerSizeTextView = itemView.findViewById(R.id.trailer_size);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int selectedTrailer = getAdapterPosition();
            mClickHandler.onListItemClick(selectedTrailer);
        }
    }
}
