'use strict';

angular.module('emr.ui.modal', ['emr.ui.popup', 'emr.ui.shared'])
    .directive('modal', ['$timeout', function($timeout){

        var transitionDelay = 500;

        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/assets/scripts/components/modal/modal.html',
            transclude: true,
            scope: {
                close: "=",
                save: "=",
                position: "="
            },
            link: function ($scope, element, attrs) {

                // invoke the zoom transformation
                var transition = "all " + transitionDelay / 1000 + "s ease";
                var transform = "translate(" + $scope.position.endX + "px," + $scope.position.endY + "px) scale(1)";
                $timeout(
                    function(){
                        element.css({ "-webkit-transition": transition, "-webkit-transform": transform });
                    },
                    30);

                var zoomOut = function(){
                    element.css({ "-webkit-transform": "translate(0, 0) scale(0.01)" });
                };

                $scope.onSave = function(){

                    $scope.save(transitionDelay);
                    zoomOut();
                };

                $scope.onClose = function(){

                    $scope.close(transitionDelay);
                    zoomOut();
                };
            }
        }
    }])
    .factory('modalService', ['$q', '$window', 'popupService', 'sharedService',
        function($q, $window, popupService, sharedService) {

            function ModalService(){

                var defaultModalSize = 0.95;

                this.show = function(options){

                    var deferred = $q.defer();

                    sharedService.getTemplate(options.template, options.templateUrl)
                        .then(function(template) {

                            // wrap the specified template with a modal directive
                            var popupTemplate = "<modal position='position' close='close' save='save'>" + template + "</modal>";

                            // capture / calculate modal start and end positions and dimensions
                            // set default variables
                            var width = $window.innerWidth * defaultModalSize;
                            var height = $window.innerHeight * defaultModalSize;
                            var centerX = $window.innerWidth / 2;
                            var centerY = $window.innerHeight / 2;

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
                            }

                            var position = {
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