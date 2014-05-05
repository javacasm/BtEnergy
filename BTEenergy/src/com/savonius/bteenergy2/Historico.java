package com.savonius.bteenergy2;

import com.savonius.bteenergy.R;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Historico extends Fragment {
public Historico(){}

@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle 	savedInstanceState)
{
	View view = inflater.inflate(R.layout.historico, container, false);
	
	return view;
	
}
}
