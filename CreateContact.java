package com.s23010921.safezone;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CreateContact extends AppCompatActivity {

    EditText etFirstName, etLastName, etContactNo;
    Spinner etPriority;
    Button btnSave;
    DBHelper dbHelper;
    ImageView contactListIcon,homeIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_contact);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etContactNo = findViewById(R.id.etContactNo);
        etPriority = findViewById(R.id.etPriority);
        btnSave = findViewById(R.id.btnSave);
        contactListIcon = findViewById(R.id.contactListIcon);
        homeIcon = findViewById(R.id.homeIcon);


        dbHelper = new DBHelper(this);

        btnSave.setOnClickListener(view -> {
            String fname = etFirstName.getText().toString().trim();
            String lname = etLastName.getText().toString().trim();
            String number = etContactNo.getText().toString().trim();
            String priority = etPriority.getSelectedItem().toString();

            if (fname.isEmpty() || lname.isEmpty() || number.isEmpty()) {
                Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            } else if (dbHelper.isPriorityTaken(priority)) {
                Toast.makeText(this, "Priority level already assigned!", Toast.LENGTH_SHORT).show();
            } else {
                boolean success = dbHelper.insertContact(fname, lname, number, priority);
                if (success) {
                    Toast.makeText(this, "Contact Saved!", Toast.LENGTH_SHORT).show();
                    clearFields();
                } else {
                    Toast.makeText(this, "Error saving contact.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // Contact List icon click
        contactListIcon.setOnClickListener(v -> {
            Intent intent = new Intent(CreateContact.this, ContactList.class);
            startActivity(intent);
        });

        // Contact List icon click
        homeIcon.setOnClickListener(v -> {
            Intent intent = new Intent(CreateContact.this, DashBoard.class);
            startActivity(intent);
        });
    }

    private void clearFields() {
        etFirstName.setText("");
        etLastName.setText("");
        etContactNo.setText("");
        etPriority.setSelection(0);
    }
}
