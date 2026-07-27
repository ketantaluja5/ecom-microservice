package com.ecommerce.order.clients;

import com.ecommerce.order.dtos.ProductResponse;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.Optional;

//@Configuration
//public class ProductServiceClientConfig {

//    @Bean
//    @LoadBalanced
//    public RestClient.Builder restClientBuilder(){
//        return RestClient.builder();
//    }

//    @Bean
//    public ProductServiceClient restClientInterface(RestClient.Builder builder){
//        RestClient restClient = builder.baseUrl("http://product-service").build();
//        RestClientAdapter adapter = RestClientAdapter.create(restClient);
//        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
//        ProductServiceClient productServiceClient = factory.createClient(ProductServiceClient.class);
//        return productServiceClient;
//    }
//}

@Configuration
public class ProductServiceClientConfig {

    @Bean
    ProductServiceClient productServiceClient(
            LoadBalancerClient loadBalancerClient) {

        ClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory();

        RestClient restClient = RestClient.builder()
                .requestInterceptor(new LoadBalancerInterceptor(loadBalancerClient))
                .requestFactory(requestFactory)
                .baseUrl("http://product-service")
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (request, response) -> Optional.empty())
                .build();

        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory.builderFor(
                                RestClientAdapter.create(restClient))
                        .build();

        return factory.createClient(ProductServiceClient.class);
    }
}