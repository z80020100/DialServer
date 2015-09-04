package com.dialserver.service_discovery;

import android.text.TextUtils;

import com.dialserver.services.DialUDPService;
import com.dialserver.utils.Constant;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

/**
 * Internal class for handling the network connection of the BroadCastFacade class on a background
 * thread.
 */
public class InitiateMulticastUDPConnection extends Thread {

    /*The Java volatile keyword is used to mark a Java variable as "being stored in main memory".
    More precisely that means, that every read of a volatile variable will be read from the computer's
    main memory, and not from the CPU cache, and that every write to a volatile variable will be written
    to main memory, and not just to the CPU cache.*/
    private MulticastSocket mMulticastSocket;
    private DialUDPService mDialService;
    private volatile boolean isDialMulticastUDPRunning = false;
    private InetAddress mMulticastIpAddress;
    private InetAddress mLocalIpAddress;

    /***
     * Create a new background thread that handles incoming Intents on the given broadcast
     * and port.
     * <p/>
     * Port no on to listen the server broadcast, It must be the same on which server broadcasting the commands.
     *
     * @param dialService
     */
    public InitiateMulticastUDPConnection(DialUDPService dialService) {

        synchronized (InitiateMulticastUDPConnection.this) {
            this.mDialService = dialService;
            try {
                mMulticastIpAddress = InetAddress.getByName(Constant.DIAL_MULTICAST_ADDRESS);
                mLocalIpAddress = InetAddress.getByName(mDialService.getLocalIpAddress());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        synchronized (InitiateMulticastUDPConnection.this) {
            isDialMulticastUDPRunning = true;
            try {
                createSocketAndReadDataFromUDP();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                stopDiscovery();
            }
        }
    }

    private void createSocket() throws IOException {
        if (mMulticastSocket == null) {
            mMulticastSocket = new MulticastSocket(Constant.DIAL_UDP_PORT);
            mMulticastSocket.joinGroup(mMulticastIpAddress);
            mMulticastSocket.setReuseAddress(true);

            ////mMulticastSocket.connect(mMulticastIpAddress,Constant.DIAL_UDP_PORT);
            ////mMulticastSocket.setSoTimeout(Constant.DIAL_UDP_MULTICAST_SOCKET_TIMEOUT);
        }
    }

    private void closeSocket() {
        if (mMulticastSocket != null) {
            /////mMulticastSocket.disconnect();
            mMulticastSocket.close();
            mMulticastSocket = null;
        }
    }

    private void stopDiscovery() {
        isDialMulticastUDPRunning = false;
        closeSocket();
    }

    private void createSocketAndReadDataFromUDP() throws IOException {
        byte[] buf;
        DatagramPacket packet;
        String message;
        while (isDialMulticastUDPRunning) {
            try {
                createSocket();
                buf = new byte[Constant.DIAL_UDP_MULTICAST_SOCKET_MAX_PACKET_BYTES];
                packet = new DatagramPacket(buf, buf.length);
                mMulticastSocket.receive(packet);
                message = new String(buf, 0, buf.length);
                if (!TextUtils.isEmpty(message)) {
                    onUDPMessageReceived(packet, message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                buf = null;
                packet = null;
                message = null;
            }
        }
    }

    /***
     * @param packet
     * @param message
     */
    private void onUDPMessageReceived(DatagramPacket packet, final String message) throws IOException {

        ////Logger.e("onUDPMessageReceived: "+ message);
        ////mDialService.sendMessageToUI("onUDPMessageReceived:\n" + message);
        ////if (message.contains(Constant.M_SEARCH_REQUEST)) {
        if (message.contains(Constant.M_SEARCH_COMPARE_1) &&
                message.contains(Constant.M_SEARCH_COMPARE_2) &&
                message.contains(Constant.M_SEARCH_COMPARE_3)) {

            mDialService.sendMessageToUI("M_SEARCH Request Received:\n" + message);

            InetAddress remoteAddress = packet.getAddress();
            int remotePort = packet.getPort();

            String location = "http://" + mLocalIpAddress.getHostAddress() + ":" + Constant.DIAL_LOCAL_PORT_SERVICE_DISCOVERY + "/dd.xml";

            String sendAckMsg = String.format(Constant.M_SEARCH_RESPONSE, location);

            DatagramPacket sendAckPacket = new DatagramPacket(sendAckMsg.getBytes(),
                    sendAckMsg.getBytes().length, remoteAddress, remotePort);

            mMulticastSocket.send(sendAckPacket);
            //////stopDiscovery();


            mDialService.sendMessageToUI("M_SEARCH Response sent:\n" + sendAckMsg);
        }
    }

    public void exitFromApp() {
        try {
            this.isDialMulticastUDPRunning = false;
            if (mMulticastSocket != null) {
                mMulticastSocket.disconnect();
                mMulticastSocket.close();
                mMulticastSocket = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isDialMulticastUDPRunning() {
        return isDialMulticastUDPRunning;
    }

    public void setIsDialMulticastUDPRunning(boolean isDialMulticastUDPRunning) {
        this.isDialMulticastUDPRunning = isDialMulticastUDPRunning;
    }
}
