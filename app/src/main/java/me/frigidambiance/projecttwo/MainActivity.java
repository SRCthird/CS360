package me.frigidambiance.projecttwo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private InventoryDatabase db;
    private ListView listView;
    private Button addButton;

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
        List<HashMap<String, String>> itemList = db.getAllItems();

        ItemAdapter adapter = new ItemAdapter(
                this,
                itemList,
                db,
                this::loadItems
        );

        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            HashMap<String, String> selected = itemList.get(position);
            int itemId = db.getIdByItem(selected.get("item"));

            Intent intent = new Intent(MainActivity.this, ItemActivity.class);
            intent.putExtra("item_id", itemId);
            intent.putExtra("item_name", selected.get("item"));
            intent.putExtra("item_location", selected.get("location"));
            startActivity(intent);
        });
    }

}
