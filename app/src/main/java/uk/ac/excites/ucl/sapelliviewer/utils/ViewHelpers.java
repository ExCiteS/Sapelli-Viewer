/**
 * Sapelli data collection platform: http://sapelli.org
 * <p>
 * Copyright 2012-2016 University College London - ExCiteS group
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.excites.ucl.sapelliviewer.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * @author mstevens
 *
 */
public final class ViewHelpers {

    private ViewHelpers() {
    }

    static public void setViewBackground(View view, Drawable background) {
        setViewBackgroundDrawable16AndUp(view, background);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    static private void setViewBackgroundDrawable16AndUp(View view, Drawable background) {
        view.setBackground(background);
    }

    /**
     * Method which returns the View representing the home button ("hamburger") on activities with an Action/Toolbar.
     *
     * TODO this doesn't seem to work (at least not on Lollipop) -> always returns null View
     *
     * @param activity
     * @return
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    static public View getActionBarHomeView(final Activity activity) {
        if (!(activity instanceof AppCompatActivity))
            return activity.findViewById(android.R.id.home);
        else
            return activity.findViewById(androidx.appcompat.R.id.home);
    }

    /**
     * Returns, as pixels, the default/preferred padding on used on (Alert)Dialog contents
     *
     * @param activity
     * @return
     */
    static public int getDefaultDialogPaddingPx(Activity activity) {
        TypedValue typedValue = new TypedValue();
        activity.getTheme().resolveAttribute(androidx.appcompat.R.attr.dialogPreferredPadding, typedValue, true);
        return (int) TypedValue.complexToDimensionPixelSize(typedValue.data, activity.getResources().getDisplayMetrics());
    }

    static public int getStartGravity() {
        return getStartGravityICS();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    static private int getStartGravityICS() {
        return Gravity.START;
    }

    static public int getEndGravity() {
        return getEndGravityICS();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    static private int getEndGravityICS() {
        return Gravity.END;
    }

    static public void setCompoundDrawablesRelativeWithIntrinsicBounds(TextView textView, int start, int top, int end, int bottom) {
        setCompoundDrawablesRelativeWithIntrinsicBoundsJBMR1(textView, start, top, end, bottom);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    static private void setCompoundDrawablesRelativeWithIntrinsicBoundsJBMR1(TextView textView, int start, int top, int end, int bottom) {
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom);
    }

    static public void setCompoundDrawablesRelativeWithIntrinsicBounds(TextView textView, Drawable start, Drawable top, Drawable end, Drawable bottom) {
        setCompoundDrawablesRelativeWithIntrinsicBoundsJBMR1(textView, start, top, end, bottom);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    static private void setCompoundDrawablesRelativeWithIntrinsicBoundsJBMR1(TextView textView, Drawable start, Drawable top, Drawable end, Drawable bottom) {
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom);
    }
}
