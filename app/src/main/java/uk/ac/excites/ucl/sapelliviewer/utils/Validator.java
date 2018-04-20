package uk.ac.excites.ucl.sapelliviewer.utils;

/**
 * Created by Julia on 17/02/2018.
 */

import android.widget.EditText;

import org.apache.commons.validator.routines.*;

public class Validator {
    public final static int URL = 0;
    public final static int EMAIL = 1;

    public static boolean isValid(EditText input, int type) {
        boolean isValid = false;
        switch (type) {
            case URL:
                isValid = isUrlValid(input);
                break;
            case EMAIL:
                isValid = isEmailValid(input);
                break;
        }
        return isValid;
    }

    private static boolean isUrlValid(EditText input) {
        String[] schemes = {"http", "https"};
        UrlValidator urlValidator = new UrlValidator(schemes);
        boolean isValid = urlValidator.isValid(input.getText().toString().trim());
        if (!isValid)
            input.setError("Please enter a valid URL");
        return isValid;
    }

    private static boolean isEmailValid(EditText input) {
        boolean isValid = EmailValidator.getInstance().isValid(input.getText().toString().trim());
        if (!isValid)
            input.setError("Please enter a valid email address");
        return isValid;
    }
}
