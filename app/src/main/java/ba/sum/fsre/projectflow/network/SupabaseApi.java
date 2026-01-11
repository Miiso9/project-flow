package ba.sum.fsre.projectflow.network;

import java.util.List;

import ba.sum.fsre.projectflow.model.AuthResponse;
import ba.sum.fsre.projectflow.model.LoginRequest;
import ba.sum.fsre.projectflow.model.RegisterRequest;
import ba.sum.fsre.projectflow.model.Team;
import ba.sum.fsre.projectflow.model.TeamInvitation;
import ba.sum.fsre.projectflow.model.TeamMember;
import ba.sum.fsre.projectflow.model.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseApi {

    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> login(@Body LoginRequest body);

    @POST("auth/v1/signup")
    Call<AuthResponse> register(@Body RegisterRequest body);

    @GET("rest/v1/users")
    Call<List<User>> getUserProfile(@Query("id") String filter, @Query("select") String select);

    @PATCH("rest/v1/users")
    Call<List<User>> updateUserProfile(@Query("id") String filter, @Body User user);

    @GET("rest/v1/users")
    Call<List<User>> getUserProfile(@Query("id") String idFilter);

    @GET("rest/v1/users")
    Call<List<User>> getUserByEmail(@Query("email") String emailFilter);


    @POST("rest/v1/rpc/create_team_with_owner")
    Call<Team> createTeamRpc(@Body ba.sum.fsre.projectflow.model.CreateTeamRequest request);

    @POST("rest/v1/teams")
    Call<List<Team>> createTeam(@Body Team team, @Header("Prefer") String prefer);

    @DELETE("rest/v1/teams")
    Call<Void> deleteTeam(@Query("id") String idFilter);

    @POST("rest/v1/team_members")
    Call<Void> addTeamMember(@Body TeamMember member);

    @GET("rest/v1/team_members")
    Call<List<TeamMember>> getMyTeams(@Query("user_id") String userIdFilter, @Query("select") String select);

    @GET("rest/v1/team_members")
    Call<List<TeamMember>> getTeamMembers(@Query("team_id") String teamIdFilter, @Query("select") String select);

    @PATCH("rest/v1/team_members")
    Call<Void> updateTeamMember(@Query("id") String idFilter, @Body TeamMember member);

    @DELETE("rest/v1/team_members")
    Call<Void> removeTeamMember(@Query("id") String idFilter);


    @POST("rest/v1/team_invitations")
    Call<Void> createInvitation(@Body TeamInvitation invitation);

    @GET("rest/v1/team_invitations")
    Call<List<TeamInvitation>> getTeamInvitations(@Query("team_id") String teamIdFilter, @Query("select") String select);

    @DELETE("rest/v1/team_invitations")
    Call<Void> deleteInvitation(@Query("id") String idFilter);

    @DELETE("rest/v1/team_invitations")
    Call<Void> deleteInvitations(@Query("team_id") String teamIdFilter, @Query("status") String statusFilter);


    @GET("rest/v1/team_invitations")
    Call<List<TeamInvitation>> getMyInvitations(@Query("invited_email") String emailFilter, @Query("status") String statusFilter, @Query("select") String select);

    @PATCH("rest/v1/team_invitations")
    Call<Void> respondToInvitation(@Query("id") String idFilter, @Body TeamInvitation update);
}
