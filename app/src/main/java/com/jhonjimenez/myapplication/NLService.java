package com.jhonjimenez.myapplication;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.jhonjimenez.myapplication.Utils.CLIENTES;
import static com.jhonjimenez.myapplication.Utils.CONSECUTIVO;
import static com.jhonjimenez.myapplication.Utils.ID_CLIENT;
import static com.jhonjimenez.myapplication.Utils.NOMBRE_BASE;
import static com.jhonjimenez.myapplication.Utils.PREFERENCES;

public class NLService extends NotificationListenerService {

    private String TAG = this.getClass().getSimpleName();
    private List<String> titles = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        Intent i = new Intent("com.kpbird.nlsexample.NOTIFICATION_LISTENER_EXAMPLE");
        i.putExtra("notification_event", "Servicio creado...");
        sendBroadcast(i);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        Log.i(TAG, "**********  onNotificationPosted");
        Log.i(TAG, "ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
        if (sbn.getNotification() != null && sbn.getPackageName().equals("com.whatsapp") && sbn.getNotification().tickerText != null) {

            Bundle bundle = sbn.getNotification().extras;
            if (bundle != null) {
                String title = (String) sbn.getNotification().tickerText;

                if (title != null) {
                    String[] arraySplit = title.split("\\+");
                    if(arraySplit.length == 2){
                        title = arraySplit[1];

                        if (title.length() == 14) {
                            //title = title.substring(2);
                            //title = title.replace(" ", "");
                            if (title.matches("[0-9 ]+")) {
                                title = "+" + title;
                                if (!titles.contains(title)) {
                                    titles.add(title);
                                    addContactToPhone(title);
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
    }

    private void addContactToPhone(String number) {

        SharedPreferences prefs = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        String consecutivo  = prefs.getString(CONSECUTIVO, "0");
        int idCliente = Integer.parseInt(consecutivo != null ? consecutivo : "0");
        String nombreBase = prefs.getString(NOMBRE_BASE, "Cliente");
        boolean flat = true;
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        idCliente++;

        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        //------------------------------------------------------ Names
        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(
                        ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                        nombreBase + " " + idCliente).build());


        //------------------------------------------------------ Mobile Number
        if (number != null) {
            ops.add(ContentProviderOperation.
                    newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build());
        }

        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
            flat = false;
            showToast("Exception: " + e.getMessage());
        }

        if (flat) {

            SharedPreferences.Editor editor = prefs.edit();
            Set<String> clientesSet = prefs.getStringSet(CLIENTES, new HashSet<String>());

            clientesSet = deleteClientesViejos(clientesSet != null ? clientesSet : new HashSet<String>());

            editor.putString(CONSECUTIVO, Integer.toString(idCliente));
            clientesSet.add(nombreBase + " " + idCliente + "|" + Utils.getDate());
            editor.putStringSet(CLIENTES, clientesSet);
            editor.apply();

            showToast(nombreBase + " " + idCliente + "  creado...");

            Intent i = new Intent("com.kpbird.nlsexample.NOTIFICATION_LISTENER_EXAMPLE");
            i.putExtra("notification_event", "cliente creado");
            sendBroadcast(i);

        }

    }

    private Set<String> deleteClientesViejos(Set<String> clientesSet) {

        Set<String> clientesSetNew = new HashSet<>();

        for (String cliente : clientesSet) {

            String[] arrayCliente = cliente.split("\\|");
            String fecha = Utils.getDate();

            if (arrayCliente[1].equals(fecha)) {
                clientesSetNew.add(cliente);
            }
        }

        return clientesSetNew;
    }

    private void showToast(final String text) {

        NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder ncomp = new NotificationCompat.Builder(this);
        ncomp.setContentTitle("Add Contact Whatsapp");
        ncomp.setContentText(text);
        ncomp.setTicker(text);
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
