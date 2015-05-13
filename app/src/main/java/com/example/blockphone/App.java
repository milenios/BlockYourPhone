package com.example.blockphone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.vk.sdk.VKSdk;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKList;

import java.io.ByteArrayOutputStream;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import db.DB_read_all;
import db.VK_Friends;

/**
 * Created by Жамбыл on 26.03.2015.
 */
public class App  extends ActionBarActivity {
    //Главная
    public static int NumberOfFriend = 0;
    //public static VKList<VKApiUser> Friends;
    String TITLES[] = {"Блокировка","Рейтинг","Выход"};
    int ICONS[] = {R.drawable.ic_action,R.drawable.ic_raiting,R.drawable.ic_quit};
    Button MainBlockButton;
    TextView Text1,Text2;
    public static String  dropdown1value, dropdown2value;
    Typeface type_thin;
    Typeface type;
    String[] Seconds = new String[] {"1 секунда","2 секунды","3 секунды","4 секунды"};
    String[] Hours = new String[] {"1 час","2 часа","3 часа","4 часа"};
    Spinner dropdown1, dropdown2;

    String NAME = Account.getFullName();
    int POINTS = Account.getPoints();
    byte [] PROFILE_PHOTO = Account.getPhotoAsBytes();

    private Toolbar toolbar;
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    DrawerLayout Drawer;
    LinearLayout layoutFromRecycler;
    ActionBarDrawerToggle mDrawerToggle;

    private static KeyguardManager.KeyguardLock kl;
    private static KeyguardManager km;

    static boolean reenabled = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("App", "OnCreate");
        setContentView(R.layout.main_app);
        LockScreenService.isMustBeLocked = false;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //window.setStatusBarColor(getResources().getColor(R.color.ColorPrimary));
             window.setStatusBarColor(Color.BLACK);
        }
        km = ((KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE));
        kl = km.newKeyguardLock(getPackageName());

        if(!Internet.isNetworkConnection(App.this)){
            Log.e("App","Restoring Acc data");
            Account.restore(App.this,false);
        }
        else
            //Account.restore(App.this,true);
            getUserData();

        startUI();

    }

    private void startUI(){
        type = Typeface.createFromAsset(getAssets(), "fonts/RobotoCondensed-Light.ttf");
        type_thin = Typeface.createFromAsset(App.this.getAssets(), "fonts/Roboto-Thin.ttf");

        dropdown1 = (Spinner)findViewById(R.id.dropdown1);
        dropdown2 = (Spinner)findViewById(R.id.dropdown2);
        dropdown1.setBackgroundColor(App.this.getResources().getColor(R.color.ColorPrimaryDark));
        dropdown2.setBackgroundColor(App.this.getResources().getColor(R.color.ColorPrimaryDark));

        dropdown1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int pos, long id) {
                dropdown1value = parent.getItemAtPosition(pos).toString();
                try {
                    ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                    ((TextView) parent.getChildAt(0)).setTextSize(20);
                    ((TextView) parent.getChildAt(0)).setGravity(Gravity.CENTER);
                } catch (Exception e) {
                    Log.e("App", "dropdown1 error");
                }
                //((TextView) parent.getChildAt(0)).setTypeface(type);

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        dropdown2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int pos, long id) {
                dropdown2value = parent.getItemAtPosition(pos).toString();
                try {
                    ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                    ((TextView) parent.getChildAt(0)).setTextSize(20);
                    ((TextView) parent.getChildAt(0)).setGravity(Gravity.CENTER);
                }catch (Exception e){
                    Log.e("App","dropdown2 error ");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Hours);
        dropdown1.setAdapter(adapter1);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Seconds);
        dropdown2.setAdapter(adapter2);

        Text1 = (TextView)findViewById(R.id.Text1);
        Text2 = (TextView)findViewById(R.id.Text2);


        Text1.setTypeface(type_thin);
        Text2.setTypeface(type_thin);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Блокировка");


        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView);
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new DrawableAdapter(TITLES,ICONS,NAME,POINTS,PROFILE_PHOTO,App.this);
        mRecyclerView.setAdapter(mAdapter);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        Drawer = (DrawerLayout) findViewById(R.id.DrawerLayoutMain);

        mDrawerToggle = new ActionBarDrawerToggle(this,Drawer,toolbar,R.string.openDrawer,R.string.closeDrawer){
            @Override
            public void onDrawerOpened(View drawerView) {super.onDrawerOpened(drawerView);}
            @Override
            public void onDrawerClosed(View drawerView) {super.onDrawerClosed(drawerView);}
        };
        Drawer.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();


        layoutFromRecycler = (LinearLayout)findViewById(R.id.layoutFromRecycler);

        MainBlockButton= (Button)findViewById(R.id.MainBlockButton);
        MainBlockButton.setBackgroundColor(App.this.getResources().getColor(R.color.ColorPrimaryDark));
        MainBlockButton.setTextColor(Color.WHITE);
        MainBlockButton.setText("Заблокировать");
        MainBlockButton.setTransformationMethod(null);
        //MainBlockButton.setTypeface(type);
        MainBlockButton.setOnClickListener(onMainBlockButtonClickListener);

        //Toast.makeText(this, String.valueOf(Account.user_id), Toast.LENGTH_LONG).show();
        final GestureDetector mGestureDetector =
                new GestureDetector(App.this,  new GestureDetector.SimpleOnGestureListener() {
                    @Override public boolean onSingleTapUp(MotionEvent e) {
                        return true;
                    }
                });
        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
                View child = recyclerView.findChildViewUnder(motionEvent.getX(),motionEvent.getY());
                if(child!=null && mGestureDetector.onTouchEvent(motionEvent)){
                    Intent intent = new Intent();
                    Drawer.closeDrawers();
                    switch(recyclerView.getChildPosition(child))
                    {
                        case 1:
                            break;
                        case 2:
                            startActivity(new Intent(App.this, Top.class));
                            break;
                        case 3:
                            new AlertDialog.Builder(App.this)
                                    .setTitle("Выход")
                                    .setMessage("Вы уверены, что хотите выйти из аккаунта?")
                                    .setNegativeButton(android.R.string.no, null)
                                    .setPositiveButton("Да",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface arg0, int arg1) {
                                                    VKSdk.logout();
                                                    VK_Friends.wipeFriendsData(App.this);
                                                    startActivity(new Intent(App.this, MainActivity.class));
                                                    Log.i("App", "Logining out");
                                                    finish();
                                                }
                                            }).create().show();
                            break;
                    }
                    return true;
                }
                return false;
            }
            @Override
            public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

            }
        });
    }
    private View.OnClickListener onMainBlockButtonClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view) {
           //THE MAIN FUNCTIONALITY
/*
            startService(new Intent(App.this,LockScreenService.class));
            LockScreenService.isMustBeLocked = true;
            String packageName = "com.android.launcher";
            String packageClass = "com.android.launcher2.Launcher";

            Intent home_intent = new Intent(Intent.ACTION_MAIN);
            home_intent.addCategory(Intent.CATEGORY_HOME);
            home_intent.setComponent(new ComponentName(packageName, packageClass));
            home_intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

        /* Here you should catch the exception when the launcher has been uninstalled,
           and let the user save themselves by opening the Market or an app list or something.
           Users sometimes use root apps to uninstall the system launcher, so your fake launcher
           is all that is left. Might as well give the poor user a hand. */
//            startActivity(home_intent);
           Lock();
        }};
    private void Lock(){
        kl.disableKeyguard();
        startService(new Intent(this, LockScreenService.class));
        LockScreenService.isMustBeLocked = true;
        Intent localIntent = new Intent(this, LockScreenActivity.class);
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        localIntent.addFlags(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
        startActivity(localIntent);
    }
    @Override
    protected void onResume() {
        reenable();
        reenabled = false;
        Log.i("App","onResume");
        LockScreenService.isMustBeLocked = false;
        super.onResume();
        VKUIHelper.onResume(this);
        setLocalData();

    }
    public static void reenable(){
        if(!reenabled) {
            kl.reenableKeyguard();
            reenabled = true;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VKUIHelper.onActivityResult(this, requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getUserData(){
        Log.i("App", "getting user data");
        VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.FIELDS,
                "id,first_name,last_name,photo_100,"));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                VKList<VKApiUser> MainUser = (VKList<VKApiUser>) response.parsedModel;
                Log.e("App", "Getting user data");
                String photoUrl = MainUser.get(0).photo_100;
                if (photoUrl != null) {
                    Bitmap photoBm = null;
                    String previousUrl = null;
                    if (Account.getPhotoUrl() != null)
                        previousUrl = Account.getPhotoUrl();
                    if (!photoUrl.equals(previousUrl)) {
                        try {
                            photoBm = Internet.convertUrlToImage(photoUrl);
                        } catch (Exception e) {
                            Logger logger = Logger.getAnonymousLogger();
                            logger.log(Level.SEVERE, "an exception was thrown while converting", e);
                        }
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    if (photoBm != null)
                        photoBm.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] b = baos.toByteArray();
                    String encodedPhoto = Base64.encodeToString(b, Base64.DEFAULT);
                    if (!encodedPhoto.equals(null)) {
                        Log.i("App", "getting user data SUCCESS");
                        setLocalData();
                    }

                    Account.setAccountData(App.this, MainUser.get(0).first_name, MainUser.get(0).last_name,
                            String.valueOf(MainUser.get(0).id),
                            encodedPhoto, MainUser.get(0).photo_100);
                }
                setLocalData();
                new DB_read_all(App.this).execute();
                new VK_Friends(App.this).execute();
            }
        });}

    private void setLocalData(){
        Log.i("App", "Setting Local Data");

        NAME = Account.getFullName();
        PROFILE_PHOTO = Account.getPhotoAsBytes();
        POINTS = Account.getPoints();

        mAdapter = new DrawableAdapter(TITLES,ICONS,NAME,POINTS,PROFILE_PHOTO,App.this);
        mRecyclerView.setAdapter(mAdapter);
    }
    @Override
    public void onBackPressed(){
        new AlertDialog.Builder(this)
                .setTitle("Выход")
                .setMessage("Вы уверены, что хотите выйти?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton("Да",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                Intent intent = new Intent(Intent.ACTION_MAIN);
                                intent.addCategory(Intent.CATEGORY_HOME);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                App.super.onBackPressed();
                                Log.i("App", "Exit");
                                finish();
                            }
                        }).create().show();
    }
}