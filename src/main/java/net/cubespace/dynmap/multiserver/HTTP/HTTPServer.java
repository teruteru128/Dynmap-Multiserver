package net.cubespace.dynmap.multiserver.HTTP;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.cubespace.dynmap.multiserver.Config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class HTTPServer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(HTTPServer.class);
    private final ServerConfig config;

    public HTTPServer(ServerConfig config) {
        this.config = config;
    }

    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup(config.Webserver_WorkerThreads);

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new HTTPServerInitializer(config));

            logger.info("Bounding to {}:{}", config.Webserver_IP, config.Webserver_Port);
            b.bind(config.Webserver_IP, config.Webserver_Port).sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("Could not bind to that IP", e);
            System.exit(-1);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
