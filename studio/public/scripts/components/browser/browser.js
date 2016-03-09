'use strict';

angular.module('browserApp', ['ngAnimate', 'emr.ui.interact'])

    .directive('browser', ['$document', '$compile', function ($document, $compile) {

        return {
            restrict: 'E',
            replace: false,
            templateUrl: '/assets/scripts/components/browser/browser.html',
            scope: {
                nodes: "=",
                onDrag: "=",
                onDrop: "=",
                getConfigBlock: "=",
                loadSources: "=",
                output: "="
            },
            link: function($scope, element, attrs){

                var body = $document.find('body');

                $scope.pageIndex = 0;

                $scope.savable = false;

                $scope.pageHeader = [];

                $scope.pages = [{
                    "categories" : $scope.nodes,
                    "definitions" : [],
                    "definition" : null
                }];

                //
                // The definition is only configurable if a load-definition function has
                // been specified.
                //
                $scope.isConfigurable = function(){
                    return ($scope.onDrop===undefined);
                };

                // set page index
                $scope.setPage = function(index){
                    $scope.pageIndex = index;
                };

                // identify whether index is current page
                $scope.isCurrentPage = function(index){
                    return $scope.pageIndex === index;
                };

                // page the navigation panel back
                $scope.pageBack = function(){

                    $scope.pages.splice($scope.pageIndex);
                    $scope.pageIndex = $scope.pageIndex - 1;
                    $scope.pageHeader.splice($scope.pageIndex);
                    $scope.savable = false;
                };

                // page the navigation panel forward
                $scope.pageForward = function(item){

                    $scope.pageHeader.push(item.name);

                    var index = $scope.pageIndex + 1;
                    $scope.pages.push({
                            "categories" : item.categories,
                            "definitions" : item.definitions,
                            "definition" : null
                        }
                    );

                    $scope.pageIndex = index;

                    $scope.savable = false;
                };

                // display a definition for configuration
                $scope.show = function(item){

                    if($scope.isConfigurable()) {

                        $scope.pageHeader.push(item.name);

                        var index = $scope.pageIndex + 1;

                        $scope.output = $scope.getConfigBlock(0, 0, item.name);

                        $scope.pages.push({
                                "categories": [],
                                "definitions": [],
                                "definition": $scope.output
                            });

                        $scope.pageIndex = index;

                        $scope.savable = true;
                    }
                };

                $scope.mouseDown = function(evt, item){

                    if($scope.onDrop) {

                        $scope.onDrag(evt.pageX, evt.pageY, {

                            dragStarted: function (x, y) {

                                // create the element that represents the dragging definition
                                var ghost = angular.element('<div id="ghost" ' +
                                    'ng-style="{top: ghostPosition.y, ' +
                                    'left: ghostPosition.x, ' +
                                    'width: 200, ' +
                                    'height: 80}"></div>');

                                // compile and append to the parent
                                $compile(ghost)($scope);
                                body.append(ghost);
                            },

                            dragging: function (x, y) {

                                // update the ghost position
                                $scope.ghostPosition = {x: x, y: y};
                            },

                            dragEnded: function (x, y, evt) {

                                $scope.onDrop(x, y, evt, item.name);

                                delete $scope.ghostPosition;

                                $scope.$apply(function () {
                                    angular.element("#ghost").remove();
                                });
                            }
                        });

                        evt.stopPropagation();
                        evt.preventDefault();
                    }
                };
            }
        }
    }])

    .directive('blockConfig', ['modalService', function (modalService) {

        /**
         * interface used to configure block parameters
         */

        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/assets/scripts/components/browser/blockConfig.html',
            scope: {
                block: "=",
                diagram: "=",
                onCancel: "=",
                onChange: "=",
                onLoadParameter: "=",
                onPropsChanged: "=",
                onSave: "="
            },
            link: function ($scope, element, attrs) {

                var dependents = {},            // dictionary of dependents for reverse lookup
                    dynamicSourceQueue = [];    // list of dynamic sources to be resolved

                /**
                 * initialize parameter configuration interface
                 */
                function init(){

                    // build a list of parameters with dynamic sources
                    $scope.block.parameters.forEach(function(parameter){

                        if (parameter.source()) { // parameter has a dynamic source

                            // track whether all of the dependencies have been collected for this parameter
                            var dependenciesCollected = true;

                            // resolve arguments and identify dependents
                            parameter.source().arguments.forEach(function (argument) {

                                dependenciesCollected = setArgument(argument);

                                // capture parameter dependencies
                                var parts = argument.path.split(":");
                                if (parts[0] === "parameter"){

                                    if (!(dependencyName in dependents))
                                        dependents[dependencyName] = [];

                                    dependents[dependencyName].push(parameter.name());
                                }
                            });

                            // if the parameter is not loaded and its dependencies have been collected -
                            // flag as loading and add to the list of dynamic parameters

                            if (!parameter.loaded && dependenciesCollected) {
                                parameter.loading = true;
                                dynamicSourceQueue.push(parameter.data);
                            }
                        }
                    });

                    loadDynamicSource();
                }

                /**
                 * load the the next dynamic source
                 */
                function loadDynamicSource(){

                    if (dynamicSourceQueue.length == 0)
                        return;

                    // pull the next parameter off of the queue
                    var dynamicSourceRequest = dynamicSourceQueue.shift();
                    $scope.onLoadParameter(dynamicSourceRequest)
                        .then(function(result){

                            var parameter = getParameter(dynamicSourceRequest.name);
                            parameter.fieldOptions = result;

                            // flag the parameter as loaded
                            parameter.loaded = true;
                            parameter.loading = false;

                            // recursively
                            loadDynamicSource();
                        },
                        function(){
                            // todo: handle exception
                        });
                }

                /**
                 * Find parameter by name
                 * @param name: name of the parameter
                 * @returns Parameter
                 */
                var getParameter = function(name) {

                    var parameters = $scope.block.parameters;
                    for (var i = 0; i < parameters.length; i++){
                        if (parameters[i].name() == name)
                            return parameters[i];
                    }
                };

                /**
                 * Resolves parameter arguments
                 * @param argument: parameter argument { name, path, value }
                 * @returns {boolean}: represents whether a parameter has been resolved
                 */
                function setArgument(argument){

                    // verify the argument has not already been set
                    if (argument.value !== null)
                        return true;

                    var argumentSet = false;

                    var parts = argument.path.split(":");
                    switch(parts[0]){

                        case "block": // block property reference



                            break;
                        case "input": // input wire reference

                            // query the diagram for a list of input wires
                            var wires = $scope.diagram.findInputWires($scope.block.id, parts[1]);
                            if (wires.length > 0) { // if at least 1 wire was found

                                // only supports a single wire
                                var wire = wires[0];

                                // retrieve the block's name
                                var blockName = $scope.diagram.findBlock(wire["from_node"]).name();

                                argument.value = blockName + "/" + wire["from_connector"];
                                argumentSet = true;
                            }

                            break;
                        case "parameter": // parameter reference

                            var parameter = parts[1].split(".");
                            var dependency = getParameter(parameter[0]);
                            if (dependency.collected) {
                                argument.value = dependency.value;
                                argumentSet = true;
                            }

                            break;

                    }

                    return argumentSet;
                }

                /**
                 * on parameter value update; flag as collected and check for new dependents to resolve
                 * @param parameter
                 */
                $scope.setValue = function(parameter) {

                    /**
                    parameter.dirty = true;
                    parameter.collected = true;

                    // check for dependents
                    if (parameter.name() in $scope.dependants){

                        var dependants = $scope.dependants[parameter.name()];

                        var dynamicSourceRequests = {
                            name: $scope.block.id,
                            parameters: [],
                            diagram: null
                        };
                        dependants.forEach(function(dependantName){

                            // retrieve the dependant parameter, clear value and mark as not loaded
                            var dependant = getParameter(dependantName);
                            dependant.value = "";
                            dependant.loaded = false;

                            var dependenciesCollected = true;
                            dependant.source().arguments.forEach(function(argument){

                                if(argument.type === 0){
                                    // represents a parameter dependency

                                    var dependencyName = argument.name;
                                    var dependency = getParameter(dependencyName);
                                    if (dependency.collected) {
                                        argument.value = dependency.value;
                                    }
                                    else{
                                        dependenciesCollected = false;
                                    }
                                }
                            });

                            if (dependenciesCollected) {
                                dependant.loading = true;
                                dynamicSourceRequests.parameters.push(dependant.data);
                            }
                        });

                        loadDynamicSources(dynamicSourceRequests);
                    }

                    if ($scope.onChange)
                        $scope.onChange();
                     */
                };

                $scope.editQuery = function(evt, parameter) {

                    var data = parameter.value;

                    var position = {
                        width: 800,
                        centerX: evt.clientX,
                        centerY: evt.clientY
                    };

                    modalService.show({
                        templateUrl: '/assets/scripts/views/editor.html',
                        controller: 'blockConfigEditorController',
                        inputs: { data: data },
                        config: {
                            name: "Edit Query",
                            showSave : true,
                            showCancel: true
                        },
                        position: position
                    }).then(function (modal) {

                        modal.close.then(function (query) {

                            if (query) {

                                parameter.value = query;
                                $scope.setValue(parameter);
                            }
                        });
                    });
                };

                init();
            }
        }
    }]);

