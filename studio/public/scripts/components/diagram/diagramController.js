'use strict';

diagramApp
    .controller('diagramNavigationController',
    function($scope, $element, $timeout, diagService, closeDialog, openDiagram, createNewDiagram, deleteExistingDiagram, currentDiagram, close) {

        $scope.pages = [];
        $scope.pageIndex = 0;
        $scope.path = "/";
        $scope.toggleAddNewCategory = false;
        $scope.categories = [""];
        $scope.diagramProperties = resetDiagramProperties();
        $scope.newDiagramProperties = resetNewDiagramProperties();

        var currentPage = 0;
        var previousLength = 0;
        var shiftInc = 0;
        var shift = '';
        var maxNumberOfPages = 3;
        var pageWidth = 250;

        $scope.onAddNewCategory = function() {

            $scope.toggleAddNewCategory = !$scope.toggleAddNewCategory;
        };

        $scope.deleteDiagram = function(item) {

            deleteExistingDiagram(item.name);

            $timeout(function() { loadDiagrams(); }, 300);

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

            if (type != "Container") {

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

        $scope.hideCaret = function(index, currentType) {

            if (currentType != "Create") {

                if (index > 0) {

                    return false;
                }
            }

            return true;
        };

        $scope.setCurrentPage = function(index) {

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

        function removePagesIfRequired() {

            if ($scope.pages.length > 1) {

                if (currentPage < $scope.pageIndex) {

                    for (var i = $scope.pageIndex; i > currentPage; i--) {

                        $scope.pages.splice(i);
                        $scope.pageIndex = $scope.pageIndex - 1;
                    }
                }
            }
        };

        $scope.switchPage = function(item) {

            removePagesIfRequired();

            $scope.pages.push(item);
            $scope.pageIndex = $scope.pageIndex + 1;
        };

        $scope.loadDiagram = function(item) {

            if (item.type === "Diagram") {

                openDiagram(item.name);

                closeDialog();
            }
        };

        $scope.onOpenPropertiesCancel = function() {

            $scope.pages.pop();
            $scope.pageIndex = $scope.pageIndex - 1;
            $scope.toggleAddNewCategory = false;
        };

        $scope.onNavPathItemClick = function(index) {

            for (var i = $scope.pageIndex; i > index; i--) {

                $scope.pages.splice(i);
                $scope.pageIndex = $scope.pageIndex - 1;
            }

        };

        this.close = $scope.close;

        function loadDiagrams() {

            diagService.listDiagrams().then(
                function (data) {

                    $scope.pages = prepareData(data);
                },
                function (code) {

                    // todo: show exception
                    console.log(code);
                }
            );
        };

        function getCurrentPath() {

            var pathItems = [];

            $scope.pages.forEach(function(page){

                if (page.name) {

                    pathItems.push(page.name);
                }
            });

            return pathItems.join("/");
        }

        function prepareData(data) {

            var rootItems = [];

            for (var i = 0; i < data.length; i++) {

                var item = data[i];
                item.type = "Diagram";

                if (item.category === "") {

                    rootItems.push(item);
                }
                else {

                    if ($scope.categories.indexOf(item.category) == -1) {

                        $scope.categories.push(item.category);
                    }

                    var categories = item.category.split("/");

                    var current = null;

                    rootItems.forEach(function(x) {

                        if (x.type === "Container") {

                            if (x.name === categories[0]) {

                                current = x;
                            }
                        }
                    });

                    if (current === null) {

                        current = { "type" : "Container", "items" : [], "name" : categories[0] }

                        rootItems.push(current);
                    }

                    for (var c = 1; c < categories.length; c++) {

                        var next_current = null;

                        current.items.forEach(function(x) {

                            if (x.type === "Container") {

                                if (x.name === categories[c]) {

                                    next_current = x;
                                }
                            }
                        });

                        if (next_current === null) {

                            next_current = { "type" : "Container", "items" : [], "name" : categories[c] }
                            current.items.push(next_current);
                            current.items.sort(sortByName);
                            current.items.sort(sortByType);
                        }

                        current = next_current;
                    }

                    current.items.push(item);
                    current.items.sort(sortByName);
                    current.items.sort(sortByType);
                }
            }

            var diagrams = [];

            rootItems.sort(sortByName);
            rootItems.sort(sortByType);

            var root = {"type" : "Container", "items" : rootItems};

            diagrams.push(root);

            return diagrams;
        }

        function sortByName(a, b) {

            var x = a.name.toLowerCase()
            var y = b.name.toLowerCase();
            return x < y ? -1 : x > y ? 1 : 0;
        }

        function sortByType(a, b) {

            var x = a.type.toLowerCase()
            var y = b.type.toLowerCase();
            return x < y ? -1 : x > y ? 1 : 0;
        }

        function sortBy(a, b) {

            var x = a.toLowerCase()
            var y = b.toLowerCase();
            return x < y ? -1 : x > y ? 1 : 0;
        }

        $scope.activeOption = "";

        $scope.onPropertiesClick = function() {

            $scope.activeOption = "properties";
        };

        $scope.onNewClick = function() {

            $scope.activeOption = "new";
        };

        $scope.onOpenClick = function() {

            $scope.activeOption = "open";
        };

        $scope.onDiagramPropertyChanged = function() {

            $scope.diagramProperties.isDirty = true;
        };

        $scope.onDiagramPropertiesReset = function() {

            resetDiagramProperties();
        };

        $scope.onDiagramPropertiesSave = function() {

            currentDiagram.data.name = $scope.diagramProperties.name;
            currentDiagram.data.description = $scope.diagramProperties.description;
            currentDiagram.data.owner = $scope.diagramProperties.owner;
            currentDiagram.data.targetEnvironment = $scope.diagramProperties.targetEnvironment;
            currentDiagram.data.category = $scope.diagramProperties.category;

            resetDiagramProperties();

            closeDialog();
        }

        function resetDiagramProperties() {

            $scope.diagramProperties = {

                name: currentDiagram.data.name,
                description: currentDiagram.data.description,
                owner: currentDiagram.data.owner,
                targetEnvironment: currentDiagram.data.targetEnvironment,
                category: currentDiagram.data.category,
                isDirty: false
            };
        };

        $scope.onDiagramNewReset = function() {

            resetNewDiagramProperties();
        };

        $scope.onDiagramNewCreate = function() {

            createNewDiagram($scope.newDiagramProperties);

            closeDialog();
        };

        $scope.onNewDiagramPropertyChanged = function() {

            $scope.newDiagramProperties.isDirty = true;
        };

        function resetNewDiagramProperties() {

            $scope.newDiagramProperties = {

                diagramName: "",
                description: "",
                owner: "",
                targetEnvironment: "PYTHON",
                category: "",
                isDirty: false
            };
        };

        function initialize() {

            loadDiagrams();

            resetDiagramProperties();

            resetNewDiagramProperties();

            $scope.onPropertiesClick();
        };

        $scope.getOpenPageStyle = function(index) {

            return {'z-index' : (1500 - index) };
        };

        $scope.getShift = function() {

            if ($scope.pages.length <= maxNumberOfPages) {

                shiftInc = 0;
                shift = 'translateX(0px)';
            }
            else if ($scope.pages.length > previousLength) {

                shiftInc++;
                shift = 'translateX(-' + (pageWidth * shiftInc) + 'px)';
            }
            else if ($scope.pages.length < previousLength) {

                shiftInc = shiftInc - (previousLength - $scope.pages.length);
                shift = 'translateX(-' + (pageWidth * shiftInc) + 'px)';
            }

            previousLength = $scope.pages.length;

            return {'-webkit-transform': shift};
        };

        initialize();

    },
    ['$scope', '$element', 'diagService', 'closeDialog', 'openDiagram', 'createNewDiagram', 'deleteExistingDiagram', 'close'
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

