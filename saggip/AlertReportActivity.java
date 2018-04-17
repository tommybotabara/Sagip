package com.example.leebet_pc.saggip;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.location.LocationListener;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlertReportActivity extends AppCompatActivity {

    //Firebase db variables
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("message");

    double longitude;
    double latitude;

    private String address = "";

    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }
        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {}
        @Override
        public void onProviderEnabled(String s) {}
        @Override
        public void onProviderDisabled(String s) {}
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_report);

        Button reportButton = (Button)findViewById(R.id.alert_dialog_emergency_submit);
        Button falseAlarmButton = (Button)findViewById(R.id.alert_dialog_emergency_false_alarm);

        //Get current location
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }

        try{

        }catch(Exception e){}

        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50, 1, locationListener);

        Geocoder geocoder;
        List<Address> addresses = new ArrayList<>();
        geocoder = new Geocoder(this, Locale.getDefault());

        try{
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            address = addresses.get(0).getAddressLine(0);
        }
        catch (Exception e){}

        Log.i("location", address);

        //Get current date
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        final String formattedDate = "Date: " + df.format(c);

        final LinearLayout alertDialogText = (LinearLayout) findViewById(R.id.alert_dialog_emergency_linear_layout);
        final TextView locationText = (TextView) findViewById(R.id.alert_dialog_emergency_location);
        final TextView dateText = (TextView) findViewById(R.id.alert_dialog_emergency_date);
        final EditText details = (EditText) findViewById(R.id.alert_dialog_emergency_details);

        locationText.setText(address);
        dateText.setText(formattedDate);

        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Firebase
                //Write to database
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("report");

                // Creating new user node, which returns the unique key value
                // new user node would be /users/$userid/
                String userId = mDatabase.push().getKey();

                // creating user object
                AlertReportClass alertReport = new AlertReportClass(latitude, longitude, formattedDate, details.getText().toString());

                // pushing user to 'users' node using the userId
                mDatabase.child(userId).setValue(alertReport);
                finish();
            }
        });

        falseAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    @IgnoreExtraProperties
    public static class AlertReportClass {

        public double latitude;
        public double longitude;
        public String date;
        public String details;

        // Default constructor required for calls to
        // DataSnapshot.getValue(User.class)
        public AlertReportClass() {
        }

        public AlertReportClass(double latitude, double longitude, String date, String details) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.date = date;
            this.details = details;
        }
    }
}
