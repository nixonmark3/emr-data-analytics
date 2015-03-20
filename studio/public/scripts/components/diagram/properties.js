
'use strict';

diagramApp.directive('properties', [function () {

    return {
        restrict: 'A',
        replace: true,
        templateUrl: '/assets/scripts/components/diagram/properties.html',
        scope: {
            block: '='
        }
    };
}]);