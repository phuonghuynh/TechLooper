package com.techlooper.config;

import com.techlooper.model.TechnicalTerm;
import com.techlooper.util.JsonUtils;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.client.transport.TransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.elasticsearch.client.TransportClientFactoryBean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Created by phuonghqh on 10/13/14.
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.techlooper.repository")
public class ElasticsearchConfiguration {

    @Resource
    private Environment environment;

    @Resource
    private TransportClient transportClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchConfiguration.class);

    @Bean
    public FactoryBean<TransportClient> transportClient() throws Exception {
        TransportClientFactoryBean factory = new TransportClientFactoryBean();
        factory.setClusterName(environment.getProperty("elasticsearch.cluster.name"));
        factory.setClusterNodes(environment.getProperty("elasticsearch.host"));
        return factory;
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() {
        return new ElasticsearchTemplate(transportClient);
    }
}
