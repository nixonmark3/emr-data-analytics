var analyticsApp = angular.module('analyticsApp', ['ngRoute', 'ngSanitize'])

    .config(function ($routeProvider, $locationProvider) {
        $routeProvider.when('/studio', {
            templateUrl: "/assets/templates/studio.html"
        });
        $routeProvider.otherwise({ redirectTo: '/studio' });
        $locationProvider.html5Mode({
            enabled: true
        });
    })
    .controller('analyticsController', function($scope, diagramService) {

        diagramService.item().then(
            function (data) {

                $scope.diagramViewModel = new viewmodels.diagramViewModel(data);
            },
            function (code) {

                // todo: show exception
                console.log(code);
            }
        );
    });

