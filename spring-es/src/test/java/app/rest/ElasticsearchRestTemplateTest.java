package app.rest;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;

/**
 * @author faith.huan 2019-10-20 11:13
 */
@SpringBootTest
@Slf4j
class ElasticsearchRestTemplateTest {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Test
    void get() {
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withIndices("es_doc")
                .withTypes("_doc")
                .withQuery(new MatchAllQueryBuilder())
                .build();
        long count = restTemplate.count(searchQuery);
        log.info("索引es_doc中有{}个文档", count);
    }
}
