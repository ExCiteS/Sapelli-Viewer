package uk.ac.excites.ucl.sapelliviewer.utils;

import java.io.IOException;

/**
 * Created by Julia on 21/02/2018.
 */

public class GeoKeyException extends IOException {
    private int responseCode;
    private String message;

    public GeoKeyException(int code, String message) {
        this.responseCode = code;
        this.message = message;
    }

    public int getResponseCode() {
        return responseCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
