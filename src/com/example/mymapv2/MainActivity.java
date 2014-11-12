package com.example.mymapv2;

import java.io.IOException;
import java.util.List;
 
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
 
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
//import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.MarshalFloat;
import com.example.mymapv2.WS_Enums.*;


public class MainActivity extends FragmentActivity {
 
	//private final String NAMESPACE = "http://gd2.usc.edu:8080/AccidentsResource/services/Accidents?wsdl";
	//private final String URL = "http://gd2.usc.edu:8080/AccidentsResource/services/Accidents?wsdl";
	private final String SOAP_ACTION = "http://gd2.usc.edu:8080/AccidentsResource/services/Accidents?wsdl";
	private final String METHOD_NAME = "accidntsByTypeToday";
	
	public String NAMESPACE ="http://service.acr";
    public String url="";
    public int timeOut = 60000;
    public IWsdl2CodeEvents eventHandler;
    public SoapProtocolVersion soapVersion;
	
    private GoogleMap googleMap;
    private MarkerOptions markerOptions;
    private LatLng latLng;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
 
        SupportMapFragment supportMapFragment = (SupportMapFragment)
        getSupportFragmentManager().findFragmentById(R.id.map);
 
        // Getting a reference to the map
        googleMap = supportMapFragment.getMap();
        googleMap.setMyLocationEnabled(true);
 
        // Getting reference to btn_find of the layout activity_main
        Button btn_find = (Button) findViewById(R.id.btn_find);
        
        Button btn_find1 = (Button) findViewById(R.id.btn_find1); //added this to get reference to traffic button
 
        // Defining button click event listener for the find button
        OnClickListener findClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Getting reference to EditText to get the user input location
                EditText etLocation = (EditText) findViewById(R.id.et_location);
 
                // Getting user input location
                String location = etLocation.getText().toString();
                new AccidentsTask().execute("");
                if(location!=null && !location.equals("")){
                    new GeocoderTask().execute(location);
                }
            }
        };
        
        OnClickListener trafficListener = new OnClickListener() { //a findClickListener for the traffic button
            @Override
            public void onClick(View v) {
                    new AccidentsTask().execute();
                }
        };
        
 
        // Setting button click event listener for the find button
        btn_find.setOnClickListener(findClickListener);
        
        btn_find1.setOnClickListener(trafficListener);
 
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.haha, menu);
        return true;
    }
    
    //List<Address> addresses = null;
    
    /*public String accidntsByTypeToday(String accidentType){
        return accidntsByTypeToday(accidentType, null);
    }*/
    
    public String accidntsByTypeToday(String accidentType)
    //,List<HeaderProperty> headers)
    {
        SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        soapEnvelope.implicitTypes = true;
        soapEnvelope.dotNet = true;
        SoapObject soapReq = new SoapObject("http://service.acr","accidntsByTypeToday");
        soapReq.addProperty("accidentType",accidentType);
        soapEnvelope.setOutputSoapObject(soapReq);
        HttpTransportSE httpTransport = new HttpTransportSE(url);
        try{
            //if (headers!=null){
            //    httpTransport.call("urn:accidntsByTypeToday", soapEnvelope,headers);
            //}else{
                httpTransport.call("urn:accidntsByTypeToday", soapEnvelope);
            //}
            Object retObj = soapEnvelope.bodyIn;
            if (retObj instanceof SoapFault){
                SoapFault fault = (SoapFault)retObj;
                Exception ex = new Exception(fault.faultstring);
                if (eventHandler != null)
                    eventHandler.Wsdl2CodeFinishedWithException(ex);
            }else{
                SoapObject result=(SoapObject)retObj;
                if (result.getPropertyCount() > 0){
                    Object obj = result.getProperty(0);
                    if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                        SoapPrimitive j =(SoapPrimitive) obj;
                        String resultVariable = j.toString();
                        return resultVariable;
                    }else if (obj!= null && obj instanceof String){
                        String resultVariable = (String) obj;
                        return resultVariable;
                    }
                }
            }
        }catch (Exception e) {
            if (eventHandler != null)
                eventHandler.Wsdl2CodeFinishedWithException(e);
            e.printStackTrace();
        }
        return "";
    }
 
    // An AsyncTask class for accessing the GeoCoding Web Service
    private class GeocoderTask extends AsyncTask<String, Void, List<Address>>{
 
        @Override
        protected List<Address> doInBackground(String... locationName) {
        	//call the accidntsByTypeToday method in this doInBackground part
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> addresses = null;
 
            try {
                // Getting a maximum of 5 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 5);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }
        
        @Override
        protected void onPostExecute(List<Address> addresses) {
 
            if(addresses==null || addresses.size()==0){
                Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
            }
 
            // Clears all the existing markers on the map
            googleMap.clear();
 
            // Adding Markers on Google Map for each matching address
            for(int i=0;i<addresses.size();i++){
 
                Address address = (Address) addresses.get(i);
 
                // Creating an instance of GeoPoint, to display in Google Map
                latLng = new LatLng(address.getLatitude(), address.getLongitude());
 
                String addressText = String.format("%s, %s",
                address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                address.getCountryName());
 
                markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(addressText);
 
                googleMap.addMarker(markerOptions);
 
                // Locate the first location
                if(i==0)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        }
    }
    
    private class AccidentsTask extends AsyncTask<String, Void, List<String>>{
    	
    	@SuppressWarnings("null")
		@Override
		protected List<String> doInBackground(String... returnVal) {
    		List<String> accidents = null;
    		//try {
    			accidents.add(accidntsByTypeToday("accidentType")); 
    		//}
            /*} catch (IOException e) {
                e.printStackTrace();
            }*/
			return accidents;
		}
 
        protected void onPostExecute(List<String> accidents) {
        	for(int i=0; i < accidents.size(); i++)
        	{
        		System.out.println(accidents.get(i));        		
        	}
        	
            
        }
 
        @Override
        protected void onPreExecute() {
            
            Toast.makeText(getBaseContext(), "Calculating...", Toast.LENGTH_SHORT).show();
        }

		
 
      
        
    }
}
