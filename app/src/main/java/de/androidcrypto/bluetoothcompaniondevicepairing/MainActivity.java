package de.androidcrypto.bluetoothcompaniondevicepairing;

import static android.os.Build.VERSION.SDK_INT;

import androidx.appcompat.app.AppCompatActivity;

import android.companion.AssociationInfo;
import android.companion.AssociationRequest;
import android.companion.BluetoothDeviceFilter;
import android.companion.CompanionDeviceManager;
import android.content.Context;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "CompanionDevicePairing";

    Button startPairing;

    private final int SELECT_DEVICE_REQUEST_CODE = 201;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startPairing = findViewById(R.id.btnStartPairing);


        startPairing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "start pairing");

                BluetoothDeviceFilter deviceFilter = new BluetoothDeviceFilter.Builder()
                        // Match only Bluetooth devices whose name matches the pattern.
                        //.setNamePattern(Pattern.compile("My device"))
                        // Match only Bluetooth devices whose service UUID matches this pattern.
                        //.addServiceUuid(new ParcelUuid(new UUID(0x123abcL, -1L)), null)
                        .build();
                /*
                BluetoothDeviceFilter deviceFilter = new BluetoothDeviceFilter.Builder()
                        // Match only Bluetooth devices whose name matches the pattern.
                        .setNamePattern(Pattern.compile("My device"))
                        // Match only Bluetooth devices whose service UUID matches this pattern.
                        .addServiceUuid(new ParcelUuid(new UUID(0x123abcL, -1L)), null)
                        .build();
                 */

                AssociationRequest pairingRequest = new AssociationRequest.Builder()
                        // Find only devices that match this request filter.
                        .addDeviceFilter(deviceFilter)
                        // Stop scanning as soon as one device matching the filter is found.
                        .setSingleDevice(false)
                        .build();
                // T = Android SDK 33 = Android 13
                if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                    CompanionDeviceManager deviceManager =
                            (CompanionDeviceManager) getSystemService(Context.COMPANION_DEVICE_SERVICE);

                    Executor executor = new Executor() {
                        @Override
                        public void execute(Runnable runnable) {
                            runnable.run();
                        }
                    };
                    deviceManager.associate(pairingRequest, executor, new CompanionDeviceManager.Callback() {

                        // Called when a device is found. Launch the IntentSender so the user can
                        // select the device they want to pair with.
                        @Override
                        public void onDeviceFound(IntentSender chooserLauncher) {
                            try {
                                Log.i(TAG, "startIntentForResult SDK >= 33");
                                startIntentSenderForResult(
                                        chooserLauncher, SELECT_DEVICE_REQUEST_CODE, null, 0, 0, 0
                                );
                            } catch (IntentSender.SendIntentException e) {
                                Log.e("MainActivity", "Failed to send intent");
                            }
                        }

                        @Override
                        public void onAssociationCreated(AssociationInfo associationInfo) {
                            // The association is created.
                            Log.i(TAG, "onAssociationCreated name: "
                                    + associationInfo.getDisplayName()
                            + " mac: " + associationInfo.getDeviceMacAddress());
                        }

                        @Override
                        public void onFailure(CharSequence errorMessage) {
                            // Handle the failure.
                            Log.e(TAG, "onFailure: " + errorMessage.toString());
                        }

                    });


                } else {
                    // Android < SDK 33
                    CompanionDeviceManager deviceManager =
                            (CompanionDeviceManager) getSystemService(Context.COMPANION_DEVICE_SERVICE);
                    deviceManager.associate(pairingRequest, new CompanionDeviceManager.Callback() {
                        // Called when a device is found. Launch the IntentSender so the user can
                        // select the device they want to pair with.
                        @Override
                        public void onDeviceFound(IntentSender chooserLauncher) {
                            try {
                                Log.i(TAG, "startIntentForResult SDK < 33");
                                startIntentSenderForResult(
                                        chooserLauncher, SELECT_DEVICE_REQUEST_CODE, null, 0, 0, 0

                                );
                            } catch (IntentSender.SendIntentException e) {
                                Log.e("MainActivity", "Failed to send intent");
                            }
                        }

                        @Override
                        public void onFailure(CharSequence error) {
                            // Handle the failure.
                            Log.e(TAG, "ERROR: " + error.toString());
                        }
                    }, null);

                }
            }
        });

    }
}