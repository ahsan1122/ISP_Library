package utilities.data.apis

import com.esp.library.exceedersesp.controllers.Profile.BasicDAO
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*
import utilities.data.applicants.*
import utilities.data.applicants.addapplication.*
import utilities.data.applicants.dynamics.DynamicResponseDAO
import utilities.data.applicants.dynamics.DynamicStagesCriteriaListDAO
import utilities.data.applicants.feedback.ApplicationsFeedbackDAO
import utilities.data.applicants.profile.ApplicationProfileDAO
import utilities.data.applicants.profile.RealTimeValuesDAO
import utilities.data.filters.FilterDAO
import utilities.data.filters.FilterDefinitionSortDAO
import utilities.data.lookup.LookupInfoListDAO
import utilities.data.lookup.LookupInfoListDetailDAO
import utilities.data.lookup.LookupInfoSearchDAO
import utilities.data.lookup.LookupItemDetailDAO
import utilities.data.setup.IdenediAuthDAO
import utilities.data.setup.OrganizationPersonaDao
import utilities.data.setup.TokenDAO
import utilities.model.Labels
import java.util.*


/**
 * Created by Ali Uppal on 4/4/2016.
 */
interface APIs {


    @get:GET("applicant/profileStatus/0")
    val userProfileStatus: Call<String>

    @get:GET("settings/")
    val getSettings: Call<SettingsDAO>

    @get:GET("lookup/info/list")
    val lookupInfoList: Call<List<LookupInfoListDAO>>

    @get:GET("orguser/personas")
    val organizations: Call<List<OrganizationPersonaDao>>

    @FormUrlEncoded
    @POST("token")
    fun getToken(@Field("grant_type") grant_type: String?, @Field("username") username: String?,
                 @Field("password") password: String?, @Field("client_id") client_id: String?): Call<TokenDAO>


    @FormUrlEncoded
    @POST("token")
    fun getIdenedirefreshToken(@Field("grant_type") grant_type: String?, @Field("username") username: String?,
                               @Field("password") password: String?, @Field("client_id") client_id: String?,
                               @Field("idenedi_code") idenedi_code: String): Call<TokenDAO>

    @POST("idenedi/linkUser")
    fun linkIdenediUser(@Body idenediAuthDAO: IdenediAuthDAO?): Call<IdenediAuthDAO>


    @GET("/webapi/idenedi/linkProfile")
    fun linkIdenediProfile(@Query("idenediCode") idenediCode: String): Call<Any>

    /*@POST("token")
    fun getIdenediToken(): Call<Any>*/

    @FormUrlEncoded
    @POST("token")
    fun getRefreshToken(@Query("id") id: String?, @Field("grant_type") grant_type: String?,
                        @Field("username") username: String?, @Field("password") password: String?,
                        @Field("client_id") client_id: String?, @Field("scope") scope: String?,
                        @Field("refresh_token") refresh_token: String?): Call<TokenDAO>


    @POST("fbtoken")
    fun postFirebaseToken(@Body firebaseTokenDAO: FirebaseTokenDAO): Call<FirebaseTokenDAO>

    @DELETE("fbtoken/{fbTokenId}")
    fun deleteFirebaseToken(@Path("fbTokenId") fbTokenId: String): Call<FirebaseTokenDAO>

    /* @GET("application/all")
    Call<ResponseApplicationsDAO> GetUserApplications(@Query("status") String status, @Query("search") String search, @Query("PageNo") int PageNo, @Query("RecordPerPage") int RecordPerPage);

    @GET("application/list")
    Call<ResponseApplicationsDAO> GetUserSubApplicationsList(@Query("search") String search,@Query("filter") int filter, @Query("PageNo") int PageNo, @Query("RecordPerPage") int RecordPerPage ,
                                                             @Query("isMySpace") boolean isMySpace,@Query("sortBy") int sortBy,@Query("applicantId") String applicantId,
                                                             @Query("definationId") int definationId);*/


   /* @POST("application/listV2")
    fun GetUserApplicationsV2(@Body filterDAO: FilterDAO): Call<ResponseApplicationsDAO>*/


    @POST("application/listV3")
    fun getUserApplicationsV3(@Body filterDAO: FilterDAO): Call<ResponseApplicationsDAO>

    @POST("application/definitionForFilter")
    fun getDefinitioList(@Body filterDAO: FilterDAO): Call<List<FilterDefinitionSortDAO>>

    /*   @POST("application/reassign/assessment")
       fun reAssignData(@Query("applicationId") applicationId: Int?, @Query("newOwnerId") newOwnerId: Int,
                        @Body dynamicStagesCriteriaListDAO: DynamicStagesCriteriaListDAO?): Call<DynamicStagesCriteriaListDAO>*/

    @POST("application/reassign/assessment")
    fun reAssignData(@Body dynamicStagesCriteriaListDAO: DynamicStagesCriteriaListDAO?): Call<Any>

    @POST("application/getCalculatedValues")
    fun getCalculatedValues(@Body dynamicResponseDAO: DynamicResponseDAO): Call<List<CalculatedMappedFieldsDAO>>

    @POST("applicant/getRealTimeValues/0/{id}/0")
    fun getRealTimeValues(@Path("id") id: Int ,@Body realTimeValuesDAO: List<RealTimeValuesDAO>): Call<List<CalculatedMappedFieldsDAO>>

    @GET("label/getlabel")
    fun getLabels(): Call<Labels>

    @GET("applicant/")
    fun Getapplicant(): Call<ApplicationProfileDAO>

    /* @GET("application/details/{id}")
    Call<DynamicResponseDAO> GetApplicationDetail(@Query("id") String id);*/

    @DELETE("application/{id}")
    fun deleteApplication(@Path("id") id: Int): Call<Any>

    @GET("application/detailsv2/{id}")
    fun GetApplicationDetailv2(@Path("id") id: String): Call<DynamicResponseDAO>

    @GET("application/feedback/{id}")
    fun GetApplicationFeedBack(@Path("id") applicationId: String): Call<List<ApplicationsFeedbackDAO>>

    @GET("application/linkedApplicationInfo/{id}")
    fun GetLinkApplicationInfo(@Path("id") applicationId: String): Call<LinkApplicationsDAO>

    @GET("category/AllWithQuery")
    fun AllCategories(): Call<List<CategoryAndDefinationsDAO>>

    @GET("category/AllWithQuery")
    fun AllWithQuery(): Call<List<DefinationsCategoriesDAO>>

    @GET("submittalRequest")
    fun getSubDefinitionList(): Call<List<CategoryAndDefinationsDAO>>

    @GET("organization/users")
    fun getUser(): Call<List<UsersListDAO>>

    @GET("definition")
    fun AllDefincations(@Query("categoryId") categoryId: Int?): Call<List<CategoryAndDefinationsDAO>>

    @GET("definition/{id}")
    fun AllDefincationForm(@Path("id") id: Int?): Call<DynamicResponseDAO>

    @GET("definition/{id}/{parent_id}")
    fun getSubDefincationForm(@Path("id") id: Int?,@Path("parent_id") parent_id: Int?): Call<DynamicResponseDAO>

    @GET("lookupitem/list/{lookupid}/0")
    fun Lookups(@Path("lookupid") lookupid: Int?): Call<List<LookUpDAO>>

    @GET("lookupitem/Item/{itemid}")
    fun getLookupItemDetail(@Path("itemid") itemid: Int?): Call<LookupItemDetailDAO>

    @POST("lookupitem/Items")
    fun postLookUpItems(@Body LookupInfoListItem: LookupInfoSearchDAO): Call<LookupInfoListDetailDAO>

    //@POST("application/submit")
    @POST("application/submitv2")
    fun SubmitApplication(@Body dynamicResponseDAO: DynamicResponseDAO): Call<Int>

    //@POST("application/create")
    @POST("application/createv2")
    fun DraftApplication(@Body filterDAO: DynamicResponseDAO): Call<Int>

    @POST("application/respond")
    fun AcceptRejectApplication(@Body post: PostApplicationsStatusDAO): Call<Int>


    /*@PUT("application/comments")
    fun AddEditComments_(@Body post: PostApplicationsCriteriaCommentsDAO): Call<Int>*/

    @PUT("applicant/")
    fun saveApplicantData(@Body post: ApplicationProfileDAO.Applicant): Call<Int>

    @PUT("application/allowLinkedApplicationSubmission/{applicationId}/{isSubmissionAllowed}")
    fun saveLinkApplicationInfo(@Path("applicationId") applicationId: Int?,
                                @Path("isSubmissionAllowed") isSubmissionAllowed: Boolean?): Call<LinkApplicationsDAO>

    @PUT("applicant/basic")
    fun saveBasicData(@Body post: BasicDAO): Call<BasicDAO>

    @PUT("applicant/section/{sectionid}/{index}")
    fun updateApplicantDataBySectionId(@Path("sectionid") sectionid: Int?, @Path("index") index: Int?, @Body post: ArrayList<ApplicationProfileDAO.Values>): Call<ApplicationProfileDAO.Values>

    @POST("applicant/section/{sectionid}")
    fun saveApplicantDataBySectionId(@Path("sectionid") sectionid: Int?, @Body post: ArrayList<ApplicationProfileDAO.Values>): Call<Int>

    @Multipart
    @PUT("application/comments")
    fun addComments(
            @Part("assessmentId") assessmentId: Int,
            @Part("comments") comments: RequestBody
    ): Call<Int>


    @Multipart
    @PUT("application/comments")
    fun EditComments(
            @Part("id") id: Int,
            @Part("assessmentId") assessmentId: Int,
            @Part("comments") comments: RequestBody
    ): Call<Int>


    @GET("currency")
    fun getCurrency(): Call<List<CurrencyDAO>>


    @Multipart
    @PUT("application/comments")
    fun feedbackComments(
            @Part file: MultipartBody.Part?,
           @Part("applicationId") applicationId: Int,
           @Part("comments") comments: RequestBody,
            @Part("isVisibletoApplicant") isVisibletoApplicant: Boolean


    ): Call<Int>

    @Multipart
    @POST("upload")
    fun upload(@Part file: MultipartBody.Part): Call<ResponseFileUploadDAO>

    @Multipart
    @POST("applicant/picture")
    fun picture(@Part file: MultipartBody.Part?): Call<String>

}
