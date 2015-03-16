'use strict';

angular.module('diagramApp', ['draggableApp'])
    .directive('diagram', ['draggableService', function (draggableService) {

        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/assets/scripts/components/diagram/diagram.html',
            scope: {
                diagram: '=viewModel'
            },
            link: function($scope, element, attrs){

                $scope.draggingWire = false;
                $scope.connectorRadius = 6;
                $scope.dragSelecting = false;

                $scope.mouseOverConnector = null;
                $scope.mouseOverWire = null;
                $scope.mouseOverBlock = null;

                var jQuery = function (element) {
                    return $(element);
                }

                var wireClass = 'wire';
                var connectorClass = 'connector';
                var blockClass = 'block';

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
                    var svg_elem =  element.get(0);
                    var matrix = svg_elem.getScreenCTM();
                    var point = svg_elem.createSVGPoint();
                    point.x = x - evt.view.scrollX;
                    point.y = y - evt.view.scrollY;
                    return point.matrixTransform(matrix.inverse());
                };

                //
                // Called on mouse down in the diagram
                //
                $scope.mouseDown = function (evt) {

                    $scope.diagram.deselectAll();

                    draggableService.startDrag(evt, {

                        //
                        // Commence dragging... setup variables to display the drag selection rect.
                        //
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

                        //
                        // Update the drag selection rect while dragging continues.
                        //
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

                        //
                        // Dragging has ended... select all that are within the drag selection rect.
                        //
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
                        // Don't attempt to mouse over anything else.
                        return;
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
                    var lastMouseCoords;

                    draggableService.startDrag(evt, {

                        // called when drag has started (exceeded threshold)
                        dragStarted: function (x, y) {

                            lastMouseCoords = translateCoordinates(x, y, evt);

                            // if necessary - select the block
                            if (!block.selected()) {
                                diagram.deselectAll();
                                block.select();
                            }
                        },

                        //
                        // Dragging selected blocks... update their x,y coordinates
                        //
                        dragging: function (x, y) {

                            var curCoords = translateCoordinates(x, y, evt);
                            var deltaX = curCoords.x - lastMouseCoords.x;
                            var deltaY = curCoords.y - lastMouseCoords.y;

                            diagram.updateSelectedBlocksLocation(deltaX, deltaY);

                            lastMouseCoords = curCoords;
                        },

                        //
                        // The block wasn't dragged... it was clicked
                        //
                        clicked: function () {
                            diagram.handleBlockClicked(block);
                        }

                    });
                };

            }
        }

    }]);
