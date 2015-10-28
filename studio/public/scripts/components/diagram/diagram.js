'use strict';

var diagramApp = angular.module('diagramApp', ['emr.ui.interact', 'emr.ui.popup', 'ngAnimate', 'blockDataViewerApp'])
    .directive('diagram', ['$compile', '$window', '$timeout', 'popupService',
        function ($compile, $window, $timeout, popupService) {

        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/assets/scripts/components/diagram/diagram.html',
            scope: {
                diagram: '=viewModel',
                onDisplay: '=',
                onSelection: '=',
                onDeselection: '=',
                nodes: '=',
                library: '=',
                loadSources: "=",
                blurBackground: "=",
                methods: "=?"
            },
            link: function($scope, element, attrs) {

                $scope.configuringBlock = false;
                $scope.configuringDiagram = false;
                $scope.creatingBlock = false;
                $scope.draggingSelection = false;
                $scope.draggingWire = false;
                $scope.mouseOverBlock = null;
                $scope.mouseOverConnector = null;
                $scope.renamingBlock = false;

                $scope.boundary = { x: 0, y: 0, width: 0, height: 0 };

                $scope.panning = false;
                $scope.canvasMatrix = [1, 0, 0, 1, 0, 0];

                $scope.internalMethods = $scope.methods || {};

                element[0].addEventListener("wheel", function(evt){
                    zoom(evt.wheelDeltaY / -100, { x: evt.offsetX, y: evt.offsetY });
                    $scope.$$phase || $scope.$apply();
                });

                var beginDragEvent = function (x, y, config) {

                    $scope.$root.$broadcast("beginDrag", {

                        x: x,
                        y: y,
                        config: config
                    });
                };

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
                    var hoverElement = searchUp($(mouseOverElement), whichClass);
                    if (!hoverElement) {
                        return null;
                    }

                    return hoverElement.scope();
                };

                //
                // Translate coordinates so they are relative to the svg element
                //
                $scope.internalMethods.translateCoordinates = function (x, y, evt) {

                    var diagram = element[0].querySelector('svg');
                    var matrix = diagram.getScreenCTM();
                    var point = diagram.createSVGPoint();
                    point.x = (x - evt.view.scrollX);
                    point.y = (y - evt.view.scrollY);

                    var transform = point.matrixTransform(matrix.inverse());

                    // adjust position based on zoom level
                    return {
                            x: (transform.x - $scope.canvasMatrix[4]) / $scope.canvasMatrix[0],
                            y: (transform.y - $scope.canvasMatrix[5]) / $scope.canvasMatrix[3]
                    };
                };

                var inverseCoordinates = function (x, y) {

                    var diagram = element[0].querySelector('svg');
                    var transX =  $scope.canvasMatrix[0] * x + $scope.canvasMatrix[4];
                    var transY =  $scope.canvasMatrix[3] * y + $scope.canvasMatrix[5];

                    var matrix = diagram.getScreenCTM().translate(transX, transY);

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

                            coords = $scope.internalMethods.translateCoordinates(x, y, evt);

                            // if necessary - select the block
                            if (!block.selected()) {

                                if ($scope.onSelection)
                                    $scope.onSelection([block]);
                            }
                        },

                        dragging: function (x, y) {

                            var curCoords = $scope.internalMethods.translateCoordinates(x, y, evt);
                            var deltaX = curCoords.x - coords.x;
                            var deltaY = curCoords.y - coords.y;

                            diagram.updateSelectedBlocksLocation(deltaX, deltaY);

                            coords = curCoords;
                        },

                        clicked: function () {

                            if ($scope.onSelection) {

                                $scope.$apply(function () {
                                    $scope.onSelection([block]);
                                });
                            }
                        }
                    });

                    evt.stopPropagation();
                    evt.preventDefault();
                };

                $scope.connectorMouseDown = function (evt, block, connector) {

                    var diagram = $scope.diagram;
                    var initialPoint;

                    // broadcast a drag event
                    beginDragEvent(evt.pageX, evt.pageY, {

                        dragStarted: function (x, y, evt) {

                            var coords = $scope.internalMethods.translateCoordinates(x, y, evt);
                            initialPoint = { x: block.x() + connector.x(), y: block.y() + connector.y() };

                            $scope.draggingWire = true;
                            $scope.dragPoint1 = initialPoint;
                            $scope.dragPoint2 = {
                                x: coords.x,
                                y: coords.y
                            };
                            $scope.dragTangent1 = diagram.computeWireSourceTangent($scope.dragPoint1, $scope.dragPoint2);
                            $scope.dragTangent2 = diagram.computeWireTargetTangent($scope.dragPoint1, $scope.dragPoint2);
                        },

                        dragging: function (x, y, evt) {

                            var coords = $scope.internalMethods.translateCoordinates(x, y, evt);

                            $scope.dragPoint1 = initialPoint;
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

                $scope.diagramMouseDown = function (evt) {

                    clearRename();

                    // ignore right clicks
                    if (evt.which === 3 || evt.button === 2)
                        return;

                    $scope.panning = true;

                    var coords;
                    beginDragEvent(evt.pageX, evt.pageY, {

                        dragStarted: function (x, y) {

                            // capture the initial drag coordinates
                            coords = {
                                x: x,
                                y: y
                            };
                        },

                        dragging: function (x, y) {

                            var curCoords = {
                                x: x,
                                y: y
                            };

                            var deltaX = curCoords.x - coords.x;
                            var deltaY = curCoords.y - coords.y;

                            $scope.canvasMatrix[4] += deltaX;
                            $scope.canvasMatrix[5] += deltaY;

                            coords = curCoords;
                        },

                        dragEnded: function (x, y, evt) {

                            $scope.$apply(function () {
                                $scope.panning = false;
                            });
                        },

                        clicked: function () {

                            // diagram click

                            $scope.$apply(function () {
                                $scope.panning = false;

                                if ($scope.diagram.getSelectionCount() > 0) {

                                    // if configured, fire the deselection event
                                    if ($scope.onDeselection)
                                        $scope.onDeselection();


                                    $scope.diagram.deselectAll();
                                }
                            });
                        }
                    });

                    evt.stopPropagation();
                    evt.preventDefault();
                };

                // todo: move to app.js
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

                                configBlock.x = ($scope.containerSize.width / 2) - 100;
                                configBlock.y = 50;

                                $scope.diagram.createBlock(configBlock);
                            }
                        });
                    });

                    evt.stopPropagation();
                    evt.preventDefault();
                };

                $scope.onBlockDisplay = function (evt, block){

                    if ($scope.onDisplay){

                        // capture the block's coordinates and set the popup's width and height
                        var position = inverseCoordinates(block.x(), block.y());
                        position.zoom = $scope.canvasMatrix[0];

                        $scope.onDisplay(position, block);
                    }

                    evt.stopPropagation();
                    evt.preventDefault();
                };

                $scope.onFit = function(evt){

                    var padding = 40;
                    var duration = 200;

                    // retrieve the diagram's boundary
                    var diagramBoundary = $scope.diagram.getBoundary();
                    $scope.boundary = {
                        x: (diagramBoundary.x1 == Number.MIN_VALUE) ? 0 : diagramBoundary.x1,
                        y: (diagramBoundary.y1 == Number.MIN_VALUE) ? 0 : diagramBoundary.y1,
                        width: (diagramBoundary.x2 - diagramBoundary.x1),
                        height: (diagramBoundary.y2 - diagramBoundary.y1)
                    };

                    // bail if either dimension is 0
                    if ($scope.boundary.width <= 0 || $scope.boundary.height <= 0)
                        return;

                    // find the ultimate zoom factor
                    var zoomX = element.width() / ($scope.boundary.width + 2 * padding);
                    var zoomY = element.height() / ($scope.boundary.height + 2 * padding);
                    var zoomFactor = (zoomX < 1 && zoomX < zoomY) ? zoomX : ((zoomY < 1 && zoomY < zoomX) ? zoomY : 1);

                    // find delta and if necessary, zoom out
                    var delta = zoomFactor - $scope.canvasMatrix[0];
                    var deltaZoom = (delta < 1) ? delta : 0;

                    // center the boundary
                    var deltaX = (element.width() / 2)
                        - zoomFactor * ($scope.boundary.width / 2 + $scope.boundary.x) - $scope.canvasMatrix[4];
                    var deltaY = (element.height() / 2)
                        - zoomFactor * ($scope.boundary.height / 2 + $scope.boundary.y) - $scope.canvasMatrix[5];

                    animateZoomPan(deltaZoom, deltaX, deltaY, duration);

                    evt.stopPropagation();
                    evt.preventDefault();
                };

                $scope.onZoom = function(evt, delta){

                    zoom(delta);

                    evt.stopPropagation();
                    evt.preventDefault();
                };

                function zoom(delta, point){

                    // if a focal point was not specified, zoom in on the center of the canvas
                    if (point === undefined)
                        point = { x: (element.width() / 2), y: (element.height() / 2) };

                    var zoomFactor = $scope.canvasMatrix[0] + delta;
                    zoomFactor = (zoomFactor < 0.2) ? 0.2 : ((zoomFactor > 2.0) ? 2.0 : zoomFactor);

                    var factor = zoomFactor / $scope.canvasMatrix[0];
                    $scope.canvasMatrix[0] = zoomFactor;
                    $scope.canvasMatrix[3] = zoomFactor;

                    // adjust the transform to zoom in a on a specific point
                    $scope.canvasMatrix[4] = (factor * $scope.canvasMatrix[4]) + (1 - factor) * point.x;
                    $scope.canvasMatrix[5] = (factor * $scope.canvasMatrix[5]) + (1 - factor) * point.y;
                }

                function animateZoomPan(deltaZoom, deltaX, deltaY, duration, iteration){

                    var stepSize = 20;
                    $timeout(function(){

                        if (iteration === undefined)
                            iteration = 0;

                        var iterations = Math.ceil(duration / stepSize);

                        $scope.canvasMatrix[0] += deltaZoom / iterations;
                        $scope.canvasMatrix[3] += deltaZoom / iterations;
                        $scope.canvasMatrix[4] += deltaX / iterations;
                        $scope.canvasMatrix[5] += deltaY / iterations;

                        if (++iteration < iterations)
                            animateZoomPan(deltaZoom, deltaX, deltaY, duration, iteration);

                    }, stepSize);
                }

                $scope.diagramMode = function(){

                    var mode = "UNKNOWN";
                    if ($scope.diagram)
                        mode = $scope.diagram.mode();

                    return mode;
                };

                $scope.renameBlockClick = function(block) {

                    $scope.renameBlock = {

                        x: block.x(),
                        y: block.y(),
                        w: 147,
                        block: block
                    };

                    $scope.renamingBlock = true;

                    // We need to set the focus on the rename input element but we must
                    // wait a while as the element has not been created yet.
                    setInterval(function() { $('#rename-input').focus() }, 10);
                };

                $scope.endRenameOnEnter = function() {

                    if(event.keyCode === 13) {

                        clearRename();
                    }
                };

                $scope.diagramClick = function() {

                    clearRename();
                };

                function clearRename() {

                    if ($scope.renamingBlock === true) {

                        $scope.renamingBlock = false;
                        delete $scope.renameBlock;
                    }
                };

            }
        }
    }]);
