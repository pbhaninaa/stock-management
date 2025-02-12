package com.example.finance;


import static android.content.Context.MODE_PRIVATE;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

import android.telephony.TelephonyManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.Manifest;

import androidx.core.app.ActivityCompat;

import java.util.List;




import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class Utils {
    public static final String PREF_NAME = "UserPrefs";
    public static final String EMAIL_KEY = "email";
    public static final String PASSWORD_KEY = "password";
    private static final String REMEMBER_ME = "rememberMe";

    public static void hideSoftNavBar(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public static void handleAppUninstall(Context context) {
        // Email subject and body
        String subject = "App Uninstallation Attempt";
        String body = "Dear Admin,\nWe noticed that someone is attempting to uninstall the app at " + getCurrentDateTime() + "." +
                "\n\nIf you need assistance, feel free to reach out to us.";

        // Send the email
        Communication.sendEmail(context, subject, body, success -> {
            if (success) {
            }
        });

        // Optionally, show a confirmation or a toast that the uninstall email has been triggered
        showToast(context, "Uninstall detected. Email sent to admin.");
    }
    public static String getCurrentDateTime() {
        // Create a SimpleDateFormat to define the format you want
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy 'at' hh:mma");

        // Get the current date and time
        Date currentDate = new Date();

        // Format the date and return it as a string
        return sdf.format(currentDate);
    }
    public static void setCaps(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isUpdating) {
                    isUpdating = true;
                    String text = s.toString();
                    if (!text.isEmpty() && Character.isLowerCase(text.charAt(0))) {
                        // Capitalize the first letter
                        String updatedText = Character.toUpperCase(text.charAt(0)) + text.substring(1);
                        editText.setText(updatedText);
                        editText.setSelection(updatedText.length()); // Move cursor to the end
                    }
                    isUpdating = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }
        });
    }

    public static void rotateImageView(ImageView imageView) {
        // Create an ObjectAnimator to rotate the ImageView
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f);
        rotateAnimator.setDuration(1000);  // Duration for one full rotation (in milliseconds)
        rotateAnimator.setRepeatCount(ObjectAnimator.INFINITE);  // Repeat infinitely
        rotateAnimator.setRepeatMode(ObjectAnimator.RESTART);  // Restart animation after one full rotation
        rotateAnimator.start();  // Start the animation
    }

    public static void LoadingLayout(Activity activity, Context context) {
        ConstraintLayout layout = activity.findViewById(R.id.load_layout);
        ImageView imageView = activity.findViewById(R.id.load_layout_image);

        if (layout == null || imageView == null) {
            showToast(context, "Error: Unable to find required views!");
            System.out.println("Error: Views are null!");
            return;
        }

        rotateImageView(imageView);
        layout.setVisibility(View.VISIBLE);

        new Handler().postDelayed(() -> {
            layout.setVisibility(View.GONE);
        }, 3000); // 5 seconds delay
    }

    private static boolean isValidPhoneNumber(String phoneNumber) {
        // Example validation for phone number (basic check for length and if it starts with a country code)
        return phoneNumber.matches("^\\+?[1-9][0-9]{7,14}$"); // Simple regex for international phone numbers
    }
    public static void setStockAvailabilityColor(TextView textView, int quantity) {
        if (quantity <= 20) {
            // Extremely low stock - Red
            textView.setTextColor(Color.RED);
        } else if (quantity <= 80) {
            // Low stock - Orange
            textView.setTextColor(Color.parseColor("#FFA500")); // Orange
        } else {
            textView.setTextColor(Color.parseColor("#FFA500")); // Orange

        }

        // Set the text
        textView.setText(String.valueOf(quantity));
    }

    public static void showToast(Context context, String message) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.custom_toast, null);

        TextView text = layout.findViewById(R.id.textViewToast);
        text.setText(message);

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    public static void success(Context context, String message) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.custom_toast, null);

        TextView text = layout.findViewById(R.id.textViewToast);
        text.setTextColor(ContextCompat.getColor(context, R.color.white));
        layout.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.white));
        text.setText(message);


        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    public static void saveString(Context context, String Pref_name, String Pref_Key, String Value) {
        SharedPreferences prefs = context.getSharedPreferences(Pref_name, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Pref_Key, Value);
        editor.apply();
    }

    public static void setFieldFocus(EditText field, Context context) {
        // Request focus for the field
        field.requestFocus();

        // Get the InputMethodManager system service
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        // Show the soft keyboard
        if (imm != null) {
            imm.showSoftInput(field, InputMethodManager.SHOW_IMPLICIT);
        }
    }
    public static String getDeviceLocation(Context context) {
        // Initialize location provider
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return "Location permission not granted";
        }

        // Use a background thread to fetch location
        ExecutorService executor = Executors.newSingleThreadExecutor();
        String[] locationResult = {"Location not found"};

        executor.execute(() -> {
            try {
                Location location = fusedLocationClient.getLastLocation().getResult();
                if (location != null) {
                    locationResult[0] = "Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude();
                }
            } catch (Exception e) {
                Log.e("LocationError", "Failed to get location", e);
            }
        });

        return locationResult[0]; // Return coordinates or "Location not found"
    }
}