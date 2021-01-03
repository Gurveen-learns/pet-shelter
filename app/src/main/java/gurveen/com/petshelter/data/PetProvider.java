package gurveen.com.petshelter.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import gurveen.com.petshelter.data.PetContract.PetEntry;

public class PetProvider extends ContentProvider {


    public static final String LOG_TAG = PetProvider.class.getSimpleName();
    private static final int PETS_WHOLE_TABLE = 100;
    /*Database Helper Object*/
    private static final int PETS_SINGLE_ROW = 101;
    private static final UriMatcher myUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        myUriMatcher.addURI(PetEntry.CONTENT_AUTHORITY,
                PetEntry.PATH_PETS,PETS_WHOLE_TABLE);
        myUriMatcher.addURI(PetEntry.CONTENT_AUTHORITY,
                PetEntry.PATH_PETS + "/#", PETS_SINGLE_ROW);
    }

    private PetDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new PetDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        Cursor cursor;

        final int code = myUriMatcher.match(uri);


        switch (code) {
            case PETS_WHOLE_TABLE:
                cursor = database.query(PetEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;

            case PETS_SINGLE_ROW:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = dbHelper.getReadableDatabase().query(PetEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;


            default:
                return null;
        }

        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;


    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int code = myUriMatcher.match(uri);

        switch (code) {
            case PETS_WHOLE_TABLE:
                return insertPet(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }


    }

    private Uri insertPet(Uri uri, ContentValues contentValues) {
        String name = contentValues.getAsString(PetEntry.COLUMN_PET_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Pet requires a name");
        }

        Integer gender = contentValues.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        if (gender == null || !PetEntry.isValidGender(gender)) {
            throw new IllegalArgumentException("Pet need a valid gender ");
        }

        Integer weight = contentValues.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
        if (weight != 0 && weight < 0) {
            throw new IllegalArgumentException("Pet need a valid weight ");
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long id = db.insert(PetContract.PetEntry.TABLE_NAME, null, contentValues);
        //id- ID of the row in which data is inserted
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
        }

        getContext().getContentResolver().notifyChange(uri,null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int code = myUriMatcher.match(uri);

        switch (code){
            case PETS_WHOLE_TABLE:
                return db.delete(PetEntry.TABLE_NAME,null,null);
            case PETS_SINGLE_ROW:
                selection = PetEntry._ID  + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                int rowsDeleted = db.delete(PetEntry.TABLE_NAME,selection,selectionArgs);
                if (rowsDeleted!=0){
                    getContext().getContentResolver().notifyChange(uri,null);
                }
                return rowsDeleted;
            default:throw new IllegalArgumentException("Delete functio is not supported for "+ uri);

        }

    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int code = myUriMatcher.match(uri);
        switch (code) {
            case PETS_WHOLE_TABLE:
                return updatePet(uri, values, null, null);

            case PETS_SINGLE_ROW:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }


    }

    private int updatePet(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        if (contentValues.containsKey(PetEntry.COLUMN_PET_NAME)) {
            String name = contentValues.getAsString(PetEntry.COLUMN_PET_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        if (contentValues.containsKey(PetEntry.COLUMN_PET_GENDER)) {
            Integer gender = contentValues.getAsInteger(PetEntry.COLUMN_PET_GENDER);
            if (gender == null || !PetEntry.isValidGender(gender)) {
                throw new IllegalArgumentException("Pet need a valid gender ");
            }
        }

        if (contentValues.containsKey(PetEntry.COLUMN_PET_WEIGHT)) {
            Integer weight = contentValues.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
            if (weight != 0 && weight < 0) {
                throw new IllegalArgumentException("Pet need a valid weight ");
            }
        }
        //If there are no values to update the don't  try to update
        if (contentValues.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        int rowsUpdated = database.update(PetEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        if (rowsUpdated !=0){
            getContext().getContentResolver().notifyChange(uri,null);
        }

        return rowsUpdated;
    }
}
