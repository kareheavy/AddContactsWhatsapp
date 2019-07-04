package com.jhonjimenez.myapplication;

import android.Manifest;
import android.app.ActivityManager;
import android.app.DialogFragment;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static com.jhonjimenez.myapplication.Utils.CLIENTES;
import static com.jhonjimenez.myapplication.Utils.PREFERENCES;

public class MainActivity extends AppCompatActivity {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    private ImageView imageView;
    private TextView textViewFecha;
    private TextView textViewCantidad;
    private RecyclerView recyclerView;
    private AdapterRecycler adapterRecycler;
    private NotificationReceiver nReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getComponentUI();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CONTACTS)) {


            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }

        }else{

            if (!isNotificationServiceEnabled()) {
                buildNotificationServiceAlertDialog();
            }
        }

        nReceiver = new NotificationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.kpbird.nlsexample.NOTIFICATION_LISTENER_EXAMPLE");
        registerReceiver(nReceiver, filter);

        toggleNotificationListenerService();

        if(isMyServiceRunning(NLService.class)){
            imageView.setColorFilter(ContextCompat.getColor(this, R.color.colorEnable), android.graphics.PorterDuff.Mode.SRC_IN);

            NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            NotificationCompat.Builder ncomp = new NotificationCompat.Builder(this);
            ncomp.setContentTitle("Add Contact Whatsapp");
            ncomp.setContentText("Servicio en ejecución.");
            ncomp.setTicker("Servicio en ejecución.");
            ncomp.setSmallIcon(R.drawable.ic_stat_name);
            ncomp.setTimeoutAfter(1000);
            ncomp.setAutoCancel(true);
            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = getString(R.string.channel_name);
                String channelId = getApplicationContext().getString(R.string.default_notification_channel_id);
                NotificationChannel channel = new NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("Servicio en ejecución.");
                nManager.createNotificationChannel(channel);
                ncomp.setChannelId(channelId);
            }

            nManager.notify(1,ncomp.build());

        }

    }

    private void getComponentUI() {

        imageView = findViewById(R.id.imageView);
        textViewFecha = findViewById(R.id.textview_fecha);
        textViewCantidad = findViewById(R.id.textview_cantidad);
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        updateUI();
    }

    private void updateUI() {

        SharedPreferences prefs = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        Set<String> clientes = prefs.getStringSet(CLIENTES, new HashSet<String>());

        adapterRecycler = new AdapterRecycler(this, clientes);

        recyclerView.setAdapter(adapterRecycler);

        textViewFecha.setText(String.format("Fecha : %1$s",Utils.getDate()));
        textViewCantidad.setText(String.format("Total %1$s contactos", clientes.size()));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {

            DialogFragment newFragment = new DialogFragmentConfiguracion();
            newFragment.setCancelable(false);
            newFragment.show(getFragmentManager(), "missiles");

            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (!isNotificationServiceEnabled()) {
                        buildNotificationServiceAlertDialog();
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    private void toggleNotificationListenerService() {
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this, NLService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(new ComponentName(this, NLService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nReceiver);
    }

    class NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, Intent intent) {
            final String servicioCreado = intent.getStringExtra("notification_event");
            if(servicioCreado.equals("Servicio creado...")){
                imageView.setColorFilter(ContextCompat.getColor(context, R.color.colorEnable), android.graphics.PorterDuff.Mode.SRC_IN);


                NotificationCompat.Builder ncomp = new NotificationCompat.Builder(getApplicationContext());
                ncomp.setContentTitle("Add Contact Whatsapp");
                ncomp.setContentText(servicioCreado);
                ncomp.setTicker(servicioCreado);
                ncomp.setSmallIcon(R.drawable.ic_stat_name);
                ncomp.setTimeoutAfter(1000);
                ncomp.setAutoCancel(true);
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                // Since android Oreo notification channel is needed.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    CharSequence name = getString(R.string.channel_name);
                    String channelId = getApplicationContext().getString(R.string.default_notification_channel_id);
                    NotificationChannel channel = new NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_DEFAULT);
                    channel.setDescription("Servicio en ejecución.");
                    notificationManager.createNotificationChannel(channel);
                    ncomp.setChannelId(channelId);
                }
                notificationManager.notify(1,ncomp.build());

            }

            updateUI();
        }
    }

    private boolean isNotificationServiceEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(), ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void buildNotificationServiceAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Acceso notificaciones");
        alertDialogBuilder.setMessage("Es necesario que actives el acceso a notificaciones para el correcto funcionamiento de la app.");
        alertDialogBuilder.setPositiveButton("Configuración",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                });
        alertDialogBuilder.setNegativeButton("Cancelar",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If you choose to not enable the notification listener
                        // the app. will not work as expected
                    }
                });
        alertDialogBuilder.create().show();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
