package ro.fortsoft.baloo;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import ro.fortsoft.baloo.strategy.DelayOnUnregisteredHostHandlingStrategy;
import ro.fortsoft.baloo.httpclient.RequestLimitedHttpClient;

/**
 * @author Serban Balamaci
 */
public class Start {

    public static void main(String[] args) throws Exception {
        String SAMPLE_URL = "https://google.de";

        RequestLimitedHttpClient instance = new RequestLimitedHttpClient(HttpClientBuilder.
                create().build(), new DelayOnUnregisteredHostHandlingStrategy(1));
        instance.addLimit("google.de", 0.2);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(5000).setConnectTimeout(5000).setSocketTimeout(5000).build();
        HttpGet request = new HttpGet(SAMPLE_URL);
        request.setConfig(requestConfig);

        long prev = System.currentTimeMillis();
        for(int i=0; i < 10; i++) {
            long current = System.currentTimeMillis();
            System.out.println("Executing after " + (current - prev) + " ms");
            HttpResponse response = instance.execute(request);
            prev = current;
            EntityUtils.consume(response.getEntity());
        }
    }

}
