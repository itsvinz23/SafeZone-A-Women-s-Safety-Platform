package com.s23010921.safezone;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContactList extends AppCompatActivity {

    RecyclerView recyclerView;
    ContactAdapter adapter;
    DBHelper dbHelper;
    ImageView homeIcon;
    Button btnCreateNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        recyclerView = findViewById(R.id.recyclerView);
        dbHelper = new DBHelper(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<ContactModel> contacts = dbHelper.getAllContacts();
        adapter = new ContactAdapter(ContactList.this, contacts);
        recyclerView.setAdapter(adapter);

        homeIcon = findViewById(R.id.homeIcon);
        homeIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ContactList.this, DashBoard.class);
            startActivity(intent);
        });
    }

    public void setAddContactIcon(View v){
        btnCreateNew = findViewById(R.id.btnCreateNew);
        Intent intent = new Intent(ContactList.this, CreateContact.class);
        startActivity(intent);
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

}
