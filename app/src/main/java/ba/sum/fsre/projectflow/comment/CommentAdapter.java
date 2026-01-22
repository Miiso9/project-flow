package ba.sum.fsre.projectflow.comment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import ba.sum.fsre.projectflow.R;
import ba.sum.fsre.projectflow.model.Comment;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> comments = new ArrayList<>();
    private String currentUserId;
    private OnCommentActionListener listener;

    public interface OnCommentActionListener {
        void onDeleteClick(Comment comment);
    }

    public CommentAdapter(String currentUserId, OnCommentActionListener listener) {
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView userName, date, text;
        ImageButton btnDelete;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.commentUserName);
            date = itemView.findViewById(R.id.commentDate);
            text = itemView.findViewById(R.id.commentText);
            btnDelete = itemView.findViewById(R.id.btnDeleteComment);
        }

        void bind(Comment comment) {
            if (comment.user != null) {
                String fullName = (comment.user.first_name + " " + comment.user.last_name).trim();
                userName.setText(fullName.isEmpty() ? comment.user.email : fullName);
            } else {
                userName.setText("Unknown User");
            }

            text.setText(comment.comment);

            if (comment.createdAt != null && comment.createdAt.length() >= 10) {
                date.setText(comment.createdAt.substring(0, 10));
            }

            if (currentUserId != null && currentUserId.equals(comment.userId)) {
                btnDelete.setVisibility(View.VISIBLE);
                btnDelete.setOnClickListener(v -> {
                    if (listener != null) listener.onDeleteClick(comment);
                });
            } else {
                btnDelete.setVisibility(View.GONE);
            }
        }
    }
}