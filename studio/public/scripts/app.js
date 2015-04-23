'use strict';

var analyticsApp = angular.module('analyticsApp',
    ['diagramApp',
        'browserApp',
        'popupApp',
        'ngRoute',
        'ngSanitize',
        'ngAnimate'])
    .config(function ($routeProvider, $locationProvider) {
        $routeProvider.when('/studio', {
            templateUrl: "/assets/templates/studio.html"
        });
        $routeProvider.otherwise({ redirectTo: '/studio' });
        $locationProvider.html5Mode({
            enabled: true
        });
    })
    .filter('unsafe', ['$sce', function ($sce) {
        return function (val) {
            return $sce.trustAsHtml(val);
        };
    }])
    .controller('analyticsController', ['$scope', '$timeout', 'diagramService', 'popupService',
        function($scope, $timeout, diagramService, popupService) {

        //
        // Initialize web socket connection
        //

        var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket;
        var sock = new WS("ws://localhost:9000/getClientSocket");

        $scope.send = function() {
            console.log("send!");
            sock.send("hello");
        };

        var receiveEvent = function(event) {
            var data = JSON.parse(event.data);

            if (data['messageType']) {
                if (data.messageType == 'ClientRegistration') {
                    $scope.clientId = data.id;
                }
            }
            else {
                console.log('job id: ' + data.jobId);
                console.log('evaluation state: ' + data.state);

                $scope.$applyAsync(function() {
                        $scope.diagramViewModel.updateStatusOfBlocks(data['blockStates']);
                        if (data.state > 0) {
                            $scope.evaluating = false;
                        }
                    }
                );
            }
        };

        sock.onmessage = receiveEvent;

        //
        // initialize scope level properties
        //

        // controls whether the off-canvas sidebar is displayed
        $scope.showSidebar = false;
        // controls whether the library is displayed
        $scope.showLibrary = false;
        // indicates whether the current diagram is being evaluated
        $scope.evaluating = false;
        // during some operations, we want to blur the background
        $scope.blurBackground = false;

        //
        // load data from the service
        //

        // load the list of definition blocks
        diagramService.listDefinitions().then(
            function (data) {

                // build a library of definitions and a nested list of nodes for browsing
                $scope.library = {};
                $scope.nodes = [];
                var categoryName;
                var category;
                data.forEach(function(item){

                    if (!category || category.name != item.category) {
                        category = {
                            name: item.category,
                            definitions: []
                        };
                        $scope.nodes.push(category);
                    }

                    category.definitions.push({name: item.name});

                    $scope.library[item.name] = item;
                });
            },
            function (code) {
                // todo: show exception
                console.log(code);
            }
        );

        // load the specified diagram
        diagramService.item().then(
            function (data) {
                $scope.diagramViewModel = new viewmodels.diagramViewModel(data);
            },
            function (code) {
                // todo: show exception
                console.log(code);
            }
        );

        // load diagrams
        diagramService.listDiagrams().then(
            function (data) {
                $scope.diagrams = data;
            },
            function (code) {

                // todo: show exception
                console.log(code);
            }
        );

        // fire the event to create a new block given a definition
        $scope.createBlock = function(x, y, evt, definitionName){

            $scope.$root.$broadcast("createBlock", {
                x: x,
                y: y,
                evt: evt,
                definitionName: definitionName
            });
        };

        $scope.toggleDiagrams = function() {

            $scope.showSidebar = !$scope.showSidebar;
        };

        $scope.toggleLibrary = function(){

            $scope.showLibrary = !$scope.showLibrary;
        };

        // load an existing diagram
        $scope.loadDiagram = function(name) {
            diagramService.item(name).then(
                function (data) {
                    $scope.toggleDiagrams();
                    $scope.diagramViewModel = new viewmodels.diagramViewModel(data);
                },
                function (code) {
                    console.log(code); // TODO show exception
                }
            );
        };

        $scope.loadSources = function(request, success){

            // attach the diagram to the request
            request.diagram = $scope.diagramViewModel.data;

            diagramService.loadSources(request).then(
                function (response) {

                    success(response);
                },
                function (code) {
                    console.log(code); // TODO show exception
                }
            );
        };

        // create a blank diagram
        $scope.createDiagram = function() {
            diagramService.item().then(
                function (data) {
                    $scope.toggleDiagrams();
                    $scope.diagramViewModel = new viewmodels.diagramViewModel(data);
                },
                function (code) {
                    console.log(code); // TODO show exception
                }
            );
        };

        // save the current diagram
        $scope.save = function(evt) {

            var data = $scope.diagramViewModel.data;
            // we need to delete the object id or the save will not work
            // TODO need a better solution for this
            delete data._id;
            diagramService.saveDiagram(data).then(
                function (data) {
                    // TODO report success back to the user
                },
                function (code) {
                    console.log(code); // TODO show exception
                }
            );

            evt.stopPropagation();
            evt.preventDefault();
        };

        // evaluate the current diagram
        $scope.evaluate = function(evt) {

            $scope.evaluating = true;

            //$timeout(function(){
            //
            //    $scope.evaluating = false;
            //}, 4000);
            //
            //return;

            var data = $scope.diagramViewModel.data;

            diagramService.evaluate($scope.clientId, data).then(
                function (data) {
                    // TODO report success back to the user
                    console.log(data);
                },
                function (code) {
                    console.log(code); // TODO show exception
                }
            );

            evt.stopPropagation();
            evt.preventDefault();
        };

        // delete the current diagram
        $scope.deleteDiagram = function() {
            diagramService.deleteDiagram($scope.diagramViewModel.data.name).then(
                function (data) {
                    // TODO report success back to the user
                    diagramService.item().then(
                        function (data) {
                            $scope.diagramViewModel = new viewmodels.diagramViewModel(data);
                        },
                        function (code) {
                            console.log(code); // TODO show exception
                        }
                    );

                },
                function (code) {
                    console.log(code); // TODO show exception
                }
            );
        };

        $scope.getBlockData = function(type, key, success){

            switch(type){
                case "list":
                    diagramService.availableBlockResults(key).then(
                       function(data){
                           success(data);
                       },
                        function (code) {
                            console.log(code); // TODO show exception
                        }
                    );
                    break;
            }
        };

        $scope.toggleDiagramConfiguration = function(evt){

            if ($scope.configuringDiagram){
                endDiagramConfiguration();
            }
            else{
                beginDiagramConfiguration();
            }

            evt.stopPropagation();
            evt.preventDefault();
        };

        var beginDiagramConfiguration = function(){

            var result = popupService.show({
                templateUrl: '/assets/scripts/components/diagram/diagramProperties.html',
                controller: 'diagramConfigController',
                inputs: {
                    diagram: $scope.diagramViewModel.data
                }}).then(function(popup){

                $scope.configuringDiagram = true;
                $scope.diagramConfiguration = popup;
            });
        };

        var endDiagramConfiguration = function(){

            $scope.diagramConfiguration.controller.close();


            $scope.configuringDiagram = false;
            delete $scope.diagramConfiguration;
        };
    }]);

