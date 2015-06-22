'use strict';

var diagramApp = angular.module('diagramApp', ['emr.ui.interact', 'emr.ui.popup', 'ngAnimate', 'blockDataViewerApp'])
    .directive('diagram', ['$compile', '$window', '$location', '$timeout', 'popupService',
        function ($compile, $window, $location, $timeout, popupService) {

        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/assets/scripts/components/diagram/diagram.html',
            scope: {
                diagram: '=viewModel',
                onConfigure: '=?',
                onSelection: '=?',
                nodes: '=?',
                library: '=?',
                loadSources: "=?",
                getBlockData: "=?",
                blurBackground: "=?",
                selectionCount: "=?"
            },
            link: function($scope, element, attrs) {

                var mainWindow = angular.element($window);

                $scope.configuringBlock = false;
                $scope.configuringDiagram = false;
                $scope.creatingBlock = false;
                $scope.draggingSelection = false;
                $scope.draggingWire = false;
                $scope.mouseOverBlock = null;
                $scope.mouseOverConnector = null;
                $scope.showingBlockData = false;
                $scope.selectionCount = 0;
                $scope.absUrl = $location.absUrl();
                $scope.mainWindowWidth = mainWindow.width();

                mainWindow.bind("resize", function () {

                    $scope.$apply(function () {

                        $scope.mainWindowWidth = mainWindow.width();
                    });
                });

                $scope.getDiagramWidth = function() {

                    if ($scope.diagram != undefined) {

                        var diagram = $scope.diagram;

                        if (diagram.blocks.length == 0) {

                            diagram.setWidth(mainWindow.width());

                            return diagram.getWidth();
                        }
                        else {

                            return diagram.getWidth();
                        }
                    }

                    return mainWindow.width();
                };

                $scope.getDiagramHeight = function() {

                    if ($scope.diagram != undefined) {

                        var diagram = $scope.diagram;

                        if (diagram.blocks.length == 0) {

                            diagram.setHeight(getDefaultDiagramHeight());

                            return diagram.getHeight();
                        }
                        else {

                            return diagram.getHeight();
                        }
                    }

                    return getDefaultDiagramHeight();
                };

                $scope.showRootConnector = function() {

                    if ($scope.diagram != undefined) {

                        var diagram = $scope.diagram;

                        if (diagram.blocks.length == 0) {

                            diagram.setWidth(mainWindow.width());
                            diagram.setHeight(getDefaultDiagramHeight());
                            return true;
                        }
                    }

                    return false;
                };

                var adjustDiagramSizeIfRequired = function(point) {

                    var diagram = $scope.diagram;

                    var increaseHeight = diagram.isPointInHeightIncreaseZone(point.y);

                    var increaseWidth = diagram.isPointInWidthIncreaseZone(point.x);

                    if (increaseHeight) {

                        $scope.$applyAsync(function() {

                            diagram.increaseHeight();
                        });
                    }

                    if (increaseWidth) {

                        $scope.$applyAsync(function() {

                            diagram.increaseWidth();
                        });
                    }
                };

                var getDefaultDiagramHeight = function() {

                    return mainWindow.height() - 55;
                };

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

                    if ($scope.diagramMode() == "OFFLINE") {

                        // translate diagram (svg) coordinates into application coordinates
                        var point = translateCoordinates(args.x, args.y, args.evt);

                        var configBlock = getConfigBlock(point.x, point.y, args.definitionName);

                        // create the block
                        $scope.diagram.createBlock(configBlock);

                        adjustDiagramSizeIfRequired(point);
                    }
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
                // Translate coordinates so they are relative to the svg element
                //
                var translateCoordinates = function (x, y, evt) {

                    var elementName;
                    if ($scope.diagramMode() == "ONLINE")
                        elementName = "online-diagram";
                    else
                        elementName = "offline-diagram";

                    var diagram = document.getElementById(elementName);
                    var matrix = diagram.getScreenCTM();
                    var point = diagram.createSVGPoint();
                    point.x = x - evt.view.scrollX;
                    point.y = y - evt.view.scrollY;
                    return point.matrixTransform(matrix.inverse());
                };

                var inverseCoordinates = function (x, y) {

                    var elementName;
                    if ($scope.diagramMode() == "ONLINE")
                        elementName = "online-diagram";
                    else
                        elementName = "offline-diagram";

                    var diagram = document.getElementById(elementName);
                    var matrix = diagram.getScreenCTM().translate(x, y);

                    return {
                        x: matrix.e, y: matrix.f
                    };
                };

                //
                // create a block configuration viewmodel
                //
                var getConfigBlock = function (x, y, definitionName) {

                    // using the current diagram's mode and specified definition -
                    // create a definition view model
                    var definition = new viewmodels.definitionViewModel($scope.diagram.mode(),
                        $scope.library[definitionName]);

                    // create a block description
                    var block = $scope.diagram.getBlockDescription(x, y, definition);

                    // create config block
                    return new viewmodels.configuringBlockViewModel(definition, block);
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
                                $scope.selectionCount = diagram.selectBlock(block);

                                if ($scope.onSelection)
                                    $scope.onSelection([block]);
                            }
                        },

                        dragging: function (x, y) {

                            var curCoords = translateCoordinates(x, y, evt);
                            var deltaX = curCoords.x - coords.x;
                            var deltaY = curCoords.y - coords.y;

                            diagram.updateSelectedBlocksLocation(deltaX, deltaY);

                            coords = curCoords;

                            adjustDiagramSizeIfRequired(coords);
                        },

                        clicked: function () {

                            if ($scope.onSelection)
                                $scope.onSelection([block]);

                            $scope.$apply(diagram.onBlockClicked(block));
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

                $scope.onBlockConfigure = function (evt, block) {

                    //
                    if ($scope.onConfigure) {

                        // capture the block's coordinates and set the popup's width and height
                        var position = inverseCoordinates(block.x(), block.y());
                        position.width = 250;
                        position.height = 300;

                        $scope.onConfigure(position, block);
                    }

                    evt.stopPropagation();
                    evt.preventDefault();
                };

                //
                // handles diagram mouse down event: drag allows users to select multiple blocks and click sets focus to
                // the diagram canvas
                //
                $scope.diagramMouseDown = function (evt) {

                    var coords;

                    beginDragEvent(evt.pageX, evt.pageY, {

                        dragStarted: function (x, y) {

                            // capture the initial drag coordinates

                            coords = translateCoordinates(x, y, evt);

                            $scope.draggingSelection = true;
                            $scope.dragSelectionRect = {x: coords.x, y: coords.y, width: 0, height: 0};
                        },

                        dragging: function (x, y) {

                            // update the selection rectangle's position and dimensions based on the current drag position

                            var curCoords = translateCoordinates(x, y, evt);

                            var width = curCoords.x - coords.x;
                            if (width < 0)
                                $scope.dragSelectionRect.x = curCoords.x;
                            $scope.dragSelectionRect.width = Math.abs(width);

                            var height = curCoords.y - coords.y;
                            if (height < 0)
                                $scope.dragSelectionRect.y = curCoords.y;
                            $scope.dragSelectionRect.height = Math.abs(height);
                        },

                        dragEnded: function (x, y, evt) {

                            $scope.$apply(function(){
                                $scope.draggingSelection = false;
                                $scope.selectionCount = $scope.diagram.applySelectionRect($scope.dragSelectionRect);
                            });

                            delete $scope.dragSelectionRect;
                        },

                        clicked: function () {

                            if ($scope.configuringBlock)
                                endBlockConfiguration(true);

                            if ($scope.showingBlockData)
                                hideBlockData();

                            if ($scope.selectionCount > 0) {
                                $scope.$apply(function(){
                                    $scope.selectionCount = $scope.diagram.deselectAll();
                                });
                            }
                        }
                    });

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

                                configBlock.x = ($scope.mainWindowWidth / 2) - 100;
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

                $scope.diagramMode = function(){

                    var mode = "UNKNOWN";
                    if ($scope.diagram)
                        mode = $scope.diagram.mode();

                    return mode;
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
