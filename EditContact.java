package com.s23010921.safezone;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditContact extends AppCompatActivity {

    EditText etFirstName, etLastName, etContactNo;
    Spinner etPriority;
    Button btnEdit, btnDelete;
    ImageView homeIcon;
    DBHelper dbHelper;
    int contactId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);

        dbHelper = new DBHelper(this);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etContactNo = findViewById(R.id.etContactNo);
        etPriority = findViewById(R.id.etPriority);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        homeIcon = findViewById(R.id.homeIcon);

        Intent intent = getIntent();
        contactId = intent.getIntExtra("id", -1);
        String fname = intent.getStringExtra("fname");
        String lname = intent.getStringExtra("lname");
        String number = intent.getStringExtra("number");
        String priority = intent.getStringExtra("priority");

        etFirstName.setText(fname);
        etLastName.setText(lname);
        etContactNo.setText(number);

        int position = ((android.widget.ArrayAdapter<String>) etPriority.getAdapter()).getPosition(priority);
        etPriority.setSelection(position);

        btnEdit.setOnClickListener(v -> {
            String newFname = etFirstName.getText().toString().trim();
            String newLname = etLastName.getText().toString().trim();
            String newNumber = etContactNo.getText().toString().trim();
            String newPriority = etPriority.getSelectedItem().toString();

            if (dbHelper.isPriorityTaken(newPriority, contactId)) {
                Toast.makeText(this, "Priority already used by another contact!", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean updated = dbHelper.updateContact(contactId, newFname, newLname, newNumber, newPriority);
            if (updated) {
                Toast.makeText(this, "Contact updated!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Update failed!", Toast.LENGTH_SHORT).show();
            }
        });

        btnDelete.setOnClickListener(v -> {
            boolean deleted = dbHelper.deleteContact(number);
            if (deleted) {
                Toast.makeText(this, "Contact deleted!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Delete failed!", Toast.LENGTH_SHORT).show();
            }
        });

        homeIcon.setOnClickListener(v -> {
            startActivity(new Intent(this, DashBoard.class));
            finish();
        });
    }
}
