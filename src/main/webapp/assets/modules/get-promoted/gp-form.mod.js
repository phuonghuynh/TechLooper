techlooper.directive("getPromotedForm", function ($http, userPromotionService, $location, utils, $q) {
  return {
    restrict: "E",
    replace: true,
    templateUrl: "modules/get-promoted/gp-form.tem.html",
    link: function (scope, element, attr, ctrl) {

      //TODO refactor to remove ignoreValidation
      scope.doPromotion = function (ignoreValidation) {
        scope.promotionForm.$setSubmitted();
        if (!ignoreValidation && !scope.promotionForm.$valid) {
          return false;
        }
        scope.masterPromotion = angular.copy(scope.promotionInfo);
        userPromotionService.refinePromotionInfo(scope.masterPromotion);
        $http.post("getPromoted", scope.masterPromotion).success(function (data, status, headers, config) {
          scope.masterPromotion.result = data;
          scope.changeState('result');
        });
      }

      scope.viewsDefers.getPromotedForm.resolve();
    }
  }
});