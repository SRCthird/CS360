package me.frigidambiance.projecttwo;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.*;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ItemAdapter extends BaseAdapter implements Filterable {
    private final Context context;
    private final InventoryDatabase db;
    private final Runnable refreshCallback;

    // Full data and the currently displayed (filtered/sorted) data
    private final List<InventoryItem> original;
    private List<InventoryItem> visible;

    // Current comparator (nullable = no specific sort beyond insertion order)
    @Nullable private Comparator<InventoryItem> currentComparator;

    public ItemAdapter(Context context,
                       List<InventoryItem> data,
                       InventoryDatabase db,
                       Runnable refreshCallback) {
        this.context = context;
        this.db = db;
        this.refreshCallback = refreshCallback;
        this.original = new ArrayList<>(data);
        this.visible = new ArrayList<>(data);
    }

    // Public API to update the whole dataset (e.g., after DB refresh)
    public void setData(List<InventoryItem> newData) {
        original.clear();
        original.addAll(newData);
        // Re-apply filter and sort
        getFilter().filter(currentQuery);
    }

    @Override public int getCount() { return visible.size(); }
    @Override public Object getItem(int position) { return visible.get(position); }
    public InventoryItem getItemAt(int position) { return visible.get(position); }
    @Override public long getItemId(int position) {
        InventoryItem it = visible.get(position);
        return it.getId() != null ? it.getId() : position;
    }

    static class ViewHolder {
        TextView itemText, locationText;
        ImageButton deleteButton, smsButton;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.inventory_item, parent, false);
            holder = new ViewHolder();
            holder.itemText = convertView.findViewById(R.id.itemText);
            holder.locationText = convertView.findViewById(R.id.locationText);
            holder.deleteButton = convertView.findViewById(R.id.deleteButton);
            holder.smsButton = convertView.findViewById(R.id.smsButton);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final InventoryItem item = getItemAt(position);
        holder.itemText.setText(item.getItem());
        holder.locationText.setText(item.getLocation());

        boolean isDeleted = item.getDeletedAt() != null;
        float alpha = isDeleted ? 0.5f : 1.0f;
        convertView.setAlpha(alpha);
        int flags = holder.itemText.getPaintFlags();
        if (isDeleted) {
            holder.itemText.setPaintFlags(flags | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.itemText.setPaintFlags(flags & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        }

        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ItemActivity.class);
            if (item.getId() != null) intent.putExtra("item_id", item.getId());
            intent.putExtra("item_name", item.getItem());
            intent.putExtra("item_location", item.getLocation());
            context.startActivity(intent);
        });

        holder.smsButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, SmsActivity.class);
            intent.putExtra("item_name", item.getItem());
            intent.putExtra("item_location", item.getLocation());
            context.startActivity(intent);
        });

        holder.deleteButton.setOnClickListener(v -> {
            Integer id = item.getId();
            if (id != null) {
                db.softDeleteItem(id);
            }
            refreshCallback.run();
        });

        return convertView;
    }

    // ----- Filtering -----
    private String currentQuery = "";

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override protected FilterResults performFiltering(CharSequence constraint) {
                final String q = constraint == null ? "" : constraint.toString().trim();
                List<InventoryItem> base = new ArrayList<>();
                if (q.isEmpty()) {
                    base.addAll(original);
                } else {
                    final String qLower = q.toLowerCase();
                    for (InventoryItem it : original) {
                        if (it.getItem().toLowerCase().contains(qLower) || it.getLocation().toLowerCase().contains(qLower)) {
                            base.add(it);
                        }
                    }
                }
                // Apply current sort if any
                if (currentComparator != null) {
                    base.sort(currentComparator);
                }
                FilterResults results = new FilterResults();
                results.values = base;
                results.count = base.size();
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override protected void publishResults(CharSequence constraint, FilterResults results) {
                currentQuery = constraint == null ? "" : constraint.toString();
                visible = results.values == null ? new ArrayList<>() : (List<InventoryItem>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    // ----- Sorting -----
    public void setSort(@Nullable Comparator<InventoryItem> comparator) {
        currentComparator = comparator;
        // Re-apply filter (which also re-applies sort)
        getFilter().filter(currentQuery);
    }

    // Common comparators
    public static Comparator<InventoryItem> byNameAsc() {
        return (a, b) -> safe(a.getItem()).compareToIgnoreCase(safe(b.getItem()));
    }
    public static Comparator<InventoryItem> byNameDesc() {
        return (a, b) -> safe(b.getItem()).compareToIgnoreCase(safe(a.getItem()));
    }
    public static Comparator<InventoryItem> byLocationAsc() {
        return (a, b) -> safe(a.getLocation()).compareToIgnoreCase(safe(b.getLocation()));
    }
    public static Comparator<InventoryItem> byLocationDesc() {
        return (a, b) -> safe(b.getLocation()).compareToIgnoreCase(safe(a.getLocation()));
    }
    private static String safe(String s) { return s == null ? "" : s; }
}
