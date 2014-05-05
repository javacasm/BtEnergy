package com.savonius.bteenergy2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;

public class PerformBackgroundTask extends AsyncTask<BluetoothSocket, Integer, Integer> {

	public BluetoothSocket btSocket;
	
	void espera(long pausa)
	{
		try {
			Thread.sleep(pausa);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	protected Integer doInBackground(BluetoothSocket... params) {
		int iValor=-1;
		if(btSocket!=null && btSocket.isConnected()){
			try {
				OutputStream  mmOutStream = btSocket.getOutputStream();
			

			//mmOutStream.write(bytes);
			InputStream  mmInputStream=btSocket.getInputStream();
			espera(100);
			BufferedReader br=new BufferedReader(new InputStreamReader(mmInputStream));
			StringBuffer sb=new StringBuffer();
			while(br.ready())
			{
				sb.append(br.readLine());
				espera(100);
			}
			String scadena=sb.toString();
			iValor=Integer.parseInt(scadena);//.replace("error opening datalog.csv","");
		
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
		
		
		return iValor;
	}

}
