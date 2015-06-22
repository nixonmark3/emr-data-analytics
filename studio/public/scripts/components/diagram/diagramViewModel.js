
var viewmodels = viewmodels || {};

viewmodels.connectorViewModel = function (data, x, y, parent) {

    //
    // private properties
    //
    this.data = data;
    this._parent = parent;
    this._x = x;
    this._y = y;
    this._normalRadius = 6;
    this._expandedRadius = 12;

    //
    // public properties
    //
    this.radius = this._normalRadius;
    this.showName = false;

    //
    // Set the connector radius to normal.
    //
    this.normalRadius = function () {
        this.radius = this._normalRadius;
    };

    //
    // Set the connector radius to expanded.
    //
    this.expandRadius = function () {
        this.radius = this._expandedRadius;
    };

    //
    // The name of the connector.
    //
    this.name = function () {
        return this.data.name;
    };

    //
    // X coordinate of the connector.
    //
    this.x = function () {
        return this._x;
    };

    //
    // Y coordinate of the connector.
    //
    this.y = function () {
        return this._y;
    };

    //
    // The parent block that the connector is attached to.
    //
    this.parent = function () {
        return this._parent;
    };
};

viewmodels.blockViewModel = function (data) {

    //
    // reference the block data
    //
    this.data = data;

    //
    // Hardcoded height of each block
    //
    this._blockHeight = 80;

    //
    // Set to true when the block is selected
    //
    this._selected = false;

    //
    // Set to true when the mouse is hovering over the block
    //
    this._hover = false;

    //
    // block type
    //
    this.definition = function () {
        return this.data.definition;
    };

    //
    // Enumeration repressenting the type of definition
    //
    this.definitionType = function(){
        return this.data.definitionType;
    };

    //
    // Name of the block
    //
    this.name = function () {
        return this.data.name;
    };

    //
    // Unique Name of the block
    //
    this.id = function () {
        return this.data.id;
    };

    //
    // X coordinate of the block
    //
    this.x = function () {
        return this.data.x;
    };

    //
    // Y coordinate of the block
    //
    this.y = function () {
        return this.data.y;
    };

    //
    // Width of the block
    //
    this.width = function () {
        return this.data.w;
    };

    //
    // Height of the block
    //
    this.height = function () {
        return this._blockHeight;
    };

    //
    // Flag block as hovering
    //
    this.hover = function () {
        this._hover = true;
    };

    //
    // Flag block as not hovering
    //
    this.leaveHover = function () {
        this._hover = false;
    };

    //
    // Is the block hovering
    //
    this.hovering = function(){
        return this._hover;
    };

    //
    // Select this block
    //
    this.select = function () {
        this._selected = true;
    };

    //
    // Deselect this block
    //
    this.deselect = function () {
        this._selected = false;
    };

    //
    // Toggle the selection state of this block
    //
    this.toggleSelected = function () {
        this._selected = !this._selected;
    };

    //
    // Returns true if the node is selected.
    //
    this.selected = function () {
        return this._selected;
    };

    this.settings = function(){
        return "test";
    };

    this.advanceProgress = function(){

        this.data.state = (this.data.state + 1) % 5;
    };

    this.updateProgress = function(state) {

        setTimeout(this.data.state = state, 500);
    };

    this.progressText = function(){

        var text;
        switch(this.data.state){
            case 0: // configuring
                text = "\uf040";
                break;
            case 1: // ready
                text = "\uf087";
                break;
            case 2: // executing
                text = "\uf085";
                break;
            case 3: // complete
                text = "\uf00c";
                break;
            case 4: // error
                text = "\uf12A";
                break;
        }

        return text;
    };

    this.stateBgStroke = function(){

        var text;
        switch(this.data.state){
            case 0: // configuring
                text = "#fdd0a2";
                break;
            case 1: // ready
                text = "#dadaeb";
                break;
            case 2: // executing
                text = "#c7e9c0";
                break;
            case 3: // complete
                text = "#aec7e8";
                break;
            case 4: // error
                text = "#ff9896";
                break;
        }

        return text;
    };

    this.stateFgStroke = function(){

        var text;
        switch(this.data.state){
            case 0: // configuring
                text = "#ff7f0e";
                break;
            case 1: // ready
                text = "#9467bd";
                break;
            case 2: // executing
                text = "#2ca02c";
                break;
            case 3: // complete
                text = "#1f77b4";
                break;
            case 4: // error
                text = "#d62728";
                break;
        }

        return text;
    };

    this.stateStrokeOffset = function(){

        var offset;
        switch(this.data.state){
            case 0: // configuring
                offset = 65.625;
                break;
            case 1: // ready
                offset = 43.75;
                break;
            case 2: // executing
                offset = 21.875;
                break;
            case 3: // complete
                offset = 0;
                break;
            case 4: // error
                offset = 0;
                break;
        }

        return offset;
    };

    //
    //
    //
    this.stateClass = function(){

        var result;
        switch(this.data.state){
            case 0:
                result = "block-config";
                break;
            case 1:
                result = "block-ready";
                break;
            case 2:
                result = "block-executing";
                break;
            case 3:
                result = "block-complete";
                break;
            case 4:
                result = "block-error";
                break;
        }

        return result;
    };

    //
    // retrieve parameter by name
    //
    this.getParameter = function(name){

        for(var p = 0; p < this.data.parameters.length; p++)
            if (this.data.parameters[p].name == name)
                return this.data.parameters[p];
    };

    // connector functions

    //
    // count the number of input and output connectors
    //
    this.inputConnectorCount = this.data.inputConnectors.length;

    this.outputConnectorCount = this.data.outputConnectors.length;

    //
    // calculate the specified connectors x-coordinate
    //
    this.calculateConnectorX = function (connectorIndex, isInput) {

        var count = (isInput) ? this.inputConnectorCount : this.outputConnectorCount;
        var spacing = this.width() / (count + 1);

        return ((connectorIndex + 1) * spacing);
    };

    //
    // create view models for specified connectors
    //
    this.createConnectorViewModels = function (connectorsData, y, parent, isInput) {
        var viewModels = [];

        if (connectorsData) {

            for (var i = 0; i < connectorsData.length; ++i) {

                var _connectorViewModel =
                    new viewmodels.connectorViewModel(connectorsData[i],
                        this.calculateConnectorX(i, isInput),
                        y,
                        parent);

                viewModels.push(_connectorViewModel);
            }
        }

        return viewModels;
    };

    //
    // create view models for input and output connectors
    //
    this.inputConnectors = this.createConnectorViewModels(this.data.inputConnectors,
        0,
        this,
        true);

    this.outputConnectors = this.createConnectorViewModels(this.data.outputConnectors,
        this.height(),
        this,
        false);
};

viewmodels.wireViewModel = function (data, parent, sourceConnector, targetConnector) {

    this.data = data;
    this.parent = parent;
    this.source = sourceConnector;
    this.target = targetConnector;

    // Set to true when the connection is selected.
    this._selected = false;

    this.sourceConnectorX = function(){
        return this.source.x();
    };

    this.sourceConnectorY = function(){
        return this.source.y();
    };

    this.sourceCoordX = function() {
        return this.source.parent().x() + this.sourceConnectorX();
    };

    this.sourceCoordY = function() {
        return this.source.parent().y() + this.sourceConnectorY();
    };

    this.sourceCoord = function() {
        return {
            x: this.sourceCoordX(),
            y: this.sourceCoordY()
        };
    };

    this.sourceTangentX = function() {
        return this.parent.computeWireSourceTangentX(this.sourceCoord(), this.targetCoord());
    };

    this.sourceTangentY = function () {
        return this.parent.computeWireSourceTangentY(this.sourceCoord(), this.targetCoord());
    };

    this.targetConnectorX = function(){
        return this.target.x();
    };

    this.targetConnectorY = function(){
        return this.target.y();
    };

    this.targetCoordX = function() {
        return this.target.parent().x() + this.targetConnectorX();
    };

    this.targetCoordY = function() {
        return this.target.parent().y() + this.targetConnectorY();
    };

    this.targetCoord = function() {
        return {
            x: this.targetCoordX(),
            y: this.targetCoordY()
        };
    };

    this.targetTangentX = function() {
        return this.parent.computeWireTargetTangentX(this.sourceCoord(),
            this.targetCoord());
    };

    this.targetTangentY = function() {
        return this.parent.computeWireTargetTangentY(this.sourceCoord(),
            this.targetCoord());
    };

    //
    // Select the wire.
    //
    this.select = function() {
        this._selected = true;
    };

    //
    // Deselect the wire.
    //
    this.deselect = function() {
        this._selected = false;
    };

    //
    // Toggle the selection state of the connection.
    //
    this.toggleSelected = function() {
        this._selected = !this._selected;
    };

    //
    // Returns true if the connection is selected.
    //
    this.selected = function() {
        return this._selected;
    };
};

viewmodels.diagramViewModel = function(data) {

    // reference the diagram data model
    this.data = data;

    // tracks the current set of block names
    var blockNames = {};

    this.createBlockViewModels = function (blocksData) {
        var models = [];

        if (blocksData) {
            for (var i = 0; i < blocksData.length; ++i) {

                blockNames[blocksData[i].name] = true;

                models.push(new viewmodels.blockViewModel(blocksData[i]));
            }
        }

        return models;
    };

    this.createDiagramViewModels = function(diagramsData){
        var models = [];

        if (diagramsData){
            for (var i = 0; i < diagramsData.length; ++i) {

                models.push(new viewmodels.diagramViewModel(diagramsData[i]));
            }
        }

        return models;
    };

    this.computeConnectorPos = function (block, connectorIndex, inputConnector) {
        return {
            x: block.x() + block.calculateConnectorX(connectorIndex, inputConnector),
            y: block.y() + (inputConnector ? 0 : block.height())
        };
    };

    //
    // Find a specific block within this diagram
    //
    this.findBlock = function (id) {

        for (var i = 0; i < this.blocks.length; ++i) {
            var block = this.blocks[i];
            if (block.data.id == id) {
                return block;
            }
        }

        throw new Error("Failed to find block " + name);
    };

    //
    // Find the specified nested diagram
    //
    this.findDiagram = function(name){
        for (var i = 0; i < this.diagrams.length; ++i) {
            var diagram = this.diagrams[i];
            if (diagram.data.name == name) {
                return diagram;
            }
        }

        throw new Error("Failed to find block " + name);
    };

    //
    // Find a specific input connector within this diagram
    //
    this.findInputConnector = function (name, connectorIndex) {

        var block = this.findBlock(name);

        if (!block.inputConnectors || block.inputConnectors.length <= connectorIndex) {
            throw new Error("Block " + name + " has invalid input connectors.");
        }

        return block.inputConnectors[connectorIndex];
    };

    //
    // Find a specific output connector within the chart.
    //
    this.findOutputConnector = function (name, connectorIndex) {

        var block = this.findBlock(name);

        if (!block.outputConnectors || block.outputConnectors.length <= connectorIndex) {
            throw new Error("Block " + name + " has invalid output connectors.");
        }

        return block.outputConnectors[connectorIndex];
    };

    //
    // Create a view model for connection from the data model.
    //
    this.createWireViewModel = function(wireData) {

        var sourceConnector = this.findOutputConnector(wireData.from_node,
            wireData.from_connectorIndex);

        var destConnector = this.findInputConnector(wireData.to_node,
            wireData.to_connectorIndex);

        return new viewmodels.wireViewModel(wireData,
            this,
            sourceConnector,
            destConnector);
    };

    this.createWireViewModels = function (wiresData) {

        var models = [];

        if (wiresData) {
            for (var i = 0; i < wiresData.length; ++i) {
                models.push(this.createWireViewModel(wiresData[i]));
            }
        }

        return models;
    };

    // create a view model for each block
    this.blocks = this.createBlockViewModels(this.data.blocks);

    // create a view model for each wire
    this.wires = this.createWireViewModels(this.data.wires);

    // create a view model for each nested diagram
    this.diagrams = this.createDiagramViewModels(this.data.diagrams);

    //
    // gets the diagram's mode
    //
    this.mode = function(){
        return this.data.mode;
    };

    //
    // Bezier curve calculations
    //

    this.computeWireSourceTangentX = function (pt1, pt2) {

        return pt1.x;
    };

    this.computeWireSourceTangentY = function (pt1, pt2) {

        return pt1.y + this.computeWireTangentOffset(pt1, pt2);
    };

    this.computeWireSourceTangent = function(pt1, pt2) {
        return {
            x: this.computeWireSourceTangentX(pt1, pt2),
            y: this.computeWireSourceTangentY(pt1, pt2)
        };
    };

    this.computeWireTargetTangentX = function (pt1, pt2) {

        return pt2.x;
    };

    this.computeWireTargetTangentY = function (pt1, pt2) {

        return pt2.y - this.computeWireTangentOffset(pt1, pt2);
    };

    this.computeWireTargetTangent = function(pt1, pt2) {
        return {
            x: this.computeWireTargetTangentX(pt1, pt2),
            y: this.computeWireTargetTangentY(pt1, pt2)
        };
    };

    this.computeWireTangentOffset = function (pt1, pt2) {

        return (pt2.y - pt1.y) / 2;
    };

    //
    // Create a view model for a new wire
    //
    this.createWire = function (startConnector, endConnector) {

        var wiresData = this.data.wires;
        if (!wiresData) {
            wiresData = this.data.wires = [];
        }

        var wiresViewModels = this.wires;
        if (!wiresViewModels) {
            wiresViewModels = this.wires = [];
        }

        var startBlock = startConnector.parent();
        var startConnectorIndex = startBlock.outputConnectors.indexOf(startConnector);
        var startConnectorType = 'output';
        if (startConnectorIndex == -1) {
            startConnectorIndex = startBlock.inputConnectors.indexOf(startConnector);
            startConnectorType = 'input';
            if (startConnectorIndex == -1) {
                throw new Error("Failed to find source connector within either inputConnectors or outputConnectors of source block.");
            }
        }

        var endBlock = endConnector.parent();
        var endConnectorIndex = endBlock.inputConnectors.indexOf(endConnector);
        var endConnectorType = 'input';
        if (endConnectorIndex == -1) {
            endConnectorIndex = endBlock.outputConnectors.indexOf(endConnector);
            endConnectorType = 'output';
            if (endConnectorIndex == -1) {
                throw new Error("Failed to find dest connector within inputConnectors or outputConnectors of dest block.");
            }
        }

        if (startConnectorType == endConnectorType) {
            throw new Error("Failed to create wire. Only output to input wires are allowed.")
        }

        if (startBlock == endBlock) {
            throw new Error("Failed to create wire. Cannot link a block with itself.")
        }

        var wireDataModel = {
            from_node: (startConnectorType == 'output') ? startBlock.data.id : endBlock.data.id,
            from_connectorIndex: (startConnectorType == 'output') ? startConnectorIndex : endConnectorIndex,
            from_connector: (startConnectorType == 'output') ? startConnector.name() : endConnector.name(),
            to_node: (startConnectorType == 'output') ? endBlock.data.id : startBlock.data.id,
            to_connectorIndex: (startConnectorType == 'output') ? endConnectorIndex : startConnectorIndex,
            to_connector: (startConnectorType == 'output') ? endConnector.name() : startConnector.name()
        };

        wiresData.push(wireDataModel);

        var outputConnector = startConnectorType == 'output' ? startConnector : endConnector;
        var inputConnector = startConnectorType == 'output' ? endConnector : startConnector;

        var wireViewModel = new viewmodels.wireViewModel(wireDataModel,
            this,
            outputConnector,
            inputConnector);
        wiresViewModels.push(wireViewModel);
    };

    //
    // Add a block to the view model.
    //
    this.createBlock = function (configBlock) {

        var block = getDataBlock(configBlock);

        if (!this.data.blocks) {
            this.data.blocks = [];
        }

        // capture new block name
        blockNames[block.name] = true;

        //
        // Update the data model.
        //
        this.data.blocks.push(block);

        //
        // Update the view model.
        //
        this.blocks.push(new viewmodels.blockViewModel(block));
    };

    this.updateBlock = function(configBlock){

        var block = this.findBlock(configBlock.id);

        if ((configBlock.name) && (!blockNames[configBlock.name])) {
            block.data.name = configBlock.name;
            blockNames[configBlock.name] = true;
        }

        var configured = true;
        var dirty = false;
        configBlock.parameters.forEach(function(configParameter){

            if (!configParameter.collected)
                configured = false;

            if (configParameter.dirty)
                dirty = true;

            var parameter = block.getParameter(configParameter.name());
            parameter.value = configParameter.value;
            parameter.collected = configParameter.collected;
        });

        if (dirty && configured)
            block.data.state = 1;
        else if (!configured)
            block.data.state = 0;
    };

    this.getBlockDescription = function(x, y, definition){

        return {
            name: this.generateBlockName(definition.name()),
            id: generateUniqueName(),
            definition: definition.name(),
            x: x,
            y: y
        };
    };

    var getDataBlock = function(configBlock){

        return new DataBlock(configBlock);
    };

    var DataBlock = function(configBlock){

        this.name = configBlock.name;
        this.id = configBlock.id;
        this.definition = configBlock.definition.name();
        this.definitionType = configBlock.definition.definitionType();
        this.state = 0;
        this.w = configBlock.definition.w();
        this.x = configBlock.x;
        this.y = configBlock.y;
        this.inputConnectors = angular.copy(configBlock.definition.inputs()); // todo copy over only what we need from definition
        this.outputConnectors = angular.copy(configBlock.definition.outputs()); // todo copy over only what we need from definition

        var parameters = [];
        var configured = true;
        configBlock.parameters.forEach(function(parameter){

            if (!parameter.collected)
                configured = false;

            parameters.push({
                name:parameter.name(),
                type:parameter.type(),
                value:parameter.value,
                collected:parameter.collected
            });
        });

        if (configured)
            this.state = 1;

        this.parameters = parameters;
    };

    this.generateBlockName = function(definitionName){

        // generate unique block name
        var index = 1;
        var name;
        do{
            name = definitionName + index;
            index++;
        }
        while(name in blockNames);

        return name;
    };

    var generateUniqueName = function() {

        var delim = "-";

        function S4() {
            return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
        }

        return (S4() + S4() + delim + S4() + delim + S4() + delim + S4() + delim + S4() + S4() + S4());
    };

    //
    // Select all blocks and connections in the chart.
    //
    this.selectAll = function () {

        var selectionCount = 0;

        var blocks = this.blocks;
        for (var i = 0; i < blocks.length; ++i) {
            var block = blocks[i];
            selectionCount++;
            block.select();
        }

        var wires = this.wires;
        for (var j = 0; j < connections.length; ++j) {
            var wire = wires[i];
            wire.select();
        }

        return selectionCount;
    };

    //
    // Deselect all blocks and wires in the diagram, return the number of selected blocks (0)
    //
    this.deselectAll = function () {

        var blocks = this.blocks;
        for (var i = 0; i < blocks.length; ++i) {
            var block = blocks[i];
            block.deselect();
        }

        var wires = this.wires;
        for (var j = 0; j < wires.length; ++j) {
            var wire = wires[j];
            wire.deselect();
        }

        return 0;
    };

    this.selectBlock = function(block){

        this.deselectAll();
        block.select();

        return 1;
    };

    this.deleteBlock = function(block){

        this.selectBlock(block);
        this.deleteSelected();
    };

    //
    // Update the location of the block and its connectors.
    //
    this.updateSelectedBlocksLocation = function (deltaX, deltaY) {

        var selectedBlocks = this.getSelectedBlocks();

        for (var i = 0; i < selectedBlocks.length; ++i) {
            var block = selectedBlocks[i];
            block.data.x += deltaX;
            block.data.y += deltaY;
        }
    };

    //
    // Handle mouse click on a particular block.
    //
    this.onBlockClicked = function (block) {

        var selectionCount = this.selectBlock(block);

        // Move node to the end of the list so it is rendered after all the other.
        // This is the way Z-order is done in SVG.

        var blockIndex = this.blocks.indexOf(block);
        if (blockIndex == -1) {
            throw new Error("Failed to find block in view model!");
        }
        this.blocks.splice(blockIndex, 1);
        this.blocks.push(block);

        return selectionCount;
    };

    //
    // Handle mouse down on a wire.
    //
    this.onWireMouseDown = function (wire, ctrlKey) {

        if (ctrlKey) {
            wire.toggleSelected();
        }
        else {
            this.deselectAll();
            wire.select();
        }
    };

    //
    // Delete all blocks and wires that are selected.
    //
    this.deleteSelected = function () {

        var newBlockViewModels = [];
        var newBlocks = [];

        var deletedBlocks = [];

        //
        // Sort blocks into:
        //		blocks to keep and
        //		blocks to delete.
        //

        for (var blockIndex = 0; blockIndex < this.blocks.length; ++blockIndex) {

            var block = this.blocks[blockIndex];
            if (!block.selected()) {
                // Only retain non-selected nodes.
                newBlockViewModels.push(block);
                newBlocks.push(block.data);
            }
            else {
                // Keep track of blocks that were deleted, so their wires can also
                // be deleted.
                deletedBlocks.push(block.data.id);
            }
        }

        var newWireViewModels = [];
        var newWires = [];

        //
        // Remove wires that are selected.
        // Also remove wires for blocks that have been deleted.
        //
        for (var wireIndex = 0; wireIndex < this.wires.length; ++wireIndex) {

            var wire = this.wires[wireIndex];
            if (!wire.selected() &&
                deletedBlocks.indexOf(wire.data.from_node) === -1 &&
                deletedBlocks.indexOf(wire.data.to_node) === -1)
            {
                //
                // The nodes this connection is attached to, where not deleted,
                // so keep the connection.
                //
                newWireViewModels.push(wire);
                newWires.push(wire.data);
            }
        }

        //
        // Update nodes and connections.
        //
        this.blocks = newBlockViewModels;
        this.data.blocks = newBlocks;
        this.wires = newWireViewModels;
        this.data.wires = newWires;

        return 0;
    };

    //
    // Select blocks and wires that fall within the selection rect
    //
    this.applySelectionRect = function (selectionRect) {

        var selectionCount = this.deselectAll();

        for (var i = 0; i < this.blocks.length; ++i) {
            var block = this.blocks[i];
            if (block.x() >= selectionRect.x &&
                block.y() >= selectionRect.y &&
                block.x() + block.width() <= selectionRect.x + selectionRect.width &&
                block.y() + block.height() <= selectionRect.y + selectionRect.height) {

                selectionCount++;

                // Select blocks that are within the selection rect
                block.select();
            }
        }

        for (var j = 0; j < this.wires.length; ++j) {
            var wire = this.wires[j];
            if (wire.source.parent().selected() &&
                wire.target.parent().selected()) {

                // Select the wire if both its parent blocks are selected
                wire.select();
            }
        }

        return selectionCount;
    };

    //
    // Get the array of blocks that are currently selected.
    //
    this.getSelectedBlocks = function () {
        var selectedBlocks = [];

        for (var i = 0; i < this.blocks.length; ++i) {
            var block = this.blocks[i];
            if (block.selected()) {
                selectedBlocks.push(block);
            }
        }

        return selectedBlocks;
    };

    //
    // Get the array of wires that are currently selected.
    //
    this.getSelectedWires = function () {
        var selectedWires = [];

        for (var i = 0; i < this.wires.length; ++i) {
            var wire = this.wires[i];
            if (wire.selected()) {
                selectedWires.push(wire);
            }
        }

        return selectedWires;
    };

    //
    // Updates the enumerated blocks with their new state.
    //
    this.updateStatusOfBlocks = function(blockStates) {

        console.log(JSON.stringify(blockStates));

        for (var i = 0; i < blockStates.length; ++i) {
            var block = this.findBlock(blockStates[i].id);
            block.updateProgress(blockStates[i].state);
        }
    }
};

viewmodels.configuringBlockViewModel = function (definition, block) {

    this.name = block.name;
    this.id = block.id;
    this.x = block.x;
    this.y = block.y;
    this.state = block.state;

    this.block = block;
    this.definition = definition;

    var blockParameters = block.parameters;

    // capture the block's parameters
    var parameters = [];
    this.definition.parameters().forEach(function(parameterDefinition){

        // if exists - reference block parameter
        var blockParameter;
        if (blockParameters){
            for(var i = 0; i < blockParameters.length; i++){

                if (blockParameters[i].name == parameterDefinition.name){

                    blockParameter = blockParameters[i];
                    break;
                }
            }
        }

        // copy parameter definition
        var parameter = new viewmodels.configuringParameterViewModel(parameterDefinition, blockParameter);

        // add to list of parameters
        parameters.push(parameter);
    });
    this.parameters = parameters;

    this.reset = function() {
        return new viewmodels.configuringBlockViewModel(this.definition, this.block);
    }
};

viewmodels.configuringParameterViewModel = function (definitionParameter, blockParameter) {

    this.data = angular.copy(definitionParameter);

    this.loaded = (this.data.source == null);
    this.loading = false;
    this.dirty = false;
    this.fieldOptions = this.data.fieldOptions;

    if (blockParameter && blockParameter.collected !== undefined){
        this.collected = blockParameter.collected;
    }
    else{
        this.collected = false;
    }

    if (blockParameter && blockParameter.value !== undefined){
        this.value = blockParameter.value;
    }
    else{
        this.value = this.data.value;
    }

    this.name = function(){
        return this.data.name;
    }

    this.type = function(){
        return this.data.type;
    }

    this.source = function(){
        return this.data.source;
    }
};

viewmodels.definitionViewModel = function (mode, data) {

    // reference the mode and the data
    this.mode = mode;
    this.data = data;

    //
    // get the definition type
    //
    this.definitionType = function(){ return this.data.definitionType; };

    //
    // get the input connectors
    //
    this.inputs = function(){ return this.modeDefinition().inputs; };

    //
    // returns the mode definition according to the specified mode
    //
    this.modeDefinition = function(){

        var modeDefinition;
        switch(this.mode){
            case "OFFLINE":
                modeDefinition = this.data.offlineDefinition;
                break;
            case "ONLINE":
                modeDefinition = this.data.onlineDefinition;
                break;
        }

        return modeDefinition;
    };

    //
    // get the definitions name
    //
    this.name = function(){ return this.data.name; };

    //
    // get the output connectors
    //
    this.outputs = function(){ return this.modeDefinition().outputs; };

    //
    // get the parameters
    //
    this.parameters = function(){ return this.modeDefinition().parameters; };

    //
    // get the width
    //
    this.w = function(){ return this.data.w; };
};

