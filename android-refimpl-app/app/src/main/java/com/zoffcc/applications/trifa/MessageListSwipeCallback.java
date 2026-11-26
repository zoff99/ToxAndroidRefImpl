package com.zoffcc.applications.trifa;

import android.graphics.Canvas;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import static com.zoffcc.applications.trifa.TRIFAGlobals.SWIPE_ICON_ANIM_MS;
import static com.zoffcc.applications.trifa.TRIFAGlobals.SWIPE_ICON_ARMED_SCALE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.SWIPE_REPLY_THRESHOLD;

/**
 * swipe-to-quote for the friend, group and conference message lists.
 *
 * swipe a text message (incoming or our own) to the right: a blue reveal
 * layer with a reply arrow appears behind the row. once the swipe passes
 * the commit threshold the arrow lights up and grows a little ("release to
 * quote").
 * releasing then quotes the message into the input box and the row snaps
 * back into place. the row is NEVER removed, so unlike the friendlist
 * pin/unpin swipe there is no remove/reinsert here: ItemTouchHelper animates
 * the row back and clearView() resets all visuals, which is what prevents
 * any swipe background from getting stuck.
 *
 * the same reveal-layer pattern as the friendlist swipe is used: only the
 * row content (msg_swipe_surface) is translated, the reveal layer behind it
 * stays in place. all visuals are view properties, nothing is canvas-painted.
 */
public class MessageListSwipeCallback extends ItemTouchHelper.SimpleCallback
{
    private static final String TAG = "trifa.MsgSwipeCallback";

    // the fragment supplies these three hooks so this one class can serve
    // all three message lists (friend, group, conference).
    public interface SwipeQuoteHandler
    {
        // is the row at this position swipeable? (incoming text + no selection mode)
        boolean can_swipe(int position);

        // stable id of the item at this position, or -1. used to verify at
        // commit time that a concurrent list rebuild did not swap another
        // message into this row while the user was swiping.
        long item_id(int position);

        // quote (reply to) the message at this position.
        void quote_message(int position);
    }

    private final SwipeQuoteHandler handler;

    // id of the message the user started swiping, for the identity guard
    private long swipe_started_id = -1;

    // "armed" = the swipe has passed the commit threshold. the reply icon
    // lights up while armed and calms down below the threshold. remembered
    // so the color/scale animation triggers only on the crossing, not every frame.
    private boolean swipe_armed = false;

    public MessageListSwipeCallback(SwipeQuoteHandler handler)
    {
        super(0, ItemTouchHelper.RIGHT); // no drag-move, swipe right only
        this.handler = handler;
    }

    // the row types that carry plain text and can be swipe-quoted:
    // incoming text and our own outgoing text (both read states).
    // file transfers, paging rows and error rows are NOT swipeable.
    public static boolean is_swipeable_text_type(int view_type)
    {
        return (view_type == Message_model.TEXT_INCOMING_NOT_READ)
               || (view_type == Message_model.TEXT_OUTGOING_NOT_READ)
               || (view_type == Message_model.TEXT_OUTGOING_HAVE_READ);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target)
    {
        return false;
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder)
    {
        return SWIPE_REPLY_THRESHOLD;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder)
    {
        int position = viewHolder.getBindingAdapterPosition();
        if (position != RecyclerView.NO_POSITION)
        {
            try
            {
                if (handler.can_swipe(position))
                {
                    return makeMovementFlags(0, ItemTouchHelper.RIGHT);
                }
            }
            catch (Exception e)
            {
                Log.i(TAG, "getMovementFlags:" + e.getMessage());
            }
        }
        // not swipeable: file transfers, paging rows, error rows,
        // selection mode, ...
        return makeMovementFlags(0, 0);
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState)
    {
        super.onSelectedChanged(viewHolder, actionState);
        if ((actionState == ItemTouchHelper.ACTION_STATE_SWIPE) && (viewHolder != null))
        {
            int position = viewHolder.getBindingAdapterPosition();
            swipe_started_id = -1;
            if (position != RecyclerView.NO_POSITION)
            {
                try
                {
                    swipe_started_id = handler.item_id(position);
                }
                catch (Exception e)
                {
                    Log.i(TAG, "onSelectedChanged:" + e.getMessage());
                }
            }
        }
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int swipeDir)
    {
        int position = viewHolder.getBindingAdapterPosition();
        if (position != RecyclerView.NO_POSITION)
        {
            try
            {
                // only quote if this row still shows the message the user
                // started swiping (a rebuild may have moved things around)
                if (handler.item_id(position) == swipe_started_id)
                {
                    handler.quote_message(position);
                }
            }
            catch (Exception e)
            {
                Log.i(TAG, "onSwiped:" + e.getMessage());
            }
        }
        // the row is NOT removed: ItemTouchHelper animates it back into place
        // and then calls clearView(), which resets all reveal visuals.
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive)
    {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE)
        {
            // move only the row content; the reveal layer behind it stays in place.
            // do NOT call super here: it would move the whole row, reveal included.
            apply_swipe_visuals(viewHolder.itemView, dX);
        }
        else
        {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder)
    {
        super.clearView(recyclerView, viewHolder);
        // always leave the row in its normal look when the swipe is over
        reset_swipe_visuals(viewHolder.itemView);
    }

    // move the row content with the finger and show the reply reveal behind it.
    // once the swipe passes the commit threshold the icon "arms": bright white
    // + gentle grow ("release to quote"). going back under calms it down again.
    private void apply_swipe_visuals(View row, float dX)
    {
        View surface = row.findViewById(R.id.msg_swipe_surface);
        View reveal = row.findViewById(R.id.swipe_reveal);
        if ((surface == null) || (reveal == null))
        {
            return;
        }

        surface.setTranslationX(dX);

        ImageView reply_icon = (ImageView) row.findViewById(R.id.swipe_reveal_reply_icon);

        if (dX <= 0)
        {
            reset_swipe_visuals(row);
            return;
        }

        reveal.setBackgroundResource(R.drawable.swipe_reveal_reply_bg);
        if (reply_icon != null)
        {
            reply_icon.setVisibility(View.VISIBLE);
        }

        // has the swipe crossed the commit threshold?
        boolean armed = false;
        if (row.getWidth() > 0)
        {
            armed = (dX >= (row.getWidth() * SWIPE_REPLY_THRESHOLD));
        }

        // only animate on the actual crossing, not on every draw frame
        if (armed != swipe_armed)
        {
            swipe_armed = armed;
            if (reply_icon != null)
            {
                int color = getResources_color(row, R.color.swipe_icon_idle);
                float scale = 1.0f;
                if (armed)
                {
                    color = getResources_color(row, R.color.swipe_reply_icon_armed);
                    scale = SWIPE_ICON_ARMED_SCALE;
                }
                reply_icon.setColorFilter(color);
                reply_icon.animate().scaleX(scale).scaleY(scale).setDuration(SWIPE_ICON_ANIM_MS).start();
            }
        }
    }

    // calm idle look for the reply icon: soft white, normal size.
    // also cancels a grow/shrink animation that might still be running.
    private void set_icon_idle(ImageView icon)
    {
        if (icon != null)
        {
            icon.animate().cancel();
            icon.setColorFilter(getResources_color(icon, R.color.swipe_icon_idle));
            icon.setScaleX(1.0f);
            icon.setScaleY(1.0f);
        }
    }

    // put the row back to its normal look: content in place, reveal hidden
    private void reset_swipe_visuals(View row)
    {
        View surface = row.findViewById(R.id.msg_swipe_surface);
        if (surface != null)
        {
            surface.setTranslationX(0f);
        }
        View reveal = row.findViewById(R.id.swipe_reveal);
        if (reveal != null)
        {
            reveal.setBackground(null);
        }
        ImageView reply_icon = (ImageView) row.findViewById(R.id.swipe_reveal_reply_icon);
        set_icon_idle(reply_icon);
        if (reply_icon != null)
        {
            reply_icon.setVisibility(View.GONE);
        }
        swipe_armed = false;
    }

    // small helper so this class does not need a Context of its own
    private static int getResources_color(View v, int color_res_id)
    {
        return v.getResources().getColor(color_res_id);
    }
}
