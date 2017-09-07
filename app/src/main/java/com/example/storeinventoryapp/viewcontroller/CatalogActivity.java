package com.example.storeinventoryapp.viewcontroller;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.storeinventoryapp.R;
import com.example.storeinventoryapp.model.ProductEntity;
import com.example.storeinventoryapp.viewmodel.ProductCursorAdapter;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = CatalogActivity.class.getName();

    final Context mContext = this;
    private static final int PRODUCT_LOADER = 1;
    private ListView mListViewProducts;
    private ProductCursorAdapter mCursorAdapter;
    private View mEmptyStateView;

    // UI Components
    private TextView mTextViewEmptyTitle;
    private TextView mTextViewEmptySubtitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Initialize UI components
        mTextViewEmptyTitle = (TextView) findViewById(R.id.text_empty_title);
        mTextViewEmptySubtitle = (TextView) findViewById(R.id.text_empty_subtitle);

        // Setup FAB to open ProductEditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, ProductEditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the list data
        mListViewProducts = (ListView) findViewById(R.id.list_products);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items
        mEmptyStateView = findViewById(R.id.empty_view);
        mListViewProducts.setEmptyView(mEmptyStateView);

        // Set up adapter to create a list item for each row of data in the Cursor
        mCursorAdapter = new ProductCursorAdapter(this, null);
        mListViewProducts.setAdapter(mCursorAdapter);

        // Setup the item click listener
        mListViewProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

               // Create new intent to go to {@link DetailActivity}
               Intent intent = new Intent(CatalogActivity.this, DetailActivity.class);

                // Form the content URI that represents the specific list item that was clicked on,
                // by appending the "id" onto the {@link ProductEntry#CONTENT_URI}.
                // Example => content://com.example.android.storeinventory/products/2, for product id = 2
                Uri currentProductUri = ContentUris.withAppendedId(ProductEntity.ProductEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentProductUri);

                // Launch the {@link DetailActivity} to display the data for the current item
                startActivity(intent);
            }
        });

        // Start the loader
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    /**
     * Method to load the cursor with records fetched from database
     * @param i
     * @param bundle
     * @return cursor
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String querySortOder = null;
        String selectClause = null;
        final String SORT_ORDER_ASC = " ASC";
        final String SORT_ORDER_DESC = " DESC";
        querySortOder = ProductEntity.ProductEntry._ID + SORT_ORDER_ASC;

        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                ProductEntity.ProductEntry._ID,
                ProductEntity.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntity.ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntity.ProductEntry.COLUMN_PRODUCT_STOCK
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,                       // Parent activity context
                ProductEntity.ProductEntry.CONTENT_URI,   // Provider content URI to query
                projection,                                 // Columns to include in the resulting Cursor
                selectClause,                               // No selection clause
                null,                                       // No selection arguments
                querySortOder                               // Default sort order
        );
    }

    /**
     * Method to execute when cursor has finished loading
     * @param loader
     * @param cursor
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Update {@link ProductCursorAdapter} with this new cursor containing updated data
        mCursorAdapter.swapCursor(cursor);
    }

    /**
     * Method to execute when data needs to be reset
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }

}
