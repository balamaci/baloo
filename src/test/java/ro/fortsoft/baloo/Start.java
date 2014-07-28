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

//        instance.addLimit("google.de", 0.2);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(5000).setConnectTimeout(5000).setSocketTimeout(5000).build();

        final RequestLimitedHttpClient instance = new RequestLimitedHttpClient(HttpClientBuilder.
                create().setDefaultRequestConfig(requestConfig).build(),
                new DelayOnUnregisteredHostHandlingStrategy(0.2));

        final HttpGet request = new HttpGet(SAMPLE_URL);

        for(int i=0; i < 10; i++) {
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    try {
                        long current = System.currentTimeMillis();
                        HttpResponse response = instance.execute(request);
                        EntityUtils.consume(response.getEntity());
                        long fin = System.currentTimeMillis();
                        System.out.println("Took " + (fin - current) + " ms");
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            };
            Thread th = new Thread(task);
            th.start();
        }
    }

}
