package com.example.mimaqm.inventoryapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.mimaqm.inventoryapp.BookContract.BookEntry;

public class InventoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabase();
    }

    private void displayDatabase() {

        BookDbHelper mDbHelper = new BookDbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        TextView databaseDisplayView = findViewById(R.id.database_display_view);

        Cursor cursor = queryData(db);

        try {

            databaseDisplayView.setText("The books table contains " + cursor.getCount() + " books.\n\n");
            databaseDisplayView.append(BookEntry._ID + " - " +
                    BookEntry.COLUMN_PRODUCT_NAME + " - " +
                    BookEntry.COLUMN_PRICE + " - " +
                    BookEntry.COLUMN_QUANTITY + " - " +
                    BookEntry.COLUMN_SUPPLIER_NAME + " - " +
                    BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER + "\n");

            // Get the index of every column
            int idColumnIndex = cursor.getColumnIndex(BookEntry._ID);
            int productNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneNumberColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            // Going through every single row of the table
            while (cursor.moveToNext()) {
                // Getting the values from the current row, using the indexes that we previously got
                int currentID = cursor.getInt(idColumnIndex);
                String currentProductName = cursor.getString(productNameColumnIndex);
                int currentPrice = cursor.getInt(priceColumnIndex);
                int currentQuantity = cursor.getInt(quantityColumnIndex);
                String currentSupplierName = cursor.getString(supplierNameColumnIndex);
                String currentSupplierPhoneNumber = cursor.getString(supplierPhoneNumberColumnIndex);

                // Displaying the values for the current row
                databaseDisplayView.append(("\n" + currentID + " - " +
                        currentProductName + " - " +
                        currentPrice + " - " +
                        currentQuantity + " - " +
                        currentSupplierName + " - " +
                        currentSupplierPhoneNumber));
            }
        } finally {
            // Closing the cursor to prevent memory leaks
            cursor.close();
        }
    }

    private void insertData() {
        // Gets a writable database
        BookDbHelper mDbHelper = new BookDbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Creating a ContentValues object to store the data for the row, that is to be inserted
        ContentValues values = new ContentValues();
        values.put("[" + BookEntry.COLUMN_PRODUCT_NAME + "]", "The Spook's Apprentice");
        values.put(BookEntry.COLUMN_PRICE, 15);
        values.put(BookEntry.COLUMN_QUANTITY, 20);
        values.put("[" + BookEntry.COLUMN_SUPPLIER_NAME + "]", "Peter Parker");
        values.put("[" + BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER + "]", "+3592212352");

        // Inserts the row into the database, returning the rowId, which could be used for logging
        long newRowId = db.insert(BookEntry.TABLE_NAME, null, values);
    }

    private Cursor queryData(SQLiteDatabase db) {
        // Creating the projection that will be used for the cursor, querying the database
        String[] projection = {
                BookEntry._ID,
                "[" + BookEntry.COLUMN_PRODUCT_NAME + "]",
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY,
                "[" + BookEntry.COLUMN_SUPPLIER_NAME + "]",
                "[" + BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER + "]"};

        return db.query(BookEntry.TABLE_NAME, projection,null,null,null,null,null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflates the menu options with the custom menu, stored in the res/menu folder
        getMenuInflater().inflate(R.menu.menu_inventory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Responds to the "Insert fake data" menu option
            case R.id.action_insert_fake_data:
                insertData();
                displayDatabase();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}