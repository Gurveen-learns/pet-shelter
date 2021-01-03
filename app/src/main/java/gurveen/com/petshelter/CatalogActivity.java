package gurveen.com.petshelter;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import gurveen.com.petshelter.data.PetContract;


public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PET_LOADER_ID = 0;

    private PetCursorAdapter mPetAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CatalogActivity.this, EditorActivity.class));
            }
        });

        ListView petListView = findViewById(R.id.list_view);
        petListView.setEmptyView(findViewById(R.id.empty_view));

        mPetAdapter = new PetCursorAdapter(this,null);
        petListView.setAdapter(mPetAdapter);

        //Let's Kick-off the Loader

        getSupportLoaderManager().initLoader(PET_LOADER_ID,null,this);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }



    private void insertDummyPet() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(PetContract.PetEntry.COLUMN_PET_NAME, "Toto");
        contentValues.put(PetContract.PetEntry.COLUMN_PET_BREED, "Terrier");
        contentValues.put(PetContract.PetEntry.COLUMN_PET_GENDER, PetContract.PetEntry.GENDER_MALE);
        contentValues.put(PetContract.PetEntry.COLUMN_PET_WEIGHT, 7);

        Uri newUri = getContentResolver().insert(PetContract.PetEntry.CONTENT_URI, contentValues);


    }

    private void deleteAllData() {
        getContentResolver().delete(PetContract.PetEntry.CONTENT_URI, null, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.catelog_activity_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertDummyPet();


                return true;

            case R.id.action_delete_all_entries:
                deleteAllData();

                return true;


        }
        return super.onOptionsItemSelected(item);
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {

        String[] projections = {
                PetContract.PetEntry._ID,
                PetContract.PetEntry.COLUMN_PET_NAME,
                PetContract.PetEntry.COLUMN_PET_BREED};


        return new CursorLoader(this,
                PetContract.PetEntry.CONTENT_URI,
                projections,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
                mPetAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
              mPetAdapter.swapCursor(null);
    }
}
