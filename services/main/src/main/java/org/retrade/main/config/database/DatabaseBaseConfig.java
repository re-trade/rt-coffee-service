package org.retrade.main.config.database;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {"org.retrade.main.repository.jpa"})
@EnableElasticsearchRepositories(basePackages = "org.retrade.main.repository.elasticsearch")
@EnableRedisRepositories(basePackages = "org.retrade.main.repository.redis")
public class DatabaseBaseConfig {
}
