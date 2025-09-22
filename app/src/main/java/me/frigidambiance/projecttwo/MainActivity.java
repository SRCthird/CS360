package me.frigidambiance.projecttwo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private InventoryDatabase db;
    private ListView listView;
    private Button addButton;
    private ItemAdapter adapter; // keep a reference if you want to reuse later

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.inventory_list);
        addButton = findViewById(R.id.buttonAddItem);
        db = new InventoryDatabase(this);

        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ItemActivity.class);
            startActivity(intent);
        });

        loadItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    private void loadItems() {
        List<InventoryItem> items = db.getAllItems();

        adapter = new ItemAdapter(
                this,
                items,
                db,
                this::loadItems
        );
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            InventoryItem selected = adapter.getItemAt(position);

            Intent intent = new Intent(MainActivity.this, ItemActivity.class);
            if (selected.getId() != null) {
                intent.putExtra("item_id", selected.getId());
            }
            intent.putExtra("item_name", selected.getItem());
            intent.putExtra("item_location", selected.getLocation());
            startActivity(intent);
        });
    }
}