package de.muv1n.muvbot.config;

import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate discordRestTemplate() {
        DnsResolver ipv4First = new SystemDefaultDnsResolver() {
            @Override
            public InetAddress[] resolve(String host) throws java.net.UnknownHostException {
                InetAddress[] addresses = super.resolve(host);
                Arrays.sort(addresses, (a, b) -> {
                    boolean aV4 = a instanceof Inet4Address;
                    boolean bV4 = b instanceof Inet4Address;
                    if (aV4 && !bV4) return -1;
                    if (!aV4 && bV4) return 1;
                    return 0;
                });
                return addresses;
            }
        };

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(
                        PoolingHttpClientConnectionManagerBuilder.create()
                                .setDnsResolver(ipv4First)
                                .setDefaultSocketConfig(
                                        SocketConfig.custom()
                                                .setSoTimeout(10, TimeUnit.SECONDS)
                                                .build()
                                )
                                .setMaxConnTotal(100)
                                .setMaxConnPerRoute(20)
                                .build()
                )
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(5_000);

        return new RestTemplate(factory);
    }
}
