'use strict';

analyticsApp

    /**
     *
     */
    .controller('blockDataController', ['$scope', '$element', '$window', '$timeout', '$q', 'diagramService', 'block', 'loadParameterSource', 'diagram', 'config', 'position', 'close', function($scope, $element, $window, $timeout, $q, diagramService, block, loadParameterSource, diagram, config, position, close){

        $scope.position = position;
        $scope.block = block.data;
        $scope.diagram = diagram;
        $scope.config = config;
        $scope.config.icon = "fa-cube";
        $scope.config.cancelLabel = "Ok";
        $scope.pages = [];
        $scope.currentPage = null;
        $scope.activePage = 0;
        $scope.blockProperties = block;
        $scope.loadParameterSource = loadParameterSource;
        $scope.loading = true;

        $scope.savePropertiesChanges = function() {

            diagram.updateBlock($scope.blockProperties);

            $scope.propertiesConfig.isDirty = false;
        };

        $scope.cancelPropertiesChanges = function() {

            $scope.blockProperties = $scope.blockProperties.reset();

            $scope.propertiesConfig.isDirty = false;
        };

        $scope.propertiesChanged = function() {

            $scope.propertiesConfig.isDirty = true;
        };

        $scope.propertiesConfig = {

            isDirty: false
        };

        $scope.setActivePage = function(selectedIndex, selectedPage) {

            $scope.loading = true;
            $scope.activePage = selectedIndex;
            $scope.currentPage = selectedPage;

            getData();
        };

        $scope.methods = {

            close: onClose
        };

        function onClose(transitionDuration) {

            close(null, transitionDuration);
        }

        $scope.setGridWidth = function(columns) {

            if (columns != null) {

                var additionalLength = 0;

                if (columns.length < 3) {
                    additionalLength++;
                }

                return (columns.length + additionalLength) * 220;
            }

            return 1000;
        };

        $scope.getBlockData = function(type, key, success) {

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

        function init() {

            $timeout(function() {

                $scope.getBlockData('Pages',
                    $scope.block.id,
                    function(results) {

                        if ($scope.blockProperties.parameters.length > 0) {

                            $scope.pages.push({name:"Properties",type:"props", data: null});
                        }

                        for(var i = 0; i < results.length; i++) {

                            $scope.pages.push(results[i]);
                        }

                        if ($scope.pages.length > 0) {

                            $scope.currentPage = $scope.pages[0];
                        }

                        $scope.config.title = $scope.block.name;
                        $scope.config.subTitle = "Configure and review block details";

                        getData();
                    });
            }, 500);
        }

        function getData() {

            if ($scope.currentPage != null) {

                if ($scope.currentPage.data == null) {

                    $scope.getBlockData(
                        $scope.currentPage.name,
                        $scope.block.id,
                        function (results) {

                            $scope.currentPage.data = results;
                            $scope.loading = false;
                        });
                }
                else {

                    $scope.loading = false;
                }
            }

        }

        init();

    }])

    /**
     *
     */
    .controller('blockGroupController', ['$scope', '$element', '$timeout', 'diagramService', 'position', 'diagram', 'close',
        function($scope, $element, $timeout, diagramService, position, diagram, close){

            $scope.loading = true;
            $scope.position = position;

            // generate the list of the selected blocks by unique name
            var blocks = diagram.getSelectedBlocks().map(function(block){ return block.id() });

            // package up the group request
            var request = {
                name: diagram.generateBlockName("Group"), // create a unique for the new group
                diagram: diagram.data,
                blocks: blocks
            };

            $timeout(function(){
                diagramService.group(request).then(

                    function (data) {

                        // reference the resulting diagram
                        $scope.diagram = new viewmodels.diagramViewModel(data);

                        // reference the nested diagram
                        $scope.nestedDiagram = $scope.diagram.findDiagram(request.name);

                        $scope.loading = false;
                    },
                    function (code) {

                        $scope.loading = false;
                    }
                );
            }, 600);

            $scope.close = function(transitionDelay){

                close(null, transitionDelay);
            };

            $scope.save = function(transitionDelay){

                close($scope.diagram, transitionDelay);
            };
        }
    ])

    /**
     *
     */
    .controller('debugController', ['$scope', '$element', '$timeout', 'diagramService', 'data', 'config', 'position', 'close',
        function($scope, $element, $timeout, diagramService, data, config, position, close){

            $scope.config = config;
            $scope.position = position;
            $scope.config.icon = "fa-code";
            $scope.config.subTitle = "Compiled Source Code";
            $scope.config.cancelLabel = "Close";

            $scope.methods = {
                close: onClose
            };

            diagramService.compile(data).then(
                function (source) {
                    $scope.editor = { data: source };
                },
                function (code) {
                    console.log(code);
                }
            );

            function onClose(transitionDelay){

                close(null, transitionDelay);
            }
        }
    ])

    /**
     *
     */
    .controller('blockConfigEditorController', ['$scope', '$element', '$timeout', 'diagramService', 'data', 'config', 'position', 'close',
        function($scope, $element, $timeout, diagramService, data, config, position, close){

            $scope.config = config;
            $scope.position = position;
            $scope.editor = { data: data };

            $scope.close = function(transitionDelay){

                close(null, transitionDelay);
            };

            $scope.save = function(transitionDelay){

                close($scope.editor.data, transitionDelay);
            };
        }
    ])

    /**
     *
     */
    .controller('editorController', ['$scope', '$element', '$timeout', 'config', 'position', 'close',
        function($scope, $element, $timeout, config, position, close){

            $scope.config = config;
            $scope.position = position;

            // todo: temporarily hardcode default json
            $scope.editor = {
                data: JSON.stringify({
                topic: "SIM",
                streamingSource: {
                    pollingSourceType: "Simulated",
                    url: "http://localhost",
                    frequency: 1,
                    keys: [
                        "IN7OUT1MODEL/TAG1.CV",
                        "IN7OUT1MODEL/TAG2.CV",
                        "IN7OUT1MODEL/TAG3.CV",
                        "IN7OUT1MODEL/TAG4.CV",
                        "IN7OUT1MODEL/TAG5.CV",
                        "IN7OUT1MODEL/TAG6.CV",
                        "IN7OUT1MODEL/TAG7.CV"
                    ]}
                }, null, '\t')
            };

            $scope.close = function(transitionDelay){

                close(null, transitionDelay);
            };

            $scope.save = function(transitionDelay){

                close($scope.editor.data, transitionDelay);
            };
        }
    ])

    /**
     * Explore dialog
     */
    .controller('exploreController', ['$scope', '$element', '$q', '$webSockets', 'analyticsService', 'toasterService', 'diagramService', 'diagram', 'block', 'config', 'position', 'close', function($scope, $element, $q, $webSockets, analyticsService, toasterService, diagramService, diagram, block, config, position, close) {

        // add the position and config variables to the current scope so that they can be accessed by modal
        $scope.position = position;
        $scope.config = config;
        $scope.config.title = block.name();
        $scope.config.subTitle = "Explore";

        $scope.methods = {
            close: onClose
        };

        $scope.features = [];
        $scope.activeIndex = 0;

        var inputWire = "in",
            toastContainerId = "modal-toaster",
            callbacks = {};

        //
        // Internal methods
        //

        /**
         * Initialize the explore block
         */
        function init() {

            // listen for describe and data messages
            $webSockets.listen("describe", function (msg) {
                return msg.messageType == "describe";
            }, onDescribe);
            $webSockets.listen("data", function (msg) {
                return msg.messageType == "data";
            }, onMessage);

            // build a list of input connections
            var blockConnections = [];
            var wires = diagram.findInputWires(block.id(), inputWire);
            wires.forEach(function (wire) {

                var blockName = diagram.findBlock(wire["from_node"]).name();
                var blockConnection = blockName + "/" + wire["from_connector"];

                blockConnections.push(blockConnection);
            });

            var request = {
                diagramId: diagram.getId(),
                sessionId: analyticsService.getSessionId(),
                diagramName: diagram.getName(),
                targetEnvironment: diagram.getTargetEnvironment(),
                blockConnections: blockConnections
            };

            diagramService.describe(request).then(null,
                function (data, status) {

                    toasterService.error(data,
                        data,
                        toastContainerId);
                }
            );
        }

        //
        function onClose(transitionDelay) {
            close(null, transitionDelay);
        }

        function onDescribe(describe) {
            $scope.features = describe.describe.features;
        }

        function onMessage(msg) {

            if (angular.isDefined(callbacks[msg.id])) {
                var deferred = callbacks[msg.id];
                delete callbacks[msg.id];

                deferred.resolve(msg);
            }
            else {
                toasterService.error("Web socket message without corresponding callback.",
                    "Web socket message without corresponding callback.",
                    toastContainerId);
            }
        }

        //
        // Public methods
        //

        /**
         * Set the active view index
         * @param index: new view index
         */
        $scope.setActiveIndex = function (index) {
            $scope.activeIndex = index;
        };

        /**
         * Fetch the specified set of features
         * @param dataSourceName: name of data source that has the features
         * @param featureNames: list of features to fetch
         * @param onSuccess: method to call on success
         */
        $scope.onFetch = function (features, onSuccess) {

            var deferred = $q.defer();

            var featureSet = features.map(function(feature){
                return { sourceName: feature.sourceName, name: feature.name };
            });

            var request = {
                diagramId: diagram.getId(),
                sessionId: analyticsService.getSessionId(),
                diagramName: diagram.getName(),
                targetEnvironment: diagram.getTargetEnvironment(),
                features: featureSet
            };

            diagramService.collect(request).then(
                function (guid) {

                    callbacks[guid] = deferred;
                },
                function (data, status) {

                    toasterService.error(data,
                        data,
                        toastContainerId);
                }
            );

            deferred.promise.then(
                function (data) {

                    onSuccess(data);
                },
                function (data, status) {

                    toasterService.error(data,
                        data,
                        toastContainerId);
                }
            );
        };

        init();
    }])

    /**
     * Library browser popup
     */
    .controller('libraryBrowserController', ['$scope', '$element', 'nodes', 'onDrag', 'onDrop', 'close',
        function($scope, $element, nodes, onDrag, onDrop, close) {

            $scope.nodes = nodes;

            $scope.onDrag = onDrag;

            $scope.onDrop = onDrop;

            $scope.close = function(){

                close();
            };

            this.close = $scope.close;
        }
    ])

    /**
     * Load Data wizard
     * Dialog that walks the user through loading, parsing, and cleaning data
     */
    .controller('loadDataController', ['$scope', '$element', '$q', '$webSockets', 'analyticsService', 'toasterService', 'diagramService', 'diagram', 'block', 'config', 'position', 'close', function($scope, $element, $q, $webSockets, analyticsService, toasterService, diagramService, diagram, block, config, position, close){

        // add the position and config variables to the current scope so that they can be accessed by modal
        $scope.position = position;
        $scope.config = config;
        $scope.config.icon = "fa-upload";
        $scope.config.subTitle = "Load Data";
        $scope.config.saveLabel = "Finish";

        $scope.methods = {
            close: onClose,
            next: onNext,
            back: onBack,
            save: onSave
        };

        $scope.features = [];

        $scope.activeStep = 0;
        $scope.direction = 0;
        $scope.previewMode = 0;
        $scope.isEvaluating = false;
        $scope.isTerminalInputOpen = false;
        $scope.menuWidth = 20;
        $scope.terminalPosition = 5000;
        $scope.codeMirrorOptions = { mode: 'python' };
        $scope.dateRange = { startTime: null, endTime: null };

        // a dictionary of file names and their associated index numbers
        $scope.fileDict = {};

        var loadConfig = block.parameters[0].value;
        $scope.source = loadConfig.source;
        $scope.parse = loadConfig.parse;
        $scope.transformations = loadConfig.transformations;

        $scope.progressSteps = [
            { name: 'source', icon: 'fa-database' },
            { name: 'parse', icon: 'fa-code' },
            { name: 'clean', icon: 'fa-paint-brush' }
        ];

        var toastContainerId = "load-data-toaster",
            codeMirror,
            callbacks = {};

        //
        // internal methods
        //

        function configureActiveStep(){

            switch($scope.activeStep){

                case 0:
                    $scope.config.title = "Select a Data Source";
                    $scope.config.showBack = false;
                    $scope.config.showNext = true;
                    $scope.config.showSave = false;
                    break;
                case 1:
                    $scope.config.title = "Parse the Data";
                    $scope.config.showBack = true;
                    $scope.config.showNext = true;
                    $scope.config.showSave = false;
                    break;
                case 2:
                    $scope.config.title = "Clean the Data";
                    $scope.config.showBack = true;
                    $scope.config.showNext = false;
                    $scope.config.showSave = true;

                    $scope.onTerminalClose();
                    break;
            }
        }

        function getConfiguration(){

            block.parameters[0].value = {
                source: $scope.source,
                parse: $scope.parse,
                transformations: $scope.transformations
            };

            return {
                diagramId: diagram.getId(),
                sessionId: analyticsService.getSessionId(),
                diagramName: diagram.getName(),
                targetEnvironment: diagram.getTargetEnvironment(),
                block: block
            };
        }

        /**
         * initialize the variables
         */
        function init(){

            $webSockets.listen("describe", function(msg){ return msg.messageType == "describe"; }, onDescribe);
            $webSockets.listen("data", function(msg){ return msg.messageType == "data"; }, onMessage);

            // give the parse element a unique id so that it can be tracked
            if ($scope.parse.id === null)
                $scope.parse.id = analyticsService.guid();

            // add file data sources
            for(var i = 0; i < $scope.source.dataSources.length; i++) {
                var source = $scope.source.dataSources[i];
                if (source.dataSourceType === 'FILE')
                    $scope.fileDict[source.name] = i;
            }

            configureActiveStep();
        }

        function onBack(){
            $scope.direction = 0;
            $scope.activeStep--;
            configureActiveStep();
        }

        function onClose(transitionDuration){

            // perform unload actions
            onUnload();

            close(null, transitionDuration);
        }


        function onMessage(msg){

            if (angular.isDefined(callbacks[msg.id])){
                var deferred = callbacks[msg.id];
                delete callbacks[msg.id];

                deferred.resolve(msg);
            }
            else{
                toasterService.error("Web socket message without corresponding callback.",
                    "Web socket message without corresponding callback.",
                    toastContainerId);
            }
        }

        function onDescribe(describe){
            $scope.features = describe.describe.features;
            $scope.isEvaluating = false;
        }

        function onNext(){
            $scope.direction = 1;

            if ($scope.activeStep == 0){
                $scope.onLoad(function(){
                    $scope.activeStep++;
                    configureActiveStep();
                });
            }
            else{
                $scope.activeStep++;
                configureActiveStep();
            }
        }

        function onSave(transitionDuration){

            // perform unload actions
            onUnload();

            var data = getConfiguration();
            close(data, transitionDuration);
        }

        function onUnload(){

            $webSockets.unlisten("describe");
            $webSockets.unlisten("data");
        }

        //
        // public methods
        //

        $scope.addFiles = function(files){

            if (!$scope.onFileDropStart(files))
                return;

            for(var index = 0; index < files.length; index++){

                var file = files[index];
                diagramService.upload(file).then(
                    function(result){   // on success

                        $scope.onFileDrop(result);
                    },
                    function(message){     // on error

                        $scope.onFileDropError(message);
                    },
                    function(evt){      // on notification

                        if (evt.lengthComputable) {

                            var complete = (evt.loaded / evt.total * 100 | 0);
                            $scope.onFileDropNotification(file, complete);
                        }
                    }
                );
            }
        };

        $scope.onBrowse = function(evt){
            document.getElementById("fileBrowser").click();

            evt.stopPropagation();
            evt.preventDefault();
        };

        $scope.codemirrorLoaded = function(_editor){
            codeMirror = _editor;
        };

        $scope.onFetch = function(features, onSuccess){

            var deferred = $q.defer();

            var featureSet = features.map(function(feature){
                return { sourceName: feature.sourceName, name: feature.name };
            });

            var request = {
                diagramId: diagram.getId(),
                sessionId: analyticsService.getSessionId(),
                diagramName: diagram.getName(),
                targetEnvironment: diagram.getTargetEnvironment(),
                features: featureSet
            };

            diagramService.collect(request).then(
                function(guid){

                    callbacks[guid] = deferred;
                },
                function(data, status){

                    toasterService.error(data,
                        data,
                        toastContainerId);
                }
            );

            deferred.promise.then(

                function(data){

                    onSuccess(data);
                },
                function(data, status){

                    toasterService.error(data,
                        data,
                        toastContainerId);
                }
            );
        };

        $scope.onFileDrop = function(files, evt){

            for(var i = 0; i < files.length; i++) {

                var file = files[i];
                var fileIndex = $scope.fileDict[file.name];
                if (fileIndex !== undefined) {
                    var item = $scope.source.dataSources[fileIndex];
                    if(item !== undefined){
                        item.progress = 100;
                        item.contentType = file.contentType;
                        item.path = file.path;

                        // set the parse type
                        if (item.contentType.indexOf("spreadsheet") > -1)
                            $scope.parse.parseType = "SPREADSHEET";
                        else if(item.contentType.indexOf("tab-separated-values") > -1){
                            $scope.parse.parseType = "SEPARATED_VALUES";
                            $scope.parse.delimiterType = "TAB";
                        }
                    }
                }
            }
        };

        /**
         * Initialize file drop.  Verify the file is valid and not a duplicate
         * @param files: files to be dropped
         * @returns {boolean}: success
         */
        $scope.onFileDropStart = function(files){

            // temporarily only allow a single file to be dropped
            if (files.length > 1 || $scope.source.dataSources.length > 0) {

                toasterService.error("Only one file can be added at this time.",
                    "Only one file can be added at this time.",
                    toastContainerId);

                return false;
            }

            for(var i = 0; i < files.length; i++){

                var file = files[i];

                if($scope.fileDict[file.name] === undefined){

                    $scope.fileDict[file.name] = $scope.source.dataSources.length;
                    $scope.source.dataSources.push({
                        dataSourceType: 'FILE',
                        name: file.name,
                        progress: 0,
                        contentType: '',
                        path: ''
                    });
                }
                else{

                    toasterService.error("Duplicate file detected.",
                        "Duplicate file detected.",
                        toastContainerId);

                    return false;
                }
            }

            return true;
        };

        $scope.onFileDropNotification = function(file, complete){

            var fileIndex = $scope.fileDict[file.name];
            if(fileIndex !== undefined)
                $scope.source.dataSources[fileIndex].progress = complete;
        };

        $scope.onFileRemove = function(name){

            var fileIndex = $scope.fileDict[name];
            if(fileIndex !== undefined) {
                delete $scope.fileDict[name];
                $scope.source.dataSources.splice(fileIndex, 1);
            }
        };

        $scope.onLoad = function(onSuccess){

            $scope.isEvaluating = true;
            var data = getConfiguration();

            diagramService.load(data).then(
                function(result){

                    if(onSuccess)
                        onSuccess(result);
                },
                function(data, status){

                    $scope.isEvaluating = false;

                    toasterService.error(data,
                        data,
                        toastContainerId);
                }
            );
        };

        $scope.onResizeMenu = function(evt){

            var container = angular.element(document.querySelector('#load-data-wrapper')),
                containerWidth,
                originX;

            $scope.$root.$broadcast("beginDrag", {
                x: evt.pageX,
                y: evt.pageY,
                config: {

                    dragStarted: function (x, y) {

                        // create reference to dialog container
                        containerWidth = container.width();
                        originX = container.offset().left;
                    },

                    dragging: function (x, y) {

                        var width = (x - originX) * 100.0 / containerWidth;
                        if (width > 5 && width < 95)
                            $scope.menuWidth = width;
                    }
                }
            });
        };

        /**
         * Set the data's source type
         * @param sourceType: new data source type
         */
        $scope.setSourceType = function(sourceType){
            $scope.source.sourceType = sourceType;

            switch(sourceType) {
                case "HBASE":

                    // retrieve a new empty query object
                    diagramService.query().then(
                        function(data){
                            $scope.query = data;
                        },
                        function(data){
                            toasterService.error(data,
                                data,
                                toastContainerId);
                        }
                    );

                    break;
            }
        };

        /**
         * Close the transformation terminal
         */
        $scope.onTerminalClose = function(){
            $scope.isTerminalInputOpen = false;
            // hide the transformation terminal by setting its position to the height of this control
            var container = angular.element(document.querySelector('#load-data-wrapper'));
            $scope.terminalPosition = container.height();
        };

        /**
         * Open the transformation terminal
         */
        $scope.onTerminalOpen = function(){

            $scope.newTransformation = {
                id: analyticsService.guid(),
                transformationType: 'FILTER',
                source: ''
            };

            $scope.isTerminalInputOpen = true;
            $scope.terminalPosition = 0;
        };

        /**
         * Add the new transformation
         */
        $scope.onTransformationAdd = function(){

            $scope.transformations.push($scope.newTransformation);
            $scope.onLoad();
            $scope.onTerminalClose();
        };

        $scope.onQuery = function(evt){

            diagramService.query($scope.query).then(
                function(data){

                },
                function(data){
                    toasterService.error(data,
                        data,
                        toastContainerId);
                }
            );

            evt.stopPropagation();
            evt.preventDefault();
        };

        $scope.setParseType = function(parseType){
            $scope.parse.parseType = parseType;
        };

        $scope.setPreviewMode = function(mode){

            $scope.previewMode = mode;
        };



        init();
    }])

    /**
     * Create diagram dialog
     */
    .controller('createDiagramController', ['$scope', 'diagramProperties', 'close', function($scope, diagramProperties, close){

        // add properties to the scope so that they accessible to the popup
        $scope.diagramProperties = diagramProperties;

        // internal variables
        var delay = 0;

        $scope.onCancel = function(){
            close(null, delay);
        };

        $scope.onSave = function(){
            close($scope.diagramProperties, delay);
        };
    }])

    /**
     * Set Online Output dialog
     * Dialog to configure an online diagram's data output
     */
    .controller('setOutputController', ['$scope', '$element', '$q', '$webSockets', 'analyticsService', 'toasterService', 'diagramService', 'diagram', 'block', 'config', 'position', 'close', function($scope, $element, $q, $webSockets, analyticsService, toasterService, diagramService, diagram, block, config, position, close){

        // add the position and config variables to the current scope so that they can be accessed by modal
        $scope.position = position;
        $scope.config = config;
        $scope.config.icon = "fa-sign-out";
        $scope.config.title = "Streaming Output Block";
        $scope.config.subTitle = "Enable / Disable Online Streaming Outputs";
        $scope.config.showCancel = true;
        $scope.config.showSave = true;
        $scope.config.saveLabel = "Ok";

        // create modal method object
        $scope.methods = {
            close: onClose,
            save: onSave
        };

        // capture block's output configuration parameter
        $scope.outputConfig = block.parameters[0].value;

        // setup view variables
        $scope.menuWidth = 20;
        $scope.optionState = {
            disabled: 0,
            editing: 1,
            enabled: 2
        };

        // initialize list of output options
        $scope.outputOptions = [
            { consumerType: "LOG", name: "Log File", enabled: false, path: "", key: "", state: $scope.optionState.disabled },
            { consumerType: "DATABASE", name: "Database", enabled: false, path: "", key: "", state: $scope.optionState.disabled },
            { consumerType: "OPC", name: "OPC Tag", enabled: false, path: "", key: "", state: $scope.optionState.disabled },
            { consumerType: "PI", name: "PI Tag", enabled: false, path: "", key: "", state: $scope.optionState.disabled },
            { consumerType: "OUT", name: "Command Line", enabled: false, path: "", key: "", state: $scope.optionState.disabled }
        ];

        // initialize the active consumer
        $scope.activeConsumer = $scope.outputOptions[0];

        /* internal variables */
        var toastContainerId = "modal-toaster",
            startingConfig = angular.copy($scope.outputConfig),
            activeCopy = null;

        /* public methods */

        /**
         * Cancel editing the active consumer output
         * @param evt: click event
         */
        $scope.onOutputCancel = function(evt){
            $scope.activeConsumer.enabled = activeCopy.enabled;
            $scope.activeConsumer.path = activeCopy.path;
            $scope.activeConsumer.key = activeCopy.key;
            $scope.activeConsumer.state = activeCopy.state;
            $scope.config.saveDisabled = false; // enable modal save button
        };

        /**
         * Begin editing the active consumer output
         * @param evt: click event
         */
        $scope.onOutputEdit = function(evt){
            activeCopy = angular.copy($scope.activeConsumer);
            $scope.activeConsumer.state = $scope.optionState.editing;
            $scope.config.saveDisabled = true; // disable modal save button
        };

        /**
         * Save and complete edit of the active consumer output
         * @param evt: cleck event
         */
        $scope.onOutputSave = function(evt){

            if($scope.activeConsumer.enabled){

                // a consumer is being enabled - either add or edit the specified consumer type
                var consumer = $scope.outputConfig.consumers.find(function(option){ return option.consumerType === $scope.activeConsumer.consumerType; });
                if (consumer === undefined){ // the consumer does not exist - add
                    consumer = {
                        consumerType: $scope.activeConsumer.consumerType,
                        path: $scope.activeConsumer.path,
                        key: $scope.activeConsumer.key
                    };
                    $scope.outputConfig.consumers.push(consumer);
                }
                else{ // the consumer exists - edit
                    consumer.path = $scope.activeConsumer.path;
                    consumer.key = $scope.activeConsumer.key;
                }
                $scope.activeConsumer.state = $scope.optionState.enabled;
            }
            else{

                // a consumer is being removed - clear active fields and remove if necessary
                var index = $scope.outputConfig.consumers.findIndex(function(option){ return option.consumerType === $scope.activeConsumer.consumerType; });
                if (index > -1)
                    $scope.outputConfig.consumers.splice(index, 1);

                $scope.activeConsumer.path = "";
                $scope.activeConsumer.key = "";
                $scope.activeConsumer.state = $scope.optionState.disabled;
            }

            $scope.config.saveDisabled = false; // enable modal save button
        };

        /**
         * drag to resize the menu
         * @param evt
         */
        $scope.onResizeMenu = function(evt){

            var container = angular.element(document.querySelector('#modal-content-container')),
                containerWidth,
                originX;

            $scope.$root.$broadcast("beginDrag", {
                x: evt.pageX,
                y: evt.pageY,
                config: {

                    dragStarted: function (x, y) {

                        // create reference to dialog container
                        containerWidth = container.width();
                        originX = container.offset().left;
                    },

                    dragging: function (x, y) {

                        var width = (x - originX) * 100.0 / containerWidth;
                        if (width > 5 && width < 95)
                            $scope.menuWidth = width;
                    }
                }
            });
        };

        /**
         * validate and set the active consumer type
         * @param consumerType
         */
        $scope.setActiveConsumerType = function(consumer){

            // todo: verify that it is ok to leave the current consumer
            if ($scope.activeConsumer !== null){

            }

            $scope.activeConsumer = consumer;
        };

        /* private methods */

        /**
         * Cancel configuration
         * @param transitionDuration
         */
        function onClose(transitionDuration){

            // revert back to original configuration
            block.parameters[0].value = startingConfig;

            // execute the close event
            close(null, transitionDuration);
        }

        /**
         * Initialize this controller
         */
        function onInit(){

            // set the state of each output option type
            $scope.outputConfig.consumers.forEach(function(output) {
                var outputOption = $scope.outputOptions.find(function(option){ return option.consumerType === output.consumerType; })
                if (outputOption !== undefined)
                    outputOption.state = $scope.optionState.enabled;
            });
        }

        /**
         * End configuation
         * @param transitionDuration
         */
        function onSave(transitionDuration){

            // execute the close event
            close(null, transitionDuration);
        }

        onInit();
    }])

    /**
     * Set Data Stream dialog
     * Dialog to configure an online diagram's data stream
     */
    .controller('setStreamController', ['$scope', '$element', '$q', '$webSockets', 'analyticsService', 'toasterService', 'diagramService', 'diagram', 'block', 'config', 'position', 'close', function($scope, $element, $q, $webSockets, analyticsService, toasterService, diagramService, diagram, block, config, position, close){

        // add the position and config variables to the current scope so that they can be accessed by modal
        $scope.position = position;
        $scope.config = config;
        $scope.config.icon = "fa-rss";
        $scope.config.title = "Configure";
        $scope.config.subTitle = "Streaming Source";
        $scope.config.showCancel = true;
        $scope.config.showSave = true;
        $scope.config.saveLabel = "Ok";

        // create modal method object
        $scope.methods = {
            close: onClose,
            save: onSave
        };

        $scope.dataStreamTypes = [
            { name: "OPC", value: "OPC" },
            { name: "OSI PI", value: "PI" },
            { name: "Simulated", value: "Simulated" }
        ];

        $scope.configStates = {
            configuring: 0,
            configured: 1,
            creating: 2
        };

        // setup view
        $scope.menuWidth = 20;
        $scope.isWorking = false;
        $scope.loadingDataStreams = false;
        $scope.configState = $scope.configStates.configuring;
        $scope.streams = { selected: {} };
        $scope.dataStreams = [];

        // capture block's stream configuration parameter
        $scope.streamConfig = block.parameters[0].value;

        /* internal variables */
        var toastContainerId = "load-data-toaster",
            defaultTopicName = "New Stream",
            startingConfig = angular.copy($scope.streamConfig),
            lastDataSource = {};

        // use the configState flag to control the save button
        $scope.$watch("configState", function(newValue, oldValue){
            $scope.config.saveDisabled = (newValue!=$scope.configStates.configured);
        });

        /* public methods */

        /**
         * initiate upload of specified files
         * @param files: set of files to be uploaded
         */
        $scope.addFiles = function(files){

            if (!$scope.onFileDropStart(files))
                return;

            for(var index = 0; index < files.length; index++){

                var file = files[index];
                diagramService.upload(file).then(
                    function(result){   // on success

                        $scope.onFileDrop(result);
                    },
                    function(message){     // on error

                        $scope.onFileDropError(message);
                    },
                    function(evt){      // on notification

                        if (evt.lengthComputable) {

                            var complete = (evt.loaded / evt.total * 100 | 0);
                            $scope.onFileDropNotification(file, complete);
                        }
                    }
                );
            }
        };

        /**
         * Browse local file system
         * @param evt
         */
        $scope.onBrowse = function(evt){
            document.getElementById("fileBrowser").click();

            evt.stopPropagation();
            evt.preventDefault();
        };

        /**
         * clear the configured file
         * @param evt
         */
        $scope.onClearFile = function(evt){
            resetDataSource();
            setConfigState();

            evt.stopPropagation();
            evt.preventDefault();
        };

        /**
         * begin data stream creation
         * @param evt
         */
        $scope.onCreateStreamBegin = function(evt){

            if($scope.configState == $scope.configStates.creating)
                return;

            $scope.newStream = { topic: defaultTopicName, sourceType: "OPC", path: "", frequency: 1, keys: "", started: null };
            $scope.dataStreams.push($scope.newStream);

            $scope.streams.selected = $scope.newStream;
            $scope.configState = $scope.configStates.creating;

            evt.stopPropagation();
            evt.preventDefault();
        };

        /**
         * cancel creation of the new data stream
         * @param evt
         */
        $scope.onCreateStreamCancel = function(evt){

            // remove the new stream from the list of data streams
            var index = $scope.dataStreams.indexOf($scope.newStream);
            if (index > -1)
                $scope.dataStreams.splice(index, 1);

            // delete the new stream
            delete $scope.newStream;

            // reset the data source
            resetDataSource();

            setConfigState();

            evt.stopPropagation();
            evt.preventDefault();
        };

        /**
         * Complete stream creation
         * @param evt: click event
         */
        $scope.onCreateStreamComplete = function(evt){

            if ($scope.newStream.topic==defaultTopicName){
                toasterService.error("Invalid topic.",
                    "Invalid topic.",
                    toastContainerId);
                return;
            }

            var distinctCount = $scope.dataStreams.filter(function(item) { return item.topic == $scope.newStream.topic; })
            if (distinctCount > 1){
                toasterService.error("Topic already exists.",
                    "Topic already exists.",
                    toastContainerId);
                return;
            }

            if($scope.newStream.keys.length == 0){
                toasterService.error("Specify a valid set of tags.",
                    "Specify a valid set of tags.",
                    toastContainerId);
                return;
            }

            // set datasource name to new topic name
            $scope.streams.selected = $scope.newStream;
            $scope.streamConfig.dataSource.name = $scope.streams.selected.topic;

            // create request
            var keys = $scope.newStream.keys.split(",").map(function(key) { return key.trim(); });
            var request = {
                topic: $scope.newStream.topic,
                sourceType: $scope.newStream.sourceType,
                path: $scope.newStream.path,
                frequency: $scope.newStream.frequency,
                keys: keys
            };

            diagramService.streamingStart(request).then(
                function(taskId){
                    delete $scope.newStream;
                    setConfigState();
                },
                function(reason){
                    toasterService.error(reason,
                        reason,
                        toastContainerId);
                });

            evt.stopPropagation();
            evt.preventDefault();
        };

        /**
         * Delete stream
         * @param evt: click event
         */
        $scope.onDeleteStream = function(evt){

            // capture selected topic
            var selectedTopic = $scope.streams.selected.topic;

            // get the index of the selected stream
            var index = $scope.dataStreams.indexOf($scope.streams.selected);
            //
            $scope.streams.selected = {};
            // remove the selected stream from the list of data streams
            if (index > -1)
                $scope.dataStreams.splice(index, 1);

            // reset the data source
            resetDataSource();

            // set configuration state
            setConfigState();

            // make a request to terminate data stream
            diagramService.streamingStop(selectedTopic).then(null,
                function(reason){
                    toasterService.error(reason,
                        reason,
                        toastContainerId);
                });

            evt.stopPropagation();
            evt.preventDefault();
        };

        /**
         * Executed on completion of successful file drop
         * @param files: files successfully dropped
         * @param evt
         */
        $scope.onFileDrop = function(files, evt){
            var file = files[0];
            var item = $scope.streamConfig.dataSource;
            item.progress = 100;
            item.contentType = file.contentType;
            item.path = file.path;

            $scope.isWorking = false;
            setConfigState();
        };

        /**
         * Initialize file drop.  Verify the file is valid and not a duplicate
         * @param files: files to be dropped
         * @returns {boolean}: success
         */
        $scope.onFileDropStart = function(files){

            // only allow a single file to be dropped
            if (files.length > 1) {
                toasterService.error("Only one file can be added at this time.",
                    "Only one file can be added at this time.",
                    toastContainerId);

                return false;
            }

            var file = files[0];

            if($scope.streamConfig.dataSource.name != file.name){

                $scope.streamConfig.dataSource = {
                        dataSourceType: 'FILE',
                        name: file.name,
                        progress: 0,
                        contentType: '',
                        path: ''
                    };

                $scope.isWorking = true;
            }
            else{

                toasterService.error("Duplicate file detected.",
                    "Duplicate file detected.",
                    toastContainerId);

                return false;
            }

            return true;
        };

        /**
         * Update file drop progress
         * @param file: the file being dropped
         * @param complete: % complete
         */
        $scope.onFileDropNotification = function(file, complete){
            $scope.streamConfig.dataSource.progress = complete;
        };

        /**
         * Set the stream source type (cache current setting, restore last)
         * @param sourceType: new stream source type
         */
        $scope.setSourceType = function(sourceType){

            // capture last configuration
            lastDataSource[$scope.streamConfig.sourceType] = $scope.streamConfig.dataSource;

            // set new data source
            $scope.streamConfig.sourceType = sourceType;

            if(lastDataSource[sourceType] !== undefined && lastDataSource[sourceType] !== null)
                $scope.streamConfig.dataSource = lastDataSource[sourceType];
            else
                resetDataSource();

            setConfigState();
        };

        /**
         * Set data stream
         */
        $scope.onSetStream = function(){
            $scope.streamConfig.dataSource.name = $scope.streams.selected.topic;
            setConfigState();
        };

        /* private methods */

        /**
         * initialize this view
         */
        function init(){

            // listen for streaming task updates
            $webSockets.listen("streaming-tasks", function (msg) {
                return msg.messageType == "streaming-tasks";
            }, onStreamingSummary);

            loadStreams();
            setConfigState();
        }

        /**
         * initiate loading a data stream
         */
        function loadStreams(){

            // request a list of active data streams
            var request = { sessionId: analyticsService.getSessionId() };
            diagramService.streamingSummary(request).then(
                function(taskId){
                    $scope.loadingDataStreams = true;
                },
                function(reason){
                    toasterService.error(reason,
                        reason,
                        toastContainerId);
                }
            );
        }

        /**
         * Determine configuration state
         */
        function setConfigState(){

            // abort if currently working
            if($scope.isWorking)
                return;

            switch($scope.streamConfig.sourceType){
                case "FILES":
                    if($scope.streamConfig.dataSource.progress===100)
                        $scope.configState = $scope.configStates.configured;
                    else
                        $scope.configState = $scope.configStates.configuring;
                    break;
                case "STREAM":
                    if($scope.streamConfig.dataSource.name!==null && $scope.streamConfig.dataSource.name.length > 0)
                        $scope.configState = $scope.configStates.configured;
                    else
                        $scope.configState = $scope.configStates.configuring;
                    break;
            }
        }

        /**
         * retrieve an empty stream template
         * @returns {{topic: string, sourceType: string, path: string, frequency: number, keys: string, started: null}}
         */
        function getEmptyStream(){
            return { topic: defaultTopicName, sourceType: "OPC", path: "", frequency: 1, keys: "", started: null };
        }

        /**
         * reset the stream configuration data source
         */
        function resetDataSource(){
            $scope.streamConfig.dataSource = { name: null, progress: 0, contentType: null, path: null };
        }

        /**
         * Cancel configuration
         * @param transitionDuration
         */
        function onClose(transitionDuration){

            // revert back to original configuration
            block.parameters[0].value = startingConfig;

            // execute the close event
            close(null, transitionDuration);
        }

        /**
         * End configuation
         * @param transitionDuration
         */
        function onSave(transitionDuration){

            // capture new stream configuration
            block.parameters[0].value = $scope.streamConfig;

            // execute the close event
            close(null, transitionDuration);
        }

        /**
         * handle streaming summary messages
         * @param message
         */
        function onStreamingSummary(message){

            // transform into streams representation
            var temp = message.items.map(function(item){
                return {
                    topic: item.topic,
                    sourceType: item.sourceType,
                    path: item.path,
                    frequency: item.frequency,
                    keys: item.keys.join(", "),
                    started: item.started
                };
            });

            $scope.dataStreams = temp;

            var selectedStream = $scope.dataStreams.find(function(item){ return item.topic == $scope.streamConfig.dataSource.name; });
            if(selectedStream)
                $scope.streams.selected = selectedStream;

            $scope.loadingDataStreams = false;
        }

        // call initialization
        init();
    }])

    /**
     * Studio terminal controller
     */
    .controller('terminalController', [ '$scope', '$timeout',
        function($scope, $timeout){

            // internal variables
            var terminalWrapper = angular.element(document.querySelector('#terminal')), // reference to the terminal div
                lastInputHeight = 35, // initialize the variable that tracks the last input height
                editor; // reference to the code mirror editor

            // flag to track whether the terminal input is open
            $scope.isTerminalInputOpen = false;
            // current terminal input height
            $scope.inputHeight = 0;
            // flag to track whether the terminal input is being resized (by dragging)
            $scope.isResizing = false;

            // code mirror options
            $scope.options = { mode: 'python' };

            //
            // internal methods
            //

            /**
             * opens and closes the terminal input control
             */
            function toggleTerminalInput(){
                $scope.isTerminalInputOpen = !$scope.isTerminalInputOpen;
                if ($scope.isTerminalInputOpen){
                    $scope.inputHeight = lastInputHeight;
                    editor.focus();
                }
                else{
                    lastInputHeight = $scope.inputHeight;
                    $scope.inputHeight = 0;
                }
            }

            //
            // public methods
            //

            /**
             * Called when code mirror control is loaded
             * @param _editor: reference to the code mirror editor
             */
            $scope.codemirrorLoaded = function(_editor){
                editor = _editor;
            };

            /**
             * Cancel - close the terminal input and clear the source code
             */
            $scope.onCancel = function(){
                toggleTerminalInput();
                $scope.source = "";
            };

            /**
             * Resizes the terminal input and output controls
             * @param evt
             */
            $scope.onResize = function(evt){

                var containerHeight,
                    originY;

                $scope.$root.$broadcast("beginDrag", {
                    x: evt.pageX,
                    y: evt.pageY,
                    config: {

                        dragStarted: function (x, y) {

                            $scope.isResizing = true;
                            containerHeight = terminalWrapper.height();
                            originY = terminalWrapper.offset().top;

                            $scope.$$phase || $scope.$apply();
                        },

                        dragging: function (x, y) {

                            var height = 100 - (y - originY) * 100.0 / containerHeight;
                            if (height > 5 && height < 95)
                                $scope.inputHeight = height;
                        },

                        dragEnded: function(){
                            $scope.isResizing = false;

                            $scope.$$phase || $scope.$apply();
                        }
                    }
                });
            };

            /**
             * Send the source code to the interpreter
             */
            $scope.onSend = function(){

                $scope.interpret(
                    $scope.source,
                    function(){     // on success
                        $scope.source = "";
                    },
                    function(){     // on failure

                    });
            };

            /**
             * Toggle the terminal input controls
             */
            $scope.onToggle = function(){
                toggleTerminalInput();
            };
    }]);