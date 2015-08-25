package com.techlooper.repository.userimport;

import com.techlooper.entity.SalaryReviewEntity;
import com.techlooper.entity.WebinarEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by phuonghqh on 8/18/15.
 */
@Repository
public interface WebinarRepository extends ElasticsearchRepository<WebinarEntity, Long> {
}