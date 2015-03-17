'use strict';

analyticsApp.factory('diagramService', function ($http, $q) {

    return {
        item: function () {

            var deferred = $q.defer();

            $http({ method: 'GET', url: '/assets/data/diagram/1.json' })
                .success(function (data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config){
                    deferred.reject(status);
                });

            return deferred.promise;
        },

        listDefinitions: function () {

            var deferred = $q.defer();

            $http({ method: 'GET', url: '/assets/data/definitions/list.json' })
                .success(function (data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config){
                    deferred.reject(status);
                });

            return deferred.promise;
        }
    };
});