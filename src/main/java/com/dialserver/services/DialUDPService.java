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

import java.io.BufferedInputStream;
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

//    /***
//     * @param reader
//     * @return
//     */
//    public synchronized String readDataFromSocket(BufferedReader reader) {
//        StringBuilder contents = new StringBuilder();
//        try {
//            String lineSp = System.getProperty("line.separator");
//            String line = null;
//            ////long timeB = System.currentTimeMillis();
//            while ((line = reader.readLine()) != null) {
//                contents.append(line).append(lineSp);
//            }
//            /*long timef = System.currentTimeMillis();
//            Logger.e("TIME: " + (timef - timeB));
//            sendMessageToUI("TIME: " + (timef - timeB));*/
//        } catch (Exception e) {
//            e.printStackTrace();
//            Logger.e(e.getMessage());
//        }
//        return contents.toString().trim();
//    }

    /**
     * @param bis
     * @return
     */
    public synchronized String readDataFromSocket(BufferedInputStream bis) {
        try {
            StringBuffer sb = new StringBuffer();
            byte[] bytes;

            while (true) {
                bytes = new byte[512];
                if (bis.read(bytes) == -1) {
                    sb.append(new String(bytes));
                    break;
                }else{
                    sb.append(new String(bytes));
                }
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e(e.getMessage());
            return "";
        }
    }


    /***
     * @param reader
     * @return
     */
    public synchronized String readDataFromSocket(BufferedReader reader) {
        StringBuilder contents = new StringBuilder();
        try {
            String line;
            int length = 0;
            while ((line = reader.readLine()) != null) {
                if (line.equals("")) { // last line of request message
                    // header is a
                    // blank line (\r\n\r\n)
                    break; // quit while loop when last line of header is
                    // reached
                }

                // checking line if it has information about Content-Length
                // weather it has message body or not
                if (line.startsWith("Content-Length: ")) { // get the
                    // content-length
                    int index = line.indexOf(':') + 1;
                    String len = line.substring(index).trim();
                    length = Integer.parseInt(len);
                }

                /////////////////contents.append(line + "\n"); // append the request
            } // end of while to read headers

            // if there is Message body, go in to this loop
            if (length > 0) {
                int read;
                while ((read = reader.read()) != -1) {
                    contents.append((char) read);
                    if (contents.length() == length)
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e(e.getMessage());
        }
        return contents.toString().trim();
    }

//    /***
//     * @param reader
//     * @return
//     */
//    public String readDataFromSocket(BufferedReader reader) {
//        StringBuilder contents = new StringBuilder();
//        try {
//            int value = 0;
//            long timeB = System.currentTimeMillis();
//            // reads to the end of the stream
//            while ((value = reader.read()) != -1) {
//                contents.append((char) value);
//            }
//            long timef = System.currentTimeMillis();
//            Logger.e("TIME: " + (timef - timeB));
//        } catch (Exception e) {
//            e.printStackTrace();
//            Logger.e(e.getMessage());
//        }
//        return contents.toString().trim();
//    }

    /***
     * @param socket
     * @throws IOException
     */
    private synchronized void closeSocket(Socket socket) throws IOException {

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
    public synchronized void stopDiscovery(Socket socket) {
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