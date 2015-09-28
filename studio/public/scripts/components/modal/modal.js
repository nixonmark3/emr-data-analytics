'use strict';

angular.module('emr.ui.modal', ['emr.ui.popup', 'emr.ui.shared'])
    .directive('modal', ['$timeout', function($timeout){

        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/assets/scripts/components/modal/modal.html',
            transclude: true,
            scope: {
                close: "=",
                save: "=",
                position: "=",
                config: "="
            },
            link: function ($scope, element, attrs) {

                var transition;
                switch($scope.position.animation.type){
                    case 'fadeIn':

                        element.css({ "opacity": "0" });
                        transition = "opacity " + $scope.position.animation.durationIn / 1000 + "s ease";
                        break;
                    default:

                        // zoom
                        element.css({ "-webkit-transform": "scale(" + $scope.position.initialScale + ")" });
                        transition = "transform " + $scope.position.animation.durationIn / 1000 + "s ease";
                        break;
                }

                var backdrop;
                if ($scope.config.backdropClass)
                    backdrop = angular.element(document.getElementsByClassName($scope.config.backdropClass));

                $timeout(
                    function(){
                        element.css({
                            "-webkit-transition": transition,
                            "-webkit-transform": "translate(" + $scope.position.endX + "px," + $scope.position.endY + "px) scale(1)",
                            "opacity": "1"
                        });

                        if (backdrop)
                            backdrop.css({
                                "-webkit-transition": "opacity " + $scope.position.animation.durationIn / 1000 + "s ease",
                                "opacity": "0.6"
                            });
                    },
                    10);

                function zoomOut(){
                    element.css({
                        "-webkit-transition": "transform " + $scope.position.animation.durationOut / 1000 + "s ease",
                        "-webkit-transform": "translate(0, 0) scale(" + $scope.position.initialScale + ")"
                    });

                    if (backdrop)
                        backdrop.css({
                            "-webkit-transition": "opacity " + $scope.position.animation.durationOut / 1000 + "s ease",
                            "opacity": "0"
                        });
                }

                $scope.onSave = function(){

                    $scope.save($scope.position.animation.durationOut);
                    zoomOut();
                };

                $scope.onClose = function(){

                    $scope.close($scope.position.animation.durationOut);
                    zoomOut();
                };
            }
        }
    }])
    .factory('modalService', ['$q', '$window', 'popupService', 'sharedService',
        function($q, $window, popupService, sharedService) {

            function ModalService(){

                var defaultModalSize = 0.90;

                this.show = function(options){

                    var deferred = $q.defer();

                    sharedService.getTemplate(options.template, options.templateUrl)
                        .then(function(template) {

                            // wrap the specified template with a modal directive
                            var popupTemplate = "<modal config='config' position='position' close='close' save='save'>" + template + "</modal>";

                            // capture / calculate modal start and end positions and dimensions
                            // set default variables
                            var width = $window.innerWidth * defaultModalSize,
                                height = $window.innerHeight * defaultModalSize,
                                centerX = $window.innerWidth / 2,
                                centerY = $window.innerHeight / 2,
                                initialScale = 0.01,
                                animation = {
                                    type: 'zoom',
                                    durationIn: 500,
                                    durationOut: 500
                                };

                            // capture overrides
                            if (options.position){
                                if (options.position.width)
                                    width = options.position.width;

                                if (options.position.height)
                                    height = options.position.height;

                                if (options.position.centerX)
                                    centerX = options.position.centerX;

                                if (options.position.centerY)
                                    centerY = options.position.centerY;

                                if (options.position.initialHeight)
                                    initialScale = options.position.initialHeight / height;
                            }

                            if (options.animation){
                                if (options.animation.type)
                                    animation.type = options.animation.type;
                                if (options.animation.durationIn)
                                    animation.durationIn = options.animation.durationIn;
                                if (options.animation.durationOut)
                                    animation.durationOut = options.animation.durationOut;
                            }

                            var position = {
                                animation: animation,
                                initialScale: initialScale,
                                width: width,
                                height: height,
                                startX: centerX - (width / 2),
                                startY: centerY - (height / 2)
                            };

                            position.endX = (($window.innerWidth / 2) - (width / 2)) - position.startX;
                            position.endY = (($window.innerHeight / 2) - (height / 2)) - position.startY;

                            // build popup options
                            var popupOptions = {
                                template: popupTemplate,
                                controller: options.controller,
                                isAnimated: false,
                                inputs: options.inputs || {}
                            };
                            popupOptions.inputs.position = position;
                            popupOptions.inputs.config = options.config;

                            // check whether a backdropClass was specified
                            if (options.config.backdropClass){
                                popupOptions.backdropClass = options.config.backdropClass;
                            }

                            // show modal using the popup service
                            popupService.show(popupOptions).then(function (popup) {
                                deferred.resolve(popup);
                            });

                        });

                    return deferred.promise;
                }
            }

            return new ModalService();
        }]);