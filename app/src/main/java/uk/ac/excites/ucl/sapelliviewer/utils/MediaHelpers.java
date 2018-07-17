package uk.ac.excites.ucl.sapelliviewer.utils;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.PictureDrawable;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.caverock.androidsvg.SVG;

import java.io.File;
import java.io.FileInputStream;
import java.util.regex.Pattern;

/**
 * @author Michalis Vitos, mstevens
 */
public final class MediaHelpers {

    /**
     * This class should never be instantiated
     */
    private MediaHelpers() {
    }

    /**
     * Image size
     */
    static private final int SIZE = 150;


    /**
     * Pattern to recognise audio files by their extension.
     * <p>
     * Based on supported audio/container file types in Android: http://developer.android.com/guide/appendix/media-formats.html
     */
    static private final Pattern audioFilePattern = Pattern.compile("(.*/)*.+\\.(3gp|mp4|mp3|m4a|aac|ts|flac|mid|xmf|mxmf|rtttl|rtx|ota|imy|ogg|mkv|wav)$", Pattern.CASE_INSENSITIVE);

    /**
     * Pattern to recognise raster image files by their extension.
     */
    static private final Pattern rasterImageFilePattern = Pattern.compile("(.*/)*.+\\.(png|jpg|gif|bmp|jpeg)$", Pattern.CASE_INSENSITIVE);

    /**
     * Pattern to recognise vector image files by their extension.
     */
    static private final Pattern vectorImageFilePattern = Pattern.compile("(.*/)*.+\\.(svg|svgz)$", Pattern.CASE_INSENSITIVE);

    /**
     * Checks whether a filename (or path) has an audio file type extension.
     * <p>
     * Recognises all Android-supported audio/container file types: http://developer.android.com/guide/appendix/media-formats.html
     *
     * @param fileNameOrPath
     * @return
     */
    static public boolean isAudioFileName(String fileNameOrPath) {
        return fileNameOrPath != null && audioFilePattern.matcher(fileNameOrPath).matches();
    }

    /**
     * Checks whether a filename (or path) has an image (raster or vector) file type extension (PNG, JPG/JPEG, GIF, BMP, SVG or SVGZ)
     *
     * @param fileNameOrPath
     * @return
     */
    static public boolean isImageFileName(String fileNameOrPath) {
        return fileNameOrPath != null && (isRasterImageFileName(fileNameOrPath) || isVectorImageFileName(fileNameOrPath));
    }

    /**
     * Checks whether a filename (or path) has an raster image file type extension (PNG, JPG/JPEG, GIF or BMP)
     *
     * @param fileNameOrPath
     * @return
     */
    static public boolean isRasterImageFileName(String fileNameOrPath) {
        return fileNameOrPath != null && rasterImageFilePattern.matcher(fileNameOrPath).matches();
    }

    /**
     * Checks whether a filename (or path) has an vector image file type extension (SVG or SVGZ)
     *
     * @param fileNameOrPath
     * @return
     */
    static public boolean isVectorImageFileName(String fileNameOrPath) {
        return fileNameOrPath != null && vectorImageFilePattern.matcher(fileNameOrPath).matches();
    }


    public static View createView(ImageView view, String path) {
        boolean isVectorBased = MediaHelpers.isVectorImageFileName(path);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(SIZE, SIZE);
        view.setLayoutParams(layoutParams);


        // Set scaling (raster-based images are only scaled down, never up; vector-based ones can be scaled up or down):
        view.setScaleType(isVectorBased ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_INSIDE);

        /* Disable h/w acceleration for vector (SVG) images
         * Reason explained here:
         *  - https://github.com/japgolly/svg-android/commit/a1a613b
         *  - http://stackoverflow.com/q/10384613/1084488 */
        if (isVectorBased)
            ViewCompat.setLayerType(view, ViewCompat.LAYER_TYPE_SOFTWARE, null);

        // Set image:
        try {
            if (!isVectorBased) {    // Raster image (PNG, JPG, GIF, ...):
                Bitmap img = BitmapFactory.decodeFile(path);
                view.setImageBitmap(img);
            } else {
                // Vector image (SVG or SVGZ):
                view.setImageDrawable(new PictureDrawable(
                        SVG.getFromInputStream(new FileInputStream(new File(path))).renderToPicture()));
            }
        } catch (Exception e) {
            Log.e("Load image", "Could not load image from " +
                    (isVectorBased ? "vector file: " : "raster file: ") + e.getMessage());
        }

        return view;
    }

}