package me.frigidambiance.projecttwo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameEditText, passwordEditText;
    private UserDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new UserDatabase(this);
        usernameEditText = findViewById(R.id.editTextUsername);
        passwordEditText = findViewById(R.id.editTextPassword);
        Button loginButton = findViewById(R.id.buttonLogin);
        Button createButton = findViewById(R.id.buttonCreateAccount);

        loginButton.setOnClickListener(view -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username and Password required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (db.checkUser(username, password)) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });

        createButton.setOnClickListener(view -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username and Password required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (db.createUser(username, password)) {
                Toast.makeText(this, "Account created. You can now log in.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
