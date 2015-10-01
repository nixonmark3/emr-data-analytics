'use strict';

angular.module('emr.ui.popup', ['emr.ui.shared', 'ngAnimate'])

    .factory('popupService', ['$document', '$compile', '$controller', '$rootScope', '$q', 'sharedService', '$animate', '$timeout',
        function($document, $compile, $controller, $rootScope, $q, sharedService, $animate, $timeout) {

            // reference the document body
            var body = $document.find('body');

            function PopupService() {

                var append = function(parent, child, isAnimated){
                    if (isAnimated){
                        $animate.enter(child, parent);
                    }
                    else{
                        parent.append(child);
                    }
                };

                var remove = function(element, isAnimated){
                    if (isAnimated){
                        $animate.leave(element);
                    }
                    else{
                        element.remove();
                    }
                };

                this.show = function(options) {

                    var deferred = $q.defer();

                    //  Validate the input parameters.
                    var controllerName = options.controller;
                    if(!controllerName) {
                        deferred.reject("No controller has been specified.");
                        return deferred.promise;
                    }

                    //  Get the actual html of the template.
                    sharedService.getTemplate(options.template, options.templateUrl)
                        .then(function(template) {

                            //  Create a new scope for the popup.
                            var popupScope = $rootScope.$new();

                            // create a close promise
                            var closeDeferred = $q.defer();

                            // create inputs for the controller
                            var inputs = {

                                $scope: popupScope,

                                close: function(result, delay) {
                                    if(delay === undefined || delay === null) delay = 0;
                                    $timeout(function() {

                                        //  Resolve the 'close' promise.
                                        closeDeferred.resolve(result);

                                        // remove backdrop element
                                        if (backdropElement)
                                            remove(backdropElement, isAnimated);

                                        //  cleanup and remove the element from the dom
                                        remove(popupElement, isAnimated);
                                        // $animate.leave(popupElement);
                                        popupScope.$destroy();

                                        inputs.close = null;
                                        deferred = null;
                                        closeDeferred = null;
                                        popup = null;
                                        inputs = null;
                                        popupElement = null;
                                        backdropElement = null;
                                        popupScope = null;

                                    }, delay);
                                }
                            };

                            //  If we have provided any inputs, pass them to the controller.
                            if(options.inputs) {
                                for(var inputName in options.inputs) {
                                    inputs[inputName] = options.inputs[inputName];
                                }
                            }

                            //  create a dom element
                            var popupElementTemplate = angular.element(template);

                            var backdropElement = null;
                            if (options.backdropClass) {
                                // if a backdrop class name has been specified

                                backdropElement = angular.element(
                                    "<div class='" + options.backdropClass + "'></div>");
                            }

                            var isAnimated = (options.isAnimated === undefined || options.isAnimated == null)
                                ? true : options.isAnimated;

                            //  Compile then link the template element, building the actual element.
                            //  Set the $element on the inputs so that it can be injected if required.
                            var popupElement = $compile(popupElementTemplate)(popupScope);
                            inputs.$element = popupElement;

                            //  Create the controller, explicitly specifying the scope to use.
                            var popupController = $controller(controllerName, inputs);

                            //  Finally, append the popup to the dom.
                            if (options.appendElement) {
                                // append to custom append element
                                append(options.appendElement, popupElement, isAnimated);
                                // $animate.enter(popupElement, options.appendElement);
                            } else {
                                // append to body when no custom append element is specified

                                if (backdropElement !== null)
                                    append(body, backdropElement, isAnimated);

                                append(body, popupElement, isAnimated);
                                // $animate.enter(popupElement, body);
                            }

                            var popup = {
                                controller: popupController,
                                scope: popupScope,
                                element: popupElement,
                                backdrop: backdropElement,
                                close: closeDeferred.promise
                            };

                            deferred.resolve(popup);

                        })
                        .then(null, function(error) {
                            deferred.reject(error);
                        });

                    return deferred.promise;
                };

            }

            return new PopupService();
        }]);