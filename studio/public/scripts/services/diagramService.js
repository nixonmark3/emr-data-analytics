'use strict';

analyticsApp

    .factory('diagramService', function ($http, $q, $timeout) {

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



        transform: function (data) {

            var deferred = $q.defer();

            $http.post('/transform', data)
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

            $http.post('/diagrams/compile', data)
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

            $http.post('/analytics/deploy', data)
                .success(function (data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config){
                    deferred.reject(status);
                });

            return deferred.promise;
        },

        evaluate: function (data) {

            var deferred = $q.defer();

            $http.post('/analytics/evaluate', data)
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

        info: function (id) {

            var deferred = $q.defer();

            $http.get('/analytics/info/' + id)
                .success(function (data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config){
                    deferred.reject(status);
                });

            return deferred.promise;
        },

        kill: function (id, mode) {

            var deferred = $q.defer();

            $http.get('/analytics/kill/' + id + '/' + mode)
                .success(function (data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config){
                    deferred.reject(status);
                });

            return deferred.promise;
        },

        upload: function(file){

            var deferred = $q.defer();

            var formData = new FormData();
            var xhr = new XMLHttpRequest();

            formData.append("file", file);

            xhr.upload.onprogress = function(event){
                deferred.notify(event);
            };

            xhr.onload = function(event){
                if (xhr.status == 200){
                    var data = angular.fromJson(xhr.responseText);
                    deferred.resolve(data);
                }
                else{
                    deferred.reject(xhr.status);
                }
            };

            xhr.open('POST', '/upload');
            xhr.send(formData);

            return deferred.promise;
        },



        listDefinitions: function () {

            var deferred = $q.defer();

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
            //$http({ method: 'GET', url: '/assets/data/diagrams/diagrams.json' })
                .success(function (data, status, headers, config) {

                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config) {

                    deferred.reject(status);
                });

            return deferred.promise;
        },

        save: function (data) {

            var deferred = $q.defer();

            $http.post('/diagrams/save', data)
                .success(function (data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config){
                    deferred.reject(status);
                });

            return deferred.promise;
        },

        streamingStart: function(data){

            var deferred = $q.defer();
            $http.post('/streaming/start', data)
                .success(function (data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config){
                    deferred.reject(status);
                });

            return deferred.promise;
        },

        streamingStop: function (topic) {

            var deferred = $q.defer();
            $http.get('/streaming/stop/' + topic)
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

        getFeatures: function(blockName){

            var deferred = $q.defer();

            $http.get('/getFeatures/' + blockName)
                .success(function (data, status, headers, config) {

                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config){

                    deferred.reject(status);
                });

            return deferred.promise;
        },

        getChartData: function(blockName, features) {

            var deferred = $q.defer();

            $http.post('/getChartData/' + blockName, {features: features})
                .success(function (data, status, headers, config) {

                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config){

                    deferred.reject(status);
                });

            return deferred.promise;
        },

        getFeatureGridData: function(blockName) {

            var deferred = $q.defer();

            $http.get('/getFeatureGridData/' + blockName)
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