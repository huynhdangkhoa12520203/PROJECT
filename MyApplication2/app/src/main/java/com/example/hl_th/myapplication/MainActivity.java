package com.example.hl_th.myapplication;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import adapter.CustomExpandListView;
import adapter.ItemListViewAdapter;
import adapter.ItemSpinnerAdapter;
import entities.AdressDetail;
import entities.Atm;
import entities.BaseItem;
import entities.DistanceAB;
import entities.ItemNavigation;
import server.AsynAtmByType;
import server.AsyncListener;
import server.DataManager;
import utils.GetDirection;
import utils.LocationUtils;
import utils.MyLocation;
import utils.Utils;
import utils.common;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private ListView menulist;
    private ItemListViewAdapter itemAdapter;
    private ArrayList<ItemNavigation> itemNavigationArrayList;
    private LocationUtils locationUtils;
    private GoogleMap mMap;
    private ProgressDialog progressDialog;
    private MyEventListener myEventListener;

    private ExpandableListView expandableListView;
    private CustomExpandListView customExpandListView;
    private ArrayList<String> header_expand = new ArrayList<>();

    private HashMap<String, ArrayList<BaseItem>> item_expand_list = new HashMap<>();

    private DrawerLayout drawer;
    private MyLocation myLocation;
    private Dialog type_vehicles;
    private Dialog type_maps;
    private Dialog type_search_atm;
    private Dialog type_list_atm;
    private Dialog type_setting;
    private Toolbar toolbar;
    private Toolbar toolbar2;
    private NavigationView navigationView;
    private ArrayList<BaseItem> arrayDistrict = new ArrayList<>();
    private ItemSpinnerAdapter spinnerItem;
    private Spinner spinner_city;
    private Spinner spinner_bank;
    private Spinner spinner_km;
    private ArrayList<String> listNameSpinner;

    private ArrayList<BaseItem> arrBank = new ArrayList<>();
    private ItemSpinnerAdapter spinnerItem1;
    private ArrayList<String> listNameSpinner1 = new ArrayList<>();

    private AdressDetail adressDetail;
    private ArrayList<Atm> atmArrayList = new ArrayList<>();
    private ArrayList<Atm> atmNearestYou = new ArrayList<>();
    private ArrayList<Atm> atmByDistrictType = new ArrayList<>();
    private ArrayList<Atm> atmArray = new ArrayList<>();
    public static int changeListExpand = 1;
    private int distance = 1;

    private TextView id_destination;
    private TextView id_time;

    private int imageacb = R.drawable.icon_acb;
    private int imagevietinbank = R.drawable.icon_vietinbank;
    private int imagetech = R.drawable.icon_tecombank;
    private int imagesac = R.drawable.icon_sacombank;
    private String travelMode = "driving";
    private Marker markerSelected;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        initView();
        if (DataManager.getInstance().getAtmDetail() != null) {
            atmArrayList = DataManager.getInstance().getAtmDetail();
        }
        customExpandListView = new CustomExpandListView(this, header_expand, item_expand_list);
        expandableListView.setAdapter(customExpandListView);
        common.initProgress(this);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundResource(R.color.colorToolbar);
        toolbar2.setBackgroundResource(R.color.colorToolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        itemAdapter = new ItemListViewAdapter(this, R.layout.layout_listview_item, itemNavigationArrayList);
        menulist.setAdapter(itemAdapter);
        navigationView.setNavigationItemSelectedListener(this);
        handlerListView();
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.id_fragment_map);
        supportMapFragment.getMapAsync(this);
    }

    /***
     * Khỏi tạo các view cho MainActivity
     */
    public void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar2 = (Toolbar) findViewById(R.id.toolbar2);
        menulist = (ListView) findViewById(R.id.menuList);
        locationUtils = new LocationUtils(this);
        locationUtils.checkInternetConnection();
        spinner_km = (Spinner) findViewById(R.id.id_km);
        id_destination = (TextView) findViewById(R.id.id_destination);
        id_time = (TextView) findViewById(R.id.id_time);
        expandableListView = (ExpandableListView) findViewById(R.id.lvExp);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        initArrayItemNavigation();
        adressDetail = new AdressDetail("Hcm");
        String x = adressDetail.getCurrentCity();
        initSpinnerItem();

        String[] spinner_km_item = {"1 km", "2 kms", "3 kms", "4 kms", "5 kms", "6 kms", "7 kms", "8 kms"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinner_km_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        spinner_km.setAdapter(arrayAdapter);
        spinner_km.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) spinner_km.getSelectedView();
                String convert = textView.getText().charAt(0) + "";
                distance = Integer.parseInt(convert);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    boolean expand = false;

    /***
     * Khởi tạo item cho spinner
     */
    private void initSpinnerItem() {
        int icon_atm = R.drawable.icon_atm_locator;
        int icon_city = R.drawable.city_ico;
        int[] images = {R.drawable.icon_acb, R.drawable.icon_vietinbank, R.drawable.icon_tecombank, R.drawable.icon_sacombank};
        listNameSpinner = new ArrayList<>();
        listNameSpinner1 = new ArrayList<>();

        arrayDistrict = new ArrayList<>();
        arrBank = new ArrayList<>();

        int numberDistrict = DataManager.getInstance().getDistrictDetails().size();
        for (int i = 0; i < numberDistrict; i++) {
            arrayDistrict.add(new BaseItem(DataManager.getInstance().getDistrictDetails().get(i).name, icon_city));
        }
        int numberBanks = DataManager.getInstance().getBankDetail().size();
        for (int i = 0; i < numberBanks; i++) {
            arrBank.add(new BaseItem(DataManager.getInstance().getBankDetail().get(i).name, images[i]));
        }

    }

    /***
     * @param type Chuỗi tên loại ngân hàng cần settup icon
     * @return value kiểu int có trong drawable
     */
    private int setUpImageAtm(String type) {
        if (type.equals("ACB"))
            return imageacb;
        if (type.equals("ViettinBank"))
            return imagevietinbank;
        if (type.equals("TechcomBank"))
            return imagetech;
        if (type.equals("Sacombank"))
            return imagesac;
        return 0;
    }

    /***
     * @param type Chuỗi tên loại ngân hàng cần settup icon
     * @return BitmapDecoription
     */

    private BitmapDescriptor setUpImageAtmSystem(String type) {
        if (type.equals("ACB"))
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        if (type.equals("ViettinBank"))
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
        if (type.equals("TechcomBank"))
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
        if (type.equals("Sacombank"))
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        return null;
    }

    /***
     * @param v Button hiện thực chức năng expandListView
     * @excute Mở rộng danh sách các Atm tìm được
     */

    public void expandAllAtm(View v) {
        initItemExpand();
        customExpandListView.notifyDataSetInvalidated();
        int x = changeListExpand;
        if (expand == false) {
            expandableListView.setVisibility(View.VISIBLE);
            expandableListView.expandGroup(0);
            expandableListView.smoothScrollToPositionFromTop(1, 5, 10000);
            expand = true;
            if (item_expand_list != null) {
                if (changeListExpand == 1) {
                    atmArray = atmArrayList;
                } else if (changeListExpand == 2) {
                    atmArray = atmNearestYou;
                } else if (changeListExpand == 3) {
                    atmArray = atmByDistrictType;
                }
                expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                    @Override
                    public boolean onChildClick(ExpandableListView expandableListView, View view, final int i, final int i1, long l) {
                        expandableListView.setVisibility(View.GONE);
                        LatLng latLng = new LatLng(atmArray.get(i1).getLat(), atmArray.get(i1).getLng());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(latLng)             // Sets the center of the map to location user
                                .zoom(15)                   // Sets the zoom
                                .bearing(90)                // Sets the orientation of the camera to east
                                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                                .build();                   // Creates a CameraPosition from the builder
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        final MarkerOptions option = new MarkerOptions();
                        option.title(atmArray.get(i1).getBanktype());
                        option.snippet(atmArray.get(i1).getAddress());
                        option.position(new LatLng(atmArray.get(i1).getLat(), atmArray.get(i1).getLng()));
                        option.icon(setUpImageAtmSystem(atmArray.get(i1).getBanktype()));
                        final Marker currentMarker = mMap.addMarker(option);
                        currentMarker.showInfoWindow();
                        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick(Marker marker) {
                                Utils.sendAddress(MainActivity.this, atmArray.get(i).getAddress());
                            }
                        });
                        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {

                                markerSelected = marker;
                                return false;
                            }
                        });
                        return false;
                    }
                });
            }
        } else {
            expandableListView.collapseGroup(0);
            expandableListView.setVisibility(View.GONE);
            expand = false;
        }
    }

    /***
     * @param v Button tìm vị trí gần nhất
     * @throws IOException
     */
    public void showATMnearestYou(View v) throws IOException {
        boolean checkLocationOfATM;
        SplashActivity.first_open = false;
        if (expand == true) {
            expand = false;
        }
        if (atmNearestYou.size() > 0) {
            atmNearestYou.clear();
        }
        if (atmArrayList != null || atmArrayList.size() != 0) {
            for (int i = 0; i < atmArrayList.size(); i++) {
                checkLocationOfATM = atmNearYou(new LatLng(atmArrayList.get(i).getLat(), atmArrayList.get(i).getLng()), distance * 1000);
                if (checkLocationOfATM == true) {
                    atmNearestYou.add(atmArrayList.get(i));
                }
            }
        }
        if (atmNearestYou.size() == 0) {

            Toast.makeText(this, "Have no Atm which near your location", Toast.LENGTH_LONG).show();
        }
        changeListExpand = 2;
        if (atmNearestYou.size() > 0) {
            mMap.clear();
            myLocation.showMyLocation();
            showListMarker(atmNearestYou);

        }

    }

    /***
     * @param atms Danh sách các atm cần hiển thị trên bản đồ
     * @throws IOException
     */
    private void showListMarker(final ArrayList<Atm> atms) throws IOException {
        mMap.clear();
        myLocation.showMyLocation();
        for (final Atm atm : atms) {
            final LatLng latLng = new LatLng(atm.getLat(), atm.getLng());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)             // Sets the center of the map to location user
                    .zoom(15)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            final MarkerOptions option = new MarkerOptions();
            option.title(atm.getBanktype());
            option.snippet(atm.getAddress());
            option.position(new LatLng(atm.getLat(), atm.getLng()));
            Drawable drawable = getResources().getDrawable(setUpImageAtm(atm.getBanktype()));
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            option.icon(setUpImageAtmSystem(atm.getBanktype()));
            final Marker currentMarker = mMap.addMarker(option);
            currentMarker.showInfoWindow();
            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    Utils.sendAddress(MainActivity.this, atm.getAddress());
                }
            });
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    markerSelected = marker;
                    return false;
                }
            });
        }
    }

    /***
     * @param latLng      tọa độ cần tính khoản cách với vị trí hiện tại của người dùng
     * @param maxdistance
     * @return
     */
    public boolean atmNearYou(LatLng latLng, int maxdistance) {

        LatLng youtLocation = new LatLng(myLocation.getMyLocation().getLatitude(), myLocation.getMyLocation().getLongitude());
        return DistanceAB.distance(latLng, youtLocation) > maxdistance ? false : true;

    }


    /***
     * Khởi tạo item cho ExpandListView
     */
    private void initItemExpand() {
        item_expand_list.clear();
        header_expand.clear();
        if (SplashActivity.first_open == true) {

            header_expand.add(new String(""));
            ArrayList<BaseItem> arrayList = new ArrayList<>();
            item_expand_list.put(header_expand.get(0), arrayList);
        } else {
            ArrayList<Atm> initListItem = new ArrayList<>();
            ArrayList<BaseItem> arrayList = new ArrayList<>();
            header_expand.add(new String(""));

            switch (changeListExpand) {
                case 1:
                    initListItem = atmArrayList;
                    break;
                case 2:
                    initListItem = atmNearestYou;
                    break;
                case 3:
                    initListItem = atmByDistrictType;
                    break;

            }
            for (int i = 0; i < initListItem.size(); i++) {
                arrayList.add(new BaseItem(initListItem.get(i).getAddress(), setUpImageAtm(initListItem.get(i).getBanktype())));
            }
            item_expand_list.put(header_expand.get(0), arrayList);
        }


    }

    /***
     * Khởi tạo các item cho navigation
     */

    private void initArrayItemNavigation() {
        itemNavigationArrayList = new ArrayList<ItemNavigation>();
        int images[] = {R.drawable.ic_type_vehecle, R.drawable.ic_typemap, R.drawable.ic_list_atm, R.drawable.search_press, R.drawable.camera, R.drawable.setting_icon};
        String[] names = {"Type vehicles", "Type map", "List Atm", "Search", "Capture", "Setting"};
        for (int i = 0; i < images.length; i++) {
            itemNavigationArrayList.add(new ItemNavigation(images[i], names[i]));
        }
    }

    /***
     * @param dialog cần hiển thị
     * @param layout layout hiện thị cho dialog
     * @param name   tên title cho dialog
     * @return
     */

    private Dialog showDialogWithType(Dialog dialog, int layout, String name) {
        dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layout);
        if (name != null || !name.equals("")) {
            dialog.setTitle(name);
        }
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialog.getWindow().setAttributes(lp);
        return dialog;

    }

    private void cancelEventButton(final Dialog dialog, View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog.isShowing())
                    dialog.dismiss();

            }
        });
    }

    private void setTravelMode(final Dialog dialog, View view, final String mode) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                travelMode = mode;
                dialog.dismiss();
            }
        });
    }

    /***
     * Thêm các item cho listview menu bên trái và thực hiện các sử lý liên quan
     */
    private void handlerListView() {
        menulist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                closeExpand();
                switch (i) {
                    case 0: {
                        type_vehicles = showDialogWithType(type_vehicles, R.layout.dialog_type_search, "");
                        Button btn_driving = (Button) type_vehicles.findViewById(R.id.btn_driving);
                        Button btn_transit = (Button) type_vehicles.findViewById(R.id.btn_transit);
                        Button btn_walking = (Button) type_vehicles.findViewById(R.id.btn_walking);
                        Button btn_cancel = (Button) type_vehicles.findViewById(R.id.id_btn_cancel_type_vehicle);


                        setTravelMode(type_vehicles, btn_driving, "driving");
                        setTravelMode(type_vehicles, btn_transit, "transit");
                        setTravelMode(type_vehicles, btn_walking, "walking");

                        cancelEventButton(type_vehicles, btn_cancel);


                        type_vehicles.show();

                        drawer.closeDrawer(GravityCompat.START);
                        break;
                    }
                    case 1:

                        type_maps = showDialogWithType(type_maps, R.layout.dialog_type_maps, "");
                        type_maps.show();
                        Button btn_normal = (Button) type_maps.findViewById(R.id.btn_normal);
                        Button btn_hybred = (Button) type_maps.findViewById(R.id.btn_hybred);
                        Button btn_satellite = (Button) type_maps.findViewById(R.id.btn_satellite);
                        Button btn_terrain = (Button) type_maps.findViewById(R.id.btn_terrain);
                        Button btn_cancel = (Button) type_maps.findViewById(R.id.id_btn_cancel_type_map);
                        cancelEventButton(type_maps, btn_cancel);

                        btn_normal.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                drawer.closeDrawer(GravityCompat.START);
                                type_maps.dismiss();

                            }
                        });
                        btn_hybred.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                drawer.closeDrawer(GravityCompat.START);
                                type_maps.dismiss();

                            }
                        });
                        btn_satellite.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                drawer.closeDrawer(GravityCompat.START);
                                type_maps.dismiss();

                            }
                        });
                        btn_terrain.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                drawer.closeDrawer(GravityCompat.START);
                                type_maps.dismiss();
                            }
                        });
                        break;
                    case 2: {
                        type_list_atm = showDialogWithType(type_list_atm, R.layout.dialog_search_atm, "");
                        spinner_city = (Spinner) type_list_atm.findViewById(R.id.id_city);
                        spinner_bank = (Spinner) type_list_atm.findViewById(R.id.id_bank);
                        spinnerItem = new ItemSpinnerAdapter(getBaseContext(), R.layout.layout_spinner_item, arrayDistrict);
                        spinner_city.setAdapter(spinnerItem);
                        spinnerItem1 = new ItemSpinnerAdapter(getBaseContext(), R.layout.layout_spinner_item, arrBank);
                        spinner_bank.setAdapter(spinnerItem1);
                        type_list_atm.show();
                        Button btn_search = (Button) type_list_atm.findViewById(R.id.id_list_atm__search);
                        btn_search.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                closeExpand();
                                int item1 = spinner_city.getSelectedItemPosition();
                                int item2 = spinner_bank.getSelectedItemPosition();
                                SplashActivity.first_open = false;
                                String district = "\'" + arrayDistrict.get(item1).getName() + "\'";
                                if (district.contains(" ")) {
                                    district = district.replace(" ", "%20");
                                }
                                String bank = "\'" + arrBank.get(item2).getName() + "\'";
                                AsynAtmByType asyncAtm = new AsynAtmByType(new AsyncListener() {
                                    @Override
                                    public void onAsyncComplete() throws IOException {

                                        atmByDistrictType.clear();
                                        if ((ArrayList<Atm>) DataManager.getInstance().getAtmByType() != null) {
                                            atmByDistrictType = (ArrayList<Atm>) DataManager.getInstance().getAtmByType();
                                        }
                                        changeListExpand = 3;
                                        expand = true;
                                        showListMarker(atmByDistrictType);
                                    }
                                });
                                String url = String.format(getString(R.string.url_get_atms_by_district_banktype), district, "&", bank);
                                asyncAtm.execute(String.format(getString(R.string.url_get_atms_by_district_banktype), district, "&", bank));
                                type_list_atm.dismiss();
                                drawer.closeDrawer(GravityCompat.START);

                            }
                        });
                        Button btn_cancel_list = (Button) type_list_atm.findViewById(R.id.id_btn_cancel_searching_by_info);
                        cancelEventButton(type_list_atm, btn_cancel_list);
                        break;
                    }
                    case 3: {

                        type_search_atm = showDialogWithType(type_search_atm, R.layout.dialog_search_atm_advance, "");
                        Button id_btn_cancel_searching = (Button) type_search_atm.findViewById(R.id.id_btn_cancel_searching);
                        cancelEventButton(type_search_atm, id_btn_cancel_searching);
                        type_search_atm.show();
                        break;
                    }
                    case 5: {
                        type_setting = showDialogWithType(type_setting, R.layout.dialog_setting, "");
                        type_setting.show();
                    }

                }
            }
        });
    }

    public void drawLineBetweenLocation(View view) {

        if (markerSelected != null) {
            LatLng latLng1 = new LatLng(markerSelected.getPosition().latitude, markerSelected.getPosition().longitude);
            LatLng latLng2 = new LatLng(myLocation.getMyLocation().getLatitude(), myLocation.getMyLocation().getLongitude());
            new GetDirection(mMap, MainActivity.this, latLng1, latLng2, travelMode, new AsyncListener() {
                @Override
                public void onAsyncComplete() throws IOException {
                    String distance = GetDirection.distance;
                    String time = GetDirection.time;
                    id_destination.setText("" + distance);
                    id_time.setText("" + time);
                }
            }).execute();
        } else
            Toast.makeText(MainActivity.this, "Please choose position before drawing", Toast.LENGTH_LONG).show();

    }

    private void closeExpand() {
        if(expand==true)
        {
            expandableListView.collapseGroup(0);
            expandableListView.setVisibility(View.GONE);
            expand = false;
        }
    }

    @Override
    public void onBackPressed() {

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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.mMap = googleMap;
        myLocation = new MyLocation(this, mMap, locationUtils);
        try {
            myLocation.showMyLocation();
        } catch (IOException e) {
            e.printStackTrace();
        }
        myEventListener = new MyEventListener(this, mMap, myLocation);
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {

                common.dismissProgress();
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                closeExpand();

            }
        });


    }
}
