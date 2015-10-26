'use strict';

var blockDataViewerApp = angular.module('blockDataViewerApp', [])
    .directive('blockDataViewer', function ($timeout) {

        return {
            restrict: 'E',
            replace: false,
            templateUrl: '/assets/scripts/components/blockDataViewer/blockDataViewer.html',
            scope: {
                results: "="
            },
            link: function ($scope, element, attrs) {


            }
        }
    });

