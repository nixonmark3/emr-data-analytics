'use strict';

analyticsApp.factory('diagramService', function ($http, $q, $timeout) {

    return {

        availableBlockResults: function (blockName) {

            var deferred = $q.defer();

            $http.get('/getAvailableResults/' + blockName)
                .success(function (data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config){
                    deferred.reject(status);
                });

            return deferred.promise;
        },

        blockStatistics: function (blockName) {

            var deferred = $q.defer();

            $http.get('/getStatistics/' + blockName)
                .success(function (data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config){
                    deferred.reject(status);
                });

            return deferred.promise;
        },

        blockPlot: function (blockName) {

            var deferred = $q.defer();

            $http.get('/getPlot/' + blockName)
                .success(function (data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config){
                    deferred.reject(status);
                });

            return deferred.promise;
        },

        blockOutputResults: function (blockName) {

            var deferred = $q.defer();

            $http.get('/getOutputResults/' + blockName)
                .success(function (data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config){
                    deferred.reject(status);
                });

            return deferred.promise;
        },

        evaluate: function (clientId, data) {

            var deferred = $q.defer();

            $http.post('/evaluate/' + clientId, data)
                .success(function (data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config){
                    deferred.reject(status);
                });

            return deferred.promise;
        },

        item: function (name) {

            var deferred = $q.defer();

            var diagramName = (typeof name == 'undefined' ? '' : '/' + name);

            // todo: temporarily intercept any diagram named Diag1
            if (diagramName == "/Diag1"){

                $http({ method: 'GET', url: '/assets/data/diagram/diag1.json' })
                    .success(function (data, status, headers, config) {
                        deferred.resolve(data);
                    })
                    .error(function (data, status, headers, config){
                        deferred.reject(status);
                    });
            }
            else{

                $http.get('/getDiagram/item' + diagramName)
                    .success(function (data, status, headers, config) {
                        deferred.resolve(data);
                    })
                    .error(function (data, status, headers, config) {
                        deferred.reject(status);
                    });
            }

            return deferred.promise;
        },

        listDefinitions: function () {

            var deferred = $q.defer();

            //$http({ method: 'GET', url: '/assets/data/definitions/list.json' })
            $http.get('/getDefinitions')
                .success(function (data, status, headers, config) {

                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config){
                    deferred.reject(status);
                });

            return deferred.promise;
        },

        loadSources: function(request){

            var deferred = $q.defer();

            $http.post('/loadSources', request)
                .success(function (data, status, headers, config) {

                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config){

                    deferred.reject(status);
                });

            return deferred.promise;
        },

        listProjects: function () {

            var deferred = $q.defer();

            $http.get('/getProjects')
                .success(function (data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config){
                    deferred.reject(status);
                });

            return deferred.promise;
        },

        listDiagrams: function () {

            var deferred = $q.defer();

            $http.get('/getDiagrams')
                .success(function (data, status, headers, config) {

                    // todo: temporarily inject an extra diagram for testing purposes
                    data.push({"_id": 0, "name": "Diag1", "description": ""});

                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config){
                    deferred.reject(status);
                });

            return deferred.promise;
        },

        saveDiagram: function (data) {

            var deferred = $q.defer();

            $http.post('/saveDiagram', data)
                .success(function (data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config){
                    deferred.reject(status);
                });

            return deferred.promise;
        },

        deleteDiagram: function (name) {

            var deferred = $q.defer();

            $http.get('/deleteDiagram/item/' + name)
                .success(function (data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config){
                    deferred.reject(status);
                });

            return deferred.promise;
        },

        getDataSet: function (name) {

            var deferred = $q.defer();

            $http.get('/getDataSet/item/' + name)
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