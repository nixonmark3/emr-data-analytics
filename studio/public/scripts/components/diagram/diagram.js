'use strict';

var diagramApp = angular.module('diagramApp', ['draggableApp', 'popupApp', 'ngAnimate', 'blockDataViewerApp'])
    .directive('diagram', ['$compile', '$window', '$location', '$timeout', 'popupService',
        function ($compile, $window, $location, $timeout, popupService) {

        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/assets/scripts/components/diagram/diagram.html',
            scope: {
                diagram: '=viewModel',
                nodes: '=',
                library: '=',
                loadSources: "=",
                getBlockData: "=",
                blurBackground: "="
            },
            link: function($scope, element, attrs) {

                $scope.draggingWire = false;
                $scope.mouseOverBlock = null;
                $scope.mouseOverConnector = null;
                $scope.configuringBlock = false;
                $scope.configuringDiagram = false;
                $scope.showingBlockData = false;
                $scope.creatingBlock = false;

                // capture the diagram's width
                $scope.diagramWidth = element.width();

                $scope.absUrl = $location.absUrl();

                //
                // listen for window resizes
                //
                angular.element($window).bind("resize", function () {

                    // update the width property on window resize
                    $scope.$apply(function () {

                        $scope.diagramWidth = element.width();
                    });
                });

                var jQuery = function (element) {
                    return $(element);
                };

                var beginDragEvent = function (x, y, config) {

                    $scope.$root.$broadcast("beginDrag", {
                        x: x,
                        y: y,
                        config: config
                    });
                };

                $scope.$on("createBlock", function (event, args) {

                    // translate diagram (svg) coordinates into application coordinates
                    var point = translateCoordinates(args.x, args.y, args.evt);

                    var configBlock = getConfigBlock(point.x, point.y, args.definitionName);

                    // create the block
                    $scope.diagram.createBlock(configBlock);
                });

                var hasClassSVG = function (obj, has) {
                    var classes = obj.attr('class');
                    if (!classes) {
                        return false;
                    }

                    var index = classes.search(has);

                    if (index == -1) {
                        return false;
                    }
                    else {
                        return true;
                    }
                };

                //
                // Search up the HTML element tree for an element the requested class.
                //
                var searchUp = function (element, parentClass) {

                    //
                    // Reached the root.
                    //
                    if (element == null || element.length == 0) {
                        return null;
                    }

                    //
                    // Check if the element has the class that identifies it as a connector.
                    //
                    if (hasClassSVG(element, parentClass)) {
                        //
                        // Found the connector element.
                        //
                        return element;
                    }

                    //
                    // Recursively search parent elements.
                    //
                    return searchUp(element.parent(), parentClass);
                };

                //
                // Hit test and retreive node and connector that was hit at the specified coordinates.
                //
                var hitTest = function (clientX, clientY) {

                    //
                    // Retreive the element the mouse is currently over.
                    //
                    return document.elementFromPoint(clientX, clientY);
                };

                //
                // Hit test and retreive node and connector that was hit at the specified coordinates.
                //
                var checkForHit = function (mouseOverElement, whichClass) {

                    //
                    // Find the parent element, if any, that is a connector.
                    //
                    var hoverElement = searchUp(jQuery(mouseOverElement), whichClass);
                    if (!hoverElement) {
                        return null;
                    }

                    return hoverElement.scope();
                };

                //
                // Translate the coordinates so they are relative to the svg element
                //
                var translateCoordinates = function (x, y, evt) {
                    var diagram = document.getElementById('diagram');
                    var matrix = diagram.getScreenCTM();
                    var point = diagram.createSVGPoint();
                    point.x = x - evt.view.scrollX;
                    point.y = y - evt.view.scrollY;
                    return point.matrixTransform(matrix.inverse());
                };

                var inverseCoordinates = function (x, y) {
                    var diagram = document.getElementById('diagram');
                    var matrix = diagram.getScreenCTM().translate(x, y);

                    return {
                        x: matrix.e, y: matrix.f
                    };
                };

                var getConfigBlock = function (x, y, definitionName) {

                    // retrieve definition
                    var definition = getDefinition(definitionName);

                    // get block description
                    var block = $scope.diagram.getBlockDescription(x, y, definition);

                    // create config block
                    return new viewmodels.configuringBlockViewModel(definition, block);
                };

                var getDefinition = function (definitionName) {

                    return $scope.library[definitionName];
                };

                $scope.mouseMove = function (evt) {

                    var mouseOverElement = hitTest(evt.clientX, evt.clientY);

                    if (mouseOverElement == null) {
                        // Mouse isn't over anything, just clear all.
                        return;
                    }

                    // check whether the mouse is over a connector
                    var scope = checkForHit(mouseOverElement, 'connector-group');
                    $scope.mouseOverConnector = (scope && scope.connector) ? scope.connector : null;

                    // check whether the mouse is over a block
                    scope = checkForHit(mouseOverElement, 'block-group');
                    $scope.mouseOverBlock = (scope && scope.block) ? scope.block : null;
                };

                //
                // Handle mouse down on block
                //
                $scope.blockMouseDown = function (evt, block) {

                    var diagram = $scope.diagram;
                    var coords;

                    beginDragEvent(evt.pageX, evt.pageY, {

                        dragStarted: function (x, y) {

                            coords = translateCoordinates(x, y, evt);

                            // if necessary - select the block
                            if (!block.selected()) {
                                diagram.deselectAll();
                                block.select();
                            }
                        },

                        dragging: function (x, y) {

                            var curCoords = translateCoordinates(x, y, evt);
                            var deltaX = curCoords.x - coords.x;
                            var deltaY = curCoords.y - coords.y;

                            diagram.updateSelectedBlocksLocation(deltaX, deltaY);

                            coords = curCoords;
                        },

                        clicked: function () {

                            block.advanceProgress();
                            $scope.$apply(diagram.handleBlockClicked(block));
                        }
                    });

                    evt.stopPropagation();
                    evt.preventDefault();
                };

                $scope.connectorMouseDown = function (evt, block, connector, connectorIndex, isInputConnector) {

                    var diagram = $scope.diagram;

                    // broadcast a drag event
                    beginDragEvent(evt.pageX, evt.pageY, {

                        dragStarted: function (x, y, evt) {

                            var coords = translateCoordinates(x, y, evt);

                            $scope.draggingWire = true;
                            $scope.dragPoint1 = diagram.computeConnectorPos(block, connectorIndex, isInputConnector);
                            $scope.dragPoint2 = {
                                x: coords.x,
                                y: coords.y
                            };
                            $scope.dragTangent1 = diagram.computeWireSourceTangent($scope.dragPoint1, $scope.dragPoint2);
                            $scope.dragTangent2 = diagram.computeWireTargetTangent($scope.dragPoint1, $scope.dragPoint2);
                        },

                        dragging: function (x, y, evt) {

                            var coords = translateCoordinates(x, y, evt);

                            $scope.dragPoint1 = diagram.computeConnectorPos(block, connectorIndex, isInputConnector);
                            $scope.dragPoint2 = {
                                x: coords.x,
                                y: coords.y
                            };
                            $scope.dragTangent1 = diagram.computeWireSourceTangent($scope.dragPoint1, $scope.dragPoint2);
                            $scope.dragTangent2 = diagram.computeWireTargetTangent($scope.dragPoint1, $scope.dragPoint2);
                        },

                        dragEnded: function (evt) {

                            if ($scope.mouseOverConnector &&
                                $scope.mouseOverConnector !== connector) {

                                // dragging is complete and the new wire is over a valid connector
                                // create a new wire
                                diagram.createWire(connector, $scope.mouseOverConnector);
                            }

                            $scope.draggingWire = false;
                            delete $scope.dragPoint1;
                            delete $scope.dragTangent1;
                            delete $scope.dragPoint2;
                            delete $scope.dragTangent2;

                            $scope.$apply();
                        },

                        clicked: function (evt) {

                            $scope.showLibraryPopup(evt);
                        }
                    });

                    evt.stopPropagation();
                    evt.preventDefault();
                };

                $scope.deleteBlock = function (evt, block) {

                    // If the configuration box is open close it!
                    if ($scope.configuringBlock) {
                        endBlockConfiguration(false);
                    }

                    // select the specified block
                    $scope.diagram.deleteBlock(block);

                    evt.stopPropagation();
                    evt.preventDefault();
                };

                $scope.toggleBlockConfiguration = function (evt, block) {

                    if ($scope.configuringBlock) {
                        endBlockConfiguration(true);
                    }
                    else {
                        beginBlockConfiguration(block);
                    }

                    evt.stopPropagation();
                    evt.preventDefault();
                };

                var beginBlockConfiguration = function (block) {

                    // capture the block coordinates and set the popup's width and height
                    var position = inverseCoordinates(block.x(), block.y());
                    position.width = 250;
                    position.height = 300;

                    // retrieve definition
                    var definition = getDefinition(block.definition());

                    // convert to config block
                    var configBlock = new viewmodels.configuringBlockViewModel(definition, block.data);

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
                    });
                };

                var endBlockConfiguration = function (save) {

                    $scope.blockConfiguration.controller.close();

                    $scope.blockConfiguration.close.then(function (configBlock) {

                        if (save)
                            $scope.diagram.updateBlock(configBlock);
                    });

                    $scope.configuringBlock = false;
                    delete $scope.blockConfiguration;
                };

                //
                // Event that handles any click on the diagram
                //
                $scope.diagramClick = function (evt) {

                    if ($scope.configuringBlock)
                        endBlockConfiguration(true);

                    if ($scope.showingBlockData)
                        hideBlockData();

                    evt.stopPropagation();
                    evt.preventDefault();
                };

                $scope.showLibraryPopup = function (evt) {

                    popupService.show({
                        templateUrl: '/assets/scripts/components/diagram/library.html',
                        controller: 'libraryController',
                        inputs: {
                            nodes: $scope.nodes,
                            getConfigBlock: getConfigBlock,
                            loadSources: $scope.loadSources

                        }
                    }).then(function (popup) {

                        $scope.creatingBlock = true;
                        $scope.blurBackground = true;

                        popup.close.then(function (configBlock) {

                            $scope.creatingBlock = false;
                            $scope.blurBackground = false;

                            if (configBlock) {

                                // todo: temp coordinates

                                configBlock.x = ($scope.diagramWidth / 2) - 100;
                                configBlock.y = 50;

                                $scope.diagram.createBlock(configBlock);
                            }
                        });
                    });

                    evt.stopPropagation();
                    evt.preventDefault();
                };

                $scope.toggleBlockData = function (evt, block) {

                    if ($scope.showingBlockData) {
                        hideBlockData();
                    }
                    else {
                        showBlockData(block);
                    }

                    evt.stopPropagation();
                    evt.preventDefault();
                };

                var showBlockData = function (block) {

                    var position = inverseCoordinates(block.x(), block.y());
                    position.width = 750;
                    position.height = 550;

                    popupService.show({
                        templateUrl: '/assets/scripts/components/diagram/blockData.html',
                        controller: 'blockDataController',
                        isAnimated: false,
                        inputs: {
                            block: block,
                            position: position,
                            getBlockData: $scope.getBlockData
                        }
                    }).then(function (popup) {

                        $scope.showingBlockData = true;
                        $scope.blurBackground = true;
                        $scope.blockDataPopup = popup;
                    });
                };

                var hideBlockData = function () {
                    $scope.blockDataPopup.controller.close();

                    $scope.showingBlockData = false;
                    $scope.blurBackground = false;
                    delete $scope.blockDataPopup;
                };
            }
        }
    }]);
