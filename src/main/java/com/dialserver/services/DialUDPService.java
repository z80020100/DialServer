package com.dialserver.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;

import com.dialserver.R;
import com.orhanobut.logger.Logger;
import com.dialserver.rest_service.InitiateDialRestService;
import com.dialserver.service_discovery.InitiateDialServiceDiscovery;
import com.dialserver.service_discovery.InitiateMulticastUDPConnection;
import com.dialserver.utils.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by infotel5 on 10/8/15.
 */
public class DialUDPService extends BaseService {

    private String mLocalIpAddress;
    private Notification notification;
    private InitiateMulticastUDPConnection mInitialUDPConnection;
    private InitiateDialServiceDiscovery mInitiateDialServiceDiscovery;
    private InitiateDialRestService mDialRestService;

    @Override
    public void onCreate() {
        super.onCreate();
        mLocalIpAddress = Utils.getLocalIpAddress(getApplicationContext());

        startMulticastUDPConnection();
        startDialServiceDiscovery();
        startDialRestService();
    }

    private void startMulticastUDPConnection() {
        mInitialUDPConnection = new InitiateMulticastUDPConnection(this);
        mInitialUDPConnection.start();
    }

    /***
     * @throws IOException
     */
    private void startDialServiceDiscovery() {
        mInitiateDialServiceDiscovery = new
                InitiateDialServiceDiscovery(this);
        mInitiateDialServiceDiscovery.start();
    }

    /***
     * @throws IOException
     */
    private void startDialRestService() {
        mDialRestService = new InitiateDialRestService(this);
        mDialRestService.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Run Service as a Forground
        notification = new Notification(R.mipmap.ic_launcher, "Dial Service Ticker",
                System.currentTimeMillis());
        Intent notificationIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.setLatestEventInfo(this, "Dial Service Title",
                "Dial Service Message", pendingIntent);
        startForeground(1234, notification);

        return START_STICKY;
    }

    /*****
     * @param out
     * @param message
     * @throws IOException
     */
    public void sendTCPMessage(BufferedWriter out, String message) throws IOException {

        String outMsg = message + System.getProperty("line.separator");
        out.write(outMsg);
        out.flush();
        ////out.close();
    }

    /***
     * @param in
     * @return
     * @throws IOException
     */
    public String readDataFromSocket(BufferedReader in) {
        StringBuilder sb = new StringBuilder();
        try {
            String line = null;
            while ((line = in.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e(e.getMessage());
        }
        ////////////////////in.close();
        return sb.toString().trim();
    }

    /***
     * @param socket
     * @throws IOException
     */
    private void closeSocket(Socket socket) throws IOException {

        if (socket != null) {
            socket.shutdownInput();
            socket.shutdownOutput();
            socket.close();
            socket = null;
        }
    }

    /***
     * @param socket
     */
    public void stopDiscovery(Socket socket) {
        try {
            closeSocket(socket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getLocalIpAddress() {
        return mLocalIpAddress;
    }

    @Override
    public void onDestroy() {
        ////startService(new Intent(getApplication(), DialUDPService.class));
        super.onDestroy();

        Logger.e("onDestroy");
        // TEMPORARY
        mInitialUDPConnection.exitFromApp();
        mInitiateDialServiceDiscovery.exitFromApp();
        mDialRestService.exitFromApp();
    }
}