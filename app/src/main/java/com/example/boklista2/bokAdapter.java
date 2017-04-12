package com.example.boklista2;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class bokAdapter extends ArrayAdapter<Bok>{


    private static final String LOG_TAG = MainActivity.class.getName();

    public bokAdapter(Context context, ArrayList<Bok> bocker) {
        super(context, 0, bocker);



    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent ){

        View listItemView = convertView;
        if (listItemView ==null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.bok_list_item, parent, false);
        }

        Bok nuvarandeBok = getItem(position);

        //set title text
        TextView TitleView = (TextView) listItemView.findViewById(R.id.titel);
        String titel = nuvarandeBok.getTitle();
        TitleView.setText(titel);

        //set authors text
        TextView authorView = (TextView) listItemView.findViewById(R.id.forfattare);
        String forfattare = nuvarandeBok.generateStringOfAuthor();
        authorView.setText(forfattare);

        //set discription text
        TextView discriptionView = (TextView) listItemView.findViewById(R.id.sammanfattning);
        String sammanfattning = nuvarandeBok.getSammanfattning();
        discriptionView.setText(sammanfattning);







        return listItemView;


    }




}