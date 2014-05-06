package com.savonius.bteenergy2;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
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


    String sDEVICEID="99"; // 75 Miguel,  99 Javacasm
    
    String sERROR="ERROR";
	String sMAX_POWER_COMMAND="M";
	String sMIN_POWER_COMMAND="m";
	String sCURRENT_POWER_COMMAND="c";
	String sREAD_HISTORICO_COMMAND="r";
	String sSET_TIME_COMMAND="T";
	String sRESET_COMMAND="R";
	String sDELETE_SD_COMMAND="d";
	byte [] bMAX_POWER_COMMAND=sMAX_POWER_COMMAND.getBytes();
	byte [] bMIN_POWER_COMMAND=sMIN_POWER_COMMAND.getBytes();
	byte [] bCURRENT_POWER_COMMAND=sCURRENT_POWER_COMMAND.getBytes();
	byte [] bSET_TIME_COMMAND=sSET_TIME_COMMAND.getBytes();
	byte [] bRESET_COMMAND=sRESET_COMMAND.getBytes();
	byte [] bDELETE_SD_COMMAND=sDELETE_SD_COMMAND.getBytes();
	byte [] bREAD_HISTORICO_COMMAND=sREAD_HISTORICO_COMMAND.getBytes();

	
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothSocket btSocket;
	private ConnectAsyncTask connectAsyncTask;
    
	Number []consumos=new Number[10000];
	
	XYPlot plot;
	static boolean bReadingValues=false;
	public void callAsynchronousTask() {
	    final Handler handler = new Handler();
	    Timer timer = new Timer();
	    TimerTask doAsynchronousTask = new TimerTask() {       
	        @Override
	        public void run() {
	            handler.post(new Runnable() {
	                public void run() {       
	                    try {
	                   //    PerformBackgroundTask performBackgroundTask = new PerformBackgroundTask();
	                        // PerformBackgroundTask this class is the class that extends AsynchTask 
	                     //   performBackgroundTask.execute();
	                        if (bReadingValues)
	                     	    return;
	                     	bReadingValues=true;
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
		                        		
		                        		
		                        		Log.i("btEnergy","getData:"+iValor);
		                        		if(plot!=null && ((iContador%5)==0))
		                        		{
		                        			Log.i("BTEnergy","Drawing");
		                        			plot.clear();
		                        		
		                        			XYSeries series1 = new SimpleXYSeries(Arrays.asList(consumos),
		                        				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, 
		                        				"Consumos");
		                        		
		                        			LineAndPointFormatter series1Format = new LineAndPointFormatter();
		                        			PointLabelFormatter plf=new PointLabelFormatter();
		                        			series1Format.setPointLabelFormatter(plf);
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
					bReadingValues=false;
	                }
	                
	            });
	        }
	    };
	    timer.schedule(doAsynchronousTask, 0, 1000); //execute in every 50000 ms
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
	int iMaxValue=0;
	int iMinValue=0;
	
	void resetData()
	{
	  iMaxValue=0;
	  iMinValue=0;
	  consumos=new Number[10000];
	  iContador=0;
	  if(plot!=null)
		  plot.clear();
	}
	
	int iContador=0;
	int getDatos()
	{
		int iValor=-1;
		if(btSocket!=null && btSocket.isConnected()){
			try {
				InputStream  mmInputStream=btSocket.getInputStream();
				espera(100);
				BufferedReader br=new BufferedReader(new InputStreamReader(mmInputStream));
				StringBuffer sb=new StringBuffer();
				while(br.ready())
				{
					String sLine=br.readLine();
					sb.append(sLine);
					if(sLine.startsWith(sMAX_POWER_COMMAND))
					{
						iMaxValue=Integer.parseInt(sLine.substring(1));
					}
					else
					if(sLine.startsWith(sMIN_POWER_COMMAND))
					{
						iMinValue=Integer.parseInt(sLine.substring(1));
					}				
					else
					{
						iValor=Integer.parseInt(sLine);
						espera(100);
						if(iValor!=-1)
							consumos[iContador++]=iValor;
						if(iContador>=consumos.length)
							iContador=0;
					}
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
			if(device.getAddress().endsWith(sDEVICEID))
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
                mTitle = getString(R.string.title_consumo);
                break;
            case 2:
                mTitle = getString(R.string.title_historico);
                break;
            case 3:
                mTitle = getString(R.string.title_comparativa);
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
        boolean bValor=true;
        
        int id = item.getItemId();
        switch(id)
        {
         case R.id.action_connect:
        
        	
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
        	break;
        	case R.id.action_reset:
        		if(btSocket!=null && btSocket.isConnected())
        		{
        			enviaComando(bRESET_COMMAND );
        			resetData();
        		}
        	    break;
        	case R.id.action_historico:
        	    {
        	    	enviaComando(bREAD_HISTORICO_COMMAND);
        	    	readHistorico();
        	    }
        	 case R.id.action_settime:
        	    {
        	    String sTime="T"+Long.toString(System.currentTimeMillis()/1000)+"t";
		
				enviaComando(sTime.getBytes(Charset.forName("ISO-8859-1")));
        	    
        	    }
        	default:
        		bValor=super.onOptionsItemSelected(item);
        		break;
            
        }
        	
        return bValor;
    }


	void readHistorico()
	{
		InputStream  mmInputStream=null;
		try
		{
			mmInputStream=btSocket.getInputStream();
			espera(100);
			BufferedReader br=new BufferedReader(new InputStreamReader(mmInputStream));
			StringBuffer sb=new StringBuffer();
			while(br.ready())
			{
				String sLine=br.readLine();
				sb.append(sLine);
				
				
					espera(100);
			
			}
			
			
		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		finally
		{
			try {
				if(mmInputStream!=null)
			
					mmInputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

    void enviaComando(byte[] bytes)
    {
    	if(btSocket==null)
    	{
    		Log.i("BTEnergy","Null btSocket trying to send command");
    		return;
    	}
	    OutputStream mmOutStream;
		try {
			mmOutStream = btSocket.getOutputStream();
			mmOutStream.write(bytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
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
