package com.dialserver.rest_service;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import com.orhanobut.logger.Logger;
import com.dialserver.services.DialUDPService;
import com.dialserver.utils.Constant;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class InitiateDialRestService extends Thread {


    /*The Java volatile keyword is used to mark a Java variable as "being stored in main memory".
    More precisely that means, that every read of a volatile variable will be read from the computer's
    main memory, and not from the CPU cache, and that every write to a volatile variable will be written
    to main memory, and not just to the CPU cache.*/
    private volatile ServerSocket mServerSocket = null;
    private DialUDPService mDialService;
    private Socket socket = null;
    private boolean isDialRestServiceRunning = true;
    private Constant.APP_STATE mAppState = Constant.APP_STATE.stopped;

    /****
     * @param dialService
     */
    public InitiateDialRestService(DialUDPService dialService) {
        this.mDialService = dialService;
    }

    @Override
    public void run() {
        try {
            this.mServerSocket = new ServerSocket(Constant.DIAL_LOCAL_PORT_REST_SERVICE);
            while (isDialRestServiceRunning) {

                if (mServerSocket == null || mServerSocket.isClosed()) {
                    isDialRestServiceRunning = false;
                    break;
                }

                //Thread.sleep(5000);
                try {
                    socket = mServerSocket.accept();
                    if (!socket.isClosed()) {
                        socket.setKeepAlive(true);
                        socket.setTcpNoDelay(true);
                        startDialRestService(socket);
                    } else {
                        Logger.e("InitiateDialRestService socket is closed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.e(e, "InitiateDialRestService while");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e(e, "InitiateDialRestService RUN");
        } finally {
        }
    }

    /***
     * @param socket
     */
    private void startDialRestService(Socket socket) {
        new RunDialRestService(socket).start();
    }

    private class RunDialRestService extends Thread {

        private Socket mSocket;

        /**
         * @param socket
         */
        private RunDialRestService(Socket socket) {
            this.mSocket = socket;
        }

        @Override
        public void run() {
            super.run();
            try {
                receiveRestServiceRequest(mSocket);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mDialService.stopDiscovery(mSocket);
            }
        }

        private void receiveRestServiceRequest(Socket socket) throws Exception {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            ////String msg = mDialService.readDataFromSocket(in);

            String msg = in.readLine();
            Logger.e("receiveRestServiceRequest:\n" + msg);
                /*if (!TextUtils.isEmpty(msg)) {
                mDialService.sendMessageToUI("BufferedWriter output DRS:\n" + msg);
                }*/

            if(!TextUtils.isEmpty(msg) && msg.contains("dial_data")){
                String fullMsg = mDialService.readDataFromSocket(in);
                Logger.e(msg+"\r\n"+fullMsg);
            }
            else if (!TextUtils.isEmpty(msg) && msg.contains(Constant.APP_INFO_REQUEST)) {
                //boolean isConnected = socket.isConnected();

                final String message = msg + System.getProperty("line.separator");
                mDialService.sendMessageToUI("App Info Request Received:\n" + message);

                sendApplicationInformationResponse(out);
            } else if (!TextUtils.isEmpty(msg) && msg.startsWith(Constant.APP_SPECIFIC_INFO_REQUEST) &&
                    msg.endsWith(Constant.HTTP_PROTOCOL)) {
                boolean isConnected = socket.isConnected();

                final String message = msg + System.getProperty("line.separator");
                mDialService.sendMessageToUI("App Info Request Received:\n" + message);

                String appName = message.split(" ")[1];
                appName = appName.substring(appName.lastIndexOf("/") + 1, appName.length()).trim();

                sendSpecificApplicationInformationResponse(appName, out);
            } else if (!TextUtils.isEmpty(msg) && msg.startsWith(Constant.APP_LAUNCH_REQUEST)) {

                String message = msg + System.getProperty("line.separator");

                String appName = message.replace(Constant.APP_LAUNCH_REQUEST, "");
                appName = appName.substring(0, appName.indexOf(" "));

                sendApplicationLaunchResponse(appName, out);
                mDialService.sendMessageToUI("\nPlease wait while YouTube is Starting:\n" + message);

                msg = mDialService.readDataFromSocket(in);
                Logger.e(msg);
                launchBrowser(appName, msg);
            }
        }
    }

    /***
     * @param appName
     * @param msg
     */
    private void launchApp(String appName, String msg) {
        if (appName != null && appName.contains(Constant.APP_NAME_YOU_TUBE)) {
            String videoId = "";
            try {
                videoId = msg.substring(msg.lastIndexOf("&v=") + 3, msg.lastIndexOf("&t="));
                Logger.e("videoId: " + videoId);
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    if (!TextUtils.isEmpty(videoId)) {
                        intent.putExtra("VIDEO_ID", videoId);
                    }
                    mDialService.startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://www.youtube.com/watch?v=" + videoId));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mDialService.startActivity(intent);
                }
            } catch (Exception e) {
                Logger.e(e, "Parse videoId");
                e.printStackTrace();
            }
        } else {
            Logger.e("AppName: " + appName);
        }
    }
    //http://www.youtube.com/tv?pairingCode=fde5b1e1-4d50-4f8f-8344-1d04a67e7673&theme=cl&v=&t=0
//https://www.youtube.com/tv?pairingCode=1bae2cff-8a67-4852-8b14-5056f535e427&theme=cl&v=&t=0&additionalDataUrl=http://localhost:12345/app/YouTube/dial_data

    /***
     * @param appName
     * @param msg
     */
    private void launchBrowser(String appName, String msg) {
        boolean isBrowserLaunched = false;
        if (appName != null && appName.contains(Constant.APP_NAME_YOU_TUBE)) {
            String videoId = "";
            try {
                videoId = msg.substring(msg.lastIndexOf("&v=") + 3, msg.lastIndexOf("&t="));
                Logger.e("videoId: " + videoId);
            } catch (Exception e) {
                Logger.e(e, "Parse videoId");
                e.printStackTrace();
            }
            if (!TextUtils.isEmpty(videoId)) {

                Uri uri = Uri.parse("https://www.youtube.com/watch?v=" + videoId);
                Logger.e("uri: " + uri.toString());
                mDialService.sendMessageToUI("URI: " + uri.toString());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mDialService.startActivity(intent);
                isBrowserLaunched = true;
            } else {
                String params = "";
                try {
                    params = msg.substring(msg.lastIndexOf("pairingCode="), msg.length());
                    params = params + "&additionalDataUrl=http://localhost:" + Constant.DIAL_LOCAL_PORT_REST_SERVICE + "/app/" + appName + "/dial_data";

                    Logger.e("params: " + params);
                } catch (Exception e) {
                    Logger.e(e, "Parse params");
                    e.printStackTrace();
                }
                if (!TextUtils.isEmpty(params)) {
                    Uri uri = Uri.parse("https://www.youtube.com/tv?" + params);
                    Logger.e("uri: " + uri.toString());
                    mDialService.sendMessageToUI("URI: " + uri.toString());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mDialService.startActivity(intent);
                    isBrowserLaunched = true;
                }
            }

            if (isBrowserLaunched) {
                mAppState = Constant.APP_STATE.running;
            }
        } else {
            Logger.e("launchBrowser AppName: " + appName);
        }
    }


    /***
     * @param appName
     * @param msg
     */
    private void launchAppOrBrowser(String appName, String msg) {
        if (appName != null && appName.contains(Constant.APP_NAME_YOU_TUBE)) {
            String videoId = "";
            try {
                videoId = msg.substring(msg.lastIndexOf("&v=") + 3, msg.lastIndexOf("&t="));
                Logger.e("videoId: " + videoId);
                if (TextUtils.isEmpty(videoId)) {
                    String params = "";
                    params = msg.substring(msg.lastIndexOf("pairingCode="), msg.length());
                    Logger.e("params: " + params);

                    if (!TextUtils.isEmpty(params)) {
                        try {
                            Uri uri = Uri.parse("http://www.youtube.com/tv?" + params);
                            Logger.e("uri: " + uri.toString());
                            mDialService.sendMessageToUI("URI: " + uri.toString());

                            /*Intent intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("http://www.youtube.com/tv?" + params));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mDialService.startActivity(intent);*/

                            Intent i = new Intent("android.intent.action.MAIN");
                            i.setComponent(ComponentName.unflattenFromString("com.android.chrome/com.android.chrome.Main"));
                            i.addCategory("android.intent.category.LAUNCHER");
                            i.setData(uri);
                            mDialService.startActivity(i);
                        } catch (ActivityNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    }
                } else {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("VIDEO_ID", videoId);
                        mDialService.startActivity(intent);
                    } catch (ActivityNotFoundException ex) {
                        Intent intent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://www.youtube.com/watch?v=" + videoId));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mDialService.startActivity(intent);
                    }
                }
            } catch (Exception e) {
                Logger.e(e, "Parse videoId");
                e.printStackTrace();
            }
        } else {
            Logger.e("AppName: " + appName);
        }
    }

    /***
     * @param out
     */
    public void sendApplicationInformationResponse(BufferedWriter out) {

        try {
            mDialService.sendTCPMessage(out, Constant.APP_INFO_RESPONSE);
            mDialService.sendMessageToUI("App Info Response Sent:\n" + Constant.APP_INFO_RESPONSE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /****
     * @param appName
     * @param out
     */
    public void sendSpecificApplicationInformationResponse(String appName, BufferedWriter out) {

        try {
            String name = appName;
            String options = "true";
            String state = mAppState.name();

            String link = "";
            if (mAppState == Constant.APP_STATE.running) {
                link = String.format(Constant.APP_SPECIFIC_INFO_RESPONSE_LINK, mDialService.getLocalIpAddress(), appName);
                mDialService.getLocalIpAddress();
            }

            final String message = String.format(Constant.APP_SPECIFIC_INFO_RESPONSE,
                    name, options, state, link);
            mDialService.sendTCPMessage(out, message);
            mDialService.sendMessageToUI("App Info Response Sent:\n" + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /****
     * @param appName
     * @param out
     */
    public void sendApplicationLaunchResponse(String appName, BufferedWriter out) {

        try {
            String localIpAddress = mDialService.getLocalIpAddress();
            final String message = String.format(Constant.APP_LAUNCH_RESPONSE,
                    localIpAddress, appName);
            mDialService.sendTCPMessage(out, message);
            mDialService.sendMessageToUI("App Launch Response Sent:\n" + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isDialRestServiceRunning() {
        return isDialRestServiceRunning;
    }

    public void exitFromApp() {

        this.isDialRestServiceRunning = false;
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (mServerSocket != null) {
                mServerSocket.close();
                mServerSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setIsDialRestServiceRunning(boolean isDialRestServiceRunning) {
        this.isDialRestServiceRunning = isDialRestServiceRunning;
    }
}
