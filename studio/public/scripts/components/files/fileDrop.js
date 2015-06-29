'use strict';

angular.module('emr.ui.files', [])

    .directive('fileDrop', ['diagramService', function(diagramService) {

        return {
            restrict: 'A',
            link: function ($scope, element, attrs) {

                element[0].addEventListener('dragover', function (evt) {
                    element.addClass("dragging-file");
                    evt.preventDefault();
                    evt.stopPropagation();
                }, false);
                element[0].addEventListener('dragenter', function (evt) {
                    evt.preventDefault();
                    evt.stopPropagation();
                }, false);
                element[0].addEventListener('dragleave', function () {
                    element.removeClass("dragging-file");
                }, false);

                element[0].addEventListener('drop', function (evt) {

                    element.removeClass("dragging-file");

                    var file = evt.dataTransfer.files[0];

                    diagramService.upload(file).then(
                        function(result){   // on success

                            if ($scope.onFileDrop)
                                $scope.onFileDrop(result, evt);
                        },
                        function(code){     // on error

                        },
                        function(evt){      // on notification

                            if (evt.lengthComputable) {
                                var complete = (evt.loaded / evt.total * 100 | 0);
                                console.log(complete);
                            }
                        }
                    );

                    evt.preventDefault();
                    evt.stopPropagation();

                }, false);
            }
        }
    }]);
