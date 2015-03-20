'use strict';

navigationApp.directive('navigation', [function () {

        return {
            restrict: 'A',
            replace: true,
            templateUrl: '/assets/scripts/components/navigation/navigation.html',
            scope: {
                navigation: "="
            },
            controller: 'navigationController'
        }
    }])
;
