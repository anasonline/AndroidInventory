package com.example.android.androidinventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    ImageView mProductImage;

    Button mSaleButton;
    Button mAddButton;
    Button mDeleteButton;
    Button mOrderButton;
    EditText mSaleQuantity;
    EditText mAddQuantity;
    EditText mOrderQuantity;

    int productQuantity;
    String productName;
    String productImage;

    /**
     * Content URI for the product to be displayed
     */
    private Uri mCurrentProductUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mSaleButton = (Button) findViewById(R.id.saleBtn);
        mAddButton = (Button) findViewById(R.id.addBtn);
        mSaleQuantity = (EditText) findViewById(R.id.saleQuantity);
        mAddQuantity = (EditText) findViewById(R.id.addQuantity);
        mDeleteButton = (Button) findViewById(R.id.deleteBtn);
        mOrderButton = (Button) findViewById(R.id.orderBtn);
        mOrderQuantity = (EditText) findViewById(R.id.orderQuantity);
        mProductImage = (ImageView) findViewById(R.id.productImage);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabEdit);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailActivity.this, EditorActivity.class);

                // Extract product id from the Uri
                long contentId = ContentUris.parseId(mCurrentProductUri);

                // Form the content URI that represents the specific product to be edited,
                // by appending the "id" (passed as input to this method) onto the
                // {@link ProductEntry#CONTENT_URI}.
                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, contentId);

                // Set the URI on the data field of the intent
                intent.setData(currentProductUri);

                startActivity(intent);
            }
        });

        // Get the intent that was used to launch this activity,
        // in order to extract the URI data from it
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // Initialize a loader to read the product data from the database
        // and display the current values
        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);

        // Set an OnClickListener on the Sale button
        mSaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sale();
            }
        });

        // Set an OnClickListener on the Add button
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add();
            }
        });

        // Set an OnClickListener on the Delete button
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
            }
        });

        // Set an OnClickListener on the Order button
        mOrderButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                order();
            }
        });
    }

    // A helper method to perform a product sale
    public void sale() {

        // Get the quantity from the EditText
        String quantityString = mSaleQuantity.getText().toString().trim();

        // If the EditText is blank, warn the user and don't proceed
        if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, "Please enter a value!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Quantity to be sold
        int quantityToSell = Integer.parseInt(quantityString);

        // If entered quantity is larger than the product quantity we have in stock, show a message
        if (quantityToSell > productQuantity) {
            Toast.makeText(this, "You don't have enough quantity for this sale",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Update the product quantity in the database
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, productQuantity - quantityToSell);

        int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

        // Show a toast message depending on whether or not the update was successful.
        if (rowsAffected == 0) {
            // If no rows were affected, then there was an error with the update.
            Toast.makeText(this, "Error! Nothing updated",
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the update was successful and we can display a toast.
            Toast.makeText(this, "Updated successfully",
                    Toast.LENGTH_SHORT).show();
        }

    }

    // A helper method to add products to stock
    public void add() {

        // Get the quantity from the EditText
        String quantityString = mAddQuantity.getText().toString().trim();

        // If the EditText is blank, warn the user and don't proceed
        if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, "Please enter a value!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Quantity to be added
        int quantityToAdd = Integer.parseInt(quantityString);

        // Update the product quantity in the database
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, productQuantity + quantityToAdd);

        int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

        // Show a toast message depending on whether or not the update was successful.
        if (rowsAffected == 0) {
            // If no rows were affected, then there was an error with the update.
            Toast.makeText(this, "Error! Nothing updated",
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the update was successful and we can display a toast.
            Toast.makeText(this, "Updated successfully",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    // This helper method sends an intent to email apps to order more units of a product
    public void order() {

        // Get the quantity from the EditText
        String quantityString = mOrderQuantity.getText().toString().trim();

        // If the EditText is blank, warn the user and don't proceed
        if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, "Please enter a value!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the string for the email's body
        Resources res = getResources();
        String email_body = res.getString(R.string.order_email_body, quantityString, productName);

        String addresses[] = {"email@supplier.com"};
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/html");
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, "New order");
        intent.putExtra(Intent.EXTRA_TEXT, email_body);
        startActivity(Intent.createChooser(intent, "Send Email"));
    }

    /**
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_IMAGE};

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
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);


            // Extract out the value from the Cursor for the given column index
            productName = cursor.getString(nameColumnIndex);
            float productPrice = cursor.getFloat(priceColumnIndex);
            productQuantity = cursor.getInt(quantityColumnIndex);
            productImage = cursor.getString(imageColumnIndex);

            // Find the views to be updated with the attributes for the current product
            mNameTextView = (TextView) findViewById(R.id.productName);
            mPriceTextView = (TextView) findViewById(R.id.priceValue);
            mQuantityTextView = (TextView) findViewById(R.id.quantityValue);

            // Update the TextViews with the attributes for the current product
            mNameTextView.setText(productName);
            mPriceTextView.setText(String.valueOf(productPrice));
            mQuantityTextView.setText(String.valueOf(productQuantity));
            mProductImage.setImageURI(Uri.parse(productImage));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameTextView.setText("");
        mPriceTextView.setText("");
        mQuantityTextView.setText("");
    }
}