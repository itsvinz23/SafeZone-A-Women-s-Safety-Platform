<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main"
    android:textColor="#000000"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#DAF7A6"
    tools:context=".SosSettings">

    <!-- Card Container -->
    <androidx.cardview.widget.CardView
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_margin="24dp"
        app:cardCornerRadius="24dp"
        app:cardElevation="8dp"
        app:cardBackgroundColor="@android:color/white"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="24dp"
            android:gravity="center_horizontal">

            <!-- Title -->
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="SOS Settings"
                android:textSize="24sp"
                android:textStyle="bold"
                android:textColor="#0b186d" />
            <ImageView
                android:layout_width="250dp"
                android:layout_height="170dp"
                android:layout_marginBottom="20dp"
                android:scaleType="centerCrop"
                android:src="@drawable/sos_settings2"/>

            <!-- Voice Trigger -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:layout_marginBottom="20dp">

                <TextView
                    android:layout_width="0dp"
                    android:layout_weight="1"
                    android:layout_height="wrap_content"
                    android:text="Voice trigger"
                    android:textColor="#0b186d"
                    android:textSize="16sp" />

                <Switch
                    android:id="@+id/switchVoiceTrigger"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:thumbTint="#C70039"
                    android:trackTint="#CBE8C4"/>
            </LinearLayout>

            <!-- Sound Alert -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:layout_marginBottom="20dp">

                <TextView
                    android:layout_width="0dp"
                    android:layout_weight="1"
                    android:layout_height="wrap_content"
                    android:text="Sound Alert"
                    android:textColor="#0b186d"
                    android:textSize="16sp" />

                <Switch
                    android:id="@+id/switchSoundAlert"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:thumbTint="#C70039"
                    android:trackTint="#CBE8C4"/>
            </LinearLayout>
            <!-- Auto Location Share -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:layout_marginBottom="20dp">

                <TextView
                    android:layout_width="0dp"
                    android:layout_weight="1"
                    android:layout_height="wrap_content"
                    android:text="Auto location share"
                    android:textColor="#0b186d"
                    android:textSize="16sp" />

                <Switch
                    android:id="@+id/switchAutoLocation"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:thumbTint="#C70039"
                    android:trackTint="#CBE8C4"/>
            </LinearLayout>

            <!-- Custom Message Section -->
            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Custom SOS message"
                android:textColor="#0b186d"
                android:textSize="16sp"
                android:layout_marginBottom="8dp"/>

            <EditText
                android:id="@+id/etCustomMessage"
                android:layout_width="match_parent"
                android:layout_height="49dp"
                android:background="@drawable/rounded_edittext"
                android:hint="Help me!!!"
                android:padding="12dp"
                android:textColor="#0b186d"
                android:textColorHint="#808080"
                android:layout_marginBottom="15dp" />

            <!-- Spinner 1: Alert Repeat Count -->
            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Alert Repeat Count"
                android:textColor="#0b186d"
                android:textSize="16sp"
                android:layout_marginBottom="8dp"/>

            <Spinner
                android:id="@+id/spinnerAlertRepeat"
                android:layout_width="match_parent"
                android:layout_height="49dp"
                android:background="@drawable/rounded_edittext"
                android:layout_marginBottom="15dp"
                android:entries="@array/alertRepeat"/>

            <!-- Spinner 2: Alert Interval -->
            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Alert Interval"
                android:textColor="@color/black"
                android:textSize="16sp"
                android:layout_marginBottom="8dp"/>

            <Spinner
                android:id="@+id/spinnerAlertInterval"
                android:layout_width="match_parent"
                android:layout_height="49dp"
                android:background="@drawable/rounded_edittext"
                android:layout_marginBottom="15dp"
                android:entries="@array/alertInterval"/>

            <!-- Save Button -->
            <Button
                android:id="@+id/btnSave"
                android:layout_width="match_parent"
                android:layout_height="50dp"
                android:onClick="setDashboard"
                android:text="Save Settings"
                android:textColor="@android:color/white"
                android:textStyle="bold"
                android:textSize="18sp"
                android:backgroundTint="#0b186d"
                android:elevation="4dp"
                android:stateListAnimator="@null"/>
        </LinearLayout>
    </androidx.cardview.widget.CardView>

</androidx.constraintlayout.widget.ConstraintLayout>
