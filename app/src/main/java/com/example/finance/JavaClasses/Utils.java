package com.example.finance;

import static android.content.Context.MODE_PRIVATE;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
    public static final String ROLE_OWNER = "Owner";
    public static final String ROLE_MANAGER = "Manager";
    public static final String ROLE_CASHIER = "Cashier";

    public static final String TAVERN_PREFS = "TavernPrefs";
    public static final String SESSION_PREFS = "UserSession";
    public static final String SESSION_USER_ID = "sessionUserId";
    public static final String SESSION_USER_NAME = "sessionUserName";
    public static final String SESSION_USER_ROLE = "sessionUserRole";

    public static void hideSoftNavBar(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public static void handleAppUninstall(Context context) {
        String subject = "App Uninstallation Attempt";
        String body = "An uninstall attempt was detected at " + getCurrentDateTime() + ".";
        Communication.sendEmail(context, subject, body, success -> {
        });
    }

    public static String getCurrentDateTime() {
        return new SimpleDateFormat("dd MMM yyyy 'at' hh:mma", Locale.getDefault()).format(new Date());
    }

    public static void setCaps(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isUpdating) {
                    isUpdating = true;
                    String text = s.toString();
                    if (!text.isEmpty() && Character.isLowerCase(text.charAt(0))) {
                        String updatedText = Character.toUpperCase(text.charAt(0)) + text.substring(1);
                        editText.setText(updatedText);
                        editText.setSelection(updatedText.length());
                    }
                    isUpdating = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public static void rotateImageView(ImageView imageView) {
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f);
        rotateAnimator.setDuration(1000);
        rotateAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        rotateAnimator.setRepeatMode(ObjectAnimator.RESTART);
        rotateAnimator.start();
    }

    public static void LoadingLayout(Activity activity, Context context) {
        ConstraintLayout layout = activity.findViewById(R.id.load_layout);
        ImageView imageView = activity.findViewById(R.id.load_layout_image);

        if (layout == null || imageView == null) {
            showToast(context, "Error: Unable to find required views!");
            return;
        }

        rotateImageView(imageView);
        layout.setVisibility(View.VISIBLE);
        new Handler().postDelayed(() -> layout.setVisibility(View.GONE), 3000);
    }

    public static void setStockAvailabilityColor(TextView textView, int quantity) {
        if (quantity <= 20) {
            textView.setTextColor(Color.RED);
        } else if (quantity <= 80) {
            textView.setTextColor(Color.parseColor("#FFA500"));
        } else {
            textView.setTextColor(ContextCompat.getColor(textView.getContext(), R.color.white));
        }
        textView.setText(String.valueOf(quantity));
    }

    public static void showToast(Context context, String message) {
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.custom_toast, null);
        TextView text = layout.findViewById(R.id.textViewToast);
        text.setText(message);
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    public static void success(Context context, String message) {
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.custom_toast, null);
        TextView text = layout.findViewById(R.id.textViewToast);
        text.setTextColor(ContextCompat.getColor(context, R.color.white));
        layout.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.warm_primary));
        text.setText(message);
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    public static void saveString(Context context, String prefName, String prefKey, String value) {
        SharedPreferences prefs = context.getSharedPreferences(prefName, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(prefKey, value);
        editor.apply();
    }

    public static void setFieldFocus(EditText field, Context context) {
        field.requestFocus();
        InputMethodManager imm =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(field, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public static String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    public static String hashPassword(String password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        byte[] hash = sha256(salt, password);
        return Base64.encodeToString(salt, Base64.NO_WRAP) + ":" +
                Base64.encodeToString(hash, Base64.NO_WRAP);
    }

    public static boolean verifyPassword(String password, String storedValue) {
        if (storedValue == null || !storedValue.contains(":")) {
            return false;
        }

        String[] parts = storedValue.split(":");
        if (parts.length != 2) {
            return false;
        }

        byte[] salt = Base64.decode(parts[0], Base64.NO_WRAP);
        byte[] expectedHash = Base64.decode(parts[1], Base64.NO_WRAP);
        byte[] actualHash = sha256(salt, password);

        if (expectedHash.length != actualHash.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < expectedHash.length; i++) {
            result |= expectedHash[i] ^ actualHash[i];
        }
        return result == 0;
    }

    private static byte[] sha256(byte[] salt, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            return digest.digest(password.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    public static void saveSession(Context context, User user) {
        SharedPreferences prefs = context.getSharedPreferences(SESSION_PREFS, MODE_PRIVATE);
        prefs.edit()
                .putLong(SESSION_USER_ID, user.getId())
                .putString(SESSION_USER_NAME, user.getFullName())
                .putString(SESSION_USER_ROLE, user.getRole())
                .apply();
    }

    public static void clearSession(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SESSION_PREFS, MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    public static boolean hasActiveSession(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SESSION_PREFS, MODE_PRIVATE);
        return prefs.contains(SESSION_USER_ID);
    }

    public static long getSessionUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SESSION_PREFS, MODE_PRIVATE);
        return prefs.getLong(SESSION_USER_ID, -1L);
    }

    public static String getSessionUserName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SESSION_PREFS, MODE_PRIVATE);
        return prefs.getString(SESSION_USER_NAME, "");
    }

    public static String getSessionUserRole(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SESSION_PREFS, MODE_PRIVATE);
        return prefs.getString(SESSION_USER_ROLE, "");
    }

    public static boolean isOwner(Context context) {
        return ROLE_OWNER.equals(getSessionUserRole(context));
    }

    public static boolean canEditInventory(Context context) {
        String role = getSessionUserRole(context);
        return ROLE_OWNER.equals(role) || ROLE_MANAGER.equals(role);
    }

    public static boolean canViewReports(Context context) {
        String role = getSessionUserRole(context);
        return ROLE_OWNER.equals(role) || ROLE_MANAGER.equals(role);
    }

    public static boolean canManageUsers(Context context) {
        return ROLE_OWNER.equals(getSessionUserRole(context));
    }

    public static boolean canResetDatabase(Context context) {
        return ROLE_OWNER.equals(getSessionUserRole(context));
    }

    public static boolean canSell(Context context) {
        String role = getSessionUserRole(context);
        return ROLE_OWNER.equals(role) || ROLE_MANAGER.equals(role) || ROLE_CASHIER.equals(role);
    }

    public static boolean hasTavernDetails(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(TAVERN_PREFS, MODE_PRIVATE);
        return !prefs.getString("tavernName", "").trim().isEmpty()
                && !prefs.getString("barmanName", "").trim().isEmpty();
    }

    public static void saveTavernDetails(Context context, String tavernName, String barmanName) {
        SharedPreferences prefs = context.getSharedPreferences(TAVERN_PREFS, MODE_PRIVATE);
        prefs.edit()
                .putString("tavernName", tavernName.trim())
                .putString("barmanName", barmanName.trim())
                .apply();
    }

    public static String getTavernName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(TAVERN_PREFS, MODE_PRIVATE);
        return prefs.getString("tavernName", "");
    }

    public static String getBarmanName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(TAVERN_PREFS, MODE_PRIVATE);
        return prefs.getString("barmanName", "");
    }
}