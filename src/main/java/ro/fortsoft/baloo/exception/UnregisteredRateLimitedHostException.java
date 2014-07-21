package ro.fortsoft.baloo.exception;

import org.apache.http.client.ClientProtocolException;

/**
 * Exception thrown by when the host is not found with a
 *
 * @author Serban Balamaci
 */
public class UnregisteredRateLimitedHostException extends ClientProtocolException {

    private String host;

    public UnregisteredRateLimitedHostException(String host) {
        super("Host " + host + " is not registered with a preset limit, use RequestLimitedHttpClient.addLimit()");
        this.host = host;
    }

    public String getHost() {
        return host;
    }
}
