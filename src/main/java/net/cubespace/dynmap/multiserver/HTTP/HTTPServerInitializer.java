package net.cubespace.dynmap.multiserver.HTTP;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import net.cubespace.dynmap.multiserver.Config.ServerConfig;
import net.cubespace.dynmap.multiserver.HTTP.Handler.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.security.Provider;
import java.security.Security;


/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class HTTPServerInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger logger = LoggerFactory.getLogger(HTTPServerInitializer.class);
    private final ServerConfig config;

    static {
        try {
            if (Security.getProvider("BC") == null) {
                var clazz = Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider");
                var subclass = clazz.asSubclass(Provider.class);
                var constructor = subclass.getConstructor();
                var provider = constructor.newInstance();
                Security.addProvider(provider);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 IllegalArgumentException | InvocationTargetException | SecurityException e) {
            throw new InternalError(e);
        }
    }

    public HTTPServerInitializer(ServerConfig config) {
        super();

        this.config = config;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = ch.pipeline();

        if (config.Webserver_EnableTls) {
            if (config.Webserver_TlsPassword == null) {
                logger.debug("config.Webserver_TlsPassword is null");
            } else {
                logger.debug("config.Webserver_TlsPassword={}", config.Webserver_TlsPassword);
            }
            var context = SslContextBuilder.forServer(new File(config.Webserver_CertificateFile), new File(config.Webserver_PrivateKeyFile), config.Webserver_TlsPassword).build();
            pipeline.addFirst("ssl", new SslHandler(context.newEngine(ch.alloc())));
        }
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());

        HTTPServerHandler httpServerHandler = new HTTPServerHandler();

        ConfigJSHandler configJSHandler = new ConfigJSHandler();
        httpServerHandler.addHandler("/standalone/config.js", configJSHandler);

        DynmapConfigJSONHandler dynmapConfigJSONHandler = new DynmapConfigJSONHandler(config);
        httpServerHandler.addHandler("/standalone/dynmap_config.json", dynmapConfigJSONHandler);

        MarkerHandler markerHandler = new MarkerHandler();
        httpServerHandler.addHandler("/tiles/_markers_/.*.json", markerHandler);

        FacesFileHandler facesFileHandler = new FacesFileHandler();
        httpServerHandler.addHandler("/tiles/faces/.*", facesFileHandler);

        TileFileHandler tileFileHandler = new TileFileHandler();
        httpServerHandler.addHandler("/tiles/*", tileFileHandler);

        MapConfigHandler mapConfigHandler = new MapConfigHandler();
        httpServerHandler.addHandler("/standalone/world/.*", mapConfigHandler);

        //Add the Statichandler (must be the last one)
        StaticFileHandler staticFileHandler = new StaticFileHandler();
        staticFileHandler.setWebDir(config.Webserver_webDir);
        staticFileHandler.addIndex("index.html");
        httpServerHandler.addHandler(".*", staticFileHandler);

        pipeline.addLast("handler", httpServerHandler);
    }
}