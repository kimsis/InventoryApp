package com.example.mimaqm.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.content.CursorLoader;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.mimaqm.inventoryapp.data.BookContract.BookEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_BOOK_LOADER = 0;

    /** Content URI for the existing book (null if it's a new book) */
    private Uri mCurrentBookUri;

    //Edit texts for the Book information
    private EditText mProductNameEditText;

    private EditText mPriceEditText;

    private EditText mQuantityEditText;

    private EditText mSupplierNameEditText;

    private EditText mSupplierPhoneNumberEditText;

    /** Boolean flag that keeps track of whether the pet has been edited (true) or not (false) */
    private boolean mBookHasChanged = false;

    private ImageButton mSubtractQuantity;
    private ImageButton mAddQuantity;
    private Button mCallSupplier;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inventory_editor);

        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        // Find all relevant views that we will need to read user input from
        mProductNameEditText =  findViewById(R.id.product_name_edit_text);
        mPriceEditText =  findViewById(R.id.price_edit_text);
        mQuantityEditText =  findViewById(R.id.quantity_edit_text);
        mSupplierNameEditText = findViewById(R.id.supplier_name_edit_text);
        mSupplierPhoneNumberEditText = findViewById(R.id.supplier_phone_number_edit_text);
        mSubtractQuantity = findViewById(R.id.subtract_image_button);
        mAddQuantity = findViewById(R.id.add_image_button);
        mCallSupplier = findViewById(R.id.call_button);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mProductNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneNumberEditText.setOnTouchListener(mTouchListener);

        if (mCurrentBookUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_book));
            mCallSupplier.setVisibility(View.GONE);
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_book));

            mCallSupplier.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String supplierNumber = mSupplierPhoneNumberEditText.getText().toString();
                    //Toast.makeText(EditorActivity.this, "PHONE NUMBER: " + supplierNumber, Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + supplierNumber));
                    startActivity(intent);
                }
            });

            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }

        mSubtractQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String productQuantityString = mQuantityEditText.getText().toString().trim();
                int quantity =0;
                if (!TextUtils.isEmpty(productQuantityString))
                    quantity = Integer.valueOf(productQuantityString);
                if (quantity != 0) {
                    String quantityText = "" + (quantity - 1);
                    mQuantityEditText.setText(quantityText);
                }
                else
                    Toast.makeText(EditorActivity.this, "You cannot have less than 0 books", Toast.LENGTH_SHORT).show();
            }
        });

        mAddQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String productQuantityString = mQuantityEditText.getText().toString().trim();
                int quantity =0;
                if (!TextUtils.isEmpty(productQuantityString))
                    quantity = Integer.valueOf(productQuantityString);
                String quantityText = "" + (quantity + 1);
                mQuantityEditText.setText(quantityText);
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void saveBook(){
        String productNameString = mProductNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierPhoneNumberString = mSupplierPhoneNumberEditText.getText().toString().trim();

        // Check if this is supposed to be a new pet
        // and check if all the fields in the editor are blank
        if (mCurrentBookUri == null &&
                TextUtils.isEmpty(productNameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(supplierNameString) &&
                TextUtils.isEmpty(supplierPhoneNumberString)) {
            // Since no fields were modified, we can return early without creating a new book.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // If any of those required fields is empty, we must not save the new product
        if (!TextUtils.isEmpty(productNameString) && !TextUtils.isEmpty(priceString)) {

            if (TextUtils.isDigitsOnly(priceString) && Integer.parseInt(priceString) != 0) {
                // Create a ContentValues object where column names are the keys,
                // and book attributes from the editor are the values.
                ContentValues values = new ContentValues();
                values.put(BookEntry.COLUMN_PRODUCT_NAME, productNameString);
                values.put(BookEntry.COLUMN_PRICE, Integer.parseInt(priceString));
                int quantity = 0;
                if (!TextUtils.isEmpty(quantityString))
                    quantity = Integer.parseInt(quantityString);
                values.put(BookEntry.COLUMN_QUANTITY, quantity);
                values.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
                values.put(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhoneNumberString);

                // Determine if this is a new or existing book by checking if mCurrentPetUri is null or not
                if (mCurrentBookUri == null) {
                    // This is a NEW product, so insert a new product into the provider,
                    // returning the content URI for the new book.
                    Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);

                    // Show a toast message depending on whether or not the insertion was successful.
                    if (newUri == null) {
                        // If the new content URI is null, then there was an error with insertion.
                        Toast.makeText(this, getString(R.string.editor_insertion_failed),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Otherwise, the insertion was successful and we can display a toast.
                        Toast.makeText(this, getString(R.string.editor_insertion_successful),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Otherwise this is an EXISTING product, so update the book with content URI: mCurrentBookUri
                    // and pass in the new ContentValues. We pass null values for the selection and selectionArgs,
                    // as the mCurrentBookUri will specify only 1 product
                    int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);

                    // Show a toast message depending on whether or not the update was successful.
                    if (rowsAffected == 0) {
                        // If no rows were affected, then there was an error with the update.
                        Toast.makeText(this, getString(R.string.editor_update_failed),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Otherwise, the update was successful and we can display a toast.
                        Toast.makeText(this, getString(R.string.editor_update_successful),
                                Toast.LENGTH_SHORT).show();
                    }
                }
                // Exit activity
                finish();
            }
            else{
                Toast toastUndone = Toast.makeText(this, R.string.edit_valid_price, Toast.LENGTH_SHORT);
                toastUndone.show();
            }
        }
        else{
            Toast toastUndone = Toast.makeText(this, R.string.edit_required_fields, Toast.LENGTH_SHORT);
            toastUndone.show();
        }
    }

    private void deleteBook(){
        if (mCurrentBookUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the deletion.
                Toast.makeText(this, getString(R.string.editor_deletion_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the deletion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_deletion_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_book, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new book, hide the "Delete" menu item.
        if (mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete_a_book);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save book to database
                    saveBook();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete_a_book:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonOnClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonOnClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonOnClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonOnClickListener);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_PRODUCT_NAME,
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY,
                BookEntry.COLUMN_SUPPLIER_NAME,
                BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER};

        return new CursorLoader(this,   // Parent activity context
                mCurrentBookUri,         // Query the content URI for the current book
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (data.moveToFirst()) {
            // Find the columns of book attributes that we're interested in
            int productNameColumnIndex = data.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = data.getColumnIndex(BookEntry.COLUMN_PRICE);
            int quantityColumnIndex = data.getColumnIndex(BookEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = data.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneNumberColumnIndex = data.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            // Extract out the value from the Cursor for the given column index
            String productName = data.getString(productNameColumnIndex);
            String price = data.getString(priceColumnIndex);
            String quantity = data.getString(quantityColumnIndex);
            String supplierName = data.getString(supplierNameColumnIndex);
            String supplierPhoneNumber = data.getString(supplierPhoneNumberColumnIndex);

            // Update the views on the screen with the values from the database
            mProductNameEditText.setText(productName);
            mPriceEditText.setText(price);
            mQuantityEditText.setText(quantity);
            mSupplierNameEditText.setText(supplierName);
            mSupplierPhoneNumberEditText.setText(supplierPhoneNumber);

        }
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        mProductNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierPhoneNumberEditText.setText("");
    }

}
