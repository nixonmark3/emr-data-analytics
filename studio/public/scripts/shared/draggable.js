
angular.module('emr.ui.interact', [])

    .directive('draggable', function () {

        return {
            restrict: 'A',
            controller: ['$scope', '$element', '$attrs', function($scope, $element, $attrs){

                $scope.dragging = false;

                $scope.coords = {
                    x: 0, y: 0
                };

                var threshold = 5,
                    config = null;

                $scope.$on("beginDrag", function (event, args) {

                    beginDrag(args);
                });

                var beginDrag = function(args){

                    config = args.config;
                    $scope.coords.x = args.x;
                    $scope.coords.y = args.y;

                    $element.mousemove(mouseMove);
                    $element.mouseup(mouseUp);
                };

                var mouseMove = function (evt) {

                    $scope.$apply(function(){

                        if (!$scope.dragging) {
                            if (Math.abs(evt.pageX - $scope.coords.x) > threshold ||
                                Math.abs(evt.pageY - $scope.coords.y) > threshold) {
                                $scope.dragging = true;

                                if (config.dragStarted) {
                                    config.dragStarted($scope.coords.x, $scope.coords.y, evt);
                                }

                                $scope.coords.x = evt.pageX;
                                $scope.coords.y = evt.pageY;

                                if (config.dragging) {
                                    // First 'dragging' call to take into account that we have
                                    // already moved the mouse by a 'threshold' amount.
                                    config.dragging($scope.coords.x, $scope.coords.y, evt);
                                }
                            }
                        }
                        else {
                            $scope.coords.x = evt.pageX;
                            $scope.coords.y = evt.pageY;

                            if (config.dragging) {
                                config.dragging($scope.coords.x, $scope.coords.y, evt);
                            }


                        }});

                    evt.stopPropagation();
                    evt.preventDefault();
                };

                var mouseUp = function (evt) {

                    $scope.coords.x = evt.pageX;
                    $scope.coords.y = evt.pageY;

                    // release

                    if ($scope.dragging) {
                        if (config.dragEnded) {
                            config.dragEnded($scope.coords.x, $scope.coords.y, evt);
                        }
                    }
                    else {
                        if (config && config.clicked) {
                            config.clicked(evt);
                        }
                    }

                    $scope.dragging = false;

                    config = null;

                    $element.unbind("mousemove", mouseMove);
                    $element.unbind("mouseup", mouseUp);

                    evt.stopPropagation();
                    evt.preventDefault();
                };
            }]
        };
    })

    .directive('resizable', ['$compile', function($compile) {

        return {
            restrict: 'A',
            link: function($scope, element, attrs) {

                // set minimum resize width and height values
                var minWidth = (attrs.minWidth) ? parseInt(attrs.minWidth) : 100;
                var minHeight = (attrs.minHeight) ? parseInt(attrs.minHeight) : 100;

                // create a template that defines the resizable elements
                var template = "<div class='resizable-handle resizable-e' ng-mousedown='onResize($event, &quot;e&quot;)'></div>"
                    + "<div class='resizable-handle resizable-s' ng-mousedown='onResize($event, &quot;s&quot;)'></div>"
                    + "<div class='resizable-handle resizable-se' ng-mousedown='onResize($event, &quot;se&quot;)'></div>";

                // add resizable elements to the parent element
                element.append($compile(template)($scope));

                // resize method
                $scope.onResize = function(evt, direction){

                    var resizeE = direction.includes('e');
                    var resizeS = direction.includes('s');

                    // The top, left of the resizable element
                    var posX, posY;

                    $scope.$root.$broadcast("beginDrag", {
                        x: evt.pageX,
                        y: evt.pageY,
                        config: {

                            dragStarted: function (x, y) {

                                // capture the elements top, left relative to the window
                                posX = element.offset().left;
                                posY = element.offset().top;
                            },

                            dragging: function (x, y) {

                                // resize in the east direction
                                if (resizeE){
                                    var width = x - posX;
                                    element.css({
                                        width: ((minWidth > width) ? minWidth : width) + 'px'
                                    });
                                }

                                // resize in the the south direction
                                if (resizeS){
                                    var height = y - posY;
                                    element.css({
                                        height: ((minHeight > height) ? minHeight : height) + 'px'
                                    });
                                }
                            }
                        }
                    });
                };
            }
        };
    }]);
