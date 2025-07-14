package org.retrade.main.config.database;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {"org.retrade.main.repository.jpa"})
@EnableElasticsearchRepositories(basePackages = "org.retrade.main.repository.elasticsearch")
public class DatabaseBaseConfig {
}
