package com.example.rusha.inventory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.rusha.inventory.data.InventoryContract.InventoryEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private EditText EditName;
    private EditText EditQuan;
    private EditText EditPrice;
    private Button buttonsell;
    private EditText EditSale;
    private EditText EditOrder;
    private Button buttonorder;
    private Button buttonadd;
    private Uri InventUri;
    private int nsold;
    private int nadd;
    private EditText EditAdd;
    private int norder;
    private boolean InventChanged = false;
    private static final int LOADER_ID = 1;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int PICK_IMAGE_REQUEST = 0;
    private static final int SEND_MAIL_REQUEST = 1;
    private static final String STATE_URI = "STATE_URI";
    private ImageView mImageView;
    private Button mFab;
    private Uri mUri;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            InventChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent i = getIntent();
        InventUri = i.getData();
        if (InventUri == null) {
            setTitle("New Chocolate");
            LinearLayout linear = (LinearLayout) findViewById(R.id.sold);
            linear.setVisibility(View.GONE);
            LinearLayout linear1 = (LinearLayout) findViewById(R.id.ord);
            linear1.setVisibility(View.GONE);
            LinearLayout linear2 = (LinearLayout) findViewById(R.id.add);
            linear2.setVisibility(View.GONE);
            invalidateOptionsMenu();
        } else {
            setTitle("Edit Details");
            getLoaderManager().initLoader(LOADER_ID, null, this);
        }

        EditName = (EditText) findViewById(R.id.edit_name);
        EditQuan = (EditText) findViewById(R.id.edit_quan);
        EditPrice = (EditText) findViewById(R.id.edit_price);
        EditSale = (EditText) findViewById(R.id.sale);
        EditAdd = (EditText) findViewById(R.id.added);
        mImageView = (ImageView) findViewById(R.id.image1);
        mFab = (Button) findViewById(R.id.button);
        EditOrder = (EditText) findViewById(R.id.order);
        buttonsell = (Button) findViewById(R.id.buttonsell);
        buttonorder = (Button) findViewById(R.id.buttonorder);
        buttonadd = (Button) findViewById(R.id.buttonadd);

        EditName.setOnTouchListener(mTouchListener);
        EditPrice.setOnTouchListener(mTouchListener);
        EditQuan.setOnTouchListener(mTouchListener);
        EditAdd.setOnTouchListener(mTouchListener);

        buttonsell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sale = EditSale.getText().toString();
                if (sale.isEmpty() && sale == null)
                    nsold = 0;
                else {
                    if (!sale.isEmpty() && sale != null) {
                        nsold = Integer.parseInt(sale);
                        String CurrentSale = EditQuan.getText().toString();
                        int CurrentQuantity = Integer.parseInt(CurrentSale);
                        int quantityFinal = CurrentQuantity - nsold;
                        if (quantityFinal >= 0) {
                            ContentValues values = new ContentValues();
                            values.put(InventoryEntry.COLUMN_QUANTITY, String.valueOf(quantityFinal));
                            int rows = getContentResolver().update(InventUri, values, null, null);
                            EditQuan.setText(String.valueOf(quantityFinal));
                            EditSale.setText("");
                            Toast.makeText(getApplicationContext(), nsold + " packs sold", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(getApplicationContext(), "No.of packs sold cannot be > stock", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(getApplicationContext(), "Invalid input", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String add = EditAdd.getText().toString();
                if (add.isEmpty() && add == null)
                    nadd = 0;
                else {
                    if (!add.isEmpty() && add != null) {
                        nadd = Integer.parseInt(add);

                        String CurrentAdd = EditQuan.getText().toString();
                        int CurrentQuantity = Integer.parseInt(CurrentAdd);
                        int quantityFinal = CurrentQuantity + nadd;
                        if (quantityFinal >= 0) {
                            ContentValues values = new ContentValues();
                            values.put(InventoryEntry.COLUMN_QUANTITY, String.valueOf(quantityFinal));
                            int rows = getContentResolver().update(InventUri, values, null, null);
                            EditQuan.setText(String.valueOf(quantityFinal));
                            EditAdd.setText("");
                            Toast.makeText(getApplicationContext(), nadd + " packs added", Toast.LENGTH_SHORT).show();
                        }

                    } else
                        Toast.makeText(getApplicationContext(), "Invalid input", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String order = EditOrder.getText().toString();
                if (order.isEmpty() && order == null)
                    norder = 0;

                else {
                    if (!order.isEmpty() && order != null)
                        norder = Integer.parseInt(order);
                    else
                        Toast.makeText(getApplicationContext(), "Invalid input", Toast.LENGTH_SHORT).show();
                }
                if (norder > 0) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:"));
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{EditName.getText().toString() + "@gmail.com"});
                    intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.or) + EditName.getText().toString() + " " + norder + " packets");
                    if (intent.resolveActivity(getPackageManager()) != null)
                        startActivity(intent);

                    String CurrentOrder = EditQuan.getText().toString();
                    int CurrentQuantity = Integer.parseInt(CurrentOrder);
                    int quantityFinal = CurrentQuantity + norder;
                    ContentValues values = new ContentValues();
                    values.put(InventoryEntry.COLUMN_QUANTITY, String.valueOf(quantityFinal));
                    int rows = getContentResolver().update(InventUri, values, null, null);
                    EditQuan.setText(String.valueOf(quantityFinal));
                    EditOrder.setText("");
                }
            }
        });

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageSelector();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mUri != null)
            outState.putString(STATE_URI, mUri.toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(STATE_URI) &&
                !savedInstanceState.getString(STATE_URI).equals("")) {
            mUri = Uri.parse(savedInstanceState.getString(STATE_URI));

            ViewTreeObserver viewTreeObserver = mImageView.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onGlobalLayout() {
                    mImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mImageView.setImageBitmap(getBitmapFromUri(mUri));
                }
            });
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                mUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mUri.toString());
                mImageView.setImageBitmap(getBitmapFromUri(mUri));
            }
        } else if (requestCode == SEND_MAIL_REQUEST && resultCode == Activity.RESULT_OK) {

        }
    }


    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

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
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }


    public void openImageSelector() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (InventUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }


    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.look));
        builder.setPositiveButton(getString(R.string.dis), discardButtonClickListener);
        builder.setNegativeButton(getString(R.string.keep), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing.
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
    public void onBackPressed() {
        if (!InventChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void insert() {

        if (norder > 0)
            Toast.makeText(getApplicationContext(), norder + " packs ordered successfully", Toast.LENGTH_SHORT).show();
        String nameString = EditName.getText().toString().trim();
        String quanString = EditQuan.getText().toString().trim();
        String priceString = EditPrice.getText().toString().trim();

        if (TextUtils.isEmpty(nameString))
            Toast.makeText(getApplicationContext(), "Name not entered", Toast.LENGTH_SHORT).show();
        if (TextUtils.isEmpty(quanString))
            Toast.makeText(getApplicationContext(), "Quantity not entered", Toast.LENGTH_SHORT).show();
        if (TextUtils.isEmpty(priceString))
            Toast.makeText(getApplicationContext(), "Price not entered", Toast.LENGTH_SHORT).show();

        if (InventUri == null && (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(quanString) ||
                TextUtils.isEmpty(priceString))) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_NAME, nameString);
        int quan = 0;
        if (!TextUtils.isEmpty(quanString))
            quan = Integer.parseInt(quanString);
        if (norder != 0)
            quan = quan + norder;
        values.put(InventoryEntry.COLUMN_QUANTITY, quan);
        Double price = 0.0;
        if (!TextUtils.isEmpty(priceString))
            price = Double.parseDouble(priceString);
        values.put(InventoryEntry.COLUMN_PRICE, price);
        if (mUri != null)
            values.put(InventoryEntry.COLUMN_IMAGE, mUri.toString());
        else if (InventUri == null) {
            Uri u = Uri.parse("android.resource://com.example.rusha.inventory/drawable/noimage");
            Toast.makeText(getApplicationContext(), "Image not selected", Toast.LENGTH_SHORT).show();
            values.put(InventoryEntry.COLUMN_IMAGE, u.toString());
        }

        if (InventUri == null) {
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.in), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.suc), Toast.LENGTH_SHORT).show();
            }
        } else {

            int rowsAffected = getContentResolver().update(InventUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.uf), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.up), Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void deletionrequest(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.con));

        builder.setPositiveButton(getString(R.string.del), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                int rows = getContentResolver().delete(InventUri, null, null);
                if (rows == 0)
                    Toast.makeText(getApplicationContext(), getString(R.string.df), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), getString(R.string.de), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                finish();
            }
        });

        builder.setNegativeButton(getString(R.string.ndel), new DialogInterface.OnClickListener() {
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


    private void deleteInvent() {
        if (InventUri != null) {

            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // User clicked "Discard" button, close the current activity.
                            finish();
                        }
                    };
            deletionrequest(discardButtonClickListener);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                insert();
                finish();
                return true;
            case R.id.action_delete:
                deleteInvent();
                return true;
            case android.R.id.home:
                if (!InventChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_NAME,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_IMAGE
        };
        return new CursorLoader(this,
                InventUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {


        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {


            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_NAME);
            int quanColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
            int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int quan = cursor.getInt(quanColumnIndex);
            Double price = cursor.getDouble(priceColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            EditName.setText(name);
            EditQuan.setText(String.valueOf(quan));
            EditPrice.setText(String.format("%.2f", price));
            mImageView.setImageURI(Uri.parse(image));
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        EditName.setText("");
        EditQuan.setText("");
        EditPrice.setText("");
    }
}
