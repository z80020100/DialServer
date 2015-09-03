package com.dialserver.service_discovery;

import android.os.Build;
import android.provider.Settings;
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

public class InitiateDialServiceDiscovery extends Thread {

    /*The Java volatile keyword is used to mark a Java variable as "being stored in main memory".
    More precisely that means, that every read of a volatile variable will be read from the computer's
    main memory, and not from the CPU cache, and that every write to a volatile variable will be written
    to main memory, and not just to the CPU cache.*/
    private volatile ServerSocket mServerSocket = null;
    private DialUDPService mDialService;
    private Socket socket = null;
    private boolean isDialServiceDiscoveryRunning = true;

    /***
     * @param dialService
     * @throws IOException
     */
    public InitiateDialServiceDiscovery(DialUDPService dialService) {
        this.mDialService = dialService;
    }

    @Override
    public void run() {
        try {
            this.mServerSocket = new ServerSocket(Constant.DIAL_LOCAL_PORT_SERVICE_DISCOVERY);
            while (isDialServiceDiscoveryRunning) {

                if (mServerSocket == null || mServerSocket.isClosed()) {
                    isDialServiceDiscoveryRunning = false;
                    break;
                }
                //Thread.sleep(5000);
                try {
                    socket = mServerSocket.accept();
                    if (!socket.isClosed()) {
                        socket.setKeepAlive(true);
                        socket.setTcpNoDelay(true);
                        startDialServiceDiscovery(socket);
                    } else {
                        Logger.e("InitiateDialServiceDiscovery socket is closed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.e(e, "InitiateDialServiceDiscovery while");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e(e, "InitiateDialServiceDiscovery RUN");
        } finally {
        }
    }

    private void startDialServiceDiscovery(Socket socket) {
        new RunDialServiceDiscovery(socket).start();
    }

    private class RunDialServiceDiscovery extends Thread {

        private Socket mSocket;

        /**
         * @param socket
         */
        private RunDialServiceDiscovery(Socket socket) {
            this.mSocket = socket;
        }

        @Override
        public void run() {
            super.run();
            try {
                receiveDeviceDescriptionRequest(mSocket);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mDialService.stopDiscovery(mSocket);
            }
        }

        /****
         * @param socket
         * @throws IOException
         */
        private void receiveDeviceDescriptionRequest(Socket socket) throws Exception {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            ////String msg = mDialService.readDataFromSocket(in);

            String msg = in.readLine();

            /*if(!TextUtils.isEmpty(msg)){
                mDialService.sendMessageToUI("BufferedWriter output DSD:\n" + msg);
            }*/

            Logger.e("receiveDeviceDescriptionRequest: "+msg);
            ////in.close();
            if (!TextUtils.isEmpty(msg) && msg.contains(Constant.DEVICE_DESC_REQUEST)) {
                ////boolean isConnected = socket.isConnected();
                mDialService.sendMessageToUI("Device Description Request Received:\n" + msg);

                final String message = msg + System.getProperty("line.separator");
                sendDeviceDescriptionResponse(out);
            }
        }

        /****
         * @param out
         */
        public void sendDeviceDescriptionResponse(BufferedWriter out) {
            ////super.run();
            try {
                String modelName = Build.MODEL;
                String manufacturer = Build.MANUFACTURER;
                String friendlyName = Build.BRAND;
                String UDN = Settings.Secure.getString(mDialService.getContentResolver(),
                        Settings.Secure.ANDROID_ID);

                final String message = String.format(Constant.DEVICE_DESC_RESPONSE,
                        mDialService.getLocalIpAddress(), friendlyName, manufacturer, modelName, UDN);
                mDialService.sendTCPMessage(out, message);
                mDialService.sendMessageToUI("Device Description Response Sent:\n" + message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isDialServiceDiscoveryRunning() {
        return isDialServiceDiscoveryRunning;
    }

    public void setIsDialServiceDiscoveryRunning(boolean isDialServiceDiscoveryRunning) {
        this.isDialServiceDiscoveryRunning = isDialServiceDiscoveryRunning;
    }

    public void exitFromApp() {
        this.isDialServiceDiscoveryRunning = false;
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
}
