package me.frigidambiance.projecttwo;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

public class ItemAdapter extends BaseAdapter {
    private final Context context;
    private final List<InventoryItem> data;
    private final InventoryDatabase db;
    private final Runnable refreshCallback;

    public ItemAdapter(Context context, List<InventoryItem> data,
                       InventoryDatabase db, Runnable refreshCallback) {
        this.context = context;
        this.data = data;
        this.db = db;
        this.refreshCallback = refreshCallback;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    /** BaseAdapter requires Object; add a typed helper below if you prefer. */
    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    /** Return stable db id if present; fallback to position. */
    @Override
    public long getItemId(int position) {
        InventoryItem item = data.get(position);
        return item.getId() != null ? item.getId() : position;
    }

    public InventoryItem getItemAt(int position) {
        return data.get(position);
    }

    static class ViewHolder {
        TextView itemText;
        TextView locationText;
        ImageButton deleteButton;
        ImageButton smsButton;
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

        final InventoryItem item = data.get(position);
        holder.itemText.setText(item.getItem());
        holder.locationText.setText(item.getLocation());

        // Edit on row click
        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ItemActivity.class);
            // Prefer passing the id if available; your ItemActivity can fetch latest from DB
            if (item.getId() != null) {
                intent.putExtra("item_id", item.getId());
            }
            intent.putExtra("item_name", item.getItem());
            intent.putExtra("item_location", item.getLocation());
            context.startActivity(intent);
        });

        // SMS
        holder.smsButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, SmsActivity.class);
            intent.putExtra("item_name", item.getItem());
            intent.putExtra("item_location", item.getLocation());
            context.startActivity(intent);
        });

        // Delete
        holder.deleteButton.setOnClickListener(v -> {
            Integer id = item.getId();
            if (id != null) {
                db.deleteItem(id);
            } else {
                // Fallback if this item wasn't loaded with an id (shouldn't happen with your getAllItems)
                InventoryItem fromDb = db.getItemByName(item.getItem());
                if (fromDb != null && fromDb.getId() != null) {
                    db.deleteItem(fromDb.getId());
                }
            }
            refreshCallback.run();
        });

        return convertView;
    }
}