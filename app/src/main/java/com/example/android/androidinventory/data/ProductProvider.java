package com.example.android.androidinventory.data;

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

import com.example.android.androidinventory.data.ProductContract.ProductEntry;

/**
 * Created by anas on 20.05.17.
 */

public class ProductProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = ProductProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the products table
     */
    private static final int PRODUCTS = 100;

    /**
     * URI matcher code for the content URI for a single product in the pets table
     */
    private static final int PRODUCT_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.androidinventory/products" will map to the
        // integer code {@link #PRODUCTS}. This URI is used to provide access to MULTIPLE rows
        // of the products table.
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS);

        // The content URI of the form "content://com.example.android.androidinventory/products/#" will map to the
        // integer code {@link #PRODUCT_ID}. This URI is used to provide access to ONE single row
        // of the products table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.androidinventory/products/3" matches, but
        // "content://com.example.android.androidinventory/products" doesn't match.
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    /**
     * Database helper object
     */
    private ProductDbHelper mDbHelper;


    @Override
    public boolean onCreate() {
        mDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a product into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertProduct(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }

        // Check that price is valid
        Integer price = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_PRICE);
        if (price < 0) {
            throw new IllegalArgumentException("Price is less than 0");
        }

        // Check that quantity is valid
        Integer quantity = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity is less than 0");
        }


        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new product with the given values
        long id = database.insert(ProductEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
