package com.dialserver.utils;

/**
 * Created by infotel5 on 10/8/15.
 */
public interface Constant {

    enum APP_STATE {
        running, stopped, installable;
    }

    String DIAL_MULTICAST_ADDRESS = "239.255.255.250";
    ////String DIAL_MULTICAST_ADDRESS = "224.0.0.251";      // MDNS for ChromeCast V2

    int DIAL_UDP_PORT = 1900;
    //int DIAL_UDP_PORT = 5353;           // MDNS for ChromeCast V2

    int DIAL_LOCAL_PORT_SERVICE_DISCOVERY = 52235;
    int DIAL_LOCAL_PORT_REST_SERVICE = 12345;

    int DIAL_UDP_MULTICAST_SOCKET_TIMEOUT = 10000;
    int DIAL_UDP_MULTICAST_SOCKET_MAX_PACKET_BYTES = 512;

    String HTTP_PROTOCOL = "HTTP/1.1";
    String SEARCH_TARGET = "ST: urn:dial-multiscreen-org:service:dial:1";

    /*String SEARCH_TARGET = "ST:urn:schemas-upnp-org:device:InternetGatewayDevice:1";*/

    String M_SEARCH_COMPARE_1 = "M-SEARCH * " + HTTP_PROTOCOL;
    String M_SEARCH_COMPARE_2 = "HOST: " + DIAL_MULTICAST_ADDRESS + ":" + DIAL_UDP_PORT;
    /*String M_SEARCH_COMPARE_2 = "Host:" + DIAL_MULTICAST_ADDRESS + ":" + DIAL_UDP_PORT;*/
    String M_SEARCH_COMPARE_3 = SEARCH_TARGET;

    /*String M_SEARCH_REQUEST = "M-SEARCH * HTTP/1.1\r\n" + "HOST: " + DIAL_MULTICAST_ADDRESS + ":" + DIAL_UDP_PORT + "\r\n" + "MAN: \"ssdp:discover\"\r\n" + "MX: 10\r\n" + "ST: "
            + SEARCH_TARGET + "\r\n\r\n";*/

    String M_SEARCH_RESPONSE = HTTP_PROTOCOL + " 200 OK\r\n" + "LOCATION: %s\r\n" +
            "CACHE-CONTROL: max-age=1800\r\n" + "EXT:\r\n" +
            "BOOTID.UPNP.ORG: 1\r\n" +
            "SERVER: Android/version UPnP/1.1 product/version\r\n" +
            SEARCH_TARGET + "\r\n\r\n";

    String DEVICE_DESC_REQUEST = "GET /dd.xml " + HTTP_PROTOCOL;
    String DEVICE_DESC_RESPONSE = HTTP_PROTOCOL + " 200 OK\r\n"
            + "Content-Type: application/xml\r\n"
            + "Application-URL: http://%s" + ":" + DIAL_LOCAL_PORT_REST_SERVICE + "/apps\r\n\n"
            + "<?xml version=\"1.0\"?>" +
            "<root" +
            "  xmlns=\"urn:schemas-upnp-org:device-1-0\"" +
            "  xmlns:r=\"urn:restful-tv-org:schemas:upnp-dd\">" +
            "  <specVersion>" +
            "    <major>1</major>" +
            "    <minor>0</minor>" +
            "  </specVersion>" +
            "  <device>" +
            "    <deviceType>urn:schemas-upnp-org:device:tvdevice:1</deviceType>" +
            "    <friendlyName>%s</friendlyName>" +
            "    <manufacturer>%s</manufacturer>" +
            "    <modelName>%s</modelName>" +
            "    <UDN>uuid:%s</UDN>" +
            "  </device>" +
            "</root>";


    String APP_INFO_REQUEST = "GET /apps " + HTTP_PROTOCOL;
    /*HTTP/1.1 404
    Not Found
    text/plain
    Content-Length:30
    Connection:close*/
    String APP_INFO_RESPONSE = HTTP_PROTOCOL + " 404 Not Found\r\n"
            + "Content-Type: text/plain\r\n"
            + "Content-Length: 30\r\n"
            + "Connection: close";

    String APP_SPECIFIC_INFO_REQUEST = "GET /apps/";

    String APP_SPECIFIC_INFO_RESPONSE = HTTP_PROTOCOL + " 200 OK\r\n"
            + "Content-Type: application/xml\r\n\n"
            + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<service xmlns=\"urn:dial-multiscreen-org:schemas:dial\" dialVer=\"1.7\">" +
            "    <name>%s</name>" +
            "    <options allowStop = \"%s\" />" +
            "    <state>%s</state>" +
            "    %s" +
            "<additionalData></additionalData>" +
            "</service>";

    String APP_LAUNCH_REQUEST = "POST /apps/";

    String APP_LAUNCH_RESPONSE = HTTP_PROTOCOL + " 201 CREATED\r\n"
            + "LOCATION: http://%s" + ":" + DIAL_LOCAL_PORT_REST_SERVICE + "/apps/%s/run\r\n"
            + "Access-Control-Allow-Origin: package:com.google.android.youtube\r\n" + "\r\n";

    String APP_SPECIFIC_INFO_RESPONSE_LINK = "<link rel=\"run\" href=\"http://%s" + ":" + DIAL_LOCAL_PORT_REST_SERVICE + "/apps/%s/run\" />";

    int SERVICE_CONNECTED = 1;
    int SERVICE_DISCONNECTED = 2;

    int SERVICE_SENT_DATA = 10;

    String APP_NAME_YOU_TUBE = "YouTube";
}


/* Received HTTP method GET
Received HTTP method GET
Received request /apps/YouTube
        Origin package:com.google.android.youtube, Host: 192.168.5.6:56789
        Sending SSDP reply to 192.168.5.4:50093
        Received HTTP method GET
        Received request /apps/YouTube
        Origin package:com.google.android.youtube, Host: 192.168.5.6:56789
        Received HTTP method POST
        Received request /apps/YouTube
        Origin package:com.google.android.youtube, Host: 192.168.5.6:56789
        Payload: checking 64 bytes
        Starting the app with params pairingCode=8b53fd1a-56bf-44b9-a99c-2ac12d72a6d7&theme=cl&v=&t=0&additionalDataUrl=http%3A%2F%2Flocalhost%3A56789%2Fapps%2FYouTube%2Fdial_data%3F


        ** LAUNCH YouTube ** with args pairingCode=8b53fd1a-56bf-44b9-a99c-2ac12d72a6d7&theme=cl&v=&t=0&additionalDataUrl=http%3A%2F%2Flocalhost%3A56789%2Fapps%2FYouTube%2Fdial_data%3F

        Execute:
        0) /opt/google/chrome/google-chrome
        1) --user-agent=Mozilla/5.0 (PS3; Leanback Shell) AppleWebKit/535.22 (KHTML, like Gecko) Chrome/19.0.1048.0 LeanbackShell/01.00.01.73 QA Safari/535.22 Sony PS3/ (PS3, , no, CH)
        2) --user-data-dir=/home/infotel5/.config/google-chrome-dial
        3) --app
        4) https://www.youtube.com/tv?pairingCode=8b53fd1a-56bf-44b9-a99c-2ac12d72a6d7&theme=cl&v=&t=0&additionalDataUrl=http%3A%2F%2Flocalhost%3A56789%2Fapps%2FYouTube%2Fdial_data%3F
        [10969:10969:0825/131342:ERROR:sandbox_linux.cc(345)] InitializeSandbox() called with multiple threads in process gpu-process

*/