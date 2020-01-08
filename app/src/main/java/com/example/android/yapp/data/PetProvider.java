package com.example.android.yapp.data;

import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.Selection;
import android.util.Log;
import android.widget.Toast;

import com.example.android.yapp.CatalogActivity;

import java.security.PublicKey;

/**
 * Created by user on 8/13/2019.
 */

public class PetProvider extends ContentProvider {

    PetDbHelper mdbHelper;
    private static final int PETS = 100;
    private static final int PETS_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static{
        sUriMatcher.addURI(PetContract.PetEntry.AUTHORITY, PetContract.PetEntry.TABLE_NAME, PETS);

        sUriMatcher.addURI(PetContract.PetEntry.AUTHORITY, PetContract.PetEntry.TABLE_NAME + "/#", PETS_ID);
    }

    /** Tag for the log messages */
    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        // TODO: Create and initialize a PetDbHelper object to gain access to the pets database.
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        mdbHelper = new PetDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase database = mdbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);

        switch (match) {
            case PETS:
                cursor = database.query(PetContract.PetEntry.TABLE_NAME, projection, selection, selectionArgs, null , null, sortOrder);
                        break;
            case PETS_ID:
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(PetContract.PetEntry.TABLE_NAME, projection, selection, selectionArgs, null , null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URi " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        int match = sUriMatcher.match(uri);

        switch (match){
            case PETS:
                return insertPet(uri, contentValues);

            default:
                throw new IllegalArgumentException("Cannot insert into unknown URI " + uri);
        }

    }

    private Uri insertPet(Uri uri, ContentValues values){

        String name = values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);

        if(name == null){
            throw new IllegalArgumentException ("Pet requires a name");
        }

        Integer gender = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);

        if(gender == null || !PetContract.PetEntry.isValidGender(gender)){
            throw new IllegalArgumentException ("Pet requires a valid gender");
        }

        Integer weight = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);

        if(weight != null && weight < 0){
            throw new IllegalArgumentException ("Pet requires a valid weight");
        }

        SQLiteDatabase database = mdbHelper.getWritableDatabase();
        long id = database.insert(PetContract.PetEntry.TABLE_NAME, null, values);

        if(id == -1){
            Log.e(LOG_TAG, "Failed to insert row for "+ uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(PetContract.PetEntry.CONTENT_URI, id);
    }


    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        int match = sUriMatcher.match(uri);

        switch (match){
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);

            case PETS_ID:
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs =new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for "+ uri);
        }
    }

    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs){

        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_NAME)){
            String name = values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);

            if(name == null){
                throw new IllegalArgumentException ("Pet requires a name");
            }
        }

        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_BREED)){

        }
        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_GENDER)){
            Integer gender = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);

            if(gender == null || !PetContract.PetEntry.isValidGender(gender)){
                throw new IllegalArgumentException ("Pet requires a valid gender");
            }
        }
        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_WEIGHT)){
            Integer weight = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);

            if(weight != null && weight < 0){
                throw new IllegalArgumentException ("Pet requires a valid weight");
            }
        }

        if(values.size() == 0){
            return 0;
        }

        SQLiteDatabase database = mdbHelper.getWritableDatabase();
        int updateInt = database.update(PetContract.PetEntry.TABLE_NAME, values, selection, selectionArgs);
        if (updateInt != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updateInt;


    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        int match = sUriMatcher.match(uri);

        switch (match){
            case PETS:
                return deletePet(uri, selection, selectionArgs);
            case PETS_ID:
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return deletePet(uri, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion is not supported for "+ uri);

        }
    }

    private int deletePet(Uri uri, String selection, String[] selectionArgs){
        SQLiteDatabase database = mdbHelper.getWritableDatabase();

        int rowsDeleted =  database.delete(PetContract.PetEntry.TABLE_NAME, selection, selectionArgs);
        if (rowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match){
            case PETS:
                return PetContract.PetEntry.CONTENT_LIST_TYPE;
            case PETS_ID:
                return PetContract.PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI" + uri + "with match" + match);
        }

    }
}
