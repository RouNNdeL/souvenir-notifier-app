package com.roundel.souvenirnotifier.adapters;
/*
 * Created by Krzysiek on 19/07/2017.
 */

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.roundel.souvenirnotifier.R;
import com.roundel.souvenirnotifier.entities.SteamUser;

import java.util.List;

public class SteamUsersAdapter extends RecyclerView.Adapter<SteamUsersAdapter.ViewHolder>
{
    private List<SteamUser> mUsers;
    private Context mContext;
    private OnItemLongClickListener mOnItemLongClickListener;
    private RecyclerView mRecyclerView;

    public SteamUsersAdapter(Context context, List<SteamUser> users)
    {
        this.mContext = context;
        this.mUsers = users;
    }

    public void swapData(List<SteamUser> users)
    {
        this.mUsers = users;
        notifyDataSetChanged();
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener)
    {
        mOnItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        ConstraintLayout content = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_user, parent, false);

        content.setOnLongClickListener(v -> {
            if(mOnItemLongClickListener != null)
            {
                mOnItemLongClickListener.onItemLongClick(mRecyclerView.getChildLayoutPosition(v));
            }
            return true;
        });

        final ViewGroup.LayoutParams lp = content.getLayoutParams();
        lp.width = parent.getWidth();
        //lp.height = parent.getHeight();
        content.setLayoutParams(lp);

        return new ViewHolder(content);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        SteamUser user = mUsers.get(position);

        TextView username = (TextView) holder.itemView.findViewById(R.id.list_text_primary);
        TextView id = (TextView) holder.itemView.findViewById(R.id.list_text_secondary);

        username.setText(user.getUsername());
        id.setText(String.valueOf(user.getSteamId64()));
    }

    @Override
    public int getItemCount()
    {
        return mUsers.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
        this.mRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView)
    {
        super.onDetachedFromRecyclerView(recyclerView);
        this.mRecyclerView = null;
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {

        ViewHolder(View itemView)
        {
            super(itemView);
        }
    }

    public interface OnItemLongClickListener
    {
        void onItemLongClick(int position);
    }
}
