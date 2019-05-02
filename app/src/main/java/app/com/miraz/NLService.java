package app.com.miraz;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.util.Log;

@SuppressLint("OverrideAbstract")
public class NLService extends NotificationListenerService {
    private String TAG = this.getClass().getSimpleName();
    private static final class ApplicationPackageNames {
        public static final String FACEBOOK_PACK_NAME = "com.facebook.katana";
        public static final String FACEBOOK_MESSENGER_PACK_NAME = "com.facebook.orca";
        public static final String WHATSAPP_PACK_NAME = "com.whatsapp";
        public static final String INSTAGRAM_PACK_NAME = "com.instagram.android";
        public static final String DIALER_PACK_NAME = "com.sonymobile.android.dialer";
    }

    /*
        These are the return codes we use in the method which intercepts
        the notifications, to decide whether we should do something or not
     */
    public static final class InterceptedNotificationCode {
        public static final int FACEBOOK_CODE = 1;
        public static final int WHATSAPP_CODE = 2;
        public static final int INSTAGRAM_CODE = 3;
        public static final int DIALER_CODE = 4;
        public static final int OTHER_NOTIFICATIONS_CODE = 5; // We ignore all notification with code == 4
    }

    public NLService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        Log.d("++++++++","got a notification.");
        String notificationTitle;
        String notificationConversationTitle;
        String notificationSubText;
        String notificationText;
        String notificationSelfDisplayName;
        String notificationInfoText;


        int notificationCode = matchNotificationCode(sbn);
        Notification notification = sbn.getNotification();

        if(notification.extras.get("android.title") != null)
            notificationTitle = notification.extras.get("android.title").toString();
        else
            notificationTitle = "";

        if(notification.extras.get("android.conversationTitle") != null)
            notificationConversationTitle = notification.extras.get("android.conversationTitle").toString();
        else
            notificationConversationTitle = "";

        if(notification.extras.get("android.subText") != null)
            notificationSubText = notification.extras.get("android.subText").toString();
        else
            notificationSubText = "";

        if(notification.extras.get("android.text") != null)
            notificationText = notification.extras.get("android.text").toString();
        else
            notificationText = "";

        if(notification.extras.get("android.selfDisplayName") != null)
            notificationSelfDisplayName = notification.extras.get("android.selfDisplayName").toString();
        else
            notificationSelfDisplayName = "";

        if(notification.extras.get("android.infoText") != null)
            notificationInfoText = notification.extras.get("android.infoText").toString();
        else
            notificationInfoText ="";

        if(notificationCode != InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE){
            Intent intent = new  Intent("app.com.miraz");
            Log.d(TAG,notificationTitle);
            Log.d(TAG,notificationConversationTitle);
            Log.d(TAG,notificationSubText);
            Log.d(TAG,notificationSelfDisplayName);
            Log.d(TAG,notificationInfoText);
            intent.putExtra("notificationCode", notificationCode);
            intent.putExtra("notificationTitle",notificationTitle);
            intent.putExtra("notificationConversationTitle",notificationConversationTitle);
            intent.putExtra("notificationSubText",notificationSubText);
            intent.putExtra("notificationText",notificationText);
            intent.putExtra("notificationSelfDisplayName",notificationSelfDisplayName);
            intent.putExtra("notificationInfoText",notificationInfoText);
            sendBroadcast(intent);
        }
    }

    private int matchNotificationCode(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        Log.d("+++++++++",packageName);

        if(packageName.equals(ApplicationPackageNames.FACEBOOK_PACK_NAME)
                || packageName.equals(ApplicationPackageNames.FACEBOOK_MESSENGER_PACK_NAME)){
            return(InterceptedNotificationCode.FACEBOOK_CODE);
        }
        else if(packageName.equals(ApplicationPackageNames.INSTAGRAM_PACK_NAME)){
            return(InterceptedNotificationCode.INSTAGRAM_CODE);
        }
        else if(packageName.equals(ApplicationPackageNames.WHATSAPP_PACK_NAME)){
            return(InterceptedNotificationCode.WHATSAPP_CODE);
        }
        else if(packageName.equals(ApplicationPackageNames.DIALER_PACK_NAME)){
            return(InterceptedNotificationCode.DIALER_CODE);
        }
        else{
            return(InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE);
        }
    }
}
