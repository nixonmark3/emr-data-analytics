'use strict';

var analyticsApp = angular.module('analyticsApp',
    ['diagramApp',
        'browserApp',
        'emr.ui.charts',
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

        /*
         *  Method that loads dynamic block parameter data
         */
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

        /*
         *
         */
        $scope.onBlockSelection = function(blocks){

            // prevent the user from losing data
            $scope.studioPropertiesPanel.isDirty = false;

            if (blocks.length == 1){

                // a single block has been selected
                var block = blocks[0];

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

        /*
        ** Diagram deselection stops editing all items
         */
        $scope.onDiagramDeselection = function(){
            $scope.studioPropertiesPanel.isDirty = false;
            $scope.studioPropertiesPanel.isVisible = false;

            delete $scope.studioProperties;
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

        // load an existing diagram
        $scope.loadDiagram = function(name) {
            diagramService.item(name).then(
                function (data) {
                    $scope.toggleDiagrams();
                    $scope.diagramViewModel = new viewmodels.diagramViewModel(data);
                    updateSelectedDiagram();
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


        $scope.onBlockConfigure = function(position, block){

            switch(block.definitionType()){

                case "CHART":

                    var modalPosition = {
                        centerX: (position.x + block.width() / 2),
                        centerY: (position.y + block.height() / 2)
                    };

                    $scope.blurBackground = true;

                    modalService.show({
                        templateUrl: '/assets/scripts/views/charts.html',
                        controller: 'chartsController',
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
                default:

                    // all other definition types should be configured using the conventional mechanism

                    if ($scope.configuringBlock){  // if a block is already being configured - save it

                        $scope.blockConfiguration.controller.save();
                    }
                    else {  // show the block configuration popup

                        // retrieve the block's definition definition
                        var definition = $scope.library[block.definition()];

                        // convert it to a config block
                        var configBlock = new viewmodels.configuringBlockViewModel(definition, block.data);

                        // show the popup
                        var result = popupService.show({
                            templateUrl: '/assets/scripts/components/diagram/blockProperties.html',
                            controller: 'blockConfigController',
                            inputs: {
                                block: configBlock,
                                position: position,
                                loadSources: $scope.loadSources
                            }
                        }).then(function (popup) {

                            $scope.configuringBlock = true;
                            $scope.blockConfiguration = popup;

                            // when the block configuration is closed - save the resulting config block
                            $scope.blockConfiguration.close.then(function (configBlock) {

                                if (configBlock)
                                    $scope.diagramViewModel.updateBlock(configBlock);

                                $scope.configuringBlock = false;
                                delete $scope.blockConfiguration;
                            });
                        });
                    }

                    break;
            }
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

        // delete the current diagram
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

        $scope.getBlockData = function(type, key, success){

            switch(type){
                case "Pages":
                    diagramService.availableBlockResults(key).then(
                       function(data){
                           success(data);
                       },
                        function (code) {
                            console.log(code); // TODO show exception
                        }
                    );
                    break;
                case "Statistics":
                    diagramService.blockStatistics(key).then(
                        function(data){
                            success(data);
                        },
                        function (code) {
                            console.log(code); // TODO show exception
                        }
                    );
                    break;
                case "Plot":
                    diagramService.blockPlot(key).then(
                        function(data){
                            success(data);
                        },
                        function (code) {
                            console.log(code); // TODO show exception
                        }
                    );
                    break;
                case "Results":
                    diagramService.blockOutputResults(key).then(
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

        /*
         *
        */
        $scope.toggleCanvas = function(showOnline){

            if (!$scope.onlineCanvas && showOnline){

                if ($scope.showLibrary)
                    libraryBrowserHide();

                $scope.onlineCanvas = true;
                $scope.compiling = true;

                // todo: temporarily using timeout to test loading screen
                $timeout(function(){diagramService.compile($scope.diagramViewModel.data).then(
                    function (data) {

                        $scope.onlineViewModel = new viewmodels.diagramViewModel(data);
                        $scope.compiling = false;
                    },
                    function (code) {
                        console.log(code); // TODO show exception
                    }
                );}, 0);
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
                    onDrop: $scope.createBlock
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

