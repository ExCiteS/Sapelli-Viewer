package uk.ac.excites.ucl.sapelliviewer.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import uk.ac.excites.ucl.sapelliviewer.utils.ScreenMetrics;
import uk.ac.excites.ucl.sapelliviewer.utils.ViewHelpers;

/**
 * This class makes if possible to safely tie fragments to a specific Activity (i.e. ProjectManagerActivity),
 * instead of using just casting getActivity(), which feels more than a bit hackish.
 *
 * @author mstevens
 * @see http://stackoverflow.com/a/24844574/1084488
 */
public abstract class ProjectManagerFragment extends DialogFragment {

    static protected final float DIALOG_VIEW_TOP_PADDING_DP = 0f;

    private AppCompatActivity activity;
    private boolean uiReady = false;

    public AppCompatActivity getOwner() {
        return activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AppCompatActivity)
            this.activity = (AppCompatActivity) context;
    }

    /**
     * Note: it's OK to return null from here
     *
     * @see androidx.fragment.app.Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getShowsDialog() || getLayoutID() == null)
            return super.onCreateView(inflater, container, savedInstanceState); // avoids crash (see stackoverflow link above)
        else
            return getRootLayout(inflater, container);
    }

    protected abstract Integer getLayoutID();

    protected final View getRootLayout() {
        return getRootLayout(getActivity().getLayoutInflater(), null);
    }

    protected final View getRootLayout(LayoutInflater inflater, ViewGroup container) {
        View rootLayout = null;
        try {
            // Inflate & check rootLayout:
            rootLayout = inflater.inflate(getLayoutID(), container, false);
            if (rootLayout == null)
                throw new NullPointerException("rootLayout null");

            // Setup UI:
            setupUI(rootLayout);

            // Done:
            uiReady = true;
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error upon inflating/setting-up framgement UI", e);
            uiReady = false;
        }
        return rootLayout;
    }

    /**
     * To be overridden by subclasses that don't need the owner activity to set-up the UI.
     *
     * @param rootLayout - never null
     * @throws Exception
     */
    protected void setupUI(final View rootLayout) throws Exception {
        // Make sure we know the owner:
        final AppCompatActivity owner = getOwner();
        if (owner == null) // just in case...
            throw new NullPointerException("Cannot get owner activity");

        // Do actual UI set-up:
        setupUI(owner, rootLayout);
    }

    /**
     * To be overridden by subclasses that need the owner activity to set-up the UI.
     *
     * @param owner      - never null
     * @param rootLayout - never null
     * @throws Exception
     */
    protected void setupUI(final AppCompatActivity owner, final View rootLayout) throws Exception {
        // does nothing by default
    }

    /**
     * @return the uiReady
     */
    public final boolean isUIReady() {
        return uiReady && activity != null;
    }

    protected View setDialogView(AlertDialog dialog) {
//        if ()
        return doSetDialogView(dialog, null, null, null, null);
    }

    protected View setDialogView(AlertDialog dialog, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight, int viewSpacingBottom) {
        return doSetDialogView(dialog, viewSpacingLeft, viewSpacingTop, viewSpacingRight, viewSpacingBottom);
    }

    private View doSetDialogView(AlertDialog dialog, Integer viewSpacingLeft, Integer viewSpacingTop, Integer viewSpacingRight, Integer viewSpacingBottom) {
        View rootLayout = null;
        if (dialog != null && (rootLayout = getRootLayout()) != null) {
            if (viewSpacingLeft == null)
                dialog.setView(rootLayout);
            else
                dialog.setView(rootLayout, viewSpacingLeft, viewSpacingTop, viewSpacingRight, viewSpacingBottom);
        }
        return rootLayout;
    }

    @Override
    public void onDetach() {
        activity = null;
        super.onDetach();
    }

    protected int getDialogLeftRightPaddingPx() {
        return ViewHelpers.getDefaultDialogPaddingPx(getActivity());
    }

    protected int getDialogMessageToViewSpacingPx() {
        return ScreenMetrics.ConvertDipToPx(getActivity(), DIALOG_VIEW_TOP_PADDING_DP);
    }
}
