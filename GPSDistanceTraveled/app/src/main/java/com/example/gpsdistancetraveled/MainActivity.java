package com.example.gpsdistancetraveled;




import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;




import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;




import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;




public class MainActivity extends AppCompatActivity {


    private static final int PERMISSION_REQUEST_CODE = 100;
    TextView latlon;
    TextView Nandhikha;
    TextView addressText;
    TextView top3LocationsText;
    TextView distanceText;
    LocationManager locationManager;
    LocationListener locationListener;
    List<Address> addresses;
    Geocoder geocoder;
    ArrayList<Location> locationArrayList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        {
            latlon = findViewById(R.id.latitude);
            Nandhikha = findViewById(R.id.Nandhikha);
            addressText = findViewById(R.id.addressText);
            top3LocationsText = findViewById(R.id.top3LocationsText);
            distanceText = findViewById(R.id.distanceText);
        }


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationClass();
        geocoder = new Geocoder(this, Locale.getDefault());


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);

        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                }
                return;
        }
    }
    public class LocationClass implements LocationListener
    {
        private Location pastLoc;
        private Location currentLoc = null;
        private double totalDistance = 0.0;
        private long lastUpdateTime = 0L;
        private boolean start = true;
        private int top1 = 0;
        private int top2 = 0;
        private int top3 = 0;
        ArrayList<Location> top3LocAL = new ArrayList<>();
        public LocationClass() {

        }

        @Override
        public void onLocationChanged(@NonNull Location location) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();

            if (location != null) {

                locationArrayList.add(location);

                for (int i = 0; i < locationArrayList.size() - 1; i++)
                {
                    Location currentLocation = locationArrayList.get(i);

                    for (int j = i + 1; j < locationArrayList.size(); j++)
                    {
                        Location comparisonLocation = locationArrayList.get(j);

                        if (currentLocation.getLatitude() == comparisonLocation.getLatitude() && currentLocation.getLongitude() == comparisonLocation.getLongitude()) {
                            locationArrayList.remove(j);
                            j--;
                        }
                    }
                }
            }

            if (currentLoc == null) {
                currentLoc = location;
                lastUpdateTime = SystemClock.elapsedRealtime();
            }
            else {
                long currentTime = SystemClock.elapsedRealtime();
                long elapsedTime = currentTime - lastUpdateTime;

                pastLoc = currentLoc;
                currentLoc = location;

                if (elapsedTime >= 5000) {

                    double distance = currentLoc.distanceTo(pastLoc);

                    if (distance > 0) {
                        totalDistance += distance;

                        distanceText.setText("Distance: " + totalDistance + " m");

                        lastUpdateTime = SystemClock.elapsedRealtime();
                        top3 = top2;
                        top2 = top1;
                        top1 = (int) elapsedTime;
                    }
                }

                if(!(locationArrayList.isEmpty()))
                {
                    if ((int) elapsedTime > top1) {
                        top3 = top2;
                        top2 = top1;
                        top1 = (int) elapsedTime;
                        top3LocAL.add(0, location);
                    }
                    else if ((int) elapsedTime > top2 && locationArrayList.size() > 1)
                    {
                        top3 = top2;
                        top2 = (int) elapsedTime;
                        top3LocAL.add(1, location);
                    }
                    else if ((int) elapsedTime > top3 && locationArrayList.size() > 2)
                    {
                        top3 = (int) elapsedTime;
                        top3LocAL.add(2, location);
                    }

                    if(top3LocAL.size() > 2) {
                        for (int i = 0; i < top3LocAL.size() - 1; i++) {
                            Location currentLocation = top3LocAL.get(i);

                            for (int j = i + 1; j < top3LocAL.size(); j++) {
                                Location comparisonLocation = top3LocAL.get(j);

                                if (currentLocation.getLatitude() == comparisonLocation.getLatitude() &&
                                        currentLocation.getLongitude() == comparisonLocation.getLongitude()) {
                                    top3LocAL.remove(j);
                                    j--;
                                }
                            }
                        }
                    }

                    if (top3LocAL.size() > 3) {
                        top3LocAL.remove(3);
                    }

                        String top3Addresses = "Top 3 Locations: \n";
                        for(int i = 0; i < top3LocAL.size(); i++)
                        {
                            double toplat = top3LocAL.get(i).getLatitude();
                            double toplon = top3LocAL.get(i).getLongitude();

                            try {
                                addresses = geocoder.getFromLocation(toplat, toplon, 1);
                                String currentAddress = "Location " + (i+1) + ": " + addresses.get(0).getAddressLine(0);
                                top3Addresses += currentAddress + "\n";
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                        }
                    top3LocationsText.setText(top3Addresses);

                }


            }

            latlon.setText("Latitude: " + String.valueOf(lat).substring(0, 7) + "; Longitude: " + String.valueOf(lon).substring(0, 7));

            try {
                addresses = geocoder.getFromLocation(lat, lon, 1);
                String currentAddress = "Current Address: \n" + addresses.get(0).getAddressLine(0);
                addressText.setText(currentAddress);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {


        }

        @Override
        public void onProviderEnabled(String provider) {


        }

        @Override
        public void onProviderDisabled(String provider) {


        }
    }
}