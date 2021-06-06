package com.game.learnto;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

public class LoginActivity extends AppCompatActivity {
    private Button btSignIn;
    private   SignInButton sign_in_with_google_button;
   private TwitterLoginButton sign_in_with_twitter_button;
    private GoogleSignInClient googleSignInClient;
    private ImageView sign_in_with_google_img, sign_in_with_twitter_img, sign_in_with_facebook_img,sign_in_with_email_img;
    private LoginButton sign_in_with_facebook_button;
    private CallbackManager mCallbackManager;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private AccessTokenTracker accessTokenTracker;
    private OAuthProvider.Builder provider;
    private FirebaseAuth.AuthStateListener authListener;
    private final static String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;
    private TextView register;

    private static final String TWITTER_KEY = "KFO0t9a7waUen9McbSalcHKww";
    private static final String TWITTER_SECRET = "QP82dc3nO8Cbn9ZRLBLuhdL9k1dFyiStTthrDw0JaI6oUahqVP";
    ValidacioDades Validacio;
    private TextInputLayout lay_login_Email, lay_login_Password;
    private EditText et_login_Email, et_Login_Password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Twitter.initialize(this);
        setContentView(R.layout.activity_login);
        firebaseAuth = FirebaseAuth.getInstance();


        register = findViewById(R.id.register_page);
        btSignIn = findViewById(R.id.sign_in_button);

        sign_in_with_facebook_button = findViewById(R.id.sign_in_with_facebook_button);
        sign_in_with_twitter_button = findViewById(R.id.sign_in_with_twitter_button);
        sign_in_with_google_button = findViewById(R.id.sign_in_with_google_button);

        sign_in_with_twitter_img = findViewById(R.id.sign_in_with_twitter_img);
        sign_in_with_google_img = findViewById(R.id.sign_in_with_google_img);
        sign_in_with_facebook_img = findViewById(R.id.sign_in_with_facebook_img);
        sign_in_with_email_img = findViewById(R.id.sign_in_with_email_img);

        lay_login_Email = findViewById(R.id.lay_login_Email);
        lay_login_Password = findViewById(R.id.lay_login_Password);
        et_login_Email = findViewById(R.id.et_login_Email);
        et_Login_Password = findViewById(R.id.et_Login_Password);

        Validacio = new ValidacioDades(this);
        provider = OAuthProvider.newBuilder("twitter.com");
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();

        googleSignInClient = GoogleSignIn.getClient(LoginActivity.this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build());
        createRequest();

        sign_in_with_google_img.setOnClickListener(v -> {
            lay_login_Email.setErrorEnabled(false);
            lay_login_Password.setErrorEnabled(false);
            et_login_Email.setEnabled(false);
            et_Login_Password.setEnabled(false);
            et_login_Email.setText("");
            et_Login_Password.setText("");
            sign_in_with_facebook_button.setVisibility(View.GONE);
            sign_in_with_twitter_button.setVisibility(View.GONE);
            sign_in_with_google_button.setVisibility(View.VISIBLE);
            btSignIn.setVisibility(View.GONE);
        });
        sign_in_with_google_button.setOnClickListener(v -> {
            SignInBtn();
        });

        sign_in_with_twitter_img.setOnClickListener(v -> {
            lay_login_Email.setErrorEnabled(false);
            lay_login_Password.setErrorEnabled(false);
            et_login_Email.setText("");
            et_Login_Password.setText("");
            et_login_Email.setEnabled(false);
            et_Login_Password.setEnabled(false);
            sign_in_with_facebook_button.setVisibility(View.GONE);
            sign_in_with_twitter_button.setVisibility(View.VISIBLE);
            sign_in_with_google_button.setVisibility(View.GONE);
            btSignIn.setVisibility(View.GONE);
        });
        sign_in_with_twitter_button.setOnClickListener(v -> {
            firebaseAuthWithTwitterCheck();
        });
        sign_in_with_facebook_img.setOnClickListener(v -> {
            et_login_Email.setText("");
            et_Login_Password.setText("");
            lay_login_Email.setErrorEnabled(false);
            lay_login_Password.setErrorEnabled(false);
            et_login_Email.setEnabled(false);
            et_Login_Password.setEnabled(false);
            sign_in_with_facebook_button.setVisibility(View.VISIBLE);
            sign_in_with_twitter_button.setVisibility(View.GONE);
            sign_in_with_google_button.setVisibility(View.GONE);
            btSignIn.setVisibility(View.GONE);
        });
        sign_in_with_email_img.setOnClickListener(v -> {
            et_login_Email.setEnabled(true);
            et_Login_Password.setEnabled(true);
            sign_in_with_facebook_button.setVisibility(View.GONE);
            sign_in_with_twitter_button.setVisibility(View.GONE);
            sign_in_with_google_button.setVisibility(View.GONE);
            btSignIn.setVisibility(View.VISIBLE);
        });
        btSignIn.setOnClickListener(v -> {
            GuadarDades();
        });
        register.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        });
        sign_in_with_facebook_button.setReadPermissions("email", "public_profile");
        sign_in_with_facebook_button.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                displayToast("facebook:onSuccess:");
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                displayToast("facebook:onCancel");

            }

            @Override
            public void onError(FacebookException error) {
                displayToast("facebook:onCancel");
                Log.d(TAG, "facebook:onError", error);
            }
        });
        authListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            updateUI(user);

        };
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if(currentAccessToken == null){
                    FirebaseAuth.getInstance().signOut();
                }

            }
        };

    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void firebaseAuthWithTwitterCheck() {
        Task<AuthResult> pendingResultTask = firebaseAuth.getPendingAuthResult();
        if (pendingResultTask != null) {
            pendingResultTask
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            // User is signed in.
                            // IdP data available in
                            // authResult.getAdditionalUserInfo().getProfile().
                            // The OAuth access token can also be retrieved:
                            // authResult.getCredential().getAccessToken().
                            // The OAuth secret can be retrieved by calling:
                            // authResult.getCredential().getSecret().
                        }
                    })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle failure.
                                }
                            });
        } else {
            Log.w(TAG, "firebaseAuthWithTwitter:failure");
            displayToast("no hi ha resultat");
            firebaseAuthWithTwitter();
            // There's no pending result so you need to start the sign-in flow.
            // See below.
        }
    }

    private void firebaseAuthWithTwitter() {
        firebaseAuth.startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        updateUI(user);
                        // User is signed in.
                        // IdP data available in
                        //authResult.getAdditionalUserInfo().getProfile();
                        Log.d(TAG, "signInWithEmail:success" + user.getDisplayName());
                        // The OAuth access token can also be retrieved:
                        // authResult.getCredential().getAccessToken().
                        // The OAuth secret can be retrieved by calling:
                        // authResult.getCredential().getSecret().
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure.
                    }
                });

    }

    private void GuadarDades() {
        if (!Validacio.CampOmplert(et_login_Email, lay_login_Email, getString(R.string.error_message_email))) {
            return;
        }
        if (!Validacio.EsEmail(et_login_Email, lay_login_Email, getString(R.string.error_message_email))) {
            return;
        }
        if (!Validacio.CampOmplert(et_Login_Password, lay_login_Password, getString(R.string.error_message_password))) {
            return;
        }
        signInWithEmailAndPassword(et_login_Email.getText().toString(), et_Login_Password.getText().toString());

    }

    private void createRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void SignInBtn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }


    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        displayToast("signInWithCredential:success");
                        updateUI(user);
                    } else {
                        displayToast("ignInWithCredential:failure");
                        Log.w(TAG, "signInWithCredential:failure", task.getException().getCause());
                    }
                });
    }

    private void signInWithEmailAndPassword(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        Validacio.WrongPassword(et_Login_Password, lay_login_Password, "Wrong password");
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    private void displayToast(String s) {
        Toast.makeText(LoginActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authListener);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(authListener!=null)
            firebaseAuth.removeAuthStateListener(authListener);
    }
}