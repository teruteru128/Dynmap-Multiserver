package net.cubespace.dynmap.multiserver.Config;

import java.util.ArrayList;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class ServerConfig {
    public ServerConfig() {
        DynMap.add(new Dynmap());
    }

    public String Webserver_IP = "0.0.0.0";
    public Integer Webserver_Port = 8080;
    public String Webserver_webDir = "web/";
    public String Webserver_Title = "Awesome Multiserver Dynmap";
    public Integer Webserver_WorkerThreads = 16;
    public boolean Webserver_EnableSsl = false;
    public String Webserver_CertificateFile = null;
    public String Webserver_PrivateKeyFile = null;
    public String Webserver_TlsPassword = null;
    public ArrayList<Dynmap> DynMap = new ArrayList<>();
}
