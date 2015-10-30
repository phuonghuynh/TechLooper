package com.techlooper.service.impl;

import com.techlooper.entity.ChallengeRegistrantDto;
import com.techlooper.entity.ChallengeRegistrantEntity;
import com.techlooper.model.ChallengePhaseEnum;
import com.techlooper.model.ChallengeRegistrantPhaseItem;
import com.techlooper.repository.elasticsearch.ChallengeRegistrantRepository;
import com.techlooper.service.ChallengeRegistrantService;
import com.techlooper.service.ChallengeService;
import org.dozer.Mapper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

import static com.techlooper.model.ChallengePhaseEnum.*;
import static org.elasticsearch.index.query.FilterBuilders.*;
import static org.elasticsearch.index.query.QueryBuilders.*;

@Service
public class ChallengeRegistrantServiceImpl implements ChallengeRegistrantService {

  @Resource
  private ElasticsearchTemplate elasticsearchTemplate;

  private final List<ChallengePhaseEnum> CHALLENGE_PHASES = Arrays.asList(FINAL, PROTOTYPE, UIUX, IDEA);

  @Resource
  private ChallengeService challengeService;

  @Resource
  private ChallengeRegistrantRepository challengeRegistrantRepository;

  @Resource
  private Mapper dozerMapper;

  public Map<ChallengePhaseEnum, ChallengeRegistrantPhaseItem> countNumberOfRegistrantsByPhase(Long challengeId) {
    Map<ChallengePhaseEnum, ChallengeRegistrantPhaseItem> numberOfRegistrantsByPhase = new HashMap<>();

    NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder().withIndices("techlooper")
      .withTypes("challengeRegistrant").withSearchType(SearchType.COUNT);
    searchQueryBuilder.withQuery(termQuery("challengeId", challengeId));

    Long numberOfRegistrants = elasticsearchTemplate.count(searchQueryBuilder.build());
    numberOfRegistrantsByPhase.put(REGISTRATION, new ChallengeRegistrantPhaseItem(REGISTRATION, numberOfRegistrants));

    searchQueryBuilder.addAggregation(AggregationBuilders.terms("sumOfRegistrants").field("activePhase"));
    Aggregations aggregations = elasticsearchTemplate.query(searchQueryBuilder.build(), SearchResponse::getAggregations);
    Terms terms = aggregations.get("sumOfRegistrants");


    Long previousPhase = 0L;
    for (ChallengePhaseEnum phaseEnum : CHALLENGE_PHASES) {
      Terms.Bucket bucket = terms.getBucketByKey(phaseEnum.getValue());
      if (bucket != null) {
        numberOfRegistrantsByPhase.put(phaseEnum, new ChallengeRegistrantPhaseItem(phaseEnum,
          bucket.getDocCount() + previousPhase));
        previousPhase = bucket.getDocCount() + previousPhase;
      }
      else {
        bucket = terms.getBucketByKey(phaseEnum.getValue().toLowerCase());
        if (bucket != null) {
          numberOfRegistrantsByPhase.put(phaseEnum, new ChallengeRegistrantPhaseItem(phaseEnum,
            bucket.getDocCount() + previousPhase));
          previousPhase = bucket.getDocCount() + previousPhase;
        }
        else {
          numberOfRegistrantsByPhase.put(phaseEnum, new ChallengeRegistrantPhaseItem(phaseEnum, previousPhase));
        }
      }
    }
    return numberOfRegistrantsByPhase;
  }

  public Long countNumberOfWinners(Long challengeId) {
    NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder().withIndices("techlooper")
      .withTypes("challengeRegistrant").withSearchType(SearchType.COUNT);

    BoolFilterBuilder boolFilterBuilder = boolFilter();
    boolFilterBuilder.must(termFilter("challengeId", challengeId));
    boolFilterBuilder.must(termFilter("activePhase", ChallengePhaseEnum.FINAL.getValue()));
    boolFilterBuilder.mustNot(missingFilter("criteria"));

    searchQueryBuilder.withQuery(filteredQuery(matchAllQuery(), boolFilterBuilder));
    return elasticsearchTemplate.count(searchQueryBuilder.build());
  }

  public Set<ChallengeRegistrantDto> findRegistrantsByChallengeIdAndPhase(Long challengeId, ChallengePhaseEnum phase, String ownerEmail) {
    if (!challengeService.isOwnerOfChallenge(ownerEmail, challengeId)) {
      return null;
    }

    if (phase == WINNER) {
      return findWinnerRegistrantsByChallengeId(challengeId);
    }

    BoolQueryBuilder challengeQuery = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("challengeId", challengeId));
    BoolQueryBuilder toPhaseQuery = QueryBuilders.boolQuery();
    challengeQuery.must(toPhaseQuery);

    if (phase == REGISTRATION) {
      toPhaseQuery.should(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders.missingFilter("activePhase")));
    }
    for (int i = ChallengePhaseEnum.ALL_CHALLENGE_PHASES.length - 1; i >= 0; i--) {
      toPhaseQuery.should(QueryBuilders.termQuery("activePhase", ALL_CHALLENGE_PHASES[i]));
      if (phase == ALL_CHALLENGE_PHASES[i]) break;
    }

    Iterable<ChallengeRegistrantEntity> registrantIterable = challengeRegistrantRepository.search(challengeQuery);
    return toChallengeRegistrantDtosWithSubmissions(registrantIterable);
  }

  public Set<ChallengeRegistrantDto> findWinnerRegistrantsByChallengeId(Long challengeId) {

    BoolQueryBuilder winnerQuery = QueryBuilders.boolQuery()
      .must(QueryBuilders.termQuery("challengeId", challengeId))
      .must(QueryBuilders.termQuery("activePhase", ChallengePhaseEnum.FINAL))
      .must(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders.existsFilter("criteria.score")));

    Iterable<ChallengeRegistrantEntity> registrantIterable = challengeRegistrantRepository.search(winnerQuery);
    return toChallengeRegistrantDtosWithSubmissions(registrantIterable);
  }

  private Set<ChallengeRegistrantDto> toChallengeRegistrantDtosWithSubmissions(Iterable<ChallengeRegistrantEntity> registrantIterable) {
    Set<ChallengeRegistrantDto> registrantDtos = new HashSet<>();
    registrantIterable.forEach(entity -> {
      ChallengeRegistrantDto dto = dozerMapper.map(entity, ChallengeRegistrantDto.class);
      dto.setSubmissions(challengeService.findChallengeSubmissionByRegistrant(entity.getChallengeId(), entity.getRegistrantId()));
      registrantDtos.add(dto);
    });

    return registrantDtos;
  }
}
