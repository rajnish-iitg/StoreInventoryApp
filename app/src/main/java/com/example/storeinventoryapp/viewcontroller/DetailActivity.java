package com.example.storeinventoryapp.viewcontroller;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.storeinventoryapp.R;
import com.example.storeinventoryapp.model.ProductEntity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = DetailActivity.class.getSimpleName();
    final Context mContext = this;
    private static final int PRODUCT_LOADER = 1;


    /** All UI Components */
    private TextView mTextViewLabelProduct;
    private TextView mTextViewLabelPrice;
    private TextView mTextViewLabelDiscount;
    private TextView mTextViewLabelStock;
    private TextView mTextViewLabelSupplier;
    private TextView mTextViewLabelPhone;
    private TextView mTextViewLabelEmail;
    private TextView mTextViewProduct;
    private TextView mTextViewProductPrice;
    private TextView mTextViewProductStock;
    private TextView mTextViewSupplier;
    private TextView mTextViewSupplierPhone;
    private TextView mTextViewSupplierEmail;
    private ImageButton mButtonPhone;
    private ImageButton mButtonEmail;
    private ImageButton mButtonDecrease;
    private ImageButton mButtonIncrease;
    private ImageView mImageProduct;


    private Uri mCurrentProductUri;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Initialize all UI components
        mTextViewLabelProduct = (TextView) findViewById(R.id.text_label_product);
        mTextViewLabelPrice = (TextView) findViewById(R.id.text_label_price);
        mTextViewLabelStock = (TextView) findViewById(R.id.text_label_stock);
        mTextViewLabelSupplier = (TextView) findViewById(R.id.text_label_supplier);
        mTextViewLabelPhone = (TextView) findViewById(R.id.text_label_phone);
        mTextViewLabelEmail = (TextView) findViewById(R.id.text_label_email);

        mTextViewProduct = (TextView) findViewById(R.id.text_product_name);
        mTextViewProductPrice = (TextView) findViewById(R.id.text_product_price);
        mTextViewProductStock = (TextView) findViewById(R.id.text_stock);
        mTextViewSupplier = (TextView) findViewById(R.id.text_supplier_name);
        mTextViewSupplierPhone = (TextView) findViewById(R.id.text_supplier_phone);
        mTextViewSupplierEmail = (TextView) findViewById(R.id.text_supplier_email);
        mButtonEmail = (ImageButton) findViewById(R.id.button_email);
        mButtonPhone = (ImageButton) findViewById(R.id.button_phone);
        mButtonIncrease = (ImageButton) findViewById(R.id.button_increase);
        mButtonDecrease = (ImageButton) findViewById(R.id.button_decrease);
        mImageProduct = (ImageView) findViewById(R.id.image_product);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();
        if(mCurrentProductUri != null) {
            getSupportLoaderManager().initLoader(PRODUCT_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,                       // Parent activity context
                mCurrentProductUri,         // Table to query
                null,                       // Projection
                null,                       // Selection clause
                null,                       // Selection arguments
                null                        // Default sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if(cursor.moveToFirst()) {
            DatabaseUtils.dumpCursor(cursor);

            int productColumnIndex = cursor.getColumnIndex(ProductEntity.ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntity.ProductEntry.COLUMN_PRODUCT_PRICE);
            int stockColumnIndex = cursor.getColumnIndex(ProductEntity.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(ProductEntity.ProductEntry.COLUMN_PRODUCT_IMAGE);
            int supplierColumnIndex = cursor.getColumnIndex(ProductEntity.ProductEntry.COLUMN_SUPPLIER_NAME);
            int phoneColumnIndex = cursor.getColumnIndex(ProductEntity.ProductEntry.COLUMN_SUPPLIER_PHONE);
            int emailColumnIndex = cursor.getColumnIndex(ProductEntity.ProductEntry.COLUMN_SUPPLIER_EMAIL);

            // Extract out the value from the Cursor for the respective column index
            final String product = cursor.getString(productColumnIndex);
            Double price = cursor.getDouble(priceColumnIndex);
            final int productStock = cursor.getInt(stockColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            final String phone = cursor.getString(phoneColumnIndex);
            final String email = cursor.getString(emailColumnIndex);
            final String image = cursor.getString(imageColumnIndex);

            mTextViewProduct.setText(product);
            mTextViewProductPrice.setText("$ " + String.format("%.02f", price));
            mTextViewProductStock.setText(Integer.toString(productStock));
            mTextViewSupplier.setText(supplier);
            mTextViewSupplierEmail.setText(email);

            if (!TextUtils.isEmpty(phone)) {
                mTextViewSupplierPhone.setText(phone);
            } else {
                mButtonPhone.setVisibility(View.GONE);
            }

            // Display image attached to the product
            ViewTreeObserver viewTreeObserver = mImageProduct.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mImageProduct.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mImageProduct.setImageBitmap(getBitmapFromUri(Uri.parse(image)));
                }
            });

            // Set OnClickListener on stock decrease button
            mButtonDecrease.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adjustStock(mCurrentProductUri, (productStock - 1));
                }
            });

            // Set OnClickListener on stock increase button
            mButtonIncrease.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adjustStock(mCurrentProductUri, (productStock + 1));
                }
            });

            // Set OnClickListener on email
            mButtonEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    orderStockByEmail(email, getString(R.string.label_email_subject, product));
                }
            });

            // Set OnClickListener on call button
            if (mButtonPhone.getVisibility() == View.VISIBLE) {
                mButtonPhone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        orderStockByPhone(phone);
                    }
                });
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // No action here
    }

    /**
     * Method to display the image
     * Credit => Used function from https://github.com/crlsndrsjmnz/MyShareImageExample
     * as was recommended as best practice for image display by forum mentor
     * @param uri - image path
     * @return Bitmap
     */
    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mImageProduct.getWidth();
        int targetH = mImageProduct.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, getString(R.string.exception_image_load_failed), fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, getString(R.string.exception_image_load_failed), e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }

    /**
     * Method to increase / decrease product stock
     * @param itemUri - uri of the product for which stock is to be updated
     * @param newStockCount - new stock
     * @return number of rows updated (this should be 1)
     */
    private int adjustStock(Uri itemUri, int newStockCount) {
        if (newStockCount < 0) {
            return 0;
        }

        ContentValues values = new ContentValues();
        values.put(ProductEntity.ProductEntry.COLUMN_PRODUCT_QUANTITY, newStockCount);
        int numRowsUpdated = getContentResolver().update(itemUri, values, null, null);
        return numRowsUpdated;
    }

    /**
     * Method to send email to supplier
     * @param emailAddress - email-id of supplier
     * @param emailSubject - email subject
     */
    public void orderStockByEmail (String emailAddress, String emailSubject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, emailAddress);
        intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * Method to call supplier
     * @param phoneNumber - supplier's phone number
     */
    public void orderStockByPhone(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * Inflate the menu options from the res/menu/menu_delete.xml file.
     * @param menu
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    /**
     * Method to handle actions when individual menu item is clicked
     * @param item
     * @return true/false
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {

            case R.id.action_edit:
                editProduct();
                return true;

            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                deleteProduct();
                return true;

            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method to launch ProductEditorActivity with the product URI so the item can be edited
     */
    public void editProduct() {
        Intent intent = new Intent(DetailActivity.this, ProductEditorActivity.class);
        intent.setData(mCurrentProductUri);
        startActivity(intent);
    }


    /**
     * Method to delete the product
     */
    private void deleteProduct() {

        // Only perform the delete if this is an existing product
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete
                Toast.makeText(this, getString(R.string.error_delete_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful
                Toast.makeText(this, getString(R.string.confirm_delete_successful), Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

}
