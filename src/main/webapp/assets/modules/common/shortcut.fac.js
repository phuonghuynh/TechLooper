angular.module("Common").factory("shortcutFactory", function (jsonValue, $location, $rootScope, historyFactory, utils) {

  var traps = {
    esc: function (e) {
      switch (utils.getView($location.path())) {
        case jsonValue.views.jobsSearch:
          if ($("#companyVideoInfor").is(":visible")) {// ESC from others, such as: Video dialog, ...
            return;
          }
          $location.path(jsonValue.routerUris.jobsSearch);
          $rootScope.$apply();
          break;
        case jsonValue.views.jobsSearchText:
          var path = historyFactory.popHistory();
          $location.path(path === undefined ? "/" : path);
          $rootScope.$apply();
          break;
      }
    }
  }

  Mousetrap.bindGlobal("esc", function (e) {
    if (e.defaultPrevented) {
      return;
    }
    traps.esc(e);
  });

  return {
    trigger: function (key) {
      Mousetrap.trigger(key);
    }
  };
});