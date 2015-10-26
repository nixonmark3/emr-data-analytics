'use strict';

var analyticsApp = angular.module('analyticsApp',
    ['diagramApp',
        'browserApp',
        'emr.ui.controls',
        'emr.ui.charts',
        'emr.ui.dashboard',
        'emr.ui.files',
        'emr.ui.grids',
        'emr.ui.modal',
        'emr.ui.panel',
        'emr.ui.popup',
        'ui.codemirror',
        'ngRoute',
        'ngSanitize',
        'ngAnimate'])

    .config(function ($routeProvider, $locationProvider, $animateProvider) {
        $routeProvider
            .when('/studio', {
                templateUrl: "/assets/templates/studio.html",
                controller: "studioController"
            })
            .when('/dashboard', {
                templateUrl: "/assets/templates/dashboard.html",
                controller: "dashboardController"
            });
        $routeProvider.otherwise({ redirectTo: '/studio' });
        $locationProvider.html5Mode({
            enabled: true
        });

        $animateProvider.classNameFilter(/^((?!(fa-spin)).)*$/);
    })

    .filter('unsafe', ['$sce', function ($sce) {
        return function (val) {
            return $sce.trustAsHtml(val);
        };
    }])

    .factory('analyticsService', [function(){

        /**
         * generate a random guid
         * @returns {string}
         */
        function guid(){
            var delim = "-";

            function s4() {
                return Math.floor((1 + Math.random()) * 0x10000)
                    .toString(16)
                    .substring(1);
            }

            return (s4() + s4() + delim + s4() + delim + s4() + delim + s4() + delim + s4() + s4() + s4());
        }

        return {

            guid: guid,

            /**
             * retrieve the websocket url
             */
            webSocketUrl: function(){

                var id;
                // attempt to retrieve the websocket url from session storage
                if (window.sessionStorage) {
                    var stored = window.sessionStorage.getItem("sessionId");
                    if (stored) {
                        id = stored;
                    }
                    else{
                        // cache the websocket url
                        id =  guid();
                        window.sessionStorage.setItem("sessionId", id);
                    }
                }

                return jsRoutes.controllers.Analytics.socket(id).webSocketURL();
            }
        }
    }])

    .factory('$webSockets', ['$rootScope', 'analyticsService', function ($rootScope, analyticsService) {

        /**
         *
         */
        return function(){

            var websocket = {};
            var listeners = [];

            websocket.isConnected = false;

            $rootScope.queuedMessages = [];

            websocket.reset = function(){

                var temp = [];
                listeners.forEach(function(listener){

                    if (listener.persist)
                        temp.push(listener);
                });

                listeners = temp;
            };

            /**
             * @param label
             * @param predicate
             * @param handler
             * @param persist
             */
            websocket.listen = function (label, predicate, handler, persist) {

                if (persist == null)
                    persist = false;

                listeners.push({ label: label, predicate: predicate, handler: handler, persist: persist });
            };

            /**
             *
             * @param label
             */
            websocket.unlisten = function(label) {

                var position = listeners.map(function(x) { return x.label; }).indexOf(label);

                console.log("Unlisten position " + position);

                if (position > -1)
                    listeners.splice(position, 1);
            };

            /**
             *
             */
            var onopen = function () {
                $rootScope.websocketAvailable = true;
                websocket.isConnected = true;
                $rootScope.$$phase || $rootScope.$apply();
                if ($rootScope.queuedMessages) {
                    for (var i = 0; i < $rootScope.queuedMessages.length; i++) {
                        ws.send(JSON.stringify($rootScope.queuedMessages[i]));
                    }
                    $rootScope.queuedMessages = null;
                    $rootScope.$$phase || $rootScope.$apply();
                }
            };

            /**
             *
             */
            var onclose = function () {
                websocket.isConnected = false;
                $rootScope.websocketAvailable = false;
                $rootScope.$$phase || $rootScope.$apply();
                $rootScope.queuedMessages = $rootScope.queuedMessages || [];

                setTimeout(function () {
                    ws = connect();
                }, 5000);
            };

            /**
             *
             * @param msg
             */
            var onmessage = function (msg) {

                console.log("message received: " + msg.data);

                var obj = JSON.parse(msg.data);
                for (var i = 0; i < listeners.length; i++) {
                    var listener = listeners[i];
                    if (listener.predicate(obj))
                        listener.handler(obj);
                }
            };

            /**
             *
             */
            var onerror = function () {
                console.log('onerror');
            };

            /**
             *
             * @param obj
             */
            websocket.send = function (obj) {

                if ($rootScope.queuedMessages)
                    $rootScope.queuedMessages.push(obj);
                else
                    ws.send(JSON.stringify(obj));
            };

            /**
             *
             * @param sock
             */
            function setHandlers(sock) {
                sock.onopen = onopen;
                sock.onclose = onclose;
                sock.onmessage = onmessage;
                sock.onerror = onerror;
            }

            /**
             *
             * @returns {WebSocket}
             */
            function connect() {
                var url = analyticsService.webSocketUrl();
                console.log("web socket url: " + url);

                var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket;
                var sock = new WS(url);
                setHandlers(sock);

                return sock;
            }

            var ws = connect();

            return websocket;
        }();
    }])

    .controller('analyticsController', ['$scope', '$rootScope', '$location', '$webSockets', function($scope, $rootScope, $location, $webSockets){

        //
        // initialize scope level properties
        //

        // represents the current view { -1 = splash, 0 = studio, 1 = dashboard }
        $rootScope.activeView = -1;
        // initialize the variable that indicates whether the service is available
        $rootScope.serviceAvailable = true;
        // initialize list of alerts
        $rootScope.alerts = [];

        // listen for analytic service pings
        $webSockets.listen("ping", function(msg) { return msg.messageType == "ping"; }, setPing, true);

        // ping the analytics service
        $webSockets.send({ messageType: "ping" });

        /**
         *
         * @param type
         * @param message
         * @param dismissible
         */
        $rootScope.addAlert = function(type, message, dismissible){

            var alert = { type: type, message: message, dismissible: dismissible };
            if (type == "serviceUnavailable")   // prepend service unavailable alerts
                $rootScope.alerts.unshift(alert);
            else
                $rootScope.alerts.push(alert);
        };

        /**
         *
         * @param message
         */
        function setPing(message){

            if (message.value != $rootScope.serviceAvailable){
                // a change in the service's availability has been observed

                if (message.value){
                    // the service is now available
                    $rootScope.serviceAvailable = true;

                    // remove service alert - should always be the first item in the list
                    $rootScope.removeAlert(0);
                }
                else{
                    // service is not available
                    $rootScope.serviceAvailable = false;

                    // add service alert
                    $rootScope.addAlert("serviceUnavailable", "Unable to connect to the Analytics Service.", false);
                }

                $rootScope.$$phase || $rootScope.$apply();
            }
        }

        /**
         *
         * @param index
         */
        $rootScope.removeAlert = function(index){
            $rootScope.alerts.splice(index, 1);
        };

        /**
         * Set the active view
         * @param index the new active view index { 0 = studio, 1 = dashboard }
         */
        $scope.setActiveView = function(index) {

            if ($rootScope.activeView != index){

                switch(index){

                    case 0:
                        $location.path('/studio');
                        break;
                    case 1:
                        $location.path('/dashboard');
                        break;
                }

                $rootScope.activeView = index;
            }
        };

        // onload - set the active view to be studio
        var init = function(){
            $scope.setActiveView(0);
        }();
    }])

    .controller('dashboardController', ['$scope', '$window', '$timeout', '$webSockets', 'diagramService', 'modalService', 'popupService',
        function($scope, $window, $timeout, $webSockets, diagramService, modalService, popupService) {


    }])

    .controller('studioController', ['$scope', '$window', '$timeout', '$webSockets', 'diagramService', 'modalService', 'toasterService', 'popupService', function($scope, $window, $timeout, $webSockets, diagramService, modalService, toasterService, popupService) {

        // initialized internal variables
        var studioWrapper = angular.element(document.querySelector('#studio-wrapper')), // reference to the studio parent element
            lastTerminalWidth = 30, // initialize the variable that tracks the terminal windows' last width
            toastContainerId = "toast-container"; // id of studio toast container

        //
        // initialize scope level properties
        //

        // current width of the studio canvas
        $scope.canvasWidth = 100;
        // flag representing whether the terminal is visible
        $scope.isTerminalVisible = false;
        // flag representing whether the terminal is being resized
        $scope.isResizingTerminal = false;
        // controls whether the library is displayed
        $scope.showLibrary = false;
        // during some operations, we want to blur the background
        $scope.blurBackground = false;
        // controls whether the offline or online canvas is shown
        $scope.onlineCanvas = false;
        // indicates whether the diagram is being transformed
        $scope.transforming = false;
        // represents the current state of the offline diagram { 0 = 'idle', 1 = 'downloading', 2 = 'running' }
        $scope.offlineState = 0;
        // represents the current state of the online diagram { 0 = 'idle', 1 = 'downloading', 2 = 'running' }
        $scope.onlineState = 0;

        $scope.evaluating = false;

        $scope.offlineDiagramMethods = {};

        $scope.diagrams = [];
        //
        $scope.terminalOutput = [];

        // setup websocket listeners
        $webSockets.reset();
        // listen for analytic task updates, append them to the terminal output
        $webSockets.listen("task-status", function(msg) { return msg.messageType == "task-status"; }, appendTerminalOutput);
        $webSockets.listen("evaluation-status", function(msg) { return msg.messageType == "evaluation-status"; }, updateEvaluationStatus);
        $webSockets.listen("deployment-status", function(msg) { return msg.messageType == "deployment-status"; }, function(msg) { updateDeploymentStatus(msg.jobInfo); });

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

        // load an empty diagram
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

        //
        // internal methods
        //

        /**
         *
         * @param taskStatus
         */
        function appendTerminalOutput(taskStatus){

            $scope.terminalOutput.push(taskStatus);

            $scope.$$phase || $scope.$apply();
        }

        /**
         * Save the current diagram
         */
        function save(onSuccess, onFailure) {

            // reference the diagram
            var diagram = $scope.diagramViewModel;

            // check whether this diagram still has the default name (which is invalid)
            if (diagram.hasDefaultName()){

                // prompt the user to give the diagram a new / unique name

                // build a diagram properties object
                var properties = {
                    name: diagram.getName(),
                    "description": diagram.getDescription(),
                    "targetEnvironment": diagram.getTargetEnvironment(),
                    "owner": diagram.getOwner(),
                    "category": diagram.getCategory()
                };

                popupService.show({
                    templateUrl: '/assets/scripts/views/createDiagram.html',
                    controller: 'createDiagramController',
                    backdropClass: 'emr-popup-backdrop',
                    inputs: {
                        diagramProperties: properties
                    }
                }).then(function(popup) {

                    popup.close.then(function (properties) {

                        if (properties) {   // on save, properties will be provided

                            // capture new
                            var diagram = $scope.diagramViewModel;
                            diagram.setName(properties.name);
                            diagram.setDescription(properties.description);
                            diagram.setTargetEnvironment(properties.targetEnvironment);
                            diagram.setOwner(properties.owner);
                            diagram.setCategory(properties.category);

                            // recursively call save again with a new (non-default) diagram name
                            save(onSuccess, onFailure);
                        }
                    });
                });
            }
            else{

                // the diagram is valid and ready to be saved

                // check whether the diagram is new
                var isNew = false;
                if (!$scope.diagramViewModel.getId())
                    isNew = true;

                // create diagram container
                var offlineDiagram = $scope.diagramViewModel.data;
                var onlineDiagram = $scope.onlineViewModel.data;
                var data = {'offline': offlineDiagram, 'online': onlineDiagram};

                diagramService.save(data).then(function (diagramId) {

                        if (isNew) {
                            // capture the new diagram id
                            $scope.diagramViewModel.setId(diagramId);
                            subscribe($scope.diagramViewModel.getId());
                        }

                        if (onSuccess)
                            onSuccess();
                    },
                    function (code) {

                        if (onFailure)
                            onFailure();
                    }
                );
            }
        }

        /* Scope Level Methods */

        $scope.compile = function(evt) {

            // retrieve the current diagram's data
            var data = diagram().data;

            var position = {
                width: 800,
                centerX: evt.clientX,
                centerY: evt.clientY
            };

            modalService.show({
                templateUrl: '/assets/scripts/views/editor.html',
                controller: 'debugController',
                inputs: { data: data },
                config: {
                    name: "Compiled Source Code",
                    showSave : true,
                    showCancel: true
                },
                position: position
            });

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
                centerX: (position.x + position.zoom * block.width() / 2),
                centerY: (position.y + position.zoom * block.height() / 2)
            };

            switch(block.definitionType()){

                case "CHART":

                    modalService.show({
                        templateUrl: '/assets/scripts/views/explore.html',
                        controller: 'exploreController',
                        inputs: {
                            block: block
                        },
                        config: {
                            title: block.data.name,
                            backdropClass: 'emr-modal-backdrop',
                            showSave : false,
                            showCancel: true
                        },
                        position: modalPosition
                    }).then(function (modal) {

                        modal.close.then(function (result) {

                            if (result) {

                            }
                        });
                    });

                    break;
                default:

                    var mode = diagram().mode();

                    var definition = new viewmodels.definitionViewModel(mode, $scope.library[block.definition()]);

                    modalService.show({
                        templateUrl: '/assets/scripts/views/blockData.html',
                        controller: 'blockDataController',
                        inputs: {
                            block: new viewmodels.configuringBlockViewModel(definition, block.data),
                            loadSources: $scope.loadSources,
                            diagram: diagram()
                        },
                        config: {
                            // todo put this back when refactor results name: block.data.name,
                            backdropClass: 'emr-modal-backdrop',
                            showSave : false,
                            showCancel: true
                        },
                        position: modalPosition
                    }).then(function (modal) {

                        modal.close.then(function (result) {

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

            if (blocks.length == 1){

                // a single block has been selected
                var block = blocks[0];

                // select the block
                diagram().onBlockClicked(block);

                // reference the current diagram mode
                var mode = diagram().mode();

                // retrieve the block's definition viewmodel
                var definition = new viewmodels.definitionViewModel(mode, $scope.library[block.definition()]);
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

        };

        $scope.onFileDrop = function(files, evt){

            var file = files[0];
            if (!file) return;

            // todo: make this configurable
            var definitionName = "LoadFile";

            // todo: base the offset on the block dimensions
            var x = evt.pageX - 100, y = evt.pageY - 20;

            // get configuration block
            var configBlock = getConfigurationBlock(x, y, evt, definitionName);
            configBlock.setParameter("Filename", file.path);

            // create the block
            diagram().createBlock(configBlock);
        };

        /**
         * Method that allows you to drag the sizing handle of the terminal div
         * @param evt
         */
        $scope.onResizeTerminal = function(evt){

            var containerWidth,
                originX;

            $scope.$root.$broadcast("beginDrag", {
                x: evt.pageX,
                y: evt.pageY,
                config: {

                    dragStarted: function (x, y) {

                        $scope.isResizingTerminal = true;
                        containerWidth = studioWrapper.width();
                        originX = studioWrapper.offset().left;

                        $scope.$$phase || $scope.$apply();
                    },

                    dragging: function (x, y) {

                        var width = (x - originX) * 100.0 / containerWidth;
                        if (width > 5 && width < 95)
                            $scope.canvasWidth = width;
                    },

                    dragEnded: function(){
                        $scope.isResizingTerminal = false;

                        $scope.$$phase || $scope.$apply();
                    }
                }
            });
        };

        /**
         * Toggle the terminal open and closed (shares space with canvas)
         * @param evt
         */
        $scope.onToggleTerminal = function(evt){

            if($scope.canvasWidth == 100) {
                $scope.canvasWidth -= lastTerminalWidth;
                $scope.isTerminalVisible = true;
            }
            else{
                lastTerminalWidth = 100 - $scope.canvasWidth;
                $scope.canvasWidth = 100;
                $scope.isTerminalVisible = false;
            }

            evt.stopPropagation();
            evt.preventDefault();
        };

        /**
         *
         */
        var loadData = function(){

            // for context, get this diagram's id
            var diagramId = $scope.diagramViewModel.getId();
            if (!diagramId){

                // save the diagram first
                save(
                    function(){ // on success: recursively call this method again
                        loadData();
                    },
                    function() { }
                );
            }
            else { // a valid diagram exists

                // todo: calculate new block position

                modalService.show({
                    templateUrl: '/assets/scripts/views/loadData.html',
                    controller: 'loadDataController',
                    animation: {
                        type: 'fadeIn',
                        durationIn: 600
                    },
                    inputs: {
                        diagramId: diagramId,
                        diagramName: $scope.diagramViewModel.getName()
                    },
                    config: {
                        backdropClass: 'emr-modal-backdrop',
                        showCancel: true,
                        showNext: true
                    },
                    position: null
                }).then(function (modal) {

                    modal.close.then(function (result) {

                        if (result) {

                        }
                    });
                });
            }
        };

        $scope.toggleDiagrams = function() {

            $scope.diagrams.length = 0;

            $scope.showSidebar = !$scope.showSidebar;

            if ($scope.showSidebar === true) {

                loadDiagrams();
            }
        };

        $scope.open = function(name) {

            diagramService.item(name).then(function (data) {

                    $scope.diagramViewModel = new viewmodels.diagramViewModel(data);
                    $scope.onlineViewModel = {};
                    $scope.toggleCanvas(false);
                    $scope.offlineState = 0;
                    $scope.onlineState = 0;

                    subscribe($scope.diagramViewModel.getId());

                    $scope.tasks($scope.diagramViewModel.getId(), function(data){

                            console.log(JSON.stringify(data));

                            data.tasks.forEach(function(item) {

                                if (item.mode == "OFFLINE") {

                                    updateEvaluationStatus(item);
                                }
                                else if(item.mode == "ONLINE") {

                                    updateDeploymentStatus(item);
                                }
                            },
                            function (code) {

                                console.log(code); // TODO show exception
                            });
                        }
                    );
                },
                function (code) {

                    console.log(code); // TODO show exception
                }
            );
        };

        $scope.createDiagram = function(item) {

            diagramService.item().then(function (data) {

                    $scope.diagramViewModel = new viewmodels.diagramViewModel(data);
                    $scope.onlineViewModel = {};
                    $scope.toggleCanvas(false);

                    $scope.diagramViewModel.data.name = item.diagramName;
                    $scope.diagramViewModel.data.description = item.description;
                    $scope.diagramViewModel.data.owner = item.owner;
                    $scope.diagramViewModel.data.category = item.category;
                    $scope.diagramViewModel.data.targetEnvironment = item.targetEnvironment;

                },
                function (code) {

                    console.log(code); // TODO show exception
                }
            );
        };

        $scope.onDiagramCommand = function(id){

            switch(id){

                case 'c': // center - evaluate
                    evaluate();
                    break;

                case 'o1': // orbit 1 - load
                    loadData();
                    break;

                case 'o2': // orbit 2 - add block

                    break;

                case 'o3': // orbit 3 - save

                    save(function(){
                        var message = 'Diagram successfully saved.';
                        toasterService.success(message,
                            message,
                            toastContainerId);
                    },
                    function(){
                        var message = 'Diagram failed to save.';
                        toasterService.error(message,
                            message,
                            toastContainerId);
                    });
                    break;
            }
        };



        var evaluate = function() {

            $scope.evaluating = true;
            $scope.offlineState = 1;   // pending

            var offlineDiagram = $scope.diagramViewModel.data;

            var onlineDiagram = $scope.onlineViewModel.data;

            var data = {'offline': offlineDiagram, 'online': onlineDiagram};

            diagramService.evaluate(data).then(
                function (data) {

                },
                function (code) {

                    console.log(code); // TODO show exception
                });
        };

        $scope.deploy = function(evt) {

            $scope.onlineState = 1;   // pending

            var offlineDiagram = $scope.diagramViewModel.data;
            var onlineDiagram = $scope.onlineViewModel.data;
            var diagrams = {'offline': offlineDiagram, 'online': onlineDiagram};

            diagramService.deploy(diagrams).then(
                function (data) {


                },
                function (code) {

                    console.log(code); // TODO show exception
                }
            );

            evt.stopPropagation();
            evt.preventDefault();
        };

        $scope.tasks = function(diagramId, onSuccess, onFailure){

            // send tasks request
            diagramService.tasks(diagramId).then(onSuccess, onFailure);

            $scope.$$phase || $scope.$apply();
        };

        /**
         *
         * @param source
         * @param onSuccess
         * @param onFailure
         */
        $scope.interpret = function(source, onSuccess, onFailure){

            // for context, get this diagram's id
            var diagramId = $scope.diagramViewModel.getId();
            if (!diagramId){

                // save the diagram first
                save(
                    function(){ // on success: recursively call this method again
                        $scope.interpret(source, onSuccess, onFailure);
                    },
                    function() {   // on failure: call the on failure method
                        if (onFailure)
                            onFailure();
                    }
                );
            }
            else{ // a valid diagram exists

                // buid a task request including the interpreter source code
                var request = {
                    diagramId: diagramId,
                    diagramName: $scope.diagramViewModel.getName(),
                    mode: "OFFLINE",
                    targetEnvironment: $scope.diagramViewModel.getTargetEnvironment(),
                    source: source,
                    metaData: ""
                };

                diagramService.interpret(request).then(onSuccess, onFailure);
            }
        };

        $scope.kill = function(evt) {

            // retrieve the current diagram's data
            var data = diagram().data;

            diagramService.kill(data.id, data.mode).then(
                function (data) {

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

                            // todo if the diagram being deleted is active
                            // unsubscribe
                            // redirect to a new diagram
                            // remove online diagram if there is one
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
                $scope.transforming = true;

                var offlineDiagram = $scope.diagramViewModel.data;

                var onlineDiagram = $scope.onlineViewModel.data;

                var diagrams = {'offline': offlineDiagram, 'online': onlineDiagram};

                diagramService.transform(diagrams).then(
                    function (data) {

                        $scope.onlineViewModel = new viewmodels.diagramViewModel(data);
                        $scope.transforming = false;
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

        $scope.toggleDiagramNavigation = function(evt){

            if ($scope.navigatingDiagrams) {

                endDiagramNavigation();
            }
            else {

                beginDiagramNavigation();
            }

            evt.stopPropagation();
            evt.preventDefault();
        };

        var beginDiagramNavigation = function(){

            var result = popupService.show({
                templateUrl: '/assets/scripts/components/diagram/diagramNavigation.html',
                controller: 'diagramNavigationController',
                inputs: {
                    diagService: diagramService,
                    closeDialog: endDiagramNavigation,
                    openDiagram: $scope.open,
                    createNewDiagram: $scope.createDiagram,
                    deleteExistingDiagram: $scope.deleteDiagram
                }
            }).then(function(popup) {

                $scope.navigatingDiagrams = true;
                $scope.diagramNavigation = popup;
            });
        };

        var endDiagramNavigation = function(){

            $scope.diagramNavigation.controller.close();

            $scope.navigatingDiagrams = false;
            delete $scope.diagramNavigation;
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

        // fire the event to begin dragging an element
        var beginDragEvent = function(x, y, config){

            $scope.$root.$broadcast("beginDrag", {
                x: x,
                y: y,
                config: config
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

        /**
         * Update the online diagram's deployment status
         * @param message
         */
        function updateDeploymentStatus(message){

            // set online diagram's state
            switch(message.state){

                case "CREATED":
                    $scope.onlineState = 1;
                    break;
                case "RUNNING":
                    $scope.onlineState = 2;
                    break;
                case "COMPLETED":
                    $scope.onlineState = 0;
                    break;
                case "FAILED":
                    $scope.onlineState = 0;

                    // todo: create failure alert
                    break;
                case "STOPPED":
                    $scope.onlineState = 0;

                    // todo: create stopped alert
                    break;
            }

            $scope.$$phase || $scope.$apply();
        }

        /**
         * Updates the offline diagram's evaluation status
         * @param message
         */
        function updateEvaluationStatus(message){

            // set offline diagram's state
            switch(message.state){

                case "CREATED":
                    $scope.offlineState = 1;
                    break;
                case "RUNNING":
                    $scope.offlineState = 2;
                    break;
                case "COMPLETED":
                    $scope.evaluating = false;
                    $scope.offlineState = 0;
                    break;
                case "FAILED":
                    $scope.evaluating = false;
                    $scope.offlineState = 0;

                    // todo: create failure alert
                    break;
                case "STOPPED":
                    $scope.evaluating = false;
                    $scope.offlineState = 0;

                    // todo: create stopped alert
                    break;
            }

            if (message.blockId != null && message.blockState != null){

                $scope.diagramViewModel.updateBlockState(message.blockId, message.blockState);
            }

            $scope.$$phase || $scope.$apply();
        }

        /**
         * Subscribe to the specified diagram id
         * @param id - diagram id
         */
        function subscribe(id){

            console.log("subscribing to diagram: " + id);
            $webSockets.send({ messageType: "subscribe", id: id });
        }

    }]);

