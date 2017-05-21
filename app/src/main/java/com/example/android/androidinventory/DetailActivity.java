package com.example.android.androidinventory;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.android.androidinventory.data.ProductContract.ProductEntry;

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the product data loader
     */
    private static final int EXISTING_PRODUCT_LOADER = 0;
    TextView mNameTextView;
    TextView mPriceTextView;
    TextView mQuantityTextView;
    /**
     * Content URI for the product to be displayed
     */
    private Uri mCurrentProductUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Get the intent that was used to launch this activity,
        // in order to extract the URI data from it
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // Initialize a loader to read the product data from the database
        // and display the current values
        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);

            // Extract out the value from the Cursor for the given column index
            String productName = cursor.getString(nameColumnIndex);
            float productPrice = cursor.getFloat(priceColumnIndex);
            int productQuantity = cursor.getInt(quantityColumnIndex);

            // Find the views to be updated with the attributes for the current product
            mNameTextView = (TextView) findViewById(R.id.productName);
            mPriceTextView = (TextView) findViewById(R.id.priceValue);
            mQuantityTextView = (TextView) findViewById(R.id.quantityValue);

            // Update the TextViews with the attributes for the current product
            mNameTextView.setText(productName);
            mPriceTextView.setText(String.valueOf(productPrice));
            mQuantityTextView.setText(String.valueOf(productQuantity));

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameTextView.setText("");
        mPriceTextView.setText("");
        mQuantityTextView.setText("");
    }
}
