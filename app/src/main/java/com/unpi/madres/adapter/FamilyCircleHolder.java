package com.unpi.madres.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import com.unpi.madres.R;
import com.unpi.madres.interfaces.IRecyclerItemClickListener;

public class FamilyCircleHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView NameVictim, NomorVictim;

    public void setiRecyclerItemClickListener(IRecyclerItemClickListener iRecyclerItemClickListener) {
        this.iRecyclerItemClickListener = iRecyclerItemClickListener;
    }

    public IRecyclerItemClickListener iRecyclerItemClickListener;

    public FamilyCircleHolder(@NonNull View itemView) {
        super(itemView);
        NameVictim = itemView.findViewById(R.id.familyName);
        NomorVictim = itemView.findViewById(R.id.familyNomor);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        iRecyclerItemClickListener.onItemClickListerer(v, getAdapterPosition());
    }
}