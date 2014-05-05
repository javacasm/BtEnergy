package com.savonius.bteenergy2;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.savonius.bteenergy.R;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

	
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothSocket btSocket;
	private ConnectAsyncTask connectAsyncTask;
    
	Number []consumos=new Number[10000];
	
	XYPlot plot;
	public void callAsynchronousTask() {
	    final Handler handler = new Handler();
	    Timer timer = new Timer();
	    TimerTask doAsynchronousTask = new TimerTask() {       
	        @Override
	        public void run() {
	            handler.post(new Runnable() {
	                public void run() {       
	                    try {
	                       PerformBackgroundTask performBackgroundTask = new PerformBackgroundTask();
	                        // PerformBackgroundTask this class is the class that extends AsynchTask 
	                        performBackgroundTask.execute();
	                        if(btSocket!=null)
	                        {
	                        //	performBackgroundTask.btSocket=btSocket;
	                        	if(btSocket.isConnected())
	                        	{
	                        		int iValor=getDatos();

		                				if(tvConsumo==null)
		                				{
		                					tvConsumo=(TextView)findViewById(R.id.txtvConsumo);
		                				}
		                				if(tvConsumo!=null && iValor!=-1)
		                					tvConsumo.setText(Integer.toString(iValor));
		                				
		                				if(plot==null)
		                					plot=(XYPlot)findViewById(R.id.mySimpleXYPlot);
		                        		
		                        		
		                        		Log.i("btEnergy","getData");
		                        		if(plot!=null)
		                        		{
		                        			plot.clear();
		                        		
		                        			XYSeries series1 = new SimpleXYSeries(Arrays.asList(consumos),
		                        				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, 
		                        				"Consumos");
		                        		
		                        			LineAndPointFormatter series1Format = new LineAndPointFormatter();
		                        			series1Format.setPointLabelFormatter(new PointLabelFormatter());
		                        			series1Format.configure(getApplicationContext(),R.xml.pfl1);
	
		                                // add a new series' to the xyplot:
		                        			plot.addSeries(series1, series1Format);
		                        			plot.redraw();
		                        		}
	                        		
	                        	}
	                        }
	                    } catch (Exception e) {
	                        Log.d("btEnergy",e.getMessage());
	                    }
	                }
	            });
	        }
	    };
	    timer.schedule(doAsynchronousTask, 0, 500); //execute in every 50000 ms
	}
	
	void espera(long pausa)
	{
		try {
			Thread.sleep(pausa);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	int iContador=0;
	int getDatos()
	{
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
				String sLine=br.readLine();
				sb.append(sLine);
				iValor=Integer.parseInt(sLine);
				espera(100);
				if(iValor!=-1)
					consumos[iContador++]=iValor;
				if(iContador>=consumos.length)
					iContador=0;
			}
			
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
		
		
		return iValor;
	}

	
	
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    Fragment comparatiFragment=new Comparativa();
    Fragment historicoFragment=new Historico();
    Fragment consumoFragment=new ConsumoInstantaneo();
    BluetoothDevice btDevice;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        
       initBluetooth();
       
    }

    private void initBluetooth()
    {
    // Init Bluetooth
    connectAsyncTask = new ConnectAsyncTask();
	
	//Get Bluettoth Adapter
	mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	
	// Check smartphone support Bluetooth
	if(mBluetoothAdapter == null){
		//Device does not support Bluetooth
		Toast.makeText(getApplicationContext(), "Not support bluetooth", 5).show();
		//finish();
		return;
	}
	
	// Check Bluetooth enabled
	if(!mBluetoothAdapter.isEnabled()){
		Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(enableBtIntent, 1);
	}
	
	
	// Queryng paried devices
	Set<BluetoothDevice> pariedDevices = mBluetoothAdapter.getBondedDevices();
	if(pariedDevices.size() > 0){
		for(BluetoothDevice device : pariedDevices){
			//if(device.getAddress().endsWith("75"))
			if(device.getAddress().endsWith("99"))
				btDevice=device;
		}
	}
	
    }

    TextView tvConsumo;
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction trans=fragmentManager.beginTransaction();
        switch(position)
        {
        case 0:
            trans.replace(R.id.container, consumoFragment);	
            break;
        case 1:
        	trans.replace(R.id.container,historicoFragment);
        	break;
        case 2:
        	trans.replace(R.id.container, comparatiFragment);
        	break;
        }

        trans.commit();
        
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
        	
        	if(btDevice!=null)
        	{
        		initBluetooth();
        		connectAsyncTask.execute(btDevice);
        	}
        	else
        	{
        		if(btSocket!=null)
					try {
						btSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        		finally
        		{
        			btSocket=null;
        			btDevice=null;
        		}
        	}
        	
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
    
    
	private class ConnectAsyncTask extends AsyncTask<BluetoothDevice, Integer, BluetoothSocket>{

		private BluetoothSocket mmSocket;
		private BluetoothDevice mmDevice;
		
		@Override
		protected BluetoothSocket doInBackground(BluetoothDevice... device) {
							
			mmDevice = device[0];
			
			try {
				
				String mmUUID = "00001101-0000-1000-8000-00805F9B34FB";
				mmSocket = mmDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(mmUUID));
				mmSocket.connect();
				
			} catch (Exception e) { }
			
			return mmSocket;
		}

		@Override
		protected void onPostExecute(BluetoothSocket result) {
			
			if(result!=null && result.isConnected())
			{
			btSocket = result;
			
			//Enable Button
			callAsynchronousTask();
			}
			
		}

	}
}
