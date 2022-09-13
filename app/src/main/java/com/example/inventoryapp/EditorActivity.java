package com.example.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;

import data.ItemContract.ItemEntry;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    ItemCursorAdapter mCursorAdapter;

    private static final int RESULT_LOAD_IMAGE = 1;

    Bitmap bitmap;
    private ImageView mImageView;
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;

    private boolean mItemHasChanged = false;

    private View.OnTouchListener mTouchListener = (view, motionEvent) -> {
        mItemHasChanged = true;
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mImageView = findViewById(R.id.editor_item_imageView);
        mNameEditText = findViewById(R.id.editor_name_textView);
        mPriceEditText = findViewById(R.id.editor_price_textView);
        mQuantityEditText = findViewById(R.id.editor_quantity_textView);
        Button mAddImageButton = findViewById(R.id.add_image_button);
        Button mCancelButton = findViewById(R.id.cancel_button);
        Button mAddButton = findViewById(R.id.add_button);

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mAddImageButton.setOnTouchListener(mTouchListener);

        mAddImageButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        mAddButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_image_button:
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
                break;

            case R.id.cancel_button:
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                } else {
                    DialogInterface.OnClickListener discardButtonClickListener =
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                                }
                            };

                    showUnsavedChangesDialog(discardButtonClickListener);
                }
                break;

            case R.id.add_button:
                int price = 0, quantity = 0;
                String name = mNameEditText.getText().toString().trim();
                int length = name.length();
                try {
                    price = Integer.parseInt(mPriceEditText.getText().toString());
                } catch (NumberFormatException nfe) {
                }

                try {
                    quantity = Integer.parseInt(mQuantityEditText.getText().toString());
                } catch (NumberFormatException nfe) {
                }

                if (length < 2){
                    Toast.makeText(this, "Please enter a valid name", Toast.LENGTH_SHORT).show();
                }else if(quantity == 0) {
                    Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show();
                }else if(price == 0) {
                    Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show();
                }else if(bitmap == null) {
                    Toast.makeText(this, "Please add an image", Toast.LENGTH_SHORT).show();
                }else {
                    saveItem();
                    finish();
                }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            if (!mItemHasChanged) {
                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                return true;
            }
            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            NavUtils.navigateUpFromSameTask(EditorActivity.this);
                        }
                    };

            showUnsavedChangesDialog(discardButtonClickListener);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }


        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                bitmap = BitmapFactory.decodeStream(inputStream);
                mImageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveItem() {
        int price = 0, quantity = 0;
        String name = mNameEditText.getText().toString().trim();
        try {
            price = Integer.parseInt(mPriceEditText.getText().toString());
        } catch (NumberFormatException nfe) {
        }

        try {
                quantity = Integer.parseInt(mQuantityEditText.getText().toString());
        } catch (NumberFormatException nfe) {
        }


            ContentValues values = new ContentValues();

            byte[] bArray = CatalogActivity.getBytesFromBitmap(bitmap);

            values.put(ItemEntry.COLUMN_ITEM_IMAGE, bArray);
            values.put(ItemEntry.COLUMN_ITEM_NAME, name);
            values.put(ItemEntry.COLUMN_ITEM_PRICE, price);
            values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);

            Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                        Toast.LENGTH_SHORT).show();

        }
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
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


}
