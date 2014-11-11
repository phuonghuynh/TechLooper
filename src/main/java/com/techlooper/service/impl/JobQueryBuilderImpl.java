package com.techlooper.service.impl;

import com.techlooper.model.TechnicalTermEnum;
import com.techlooper.service.JobQueryBuilder;
import com.techlooper.util.EncryptionUtils;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Created by phuonghqh on 11/8/14.
 */
@Component
public class JobQueryBuilderImpl implements JobQueryBuilder {

  public QueryBuilder getTechnicalTermsQuery() {
    final BoolQueryBuilder technicalTermsQuery = QueryBuilders.boolQuery();
    final FilterBuilder isActiveJobFilter = FilterBuilders.termFilter("isActive", 1);
    Stream.of(TechnicalTermEnum.values())
      .map(term -> QueryBuilders.filteredQuery(
        QueryBuilders.multiMatchQuery(term, SEARCH_JOB_FIELDS).operator(MatchQueryBuilder.Operator.AND), isActiveJobFilter))
      .forEach(technicalTermsQuery::should);
    return technicalTermsQuery;
  }

  public QueryBuilder getTechnicalTermQuery(TechnicalTermEnum term) {
    return QueryBuilders.multiMatchQuery(term, SEARCH_JOB_FIELDS).operator(MatchQueryBuilder.Operator.AND);
  }

  public QueryBuilder getTechnicalSkillQuery(String skill) {
    return QueryBuilders.multiMatchQuery(skill, SEARCH_JOB_FIELDS).operator(MatchQueryBuilder.Operator.AND);
  }

  public NativeSearchQueryBuilder getVietnamworksJobQuery() {
    return new NativeSearchQueryBuilder().withIndices(ES_VIETNAMWORKS_INDEX).withTypes("job");
  }

  public AggregationBuilder getTechnicalTermAggregation(TechnicalTermEnum term) {
    return AggregationBuilders.filter(term.name()).filter(FilterBuilders.queryFilter(this.getTechnicalTermQuery(term)));
  }

  public FilterAggregationBuilder getSkillIntervalAggregation(String skill, QueryBuilder skillQuery, String period, Integer interval) {
    String intervalDate = LocalDate.now().minusDays(interval).format(DateTimeFormatter.ofPattern("YYYYMMdd"));
    QueryBuilder approveDateQuery = QueryBuilders.rangeQuery("approvedDate").to("now-" + interval + period);
    BoolQueryBuilder filterQuery = QueryBuilders.boolQuery().must(skillQuery).must(approveDateQuery);
    return AggregationBuilders.filter(EncryptionUtils.encodeHexa(skill) + "-" + period + "-" + intervalDate)
      .filter(FilterBuilders.queryFilter(filterQuery));
  }

  public List<List<FilterAggregationBuilder>> toSkillAggregations(List<String> skills) {
    return skills.stream().map(skill -> {
      QueryBuilder skillQuery = this.getTechnicalSkillQuery(skill);

      List<FilterAggregationBuilder> builders = new LinkedList<>();
      for (int i = 0; i < 32; ++i) {
        builders.add(this.getSkillIntervalAggregation(skill, skillQuery, "d", i));
      }
      builders.add(this.getSkillIntervalAggregation(skill, skillQuery, "w", 0));
      builders.add(this.getSkillIntervalAggregation(skill, skillQuery, "w", 1));

      return builders;
    }).collect(toList());
  }

//  public abstract int getLastNumberOfDays();

}
