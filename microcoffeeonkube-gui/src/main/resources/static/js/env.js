/**
 * Contains environment specific configuration of the application.
 *
 * Assigns __env to the root window object.
 */
(function(window) {
    window.__env = window.__env || {};

    // REST services (http)
//    window.__env.locationServiceUrl = 'http://192.168.99.100:8081';
//    window.__env.menuServiceUrl = 'http://192.168.99.100:8082';
//    window.__env.orderServiceUrl = 'http://192.168.99.100:8082';

    // REST services (https)
//    window.__env.locationServiceUrl = 'https://192.168.99.100:8444';
//    window.__env.menuServiceUrl = 'https://192.168.99.100:8445';
//    window.__env.orderServiceUrl = 'https://192.168.99.100:8445';

    // REST services (http)
    window.__env.locationServiceUrl = 'http://${MICROCOFFEE_WEB_HOST}:${MICROCOFFEE_WEB_PORT_LOCATION_HTTP}';
    window.__env.menuServiceUrl = 'http://${MICROCOFFEE_WEB_HOST}:${MICROCOFFEE_WEB_PORT_ORDER_HTTP}';
    window.__env.orderServiceUrl = 'http://${MICROCOFFEE_WEB_HOST}:${MICROCOFFEE_WEB_PORT_ORDER_HTTP}';

    // REST services (https)
    window.__env.locationServiceUrl = 'https://${MICROCOFFEE_WEB_HOST}:${MICROCOFFEE_WEB_PORT_LOCATION_HTTPS}';
    window.__env.menuServiceUrl = 'https://${MICROCOFFEE_WEB_HOST}:${MICROCOFFEE_WEB_PORT_ORDER_HTTPS}';
    window.__env.orderServiceUrl = 'https://${MICROCOFFEE_WEB_HOST}:${MICROCOFFEE_WEB_PORT_ORDER_HTTPS}';

    // Logging
    window.__env.enableDebug = true;

}(this));
