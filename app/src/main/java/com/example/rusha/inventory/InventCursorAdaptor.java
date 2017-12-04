package com.example.rusha.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.rusha.inventory.data.InventoryContract;


/**
 * Created by rusha on 5/31/2017.
 */

public class InventCursorAdaptor extends CursorAdapter {

    private static final String LOG_TAG = InventCursorAdaptor.class.getSimpleName();

    public InventCursorAdaptor(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {

        TextView nameText = (TextView) view.findViewById(R.id.name);
        final TextView quanText = (TextView) view.findViewById(R.id.quan);
        TextView priceText = (TextView) view.findViewById(R.id.price);
        Button decreaseButton = (Button) view.findViewById(R.id.decreaseone);
        final String idColumn = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry._ID));
        final Uri uri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, Long.parseLong(idColumn));
        final int quantity = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_QUANTITY));
        decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity1 = quantity;
                if (quantity1 != 0)
                    quantity1--;
                Log.v(LOG_TAG, "ROW ID = " + idColumn + " " + quantity1);
                ContentValues value = new ContentValues();
                value.put(InventoryContract.InventoryEntry.COLUMN_QUANTITY, String.valueOf(quantity1));
                view.getContext().getContentResolver().update(uri, value, null, null);
            }
        });
        String namec = cursor.getString(cursor.getColumnIndex("name"));
        Double price = cursor.getDouble(cursor.getColumnIndex("price"));
        nameText.setText(namec);
        int quantityFinal = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_QUANTITY));
        if (quantityFinal == 0)
            quanText.setText("Out of Stock");
        else if (quantityFinal == 1)
            quanText.setText(String.valueOf(quantityFinal) + " pack available");
        else
            quanText.setText(String.valueOf(quantityFinal) + " packs available");

        if (price == 0)
            priceText.setText("Price not set");
        else
            priceText.setText("$ " + String.format("%.2f", price));
    }
}
