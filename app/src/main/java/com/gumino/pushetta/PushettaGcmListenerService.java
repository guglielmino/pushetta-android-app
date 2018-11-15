package com.gumino.pushetta;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;

import com.google.android.gms.gcm.GcmListenerService;
import com.gumino.core.contentprovider.PushMessageContentProvider;
import com.gumino.core.database.PushMessageTable;
import com.gumino.pushetta.core.IPushettaServiceClient;
import com.gumino.pushetta.core.PushettaClientFactory;
import com.gumino.pushetta.core.PushettaConfigs;
import com.gumino.pushetta.core.PushettaConsts;
import com.gumino.pushetta.core.dto.PushMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Random;

public class PushettaGcmListenerService extends GcmListenerService {
    private static final String TAG = "PushettaGcmListenerService";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.d(TAG, "From: " + from);
        processNotificationData(data);
    }


    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    private void processNotificationData(Bundle extras) {
        String alert_msg = extras.getString("alert_msg");
        String push_type = extras.getString("push_type");
        String titolo = "default";
        String urlicona = "default";
        if (push_type.equalsIgnoreCase(PushettaConsts.PUSHMESSAGE_TYPE_PLAIN)) {
            try {
                JSONObject json = new JSONObject(extras.getString("data_dic"));
                final Integer message_id = json.getInt("message_id");
                String channel_name = json.getString("channel_name");
                titolo = channel_name;
                String channel_image_url = json.getString("channel_image_url");
                urlicona = channel_image_url;


                // Se il messaggio ha un one time token lo acquisisco
                if (json.has("ott")) {
                    String oneTimeToken = json.getString("ott");
                    PushettaConfigs.setOtt(oneTimeToken);
                }

                IPushettaServiceClient client = PushettaClientFactory
                        .getClient();
                PushMessage result = client.getPushMessageSync(message_id);

                DateFormat df = new SimpleDateFormat(
                        PushettaConsts.DATABASE_DATE_FORMAT);

                ContentValues values = new ContentValues();
                values.put(PushMessageTable.COLUMN_ID, message_id);
                values.put(PushMessageTable.COLUMN_BODY, result.getBody());
                values.put(PushMessageTable.COLUMN_CHANNEL_NAME, channel_name);
                values.put(PushMessageTable.COLUMN_CHANNEL_ICON_IMAGE,
                        channel_image_url);

                // Nota le date vengono messe come stringhe nel Db

                values.put(PushMessageTable.COLUMN_DATE_CREATED,
                        df.format(result.getDate_created()));
                if (result.getExpire() != null)
                    values.put(PushMessageTable.COLUMN_DATE_EXPIRE,
                            df.format(result.getExpire()));

                values.put(PushMessageTable.COLUMN_PREVIEW_URL, result.getPreview_url());

                Uri pushMessageUri = getContentResolver().insert(
                        PushMessageContentProvider.CONTENT_URI, values);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        sendNotification(alert_msg, titolo, urlicona);
    }

    private void sendNotification(final String messaggio, final String titolo, String urlicona) {
        mNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        final PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        final Uri sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification);

        Bitmap bmp = null;
        try {
            InputStream in = new URL(urlicona).openStream();
            bmp = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load image, decode it to Bitmap and return Bitmap to callback

        int NOTIFICATION_ID = randInt(0, 100);

        Context localCtx = PushettaGcmListenerService.this;

        // Notification channel introduced with API >= 26
        NotificationManager notificationManager =
                (NotificationManager) localCtx.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("default",
                titolo,
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(messaggio);
        notificationManager.createNotificationChannel(channel);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(localCtx, "default")
                .setSmallIcon(R.drawable.ic_smallicon)
             //   .setLargeIcon(bmp)
                .setContentTitle(titolo)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(messaggio))
                .setSound(sound)
                .setContentText(messaggio);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

    }

    public static int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }
}
