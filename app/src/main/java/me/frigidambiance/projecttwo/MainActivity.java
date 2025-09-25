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
        Button addButton = findViewById(R.id.buttonAddItem);
        filterInput = findViewById(R.id.filterInput);
        sortSpinner = findViewById(R.id.sortSpinner);
        db = new InventoryDatabase(this);

        // Sort options
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, SORT_OPTIONS);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);

        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ItemActivity.class);
            startActivity(intent);
        });

        loadItems();
        setupFiltering();
        setupSorting();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    private void loadItems() {
        List<InventoryItem> items = db.getAllItems();

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