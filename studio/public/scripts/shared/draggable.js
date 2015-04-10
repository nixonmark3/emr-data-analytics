
angular.module('draggableApp', [])
    .controller('draggableController', ['$scope', '$element', '$attrs', function($scope, $element, $attrs){

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
                if (config.clicked) {
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
    }])
    .directive('draggable', function () {
        return {
            restrict: 'A',
            controller: 'draggableController'
        };
    });

