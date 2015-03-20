var analyticsApp = angular.module('analyticsApp',
    ['diagramApp',
        'ngRoute',
        'ngSanitize',
        'ngAnimate',
        'ui.bootstrap'])

    .config(function ($routeProvider, $locationProvider) {
        $routeProvider.when('/studio', {
            templateUrl: "/assets/templates/studio.html"
        });
        $routeProvider.otherwise({ redirectTo: '/studio' });
        $locationProvider.html5Mode({
            enabled: true
        });
    })
    .filter('unsafe', ['$sce', function ($sce) {
        return function (val) {
            return $sce.trustAsHtml(val);
        };
    }])
    .controller('analyticsController', function($scope, diagramService) {

        //
        // get data from service
        //

        // load the list of definition blocks
        diagramService.listDefinitions().then(
            function (data) {

                // initialize the pages
                $scope.pages = [{
                    "categories" : data,
                    "definitions" : [],
                    "definition" : null
                }];
            },
            function (code) {

                // todo: show exception
                console.log(code);
            }
        );

        // load the specified diagram
        diagramService.item().then(
            function (data) {

                $scope.diagramViewModel = new viewmodels.diagramViewModel(data);
            },
            function (code) {

                // todo: show exception
                console.log(code);
            }
        );

        //
        // Events
        //

        // fire the event to begin dragging an element
        var beginDragEvent = function(x, y, config){

            $scope.$root.$broadcast("beginDrag", {
                x: x,
                y: y,
                config: config
            });
        };

        // fire the event to create a new block given a definition
        var createBlockEvent = function(x, y, evt, definition){

            $scope.$root.$broadcast("createBlock", {
                x: x,
                y: y,
                evt: evt,
                definition: definition
            });
        };

        var deleteSelectedEvent = function(){

            $scope.$root.$broadcast("deleteSelected");
        };

        var deselectAllEvent = function(){

            $scope.$root.$broadcast("deselectAll");
        };

        var selectAllEvent = function(){

            $scope.$root.$broadcast("selectAll");
        };

        $scope.showNav = false;

        $scope.toggleDiagrams = function(){
           $scope.showNav = !$scope.showNav;
        };

        $scope.showLibrary = false;

        $scope.toggleLibrary = function(){

            $scope.showLibrary = !$scope.showLibrary;
        };

        //
        // Navigation variable and functions
        //

        $scope.draggingGhost = false;

        $scope.ghostPosition = {
           x: 0, y: 0
        };

        // initialize the page index
        $scope.pageIndex = 0;

        $scope.pageHeader = [];

        // set page index
        $scope.setPage = function(index){
            $scope.pageIndex = index;
        };

        // identify whether index is current page
        $scope.isCurrentPage = function(index){
            return $scope.pageIndex === index;
        };

        // page the navigation panel back
        $scope.pageBack = function(){

            $scope.pages.splice($scope.pageIndex);
            $scope.pageIndex = $scope.pageIndex - 1;
            $scope.pageHeader.splice($scope.pageIndex);
        };

        // page the navigation panel forward
        $scope.pageForward = function(item){

            $scope.pageHeader.push(item.name);

            var index = $scope.pageIndex + 1;
            $scope.pages.push({
                    "categories" : item.categories,
                    "definitions" : item.definitions,
                    "definition" : null
                }
            );

            $scope.pageIndex = index;
        };

        // display a definition for configuration
        $scope.show = function(item){

            $scope.pageHeader.push(item.name);

            var index = $scope.pageIndex + 1;
            $scope.pages.push({
                    "categories" : [],
                    "definitions" : [],
                    "definition" : item
                }
            );

            $scope.pageIndex = index;
        };

        $scope.mouseDown = function(evt, item){

            beginDragEvent(evt.pageX, evt.pageY, {

                dragStarted: function (x, y) {
                    // flip dragging ghost flag
                    $scope.draggingGhost = true;
                },

                dragging: function (x, y) {
                    // update ghost position
                    $scope.ghostPosition = { x: x, y: y };
                },

                dragEnded: function (x, y, evt) {

                    $scope.ghostPosition = { x: x, y: y };

                    createBlockEvent(x, y, evt, item);

                    $scope.$apply($scope.draggingGhost = false);
                }

            });

            evt.stopPropagation();
            evt.preventDefault();
        };
    });

