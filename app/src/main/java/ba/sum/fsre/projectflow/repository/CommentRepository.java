package ba.sum.fsre.projectflow.repository;

import java.util.List;
import ba.sum.fsre.projectflow.model.Comment;
import ba.sum.fsre.projectflow.network.SupabaseApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentRepository {
    private SupabaseApi api;

    public CommentRepository(SupabaseApi api) {
        this.api = api;
    }

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }

    public void getComments(String taskId, DataCallback<List<Comment>> callback) {
        // We select all comment fields (*), plus the related user details
        // Note: 'user:users(*)' relies on Supabase detecting the foreign key.
        // If your FK is named differently, it might be 'user:users!user_id_fkey(*)'
        String select = "*,user:users(*)";

        // Ordering by created_at.desc gives us newest comments first
        api.getCommentsForTask("eq." + taskId, select, "created_at.desc")
                .enqueue(new Callback<List<Comment>>() {
                    @Override
                    public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                                callback.onError("Failed to fetch comments: " + response.code() + " " + errorBody);
                            } catch (Exception e) {
                                callback.onError("Failed to fetch comments");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Comment>> call, Throwable t) {
                        callback.onError("Network error: " + t.getMessage());
                    }
                });
    }

    public void createComment(Comment comment, DataCallback<Comment> callback) {
        // "return=representation" ensures Supabase returns the created object with its new ID and timestamp
        api.createComment(comment, "return=representation").enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        callback.onError("Failed to post comment: " + errorBody);
                    } catch (Exception e) {
                        callback.onError("Failed to post comment: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}