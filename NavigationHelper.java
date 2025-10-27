package com.s23010921.safezone;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

public class NavigationHelper {

    public static void goToSOS(Activity activity) {
        Intent intent = new Intent(activity, SOSActivatedActivity.class);
        activity.startActivity(intent);
    }

    public static void goToHome(Activity activity) {
        Intent intent = new Intent(activity, DashBoard.class);
        activity.startActivity(intent);
    }

    public static void goToProfile(Activity activity) {
        Intent intent = new Intent(activity, ProfileUpdate.class);
        activity.startActivity(intent);
    }

    public static void goToLogout(Activity activity) {
        Intent intent = new Intent(activity, Login.class);
        activity.startActivity(intent);
    }

    public static void goToContactList(Activity activity) {
        Intent intent = new Intent(activity, ContactList.class);
        activity.startActivity(intent);
    }
    public static void goToSafePlaces(Activity activity) {
        Intent intent = new Intent(activity, SafePlace.class);
        activity.startActivity(intent);
    }

    public static void goToShareLocation(Activity activity) {
        Intent intent = new Intent(activity, ShareLocation.class);
        activity.startActivity(intent);
    }
    public static void goToAddFeedback(Activity activity) {
        Intent intent = new Intent(activity, AddFeedBack.class);
        activity.startActivity(intent);
    }
    public static void goToFeedbackList(Activity activity) {
        Intent intent = new Intent(activity, ViewFeedback.class);
        activity.startActivity(intent);
    }
}
