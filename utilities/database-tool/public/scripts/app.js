var dependencies = [
    'ngRoute',
    'ui.bootstrap',
    'myApp.controllers'
];

var app = angular.module('myApp', dependencies);

app.config(function ($routeProvider, $locationProvider) {
    $routeProvider
        .when('/', {
            templateUrl: '/assets/views/main.html'
        })
        .otherwise({
            redirectTo: '/'
        });

    $locationProvider.html5Mode({
        enabled: true,
        requireBase: false
    });
});

var controllersModule = angular.module('myApp.controllers', []);