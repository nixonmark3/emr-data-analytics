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

        /**
         * get or create session id
         * @returns {guid}
         */
        function getSessionId(){

            var sessionId;
            var stored = window.sessionStorage.getItem("sessionId");
            if (stored) {
                sessionId = stored;
            }
            else{
                // cache the websocket url
                sessionId =  guid();
                window.sessionStorage.setItem("sessionId", sessionId);
            }

            return sessionId;
        }

        return {

            guid: guid,

            getSessionId: getSessionId,

            /**
             * retrieve the websocket url
             */
            webSocketUrl: function(){

                var id = getSessionId();
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

    .controller('studioController', ['$scope', '$window', '$q', '$timeout', '$webSockets', 'analyticsService', 'diagramService', 'modalService', 'toasterService', 'popupService', function($scope, $window, $q, $timeout, $webSockets, analyticsService, diagramService, modalService, toasterService, popupService) {

        // initialize internal variables
        var callbacks = {},         // dictionary of deferred callbacks
            studioWrapper = angular.element(document.querySelector('#studio-wrapper')), // reference to the studio parent element
            lastTerminalWidth = 30, // initialize the variable that tracks the terminal windows' last width
            toastContainerId = "toast-container", // id of studio toast container
            evaluationState = {
                idle: 0,
                starting: 1,
                running: 2,
                stopping: 3
            },
            viewState = {
                diagram: 0,
                transitionDashboard: 1,
                dashboard: 2,
                transitionDiagram: 3
            };

        //
        // initialize scope level properties
        //

        // current width of the studio canvas
        $scope.canvasWidth = 100;
        // flag representing whether the terminal is visible
        $scope.isTerminalVisible = false;
        // flag representing whether the terminal is being resized
        $scope.isResizingTerminal = false;
        // flag representing whether the library is displayed
        $scope.showLibrary = false;
        // controls whether the offline or online canvas is shown
        $scope.onlineCanvas = false;
        // indicates whether the diagram is being transformed
        $scope.transforming = false;
        // represents the current state of the offline diagram
        $scope.offlineState = evaluationState.idle;
        // represents the current state of the online diagram
        $scope.onlineState = evaluationState.idle;
        // flag representing whether the online data view or diagram is displayed
        $scope.onlineViewState = viewState.diagram;
        // set of offline diagram methods
        $scope.offlineDiagramMethods = {};
        // terminal output messages
        $scope.terminalOutput = [];
        // online trend data and number of points to display
        $scope.onlineChart = [];
        $scope.onlineChartSize = 40;

        // setup websocket listeners
        $webSockets.reset();
        // listen for analytic task updates, append them to the terminal output
        $webSockets.listen("task-status", function(msg) { return msg.messageType == "task-status"; }, onTaskStatus);
        // listen for task variables
        $webSockets.listen("task-variable", function(msg) { return msg.messageType == "task-variable"; }, onTaskVariable);

        $window.addEventListener("unload", unsubscribe);

        $scope.$on("$destroy", function(){
            $window.removeEventListener("unload", unsubscribe);
            unsubscribe();
        });

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
         * compile the currrent diagram, display the source.
         */
        function compile() {

            // retrieve the current diagram's data
            var data = getDiagram().data;
            var title = ($scope.onlineCanvas) ? "Online Diagram" : "Offline Diagram";

            modalService.show({
                templateUrl: '/assets/scripts/views/editor.html',
                controller: 'debugController',
                animation: {
                    type: 'fadeIn',
                    durationIn: 600
                },
                inputs: { data: data },
                config: {
                    title: title,
                    backdropClass: 'emr-modal-backdrop',
                    showCancel: true
                }
            });
        }

        /**
         * handle task status messages
         * @param message: web sockets messages of type: task-status
         */
        function onTaskStatus(message){

            if (message.statusType === "DATA")
                resolveCallback(message);

            if($scope.evaluationId && $scope.evaluationId === message.id)
                updateEvaluationStatus(message);
            else if($scope.deploymentId && $scope.deploymentId === message.id)
                updateDeploymentStatus(message);

            $scope.terminalOutput.push(message);
            $scope.$$phase || $scope.$apply();
        }

        /**
         * handle task variable messages
         * @param message: web sockets messages of type: task-variable
         */
        function onTaskVariable(message){

            var temp = [];
            message.values.forEach(function (value) {
                temp.push(parseFloat(value));
            });
            $scope.onlineChart = temp;
            $scope.$$phase || $scope.$apply();
        }

        function beginDiagramNavigation(){

            var result = popupService.show({
                templateUrl: '/assets/scripts/components/diagram/diagramNavigation.html',
                controller: 'diagramNavigationController',
                backdropClass: 'emr-popup-backdrop-nav',
                inputs: {
                    diagService: diagramService,
                    closeDialog: endDiagramNavigation,
                    openDiagram: $scope.open,
                    createNewDiagram: $scope.createDiagram,
                    deleteExistingDiagram: $scope.deleteDiagram,
                    currentDiagram: getDiagram()
                }
            }).then(function(popup) {

                $scope.navigatingDiagrams = true;
                $scope.diagramNavigation = popup;
            });
        }

        function endDiagramNavigation(){

            $scope.diagramNavigation.controller.close();

            $scope.navigatingDiagrams = false;
            delete $scope.diagramNavigation;
        }

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
        var getDiagram = function(){
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
         * Updates the offline diagram's evaluation status
         * @param message
         */
        function updateDeploymentStatus(message){

            // set offline diagram's state
            switch(message.statusType){

                case "STARTED":
                    $scope.onlineState = evaluationState.running;
                    break;
                case "TERMINATED":
                    $scope.onlineState = evaluationState.idle;
                    break;
                case "FAILED":
                    $scope.onlineState = evaluationState.idle;

                    toasterService.error("Diagram deployment failed.",
                        message.message,
                        toastContainerId);

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
            switch(message.statusType){

                case "STARTED":
                    $scope.offlineState = evaluationState.running;
                    break;
                case "OUTPUT":
                    var stateChange = message.message.split(",");
                    if (stateChange.length == 2) {
                        var state = Number(stateChange[1]);
                        if (!Number.isNaN(state))
                            $scope.diagramViewModel.updateBlockState(stateChange[0], state);
                    }
                    break;

                case "COMPLETED":
                    $scope.offlineState = evaluationState.idle;
                    break;
                case "FAILED":
                    $scope.offlineState = evaluationState.idle;

                    toasterService.error("Diagram evaluation failed.",
                        message.message,
                        toastContainerId);

                    break;
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

        /**
         * Unsubscribe the current diagram
         */
        var unsubscribe = function(){

            var diagramId = getDiagram().getId();
            if (diagramId){
                // terminate the diagram's analytics session
                diagramService.terminate(diagramId, "OFFLINE");
            }
        };

        /**
         * Create block data for the coordinates and definition name
         * @param x
         * @param y
         * @param definitionName
         * @returns {*|BlockData}
         */
        function getBlockData(x, y, definitionName){

            // reference the current diagram
            var currentDiagram = getDiagram();

            // get the definition view model
            var definitionViewModel = new viewmodels.definitionViewModel(currentDiagram.mode(), $scope.library[definitionName]);

            // translate diagram coordinates
            var point = $scope.offlineDiagramMethods.translateCoordinates(x, y);

            // get the block data
            return currentDiagram.getBlockData(point.x, point.y, definitionViewModel);
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

        /**
         * Method to resolve deferred web socket callbacks
         * @param message: web socket message that may be associated with a deferred callback
         */
        function resolveCallback(message){

            if (angular.isDefined(callbacks[message.id])){
                // a callback exists: pop from the callbacks dictionary and resolve

                var deferred = callbacks[message.id];
                delete callbacks[message.id];

                var data = JSON.parse(message.message);
                deferred.resolve(data);
            }
        }

        /**
         * Terminate the analytics task for the current diagram
         */
        function terminate(){
            var diagram = getDiagram();
            var mode = diagram.getMode();
            diagramService.terminate(diagram.getId(), mode)
                .then(
                    function () {

                        // todo: race condition with terminate task status message
                        /*
                        if(mode == "OFFLINE" && $scope.offlineState == evaluationState.running)
                            $scope.offlineState = evaluationState.stopping;
                        else if(mode == "ONLINE" && $scope.onlineState == evaluationState.running)
                            $scope.onlineState = evaluationState.stopping;

                        $scope.$$phase || $scope.$apply();
                        */
                    },
                    function (reason) {

                        var message = 'Termination failed.';
                        toasterService.error(message,
                            message,
                            toastContainerId);
                    }
                );
        }

        //
        // public methods
        //

        /**
         * Load the specified dynamic parameter source
         * @param request: represents the parameter to be loaded
         */
        $scope.onLoadParameterSource = function(parameter){

            var deferred = $q.defer();

            var currentDiagram = getDiagram();
            var request = {    // initialize dynamic source request
                diagramId: currentDiagram.getId(),
                sessionId: analyticsService.getSessionId(),
                diagramName: currentDiagram.getName(),
                mode: currentDiagram.getMode(),
                targetEnvironment: currentDiagram.getTargetEnvironment(),
                parameter: parameter
            };

            diagramService.resolveSource(request).then(
                function(data){

                    switch(parameter.source.parameterSourceType){
                        case "JAR":
                            deferred.resolve(data);
                            break;
                        case "PYTHONSCRIPT":

                            console.log(data);

                            // python script results are returned via websockets, store the promise as a callback
                            callbacks[data] = deferred;
                            break;
                    }
                },
                function(status){
                    deferred.reject(status);
                }
            );

            return deferred.promise;
        };

        /*
        ** Display block details / results
         */
        $scope.onBlockDisplay = function(position, block){

            var diagram = getDiagram();
            if (!diagram.getId()){

                // save the diagram first
                save(
                    function(){ // on success: recursively call this method again
                        $scope.onBlockDisplay(position, block);
                    },
                    function() {
                        var message = 'Diagram failed to save.';
                        toasterService.error(message,
                            message,
                            toastContainerId);
                    }
                );
            }
            else{

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
                                diagram: diagram,
                                block: block
                            },
                            config: {
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

                    case "LOAD":

                        modalService.show({
                            templateUrl: '/assets/scripts/views/loadData.html',
                            controller: 'loadDataController',
                            inputs: {
                                diagram: diagram,
                                block: block.data
                            },
                            config: {
                                backdropClass: 'emr-modal-backdrop',
                                showCancel: true,
                                showNext: true
                            },
                            position: modalPosition
                        }).then(function (modal) {

                            modal.close.then(function (result) {

                                if (result) {

                                }
                            });
                        });

                        break;

                    case "STREAM":

                        modalService.show({
                            templateUrl: '/assets/scripts/views/setStream.html',
                            controller: 'setStreamController',
                            inputs: {
                                diagram: diagram,
                                block: block.data
                            },
                            config: {
                                backdropClass: 'emr-modal-backdrop'
                            },
                            position: modalPosition
                        }).then(function (modal) {

                            modal.close.then(function (result) {

                                if (result) {

                                }
                            });
                        });

                        break;

                    case "OUTPUT":

                        modalService.show({
                            templateUrl: '/assets/scripts/views/setOutput.html',
                            controller: 'setOutputController',
                            inputs: {
                                diagram: diagram,
                                block: block.data
                            },
                            config: {
                                backdropClass: 'emr-modal-backdrop'
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

                        var mode = diagram.mode();
                        var definition = new viewmodels.definitionViewModel(mode, $scope.library[block.definition()]);

                        modalService.show({
                            templateUrl: '/assets/scripts/views/blockData.html',
                            controller: 'blockDataController',
                            inputs: {
                                block: new viewmodels.configuringBlockViewModel(definition, block.data),
                                loadParameterSource: $scope.onLoadParameterSource,
                                diagram: diagram
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
                getDiagram().onBlockClicked(block);

                // reference the current diagram mode
                var mode = getDiagram().mode();

                // retrieve the block's definition viewmodel
                var definition = new viewmodels.definitionViewModel(mode, $scope.library[block.definition()]);
            }
        };

        $scope.onCreateBlock = function(x, y, evt, definitionName){

            var blockData = getBlockData(x, y, definitionName);

            // create the block
            getDiagram().addBlock(blockData);
        };

        /*
        ** When all blocks are being deselected - stop editing and hide the studio properties panel
         */
        $scope.onDiagramDeselection = function(){

        };

        $scope.onFileDrop = function(files, evt){

            var file = files[0];
            if (!file) return;

            // retrieve the offline diagram's current zoom level
            var zoomLevel = $scope.offlineDiagramMethods.getZoomLevel();

            // identify the load definition and the position of the new block
            var definitionName = "Load",
                x = evt.pageX - zoomLevel * 100,
                y = evt.pageY - zoomLevel * 40;

            // create the block data
            var blockData = getBlockData(x, y, definitionName);

            // reference the load configuration
            var config = blockData.parameters[0].value;

            // set the file path
            config.source.dataSources.push({
                "dataSourceType": "FILE",
                "name": file.name,
                "progress": 100,
                "contentType": file.contentType,
                "path": file.path
            });

            // give this parse instance a guid
            config.parse.id = analyticsService.guid();

            // set the parse type
            if (file.contentType.indexOf("spreadsheet") > -1)
                config.parse.parseType = "SPREADSHEET";

            // add the block to the current diagram
            getDiagram().addBlock(blockData);
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

            if ($scope.navigatingDiagrams) {

                endDiagramNavigation();
            }

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
            var diagram = getDiagram();
            var diagramId = diagram.getId();

            if (!diagramId){

                // save the diagram first
                save(
                    function(){ // on success: recursively call this method again
                        loadData();
                    },
                    function(){
                        var message = 'Diagram failed to save.';
                        toasterService.error(message,
                            message,
                            toastContainerId);
                    }
                );
            }
            else { // a valid diagram exists

                // retrieve the offline diagram's current zoom level
                var zoomLevel = $scope.offlineDiagramMethods.getZoomLevel();

                // determine the position of the new block
                var diagramContainer = angular.element(document.querySelector('#studio-container'));

                // initialize the position variable - y is always in the same spot
                var position = { centerX: 0, centerY: diagramContainer.offset().top + zoomLevel * 90};

                // retrieve the diagram's boundary
                var boundary = diagram.getBoundary();
                if (boundary.x2 == Number.MIN_VALUE){ // the diagram is currently empty, set x to the center of the diagram

                    position.centerX = diagramContainer.offset().left + diagramContainer.width() / 2;
                }
                else{
                    var coords = $scope.offlineDiagramMethods.inverseCoordinates(boundary.x2, 0);

                    // position the new block to the right of the diagram boundary
                    position.centerX = coords.x + zoomLevel * 120;
                }

                // create block data
                var definitionName = "Load",
                    x = position.centerX - zoomLevel * 100,
                    y = position.centerY - zoomLevel * 40;

                var blockData = getBlockData(x, y, definitionName);

                modalService.show({
                    templateUrl: '/assets/scripts/views/loadData.html',
                    controller: 'loadDataController',
                    animation: {
                        type: 'fadeIn',
                        durationIn: 600
                    },
                    inputs: {
                        diagram: $scope.diagramViewModel,
                        block: blockData
                    },
                    config: {
                        backdropClass: 'emr-modal-backdrop',
                        showCancel: true,
                        showNext: true
                    },
                    position: position
                }).then(function (modal) {

                    // drop the new block on the canvas

                    modal.close.then(function (result) {

                        if (result) {
                            // create the block
                            getDiagram().addBlock(result.block);
                        }
                    });
                });
            }
        };

        $scope.open = function(name) {

            diagramService.item(name).then(function (data) {

                    $scope.diagramViewModel = new viewmodels.diagramViewModel(data);
                    $scope.onlineViewModel = {};
                    $scope.toggleCanvas(false);
                    $scope.offlineState = 0;
                    $scope.onlineState = 0;
                    $scope.terminalOutput = []; // clear the terminal 

                    subscribe($scope.diagramViewModel.getId());

                    $scope.tasks($scope.diagramViewModel.getId(), function(data){

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

                case 'c': // center - evaluate or deploy

                    if($scope.onlineCanvas)
                        deploy();
                    else
                        evaluate();

                    break;

                case 'o1': // orbit 1 - load
                    loadData();
                    break;

                case 'o2': // orbit 2 - source code
                    compile();
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

                case 'x':
                    terminate();

                    break;
            }
        };

        /**
         * initiate deployment of the online diagram
         */
        var deploy = function(){

            // put the client in a pending deployment state
            $scope.onlineState = evaluationState.starting;

            // capture the offline and online diagrams
            var data = {
                'offline': $scope.diagramViewModel.data,
                'online': $scope.onlineViewModel.data
            };

            // initiate deployment
            diagramService.deploy(data).then(
                function (taskId) {

                    // capture the deployment id
                    $scope.deploymentId = taskId;
                },
                function (reason) {

                    var message = 'Deployment failed.';
                    toasterService.error(message,
                        message,
                        toastContainerId);
                }
            );
        };

        /**
         * initiate evaluation of the offline diagram
         */
        var evaluate = function() {

            var diagram = $scope.diagramViewModel;
            if (!diagram.getId()){

                // save the diagram first
                save(
                    function(){ // on success: recursively call this method again
                        evaluate();
                    },
                    function(){
                        var message = 'Diagram failed to save.';
                        toasterService.error(message,
                            message,
                            toastContainerId);
                    }
                );
            }
            else{

                // put the client in a pending evaluation state
                $scope.offlineState = evaluationState.starting;

                var data = {
                    'offline': diagram.data,
                    'online': $scope.onlineViewModel.data
                };

                diagramService.evaluate(data).then(
                    function(taskId){

                        // reference this evaluation's task id
                        $scope.evaluationId = taskId;
                    },
                    function (reason) {

                        $scope.offlineState = evaluationState.idle;

                        var message = 'Diagram failed to evaluate.';
                        toasterService.error(message,
                            message,
                            toastContainerId);
                    });
            }
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
                    sessionId: analyticsService.getSessionId(),
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
            var data = getDiagram().data;

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

            if ($scope.navigatingDiagrams) {

                endDiagramNavigation();
            }

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

        $scope.toggleDiagramNavigation = function(evt){

            if ($scope.navigatingDiagrams)
                endDiagramNavigation();
            else
                beginDiagramNavigation();

            evt.stopPropagation();
            evt.preventDefault();
        };

        /**
         * Toggle between the online diagram and dashboard views
         * @param evt
         */
        $scope.toggleOnlineView = function(evt){

            // transition duration (ms): amount of time to wait for the transition animation to unfold
            var duration = 500;

            switch($scope.onlineViewState){
                case viewState.diagram:

                    // switch to the dashboard view
                    $scope.onlineViewState = viewState.transitionDashboard;
                    $timeout(function() {
                        $scope.onlineViewState = viewState.dashboard;
                    }, duration);
                    break;

                case viewState.dashboard:

                    // switch to the diagram view
                    $scope.onlineViewState = viewState.transitionDiagram;
                    $timeout(function() {
                        $scope.onlineViewState = viewState.diagram;
                    }, duration);
                    break;
            }

            evt.stopPropagation();
            evt.preventDefault();
        };

    }]);

