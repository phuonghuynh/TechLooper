techlooper.controller("jobListingController", function (apiService, $scope, vnwConfigService, $routeParams, $location, utils) {

  $scope.locationsConfig = vnwConfigService.locationsSearchSelectize;

  var searchText = $routeParams.searchText;

  if (!searchText) {
    apiService.listAllJobs().success(function(response) {
      $scope.totalPage = response.totalPage;
      $scope.totalJob = response.totalJob;
      $scope.page = response.page;
      $scope.jobs = response.jobs;
    });
  } else {
    var searchParams = searchText.split("+");
    var keyword = searchParams.length > 0 ? capitalizeWords(searchParams[0]) : "";
    var locationId = searchParams.length > 1 ? searchParams[1] : "";
    var page = $routeParams.page ? $routeParams.page : 1;

    var location = "";
    if (locationId) {
      location = utils.toAscii(vnwConfigService.getLocationText(locationId));
    }

    apiService.filterJob(keyword, location, page).success(function(response) {
      $scope.totalPage = response.totalPage;
      $scope.totalJob = response.totalJob;
      $scope.page = response.page;
      $scope.jobs = response.jobs;
    });

    $scope.searchJob = {keyword : keyword, locationId : locationId, location : location};
  }

  $scope.getPageRange = function() {
    var numberOfShownPages = 5;

    var start = 1;
    var end = numberOfShownPages;
    if ($scope.totalPage > numberOfShownPages) {
      var median = numberOfShownPages % 2 == 0 ? numberOfShownPages / 2 : Math.floor(numberOfShownPages / 2) + 1;
      var distance = median - 1;
      if ($scope.page > median) {
        if ($scope.totalPage - $scope.page < distance) {
          start = $scope.page - (distance + (numberOfShownPages - ($scope.totalPage - $scope.page) - median));
        } else {
          start = $scope.page - distance;
        }
        if ($scope.page + distance <= $scope.totalPage) {
          end = $scope.page + distance;
        } else {
          end = $scope.totalPage;
        }
      } else {
        start = 1;
        end = numberOfShownPages;
      }
    }

    var list = [];
    for (var i = start; i <= end; i++) {
      list.push(i);
    }

    $scope.previousPage = {
      isEnable : false,
      pageIndex : 1
    };

    $scope.nextPage = {
      isEnable : false,
      pageIndex : 1
    };

    if (start > 1 || (start == 1 && $scope.page > 1)) {
      $scope.previousPage.isEnable = true;
      $scope.previousPage.pageIndex = $scope.page - 1;
    }

    if ($scope.page < $scope.totalPage) {
      $scope.nextPage.isEnable = true;
      $scope.nextPage.pageIndex = $scope.page + 1;
    }

    return list;
  }

  $scope.filterJob = function() {
    var keyword = $scope.searchJob.keyword ? utils.toAscii($scope.searchJob.keyword) : "en";
    var locationId = $scope.searchJob.locationId ? $scope.searchJob.locationId : "";
    var location = "";
    if (locationId) {
      location = vnwConfigService.getLocationText(locationId, "en");
    }
    var page = 1;

    //apiService.filterJob(keyword, location, page).success(function(response) {
    //  $scope.totalPage = response.totalPage;
    //  $scope.totalJob = response.totalJob;
    //  $scope.page = response.page;
    //  $scope.jobs = response.jobs;
    //});

    var searchPath = $scope.buildSearchPath(keyword, locationId, location, page);
    $location.path(searchPath);
  }

  $scope.buildSearchPath = function(keyword, locationId, location, page) {
    var result = "/job-listing/";
    if (keyword) {
      result += utils.toAscii(keyword);
    }
    if (locationId) {
      result += "+" + locationId;
    }
    if (location) {
      result += "+" + utils.toAscii(location);
    }
    if (page && page > 1) {
      result += "/" + page;
    }
    return result;
  }

  capitalizeWords : function capitalizeWords(str) {
    if (str) {
      str = str.replace(/-/g, ' ');
      return str.replace(/\w\S*/g, function(txt) {
        return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
      });
    }
    return "";
  }

});