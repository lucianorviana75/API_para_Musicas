package com.example.musicplayerlocal.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public class MusicRepository {

    public static List<String> getAllMusicPaths(Context context) {

        List<String> paths = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String selection =
                MediaStore.Audio.Media.IS_MUSIC + "!= 0 AND (" +
                        MediaStore.Audio.Media.MIME_TYPE + "=? OR " +
                        MediaStore.Audio.Media.MIME_TYPE + "=?)";

        String[] selectionArgs = {
                "audio/mpeg",
                "audio/mp4"
        };

        Cursor cursor = context.getContentResolver().query(
                uri,
                null,
                selection,
                selectionArgs,
                MediaStore.Audio.Media.TITLE + " ASC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String path = cursor.getString(
                        cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                );
                paths.add(path);
            }
            cursor.close();
        }

        return paths;
    }
}