'use strict';

navigationApp.directive('navigation', [function () {

        return {
            restrict: 'A',
            replace: false,
            templateUrl: '/assets/scripts/components/navigation/navigation.html',
            scope: {
                navigation: "="
            },
            controller: 'navigationController'
        }
    }])
;
