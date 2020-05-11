package com.example.SalinasCrimeMap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.google.android.gms.maps.CameraUpdateFactory.newCameraPosition;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {


    private ImageButton refresh;
    private MapFragment map;
    private ArrayList<CrimeEvent> allCrimeEvents = new ArrayList<CrimeEvent>();
    private HashMap<String, Marker> allMarkers = new HashMap<String, Marker>();
    private GoogleMap googleMap;
    private ProgressBar spinner;
    private Thread extractThread;
    private CrimeEvent tempCrimeEvent;
    private String crimeDetails = "";
    private String mainWebLink = "https://news.salinaspd.com/";
    final private  double latitude = 36.6777;
    final private double longitude = -121.6555;
    private AlertDialog alertDialog = null;

    int navItemId = 0;
    MenuItem navItem = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.mainApp);

//        constraintLayout.setBackgroundColor(Color.parseColor("#ffffff"));//white
        constraintLayout.setBackgroundColor(Color.parseColor("#28004d"));


        refresh = findViewById(R.id.refresh);


        getWebsite();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        spinner = findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);




        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        Log.d("size0", String.valueOf(allCrimeEvents.size()));

        Log.d("size1.1", String.valueOf(allCrimeEvents.size()));

        map = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        map.getMapAsync(this);




    }

    private void getWebsite() {


        extractThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<CrimeEvent> tempAllCrimeEvents = new ArrayList<CrimeEvent>();
                final StringBuilder builder = new StringBuilder();
                try {
                    Log.d("testing1", "--------------");
                    Document doc = Jsoup.connect(mainWebLink).get();
                    Log.d("testing3", "--------------");

                    //String title = doc.title();
                    Elements trs = doc.select("tr");
                    Log.d("testing4", "--------------");


                    //builder.append(title).append("\n");

                    for (Element tr : trs) {

                        Log.d("testing2", "--------------");

                        if (Character.isDigit(tr.text().charAt(0))) {

                            CrimeEvent crimeEvent = new CrimeEvent(MainActivity.this);

                            Elements tds = tr.select("td");
                            String ahref = tds.get(2).select("a").attr("href");

                            Log.d("testing4", "--------------");



                            crimeEvent.setListNumber(tds.get(0).text());
                            crimeEvent.setDate(tds.get(1).text());
                            crimeEvent.setTopic(tds.get(2).text());
                            crimeEvent.setHtmlLink(ahref);

                            crimeEvent.setLocation(tds.get(3).text());

                            tempAllCrimeEvents.add(crimeEvent);

                        }


                    }
                    Log.d("allCrimeEvents1-", String.valueOf(allCrimeEvents.size()));
                    Log.d("tempAllCrimeEvents1", String.valueOf(tempAllCrimeEvents.size()));
                    allCrimeEvents = tempAllCrimeEvents;
                    Log.d("allCrimeEvents2-", String.valueOf(allCrimeEvents.size()));


                } catch (IOException e) {
                    builder.append("Error : ").append(e.getMessage()).append("\n");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //crimeData.setText(builder.toString());


                    }
                });
            }
        });

        extractThread.start();


    }

    public void plotLocation(GoogleMap googleMap, CrimeEvent crimeEvent) {


        GoogleMap gmap = googleMap;
        String loc = crimeEvent.getLocation();;

        if(!crimeEvent.getLocation().equals("")){
            loc = "Salinas CA " + crimeEvent.getLocation();
        }

        String lnum = crimeEvent.getListNumber();
        String top = crimeEvent.getTopic();
        CrimeEvent crime = crimeEvent;

        LatLng address = null;

        address  = crime.getLocationFromAddress( loc);


        if (address != null) {


            MarkerOptions markerOptions = new MarkerOptions().position(address).title(lnum + ". " + top)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                    .visible(true);

            Marker marker = gmap.addMarker(markerOptions);
            marker.showInfoWindow();
            marker.setTag(crimeEvent.getListNumber());
            allMarkers.put((String) marker.getTag(), marker);

        }


    }

    public void parseSpecificCimeEvent(Elements tbodys) {


        for (Element tbody : tbodys) {

            Elements div = tbody.select("div");

            for (Element p : div) {
                Log.d("ps", String.valueOf(p.html()));
                crimeDetails += p.html() + "\n";

            }
        }
    }



    public String getCrimePageInfo(final CrimeEvent crimeEvent){


        Thread extractCrimePageThread = new Thread(new Runnable() {
            @Override
            public void run() {

                crimeDetails = "";

                try {

                    Document doc = Jsoup.connect(mainWebLink + crimeEvent.getHtmlLink()).get();

                    Elements tbodys = doc.select("tbody");

                    parseSpecificCimeEvent(tbodys);


                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });

        extractCrimePageThread.start();
        try {
            extractCrimePageThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /////////////////////////////////////


        return crimeDetails;

    }

    public void showEventInfo(CrimeEvent crimeEvent){

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogTheme);
        alertDialogBuilder.setTitle(crimeEvent.getListNumber() + ". " + crimeEvent.getTopic());



        String crimeInfo = getCrimePageInfo(crimeEvent);

        alertDialogBuilder
                .setMessage(Html.fromHtml(crimeInfo))
                .setCancelable(false)
                .setNegativeButton("Exit",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing\
                        if(alertDialog != null){
                            alertDialog.dismiss();
                        }
                        dialog.cancel();
                    }
                });

        // create alert dialog
        alertDialog = alertDialogBuilder.create();



        // show it
        alertDialog.show();


        int orientation = getResources().getConfiguration().orientation;
        int width = 0;
        int height = 0;

        if(orientation == Configuration.ORIENTATION_PORTRAIT){
            width = (int)(getResources().getDisplayMetrics().widthPixels*1);
            height = (int)(getResources().getDisplayMetrics().heightPixels*0.50);
        }
        else{
            width = (int)(getResources().getDisplayMetrics().widthPixels*1);
            height = (int)(getResources().getDisplayMetrics().heightPixels*.90);

        }



        Window window = alertDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;

        window.setAttributes(wlp);
        window.setBackgroundDrawableResource(R.color.darkerViolet);


        alertDialog.getWindow().setLayout(width, height);


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if(alertDialog != null){
            int orientation = getResources().getConfiguration().orientation;
            int width = 0;
            int height = 0;

            if(orientation == Configuration.ORIENTATION_PORTRAIT){
                width = (int)(getResources().getDisplayMetrics().widthPixels*1);
                height = (int)(getResources().getDisplayMetrics().heightPixels*0.50);
            }
            else{
                width = (int)(getResources().getDisplayMetrics().widthPixels*1);
                height = (int)(getResources().getDisplayMetrics().heightPixels*.90);

            }
            alertDialog.getWindow().setLayout(width, height);
        }
    }


    public void createCrimeEventButton(final CrimeEvent crimeEvent) {

        final String buttonText = crimeEvent.getListNumber() + ". Date: " + crimeEvent.getDate() + "\nTopic: "
                + crimeEvent.getTopic() + "\nLocation: " + crimeEvent.getLocation();

        tempCrimeEvent = new CrimeEvent(crimeEvent.getListNumber(), crimeEvent.getDate(), crimeEvent.getTopic(), crimeEvent.getLocation(),crimeEvent.getHtmlLink());

        final Button myButton = new Button(MainActivity.this);
        myButton.setText(buttonText);
        myButton.setTag(crimeEvent.getListNumber());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            myButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.darkViolet)));
            myButton.setTextColor(getApplication().getResources().getColor(R.color.white));
        }

//        getResources().getColor(R.color.blueViolet)
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("cliked on: ", (String) myButton.getTag());
                Log.d("TempAddress: ", (String) tempCrimeEvent.getLocation());
                Log.d("ActualAddress: ", (String) crimeEvent.getLocation());

                if(alertDialog != null){
                    if(!alertDialog.isShowing()){
                        showEventInfo(crimeEvent);
                    }
                }
                else{
                    showEventInfo(crimeEvent);
                }

                try {
                   if(!crimeEvent.getLocation().equals("")){
                       CameraPosition cameraPosition = new CameraPosition.Builder()
                               .target(crimeEvent.getLocationFromAddress("Salinas CA " + crimeEvent.getLocation()))
                               .zoom(17)
                               .build();

                       CameraUpdate cameraUpdate = newCameraPosition(cameraPosition);

                       googleMap.moveCamera(cameraUpdate);

                       Marker tempMarker = allMarkers.get(crimeEvent.getListNumber());
                       tempMarker.showInfoWindow();

                        Iterator it = allMarkers.entrySet().iterator();

                        while(it.hasNext()){
                            Map.Entry pair = (Map.Entry) it.next();
                            allMarkers.get(pair.getKey()).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                        }


                       tempMarker.setIcon(BitmapDescriptorFactory.defaultMarker());
                   }
               }
               catch (Exception e){
                   e.printStackTrace();
               }

            }
        });

        LinearLayout layout =  findViewById(R.id.buttonList);
        layout.addView(myButton);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        navItemId = item.getItemId();
        navItem = item;
        spinner.setVisibility(View.VISIBLE);


        Log.d("size4", String.valueOf(allCrimeEvents.size()));



        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View view, float v) {

            }

            @Override
            public void onDrawerOpened(@NonNull View view) {

                Log.d("Drawer open", "Drawer Open");
            }

            @Override
            public void onDrawerClosed(@NonNull View view) {



             if (navItemId == R.id.plot1_20) {

                 resetToSalinas();
                    Log.d("plot_1_20", String.valueOf(R.id.plot1_20));
                    getWebsite();//After this, allCrimesEvents arrayList is populated
                    try {
                        extractThread.join();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    plot(0,20);
                    resetToSalinas();
                    setInternetConnectionToast();


                } else if (navItemId == R.id.plot21_40) {
                    Log.d("plot_21_40", String.valueOf(R.id.plot21_40));

                    getWebsite();//After this, allCrimesEvents arrayList is populated
                    plot(20,40);
                    resetToSalinas();
                    setInternetConnectionToast();

                } else if (navItemId == R.id.plot41_60) {

                    getWebsite();//After this, allCrimesEvents arrayList is populated
                    plot(40,60);
                    resetToSalinas();
                    setInternetConnectionToast();

                } else if (navItemId == R.id.plot61_80) {
                    getWebsite();//After this, allCrimesEvents arrayList is populated
                    plot(60,80);
                    resetToSalinas();
                    setInternetConnectionToast();

                } else if (navItemId == R.id.plot81_100) {
                    getWebsite();//After this, allCrimesEvents arrayList is populated
                    plot(81,100);
                    resetToSalinas();
                    setInternetConnectionToast();
                }
                spinner.setVisibility(View.GONE);
                navItemId = 0;
                navItem = null;

            }

            @Override
            public void onDrawerStateChanged(int i) {

            }
        });

        return true;
    }



    public void plot(int start, int finish) {

        Log.d("size2", String.valueOf(allCrimeEvents.size()));

        LinearLayout layout = findViewById(R.id.buttonList);
        layout.removeAllViews();
        googleMap.clear();
        allMarkers = new HashMap<String, Marker>();

        if(allCrimeEvents.size() > 0){

            for (int i = start; i < finish; i++) {
                createCrimeEventButton(allCrimeEvents.get(i));

                plotLocation(googleMap, allCrimeEvents.get(i));

            }
        }
        else {
            boolean connection = setInternetConnectionToast();
            if(connection == true){
                Toast.makeText(this, "Error. UNKNOWN", Toast.LENGTH_SHORT).show();
            }

        }
    }

    public boolean setInternetConnectionToast(){

        boolean connection =checkInternetConnection();
        if(connection == false) {
            Toast.makeText(this, "Error. No internet Connection", Toast.LENGTH_SHORT).show();
            Log.d("Connection Error", "Connection Error");
        }

        return connection;
    }

    public boolean checkInternetConnection(){

        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        }
        else
            connected = false;

        return connected;
    }

    public void resetToSalinas(){




        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude))
                .zoom(11)
                .build();

        CameraUpdate cameraUpdate = newCameraPosition(cameraPosition);

        googleMap.moveCamera(cameraUpdate);



    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {


        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude))
                .zoom(11)
                .build();

        CameraUpdate cameraUpdate = newCameraPosition(cameraPosition);

        googleMap.moveCamera(cameraUpdate);


        this.googleMap = googleMap;

        refresh.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                resetToSalinas();;
            }
        });


        try {
            Log.d("allEventsSize1", String.valueOf(allCrimeEvents.size()));
            extractThread.join();
            Log.d("allEventsSize2", String.valueOf(allCrimeEvents.size()));
            Log.d("size1", String.valueOf(allCrimeEvents.size()));
            plot(0,20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }




        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                spinner.setVisibility(View.VISIBLE);
            }
        });
        navigationView.setNavigationItemSelectedListener(this);

        Log.d("size5", String.valueOf(allCrimeEvents.size()));







    }
}


