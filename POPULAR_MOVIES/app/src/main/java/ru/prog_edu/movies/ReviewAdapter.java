package ru.prog_edu.movies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewHolder> {
    private final ArrayList<Review> reviewArrayList;

    ReviewAdapter(ArrayList<Review> reviewArrayList) {
        this.reviewArrayList = reviewArrayList;
    }

    @NonNull
    @Override
    public ReviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context;
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.review_item, parent, false);
        return new ReviewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewHolder holder, int position) {
        holder.getReviewAuthorTextView().setText(reviewArrayList.get(position).getAuthor());
        holder.getReviewContentTextView().setText(reviewArrayList.get(position).getContent());
    }

    @Override
    public int getItemCount() {
        return reviewArrayList.size();
    }


    class ReviewHolder extends RecyclerView.ViewHolder {

        private final TextView reviewAuthorTextView;
        private final TextView reviewContentTextView;

        TextView getReviewAuthorTextView() {
            return reviewAuthorTextView;
        }

        TextView getReviewContentTextView() {
            return reviewContentTextView;
        }

        ReviewHolder(View itemView) {
            super(itemView);
            reviewAuthorTextView = itemView.findViewById(R.id.tv_author);
            reviewContentTextView = itemView.findViewById(R.id.tv_content);

        }
    }
}
