package com.view.core.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.List;

/**
 * @author:  xxx
 * @create: 2019/12/29 下午5:25
 * @email:  xxx.xxx.xxx
 * @version:
 * @descripe:
 **/
public class LocationUtil {

    private static final String TAG = LocationUtil.class.getSimpleName();

    private static LocationUtil INSTANCE;
    private Context mContext;

    private LocationManager mLocationManager;
    private String provider;
    private Location mLocation;

    public static LocationUtil getInstance() {
        if (INSTANCE == null)
            INSTANCE = new LocationUtil();
        return INSTANCE;
    }

    public boolean initLocationManager(Context context) {

        mContext = context;
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (mLocationManager == null) {
            Log.e(TAG, "initLocationManager: Location Service is null");
            return false;
        }

        List<String> list = mLocationManager.getAllProviders();
        if (list == null || list.size() < 1) {
            Log.e(TAG, "initLocationManager: there is no enable provider");
            return false;
        }
        if (list.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        }else if (list.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else {
            provider = null;
            Log.e(TAG, "initLocationManager: please check GPS or Network is turn on");
            return false;
        }

        Log.d(TAG, "initLocationManager: provider="+ provider);
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return false;
        }
        mLocationManager.requestLocationUpdates(provider, 1000, 1, mListener);
        return true;
    }

    private LocationListener mListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged: location="+ location);
            mLocation = location;
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
    };

    private Location getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getAllProviders();
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    public double[] getLastLocationInfo(){
        Location location = getLastKnownLocation();
        if(location == null){
            Log.e(TAG, "getLocationInfo: can't get location info");
            return null;
        }
        double[] result = new double[2];
        result[0] = location.getLongitude();
        result[1] = location.getLatitude();
        return result;
    }

    public double[] getLocationInfo() {
        if (mLocationManager == null || provider == null) {
            Log.e(TAG, "getLocationInfo: please executed initLocationManager first");
            return null;
        }
        double[] result = new double[2];
        if (ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        if(mLocation == null)
            mLocation = mLocationManager.getLastKnownLocation(provider);
        if(mLocation == null){
            Log.e(TAG, "getLocationInfo: can't get location info");
            return null;
        }
        result[0] = mLocation.getLongitude();
        result[1] = mLocation.getLatitude();
        return result;
    }

    public boolean destoryLocationManager(){
        if(mLocationManager != null)
            mLocationManager.removeUpdates(mListener);
        mLocation = null;
        return true;
    }
}
