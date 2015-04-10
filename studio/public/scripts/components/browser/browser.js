'use strict';

angular.module('browserApp', ['ngAnimate', 'draggableApp'])
    .directive('browser', ['$compile', function ($compile) {

        return {
            restrict: 'E',
            replace: false,
            templateUrl: '/assets/scripts/components/browser/browser.html',
            scope: {
                nodes: "=",
                onDrop: "=",
                getConfigBlock: "=",
                loadSources: "=",
                savable: "=",
                output: "="
            },
            link: function($scope, element, attrs){

                $scope.pageIndex = 0;

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

                    if ($scope.onDrop !== undefined)
                        return false;
                    else
                        return true;
                };

                // fire the event to begin dragging an element
                var beginDragEvent = function(x, y, config){

                    $scope.$root.$broadcast("beginDrag", {
                        x: x,
                        y: y,
                        config: config
                    });
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

                        beginDragEvent(evt.pageX, evt.pageY, {

                            dragStarted: function (x, y) {

                                // create the element that represents the dragging definition
                                var ghost = angular.element('<div id="ghost" ' +
                                    'ng-style="{top: ghostPosition.y, ' +
                                    'left: ghostPosition.x, ' +
                                    'width: 200, ' +
                                    'height: 80}"></div>');

                                // compile and append to the parent
                                $compile(ghost)($scope);
                                element.parent().append(ghost);
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
    .directive('blockConfig', [function () {

        return {
            restrict: 'E',
            replace: false,
            templateUrl: '/assets/scripts/components/browser/blockConfig.html',
            scope: {
                block: "=",
                loadSources: "="
            },
            link: function ($scope, element, attrs) {

                // list of dependants for reverse lookup
                $scope.dependants = {};

                var init = function(){

                    // assemble initial list of dynamic parameters
                    var dynamicSourceRequests = [];
                    $scope.block.parameters.forEach(function(parameter){

                        // if the parameter is not loaded and has no dependencies -
                        // flag as loading and add to the dynamicSourceRequests list


                        if (!parameter.loaded){

                            var request = {
                                name: parameter.name,
                                method: parameter.options.method,
                                arguments: [],
                                fieldOptions: []
                            };

                            var dependenciesCollected = true;
                            parameter.options.dependencies.forEach(function(dependencyName){

                                var dependency = getParameter(dependencyName);
                                if (dependency.collected) {
                                    request.arguments.push(dependency.value);
                                }
                                else{
                                    dependenciesCollected = false;
                                    // todo: break ?
                                }
                            });

                            if (dependenciesCollected) {
                                parameter.loading = true;
                                dynamicSourceRequests.push(request);
                            }
                        }

                        parameter.options.dependencies.forEach(function(dependencyName){

                            if (!(dependencyName in $scope.dependants))
                                $scope.dependants[dependencyName] = [];

                            $scope.dependants[dependencyName].push(parameter.name);

                        });
                    });

                    loadDynamicSources(dynamicSourceRequests);
                };

                var loadDynamicSources = function(request){

                    if (request.length === 0)
                        return;

                    $scope.loadSources(request, function(response){

                        response.forEach(function(source){

                            var parameter = getParameter(source.name);

                            // toggle value
                            var temp = parameter.value;
                            parameter.value = "";

                            parameter.options.fieldOptions = source.fieldOptions;
                            parameter.value = temp;
                            parameter.loaded = true;
                            parameter.loading = false;
                        });
                    });
                };

                var getParameter = function(name){

                    var parameters = $scope.block.parameters;
                    for (var i = 0; i < parameters.length; i++){
                        if (parameters[i].name == name)
                            return parameters[i];
                    }
                };

                $scope.setValue = function(parameter){

                    parameter.dirty = true;
                    parameter.collected = true;

                    // check for dependants
                    if (parameter.name in $scope.dependants){

                        var dependants = $scope.dependants[parameter.name];
                        var dynamicSourceRequests = [];
                        dependants.forEach(function(dependantName){

                            var dependant = getParameter(dependantName);
                            dependant.value = "";
                            dependant.loaded = false;
                            var request = {
                                name: dependant.name,
                                method: dependant.options.method,
                                arguments: [],
                                fieldOptions: []
                            };

                            var dependenciesCollected = true;
                            dependant.options.dependencies.forEach(function(dependencyName){

                                var dependency = getParameter(dependencyName);
                                if (dependency.collected) {
                                    request.arguments.push(dependency.value);
                                }
                                else{
                                    dependenciesCollected = false;
                                    // todo: break ?
                                }
                            });

                            if (dependenciesCollected) {
                                dependant.loading = true;
                                dynamicSourceRequests.push(request);
                            }
                        });

                        loadDynamicSources(dynamicSourceRequests);
                    }
                };

                // initialize
                init();
            }
        }
    }]);

