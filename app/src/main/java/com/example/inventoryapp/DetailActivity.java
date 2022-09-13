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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import data.ItemContract.ItemEntry;

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener{

    private static final int ITEM_LOADER = 0;
    private int itemQuantity, quantity;
    private String itemName;
    private int itemId;
    private Uri mCurrentItemUri;
    private ImageView mImageView;
    private TextView mNameTextView;
    private TextView mPriceTextView;
    private TextView mQuantityTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        mImageView = findViewById(R.id.detail_item_imageView);
        mNameTextView = findViewById(R.id.detail_name_textView);
        mPriceTextView = findViewById(R.id.detail_price_textView);
        mQuantityTextView = findViewById(R.id.detail_quantity_textView);
        Button mDeleteItemButton = findViewById(R.id.delete_item_button);
        Button mOrderItemButton = findViewById(R.id.order_item_button);
        Button mDecreaseButton = findViewById(R.id.decrease_button);
        Button mIncreaseButton = findViewById(R.id.increase_button);

        mDeleteItemButton.setOnClickListener(this);
        mOrderItemButton.setOnClickListener(this);
        mIncreaseButton.setOnClickListener(this);
        mDecreaseButton.setOnClickListener(this);

        getLoaderManager().initLoader(ITEM_LOADER, null, this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.decrease_button:
                if(quantity > 1){
                    quantity--;
                }
                else
                    Toast.makeText(this, "Minimum item quantity required", Toast.LENGTH_SHORT).show();

                mQuantityTextView.setText(Integer.toString(quantity));
                break;

            case R.id.increase_button:
                quantity++;
                mQuantityTextView.setText(Integer.toString(quantity));
                break;

            case R.id.order_item_button:
                    itemQuantity = itemQuantity + quantity;
                    ContentValues values = new ContentValues();
                    values.put(ItemEntry._ID, itemId);
                    values.put(ItemEntry.COLUMN_ITEM_QUANTITY, itemQuantity);
                    String selection = ItemEntry._ID + "=?";
                    String[] selectionArgs = new String[]{String.valueOf(itemId)};
                    getContentResolver().update(ItemEntry.CONTENT_URI, values, selection, selectionArgs);

                    Intent selectorIntent = new Intent(Intent.ACTION_SENDTO);
                    selectorIntent.setData(Uri.parse("mailto:"));

                    final Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"inventorystore@gmail.com"});
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Order Placement");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "Product Name: " + itemName + "\n Quantity:            " + quantity);
                    emailIntent.setSelector(selectorIntent);

                    startActivity(Intent.createChooser(emailIntent, "Send email..."));

                break;

            case R.id.delete_item_button:
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String selection = ItemEntry._ID + "=?";
                                String[] selectionArgs = new String[]{String.valueOf(itemId)};
                                int rowsAffected = getContentResolver().delete(ItemEntry.CONTENT_URI, selection, selectionArgs);
                                Toast.makeText(getBaseContext(), "Item deleted successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        };

                showDeleteItemDialog(discardButtonClickListener);

        }
    }

    private void showDeleteItemDialog(
            DialogInterface.OnClickListener deleteButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_to_delete);
        builder.setPositiveButton(R.string.delete, deleteButtonClickListener);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_IMAGE,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_QUANTITY };

        return new CursorLoader(this,
                mCurrentItemUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int idColumnIndex = cursor.getColumnIndex(ItemEntry._ID);
            int imageColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_IMAGE);
            int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);

            itemId = cursor.getInt(idColumnIndex);
            byte[] bArray = cursor.getBlob(imageColumnIndex);
            itemName = cursor.getString(nameColumnIndex);
            int itemPrice = cursor.getInt(priceColumnIndex);
            itemQuantity = cursor.getInt(quantityColumnIndex);
            quantity = 1;

            Bitmap bitmap = getBitmapFromBytes(bArray);
            mImageView.setImageBitmap(bitmap);


            mNameTextView.setText(itemName);
            mPriceTextView.setText(Integer.toString(itemPrice));
            mQuantityTextView.setText(Integer.toString(quantity));
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    public static Bitmap getBitmapFromBytes(byte[] bytes) {
        if (bytes != null) {
            return BitmapFactory.decodeByteArray(bytes, 0 ,bytes.length);
        }
        return null;
    }

}