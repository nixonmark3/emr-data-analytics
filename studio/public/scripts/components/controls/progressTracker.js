
controlsApp

    .directive('progressTracker', [function(){

    return {
        restrict: 'E',
        replace: true,
        templateUrl: '/assets/scripts/components/controls/progressTracker.html',
        scope: {
            radius: "@",
            steps: "=",
            activeStep: "=",
            direction: "="
        },
        link: function($scope, element, attrs){

            var stepCount = $scope.steps.length;
            $scope.stepMargin = (element.width() - (2 * stepCount * $scope.radius)) / (stepCount - 1);
        }
    }

}]);