package com.example.android.yapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by user on 8/5/2019.
 */

public final class PetContract {

    private PetContract(){}

    public static final String PATH_PETS = "pets";

    public static final class PetEntry implements BaseColumns {

        public static final Uri baseUri = Uri.parse("content://com.example.android.yapp");

        public static final String CONTENT_AUTHORITY = "com.example.android.yapp";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(baseUri, PATH_PETS);

//        The MIME type of the {@link #CONTENT_URI} for a list of pets
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PETS ;

//        The MIME type of the {@link #CONTENT_URI} for a list of pets
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PETS ;;

        public static final String TABLE_NAME = "pets";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PET_NAME = "name";
        public static final String COLUMN_PET_BREED = "breed";
        public static final String COLUMN_PET_GENDER = "gender";
        public static final String COLUMN_PET_WEIGHT = "weight";
        public static final String AUTHORITY = "com.example.android.yapp";

        public static final int GENDER_MALE= 1;
        public static final int GENDER_FEMALE= 2;
        public static final int GENDER_UNKNOWN=0;

        public static boolean isValidGender(int gender){
            if(gender == GENDER_FEMALE || gender == GENDER_MALE || gender == GENDER_UNKNOWN ){
                return true;
            }
            return false;
        }
    }
}
