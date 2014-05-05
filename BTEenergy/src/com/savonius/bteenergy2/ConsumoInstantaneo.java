package com.savonius.bteenergy2;

import com.savonius.bteenergy.R;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ConsumoInstantaneo extends Fragment {

	public ConsumoInstantaneo(){}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle 	savedInstanceState)
	{
		View view = inflater.inflate(R.layout.consumo_instantaneo, container, false);
		
		return view;
		
	}
}
