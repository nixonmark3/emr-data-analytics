'use strict';

analyticsApp

    .controller('blockDataController', ['$scope', '$element', '$window', '$timeout', '$q', 'diagramService', 'block', 'loadSources', 'diagram', 'config', 'position', 'close', function($scope, $element, $window, $timeout, $q, diagramService, block, loadSources, diagram, config, position, close){

        $scope.position = position;
        $scope.block = block.block;
        $scope.config = config;
        $scope.config.icon = "fa-cube";
        $scope.config.cancelLabel = "Close";
        $scope.pages = [];
        $scope.currentPage = null;
        $scope.activePage = 0;
        $scope.blockProperties = block;
        $scope.loadData = loadSources;
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

    .controller('debugController', ['$scope', '$element', '$timeout', 'diagramService', 'data', 'config', 'position', 'close',
        function($scope, $element, $timeout, diagramService, data, config, position, close){

            $scope.config = config;
            $scope.position = position;

            diagramService.compile(data).then(
                function (source) {
                    $scope.editor = { data: source };
                },
                function (code) {
                    console.log(code); // TODO show exception
                }
            );

            $scope.close = function(transitionDelay){

                close(null, transitionDelay);
            };
        }
    ])

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

    .controller('exploreController', ['$scope', '$element', '$timeout', '$q', '$animate', 'diagramService', 'colorService', 'block', 'config', 'position', 'close',
        function($scope, $element, $timeout, $q, $animate, diagramService, colorService, block, config, position, close){

            $scope.block = block;
            $scope.config = config;
            $scope.position = position;
            $scope.loading = true;
            $scope.fetching = false;
            $scope.rendering = false;
            $scope.activeIndex = 0;
            $scope.chartMethods = {};
            $scope.hideChartMenus = false;
            $scope.rangeSliderDisabled = false;

            $scope.config.subTitle = "Explore";

            $scope.methods = {
                close: onClose
            };

            // load the set of features after the modal animation has completed
            $timeout(function() {

                diagramService.getFeatures($scope.block.id()).then(

                    function (data) {

                        $scope.features = data;
                        $scope.loading = false;
                    },
                    function (code) {

                        $scope.loading = false;
                    }
                );
            }, 400);

            // initialize the chart object
            $scope.chartOptions = {
                type: "line",
                scaled: false,
                override: false, 
                colorSet: "default",
                x: { min: null, max: null, minActual: null, maxActual: null },
                y: { min: null, max: null, minActual: null, maxActual: null },
                series: []
            };

            // initialize new series object
            $scope.newSeries = {
                x: {
                    name: null, min: null, max: null
                },
                y: {
                    name: null, min: null, max: null
                },
                flipToFront: false,
                flipToBack: false
            };

            $scope.$watch(
                function() {
                    return $scope.chartOptions.y.minActual;
                },
                function() {
                    if($scope.loading || $scope.fetching) {

                        return;
                    }

                    if ($scope.chartOptions.y.minActual != null)
                        $scope.render();
                }
            );

            $scope.$watch(
                function() {
                    return $scope.chartOptions.y.maxActual;
                },
                function() {
                    if($scope.loading || $scope.fetching) {

                        return;
                    }

                    if ($scope.chartOptions.y.maxActual != null)
                        $scope.render();
                }
            );

            /* general methods */

            function onClose(transitionDelay) {

                close(null, transitionDelay);
            }

            $scope.setActiveIndex = function(index) {

                $scope.activeIndex = index;
                $scope.hideChartMenus = false;
            };

            /* chart methods */

            $scope.showClose = function(series) {

                series.flipToBack = true;
                series.flipToFront = false;
            };

            $scope.showCheck = function(series) {

                series.flipToBack = false;
                series.flipToFront = true;
            };

            $scope.collapseChartMenusClick = function() {

                $scope.hideChartMenus = true;
            };

            $scope.addAllSeries = function() {

                if ($scope.chartOptions.type == 'line') {

                    resetSeries();

                    var currentConfiguredSeries = getCurrentConfiguredSeries();

                    for (var featureIndex in $scope.features) {

                        if (featureIndex > 0) {

                            var seriesName = $scope.features[featureIndex].column;

                            if (currentConfiguredSeries.indexOf(seriesName) == -1) {

                                var seriesToAdd = angular.copy($scope.newSeries);

                                var feature = $scope.features[featureIndex];
                                seriesToAdd.y.name = feature.column;
                                seriesToAdd.y.min = feature.min;
                                seriesToAdd.y.max = feature.max;

                                $scope.chartOptions.series.push(seriesToAdd);
                            }
                        }
                    }

                    updateChartBounds();

                    $scope.fetchData();
                }
            };

            var getCurrentConfiguredSeries = function() {

                var configuredSeries = [];

                for (var series in $scope.chartOptions.series) {

                    configuredSeries.push($scope.chartOptions.series[series].y.name);
                }

                return configuredSeries;

            };

            $scope.removeAllSeries = function() {

                $scope.chartOptions.series.length = 0;

                updateChartBounds();

                $scope.render();
            };

            $scope.removeSeries = function(series) {

                $scope.chartOptions.series.splice($scope.chartOptions.series.indexOf(series), 1);

                updateChartBounds();

                $scope.render();
            };

            $scope.addSeries = function() {

                if ($scope.newSeries.y.name == null) return;

                if ($scope.hasXCoordinate() && $scope.newSeries.x.name == null) return;

                var seriesToAdd = angular.copy($scope.newSeries);

                $scope.chartOptions.series.push(seriesToAdd);

                updateChartBounds();

                resetSeries();

                $scope.fetchData();
            };

            /**
             *  Configure
             */
            $scope.selectFeature = function(type){

                var dimension;
                if (type == 'y')
                    dimension = $scope.newSeries.y;
                else if (type == 'x')
                    dimension = $scope.newSeries.x;

                var feature = getFeatureStatistics(dimension.name);
                dimension.min = feature.min;
                dimension.max = feature.max;
            };

            // retrieve a color by configured color set and index
            $scope.getColor = function(index) {

                return colorService.getColor($scope.chartOptions.colorSet, index);
            };

            // determines whether currently selected chart type has a configurable x coordinate
            $scope.hasXCoordinate = function() {

                return ($scope.chartOptions.type == 'scatter');
            };

            $scope.onPage = function(){

                var deferred = $q.defer();

                diagramService.getFeatureGridData($scope.block.id()).then(
                    function (data) {

                        deferred.resolve(data);
                    },
                    function (code) {

                        deferred.reject(code);
                    }
                );

                return deferred.promise;
            };

            $scope.toggleScale = function(){

                if($scope.chartOptions.scaled)
                    $scope.rangeSliderDisabled = true;
                else
                    $scope.rangeSliderDisabled = false;

                $scope.render();
            };

            // fetch data and render the chart
            $scope.fetchData = function() {

                $scope.fetching = true;

                // assemble a distinct list of features
                // todo: to maintain a dictionary of features as series and added and removed
                var features = [];
                for(var i = 0; i < $scope.chartOptions.series.length; i++){

                    var series = $scope.chartOptions.series[i];
                    if (series.x.name != null && features.indexOf(series.x.name) == -1)
                        features.push(series.x.name);
                    if (features.indexOf(series.y.name) == -1)
                        features.push(series.y.name);
                }

                diagramService.getChartData($scope.block.id(), features).then(
                    function (data) {

                        $scope.fetching = false;

                        $scope.render(data);
                    },
                    function (code) {

                        $scope.rendering = false;
                    }
                );
            };

            $scope.render = function(data){

                $scope.rendering = true;
                $scope.chartMethods.render($scope.chartOptions, data);
                $scope.rendering = false;
            };

            function resetSeries() {

                $scope.newSeries.y = {
                    name: null,
                    min: null,
                    max: null
                };
                $scope.newSeries.x = {
                    name: null,
                    min: null,
                    max: null
                };
                $scope.newSeries.flipToFront = false;
                $scope.newSeries.flipToBack = false;
            }

            function updateChartBounds() {

                var count = 0;
                var xMin = 0;
                var xMax = 0;
                var yMin = 0;
                var yMax = 0;

                for (var seriesIndex in $scope.chartOptions.series) {

                    var yFeature = getFeatureStatistics($scope.chartOptions.series[seriesIndex].y.name);
                    var xFeature = ($scope.chartOptions.series[seriesIndex].x.name) ? getFeatureStatistics($scope.chartOptions.series[seriesIndex].x.name) : null;

                    var yFeatureMin = Number(yFeature.min);
                    var yFeatureMax = Number(yFeature.max);

                    if (xFeature != null) {

                        var xFeatureMin = Number(xFeature.min);
                        var xFeatureMax = Number(xFeature.max);
                    }

                    var yFeatureCount = Number(yFeature.count);

                    if (count === 0) {

                        count = 1;

                        yMin = yFeatureMin;
                        yMax = yFeatureMax;

                        if (xFeature != null) {

                            xMin = xFeatureMin;
                            xMax = xFeatureMax;
                        }
                        else {

                            xMin = 0;
                            xMax = yFeatureCount;
                        }
                    }
                    else {

                        if (yFeatureMin < yMin) {

                            yMin = yFeatureMin;
                        }

                        if (yFeatureMax > yMax) {

                            yMax = yFeatureMax;
                        }

                        if (xFeature != null) {

                            if (xFeatureMin < xMin) {

                                xMin = xFeatureMin;
                            }

                            if (xFeatureMax > xMax) {

                                xMax = xFeatureMax;
                            }
                        }
                        else {

                            if (xMin > 0) {

                                xMin = 0;
                            }

                            if (yFeatureCount > xMax) {

                                xMax = yFeatureCount;
                            }
                        }
                    }
                }

                $scope.chartOptions.y.min = yMin;
                $scope.chartOptions.y.max = yMax;
                $scope.chartOptions.x.min = xMin;
                $scope.chartOptions.x.max = xMax;
            }

            function getFeatureStatistics(selectedFeature) {

                var statistics = null;

                for (var i = 0; i < $scope.features.length; i++){

                    var feature = $scope.features[i];
                    if (feature.column === selectedFeature) {
                        statistics =  feature.statistics;
                        break;
                    }
                }

                return statistics;
            }

            $scope.updateChartType = function(){

                $scope.chartOptions.x = { min: null, max: null, minActual: null, maxActual: null };
                $scope.chartOptions.y = { min: null, max: null, minActual: null, maxActual: null };
                $scope.chartOptions.series = [];

                resetSeries();
            };
        }
    ])

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

    .controller('loadDataController', ['$scope', '$element', '$webSockets', 'toasterService', 'diagramService', 'diagramId', 'diagramName', 'config', 'position', 'close', function($scope, $element, $webSockets, toasterService, diagramService, diagramId, diagramName, config, position, close){



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
        $scope.menuWidth = 20;
        $scope.chartMethods = {};

        $webSockets.listen("describe", function(msg){ return msg.messageType == "describe"; }, onDescribe);

        // a dictionary of file names and their associated index numbers
        $scope.fileDict = {};

        $scope.source = {
            sourceType: 'FILES',
            dataSources: []
        };

        $scope.parse = {
            parseType: 'SEPARATED_VALUES',
            header: true,
            delimiterType: 'COMMA',
            delimiterChar: '',
            missingValueMode: 'PERMISSIVE',
            quoteChar: '"',
            commentChar: '#'
        };

        $scope.transformations = [];

        $scope.progressSteps = [
            { name: 'source', icon: 'fa-database' },
            { name: 'parse', icon: 'fa-code' },
            { name: 'clean', icon: 'fa-paint-brush' }
        ];

        var toastContainerId = "load-data-toaster";

        // internal methods

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
                    break;
            }
        }

        function onBack(){
            $scope.direction = 0;
            $scope.activeStep--;
            configureActiveStep();
        }

        function onClose(transitionDuration){

            $webSockets.unlisten("describe");
            close(null, transitionDuration);
        }

        function onDescribe(describe){
            $scope.features = describe.describe.features;
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
            close(null, transitionDuration);
        }

        // public methods

        $scope.addFiles = function(files){

            $scope.onFileDropStart(files);

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

        $scope.onFetch = function(featureNames, onSuccess){

            var request = {
                diagramId: diagramId,
                diagramName: diagramName,
                dataFrameName: 'load_out',
                features: featureNames
            };

            diagramService.collect(request).then(
                function(result){
                    onSuccess(result);
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
                        item.path = file.path;
                    }
                }
            }
        };

        $scope.onFileDropStart = function(files){

            for(var i = 0; i < files.length; i++){

                var file = files[i];

                if($scope.fileDict[file.name] === undefined){

                    $scope.fileDict[file.name] = $scope.source.dataSources.length;
                    $scope.source.dataSources.push({
                        dataSourceType: 'FILE',
                        name: file.name,
                        progress: 0,
                        path: ''
                    });
                }
                else{

                    // todo: notify user file already exists
                }
            }
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

            var data = {
                diagramId: diagramId,
                diagramName: diagramName,
                source: $scope.source,
                parse: $scope.parse,
                transformations: $scope.transformations
            };

            diagramService.load(data).then(
                function(result){

                    onSuccess(result);
                },
                function(data, status){

                    toasterService.error(data,
                        data,
                        toastContainerId);
                }
            );
        };

        $scope.onResizeMenu = function(evt){

            var containerWidth,
                originX;

            $scope.$root.$broadcast("beginDrag", {
                x: evt.pageX,
                y: evt.pageY,
                config: {

                    dragStarted: function (x, y) {

                        // create reference to dialog container
                        var container = angular.element(document.querySelector('#load-data-wrapper'));
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

        $scope.setSourceType = function(sourceType){
            $scope.source.sourceType = sourceType;
        };

        $scope.setParseType = function(parseType){
            $scope.parse.parseType = parseType;
        };

        $scope.setPreviewMode = function(mode){

            $scope.previewMode = mode;
        };

        configureActiveStep();
    }])

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