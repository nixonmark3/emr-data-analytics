'use strict';

diagramApp
    .controller('diagramNavigationController',
    ['$scope', '$element', 'diagService', 'closeDialog', 'close',
        function($scope, $element, diagService, closeDialog, close) {

            $scope.pages = [];
            $scope.pageIndexs = [0];
            $scope.pageIndex = 0;
            $scope.showDiagramInformation = false;
            $scope.path = "/";

            var lastPage = 0;
            var currentPage = 0;

            $scope.deleteDiagram = function() {

                console.log("delete diagram!")
            };

            $scope.copyDiagram = function() {

                console.log("copy diagram!")
            };

            $scope.exportDiagram = function() {

                console.log("export diagram!")
            };

            $scope.isHighLighted = function(name) {

                var highlighted = false;

                if ($scope.pages.length > 1) {

                    for (var i = 1; i < $scope.pages.length; i++) {

                        if ($scope.pages[i].name === name) {

                            highlighted = true;
                        }
                    }
                }

                return highlighted;
            };

            $scope.isDiagramPage = function(type) {

                if (type === "Diagram") {

                    return true;
                }

                return false;
            }

            $scope.hideFileType = function(index, currentType, requestedType) {

                var hide = true;

                if (index > 0) {

                    if (currentType === requestedType) {

                        hide = false;
                    }
                }

                return hide;
            };

            $scope.isFirstPage = function(index) {

                if (index === 0) {

                    return true;
                }

                return false;
            };

            $scope.hideCaret = function(index) {

                if (index > 1) {

                    return false;
                }

                return true;
            };

            $scope.setCurrentPage = function(index) {

                lastPage = currentPage;
                currentPage = index;
            }

            $scope.isCurrentPage = function(index) {

                if ($scope.pageIndex.indexOf(index) > -1) {

                    return true;
                }

                return false;
            };

            $scope.close = function() {

                close();
            };

            $scope.switchPage = function(item) {

                $scope.showDiagramInformation = false;

                if ($scope.pageIndexs.length > 1) {

                    if (lastPage === currentPage) {

                        var i = $scope.pageIndexs.pop();
                        $scope.pages.splice(i);
                        $scope.pageIndex = $scope.pageIndex - 1;
                    }
                    else {

                        if (currentPage < lastPage) {

                            for (var i = $scope.pageIndex; i > currentPage; i--) {

                                $scope.pages.splice(i);
                                $scope.pageIndex = $scope.pageIndex - 1;
                            }
                        }

                        lastPage = currentPage;
                    }
                }

                var index = $scope.pageIndex + 1;
                $scope.pages.push(item);
                $scope.pageIndexs.push(index);
                $scope.pageIndex = index;
            };

            $scope.loadDiagram = function(item) {

                if (item.type === "Diagram") {

                    console.log('Open diagram!');
                    closeDialog();
                }
            };

            $scope.createNewDiagram = function() {

                console.log('Create new diagram!');
                closeDialog();
            };

            this.close = $scope.close;

            function loadDiagrams() {

                diagService.listDiagrams().then(
                    function (data) {

                        console.log(data);

                        $scope.pages = data;
                    },
                    function (code) {

                        // todo: show exception
                        console.log(code);
                    }
                );
            };

            loadDiagrams();
        }
    ])
    .controller('diagramConfigController',
    ['$scope', '$element', 'diagram', 'close',
        function($scope, $element, diagram, close) {

            $scope.diagram = diagram;

            $scope.close = function(){

                close(diagram);
            };

            this.close = $scope.close;
        }
    ])
    .controller('libraryController',
    ['$scope', '$element', 'nodes', 'getConfigBlock', 'loadSources', 'close',
        function($scope, $element, nodes, getConfigBlock, loadSources, close) {

            $scope.nodes = nodes;

            $scope.getConfigBlock = getConfigBlock;

            $scope.loadSources = loadSources;

            $scope.savable = false;

            $scope.save = function(block){

                close(block);
            };

            $scope.cancel = function(){

                close();
            };
        }
    ]);

