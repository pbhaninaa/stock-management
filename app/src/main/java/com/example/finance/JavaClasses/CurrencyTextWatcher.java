package com.example.finance;


import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyTextWatcher implements TextWatcher {
    private final EditText editText;
    private String current = "";

    public CurrencyTextWatcher(EditText editText) {
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // No action needed
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!s.toString().equals(current)) {
            editText.removeTextChangedListener(this);
            // Temporarily remove the listener to prevent infinite loop

            String cleanString = s.toString().replaceAll("[^\\d]", "");
            // Remove all non-numeric characters
            double parsed = cleanString.isEmpty() ? 0.0 : Double.parseDouble(cleanString);

            // Use DecimalFormat to enforce two decimal places
            DecimalFormat formatter = new DecimalFormat("#,##0.00");
            String formatted = formatter.format(parsed / 100);
            // Divide by 100 to handle cents

            current = formatted;
            editText.setText(formatted);
            editText.setSelection(formatted.length());

            editText.addTextChangedListener(this); // Add the listener back
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        // No action needed
    }
}

