package uk.ac.excites.ucl.sapelliviewer.utils;

import java.io.IOException;

/**
 * Created by Julia on 21/02/2018.
 */

public class NoConnectivityException extends IOException {

    @Override
    public String getMessage() {
        return "No connectivity exception";
    }

}