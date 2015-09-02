//package com.ril.dialserver.service_discovery;
//
//import android.os.Build;
//import android.provider.Settings;
//
//import com.ril.dialserver.services.DialUDPService;
//import com.ril.dialserver.utils.Constant;
//
//import java.io.BufferedWriter;
//import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.net.Socket;
//
//public class InitialTCPSender extends Thread {
//
//    private volatile Socket mSenderSocket;
//    private DialUDPService mDialService;
//
//    /***
//     *
//     * @param dialService
//     * @param socket
//     * @throws IOException
//     */
//    public InitialTCPSender(DialUDPService dialService, Socket socket) throws IOException {
//        this.mDialService = dialService;
//        this.mSenderSocket = socket;
//    }
//
//    @Override
//    public void run() {
//        ////super.run();
//
//        try {
//            String modelName = Build.MODEL;
//            String manufacturer = Build.MANUFACTURER;
//            String friendlyName = Build.BRAND;
//            String UDN = Settings.Secure.getString(mDialService.getContentResolver(),
//                    Settings.Secure.ANDROID_ID);
//
//            final String message = String.format(Constant.DEVICE_DESC_RESPONSE, friendlyName, manufacturer, modelName, UDN);
//            sendTCPMessage(message);
//            mDialService.sendMessageToUI("TCP xml Response Sent:\n" + message);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /***
//     *
//     * @param message
//     * @throws IOException
//     */
//    protected void sendTCPMessage(String message) throws IOException {
//        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(mSenderSocket.getOutputStream()));
//        String outMsg = message + System.getProperty("line.separator");
//        out.write(outMsg);
//        out.flush();
//    }
//}
