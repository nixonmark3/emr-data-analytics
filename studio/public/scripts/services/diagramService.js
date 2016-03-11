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

        collect: function(request){

            var deferred = $q.defer();

            $http.post('/analytics/collect', request)
                .success(function (data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config){
                    deferred.reject(status);
                });

            return deferred.promise;
        },

        describe: function(request){

            var deferred = $q.defer();

            $http.post('/analytics/describe', request)
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

            $http.post('/diagrams/transform', data).then(
                function (response) {
                    deferred.resolve(response.data);
                },
                function (response){
                    deferred.reject(response.data);
                });

            return deferred.promise;
        },

        compile: function (data) {

            var deferred = $q.defer();

            $http.post('/diagrams/compile', data).then(
                function (response) {
                    deferred.resolve(response.data);
                },
                function (response){
                    deferred.reject(response.data);
                });

            return deferred.promise;
        },

        deploy: function (data) {

            var deferred = $q.defer();

            $http.post('/analytics/deploy', data).then(
                function (response) {
                    deferred.resolve(response.data);
                },
                function (response){
                    deferred.reject(response.data);
                });

            return deferred.promise;
        },

        evaluate: function (data) {

            var deferred = $q.defer();

            $http.post('/analytics/evaluate', data).then(
                function (response) {
                    deferred.resolve(response.data);
                },
                function (response){
                    deferred.reject(response.data);
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

        tasks: function (id) {

            var deferred = $q.defer();

            $http.get('/analytics/tasks/' + id)
                .success(function (data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config){
                    deferred.reject(status);
                });

            return deferred.promise;
        },

        interpret: function(data) {

            var deferred = $q.defer();

            $http.post('/analytics/interpret', data)
                .success(function (data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config){
                    deferred.reject(status);
                });

            return deferred.promise;
        },

        terminate: function (id, mode) {

            var deferred = $q.defer();

            $http.get('/analytics/terminate/' + id + '/' + mode)
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

        load: function(request){

            var deferred = $q.defer();

            $http.post('/analytics/load', request).then(
                function (response) {

                    deferred.resolve(response.data);
                },
                function (response){

                    deferred.reject(response.data);
                });

            return deferred.promise;
        },

        listDefinitions: function () {

            var deferred = $q.defer();

            $http.get('/definitions/all')
                .success(function (data, status, headers, config) {

                    deferred.resolve(data);
                })
                .error(function (data, status, headers, config){
                    deferred.reject(status);
                });

            return deferred.promise;
        },

        resolveSource: function(request){

            var deferred = $q.defer();

            $http.post('/sources/resolve', request).then(
                function (response) {
                    deferred.resolve(response.data);
                },
                function (response){
                    deferred.reject(response.status);
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

            $http.get('/diagrams/all')
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
            $http.post('/streaming/start', data).then(
                function (response) {
                    deferred.resolve(response.data);
                },
                function (response){
                    deferred.reject(response.data);
                });

            return deferred.promise;
        },

        streamingStop: function (topic) {

            var deferred = $q.defer();
            $http.get('/streaming/stop/' + topic).then(
                function (response) {
                    deferred.resolve(response.data);
                },
                function (response){
                    deferred.reject(response.data);
                });

            return deferred.promise;
        },

        streamingSummary: function(request){

            var deferred = $q.defer();
            $http.post('/streaming/summary', request).then(
                function (response) {
                    deferred.resolve(response.data);
                },
                function (response){
                    deferred.reject(response.data);
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
        },

        query: function(request){

            var deferred = $q.defer();

            if (request === undefined){
                $http.get('/data/hbase/query').then(
                    function (response) {
                        deferred.resolve(response.data);
                    },
                    function (response){
                        deferred.reject(response.data);
                    });
            }
            else{
                $http.post('/data/hbase/query', request).then(
                    function (response) {
                        deferred.resolve(response.data);
                    },
                    function (response){
                        deferred.reject(response.data);
                    });
            }

            return deferred.promise;
        }
    };
});