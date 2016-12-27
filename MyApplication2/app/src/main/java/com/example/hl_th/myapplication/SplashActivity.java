package com.example.hl_th.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.List;

import entities.Atm;
import server.AsynDistrict;
import server.AsyncAtm;
import server.AsyncBank;
import server.AsyncListener;
import server.DataManager;
import utils.Utils;


public class SplashActivity extends AppCompatActivity {

    private static final int INTERNET_CONNECTION = 1;
    private static final int GPS_CONNECTION = 2;
    public static boolean first_open = true;
    boolean is_network_available=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        try {
            SplashActivity.this.getSupportActionBar().hide();
        } catch (Exception ex) {
            Toast.makeText(SplashActivity.this, "Error Action bar. Try again please!", Toast.LENGTH_LONG).show();
        }

        Utils.activity = this;
        try
        {
            is_network_available=Utils.isNetworkAvailable(this);
        }catch (Exception ex)
        {
            Toast.makeText(SplashActivity.this, "Error System! Check connection again", Toast.LENGTH_LONG).show();
        }

        if (is_network_available) {
            checkGps();
        } else {
            //Show message about Internet problem and allow user config connection
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setMessage("Please connect Internet!");
            dlgAlert.setTitle("Connection is fail!");
            dlgAlert.setPositiveButton("Configuration", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //
                    startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), INTERNET_CONNECTION);
                }
            });
            dlgAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            dlgAlert.create().show();
        }
    }

    /***
     * Nhận kết quả trả về từ quá trình cài đặt
     *
     * @param requestCode Mã code gắn với Intent
     * @param resultCode  Mã code để check Intent trả về
     * @param data        Dữ liệu trả về
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTERNET_CONNECTION) {
            if (!Utils.isNetworkAvailable(this)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // waiting for connection in 5s
                        boolean hasConnection;
                        int count = 0;
                        do {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                            }
                            hasConnection = Utils.isNetworkAvailable(SplashActivity.this);
                        } while (!hasConnection && count++ < 5);
                        // app is being continued ...
                        final boolean finalResult = hasConnection;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (finalResult) {
                                    checkGps();
                                } else {
                                    showExitDialog("Connection Fail!", "Please connect Internet!");
                                }
                            }
                        });
                    }
                }).start();
            } else {
                checkGps();
            }
        } else if (requestCode == GPS_CONNECTION) {
            if (!Utils.isGPSEnabled(this)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // waiting for connection in 5s
                        boolean hasConnection;
                        int count = 0;
                        do {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                            }
                            hasConnection = Utils.isGPSEnabled(SplashActivity.this);
                        } while (!hasConnection && count++ < 5);
                        // app is being continued ...
                        final boolean finalResult = hasConnection;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (finalResult) {
                                    startApp();
                                } else {
                                    showExitDialog("Fail connection !", "Please connect GPS!");
                                }
                            }
                        });
                    }
                }).start();
            } else {
                startApp();
            }
        }
    }

    /***
     * @param title   Tên title message
     * @param message Nội dung message
     */
    private void showExitDialog(String title, String message) {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage(message);
        dlgAlert.setTitle(title);
        dlgAlert.setPositiveButton("Config", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        dlgAlert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        dlgAlert.create().show();
    }

    /***
     * Kiểm tra Gps của điện thoại đã mở hay chưa
     */
    private void checkGps() {
        if (Utils.checkJps()) {

            try
            {
                startApp();
            }
            catch (Exception ex)
            {
                Toast.makeText(SplashActivity.this, "Error connection. Try again please!", Toast.LENGTH_LONG).show();
            }
        } else {
            //Show message about GPS problem and exit this app
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setMessage("Please connect GPS!");
            dlgAlert.setTitle("Fail connection GPS ");
            dlgAlert.setPositiveButton("Configuration", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), GPS_CONNECTION);
                }
            });
            dlgAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            dlgAlert.create().show();
        }
    }

    /***
     * Thực hiện các request lên server để get các atm đã được lưu
     */

    private void startApp() {

        try {
            AsyncBank asyncBank = new AsyncBank(new AsyncListener() {
                @Override
                public void onAsyncComplete() {
                    String url = "http://jobmaps.tk/android_connect/get_all_atm.php";
                    AsyncAtm asyncAtm = new AsyncAtm(new AsyncListener() {
                        @Override
                        public void onAsyncComplete() {
                            AsynDistrict asynDistrict = new AsynDistrict(new AsyncListener() {
                                @Override
                                public void onAsyncComplete() {

                                    List<Atm> atms = DataManager.getInstance().getAtmDetail();
                                    if (atms != null && atms.size() > 0) {
                                        //Go to Map activity
                                        Toast.makeText(SplashActivity.this, "Download data successfully", Toast.LENGTH_LONG).show();

                                    } else {
                                        Toast.makeText(SplashActivity.this, "Download data is fail! Checking and Try again", Toast.LENGTH_LONG).show();

                                    }
                                    Intent i = new Intent(SplashActivity.this, MainActivity.class);
                                    startActivity(i);
                                    finish();
                                }
                            });
                            asynDistrict.execute("http://jobmaps.tk/android_connect/get_all_district.php");

                        }
                    });
                    asyncAtm.execute(url);

                }
            });
            asyncBank.execute("http://jobmaps.tk/android_connect/get_all_bank.php");

        } catch (Exception ex) {
            Toast.makeText(SplashActivity.this, "Error System. Try again please!", Toast.LENGTH_LONG).show();
        }


    }
}
