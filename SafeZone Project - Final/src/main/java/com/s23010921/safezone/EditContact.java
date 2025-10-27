package com.s23010921.safezone;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EditContact extends AppCompatActivity {

    EditText etFirstName, etLastName, etContactNo;
    MaterialAutoCompleteTextView etPriority;
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

        // Setup the dropdown adapter for MaterialAutoCompleteTextView
        setupPriorityDropdown();

        Intent intent = getIntent();
        contactId = intent.getIntExtra("id", -1);
        String fname = intent.getStringExtra("fname");
        String lname = intent.getStringExtra("lname");
        String number = intent.getStringExtra("number");
        String priority = intent.getStringExtra("priority");

        etFirstName.setText(fname);
        etLastName.setText(lname);
        etContactNo.setText(number);

        etPriority.setText(priority, false);

        String currentPriority = getIntent().getStringExtra("priority");
        TextView tvCurrentPriority = findViewById(R.id.tvCurrentPriority);
        tvCurrentPriority.setText(currentPriority);

        setupPriorityBoxes();

        btnEdit.setOnClickListener(v -> {
            String newFname = etFirstName.getText().toString().trim();
            String newLname = etLastName.getText().toString().trim();
            String newNumber = etContactNo.getText().toString().trim();
            String newPriority = etPriority.getText().toString().trim();

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

    private void setupPriorityDropdown() {
        // Get the priority array from resources
        String[] priorityArray = getResources().getStringArray(R.array.priority);

        // Create adapter for the dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                priorityArray
        );

        // Set the adapter to the MaterialAutoCompleteTextView
        etPriority.setAdapter(adapter);

        // Optional: Set threshold for showing suggestions (1 means show after 1 character)
        etPriority.setThreshold(1);
    }

    public void setSosButton(View v) {
        NavigationHelper.goToSOS(this);
    }

    public void setHome(View v) {
        NavigationHelper.goToHome(this);
    }

    public void setLogout(View v) {
        NavigationHelper.goToLogout(this);
    }
    public void setProfile(View v) {
        NavigationHelper.goToProfile(this);
    }
    public void setContactList(View v) {
        NavigationHelper.goToContactList(this);
    }

    private void setupPriorityBoxes() {
        LinearLayout priorityContainer = findViewById(R.id.priorityContainer);
        priorityContainer.removeAllViews(); // Clear existing views

        List<ContactModel> allContacts = dbHelper.getAllContacts();
        Set<String> takenPriorities = new HashSet<>();

        // Get all taken priorities (excluding current contact)
        for (ContactModel contact : allContacts) {
            if (contact.getId() != contactId) {
                takenPriorities.add(contact.getPriority());
            }
        }

        // Create priority boxes for 1-20
        for (int i = 1; i <= 20; i++) {
            String priority = String.valueOf(i);
            boolean isTaken = takenPriorities.contains(priority);
            boolean isCurrent = priority.equals(getIntent().getStringExtra("priority"));

            MaterialCardView priorityCard = new MaterialCardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    dpToPx(50), dpToPx(50)
            );
            cardParams.setMargins(0, 0, dpToPx(8), 0);
            priorityCard.setLayoutParams(cardParams);
            priorityCard.setRadius(dpToPx(12));
            priorityCard.setCardElevation(4);

            if (isCurrent) {
                priorityCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                priorityCard.setStrokeWidth(dpToPx(2));
                priorityCard.setStrokeColor(getResources().getColor(android.R.color.holo_green_dark));
            } else if (isTaken) {
                priorityCard.setCardBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                priorityCard.setStrokeWidth(0);
            } else {
                priorityCard.setCardBackgroundColor(getResources().getColor(com.google.android.material.R.color.design_default_color_primary));
                priorityCard.setStrokeWidth(dpToPx(2));
                priorityCard.setStrokeColor(getResources().getColor(com.google.android.material.R.color.design_default_color_primary));
            }

            TextView priorityText = new TextView(this);
            priorityText.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            priorityText.setGravity(Gravity.CENTER);
            priorityText.setText(priority);
            priorityText.setTextSize(16);
            priorityText.setTypeface(null, Typeface.BOLD);

            if (isTaken && !isCurrent) {
                priorityText.setTextColor(getResources().getColor(android.R.color.white));
                priorityCard.setEnabled(false);
            } else {
                priorityText.setTextColor(getResources().getColor(android.R.color.white));
                priorityCard.setEnabled(true);
            }

            // Add click listener for available priorities
            if (!isTaken || isCurrent) {
                priorityCard.setOnClickListener(v -> {
                    etPriority.setText(priority, false);
                });
            }

            priorityCard.addView(priorityText);
            priorityContainer.addView(priorityCard);
        }
    }
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}