package com.ecommerce.order.clients;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.Optional;

@Configuration
public class UserServiceClientConfig {

    @Autowired(required = false)
    private ObservationRegistry observationRegistry;

    @Autowired(required = false)
    private Tracer tracer;

    @Autowired(required = false)
    private Propagator propagator;

    @Bean
    UserServiceClient userServiceClient(LoadBalancerClient loadBalancerClient){
        ClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();

        RestClient.Builder builder = RestClient.builder()
                .requestInterceptor(new LoadBalancerInterceptor(loadBalancerClient))
                .requestFactory(requestFactory)
                .baseUrl("http://user-service")
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (request, response) -> Optional.empty());
//                .build();

        if (observationRegistry != null) {
            builder.requestInterceptor(createTracingInterceptor());
        }
        RestClient restClient = builder.build();


        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(
                RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(UserServiceClient.class);
    }

    private ClientHttpRequestInterceptor createTracingInterceptor() {
        return ((request, body, execution) ->
        {
            if(tracer != null && propagator != null && tracer.currentSpan() != null){
                propagator.inject(tracer.currentTraceContext().context(),
                        request.getHeaders(),
                        (carrier, key, value) -> {
                            assert carrier != null;
                            carrier.add(key,value);
                        });
            }
            return execution.execute(request,body);
        });
    }
}
