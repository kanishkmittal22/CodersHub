package com.xoxoharsh.codershub;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.xoxoharsh.codershub.util.FirebaseUtil;
import java.util.HashMap;
import java.util.Map;
public class SignUpActivity3 extends AppCompatActivity {
    EditText emailIdEditText,passwordEditText,confirmPasswordEditText;
    Button createAccountBtn;
    TextView loginTextView;
    ProgressBar progressBar;
    private String cfHandle,lHandle,gfgHandle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up3);
        Log.d("CodersHub_Errors","Entered SignupActivity3");
        emailIdEditText = findViewById(R.id.emailId);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        createAccountBtn = findViewById(R.id.createAccount);
        loginTextView = findViewById(R.id.login);
        progressBar = findViewById(R.id.Progress_Bar);
        Intent intent = getIntent();
        cfHandle = intent.getStringExtra("codeforces");
        lHandle = intent.getStringExtra("leetcode");
        gfgHandle = intent.getStringExtra("gfg");
        createAccountBtn.setOnClickListener((v)-> createAccount());
        loginTextView.setOnClickListener((v)-> startActivity(new Intent(SignUpActivity3.this,LoginActivity.class)));
    }
    void createAccount(){
        String email  = emailIdEditText.getText().toString().trim();
        String password  = passwordEditText.getText().toString().trim();
        String confirmPassword  = confirmPasswordEditText.getText().toString().trim();
        boolean isValidated = validateData(email,password,confirmPassword);
        if(!isValidated){
            return;
        }
        createAccountInFirebase(email,password);
    }
    void createAccountInFirebase(String email,String password) {
        changeInProgress(true);
        Log.d("CodersHub_Errors","Creating account in firebase");
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignUpActivity3.this, task -> {
                    if (task.isSuccessful()) {
                        DocumentReference userDocRef = FirebaseUtil.currentUserDetails();
                        Map<String, Object> handlesData = new HashMap<>();
                        if (!cfHandle.isEmpty()) {
                            Map<String,Object>cf = new HashMap<>();
                            cf.put("Handle",cfHandle);
                            handlesData.put("codeforces", cf);
                        }
                        if (!lHandle.isEmpty()) {
                            Map<String,Object>le = new HashMap<>();
                            le.put("Handle",lHandle);
                            handlesData.put("leetcode", le);
                        }
                        if (!gfgHandle.isEmpty()) {
                            Map<String,Object>gfg = new HashMap<>();
                            gfg.put("Handle",gfgHandle);
                            handlesData.put("gfg", gfg);
                        }
                        Log.d("CodersHub_Errors",handlesData.toString());
                        userDocRef.set(handlesData)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Log.d("CodersHub_Errors","Handles Stored succesfully");
                                        Toast.makeText(SignUpActivity3.this, "Succesfully created account", Toast.LENGTH_LONG).show();
                                        SendRequestWithoutResponseTask task2 = new SendRequestWithoutResponseTask();
                                        task2.setCallback(new SendRequestWithoutResponseTask.Callback() {
                                            @Override
                                            public void onTaskCompleted(int responseCode) {
                                                Log.d("CodersHub_Errors", "Task completed with response code: " + responseCode);
                                            }
                                        });
                                        Toast.makeText(SignUpActivity3.this, "Scrapping your Data please wait for few seconds", Toast.LENGTH_LONG).show();
                                        task2.execute(FirebaseUtil.currentUserEmail());
                                        Toast.makeText(SignUpActivity3.this, "Please verify your email to continue", Toast.LENGTH_SHORT).show();
                                        Log.d("CodersHub_Errors","request send successfully");

                                    } else {
                                        String errorMsg = "Handles not stored";
                                        Toast.makeText(SignUpActivity3.this, errorMsg, Toast.LENGTH_SHORT).show();
                                    }
                                    changeInProgress(false);
                                    firebaseAuth.getCurrentUser().sendEmailVerification();
                                    firebaseAuth.signOut();
                                    Intent intent = new Intent(SignUpActivity3.this, LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                });
                    } else {
                        String errorMessage = task.getException().getLocalizedMessage();
                        Toast.makeText(SignUpActivity3.this, errorMessage, Toast.LENGTH_SHORT).show();
                        changeInProgress(false);
                    }
                });
    }
    void changeInProgress(boolean inProgress){
        if(inProgress){
            progressBar.setVisibility(View.VISIBLE);
            createAccountBtn.setVisibility(View.GONE);
        }else{
            progressBar.setVisibility(View.GONE);
            createAccountBtn.setVisibility(View.VISIBLE);
        }
    }
    boolean validateData(String email,String password1,String confirmPassword1){
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailIdEditText.setError("Email is invalid");
            return false;
        }
        if(password1.length()<6){
            passwordEditText.setError("Password length is invalid");
            return false;
        }
        if(!password1.equals(confirmPassword1)){
            confirmPasswordEditText.setError("Password not matched");
            return false;
        }
        return true;
    }
}