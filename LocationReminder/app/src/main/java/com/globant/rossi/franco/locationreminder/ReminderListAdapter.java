package com.globant.rossi.franco.locationreminder;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Matias on 13/3/2016.
 */
public class ReminderListAdapter extends ArrayAdapter<Reminder> {
    private MainActivity mainActivity;
    private LayoutInflater layoutInflater;
    private ListView listView;

    public ReminderListAdapter(Context context, List<Reminder> reminders, LayoutInflater inflater, MainActivity mainActivity) {
        super(context, R.layout.reminder_list_item, reminders);
        layoutInflater = inflater;
        this.mainActivity = mainActivity;
    }

    public void setListView(ListView value)
    {
        listView = value;
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

        Button deleteButton = (Button)itemView.findViewById(R.id.list_item_delete_button);
        deleteButton.setTag(reminder);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.deleteReminder((Reminder) v.getTag());
            }
        });

        ListItemViewHolder holder = new ListItemViewHolder();
        holder.mainView = (LinearLayout) itemView.findViewById(R.id.list_item_main_view);
        holder.deleteView = (RelativeLayout) itemView.findViewById(R.id.list_item_delete_view);;
        itemView.setOnTouchListener(new SwipeDetector(holder, position));

        return itemView;
    }

    public static class ListItemViewHolder {
        public LinearLayout mainView;
        public RelativeLayout deleteView;

        /* other views here */
    }

    public class SwipeDetector implements View.OnTouchListener {

        private static final int MIN_DISTANCE = 300;
        private static final int MIN_LOCK_DISTANCE = 30; // disallow motion intercept
        private boolean motionInterceptDisallowed = false;
        private float downX, upX;
        private ListItemViewHolder holder;
        private int position;

        public SwipeDetector(ListItemViewHolder h, int pos) {
            holder = h;
            position = pos;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    downX = event.getX();
                    return true; // allow other events like Click to be processed
                }

                case MotionEvent.ACTION_MOVE: {
                    upX = event.getX();
                    float deltaX = downX - upX;

                    if (Math.abs(deltaX) > MIN_LOCK_DISTANCE && listView != null && !motionInterceptDisallowed) {
                        listView.requestDisallowInterceptTouchEvent(true);
                        motionInterceptDisallowed = true;
                    }

                    if (deltaX > 0) {
                        //holder.deleteView.setVisibility(View.GONE);
                    } else {
                        // if first swiped left and then swiped right
                        holder.deleteView.setVisibility(View.VISIBLE);
                    }

                    swipe(-(int) deltaX);
                    return true;
                }

                case MotionEvent.ACTION_UP:
                    upX = event.getX();
                    float deltaX = upX - downX;
                    if (Math.abs(deltaX) > MIN_DISTANCE) {
                        Log.i("DELTAX: ", "MIN DISTANCE");
                        // left or right
                        swipeRemove();
                    } else {
                        swipe(0);
                    }

                    if (listView != null) {
                        listView.requestDisallowInterceptTouchEvent(false);
                        motionInterceptDisallowed = false;
                    }

                    holder.deleteView.setVisibility(View.VISIBLE);
                    return true;

                case MotionEvent.ACTION_CANCEL:
                    holder.deleteView.setVisibility(View.VISIBLE);
                    return false;
            }

            return true;
        }

        private void swipe(int distance) {
            View animationView = holder.mainView;
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) animationView.getLayoutParams();
            params.rightMargin = -distance;
            params.leftMargin = distance;
            animationView.setLayoutParams(params);
        }

        private void swipeRemove() {
            mainActivity.deleteReminder(getItem(position));
        }
    }
}