package com.example.sspulibrary;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.sspulibrary.R;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.handheld.uhfr.UHFRManager;
import com.uhf.api.cls.Reader;

import java.util.Set;



//


//
public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    private ScanUtil instance;
    public static UHFRManager mUhfrManager;//uhf
    public static Set<String> mSetEpcs; //epc set ,epc list   //借调方法




    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint({"SourceLockedOrientationActivity", "MissingPermission"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottomNav);
        mSharedPreferences = getSharedPreferences("UHF", MODE_PRIVATE);
        if (savedInstanceState==null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,new scanFragment()).commit();
        }









        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {


                Fragment fragment = null;
                switch (item.getItemId()){
                    case R.id.scanHome:

                        fragment = new scanFragment();

                        break;
                    case R.id.saveHome:
                        fragment = new saveFragment();
                        break;
                    case R.id.settingHome:
                        fragment = new settingFragment();
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,fragment).commit();
                return true;
            }
        });
    }

    private SharedPreferences mSharedPreferences;
    @Override
    protected void onStart() {
        super.onStart();
//            Log.e(TAG, "[onStart]");

        if (Build.VERSION.SDK_INT == 29) {
            instance = com.example.sspulibrary.ScanUtil.getInstance(this);
            instance.disableScanKey("134");
        }
      mUhfrManager = UHFRManager.getInstance();// Init Uhf module
        if(mUhfrManager!=null){
            Reader.READER_ERR err = mUhfrManager.setPower(mSharedPreferences.getInt("readPower",33), mSharedPreferences.getInt("writePower",33));//set uhf module power

            if(err== Reader.READER_ERR.MT_OK_ERR){
                mUhfrManager.setRegion(Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion", 1)));
                Toast.makeText(getApplicationContext(),"FreRegion:"+Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion",1))+
                        "\n"+"Read Power:"+mSharedPreferences.getInt("readPower",33)+
                        "\n"+"Write Power:"+mSharedPreferences.getInt("writePower",33),Toast.LENGTH_LONG).show();
//                showToast(getString(R.string.inituhfsuccess));
            }else {

                Reader.READER_ERR err1 = mUhfrManager.setPower(30, 30);//set uhf module power
                if(err1== Reader.READER_ERR.MT_OK_ERR) {
                    mUhfrManager.setRegion(Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion", 1)));
                    Toast.makeText(getApplicationContext(), "FreRegion:" + Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion", 1)) +
                            "\n" + "Read Power:" + 30 +
                            "\n" + "Write Power:" + 30, Toast.LENGTH_LONG).show();
                }else {
                    showToast(getString(R.string.inituhffail));
                }
            }
        }else {
            showToast(getString(R.string.inituhffail));
        }
    }


    private Toast mToast;
    private void showToast(String info) {
        if (mToast == null) {
            mToast = Toast.makeText(this, info, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(info);
        }
        mToast.show();
    }

}