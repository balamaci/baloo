package ro.fortsoft.baloo.httpclient;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import ro.fortsoft.baloo.exception.UnregisteredRateLimitedHostException;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Serban Balamaci
 */
public class RequestLimitedHttpClient implements HttpClient {

    private Map<String, RateLimiter> rateLimiters = Maps.newConcurrentMap();

    private final HttpClient httpClient;

    private final boolean throwExceptionForUnregisteredHost;

    /**
     * Constr.
     * @param httpClient httpClient
     * @param throwExceptionForUnregisteredHost if the request is to an unknown host that it's not registered with it's own limit
     *                                thrown exception instead of
     */
    public RequestLimitedHttpClient(HttpClient httpClient, boolean throwExceptionForUnregisteredHost) {
        this.httpClient = httpClient;
        this.throwExceptionForUnregisteredHost = throwExceptionForUnregisteredHost;
    }

    @Override
    public HttpParams getParams() {
        return httpClient.getParams();
    }

    @Override
    public ClientConnectionManager getConnectionManager() {
        return httpClient.getConnectionManager();
    }

    public HttpResponse execute(final HttpUriRequest request) throws IOException, ClientProtocolException {
        acquireRateLimitToken(request);

        return httpClient.execute(request);
    }

    public HttpResponse execute(final HttpUriRequest request, final HttpContext context)
            throws IOException, ClientProtocolException {
        Args.notNull(request, "HTTP request");
        acquireRateLimitToken(request);

        return httpClient.execute(request, context);
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException, ClientProtocolException {
        acquireRateLimitToken(target);
        return httpClient.execute(target, request);
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException,
            ClientProtocolException {
        acquireRateLimitToken(target);

        return httpClient.execute(target, request, context);
    }

    @Override
    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException,
            ClientProtocolException {
        acquireRateLimitToken(request);

        return httpClient.execute(request, responseHandler);
    }

    @Override
    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context)
            throws IOException, ClientProtocolException {
        acquireRateLimitToken(request);

        return httpClient.execute(request, responseHandler);
    }

    @Override
    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler)
            throws IOException, ClientProtocolException {
        acquireRateLimitToken(target);

        return httpClient.execute(target, request, responseHandler);
    }

    @Override
    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context)
            throws IOException, ClientProtocolException {
        acquireRateLimitToken(target);

        return httpClient.execute(target, request, responseHandler, context);
    }

    private static HttpHost determineTarget(final HttpUriRequest request) throws ClientProtocolException {
        // A null target may be acceptable if there is a default target.
        // Otherwise, the null target is detected in the director.
        HttpHost target = null;

        final URI requestURI = request.getURI();
        if (requestURI.isAbsolute()) {
            target = URIUtils.extractHost(requestURI);
            if (target == null) {
                throw new ClientProtocolException("URI does not specify a valid host name: "
                        + requestURI);
            }
        }
        return target;
    }

    private void acquireRateLimitToken(HttpUriRequest request) throws ClientProtocolException {
        HttpHost httpHost = determineTarget(request);
        acquireRateLimitToken(httpHost);
    }

    private void acquireRateLimitToken(HttpHost httpHost) throws ClientProtocolException {
        String host = httpHost.getHostName();

        RateLimiter rateLimiter = rateLimiters.get(host);
        if(rateLimiter == null) {
            if(throwExceptionForUnregisteredHost) {
                throw new UnregisteredRateLimitedHostException(host);
            }
        } else {
            rateLimiter.acquire();
        }
    }

    /**
     * Register a limit for a specific host
     * @param host a host string, like facebook.com, google.com
     * @param rate the requests per second
     */
    public void addLimit(String host, double rate) {
        RateLimiter rateLimiter = RateLimiter.create(rate);

        rateLimiters.put(host, rateLimiter);
    }

    /**
     * Register a limit for a specific host. See {@code RateLimiter.create}
     * @param host host a host string, like facebook.com, google.com
     * @param rate rate the requests per second
     * @param warmupPeriod the duration of the period where the {@code RateLimiter} ramps up its
     *        rate, before reaching its stable (maximum) rate
     * @param unit the time unit of the warmupPeriod argument
     */
    public void addLimit(String host, double rate, long warmupPeriod, TimeUnit unit) {
        RateLimiter rateLimiter = RateLimiter.create(rate, warmupPeriod, unit);

        rateLimiters.put(host, rateLimiter);
    }

}
