package com.techlooper.service;

import com.techlooper.entity.PriceJobReport;
import com.techlooper.entity.SalaryReview;
import com.techlooper.model.SalaryReviewSurvey;

/**
 * Created by NguyenDangKhoa on 3/19/15.
 */
public interface UserEvaluationService {

    void reviewSalary(SalaryReview salaryReview);

    void deleteSalaryReview(SalaryReview salaryReview);

    boolean saveSalaryReviewSurvey(SalaryReviewSurvey salaryReviewSurvey);

    void priceJob(PriceJobReport priceJobReport);
}
