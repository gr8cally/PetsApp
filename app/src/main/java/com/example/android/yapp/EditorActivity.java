/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.yapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.yapp.data.PetContract;
import com.example.android.yapp.data.PetDbHelper;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    private boolean mPetHasChanged = false;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int     mGender = 0;

    Uri singleUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = this.getIntent();
        singleUri = intent.getData();
        Log.e("Tag", singleUri + " kk");
        if (singleUri == null){
            setTitle(getString(R.string.editor_activity_title_new_pet));

            invalidateOptionsMenu();
            }
        else {
            getSupportActionBar().setTitle("Edit Pet");
            getSupportLoaderManager().initLoader(0, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        setupSpinner();

        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (singleUri == null){
            MenuItem item = menu.findItem(R.id.action_delete);
            item.setVisible(false);
        }
        return true;
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetContract.PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetContract.PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetContract.PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    public void zeroDefault(EditText editText){
        if (TextUtils.isEmpty(editText.getText().toString().trim())){
            editText.setText("0");
        }
    }

    public void insertPet(){
        zeroDefault(mWeightEditText);
        String inputName = mNameEditText.getText().toString().trim();

        /** EditText field to enter the pet's breed */
        String inputBreed =  mBreedEditText.getText().toString().trim();

        /** EditText field to enter the pet's weight */
        int inputWeight = Integer.parseInt(mWeightEditText.getText().toString().trim());

        /** EditText field to enter the pet's gender */
        String inputGender =  mGenderSpinner.getSelectedItem().toString();

        if(TextUtils.isEmpty(inputBreed) && TextUtils.isEmpty(inputName) && mGenderSpinner.getSelectedItemPosition() == 0){
            return;
        }

        ContentValues values = new ContentValues();
        values.put(PetContract.PetEntry.COLUMN_PET_NAME, inputName);
        values.put(PetContract.PetEntry.COLUMN_PET_BREED, inputBreed);
        values.put(PetContract.PetEntry.COLUMN_PET_WEIGHT, inputWeight);
        values.put(PetContract.PetEntry.COLUMN_PET_GENDER, mGender);


        Uri uri = getContentResolver().insert(PetContract.PetEntry.CONTENT_URI, values);
//        PetDbHelper mDbHelper = new PetDbHelper(this);
//        SQLiteDatabase liteDatabase =  mDbHelper.getWritableDatabase();
//        long newRowId = liteDatabase.insert(PetContract.PetEntry.TABLE_NAME, null, values);
//
        if(uri == null){
            Toast.makeText(this, getString(R.string.pet_failed), Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this,getString(R.string.pet_added), Toast.LENGTH_SHORT).show();
        }
    }

    public void updatePet(Uri uri){
        String updatedName = mNameEditText.getText().toString().trim();
        String updatedBreed = mBreedEditText.getText().toString().trim();
        int updatedGender = mGender;
        int updatedWeight = Integer.parseInt(mWeightEditText.getText().toString().trim());

        ContentValues values = new ContentValues();
        values.put(PetContract.PetEntry.COLUMN_PET_NAME, updatedName);
        values.put(PetContract.PetEntry.COLUMN_PET_BREED, updatedBreed);
        values.put(PetContract.PetEntry.COLUMN_PET_GENDER, updatedGender);
        values.put(PetContract.PetEntry.COLUMN_PET_WEIGHT, updatedWeight);

        int updatedRows = getContentResolver().update(uri, values, null, null);

        Toast.makeText(this, updatedRows + " Row Updated", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:

                if (singleUri == null){
                    insertPet();
                    finish();
                }
                else {
                    updatePet(singleUri);
                    finish();
                }

                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                Log.e("Tag heuer ", " delete clicked");
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                if (!mPetHasChanged){
                    // Navigate back to parent activity (CatalogActivity)
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                AlertDialog.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);

                    }
                };
                showUnsavedChangesDialog(listener);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = new String[]{
                PetContract.PetEntry._ID,
                PetContract.PetEntry.COLUMN_PET_NAME,
                PetContract.PetEntry.COLUMN_PET_BREED,
                PetContract.PetEntry.COLUMN_PET_GENDER,
                PetContract.PetEntry.COLUMN_PET_WEIGHT
        };

        String selection = PetContract.PetEntry._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(ContentUris.parseId(singleUri))};
        return new CursorLoader(this, singleUri, projection, selection, selectionArgs, null);

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        updateInput(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        clearInput();
    }

    public void updateInput(Cursor cursor){
        cursor.moveToNext();
        String name = cursor.getString(cursor.getColumnIndexOrThrow(PetContract.PetEntry.COLUMN_PET_NAME));
        String breed = cursor.getString(cursor.getColumnIndexOrThrow(PetContract.PetEntry.COLUMN_PET_BREED));
        int gender = cursor.getInt(cursor.getColumnIndexOrThrow(PetContract.PetEntry.COLUMN_PET_GENDER));
        int weight = cursor.getInt(cursor.getColumnIndexOrThrow(PetContract.PetEntry.COLUMN_PET_WEIGHT));

        mNameEditText.setText(name);
        mBreedEditText.setText(breed);
        mWeightEditText.setText(String.valueOf(weight));

        switch (gender){
            case PetContract.PetEntry.GENDER_UNKNOWN:
                mGenderSpinner.setSelection(0);
                break;
            case PetContract.PetEntry.GENDER_MALE:
                mGenderSpinner.setSelection(1);
                break;
            case PetContract.PetEntry.GENDER_FEMALE:
                mGenderSpinner.setSelection(2);
                break;
        }
    }

    public void clearInput(){
        mNameEditText.setText(null);
        mBreedEditText.setText(null);
        mWeightEditText.setText(null);
        mGenderSpinner.setSelection(0);
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener(){
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mPetHasChanged = true;
            return false;
        }
    };

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener){
        AlertDialog.Builder builder =  new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!mPetHasChanged){
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };

        showUnsavedChangesDialog(listener);
    }

    private void showDeleteConfirmationDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deletePet();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void deletePet(){

        int rowsDeleted = getContentResolver().delete(singleUri, null, null);

        if(rowsDeleted >=1){
            finish();
            Toast.makeText(this, getString(R.string.editor_detete_pet_successful), Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this,getString(R.string.editor_detete_pet_failed), Toast.LENGTH_SHORT).show();
        }
    }
}