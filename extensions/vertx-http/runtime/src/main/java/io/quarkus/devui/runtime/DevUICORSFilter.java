package io.quarkus.devui.runtime;

import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

import io.quarkus.vertx.http.runtime.cors.CORSFilter;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class DevUICORSFilter implements Handler<RoutingContext> {
    private DevUICORSConfig corsConfig;

    private static final Logger LOG = Logger.getLogger(DevUICORSFilter.class);

    private static final String HTTP_PORT_CONFIG_PROP = "quarkus.http.port";
    private static final String HTTPS_PORT_CONFIG_PROP = "quarkus.http.ssl-port";
    private static final String LOCAL_HOST = "localhost";
    private static final String LOCAL_HOST_IP = "127.0.0.1";
    private static final String HTTP_LOCAL_HOST = "http://" + LOCAL_HOST;
    private static final String HTTPS_LOCAL_HOST = "https://" + LOCAL_HOST;
    private static final String HTTP_LOCAL_HOST_IP = "http://" + LOCAL_HOST_IP;
    private static final String HTTPS_LOCAL_HOST_IP = "https://" + LOCAL_HOST_IP;

    public DevUICORSFilter(DevUICORSConfig corsConfig) {
        this.corsConfig = corsConfig;
    }

    private CORSFilter corsFilter() {
        if (this.corsConfig.origins.isEmpty()) {
            int httpPort = ConfigProvider.getConfig().getValue(HTTP_PORT_CONFIG_PROP, int.class);
            int httpsPort = ConfigProvider.getConfig().getValue(HTTPS_PORT_CONFIG_PROP, int.class);
            this.corsConfig.origins = Optional.of(List.of(
                    HTTP_LOCAL_HOST + ":" + httpPort,
                    HTTP_LOCAL_HOST_IP + ":" + httpPort,
                    HTTPS_LOCAL_HOST + ":" + httpsPort,
                    HTTPS_LOCAL_HOST_IP + ":" + httpsPort));
        }
        return new CORSFilter(this.corsConfig);
    }

    @Override
    public void handle(RoutingContext event) {
        corsFilter().handle(event);
    }
}
