package ba.sum.fsre.projectflow.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import ba.sum.fsre.projectflow.model.Team;
import ba.sum.fsre.projectflow.model.TeamInvitation;
import ba.sum.fsre.projectflow.model.TeamMember;
import ba.sum.fsre.projectflow.network.RetrofitClient;
import ba.sum.fsre.projectflow.network.SupabaseApi;
import ba.sum.fsre.projectflow.repository.TeamRepository;
import ba.sum.fsre.projectflow.storage.TokenManager;

public class TeamViewModel extends ViewModel {

    private final MutableLiveData<List<TeamMember>> myTeams = new MutableLiveData<>();
    private final MutableLiveData<List<TeamMember>> teamMembers = new MutableLiveData<>();
    private final MutableLiveData<List<TeamInvitation>> teamInvitations = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasPendingInvitations = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navigateBack = new MutableLiveData<>();

    public LiveData<List<TeamMember>> getMyTeams() { return myTeams; }
    public LiveData<List<TeamMember>> getTeamMembers() { return teamMembers; }
    public LiveData<List<TeamInvitation>> getTeamInvitations() { return teamInvitations; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<Boolean> getOperationSuccess() { return operationSuccess; }
    public LiveData<Boolean> getHasPendingInvitations() { return hasPendingInvitations; }
    public LiveData<Boolean> getNavigateBack() { return navigateBack; }

    private TeamRepository getRepository(Context context) {
        SupabaseApi api = RetrofitClient.getClient(context).create(SupabaseApi.class);
        return new TeamRepository(api);
    }

    public void loadMyTeams(Context context) {
        loading.setValue(true);
        TokenManager tm = new TokenManager(context);
        String userId = tm.getUserId();

        getRepository(context).getMyTeams(userId, new TeamRepository.DataCallback<List<TeamMember>>() {
            @Override
            public void onSuccess(List<TeamMember> data) {
                loading.setValue(false);
                myTeams.setValue(data);
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }

    public void createTeam(Context context, String name, String description) {
        loading.setValue(true);
        TokenManager tm = new TokenManager(context);
        String userId = tm.getUserId();

        getRepository(context).createTeam(name, description, userId, new TeamRepository.DataCallback<Team>() {
            @Override
            public void onSuccess(Team data) {
                loading.setValue(false);
                operationSuccess.setValue(true);
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }

    public void loadTeamDetails(Context context, String teamId) {
        loading.setValue(true);
        TeamRepository repo = getRepository(context);
        
        repo.getTeamMembers(teamId, new TeamRepository.DataCallback<List<TeamMember>>() {
            @Override
            public void onSuccess(List<TeamMember> members) {
                teamMembers.setValue(members);
                    repo.getInvitations(teamId, new TeamRepository.DataCallback<List<TeamInvitation>>() {
                    @Override
                    public void onSuccess(List<TeamInvitation> invitations) {
                        loading.setValue(false);
                        teamInvitations.setValue(invitations);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        loading.setValue(false);
                        error.setValue(errorMessage);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }
    
    public void inviteMember(Context context, String teamId, String email, String role) {
        loading.setValue(true);
        TokenManager tm = new TokenManager(context);
        String userId = tm.getUserId();
        
        TeamRepository repo = getRepository(context);
        
        repo.getUserByEmail(email, new TeamRepository.DataCallback<ba.sum.fsre.projectflow.model.User>() {
            @Override
            public void onSuccess(ba.sum.fsre.projectflow.model.User user) {
                repo.sendInvitation(teamId, email, userId, role, new TeamRepository.DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        loading.setValue(false);
                        operationSuccess.setValue(true);
                        loadTeamDetails(context, teamId);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        loading.setValue(false);
                        error.setValue(errorMessage);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }
    
    public void deleteInvitation(Context context, String teamId, String invitationId) {
        loading.setValue(true);
        getRepository(context).deleteInvitation(invitationId, new TeamRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                loading.setValue(false);
                loadTeamDetails(context, teamId);
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }

    public void clearInvitationHistory(Context context, String teamId) {
        loading.setValue(true);
        getRepository(context).clearInvitationHistory(teamId, new TeamRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                loading.setValue(false);
                loadTeamDetails(context, teamId);
                operationSuccess.setValue(true);
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }
    
    public void updateRole(Context context, String teamId, String memberId, String newRole) {
        loading.setValue(true);
        getRepository(context).updateMemberRole(memberId, newRole, new TeamRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                loading.setValue(false);
                loadTeamDetails(context, teamId);
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }

    public void removeMember(Context context, String teamId, String memberId) {
        loading.setValue(true);
        getRepository(context).removeMember(memberId, new TeamRepository.DataCallback<Void>() {
             @Override
            public void onSuccess(Void data) {
                loading.setValue(false);
                loadTeamDetails(context, teamId);
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }


    public void loadMyInvitations(Context context) {
        loading.setValue(true);
        TokenManager tm = new TokenManager(context);
        String userId = tm.getUserId();
        
        TeamRepository repo = getRepository(context);
        repo.getUserProfile(userId, new TeamRepository.DataCallback<ba.sum.fsre.projectflow.model.User>() {
            @Override
            public void onSuccess(ba.sum.fsre.projectflow.model.User user) {
                if (user != null && user.email != null) {
                    repo.getMyInvitations(user.email, new TeamRepository.DataCallback<List<TeamInvitation>>() {
                        @Override
                        public void onSuccess(List<TeamInvitation> data) {
                             loading.setValue(false);
                             teamInvitations.setValue(data);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            loading.setValue(false);
                            error.setValue(errorMessage);
                        }
                    });
                } else {
                     loading.setValue(false);
                     error.setValue("User email not found");
                }
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }

    public void respondToInvitation(Context context, String invitationId, String status) {
        loading.setValue(true);
        getRepository(context).respondToInvitation(invitationId, status, new TeamRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                 loadMyInvitations(context);
                 operationSuccess.setValue(true);
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }

    public void acceptInvitation(Context context, TeamInvitation invitation) {
        loading.setValue(true);
        TeamRepository repo = getRepository(context);
        
        repo.respondToInvitation(invitation.id, "accepted", new TeamRepository.DataCallback<Void>() {
             @Override
             public void onSuccess(Void data) {
                 loadMyInvitations(context);
                 operationSuccess.setValue(true);
             }
 
             @Override
             public void onError(String errorMessage) {
                 loading.setValue(false);
                 error.setValue(errorMessage);
             }
         });
    }

    public void deleteTeam(Context context, String teamId) {
        loading.setValue(true);
        getRepository(context).deleteTeam(teamId, new TeamRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                loading.setValue(false);
                operationSuccess.setValue(true);
                navigateBack.setValue(true);
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }

    public void leaveTeam(Context context, String teamId) {
        loading.setValue(true);
        TokenManager tm = new TokenManager(context);
        String userId = tm.getUserId();
        
        TeamRepository repo = getRepository(context);
        repo.getTeamMembers(teamId, new TeamRepository.DataCallback<List<TeamMember>>() {
            @Override
            public void onSuccess(List<TeamMember> members) {
                String myMemberId = null;
                for (TeamMember m : members) {
                    if (m.userId.equals(userId)) {
                        myMemberId = m.id;
                        break;
                    }
                }
                
                if (myMemberId != null) {
                    repo.removeMember(myMemberId, new TeamRepository.DataCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            loading.setValue(false);
                            operationSuccess.setValue(true);
                            navigateBack.setValue(true);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            loading.setValue(false);
                            error.setValue(errorMessage);
                        }
                    });
                } else {
                    loading.setValue(false);
                    error.setValue("Could not find team member record to leave.");
                }
            }

            @Override
            public void onError(String errorMessage) {
                loading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }
    public void checkPendingInvitations(Context context) {
        TokenManager tm = new TokenManager(context);
        String userId = tm.getUserId();
        
        TeamRepository repo = getRepository(context);
        repo.getUserProfile(userId, new TeamRepository.DataCallback<ba.sum.fsre.projectflow.model.User>() {
            @Override
            public void onSuccess(ba.sum.fsre.projectflow.model.User user) {
                if (user != null && user.email != null) {
                    repo.getMyInvitations(user.email, new TeamRepository.DataCallback<List<TeamInvitation>>() {
                        @Override
                        public void onSuccess(List<TeamInvitation> data) {
                             if (data != null && !data.isEmpty()) {
                                 hasPendingInvitations.setValue(true);
                             } else {
                                 hasPendingInvitations.setValue(false);
                             }
                        }

                        @Override
                        public void onError(String errorMessage) {

                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {

            }
        });
    }
}
