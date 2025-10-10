package com.s23010921.safezone;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class DashBoard extends AppCompatActivity {

    CardView safePlacesCard,settingsCard,contactsCard;
    ImageView imgSafePlaces,imgSOSSettings,imgContacts;
    TextView txtSafePlaces,txtSOSSettings,txtContacts;

    MaterialButton sosButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dash_board);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
            });
        }

        public void setSafePlace(View v){
            safePlacesCard = findViewById(R.id.safePlacesCard);
            imgSafePlaces = findViewById(R.id.imgSafePlaces);
            txtSafePlaces = findViewById(R.id.txtSafePlaces);
                Intent intent = new Intent(DashBoard.this, SafePlace.class);
                startActivity(intent);

        }
        public void setSOSSettings(View v){
            settingsCard = findViewById(R.id.settingsCard);
            imgSOSSettings = findViewById(R.id.imgSafePlaces);
            txtSOSSettings = findViewById(R.id.txtSOSSettings);
            Intent intent = new Intent(DashBoard.this, SosSettings.class);
            startActivity(intent);
        }
        public void setContactsCard(View v){
            contactsCard = findViewById(R.id.contactsCard);
            imgContacts = findViewById(R.id.imgContacts);
            txtContacts = findViewById(R.id.txtContacts);
            Intent intent = new Intent(DashBoard.this, ContactList.class);
            startActivity(intent);
        }

        public void setSosButton(View v){
            sosButton = findViewById(R.id.sosButton);
            Intent intent = new Intent(DashBoard.this, SOSActivatedActivity.class);
            startActivity(intent);
        }

}