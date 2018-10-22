package com.example.mimaqm.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mimaqm.inventoryapp.data.BookContract.BookEntry;

public class BookCursorAdapter extends CursorAdapter {


    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // Set the cursor as a tag on the view, to be used for the sell button
        int id = cursor.getInt(cursor.getColumnIndex(BookEntry._ID));

        // Find the views, in which to store the Book details
        TextView productNameTextView = view.findViewById(R.id.product_name_text_view);
        final TextView QuantityTextView = view.findViewById(R.id.product_quantity_text_view);
        TextView PriceTextView = view.findViewById(R.id.product_price_text_view);
        Button sellButton = view.findViewById(R.id.sell_button_view);
        LinearLayout productInfo = view.findViewById(R.id.product_info);

        // Get the indexes of the attributes from the cursor
        final int productIdColumnIndex = cursor.getColumnIndex(BookEntry._ID);
        final int productNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
        final int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
        final int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);
        final int supplierNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
        final int supplierPhoneNumberColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

        // Read the book attributes from the cursor
        final long productId = cursor.getLong(productIdColumnIndex);
        final String productName = cursor.getString(productNameColumnIndex);
        final String productPrice = cursor.getString(priceColumnIndex);
        final String productQuantity = cursor.getString(quantityColumnIndex);
        String supplierNameString = cursor.getString(supplierNameColumnIndex);
        String supplierPhoneNumberString = cursor.getString(supplierPhoneNumberColumnIndex);


        // Checks if the supplier and his phone number are known or not
        if (TextUtils.isEmpty(supplierNameString)) {
            supplierNameString = context.getString(R.string.unknown_supplier);
        }
        final String supplierName = supplierNameString;
        if (TextUtils.isEmpty(supplierPhoneNumberString)) {
            supplierPhoneNumberString = context.getString(R.string.unknown_supplier_phone_number);
        }
        final String supplierPhoneNumber = supplierPhoneNumberString;

        // Update the TextViews with the attributes for the current book
        productNameTextView.setText(productName);
        final String priceText = "Price" + productPrice;
        PriceTextView.setText(priceText);
        final String quantityText = "Quantity" + productQuantity;
        QuantityTextView.setText(quantityText);

        productInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, EditorActivity.class);

                // Create the uri for the current book
                Uri currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, productId);

                // Set the URI on the data field of the intent
                intent.setData(currentBookUri);

                // Launch the {@link EditorActivity} to display the data for the current book.
                context.startActivity(intent);

            }
        });


        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Uri currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, productId);

                ContentValues values = new ContentValues();
                values.put(BookEntry.COLUMN_PRODUCT_NAME, productName);
                values.put(BookEntry.COLUMN_PRICE, productPrice);

                if(Integer.valueOf(productQuantity) > 0){
                    values.put(BookEntry.COLUMN_QUANTITY, Integer.valueOf(productQuantity) - 1);
                }
                else{
                    Toast insufficientQuantityToast = Toast.makeText(context, "There is nothing left to be sold.", Toast.LENGTH_SHORT);
                    insufficientQuantityToast.show();
                }

                values.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierName);
                values.put(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhoneNumber);

                context.getContentResolver().update(currentBookUri, values, null, null);

                QuantityTextView.setText(quantityText);
            }
        });

    }
}
