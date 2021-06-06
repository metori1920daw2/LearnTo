package com.game.learnto;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

public class ValidacioDades {
    private Context context;
    ValidacioDades(Context context) {
        this.context = context;
    }

  public  boolean CampOmplert(EditText editText, TextInputLayout textInputLayout, String message) {
        String value = editText.getText().toString().trim();
        if (value.isEmpty()) {
            textInputLayout.setError(message);
            hideKeyboardFrom(editText);
            return false;
        } else {
            textInputLayout.setErrorEnabled(false);
        }

        return true;
    }

    boolean  EsEmail(EditText editText, TextInputLayout textInputLayout, String message) {
        String value = editText.getText().toString().trim();
        if (value.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            textInputLayout.setError(message);
            hideKeyboardFrom(editText);
            return false;
        } else {
            textInputLayout.setErrorEnabled(false);
        }
        return true;
    }

    boolean ConfirmPassword(EditText editText1, EditText editText2, TextInputLayout textInputLayout, String message) {
        String v1 = editText1.getText().toString().trim();
        String v2 = editText2.getText().toString().trim();
        if (!v1.contentEquals(v2)) {
            textInputLayout.setError(message);
            hideKeyboardFrom(editText2);
            return false;
        } else {
            textInputLayout.setErrorEnabled(false);
        }
        return true;
    }

    void WrongPassword(EditText pass, TextInputLayout textInputLayout, String message) {
        textInputLayout.setError(message);
        hideKeyboardFrom(pass);

    }
    private void hideKeyboardFrom(View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}
