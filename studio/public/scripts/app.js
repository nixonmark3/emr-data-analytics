'use strict';

var analyticsApp = angular.module('analyticsApp',
    ['diagramApp',
        'browserApp',
        'emr.ui.charts',
        'emr.ui.files',
        'emr.ui.grids',
        'emr.ui.modal',
        'emr.ui.panel',
        'emr.ui.popup',
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
    .controller('analyticsController', ['$scope', '$window', '$timeout', 'diagramService', 'modalService', 'popupService',
        function($scope, $window, $timeout, diagramService, modalService, popupService) {

        //
        // initialize scope level properties
        //

        // controls whether the library is displayed
        $scope.showLibrary = false;
        // controls whether the off-canvas sidebar is displayed
        $scope.showSidebar = false;
        // indicates whether the current diagram is being evaluated
        $scope.evaluating = false;
        // during some operations, we want to blur the background
        $scope.blurBackground = false;
        // controls whether the offline or online canvas is shown
        $scope.onlineCanvas = false;
        // indicates whether the diagram is being compiled
        $scope.compiling = false;

        // tracks whether a block is currently being configured
        $scope.configuringBlock = false;

        $scope.waitingForDelete = false;

        $scope.offlineDiagramMethods = {};

        // initialize the studio properties panel to be on the right-side of the canvas
        var studioContainer = angular.element($('#studio-container'));
        $scope.studioPropertiesPanel = {
            isDirty: false,
            isVisible: false,
            x: studioContainer.width() - 250 - 20,
            y: 20,
            width: 250,
            height: studioContainer.height() - 20 - 20
        };

        //
        // load data from the service
        //

        // load the list of definition blocks
        diagramService.listDefinitions().then(
            function (data) {

                // build a library of definitions and a nested list of nodes for browsing
                $scope.library = {};
                $scope.nodes = [];

                var category;
                data.forEach(function(item){

                    if (!category || category.name != item.category) {
                        category = {
                            name: item.category,
                            definitions: []
                        };
                        $scope.nodes.push(category);
                    }

                    category.definitions.push({ name: item.name });

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
                $scope.onlineViewModel = {};
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
                $scope.diagrams.forEach(function(item) {

                    initializeNavigationItem(item);
                });
            },
            function (code) {

                // todo: show exception
                console.log(code);
            }
        );

        /* Scope Level Methods */

        $scope.debug = function(evt) {

            // retrieve the current diagram's data
            var data = diagram().data;

            diagramService.debug(data).then(
                function (source) {

                    // todo: show source
                    console.log(source);
                },
                function (code) {
                    console.log(code); // TODO show exception
                }
            );

            evt.stopPropagation();
            evt.preventDefault();
        };

        /*
         ** Deploy the online diagram
         */
        $scope.deploy = function(evt) {

            var data = $scope.onlineViewModel.data;

            diagramService.deploy(data).then(
                function (jobId) {

                    beginDeploymentNotifications(jobId);
                },
                function (code) {
                    console.log(code); // TODO show exception
                }
            );

            evt.stopPropagation();
            evt.preventDefault();
        };

        function getConfigurationBlock(x, y, evt, definitionName){

            var currentDiagram = diagram();

            // translate diagram coordinates
            // todo: update to also support online diagram methods
            var point = $scope.offlineDiagramMethods.translateCoordinates(x, y, evt);

            var definition = new viewmodels.definitionViewModel(currentDiagram.mode(),
                $scope.library[definitionName]);

            // create a block description
            var block = currentDiagram.getBlockDescription(point.x, point.y, definition);

            return new viewmodels.configuringBlockViewModel(definition, block);
        }

        /*
         *  Method that loads dynamic block parameter data
         */
        $scope.loadSources = function(request, success){

            // attach the diagram to the request
            request.diagram = diagram().data;

            diagramService.loadSources(request).then(
                function (response) {

                    success(response);
                },
                function (code) {
                    console.log(code); // TODO show exception
                }
            );
        };

        /*
        ** Display block details / results
         */
        $scope.onBlockDisplay = function(position, block){

            var modalPosition = {
                centerX: (position.x + block.width() / 2),
                centerY: (position.y + block.height() / 2)
            };

            $scope.blurBackground = true;

            switch(block.definitionType()){

                case "CHART":

                    modalService.show({
                        templateUrl: '/assets/scripts/views/explore.html',
                        controller: 'exploreController',
                        inputs: {
                            block: block
                        },
                        config: {
                            name: 'test'
                        },
                        position: modalPosition
                    }).then(function (modal) {

                        modal.close.then(function (result) {

                            $scope.blurBackground = false;

                            if (result) {

                            }
                        });
                    });

                    break;
                default:

                    modalService.show({
                        templateUrl: '/assets/scripts/views/blockData.html',
                        controller: 'blockDataController',
                        inputs: {
                            block: block
                        },
                        position: modalPosition
                    }).then(function (modal) {

                        modal.close.then(function (result) {

                            $scope.blurBackground = false;

                            if (result) {

                            }
                        });
                    });

                    break;
            }
        };

        /*
        ** On block[s] selection - show the properties panel
         */
        $scope.onBlockSelection = function(blocks){

            // prevent the user from losing data
            $scope.studioPropertiesPanel.isDirty = false;

            if (blocks.length == 1){

                // a single block has been selected
                var block = blocks[0];

                // select the block
                diagram().onBlockClicked(block);

                // reference the current diagram mode
                var mode = diagram().mode();

                // retrieve the block's definition viewmodel
                var definition = new viewmodels.definitionViewModel(mode, $scope.library[block.definition()]);

                // create a studio properties object

                $scope.studioProperties = {
                    type: "BLOCK",
                    viewModel: new viewmodels.configuringBlockViewModel(definition, block.data)
                };

                // show the studio properties panel
                $scope.studioPropertiesPanel.isVisible = true;

            }
        };

        $scope.onCreateBlock = function(x, y, evt, definitionName){

            // get configuration block
            var configBlock = getConfigurationBlock(x, y, evt, definitionName);

            // create the block
            diagram().createBlock(configBlock);
        };

        /*
        ** When all blocks are being deselected - stop editing and hide the studio properties panel
         */
        $scope.onDiagramDeselection = function(){
            $scope.studioPropertiesPanel.isDirty = false;
            $scope.studioPropertiesPanel.isVisible = false;

            delete $scope.studioProperties;
        };

        $scope.onFileDrop = function(files, evt){

            var file = files[0];
            if (!file) return;

            // todo: make this configurable
            var definitionName = "LoadCSV";

            // todo: base the offset on the block dimensions
            var x = evt.pageX - 100, y = evt.pageY - 20;

            // get configuration block
            var configBlock = getConfigurationBlock(x, y, evt, definitionName);
            configBlock.setParameter("Filename", file.path);

            // create the block
            diagram().createBlock(configBlock);
        };

        /*
         ** Cancel the edits performed in the studio properties panel
         */
        $scope.studioPropertiesCancel = function(){

            var viewModel = $scope.studioProperties.viewModel.reset();
            $scope.studioProperties = {
                type: "BLOCK",
                viewModel: viewModel
            };
            $scope.studioPropertiesPanel.isDirty = false;
        };

        /*
        ** Sets the studio properties savable flag when a properties is changed
         */
        $scope.studioPropertiesChange = function(){

            $scope.studioPropertiesPanel.isDirty = true;
        };

        /*
        ** Commit the modified properties to the view model
         */
        $scope.studioPropertiesSave = function(){

            diagram().updateBlock($scope.studioProperties.viewModel);
            $scope.studioPropertiesPanel.isDirty = false;
        };

        $scope.toggleDiagrams = function() {

            $scope.showSidebar = !$scope.showSidebar;
        };

        $scope.loadDiagram = function(name) {

            diagramService.item(name).then(
                function (data) {

                    $scope.toggleDiagrams();
                    $scope.toggleCanvas(false);
                    $scope.diagramViewModel = new viewmodels.diagramViewModel(data);
                    $scope.onlineViewModel = {};
                    updateSelectedDiagram();
                },
                function (code) {

                    console.log(code); // TODO show exception
                }
            );
        };

        $scope.createDiagram = function() {
            diagramService.item().then(
                function (data) {

                    $scope.toggleDiagrams();
                    $scope.toggleCanvas(false);
                    $scope.diagramViewModel = new viewmodels.diagramViewModel(data);
                    $scope.onlineViewModel = {};
                },
                function (code) {

                    console.log(code); // TODO show exception
                }
            );
        };

        $scope.save = function(evt) {

            var offlineDiagram = $scope.diagramViewModel.data;

            var onlineDiagram = $scope.onlineViewModel.data;

            var data = {'offline': offlineDiagram, 'online': onlineDiagram};

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

        $scope.evaluate = function(evt) {

            $scope.evaluating = true;

            var offlineDiagram = $scope.diagramViewModel.data;

            var onlineDiagram = $scope.onlineViewModel.data;

            var data = {'offline': offlineDiagram, 'online': onlineDiagram};

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

        $scope.deploy = function(evt) {

            var offlineDiagram = $scope.diagramViewModel.data;

            var onlineDiagram = $scope.onlineViewModel.data;

            var diagrams = {'offline': offlineDiagram, 'online': onlineDiagram};

            diagramService.deploy(diagrams).then(
                function (jobId) {

                    beginDeploymentNotifications(jobId);
                },
                function (code) {

                    console.log(code); // TODO show exception
                }
            );

            evt.stopPropagation();
            evt.preventDefault();
        };

        $scope.onGroup = function(evt){

            modalService.show({
                templateUrl: '/assets/scripts/views/blockGroup.html',
                controller: 'blockGroupController',
                inputs: {
                    diagram: $scope.diagramViewModel
                }
            }).then(function (modal) {

                $scope.blurBackground = true;

                modal.close.then(function (result) {

                    $scope.blurBackground = false;

                    if (result) {

                        console.log(result);
                        $scope.diagramViewModel = result;
                    }
                });
            });
        };

        $scope.deleteDiagram = function(diagramName) {

            diagramService.deleteDiagram(diagramName).then(
                function (data) {
                    // TODO report success back to the user

                    diagramService.item().then(
                        function (data) {

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

        $scope.toggleCanvas = function(showOnline){

            if (!$scope.onlineCanvas && showOnline){

                if ($scope.showLibrary)
                    libraryBrowserHide();

                $scope.onlineCanvas = true;
                $scope.compiling = true;

                var offlineDiagram = $scope.diagramViewModel.data;

                var onlineDiagram = $scope.onlineViewModel.data;

                var diagrams = {'offline': offlineDiagram, 'online': onlineDiagram};

                diagramService.compile(diagrams).then(
                    function (data) {

                        $scope.onlineViewModel = new viewmodels.diagramViewModel(data);
                        $scope.compiling = false;
                    },
                    function (code) {

                        console.log(code); // TODO show exception
                    }
                );
            }
            else if($scope.onlineCanvas && !showOnline){
                $scope.onlineCanvas = false;
            }
        };

        $scope.libraryToggle = function(evt){

            if (!$scope.showLibrary) {
                libraryBrowserShow();
            }
            else{
                libraryBrowserHide();
            }

            evt.stopPropagation();
            evt.preventDefault();
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

        /* private analytic methods */

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

        // fire the event to begin dragging an element
        var beginDragEvent = function(x, y, config){

            $scope.$root.$broadcast("beginDrag", {
                x: x,
                y: y,
                config: config
            });
        };

        var endDiagramConfiguration = function(){

            $scope.diagramConfiguration.controller.close();

            $scope.configuringDiagram = false;
            delete $scope.diagramConfiguration;
        };

        var beginDeploymentNotifications = function(jobId){

            var result = popupService.show({
                templateUrl: '/assets/scripts/components/diagram/deploymentNotifications.html',
                controller: 'deploymentNotificationController',
                inputs: {
                    jobId: jobId
                }}).then(function(popup){

                $scope.deployed = true;

                popup.close.then(function (jobId) {

                    diagramService.kill(jobId).then(
                        function (data) {

                            $scope.deployed = false;
                        },
                        function (code) {
                            console.log(code); // TODO show exception
                        }
                    );
                });
            });
        };

        /*
         * Returns the diagram that is currently in view
         * */
        var diagram = function(){
            if($scope.onlineCanvas)
                return $scope.onlineViewModel;
            else
                return $scope.diagramViewModel;
        };

        var updateSelectedDiagram = function() {
            if ($scope.diagrams) {
                $scope.$applyAsync(function() {
                    $scope.diagrams.forEach(function (item) {
                        if (item.selected == true) {
                            item.selected = false;
                        }
                        if (item.name == $scope.diagramViewModel.data.name) {
                            item.selected = true;
                        }
                    });
                });
            }
        };

        var initializeNavigationItem = function(item) {
            item['showProperties'] = false;
            item['selected'] = false;
        };

        // show library browser popup
        var libraryBrowserShow = function(){

            var result = popupService.show({
                templateUrl: '/assets/scripts/views/libraryBrowser.html',
                controller: 'libraryBrowserController',
                appendElement: angular.element($('#studio-container')),
                inputs: {
                    nodes: $scope.nodes,
                    onDrag: beginDragEvent,
                    onDrop: $scope.onCreateBlock
                }
            }).then(function (popup) {

                $scope.showLibrary = true;
                $scope.libraryBrowser = popup;
            });
        };

        // hide and destroy library browser
        var libraryBrowserHide = function(){

            $scope.libraryBrowser.controller.close();

            $scope.showLibrary = false;
            delete $scope.libraryBrowser;
        };

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
                else if (data.messageType == 'AddDiagram') {
                    $scope.$apply(function() {
                        initializeNavigationItem(data);
                        $scope.diagrams.push(data);
                    });
                    updateSelectedDiagram();
                }
                else if (data.messageType == 'UpdateDiagram') {
                    $scope.$apply(function() {
                        $scope.diagrams.forEach(function(item){
                            if (item.name == data.name) {
                                item.description = data.description;
                                item.owner = data.owner;
                            }
                        });
                    });
                }
                else if (data.messageType == 'DeleteDiagram') {
                    $scope.$apply(function() {

                        $scope.diagrams.forEach(function(element, index, array){
                            if(element.name === data.name){
                                $scope.diagrams.splice(index, 1);
                            }
                        });

                        $scope.waitingForDelete = false;
                    });
                    updateSelectedDiagram();
                }
                else {
                    // todo : we got a message that we can't handle
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

    }])

    .controller('deploymentNotificationController',
    ['$scope', '$element', 'jobId', 'close',
        function($scope, $element, jobId, close) {

            $scope.jobId = jobId;

            $scope.close = function(){

                close(jobId);
            };
        }
    ]);

