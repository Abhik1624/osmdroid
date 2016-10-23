package org.osmdroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.tileprovider.util.Counters;
import org.osmdroid.tileprovider.util.StorageUtils;
import org.osmdroid.views.MapView;

import java.io.File;
import java.util.List;

/**
 * Created by alex on 10/21/16.
 */

public class PreferenceActivity extends Activity implements View.OnClickListener {
    CheckBox checkBoxDebugTileProvider,
            checkBoxDebugMode,
            checkBoxHardwareAcceleration;
    Button buttonSetCache,
            buttonManualCacheEntry;
    TextView textViewCacheDirectory;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefs);
        checkBoxDebugTileProvider = (CheckBox) findViewById(R.id.checkBoxDebugTileProvider);
        checkBoxDebugMode = (CheckBox) findViewById(R.id.checkBoxDebugMode);
        checkBoxHardwareAcceleration = (CheckBox) findViewById(R.id.checkBoxHardwareAcceleration);
        buttonSetCache = (Button) findViewById(R.id.buttonSetCache);
        buttonManualCacheEntry = (Button) findViewById(R.id.buttonManualCacheEntry);
        textViewCacheDirectory = (TextView) findViewById(R.id.textViewCacheDirectory);

        checkBoxDebugTileProvider.setOnClickListener(this);
        checkBoxDebugMode.setOnClickListener(this);
        checkBoxHardwareAcceleration.setOnClickListener(this);
        buttonSetCache.setOnClickListener(this);
        buttonManualCacheEntry.setOnClickListener(this);
        ((TextView)findViewById(R.id.textViewCurrentCache)).setText(
                "MAX: " + android.text.format.Formatter.formatShortFileSize(this, OpenStreetMapTileProviderConstants.TILE_MAX_CACHE_SIZE_BYTES)  +
                "\nTRIM: " + android.text.format.Formatter.formatShortFileSize(this, OpenStreetMapTileProviderConstants.TILE_TRIM_CACHE_SIZE_BYTES) +
                        "\nCURRENT: " + android.text.format.Formatter.formatShortFileSize(this, new SqlTileWriter().getCurrentSize())
        );
        ((TextView)findViewById(R.id.textViewCurrentCounters)).setText(Counters.asString());
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        checkBoxDebugMode.setChecked(prefs.getBoolean("checkBoxDebugMode", OpenStreetMapTileProviderConstants.DEBUGMODE));
        checkBoxDebugTileProvider.setChecked(prefs.getBoolean("checkBoxDebugTileProvider", OpenStreetMapTileProviderConstants.DEBUG_TILE_PROVIDERS));
        checkBoxHardwareAcceleration.setChecked(prefs.getBoolean("checkBoxHardwareAcceleration", MapView.hardwareAccelerated));
        textViewCacheDirectory.setText(prefs.getString("textViewCacheDirectory", OpenStreetMapTileProviderConstants.TILE_PATH_BASE.getAbsolutePath()));

    }

    @Override
    public void onPause() {
        super.onPause();
        OpenStreetMapTileProviderConstants.DEBUGMODE = checkBoxDebugMode.isChecked();
        OpenStreetMapTileProviderConstants.DEBUG_TILE_PROVIDERS = checkBoxDebugTileProvider.isChecked();
        MapView.hardwareAccelerated = checkBoxHardwareAcceleration.isChecked();
        OpenStreetMapTileProviderConstants.setCachePath(textViewCacheDirectory.getText().toString());

        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
        edit.putBoolean("checkBoxDebugMode", checkBoxDebugMode.isChecked());
        edit.putBoolean("checkBoxDebugTileProvider", checkBoxDebugTileProvider.isChecked());
        edit.putBoolean("checkBoxHardwareAcceleration", checkBoxHardwareAcceleration.isChecked());
        edit.putString("textViewCacheDirectory", textViewCacheDirectory.getText().toString());
        edit.commit();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.buttonManualCacheEntry: {
                showManualEntry();
            }
            break;
            case R.id.buttonSetCache: {
                showPickCacheFromList();
            }
            break;

        }
    }

    private void showPickCacheFromList() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Cache Location");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice);

        final List<StorageUtils.StorageInfo> storageList = StorageUtils.getStorageList();
        for (int i=0; i < storageList.size(); i++) {
            if (!storageList.get(i).readonly)
                arrayAdapter.add(storageList.get(i).path);
        }

        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String item = arrayAdapter.getItem(which);
                try {
                    new File(item + File.separator + "osmdroid" + File.separator + "tiles" + File.separator).mkdirs();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                textViewCacheDirectory.setText(item + File.separator + "osmdroid" + File.separator + "tiles");
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    private void showManualEntry() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Cache Location");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setLines(1);
        input.setText(textViewCacheDirectory.getText().toString());
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                File file = new File(input.getText().toString());
                if (!file.exists()) {
                    input.setError("Does not exist");
                } else if (file.exists() && !file.isDirectory()) {
                    input.setError("Not a directory");
                } else if (!StorageUtils.isWritable(file)){
                    input.setError("Not writable");
                } else {
                    input.setError(null);
                }
            }
        });
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (input.getError()==null) {
                    textViewCacheDirectory.setText(input.getText().toString());
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }
}
