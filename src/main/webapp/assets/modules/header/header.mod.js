angular.module("Header").directive("translation", function() {
   return {
      restrict : "A", // This mens that it will be used as an attribute and NOT as an element.
      replace : false,
      templateUrl : "modules/translation/translation.tpl.html",
      controller : "translationController"
   }
});
