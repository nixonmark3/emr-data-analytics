'use strict';

angular.module('emr.ui.panel', [])

    .directive('panel', [function() {

        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/assets/scripts/components/panel/panel.html',
            transclude: true,
            scope: {
                config: "=",
                onCancel: "=",
                onSave: "=",
                title: "="
            },
            link: function ($scope, element, attrs) {

                // initialize the minimize variables
                $scope.isMinimized = false;
                var maximizedHeight = $scope.config.height;
                var minimizedHeight = 42;

                // set minimum resize width and height values
                var minWidth = (attrs.minWidth) ? parseInt(attrs.minWidth) : 100;
                var minHeight = (attrs.minHeight) ? parseInt(attrs.minHeight) : 100;

                /* Scope Level methods */

                $scope.cancel = function(){
                    $scope.onCancel();
                };

                // move the panel
                $scope.onDrag = function(evt){

                    var originX, originY;

                    $scope.$root.$broadcast("beginDrag", {
                        x: evt.pageX,
                        y: evt.pageY,
                        config: {

                            dragStarted: function (x, y) {

                                // capture the parent's offset from the window
                                originX = element.offsetParent().offset().left;
                                originY = element.offsetParent().offset().top;
                            },

                            dragging: function (x, y) {

                                // set the panel's new position
                                $scope.config.x = x - originX - 20;
                                $scope.config.y = y - originY - 20;
                            }
                        }
                    });
                };

                // resize the panel
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
                                    $scope.config.width = ((minWidth > width) ? minWidth : width);
                                }

                                // resize in the the south direction
                                if (resizeS){
                                    var height = y - posY;
                                    $scope.config.height = ((minHeight > height) ? minHeight : height);
                                }
                            }
                        }
                    });
                };

                // verify that the title has been changed
                $scope.onTitleChange = function(){

                    $scope.config.isDirty = true;
                };

                $scope.save = function(){
                    $scope.onSave();
                };

                $scope.toggleMinimize = function(){

                    if ($scope.isMinimized) {
                        $scope.config.height = maximizedHeight;
                        $scope.isMinimized = false;
                    }
                    else {
                        $scope.isMinimized = true;
                        maximizedHeight = $scope.config.height;
                        $scope.config.height = minimizedHeight;
                    }

                };
            }
        }
    }]);
