package com.unpi.madres.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.unpi.madres.R;
import com.unpi.madres.interfaces.IRecyclerItemClickListener;

public class VolunteerCircleHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView NameVictim, NomorVictim;
    public ImageView StatusVictim;

    public void setiRecyclerItemClickListener(IRecyclerItemClickListener iRecyclerItemClickListener) {
        this.iRecyclerItemClickListener = iRecyclerItemClickListener;
    }

    public IRecyclerItemClickListener iRecyclerItemClickListener;

    public VolunteerCircleHolder(@NonNull View itemView) {
        super(itemView);
        NameVictim = itemView.findViewById(R.id.victimName);
        NomorVictim = itemView.findViewById(R.id.victimNomor);
        StatusVictim = itemView.findViewById(R.id.victimStatus);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        iRecyclerItemClickListener.onItemClickListerer(v, getAdapterPosition());
    }
}