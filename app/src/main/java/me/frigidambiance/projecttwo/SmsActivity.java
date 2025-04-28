package me.frigidambiance.projecttwo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class SmsActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 1001;

    private TextView permissionStatusText;
    private Button sendSmsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);
        TextView itemDetails = findViewById(R.id.textViewItemDetails);

        String itemName = getIntent().getStringExtra("item_name");
        String itemLocation = getIntent().getStringExtra("item_location");

        itemDetails.setText("Item: " + itemName + "\nLocation: " + itemLocation);

        permissionStatusText = findViewById(R.id.textViewPermissionStatus);
        sendSmsButton = findViewById(R.id.buttonSendSms);
        Button requestPermissionButton = findViewById(R.id.buttonRequestPermission);

        updatePermissionStatus();

        requestPermissionButton.setOnClickListener(view -> requestSmsPermission());

        sendSmsButton.setOnClickListener(view -> sendSmsNotification());
    }

    private void updatePermissionStatus() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            permissionStatusText.setText(R.string.permission_granted);
            sendSmsButton.setEnabled(true);
        } else {
            permissionStatusText.setText(R.string.permission_denied);
            sendSmsButton.setEnabled(false);
        }
    }

    private void requestSmsPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.SEND_SMS},
                SMS_PERMISSION_CODE);
    }


    // Todo: Add item information into the text
    private void sendSmsNotification() {
        try {
            String itemName = getIntent().getStringExtra("item_name");
            String itemLocation = getIntent().getStringExtra("item_location");

            String message = "Low inventory alert: " + itemName + " is running low in " + itemLocation + ".";

            SmsManager smsManager = getSystemService(SmsManager.class);
            smsManager.sendTextMessage(
                    "+10000000000",
                    null,
                    message,
                    null,
                    null
            );

            Toast.makeText(this, "SMS Sent: " + message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS Permission Denied", Toast.LENGTH_SHORT).show();
            }
            updatePermissionStatus();
        }
    }
}
