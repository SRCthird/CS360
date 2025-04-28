package me.frigidambiance.projecttwo;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.*;
import java.util.*;

public class ItemAdapter extends BaseAdapter {
    private final Context context;
    private final List<HashMap<String, String>> data;
    private final InventoryDatabase db;
    private final Runnable refreshCallback;

    public ItemAdapter(Context context, List<HashMap<String, String>> data,
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

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
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
        final HashMap<String, String> item = data.get(position);

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

        holder.itemText.setText(item.get("item"));
        holder.locationText.setText(item.get("location"));

        // Edit on row click
        convertView.setOnClickListener(v -> {
            int itemId = db.getIdByItem(item.get("item"));
            Intent intent = new Intent(context, ItemActivity.class);
            intent.putExtra("item_id", itemId);
            intent.putExtra("item_name", item.get("item"));
            intent.putExtra("item_location", item.get("location"));
            context.startActivity(intent);
        });

        // SMS
        holder.smsButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, SmsActivity.class);
            intent.putExtra("item_name", item.get("item"));
            intent.putExtra("item_location", item.get("location"));
            context.startActivity(intent);
        });

        // Delete
        holder.deleteButton.setOnClickListener(v -> {
            int id = db.getIdByItem(item.get("item"));
            db.deleteItem(id);
            refreshCallback.run();
        });

        return convertView;
    }
}