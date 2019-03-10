package com.example.nanyu.faceyou.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.nanyu.faceyou.MyFace;
import com.example.nanyu.faceyou.R;

import java.util.List;

public class FaceAdapter extends RecyclerView.Adapter<FaceAdapter.ViewHolder> {

    private List<MyFace> myFaceList;

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView faceName;
        TextView number;

        public ViewHolder(View view){
            super(view);

            faceName = view.findViewById(R.id.face_name_1);
            number = view.findViewById(R.id.number);
        }

    }

    public FaceAdapter(List<MyFace> myFaceList) {
        this.myFaceList = myFaceList;
    }


    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.face_item,parent,false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder( ViewHolder holder, int position) {

        MyFace myFace = myFaceList.get(position);
        holder.faceName.setText(myFace.getName());
        holder.number.setText(myFace.getNumber());
    }



    @Override
    public int getItemCount() {
        return myFaceList.size();
    }
}
