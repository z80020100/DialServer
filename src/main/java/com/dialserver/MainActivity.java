package com.dialserver;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import com.orhanobut.logger.Logger;
import com.dialserver.services.DialUDPService;
import com.dialserver.utils.Constant;

import java.lang.ref.WeakReference;

public class MainActivity extends Activity {

    private final Messenger mMessenger = new Messenger(new ServiceHandler());
    private Messenger mService;
    private TextView mInfoTv;
    private Button mReconnectButton;
    private Intent mIntent;
    private ScrollView mScrollView;
    private WeakReference<DialServiceConnection> mDialServiceConnWfr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mInfoTv = (TextView) findViewById(R.id.tvInfo);
        mReconnectButton = (Button) findViewById(R.id.btn_reconnect);
        mScrollView = (ScrollView) findViewById(R.id.scrollview);

        WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock multilock = wm.createMulticastLock("lock");
        multilock.acquire();

        startService();

        mReconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInfoTv.setText("");
                startActivity(new Intent(getApplication(), MainActivity.class));
                finish();
                mReconnectButton.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInfoTv.setText("");
            }
        });
    }

    public void startService() {
        mIntent = new Intent(getApplication(), DialUDPService.class);
        startService(mIntent);
        mDialServiceConnWfr = new WeakReference<DialServiceConnection>(new DialServiceConnection());
        bindService(mIntent, mDialServiceConnWfr.get(), Context.BIND_AUTO_CREATE);
    }

    private class DialServiceConnection implements ServiceConnection {

        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            //textStatus.setText("Attached.");
            Logger.e("onServiceConnected.");
            mInfoTv.append("SERVICE_CONNECTED\n\n");
            try {
                Message msg = Message.obtain(null, Constant.SERVICE_CONNECTED);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
                // In this case the service has crashed before we could even do anything with it
            } finally {
                mReconnectButton.setVisibility(View.GONE);
            }
        }

        /***
         * @param className
         */
        public void onServiceDisconnected(ComponentName className) {
            Logger.e("onServiceDisconnected.");
            mInfoTv.append("SERVICE_DISCONNECTED\n\n");
        }
    }

    private class ServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constant.SERVICE_SENT_DATA:
                    Logger.e("SERVICE_SENT_DATA. " + msg.obj);
                    mInfoTv.append(msg.obj + "\n\n");
                    mScrollView.fullScroll(View.FOCUS_DOWN);
                    break;
                case Constant.SERVICE_DISCONNECTED:
                    Logger.e("SERVICE_DISCONNECTED. " + msg.what);
                    mInfoTv.append("SERVICE_DISCONNECTED" + "\n\n");
                    mScrollView.fullScroll(View.FOCUS_DOWN);
                    mReconnectButton.setVisibility(View.VISIBLE);
                    unbindService(mDialServiceConnWfr.get());
                    stopService(mIntent);
                    mDialServiceConnWfr.clear();

                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mDialServiceConnWfr.get());
        stopService(mIntent);
        mDialServiceConnWfr.clear();
    }
}
