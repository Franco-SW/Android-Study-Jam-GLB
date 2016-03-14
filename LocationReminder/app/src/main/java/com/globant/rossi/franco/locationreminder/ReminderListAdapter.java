package com.globant.rossi.franco.locationreminder;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by matia on 13/3/2016.
 */
public class ReminderListAdapter extends ArrayAdapter<Reminder> {

    private LayoutInflater layoutInflater;
    public ReminderListAdapter(Context context, List<Reminder> reminders, LayoutInflater inflater) {
        super(context, R.layout.reminder_list_item, reminders);
        layoutInflater = inflater;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View itemView = convertView;

        if(itemView == null)
        {
            itemView = layoutInflater.inflate(R.layout.reminder_list_item, parent, false);
        }

        Reminder reminder = getItem(position);

        TextView titleTextView = (TextView)itemView.findViewById(R.id.list_item_title);
        titleTextView.setText(reminder.title);



        return itemView;
    }
}