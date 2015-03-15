'use strict';

analyticsApp.directive('diagram', [function () {

    return {
        restrict: 'E',
        replace: true,
        templateUrl: '/assets/scripts/components/diagram/diagram.html',
        scope: {
            diagram: '=viewModel'
        },
        link: function(){

        }
    }

}]);
