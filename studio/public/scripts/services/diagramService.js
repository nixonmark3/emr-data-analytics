'use strict';

analyticsApp.factory('diagramService', function ($http, $q) {

    return {
        item: function () {

            var deferred = $q.defer();

            $http.get('/getDiagram/item/test1')
                .success(function (data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config) {
                    deferred.reject(status);
                });

            return deferred.promise;
        }
    };
});