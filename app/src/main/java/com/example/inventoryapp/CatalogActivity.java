package com.example.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;

import data.ItemContract.ItemEntry;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ITEM_LOADER = 0;

    ItemCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
            startActivity(intent);
        });

        ListView itemListView = findViewById(R.id.list);
        itemListView.setClickable(true);

        View emptyView = findViewById(R.id.empty_view);
        itemListView.setEmptyView(emptyView);
        itemListView.setClickable(true);

        mCursorAdapter = new ItemCursorAdapter(this, null);
        itemListView.setAdapter(mCursorAdapter);

        itemListView.setOnItemClickListener((adapterView, view, position, id) -> {
            Intent intent = new Intent(CatalogActivity.this, DetailActivity.class);

            Uri currentItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);

            intent.setData(currentItemUri);

            startActivity(intent);
        });

        getLoaderManager().initLoader(ITEM_LOADER, null, this);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public static byte[] getBytesFromBitmap(Bitmap bitmap) {
        if (bitmap!=null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
            return stream.toByteArray();
        }
        return null;
    }

    private void insertItem(){
        ContentValues values = new ContentValues();
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.lenovo_laptop);
        byte[] bArray = getBytesFromBitmap(bitmap);
        values.put(ItemEntry.COLUMN_ITEM_IMAGE, bArray);
        values.put(ItemEntry.COLUMN_ITEM_NAME, "Lenovo Laptop");
        values.put(ItemEntry.COLUMN_ITEM_PRICE, 60000);
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, 5);

        Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        // Respond to a click on the "Insert dummy data" menu option
        if (item.getItemId() == R.id.action_insert_dummy_data) {
            insertItem();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_IMAGE,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_QUANTITY,};

        return new CursorLoader(this,
                ItemEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished( Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset( Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    public void clickOnSale(long id, int quantity) {
        ContentValues values = new ContentValues();
        values.put(ItemEntry._ID, id);
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);
        String selection = ItemEntry._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(id)};

        if (quantity >= 0) {
            int rowsAffected = getContentResolver().update(ItemEntry.CONTENT_URI, values, selection, selectionArgs);
        }
        else{
            Toast.makeText(this, "No more items to sell", Toast.LENGTH_SHORT).show();
        }
    }

}
