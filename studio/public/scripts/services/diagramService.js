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

        deploy: function (data) {

            var deferred = $q.defer();

            $http.post('/deploy', data)
                .success(function (data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config){
                    deferred.reject(status);
                });

            return deferred.promise;
        },

        group: function(request){

            var deferred = $q.defer();

            $http.post('/group', request)
                .success(function (data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config){
                    deferred.reject(status);
                });

            return deferred.promise;
        },

        kill: function (jobId) {

            var deferred = $q.defer();

            $http.get('/kill/' + jobId)
                .success(function (data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config){
                    deferred.reject(status);
                });

            return deferred.promise;
        },

        compile: function (data) {

            var deferred = $q.defer();

            $http.post('/compile', data)
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

            $http.get('/getDiagram/item' + diagramName)
                .success(function (data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config) {
                    deferred.reject(status);
                });

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
        },

        getFeatures: function(){

            var deferred = $q.defer();

            $http({ method: 'GET', url: '/assets/data/results/features.json' })
                .success(function (data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config){
                    deferred.reject(status);
                });

            return deferred.promise;
        },

        getSampleData: function(){

            var deferred = $q.defer();

            $http({ method: 'GET', url: '/assets/data/results/data.json' })
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