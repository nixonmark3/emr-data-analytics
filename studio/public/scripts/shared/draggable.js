
angular.module('draggableApp', [])
    .factory('draggableService', ['$document', function ($document) {

        var _element = $document;

        var threshold = 5;

        return {
            registerElement: function(element) {

                _element = element;
            },

            startDrag: function (evt, config) {

                var dragging = false;
                var x = evt.pageX;
                var y = evt.pageY;

                var mouseMove = function (evt) {

                    if (!dragging) {
                        if (Math.abs(evt.pageX - x) > threshold ||
                            Math.abs(evt.pageY - y) > threshold) {
                            dragging = true;

                            if (config.dragStarted) {
                                config.dragStarted(x, y, evt);
                            }

                            if (config.dragging) {
                                // First 'dragging' call to take into account that we have
                                // already moved the mouse by a 'threshold' amount.
                                config.dragging(evt.pageX, evt.pageY, evt);
                            }
                        }
                    }
                    else {
                        if (config.dragging) {
                            config.dragging(evt.pageX, evt.pageY, evt);
                        }

                        x = evt.pageX;
                        y = evt.pageY;
                    }
                };

                var released = function () {

                    if (dragging) {
                        if (config.dragEnded) {
                            config.dragEnded();
                        }
                    }
                    else {
                        if (config.clicked) {
                            config.clicked();
                        }
                    }

                    _element.unbind("mousemove", mouseMove);
                    _element.unbind("mouseup", mouseUp);
                };

                var mouseUp = function (evt) {

                    released();

                    evt.stopPropagation();
                    evt.preventDefault();
                };

                // configure
                _element.mousemove(mouseMove);
                _element.mouseup(mouseUp);

                evt.stopPropagation();
                evt.preventDefault();
            }
        }
    }])
    .directive('draggable', function () {
        return {
            restrict: 'A',
            controller: function($scope, $element, $attrs, draggableService) {

                //
                // Register the directives element as the draggable
                //
                draggableService.registerElement($element);

            }
        };
    });

