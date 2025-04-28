package me.frigidambiance.projecttwo;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ItemActivity extends AppCompatActivity {
    private InventoryDatabase db;
    private EditText editTextItem, editTextLocation;
    private int itemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        editTextItem = findViewById(R.id.editTextItem);
        editTextLocation = findViewById(R.id.editTextLocation);
        Button saveButton = findViewById(R.id.buttonSaveItem);

        db = new InventoryDatabase(this);

        // Check if we're updating an item
        Intent intent = getIntent();
        if (intent.hasExtra("item_id")) {
            itemId = intent.getIntExtra("item_id", -1);
            String name = intent.getStringExtra("item_name");
            String location = intent.getStringExtra("item_location");
            editTextItem.setText(name);
            editTextLocation.setText(location);
        }

        saveButton.setOnClickListener(v -> {
            String name = editTextItem.getText().toString().trim();
            String location = editTextLocation.getText().toString().trim();

            if (name.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Both fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success;
            if (itemId == -1) {
                success = db.insertItem(name, location);
            } else {
                success = db.updateItem(itemId, name, location);
            }

            if (success) {
                finish(); // return to main
            } else {
                Toast.makeText(this, "Failed to save item", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
