'use strict';

var diagramApp = angular.module('diagramApp', ['draggableApp'])
    .directive('diagram', ['$compile', function ($compile, $timeout) {

        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/assets/scripts/components/diagram/diagram.html',
            scope: {
                diagram: '=viewModel'
            },
            link: function($scope, element, attrs){

                $scope.draggingWire = false;
                $scope.currentHoverConnector = null;
                $scope.dragSelecting = false;
                $scope.configuringBlock = false;
                $scope.configurationBlock = {};

                $scope.mouseOverConnector = null;
                $scope.mouseOverWire = null;
                $scope.mouseOverBlock = null;

                $scope.renamingBlock = false;

                var jQuery = function (element) {
                    return $(element);
                };

                var wireClass = 'wire-group';
                var connectorClass = 'connector-group';
                var blockClass = 'block-group';

                var beginDragEvent = function(x, y, config){

                    $scope.$root.$broadcast("beginDrag", {
                        x: x,
                        y: y,
                        config: config
                    });
                };

                $scope.$on("createBlock", function (event, args) {

                    var point = translateCoordinates(args.x, args.y, args.evt);
                    $scope.diagram.createBlock(point.x, point.y, args.definition);
                });

                $scope.$on("deleteSelected", function (event) {

                    $scope.diagram.deleteSelected();
                });

                $scope.$on("deselectAll", function (event) {

                    $scope.diagram.deselectAll();
                });

                $scope.$on("selectAll", function (event) {

                    $scope.diagram.selectAll();
                });

                var hasClassSVG = function(obj, has) {
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
                var translateCoordinates = function(x, y, evt) {
                    var diagram = document.getElementById('diagram');
                    var matrix = diagram.getScreenCTM();
                    var point = diagram.createSVGPoint();
                    point.x = x - evt.view.scrollX;
                    point.y = y - evt.view.scrollY;
                    return point.matrixTransform(matrix.inverse());
                };

                var inverseCoordinates = function(x, y){
                    var diagram = document.getElementById('diagram');
                    var matrix = diagram.getScreenCTM().translate(x, y);

                    return {
                        x: matrix.e, y: matrix.f
                    };
                };

                //
                // Called on mouse down in the diagram
                //
                $scope.mouseDown = function (evt) {

                    $scope.diagram.deselectAll();

                    beginDragEvent(evt.pageX, evt.pageY, {

                        dragStarted: function (x, y) {

                            $scope.dragSelecting = true;
                            var startPoint = translateCoordinates(x, y, evt);
                            $scope.dragSelectionStartPoint = startPoint;
                            $scope.dragSelectionRect = {
                                x: startPoint.x,
                                y: startPoint.y,
                                width: 0,
                                height: 0
                            };
                        },

                        dragging: function (x, y) {

                            var startPoint = $scope.dragSelectionStartPoint;
                            var curPoint = translateCoordinates(x, y, evt);

                            $scope.dragSelectionRect = {
                                x: curPoint.x > startPoint.x ? startPoint.x : curPoint.x,
                                y: curPoint.y > startPoint.y ? startPoint.y : curPoint.y,
                                width: curPoint.x > startPoint.x ? curPoint.x - startPoint.x : startPoint.x - curPoint.x,
                                height: curPoint.y > startPoint.y ? curPoint.y - startPoint.y : startPoint.y - curPoint.y
                            };
                        },

                        dragEnded: function () {
                            $scope.dragSelecting = false;
                            $scope.diagram.applySelectionRect($scope.dragSelectionRect);
                            delete $scope.dragSelectionStartPoint;
                            delete $scope.dragSelectionRect;
                        }
                    });
                };

                //
                // Called for each mouse move on the svg element.
                //
                $scope.mouseMove = function (evt) {

                    //
                    // Clear out all cached mouse over elements.
                    //
                    $scope.mouseOverWire = null;
                    $scope.mouseOverConnector = null;
                    $scope.mouseOverBlock = null;

                    var mouseOverElement = hitTest(evt.clientX, evt.clientY);
                    if (mouseOverElement == null) {
                        // Mouse isn't over anything, just clear all.
                        return;
                    }

                    if (!$scope.draggingWire) { // Only allow 'connection mouse over' when not dragging out a connection.

                        // Figure out if the mouse is over a connection.
                        var scope = checkForHit(mouseOverElement, wireClass);
                        $scope.mouseOverWire = (scope && scope.wire) ? scope.wire : null;
                        if ($scope.mouseOverWire) {
                            // Don't attempt to mouse over anything else.
                            return;
                        }
                    }

                    // Figure out if the mouse is over a connector.
                    var scope = checkForHit(mouseOverElement, connectorClass);
                    $scope.mouseOverConnector = (scope && scope.connector) ? scope.connector : null;
                    if ($scope.mouseOverConnector) {
                        // Expand the radius of the connector to make it easier to use.
                        scope.connector.radius = scope.connector.expandedRadius();
                        scope.connector.showName = true;
                        $scope.currentHoverConnector = scope.connector;
                        return;
                    }
                    else {
                        if ($scope.currentHoverConnector) {
                            // If we have expanded a connector reduce the radius to normal.
                            $scope.currentHoverConnector.radius = $scope.currentHoverConnector.normalRadius();
                            $scope.currentHoverConnector.showName = false;
                            $scope.currentHoverConnector = null;
                        }
                    }

                    // Figure out if the mouse is over a node.
                    var scope = checkForHit(mouseOverElement, blockClass);
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

                        dragEnded: function () {

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
                        }
                    });

                    evt.stopPropagation();
                    evt.preventDefault();
                };

                $scope.deleteBlock = function(){
                    $scope.diagram.deleteSelected();

                };

                $scope.toggleConfiguration = function(evt){

                    if ($scope.configuringBlock){
                        endBlockConfiguration();
                    }
                    else{
                        beginBlockConfiguration(evt);
                    }
                };

                var beginBlockConfiguration = function(block){

                    var point = inverseCoordinates(block.x(), block.y());

                    $scope.configurationBlock = {
                        x: point.x,
                        y: point.y,
                        block: block
                    };

                    $scope.configuringBlock = true;
                };

                var endBlockConfiguration = function(){
                    $scope.configuringBlock = false;
                    delete $scope.configurationBlock;
                };

                $scope.renameClick = function(evt) {
                    var point = inverseCoordinates(evt.x(), evt.y());

                    $scope.renameBlock = {
                        x: point.x,
                        y: point.y,
                        block: evt
                    };

                    $scope.renamingBlock = true;

                    // We need to set the focus on the rename input element but we must
                    // wait a while as the element has not been created yet.
                    setInterval(function() { $('#rename-input').focus() }, 10);
                };

                $scope.diagramClick = function() {
                    $scope.renamingBlock = false;
                    delete $scope.renameBlock;
                };

                $scope.endRenameOnEnter = function() {
                    if(event.keyCode == 13) {
                        $scope.diagramClick();
                    }
                };
            }
        }
    }]
);
