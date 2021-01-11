package com.example.paotonet.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.paotonet.Activities.ParentInterface;
import com.example.paotonet.Objects.Parent;
import com.example.paotonet.Objects.Teacher;
import com.example.paotonet.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ParentLogin extends AppCompatActivity implements View.OnClickListener {
    EditText email, password;
    Button login, back;
    TextView invalid;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_login);

        login = (Button) findViewById(R.id.login);
        back = (Button) findViewById(R.id.back);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        invalid = (TextView) findViewById(R.id.invalid_info);
        firebaseAuth = FirebaseAuth.getInstance();

        login.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == login) {
            String emailText = email.getText().toString();
            String passwordText = password.getText().toString();
            signIn(emailText, passwordText);
        }
        if (v == back) {
            firebaseAuth.signOut();
            finish();
        }
    }

    private void signIn(String email, String password) {
        // If the email field or password is empty, send the user an appropriate message
        if (email.length()<1 || password.length()<1)
            Toast.makeText(getApplicationContext(), "Please enter email address and password", Toast.LENGTH_SHORT).show();
        else {
            // check if email and password is valid
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // get the user object
                        String userId = firebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users/parents");
                        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(userId)) {
                                    // send success message to user and move to parent interface
                                    Toast.makeText(getApplicationContext(),"Login successfully",Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(getApplicationContext(), ParentInterface.class);
                                    // Get the parent name, child id and kindergarten id and pass it to the next intent
                                    Parent p = dataSnapshot.child(userId).getValue(Parent.class);
                                    intent.putExtra("userName", p.getName());
                                    intent.putExtra("childId", p.getChildId());
                                    intent.putExtra("kindergartenId", p.getKindergartenId());
                                    startActivity(intent);
                                } else {
                                    resetLogin();
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.w(null, "loadPost:onCancelled", databaseError.toException());
                            }
                        });
                    // if the email and password are not valid
                    } else
                        resetLogin();
                }
            });
        }

    }

    private void resetLogin() {
        invalid.setText("Incorrect email address or password. Please try again.");
        email.setText("");
        password.setText("");
    }
}