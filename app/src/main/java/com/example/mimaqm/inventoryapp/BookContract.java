package com.example.mimaqm.inventoryapp;

import android.provider.BaseColumns;

public final class BookContract {

    public BookContract(){}

    public static final class BookEntry implements BaseColumns {

        public final static String TABLE_NAME = "books";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_PRODUCT_NAME = "Product Name";

        public final static String COLUMN_PRICE = "Price";

        public final static String COLUMN_QUANTITY = "Quantity";

        public final static String COLUMN_SUPPLIER_NAME = "Supplier Name";

        public final static String COLUMN_SUPPLIER_PHONE_NUMBER = "Supplier Phone Number";


    }
}
