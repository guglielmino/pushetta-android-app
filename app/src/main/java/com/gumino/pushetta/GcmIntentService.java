package com.gumino.pushetta;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;


import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.gumino.core.contentprovider.PushMessageContentProvider;
import com.gumino.core.database.PushMessageTable;
import com.gumino.pushetta.core.IPushettaServiceClient;
import com.gumino.pushetta.core.PushettaClientFactory;
import com.gumino.pushetta.core.PushettaConfigs;
import com.gumino.pushetta.core.PushettaConsts;
import com.gumino.pushetta.core.dto.PushMessage;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageSize;


public class GcmIntentService extends IntentService {

    Bitmap icona = null;

    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) { // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that
             * GCM will be extended in the future with new message types, just
             * ignore any message types you're not interested in, or that you
             * don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
                    .equals(messageType)) {
                sendNotification("Send error: " + extras.toString(), null, null);
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
                    .equals(messageType)) {
                sendNotification("Deleted messages on server: "
                        + extras.toString(), null, null);
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
                    .equals(messageType)) {
                processNotificationData(extras);

                Log.i(PushettaConsts.TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

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

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(final String messaggio, final String titolo, String urlicona) {
        mNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        final PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        final Uri sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification);

        // Load image, decode it to Bitmap and return Bitmap to callback
        ImageSize targetSize = new ImageSize(50, 50);
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.loadImage(urlicona, targetSize, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                setIcon(loadedImage);
                int NOTIFICATION_ID = randInt(0, 100);

                Context localCtx = GcmIntentService.this;

                // Notification channel introduced with API >= 26
                NotificationManager notificationManager =
                        (NotificationManager) localCtx.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel channel = new NotificationChannel("default",
                        titolo,
                        NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription(messaggio);
                notificationManager.createNotificationChannel(channel);

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(localCtx, "default")

                        .setSmallIcon(R.drawable.ic_launcher)
                        .setLargeIcon(getIcon())
                        .setContentTitle(titolo)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(messaggio))
                        .setSound(sound)
                        .setContentText(messaggio);

                mBuilder.setContentIntent(contentIntent);
                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

            }
        });


    }


    private void setIcon(Bitmap i) {
        icona = i;
    }

    private Bitmap getIcon() {
        return icona;
    }


    public static int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

}