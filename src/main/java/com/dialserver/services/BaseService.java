package com.dialserver.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import com.dialserver.utils.Constant;

/**
 * Created by infotel5 on 18/8/15.
 */
public abstract class BaseService extends Service{

    // Target we publish for clients to send messages to IncomingHandler.
    final Messenger mReceivingMessenger = new Messenger(new IncomingHandler());

    protected Messenger mSenderMessenger;

    protected Handler mHandler;
    public boolean isConnected = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mReceivingMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private class IncomingHandler extends Handler { // Handler of incoming messages from clients.

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constant.SERVICE_CONNECTED:
                    isConnected = true;
                    mSenderMessenger = msg.replyTo;
                    sendMessageToUI(msg.replyTo, Message.obtain(null, Constant.SERVICE_CONNECTED));
                    break;
                /*case Constant.SERVICE_DISCONNECTED:
                    isConnected = false;
                    mSenderMessenger = msg.replyTo;
                    sendMessageToUI(msg.replyTo, Message.obtain(null, Constant.SERVICE_DISCONNECTED));
                    break;*/
            }
        }
    }

    /**
     * @param messenger
     * @param message
     */
    public void sendMessageToUI(Messenger messenger, Message message) {
        try {
            messenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /***
     * @param message
     */
    public void sendMessageToUI(Message message) {
        try {
            mSenderMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /***
     * @param message
     */
    public void sendMessageToUI(String message) {
        try {
            mSenderMessenger.send(Message.obtain(null, Constant.SERVICE_SENT_DATA, message));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
