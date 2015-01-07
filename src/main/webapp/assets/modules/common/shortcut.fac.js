angular.module("Common").factory("shortcutFactory", function (jsonValue, $location, $rootScope, historyFactory, utils) {

  var $$ = {
    goBack: function () {
      var path = historyFactory.popHistory();
      $location.path(path === undefined ? "/" : path);
    }
  }

  var traps = {
    esc: function (e) {
      switch (utils.getView()) {
        case jsonValue.views.jobsSearchText:
          if ($("#companyVideoInfor").is(":visible")) {// ESC from others, such as: Video dialog, ...
            $(".playerVideo").attr("src", "");
            return;
          }
          $location.path(jsonValue.routerUris.jobsSearch);
          break;

        default:
          $$.goBack();
          break;
      }
    },

    enter: function (e) {
      switch (utils.getView()) {
        case jsonValue.views.jobsSearchText:
        case jsonValue.views.jobsSearch:
          utils.sendNotification(jsonValue.notifications.defaultAction);
          break;
      }
    }
  }

  Mousetrap.bindGlobal("esc", function (e) {
    if (e.defaultPrevented) {
      return;
    }
    traps.esc(e);
    utils.hideNavigationBar();
  });

  Mousetrap.bindGlobal("enter", function (e) {
    if (e.defaultPrevented) {
      return;
    }
    traps.enter(e);
  });

  return {
    initialize: function () {},

    trigger: function (key) {
      Mousetrap.trigger(key);
    }
  };
});