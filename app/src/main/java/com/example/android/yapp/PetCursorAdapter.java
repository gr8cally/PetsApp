package com.example.android.yapp;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.yapp.data.PetContract;

/**
 * Created by user on 8/23/2019.
 */

public class PetCursorAdapter extends CursorAdapter {
    public PetCursorAdapter(Context context, Cursor cursor){
        super(context, cursor , 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView summaryTextView = (TextView) view.findViewById(R.id.summary);

        String nameString = cursor.getString(cursor.getColumnIndexOrThrow(PetContract.PetEntry.COLUMN_PET_NAME));
        String prelimBreed = cursor.getString(cursor.getColumnIndexOrThrow(PetContract.PetEntry.COLUMN_PET_BREED));
        String summaryString = "";
        if (TextUtils.isEmpty(prelimBreed)){
            summaryString = "Unknown breed";
        }
        else{
            summaryString = prelimBreed;
        }

        nameTextView.setText(nameString);
        summaryTextView.setText(summaryString);
    }
}
