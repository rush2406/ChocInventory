package com.example.rusha.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * Created by rusha on 5/31/2017.
 */

public class InventoryProvider extends ContentProvider {

    private InventoryDbHelper mDbHelper;
    private static final int INVENT = 100;
    private static final int INVENT_ID = 101;
    private long id;
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH, INVENT);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH + "/#", INVENT_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;

    }

    private Uri insertInvent(Uri uri, ContentValues contentValues) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        String name = contentValues.getAsString(InventoryContract.InventoryEntry.COLUMN_NAME);
        Integer quan = contentValues.getAsInteger(InventoryContract.InventoryEntry.COLUMN_QUANTITY);
         Double price = contentValues.getAsDouble(InventoryContract.InventoryEntry.COLUMN_PRICE);


        if (name.isEmpty() && name == null)
            throw new IllegalArgumentException("Name required");

        if (quan < 0 && quan != null)
            throw new IllegalArgumentException("Quantity cannot be <=0");

        if (price < 0.0 && price != null)
            throw new IllegalArgumentException("Price cannot be <=0");

        id = database.insert(InventoryContract.InventoryEntry.TABLE_NAME, null, contentValues);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENT:
                return insertInvent(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion not supported for " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor c;
        switch (match) {
            case INVENT:

                c = db.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case INVENT_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                c = db.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rows;
        switch (match) {
            case INVENT:
                rows = db.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INVENT_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rows = db.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Delete failed");
        }
        if (rows != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rows;
    }

    private int updateInvent(Uri uri, ContentValues contentValues, String selection, String[] strings) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();


        if (contentValues.containsKey(InventoryContract.InventoryEntry.COLUMN_NAME)) {
            String name = contentValues.getAsString(InventoryContract.InventoryEntry.COLUMN_NAME);
            if (name.isEmpty() && name == null)
                throw new IllegalArgumentException("Name required");
        }

        if (contentValues.containsKey(InventoryContract.InventoryEntry.COLUMN_QUANTITY)) {
            Integer quan = contentValues.getAsInteger(InventoryContract.InventoryEntry.COLUMN_QUANTITY);
            if (quan < 0 && quan != null)
                throw new IllegalArgumentException("Quantity cannot be <=0");
        }

        if (contentValues.containsKey(InventoryContract.InventoryEntry.COLUMN_PRICE)) {
            Double price = contentValues.getAsDouble(InventoryContract.InventoryEntry.COLUMN_PRICE);
            if (price < 0.0 && price != null)
                throw new IllegalArgumentException("Price cannot be <=0");
        }

        if (contentValues.size() == 0) {
            return 0;
        }
        int rows = database.update(InventoryContract.InventoryEntry.TABLE_NAME, contentValues, selection, strings);
        if (rows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rows;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] strings) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENT:
                return updateInvent(uri, contentValues, selection, strings);
            case INVENT_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                strings = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateInvent(uri, contentValues, selection, strings);
            default:
                throw new IllegalArgumentException("Update Error");
        }
    }


    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENT:
                return InventoryContract.CONTENT_LIST_TYPE;
            case INVENT_ID:
                return InventoryContract.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
