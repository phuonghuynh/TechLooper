angular.module("Header").controller("headerController", function ($scope, headerService) {
  var chart = headerService.getChart();
  $('.btn-setting').click(headerService.showSetting);
  $("i[techlooper='chartsMenu']").click(headerService.changeChart);
  headerService.changeChart();
  //$scope.langKeys = jsonValue.availableLanguageKeys;
});