package com.example.storeinventoryapp.viewcontroller;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.storeinventoryapp.R;
import com.example.storeinventoryapp.model.ProductEntity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class ProductEditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = ProductEditorActivity.class.getSimpleName();
    private static final int IMAGE_REQUEST_CODE = 0;
    private static final int EXISTING_ITEM_LOADER = 0;
    private static final String STATE_IMAGE_URI = "STATE_IMAGE_URI";

    final Context mContext = this;

    /** All UI Components */
    private TextView mTextViewInfo;
    private TextView mTextViewError;
    private TextView mTextViewProduct;
    private TextView mTextViewPrice;
    private TextView mTextViewDiscount;
    private TextView mTextViewStock;
    private TextView mTextViewSupplier;
    private TextView mTextViewSupplierPhone;
    private TextView mTextViewSupplierEmail;
    private EditText mEditTextProduct;
    private EditText mEditTextProductPrice;
    private EditText mEditTextProductStock;
    private EditText mEditTextSupplier;
    private EditText mEditTextSupplierPhone;
    private EditText mEditTextSupplierEmail;
    private Button mButtonAddImage;
    private ImageView mImageProduct;

    private Uri mCurrentProductUri;
    private Uri mImageUri;

    /** Variables to hold data from input fields */
    private String product;
    private String strPrice;
    private Double productPrice;
    private String strStock;
    private int productStock;
    private String supplier;
    private String supplierPhone;
    private String supplierEmail;
    private String imagePath;



    // Boolean flag that keeps track of whether the pet has been edited (true) or not (false)
    private boolean mItemHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if adding a new product or editing an existing one.
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // Check if CurrentProductUri is null (for new product) or not (for existing product)
        // Set action bar title accordingly
        // Also, for existing product, initialize a loader to fetch data from the database and
        // display the current values in the editor
        if (mCurrentProductUri == null) {
            setTitle(getTitle() + getString(R.string.title_editor_add));
        } else {
            setTitle(getTitle() + getString(R.string.title_editor_edit));
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        // Initialize all UI components
        initializeUIElements();

        if (mCurrentProductUri == null) {
            mButtonAddImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonImageClick();
                }
            });
        } else {
            mButtonAddImage.setVisibility(View.GONE);
        }
    }

    /**
     * This method initializes all UI components used in the Activity
     */
    public void initializeUIElements() {
        mTextViewInfo = (TextView) findViewById(R.id.text_required_info);
        mTextViewError = (TextView) findViewById(R.id.text_error);
        mTextViewProduct = (TextView) findViewById(R.id.text_label_product);
        mTextViewPrice = (TextView) findViewById(R.id.text_label_price);
        mTextViewStock = (TextView) findViewById(R.id.text_label_stock);
        mTextViewSupplier = (TextView) findViewById(R.id.text_label_supplier);
        mTextViewSupplierEmail = (TextView) findViewById(R.id.text_label_email);
        mTextViewSupplierPhone = (TextView) findViewById(R.id.text_label_phone);

        mEditTextProduct = (EditText) findViewById(R.id.edit_product_name);
        mEditTextProductPrice = (EditText) findViewById(R.id.edit_product_price);
        mEditTextProductStock = (EditText) findViewById(R.id.edit_product_stock);
        mEditTextSupplier = (EditText) findViewById(R.id.edit_supplier_name);
        mEditTextSupplierEmail = (EditText) findViewById(R.id.edit_supplier_email);
        mEditTextSupplierPhone = (EditText) findViewById(R.id.edit_supplier_phone);

        mButtonAddImage = (Button) findViewById(R.id.button_add_image);
        mImageProduct = (ImageView) findViewById(R.id.image_product);

        // Setup OnTouchListeners on all the input fields, to determine if the user
        // has touched or modified them
        mEditTextProduct.setOnTouchListener(mTouchListener);
        mEditTextProductPrice.setOnTouchListener(mTouchListener);
        mEditTextProductStock.setOnTouchListener(mTouchListener);
        mEditTextSupplier.setOnTouchListener(mTouchListener);
        mEditTextSupplierEmail.setOnTouchListener(mTouchListener);
        mEditTextSupplierPhone.setOnTouchListener(mTouchListener);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mImageUri != null) {
            outState.putString(STATE_IMAGE_URI, mImageUri.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(STATE_IMAGE_URI) &&
                !savedInstanceState.getString(STATE_IMAGE_URI).equals("")) {
            mImageUri = Uri.parse(savedInstanceState.getString(STATE_IMAGE_URI));

            ViewTreeObserver viewTreeObserver = mImageProduct.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mImageProduct.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mImageProduct.setImageBitmap(getBitmapFromUri(mImageUri));
                }
            });
        }
    }


    /**
     * Method to select a picture from device's media storage
     */
    private void buttonImageClick() {
        Intent intent = new Intent();

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.action_select_picture)), IMAGE_REQUEST_CODE);
    }

    /**
     * Method to fetch data for an existing product into a cursor
     * @param i
     * @param bundle
     * @return cursor
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                ProductEntity.ProductEntry._ID,
                ProductEntity.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntity.ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntity.ProductEntry.COLUMN_PRODUCT_STOCK,
                ProductEntity.ProductEntry.COLUMN_PRODUCT_STOCK,
                ProductEntity.ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductEntity.ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntity.ProductEntry.COLUMN_SUPPLIER_PHONE,
                ProductEntity.ProductEntry.COLUMN_SUPPLIER_EMAIL
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,       // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current pet
                projection,                 // Columns to include in the resulting Cursor
                null,                       // No selection clause
                null,                       // No selection arguments
                null);                      // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {

            DatabaseUtils.dumpCursor(cursor);

            int productColumnIndex = cursor.getColumnIndex(ProductEntity.ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntity.ProductEntry.COLUMN_PRODUCT_PRICE);
            int stockColumnIndex = cursor.getColumnIndex(ProductEntity.ProductEntry.COLUMN_PRODUCT_STOCK);
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
            String image = cursor.getString(imageColumnIndex);

            mEditTextProduct.setText(product);
            mEditTextSupplier.setText(supplier);
            mEditTextSupplierEmail.setText(email);
            mEditTextSupplierPhone.setText(phone);
            mEditTextProductPrice.setText(String.format("%.02f", price));

            mEditTextProductStock.setText(String.valueOf(productStock));
            mImageProduct.setImageBitmap(getBitmapFromUri(Uri.parse(image)));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mEditTextProduct.setText("");
        mEditTextProductPrice.setText("");
        mEditTextProductStock.setText("");
        mEditTextSupplier.setText("");
        mEditTextSupplierEmail.setText("");
        mEditTextSupplierPhone.setText("");
    }

    /**
     * Method to set selected image to ImageView holder if request is successful
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST_CODE && (resultCode == RESULT_OK)) {
            try {
                mImageUri = data.getData();
                int takeFlags = data.getFlags();
                takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                try {
                    getContentResolver().takePersistableUriPermission(mImageUri, takeFlags);
                }
                catch (SecurityException e){
                    e.printStackTrace();
                }

                mImageProduct.setImageBitmap(getBitmapFromUri(mImageUri));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
     * Inflate the menu options from the res/menu/menu_editor.xml file.
     * @param menu
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * Method to add clear top flag so it doesn't create new instance of parent
     * @return intent
     */
    @Override
    public Intent getSupportParentActivityIntent() {
        Intent intent = super.getSupportParentActivityIntent();
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        return intent;
    }

    /**
     * Method to handle actions when individual menu item is clicked
     * @param item
     * @return true/false
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (mCurrentProductUri == null) {
                    addProduct();
                } else {
                    updateProduct();
                }
                return true;

            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                if (!mItemHasChanged && !hasEntry()) {
                    finish();
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                finish();
                                                    //NavUtils.navigateUpFromSameTask(ProductEditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with navigating up to parent activity
        if (!mItemHasChanged && !hasEntry()) {
            onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, navigate to parent activity.
                        finish();
                    }
                };

        // Show a dialog that notifies the user they have unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     * @param discardButtonClickListener - click listener action to take when user confirms discarding changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_unsaved_changes);
        builder.setPositiveButton(R.string.action_yes, discardButtonClickListener);
        builder.setNegativeButton(R.string.action_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog and continue editing
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Method to check if any entry has been made. This is to handle situation when motion event
     * is not detected, yet entries have been made (e.g. using emulator and typing using keyboard)
     */
    public boolean hasEntry() {
        boolean hasInput = false;

        if (!TextUtils.isEmpty(mEditTextProduct.getText().toString()) ||
                !TextUtils.isEmpty(mEditTextProductPrice.getText().toString()) ||
                !TextUtils.isEmpty(mEditTextProductStock.getText().toString()) ||
                !TextUtils.isEmpty(mEditTextSupplier.getText().toString()) ||
                !TextUtils.isEmpty(mEditTextSupplierEmail.getText().toString()) ||
                !TextUtils.isEmpty(mEditTextSupplierPhone.getText().toString()) ||
                (mImageProduct.getDrawable() != null)) {
            hasInput = true;
        }
        return hasInput;
    }

    /**
     * Method to add a new product to database
     */
    public void addProduct() {

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.

        if (getEditorInputs()) {
            ContentValues values = new ContentValues();
            values.put(ProductEntity.ProductEntry.COLUMN_PRODUCT_NAME, product);
            values.put(ProductEntity.ProductEntry.COLUMN_PRODUCT_PRICE, productPrice);
            values.put(ProductEntity.ProductEntry.COLUMN_PRODUCT_STOCK, productStock);
            values.put(ProductEntity.ProductEntry.COLUMN_PRODUCT_IMAGE, imagePath);
            values.put(ProductEntity.ProductEntry.COLUMN_SUPPLIER_NAME, supplier);
            values.put(ProductEntity.ProductEntry.COLUMN_SUPPLIER_PHONE, supplierPhone);
            values.put(ProductEntity.ProductEntry.COLUMN_SUPPLIER_EMAIL, supplierEmail);

            mCurrentProductUri = getContentResolver().insert(ProductEntity.ProductEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful
            // If the row ID is -1, then there was an error with insertion.
            // Otherwise, the insertion was successful and we can display a toast with the row ID.
            if (mCurrentProductUri == null) {
                Toast.makeText(this, getString(R.string.error_insert_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.confirm_insert_successful), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /**
     * Method to update an existing product
     */
    public void updateProduct() {

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.

        if (getEditorInputs()) {
            ContentValues values = new ContentValues();
            values.put(ProductEntity.ProductEntry.COLUMN_PRODUCT_NAME, product);
            values.put(ProductEntity.ProductEntry.COLUMN_PRODUCT_PRICE, productPrice);
            values.put(ProductEntity.ProductEntry.COLUMN_PRODUCT_STOCK, productStock);
            values.put(ProductEntity.ProductEntry.COLUMN_SUPPLIER_NAME, supplier);
            values.put(ProductEntity.ProductEntry.COLUMN_SUPPLIER_PHONE, supplierPhone);
            values.put(ProductEntity.ProductEntry.COLUMN_SUPPLIER_EMAIL, supplierEmail);

            int numRowsUpdated = getContentResolver().update(mCurrentProductUri, values, null, null);

            // Display error message in Log if product stock fails to update
            if (!(numRowsUpdated > 0)) {
                Toast.makeText(this, getString(R.string.error_update_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.confirm_update_successful), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /**
     * Method to get editor inputs and validate them
     */
    public boolean getEditorInputs() {

        product = mEditTextProduct.getText().toString().trim();
        strPrice = mEditTextProductPrice.getText().toString().trim();
        strStock = mEditTextProductStock.getText().toString().trim();
        supplier = mEditTextSupplier.getText().toString().trim();
        supplierEmail = mEditTextSupplierEmail.getText().toString().trim();
        supplierPhone = mEditTextSupplierPhone.getText().toString().trim();

        // Check product name
        if (TextUtils.isEmpty(product)) {
            mEditTextProduct.requestFocus();
            mEditTextProduct.setError(getString(R.string.error_empty_product));
            return false;
        }

        // Check product price
        if (TextUtils.isEmpty(strPrice)) {
            mEditTextProductPrice.requestFocus();
            mEditTextProductPrice.setError(getString(R.string.error_empty_price));
            return false;
        }

        // Check product stock
        if (TextUtils.isEmpty(strStock)) {
            mEditTextProductStock.requestFocus();
            mEditTextProductStock.setError(getString(R.string.error_empty_stock));
            return false;
        }

        // Check supplier name
        if (TextUtils.isEmpty(supplier)) {
            mEditTextSupplier.requestFocus();
            mEditTextSupplier.setError(getString(R.string.error_empty_supplier));
            return false;
        }

        // Check supplier email
        if (TextUtils.isEmpty(supplierEmail) || (!Patterns.EMAIL_ADDRESS.matcher(supplierEmail).matches())) {
            mEditTextSupplierEmail.requestFocus();
            mEditTextSupplierEmail.setError(getString(R.string.error_invalid_email));
            return false;
        }

        // Check if image selected
        if (mCurrentProductUri == null) {
            if (mImageUri == null) {
                mTextViewError.setVisibility(View.VISIBLE);
                mTextViewError.setText(getString(R.string.error_no_image));
                return false;
            } else {
                mTextViewError.setVisibility(View.GONE);
                mTextViewError.setText("");
                imagePath = mImageUri.toString();
            }
        }

        productPrice = Double.valueOf(strPrice);
        productStock = Integer.valueOf(strStock);
        return true;
    }































}
