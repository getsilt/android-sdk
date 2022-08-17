package com.silt.siltsdk;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/**
 * A content provider that allows to store the camera image internally without requesting the
 * permission to access the external storage to take shots.
 */
public class CameraPictureProvider extends ContentProvider {
    private static final String TAG = "CameraPictureProvider";
    public static final String FILENAME = "tempPicture.jpg"; // static to save storage space
    public static Uri content_uri; // = Uri.parse("content://com.silt.siltdemojava/cameraPicture");;

    @Override
    public boolean onCreate() {
        try {
            File picture = new File(getContext().getFilesDir(), FILENAME);
            content_uri = Uri.parse("content://"+getContext().getApplicationContext().getPackageName()+"/cameraPicture");
            if (!picture.exists())
                if (picture.createNewFile()) {
                    getContext().getContentResolver().notifyChange(content_uri, null);
                    Log.d(TAG, "created file uri: " + picture.getAbsolutePath());
                    return true;
                }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        try {
            File picture = new File(getContext().getFilesDir(), FILENAME);
            if (!picture.exists())
                picture.createNewFile();
            return ParcelFileDescriptor.open(picture, ParcelFileDescriptor.MODE_READ_WRITE);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        String lc = uri.getPath().toLowerCase();
        if (lc.endsWith(".jpg") || lc.endsWith(".jpeg"))
            return "image/jpeg";
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}