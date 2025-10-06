package me.frigidambiance.projecttwo;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private InventoryDatabase db;
    private ListView listView;
    private EditText filterInput;
    private Spinner sortSpinner;
    private ItemAdapter adapter;
    private Button addButton, syncButton;
    private CheckBox showDeleted;
    private boolean includeDeleted = false;

    private static final List<String> SORT_OPTIONS = Arrays.asList(
            "Item A→Z",
            "Item Z→A",
            "Location A→Z",
            "Location Z→A"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.inventory_list);
        addButton = findViewById(R.id.buttonAddItem);
        syncButton = findViewById(R.id.buttonSyncNow);
        showDeleted = findViewById(R.id.checkShowDeleted);

        db = new InventoryDatabase(this);

        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ItemActivity.class);
            startActivity(intent);
        });

        syncButton.setOnClickListener(v -> {
            me.frigidambiance.projecttwo.sync.InventorySyncWorker.enqueueOneTime(this);
            // Optional: toast so user gets feedback
            Toast.makeText(this, "Sync started", Toast.LENGTH_SHORT).show();
        });

        showDeleted.setOnCheckedChangeListener((btn, checked) -> {
            includeDeleted = checked;
            loadItems(); // reload with the new filter
        });

        loadItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        me.frigidambiance.projecttwo.sync.InventorySyncWorker.enqueueOneTime(this);
        loadItems();
    }

    private void loadItems() {
        List<InventoryItem> items = db.getAllItems(includeDeleted);

        if (adapter == null) {
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
                if (selected.getId() != null) intent.putExtra("item_id", selected.getId());
                intent.putExtra("item_name", selected.getItem());
                intent.putExtra("item_location", selected.getLocation());
                startActivity(intent);
            });
        } else {
            adapter.setData(items); // reapply current filter/sort
        }
    }

    private void setupFiltering() {
        filterInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) adapter.getFilter().filter(s);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSorting() {
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (adapter == null) return;
                switch (SORT_OPTIONS.get(position)) {
                    case "Item A→Z":
                        adapter.setSort(ItemAdapter.byNameAsc());
                        break;
                    case "Item Z→A":
                        adapter.setSort(ItemAdapter.byNameDesc());
                        break;
                    case "Location A→Z":
                        adapter.setSort(ItemAdapter.byLocationAsc());
                        break;
                    case "Location Z→A":
                        adapter.setSort(ItemAdapter.byLocationDesc());
                        break;
                    default:
                        adapter.setSort(null);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {
                if (adapter != null) adapter.setSort(null);
            }
        });
    }
}