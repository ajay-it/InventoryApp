package com.example.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import data.ItemContract.ItemEntry;

public class ItemCursorAdapter extends CursorAdapter {

    private final CatalogActivity catalogActivity;

    public ItemCursorAdapter(CatalogActivity context, Cursor c) {
        super(context, c, 0);
        this.catalogActivity = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView itemImageView = view.findViewById(R.id.item_imageView);
        TextView nameTextView = view.findViewById(R.id.name_textView);
        TextView priceTextView = view.findViewById(R.id.price_textView);
        TextView quantityTextView = view.findViewById(R.id.quantity_textView);

        int idColumnIndex = cursor.getColumnIndex(ItemEntry._ID);
        int imageColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_IMAGE);
        int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);

        final int itemId = cursor.getInt(idColumnIndex);
        byte[] bArray = cursor.getBlob(imageColumnIndex);
        String itemName = cursor.getString(nameColumnIndex);
        int itemPrice = cursor.getInt(priceColumnIndex);
        int itemQuantity = cursor.getInt(quantityColumnIndex);

        Bitmap bitmap = getBitmapFromBytes(bArray);
        itemImageView.setImageBitmap(bitmap);

        nameTextView.setText(itemName);
        priceTextView.setText(Integer.toString(itemPrice));
        quantityTextView.setText(Integer.toString(itemQuantity));

        Button sellButton = view.findViewById(R.id.sell_button);
        sellButton.setOnClickListener(new View.OnClickListener() {
            int updatedQuantity = itemQuantity - 1;

            @Override
            public void onClick(View v) {
                catalogActivity.clickOnSale(itemId, updatedQuantity);
            }
        });

    }

    public static Bitmap getBitmapFromBytes(byte[] bytes) {
        if (bytes != null) {
            return BitmapFactory.decodeByteArray(bytes, 0 ,bytes.length);
        }
        return null;
    }
    
}
