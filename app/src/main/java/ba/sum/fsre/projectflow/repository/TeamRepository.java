package ba.sum.fsre.projectflow.repository;

import android.util.Log;

import java.util.List;

import ba.sum.fsre.projectflow.model.Team;
import ba.sum.fsre.projectflow.model.TeamInvitation;
import ba.sum.fsre.projectflow.model.TeamMember;
import ba.sum.fsre.projectflow.model.User;
import ba.sum.fsre.projectflow.network.SupabaseApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeamRepository {
    private SupabaseApi api;

    public TeamRepository(SupabaseApi api) {
        this.api = api;
    }

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }

    public void createTeam(String name, String description, String ownerId, DataCallback<Team> callback) {
        ba.sum.fsre.projectflow.model.CreateTeamRequest request = new ba.sum.fsre.projectflow.model.CreateTeamRequest(name, description);
        api.createTeamRpc(request).enqueue(new Callback<Team>() {
            @Override
            public void onResponse(Call<Team> call, Response<Team> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        callback.onError("Failed to create team: " + response.code() + " - " + errorBody);
                    } catch (Exception e) {
                        callback.onError("Failed to create team: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<Team> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void deleteTeam(String teamId, DataCallback<Void> callback) {
        api.deleteTeam("eq." + teamId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed to delete team");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void addMember(String teamId, String userId, String role, DataCallback<Void> callback) {
        TeamMember member = new TeamMember(teamId, userId, role);
        api.addTeamMember(member).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                     try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        callback.onError("Failed to add member: " + response.code() + " - " + errorBody);
                    } catch (Exception e) {
                        callback.onError("Failed to add member: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getMyTeams(String userId, DataCallback<List<TeamMember>> callback) {
        api.getMyTeams("eq." + userId, "*,teams(*)").enqueue(new Callback<List<TeamMember>>() {
            @Override
            public void onResponse(Call<List<TeamMember>> call, Response<List<TeamMember>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                     try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        callback.onError("Failed to fetch teams: " + response.code() + " - " + errorBody);
                    } catch (Exception e) {
                        callback.onError("Failed to fetch teams: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<TeamMember>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getTeamMembers(String teamId, DataCallback<List<TeamMember>> callback) {
        api.getTeamMembers("eq." + teamId, "*,users(*)").enqueue(new Callback<List<TeamMember>>() {
            @Override
            public void onResponse(Call<List<TeamMember>> call, Response<List<TeamMember>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch members");
                }
            }

            @Override
            public void onFailure(Call<List<TeamMember>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void updateMemberRole(String memberId, String newRole, DataCallback<Void> callback) {
        TeamMember member = new TeamMember();
        member.role = newRole;
        
        api.updateTeamMember("eq." + memberId, member).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed to update role");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
    
    public void removeMember(String memberId, DataCallback<Void> callback) {
         api.removeTeamMember("eq." + memberId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed to remove member");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void sendInvitation(String teamId, String email, String invitedBy, String role, DataCallback<Void> callback) {
        TeamInvitation invitation = new TeamInvitation(teamId, email, invitedBy, "pending", role);
        api.createInvitation(invitation).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed to send invitation");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getInvitations(String teamId, DataCallback<List<TeamInvitation>> callback) {
        api.getTeamInvitations("eq." + teamId, "*").enqueue(new Callback<List<TeamInvitation>>() {
            @Override
            public void onResponse(Call<List<TeamInvitation>> call, Response<List<TeamInvitation>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch invitations");
                }
            }

            @Override
            public void onFailure(Call<List<TeamInvitation>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void deleteInvitation(String invitationId, DataCallback<Void> callback) {
        api.deleteInvitation("eq." + invitationId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                 if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed to delete invitation");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void clearInvitationHistory(String teamId, DataCallback<Void> callback) {
        api.deleteInvitations("eq." + teamId, "neq.pending").enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                     try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        callback.onError("Failed to clear history: " + response.code() + " - " + errorBody);
                    } catch (Exception e) {
                        callback.onError("Failed to clear history: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getMyInvitations(String email, DataCallback<List<TeamInvitation>> callback) {
        api.getMyInvitations("eq." + email, "eq.pending", "*,teams(name),users:users!invited_by(first_name,last_name,email)").enqueue(new Callback<List<TeamInvitation>>() {
            @Override
            public void onResponse(Call<List<TeamInvitation>> call, Response<List<TeamInvitation>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                     try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        callback.onError("Failed to fetch invitations: " + response.code() + " - " + errorBody);
                    } catch (Exception e) {
                        callback.onError("Failed to fetch invitations: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<TeamInvitation>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void respondToInvitation(String invitationId, String status, DataCallback<Void> callback) {
        TeamInvitation update = new TeamInvitation();
        update.status = status;
        
        api.respondToInvitation("eq." + invitationId, update).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                     try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        callback.onError("Failed to respond: " + response.code() + " - " + errorBody);
                    } catch (Exception e) {
                        callback.onError("Failed to respond: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getUserProfile(String userId, DataCallback<User> callback) {
        api.getUserProfile("eq." + userId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("Failed to fetch user");
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
               callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getUserByEmail(String email, DataCallback<User> callback) {
        api.getUserByEmail("eq." + email).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("User not found");
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getTeamById(String teamId, DataCallback<Team> callback) {
        api.getTeamById("eq." + teamId).enqueue(new Callback<List<Team>>() {
            @Override
            public void onResponse(Call<List<Team>> call, Response<List<Team>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("Team not found");
                }
            }

            @Override
            public void onFailure(Call<List<Team>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}
