package com.savonius.bteenergy2;


import com.savonius.bteenergy.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Comparativa  extends Fragment{

	public Comparativa(){}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle 	savedInstanceState)
	{
		View view = inflater.inflate(R.layout.comparativa, container, false);
		
		return view;
		
	}
}
