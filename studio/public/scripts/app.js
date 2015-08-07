'use strict';

var analyticsApp = angular.module('analyticsApp',
    ['diagramApp',
        'browserApp',
        'emr.ui.controls',
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

                // todo: retrieve the url prefix from the server
                var url = "ws://localhost:9000/analytics/socket/";
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

                return url + id;
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
             *
             * @param predicate
             * @param handler
             * @param persist
             */
            websocket.listen = function (predicate, handler, persist) {

                if (persist == null)
                    persist = false;

                listeners.push({ p: predicate, h: handler, persist: persist });
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
                    if (listener.p(obj))
                        listener.h(obj);
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
        $webSockets.listen(function(msg) { return msg.type == "ping"; }, setPing, true);

        // ping the analytics service
        $webSockets.send({ type: "ping" });

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

        // reference the cards element
        var cards = angular.element('#dashboard-cards');

        // initialize variables, cards and selector
        $scope.loading = false;

        $scope.jobs = [];

        $scope.cards = {
            online: -1,
            offline: -1
        };

        $scope.selector = {
            index: 0,
            width: (cards.width() / 4),
            height: cards.height()
        };

        // set up websockets
        $webSockets.reset();
        $webSockets.listen(function(msg) { return msg.type == "jobs-summary"; }, setCards);
        $webSockets.listen(function(msg) { return msg.type == "job-infos"; }, setJobs);
        $webSockets.send({ type: "dashboard" });
        $webSockets.send({ type: "jobs-summary" });

        $webSockets.listen(function(msg) { return msg.type == "evaluationStatus"; }, updateOfflineJob);
        $webSockets.listen(function(msg) { return msg.type == "deploymentStatus"; }, function(msg) { updateOnlineJob(msg.jobInfo); });

        // watch for changes in container dimensions
        $scope.$watch(function() { return cards.width(); }, function(newWidth, oldWidth) {
            $scope.selector.width = (newWidth / 4);
        });

        /**
         * Set the active card index
         * @param index
         */
        $scope.activeCard = function(index){

            if ($scope.selector.index != index) {
                $scope.selector.index = index;
                requestJobs();
            }

            $scope.$$phase || $scope.$apply();
        };

        /**
         *
         * @param job
         */
        $scope.kill = function(job){

            job.killing = true;

            // send kill request
            diagramService.kill(job.diagramId, job.mode).then(
                function (data) { },
                function (code) {

                    console.log(code); // TODO show exception
                }
            );

            $scope.$$phase || $scope.$apply();
        };

        $scope.formatDate = function(unix){

            var date = new Date(unix);
            return date.toLocaleString();
        };

        /**
         *
         */
        function requestJobs(){

            switch($scope.selector.index){

                case 0:
                    $webSockets.send({ type: "online-jobs" });
                    $scope.loading = true;
                    break;
                case 1:
                    $webSockets.send({ type: "online-jobs" });
                    $scope.loading = true;
                    break;
                case 2:
                    $webSockets.send({ type: "offline-jobs" });
                    $scope.loading = true;
                    break;
                case 3:
                    $scope.jobs = [];
                    break;
            }
        }

        /**
         *
         * @param message
         */
        function setCards(message){

            $scope.cards = {
                online: message.online,
                offline: message.offline
            };

            $scope.$$phase || $scope.$apply();
        }

        /**
         *
         * @param jobs
         */
        function setJobs(jobs){

            $scope.jobs = jobs.items;
            $scope.loading = false;

            $scope.$$phase || $scope.$apply();
        }

        function updateOfflineJob(message){

            if ($scope.selector.index == 2){ // offline jobs

                var job;
                var jobIndex = 0;
                $scope.jobs.forEach(function(element, index){

                    if (element.diagramId == message.diagramId){

                        job = element;
                        jobIndex = index;
                    }
                });

                if (job){
                    // the updated online job is in the list
                    switch(message.state){
                        case "COMPLETED":
                        case "FAILED":
                        case "STOPPED":

                            $scope.jobs.splice(jobIndex, 1);
                            break;
                    }
                }
                else{
                    // the item is not in the list - append if running
                    if (message.state == "RUNNING")
                        $scope.jobs.push(message);
                }
            }

            $scope.$$phase || $scope.$apply();
        }

        function updateOnlineJob(message){

            if ($scope.selector.index == 0 || $scope.selector.index == 1){ // online jobs

                var job;
                var jobIndex = 0;
                $scope.jobs.forEach(function(element, index){

                    if (element.diagramId == message.diagramId){

                        job = element;
                        jobIndex = index;
                    }
                });

                if (job){
                    // the updated online job is in the list
                    switch(message.state){
                        case "COMPLETED":
                        case "FAILED":
                        case "STOPPED":

                            $scope.jobs.splice(jobIndex, 1);
                            break;
                    }
                }
                else{
                    // the item is not in the list - append if running
                    if (message.state == "RUNNING")
                        $scope.jobs.push(message);
                }
            }

            $scope.$$phase || $scope.$apply();
        }

        // make request to initialize list of jobs
        requestJobs();
    }])

    .controller('studioController', ['$scope', '$window', '$timeout', '$webSockets', 'diagramService', 'modalService', 'popupService',
        function($scope, $window, $timeout, $webSockets, diagramService, modalService, popupService) {

        //
        // initialize scope level properties
        //

        // controls whether the library is displayed
        $scope.showLibrary = false;
        // controls whether the off-canvas sidebar is displayed
        $scope.showSidebar = false;
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

        $scope.offlineDiagramMethods = {};

        $scope.diagrams = [];

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

        // setup websocket listeners
        $webSockets.reset();
        $webSockets.listen(function(msg) { return msg.type == "evaluationStatus"; }, updateEvaluationStatus);
        $webSockets.listen(
            function(msg) { return msg.type == "deploymentStatus"; },
            function(msg) { updateDeploymentStatus(msg.jobInfo); }
        );

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

        function loadDiagrams() {

            $scope.diagrams.length = 0;

            diagramService.listDiagrams().then(
                function (data) {

                    $scope.diagrams = data;
                    $scope.diagrams.forEach(function (item) {

                        initializeNavigationItem(item);
                    });
                },
                function (code) {

                    // todo: show exception
                    console.log(code);
                }
            );
        };

        $scope.loadAvailableDiagrams = function() {

            console.log('called');
            return loadDiagrams();
        };

        /* Scope Level Methods */

        $scope.compile = function(evt) {

            // retrieve the current diagram's data
            var data = diagram().data;

            diagramService.compile(data).then(
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
                            name: block.data.name,
                            showSave : false,
                            showCancel: true
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
                        config: {
                            // todo put this back when refactor results name: block.data.name,
                            showSave : false,
                            showCancel: true
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

            $scope.diagrams.length = 0;

            $scope.showSidebar = !$scope.showSidebar;

            if ($scope.showSidebar === true) {

                loadDiagrams();
            }
        };

        $scope.open = function(name) {

            diagramService.item(name).then(
                function (data) {
                    $scope.diagramViewModel = new viewmodels.diagramViewModel(data);
                    $scope.onlineViewModel = {};
                    $scope.toggleDiagrams();
                    $scope.toggleCanvas(false);

                    // reset execution state
                    $scope.offlineState = 0;
                    $scope.onlineState = 0;

                    subscribe($scope.diagramViewModel.getId());

                    updateSelectedDiagram();

                    $scope.info($scope.diagramViewModel.getId(),
                        function(data){

                            data.items.forEach(function(item){

                                if (item.mode == "OFFLINE"){
                                    updateEvaluationStatus(item);
                                }
                                else if(item.mode == "ONLINE"){
                                    updateDeploymentStatus(item);
                                }
                            });
                        }
                    );
                },
                function (code) {

                    console.log(code); // TODO show exception
                }
            );
        };

        $scope.createDiagram = function() {
            diagramService.item().then(
                function (data) {

                    $scope.diagramViewModel = new viewmodels.diagramViewModel(data);
                    $scope.onlineViewModel = {};
                    $scope.toggleDiagrams();
                    $scope.toggleCanvas(false);
                },
                function (code) {

                    console.log(code); // TODO show exception
                }
            );
        };

        /**
         * Save the current diagram
         * @param evt
         */
        $scope.save = function(evt) {

            // check whether the diagram is new
            var isNew = false;
            if (!$scope.diagramViewModel.getId())
                isNew = true;

            // create diagram container
            var offlineDiagram = $scope.diagramViewModel.data;
            var onlineDiagram = $scope.onlineViewModel.data;
            var data = {'offline': offlineDiagram, 'online': onlineDiagram};

            diagramService.save(data).then(
                function (diagramId) {

                    if (isNew){

                        // capture the new diagram id
                        $scope.diagramViewModel.setId(diagramId);
                        subscribe($scope.diagramViewModel.getId());
                    }
                },
                function (code) {

                    console.log(code); // TODO show exception
                }
            );

            evt.stopPropagation();
            evt.preventDefault();
        };

        $scope.evaluate = function(evt) {

            $scope.offlineState = 1;   // pending

            var offlineDiagram = $scope.diagramViewModel.data;

            var onlineDiagram = $scope.onlineViewModel.data;

            var data = {'offline': offlineDiagram, 'online': onlineDiagram};

            diagramService.evaluate(data).then(
                function (data) {

                },
                function (code) {

                    console.log(code); // TODO show exception
                }
            );

            evt.stopPropagation();
            evt.preventDefault();
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

        $scope.info = function(diagramId, onSuccess){

            // send info request
            diagramService.info(diagramId).then(onSuccess,
                function (code) {

                    console.log(code); // TODO show exception
                }
            );

            $scope.$$phase || $scope.$apply();
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

        $scope.deleteDiagram = function(diagram) {

            var diagramName = diagram.name;

            $scope.diagrams.splice($scope.diagrams.indexOf(diagram), 1);

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
                    closeDialog: endDiagramNavigation
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
                    $scope.offlineState = 0;
                    break;
                case "FAILED":
                    $scope.offlineState = 0;

                    // todo: create failure alert
                    break;
                case "STOPPED":
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
            $webSockets.send({ type: "subscribe", id: id });
        }

    }]);

