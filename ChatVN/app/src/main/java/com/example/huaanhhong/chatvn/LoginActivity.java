package com.example.huaanhhong.chatvn;

import android.Manifest;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.huaanhhong.chatvn.Function.Model.User;
import com.example.huaanhhong.chatvn.Function.data.SharedPreferenceHelper;
import com.example.huaanhhong.chatvn.Function.data.StaticConfig;
import com.example.huaanhhong.chatvn.Linphone.EchoCancellerCalibrationFragment;
import com.example.huaanhhong.chatvn.Linphone.LinphoneManager;
import com.example.huaanhhong.chatvn.Linphone.LinphonePreferences;
import com.example.huaanhhong.chatvn.Linphone.LinphoneService;
import com.example.huaanhhong.chatvn.Retrofit.APIRetrofit;
import com.example.huaanhhong.chatvn.Retrofit.ServerInterface;
import com.example.huaanhhong.chatvn.Retrofit.TokenRequest;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonElement;

import org.json.JSONException;
import org.json.JSONObject;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListenerBase;
import org.linphone.core.LinphoneProxyConfig;

import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity implements OnClickListener {


    @BindView(R.id.edt_email)
    EditText mEdtEmail;
    @BindView(R.id.edt_sigup_password)
    EditText mEdtPassword;
    @BindView(R.id.edt_sigup_confirm)
    EditText mEdtConfirmPassword;
    @BindView(R.id.btn_sigup)
    Button mBtnSignup;
    @BindView(R.id.btn_signin)
    Button mBtnSignin;

    SignInButton mBtnLoginGG;
    LoginButton mBtnLoginFacebook;
    private static LoginActivity instance;

    /**
     * String name pass, boolean......
     */
    String mEmail, mPass, mConfirmPass,mMobile;
    private boolean accountLinphoneCreated = false,newAccount = false;
    private Handler mHandle;
    private boolean firstTimeAccess;
    /**Linphone
     * */
    private LinphoneCoreListenerBase mListener;
    private LinphoneAddress address;
    private LinphonePreferences mPrefs;
    private ProgressDialog progress;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 201;

    /**
     * firebase
     */
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser user;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;



    private static String TAG = "LoginActivity";
//    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;

    CallbackManager mCallbackManager;
    private static final int RC_SIGN_IN = 9001;

    private final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);


    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.setApplicationId("329131324213987");
        FacebookSdk.sdkInitialize(this);

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        /** khoi dong firebase
         * */
        mFirebaseAuth=FirebaseAuth.getInstance();
        mFirebaseDatabase=FirebaseDatabase.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    StaticConfig.UID = user.getUid();
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    if (firstTimeAccess) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        LoginActivity.this.finish();
                    }
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                firstTimeAccess = false;
            }
        };

        mCallbackManager=CallbackManager.Factory.create();

        /** click ui
         * */
        mBtnSignup.setOnClickListener(this);
        mBtnSignin.setOnClickListener(this);
        /**Facebook*/
        mBtnLoginFacebook=(LoginButton) findViewById(R.id.btn_loginfacebook);
        mBtnLoginFacebook.setReadPermissions("email", "public_profile");
        mBtnLoginFacebook.registerCallback(mCallbackManager,facebookcallback);
        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, null/* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mBtnLoginGG=(SignInButton) findViewById(R.id.btn_logingoogle);
        mBtnLoginGG.setOnClickListener(this);

        /**
         * Linphone
         * */
        mPrefs = LinphonePreferences.instance();
        if (!LinphoneService.isReady())  {
            org.linphone.mediastream.Log.i("CNN","ASSISTANCE XET SERVIC");
            startService(new Intent(Intent.ACTION_MAIN).setClass(this, LinphoneService.class));
        }
        mListener = new LinphoneCoreListenerBase(){

            @Override
            public void registrationState(LinphoneCore lc, LinphoneProxyConfig cfg, LinphoneCore.RegistrationState state, String smessage) {
                android.util.Log.i("CNN", "AssistantActivity_registersrationState");
                if(accountLinphoneCreated && !newAccount){
                    if(address != null && address.asString().equals(cfg.getAddress().asString()) ) {
                        if (state == LinphoneCore.RegistrationState.RegistrationOk) {
                            if(progress != null)
                                progress.dismiss();
                             Toast.makeText(LoginActivity.this,"Dang ki thanh cong",Toast.LENGTH_LONG).show();
                            if (LinphoneManager.getLc().getDefaultProxyConfig() != null) {
                                launchEchoCancellerCalibration(true);
                            }
                        } else if (state == LinphoneCore.RegistrationState.RegistrationFailed) {
                            if(progress != null)
                                progress.dismiss();
                        } else if(!(state == LinphoneCore.RegistrationState.RegistrationProgress)) {
                            if(progress != null)
                                progress.dismiss();
                        }
                    }
                }
            }
        };
        instance = this;
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.addListener(mListener);
        }
      if(mPrefs.isFirstComed()){
          Intent intent=new Intent(LoginActivity.this,MainActivity.class);
          startActivity(intent);
          finish();
      }

    }



//        //Khoi tao dialog waiting khi dang nhap
//        waitingDialog = new LovelyProgressDialog(this).setCancelable(false);


    public static LoginActivity instance() {
        android.util.Log.i("CNN", "AssistantActivity_instance");
        return instance;
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.btn_sigup:
                    mEmail = mEdtEmail.getText().toString();
                    mPass = mEdtPassword.getText().toString();
                    mConfirmPass = mEdtConfirmPassword.getText().toString();

                    if(validate(mEmail, mPass, mConfirmPass)){
                        new signupByEmail().execute(mEmail,mPass);
                    }else {
                        Toast.makeText(this, "Invalid email or not match password", Toast.LENGTH_LONG).show();
                    }


//                    if (mEmail == null || mPass == null||mConfirmPass==null) {
//                        Toast.makeText(LoginActivity.this, this.getString(R.string.thieuthongtin), Toast.LENGTH_LONG).show();
//                    }
//                    else if(!mPass.equals(mConfirmPass)) {
//                        Toast.makeText(LoginActivity.this, this.getString(R.string.saimatkhau), Toast.LENGTH_LONG).show();
//                    }
//                    else
//                    {
//                        new signupByEmail().execute(mEmail,mPass);
//                    }
                    break;
                case R.id.btn_signin:
                    mEmail = mEdtEmail.getText().toString();
                    mPass = mEdtPassword.getText().toString();

                    if (validate_singnin(mEmail, mPass)){

                        new loginByEmail().execute(mEmail,mPass);

                    }else {
                        Toast.makeText(this, "Invalid email or empty password", Toast.LENGTH_LONG).show();
                    }


//                    mEmail="invanphat2014@gmail.com";
//                    mPass="12345678";





//                    if (mEmail == null || mPass == null) {
//                        Toast.makeText(LoginActivity.this, this.getString(R.string.thieuthongtin), Toast.LENGTH_LONG).show();
//                    }
//                    else {
//                        new loginByEmail().execute(mEmail,mPass);
//                    }

                    break;
                case R.id.btn_logingoogle:
                    signIn();
                    break;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validate_singnin(String mEmail, String mPass) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(mEmail);
        return (mPass.length() > 0 || mPass.equals(";")) && matcher.find();
    }

    private boolean validate(String mEmail, String mPass, String mConfirmPass) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(mEmail);
        return mPass.length() > 0 && mConfirmPass.equals(mPass) && matcher.find();
    }
    /** Sign up bang email,GG,facebook --qua trinh nay dien ra la mot asysntask, bao gom:
     * firebase
     * retrofit webservice
     * linphone
     * thong tin bao gom : email,password, mobilevausername la mot so random
     * /////////////////////////////////////////////////////////////////// */

    /**Firebase*/
    private ProgressDialog progressDialog;
    class signupByEmail extends AsyncTask<String,Void,Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog=new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage(getString(R.string.dialogchodangki));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            mFirebaseAuth.createUserWithEmailAndPassword(strings[0],strings[1])
                    .addOnCompleteListener(listenCompleteSignup);
            return null;
        }
    }
    // Lang nghe dang ki firebase co thanh cong hay khong
    private OnCompleteListener listenCompleteSignup=new OnCompleteListener() {
        @Override
        public void onComplete(@NonNull Task task) {

            try {

                if(task.isComplete()&&task.isSuccessful()){
                    //saveData();
                    //tiep tuc sign up webservice

                    signupWebservice();
                }
                else {
                    progress.dismiss();
                    Toast.makeText(LoginActivity.this, getString(R.string.err),Toast.LENGTH_LONG).show();
                    Log.i("CNN","DA CO LOI", task.getException());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("SIGNUP",e.toString());

            }
        }
    };
    ///Login firebase/////
    class loginByEmail extends AsyncTask<String,Void,Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog=new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage(getString(R.string.thongbaodangnhap));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        @Override
        protected Boolean doInBackground(String... voids) {

            mFirebaseAuth.signInWithEmailAndPassword(voids[0],voids[1])
                    .addOnCompleteListener(completesigin);

            return null;
        }
    }
    ////Lang nghe dang nhap firebase co thanh cong hay khong////////
    private OnCompleteListener completesigin=new OnCompleteListener() {
        @Override
        public void onComplete(@NonNull Task task) {

            try {

                if(task.isComplete()&&task.isSuccessful()){
                    Log.i("CNN","Dang nhap firebase thanh cong");
                    
                     //saveData();
                    //Lognin firebase
                    LogninWebservice();
                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "da co loi xay ra tren firebase",Toast.LENGTH_LONG).show();
                    Log.i("CNN","DA CO LOI", task.getException());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("LOGNIN",e.toString());
            }
        }
    };
    ///LoginFacebook
    private FacebookCallback<LoginResult> facebookcallback=new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {

            AuthCredential credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
            mFirebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener(LoginActivity.this,onlistencompleteFacebook);
        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onError(FacebookException error) {

            Toast.makeText(LoginActivity.this,error.toString(),Toast.LENGTH_LONG).show();
        }
    };

    private OnCompleteListener onlistencompleteFacebook=

            new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {

            if(task.isComplete()&&task.isSuccessful()){
                //saveData();
                user = mFirebaseAuth.getCurrentUser();
                User newuser=new User();
                mPass=user.getUid().substring(1,7);
                mEmail=newuser.email;
                signupWebservice();
            }
            else{
                Toast.makeText(LoginActivity.this,task.getException().toString(),Toast.LENGTH_LONG).show();
            }

        }
    };
    /////Google////////////
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        progressDialog=new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("Dang dang ki");
        progressDialog.setCancelable(false);
        progressDialog.show();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            signupWebservice();
                        } else {

                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // [START_EXCLUDE]
                        progressDialog.dismiss();
                        // [END_EXCLUDE]
                    }
                });
    }

    ////save data len firebase sao khi dang ki,hoac dang nhap thanh cong
    private void saveData() {


        Log.i("BLH","luu thong tin len firebase");
        mFirebaseDatabase.getInstance().getReference().child("ListUser/" + StaticConfig.UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                HashMap hashUser = (HashMap) dataSnapshot.getValue();
                User userInfo = new User();
                userInfo.name = (String) hashUser.get("name");
                userInfo.email = (String) hashUser.get("email");
                userInfo.avata = (String) hashUser.get("avata");
                SharedPreferenceHelper.getInstance(LoginActivity.this).saveUserInfo(userInfo);
                Log.i("BLH","luu thong tin len preferent");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

     //-END.
    //ket thuc CV tren firebase////////////////

   /** signup tren Webservice */////////////////////
    private void signupWebservice() {
        TokenRequest tokensignup=new TokenRequest();
        tokensignup.setPassword(mPass);
        UUID myuuid = UUID.randomUUID();
        long highbits = myuuid.getMostSignificantBits();
            //        long lowbits = myuuid.getLeastSignificantBits();
        mMobile= "+84"+(String.valueOf(highbits)).substring(1,8);
        tokensignup.setProfile(new TokenRequest.Profile(mMobile,mEmail));
        tokensignup.setUsername(mMobile);

        ServerInterface serverSignup=new APIRetrofit().postjson().create(ServerInterface.class);
        Call<JsonElement> call= serverSignup.postsignup(tokensignup);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                Log.i("CNN",response.body().toString());
                try {
                    JSONObject data = new JSONObject(response.body().toString());
                    String err = data.getString("err_code");

                    if (err.equals("0")) {
                        //show dialog thong bao check mail de tiep tuc login
                        mHandle=new Handler();
                        showdialogcheckmail();

                        initNewUserInfo();

                        LogninWebservice();
                    }
                    else {
                        LogninWebservice();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {

            }
        });
    }
    /** nhac nho check mail
     * */
    private void showdialogcheckmail(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mHandle.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(progressDialog!=null){
                            progressDialog.dismiss();
                        }
                        dialogCheckmail();
                    }
                }, 50000);
            }
        });
    }
    private Runnable onPlay=new Runnable() {
        @Override
        public void run() {

            }
    };
    /** dialog nhac nho check mail */
    private void dialogCheckmail() {
        progressDialog=new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage(getString(R.string.messageNhaccheckmail));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
  /** LOgin Webservice de lay token *///////////////////
  int mCount=0;
    private void LogninWebservice() {
        Log.i("CNN","login lan "+mCount);
        TokenRequest token = new TokenRequest();
        token.setUsername(mEmail);
        token.setPassword(mPass);
        ServerInterface serverinterface = new APIRetrofit().postjson().create(ServerInterface.class);
        Call<JsonElement> call = serverinterface.postsignin(token);
        call.enqueue(new Callback<JsonElement>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                Log.i("CNN", response.body().toString());
                Log.i("CNN",(response.body().toString()).substring(56,60));
                try {
                    JSONObject data = new JSONObject(response.body().toString());
                    String err = data.getString("err_code");
                    JSONObject object = data.getJSONObject("data");
//                    Log.i("CNN","conftokennnnnnnnnn"+conf_token+" "+mobile);
                    if(((response.body().toString()).substring(56,60)).equals("null")) {
                        Log.i("CNN","CONF TOKEN NULL");
                        //tiep tuc cho checkmail
                        mHandle=new Handler();
                        mHandle.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(mCount==11){
//                                    dimiss();
                                    LogninWebservice();
                                }
                                else if(mCount<11){
                                    LogninWebservice();
                                }
                                mCount++;
                            }
                        },40000);
                    }
                    JSONObject objectprofile = object.getJSONObject("profile");
                    String conf_token = object.getString("conf_token");
                    String mobile=objectprofile.getString("mobile");
                    if(!err.equals("0")){
                       dimiss();
                    }
                    else if(conf_token!=null){
                        if (progressDialog!=null){
                        progressDialog.dismiss();}


//                        initNewUserInfo();
                        saveData();
                        /** tiep tuc signupLinphone */
                        LinphoneAddress.TransportType transport;
                        transport = LinphoneAddress.TransportType.LinphoneTransportUdp;
                        String tokenf=conf_token.substring(0,25);
                        genericLogIn(mobile,tokenf,null,"104.198.213.170",transport);
                        Log.i("CNN",tokenf);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {

            }
        });
    }

    private void initNewUserInfo() {
        User newUser = new User();
        newUser.email = user.getEmail();
        newUser.mobil=mMobile;
        newUser.name = user.getEmail().substring(0, user.getEmail().indexOf("@"));
        newUser.avata = StaticConfig.STR_DEFAULT_BASE64;
        FirebaseDatabase.getInstance().getReference().child("ListUser/" + user.getUid()).setValue(newUser);
    }

    //dimiss
    private void dimiss(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(progressDialog!=null) {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this,getString(R.string.err),Toast.LENGTH_SHORT).show();
                }
    }});}
     //-END.
    /////////ket thuc signup tren webservice//////////
    /**
     * Tien hanh signup vao linphone
     * */
    public void genericLogIn(String username, String password, String displayName, String domain, LinphoneAddress.TransportType transport) {
        android.util.Log.i("CNN", "AssistantActivity_genericLogIn");
        if(accountLinphoneCreated) {
            retryLogin(username, password, displayName, domain, transport);
        } else {
            logIn(username, password, displayName, domain, transport, false);
        }
    }
    private void logIn(String username, String password, String displayName, String domain, LinphoneAddress.TransportType transport, boolean sendEcCalibrationResult) {
        android.util.Log.i("CNN", "AssistantActivity_logIn");
        saveCreatedAccount(username, password, displayName, domain, transport);
    }
    public void retryLogin(String username, String password, String displayName, String domain, LinphoneAddress.TransportType transport) {
        android.util.Log.i("CNN", "AssistantActivity_retrylogIn");
        accountLinphoneCreated = false;
        saveCreatedAccount(username, password, displayName, domain, transport);
    }
    public void saveCreatedAccount(String username, String password, String displayName, String domain, LinphoneAddress.TransportType transport) {
        android.util.Log.i("CNN", "AssistantActivity_saveCreatedAccount");
        if (accountLinphoneCreated)
            return;

        if(username.startsWith("sip:")) {
            username = username.substring(4);
        }

        if (username.contains("@"))
            username = username.split("@")[0];

        if(domain.startsWith("sip:")) {
            domain = domain.substring(4);
        }

        String identity = "sip:" + username + "@" + domain;
        try {
            address = LinphoneCoreFactory.instance().createLinphoneAddress(identity);
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
        }

        if(address != null && displayName != null && !displayName.equals("")){
            address.setDisplayName(displayName);
        }

        boolean isMainAccountLinphoneDotOrg = domain.equals(getString(R.string.default_domain));
        LinphonePreferences.AccountBuilder builder = new LinphonePreferences.AccountBuilder(LinphoneManager.getLc())
                .setUsername(username)
                .setDomain(domain)
                .setDisplayName(displayName)
                .setPassword(password);

        if (isMainAccountLinphoneDotOrg) {
            if (getResources().getBoolean(R.bool.disable_all_security_features_for_markets)) {
                builder.setProxy(domain)
                        .setTransport(LinphoneAddress.TransportType.LinphoneTransportTcp);
            }
            else {
                builder.setProxy(domain)
                        .setTransport(LinphoneAddress.TransportType.LinphoneTransportTls);
            }

            builder.setExpires("604800")
                    .setAvpfEnabled(true)
                    .setAvpfRRInterval(3)
                    .setQualityReportingCollector("sip:voip-metrics@sip.linphone.org")
                    .setQualityReportingEnabled(true)
                    .setQualityReportingInterval(180)
                    .setRealm("sip.linphone.org")
                    .setNoDefault(false);


            mPrefs.setStunServer(getString(R.string.default_stun));
            mPrefs.setIceEnabled(true);
        } else {
            String forcedProxy = "";
            if (!TextUtils.isEmpty(forcedProxy)) {
                builder.setProxy(forcedProxy)
                        .setOutboundProxyEnabled(true)
                        .setAvpfRRInterval(5);
            }

            if(transport != null) {
                builder.setTransport(transport);
            }
        }

//        if (getResources().getBoolean(R.bool.enable_push_id)) {
//            String regId = mPrefs.getPushNotificationRegistrationID();
//            String appId = getString(R.string.push_sender_id);
//            if (regId != null && mPrefs.isPushNotificationEnabled()) {
//                String contactInfos = "app-id=" + appId + ";pn-type=google;pn-tok=" + regId;
//                builder.setContactParameters(contactInfos);
//            }
//        }

        try {
            builder.saveNewAccount();
            if(!newAccount) {
                displayRegistrationInProgressDialog();
            }
            accountLinphoneCreated = true;
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
        }
    }
    //show dialog register linphone
    public void displayRegistrationInProgressDialog(){
        android.util.Log.i("CNN", "AssistantActivity_displayRegistrationInProgressDialog");
        if(LinphoneManager.getLc().isNetworkReachable()) {
            progress = ProgressDialog.show(this,null,null);
            Drawable d = new ColorDrawable(getResources().getColor(R.color.dialog));
            d.setAlpha(200);
            progress.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            progress.getWindow().setBackgroundDrawable(d);
            progress.setContentView(R.layout.progress_dialog);
            progress.show();

        }
        else{
            Log.i("CNN","khong co mang");
        }
    }
    public void checkAndRequestAudioPermission() {
        android.util.Log.i("CNN", "AssistantActivity_checkAndRequestAudioPermission");
        if (getPackageManager().checkPermission(android.Manifest.permission.RECORD_AUDIO, getPackageName()) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.RECORD_AUDIO)) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        android.util.Log.i("CNN", "AssistantActivity_onRequestPermissionResult");
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (getPackageManager().checkPermission(Manifest.permission.RECORD_AUDIO, getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                launchEchoCancellerCalibration(true);
            } else {
                success();
            }
        } else {
            success();
        }
    }

    private void launchEchoCancellerCalibration(boolean sendEcCalibrationResult) {
        android.util.Log.i("CNN", "AssistantActivity_launchEchoCancellerCalibration");
        if (getPackageManager().checkPermission(android.Manifest.permission.RECORD_AUDIO, getPackageName()) == PackageManager.PERMISSION_GRANTED) {
            boolean needsEchoCalibration = LinphoneManager.getLc().needsEchoCalibration();
            if (needsEchoCalibration && mPrefs.isFirstLaunch()) {
                EchoCancellerCalibrationFragment fragment = new EchoCancellerCalibrationFragment();
                fragment.enableEcCalibrationResultSending(sendEcCalibrationResult);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.framelayout, fragment);
                transaction.commitAllowingStateLoss();
            } else {

                success();
            }
        } else {
            checkAndRequestAudioPermission();
        }
    }
    public void isEchoCalibrationFinished() {
        android.util.Log.i("CNN", "AssistantActivity_isEchoCalibrationFinished");
        success();
    }
    public void success() {
        android.util.Log.i("CNN", "AssistantActivity_success");
        mPrefs.firstLaunchSuccessful();
        mPrefs.firstComedSuccessful();
        Intent intent=new Intent(LoginActivity.this,MainActivity.class);
        startActivity(intent);
        finish();

    }
    ////////////////////KET THUC LOGIN BANG LINPHONE//////////////////////////////////
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // [START_EXCLUDE]
//                updateUI(null);
                // [END_EXCLUDE]
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        android.util.Log.i("BLH", "AssistantActivity_onResume");
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.addListener(mListener);
            android.util.Log.i("BLH", "ON RESUME ASSISTANT ACTIVITY");
        }
    }

    @Override
    protected void onPause() {
        android.util.Log.i("CNN", "AssistantActivity_onPause");
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.removeListener(mListener);
        }

        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuthListener!=null){
            mFirebaseAuth.removeAuthStateListener(mAuthListener);
        }

    }

    /**Lang nghe AuthFirebase
     * */
//    private FirebaseAuth.AuthStateListener getmAuthListener=new FirebaseAuth.AuthStateListener() {
//        @Override
//        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//
//            user = firebaseAuth.getCurrentUser();
//            if (user != null) {
//                // User is signed in
//                StaticConfig.UID = user.getUid();
//                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
//                if (firstTimeAccess) {
//                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
//                    LoginActivity.this.finish();
//                }
//            } else {
//                Log.d(TAG, "onAuthStateChanged:signed_out");
//            }
//            firstTimeAccess = false;
//        }
//
//
//    };

//    private FirebaseAuth.AuthStateListener listenAuthState=new FirebaseAuth.AuthStateListener() {
//        @Override
//        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//
//            user = firebaseAuth.getCurrentUser();
//            if (user != null) {
//                // User is signed in
//                StaticConfig.UID = user.getUid();
//                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
//                if (firstTimeAccess) {
//                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
//                    LoginActivity.this.finish();
//                }
//            } else {
//                Log.d(TAG, "onAuthStateChanged:signed_out");
//            }
//            firstTimeAccess = false;
//        }
//
//
//    };

}

