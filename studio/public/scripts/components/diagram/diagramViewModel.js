
var viewmodels = viewmodels || {};

viewmodels.connectorViewModel = function (data, x, y, parent) {

    this.data = data;
    this._parent = parent;
    this._x = x;
    this._y = y;

    //
    // The name of the connector.
    //
    this.name = function () {
        return this.data.name;
    }

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
    // Name of the block
    //
    this.name = function () {
        return this.data.name;
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

    //
    // Return the text representation of the state
    //
    this.state = function(){
        return stateLabel(this.data.state);
    };

    var stateLabel = function(state){

        var label;
        switch(state){
            case 0:
                label = "Configuring";
                break;
            case 1:
                label = "Ready";
                break;
            case 2:
                label = "Executing";
                break;
            case 3:
                label = "Complete";
                break;
        }

        return label;
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
        }

        return result;
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

    this.sourceCoordX = function () {
        return this.source.parent().x() + this.source.x();
    };

    this.sourceCoordY = function () {
        return this.source.parent().y() + this.source.y();
    };

    this.sourceCoord = function () {
        return {
            x: this.sourceCoordX(),
            y: this.sourceCoordY()
        };
    };

    this.sourceTangentX = function () {
        return this.parent.computeWireSourceTangentX(this.sourceCoord(), this.targetCoord());
    };

    this.sourceTangentY = function () {
        return this.parent.computeWireSourceTangentY(this.sourceCoord(), this.targetCoord());
    };

    this.targetCoordX = function () {
        return this.target.parent().x() + this.target.x();
    };

    this.targetCoordY = function () {
        return this.target.parent().y() + this.target.y();
    };

    this.targetCoord = function () {
        return {
            x: this.targetCoordX(),
            y: this.targetCoordY()
        };
    };

    this.targetTangentX = function () {
        return this.parent.computeWireTargetTangentX(this.sourceCoord(),
            this.targetCoord());
    };

    this.targetTangentY = function () {
        return this.parent.computeWireTargetTangentY(this.sourceCoord(),
            this.targetCoord());
    };

    //
    // Select the wire.
    //
    this.select = function () {
        this._selected = true;
    };

    //
    // Deselect the wire.
    //
    this.deselect = function () {
        this._selected = false;
    };

    //
    // Toggle the selection state of the connection.
    //
    this.toggleSelected = function () {
        this._selected = !this._selected;
    };

    //
    // Returns true if the connection is selected.
    //
    this.selected = function () {
        return this._selected;
    };
};

viewmodels.diagramViewModel = function(data) {

    // reference the diagram data model
    this.data = data;

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

    this.computeConnectorPos = function (block, connectorIndex, inputConnector) {
        return {
            x: block.x() + block.calculateConnectorX(connectorIndex, inputConnector),
            y: block.y() + (inputConnector ? 0 : block.height())
        };
    };

    //
    // Find a specific block within this diagram
    //
    this.findBlock = function (name) {

        for (var i = 0; i < this.blocks.length; ++i) {
            var block = this.blocks[i];
            if (block.data.name == name) {
                return block;
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

    //
    // Compute the tangent for the bezier curve.
    //
    this.computeWireSourceTangentX = function (pt1, pt2) {

        return pt1.x + this.computeWireTangentOffset(pt1, pt2);
    };

    //
    // Compute the tangent for the bezier curve.
    //
    this.computeWireSourceTangentY = function (pt1, pt2) {

        return pt1.y;
    };

    //
    // Compute the tangent for the bezier curve.
    //
    this.computeWireSourceTangent = function(pt1, pt2) {
        return {
            x: this.computeWireSourceTangentX(pt1, pt2),
            y: this.computeWireSourceTangentY(pt1, pt2)
        };
    };

    //
    // Compute the tangent for the bezier curve.
    //
    this.computeWireTargetTangentX = function (pt1, pt2) {

        return pt2.x - this.computeWireTangentOffset(pt1, pt2);
    };

    //
    // Compute the tangent for the bezier curve.
    //
    this.computeWireTargetTangentY = function (pt1, pt2) {

        return pt2.y;
    };

    //
    // Compute the tangent for the bezier curve.
    //
    this.computeWireTargetTangent = function(pt1, pt2) {
        return {
            x: this.computeWireTargetTangentX(pt1, pt2),
            y: this.computeWireTargetTangentY(pt1, pt2)
        };
    };

    this.computeWireTangentOffset = function (pt1, pt2) {

        return (pt2.x - pt1.x) / 2;
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
            from_node: (startConnectorType == 'output') ? startBlock.data.name : endBlock.data.name,
            from_connectionIndex: (startConnectorType == 'output') ? startConnectorIndex : endConnectorIndex,
            to_node: (startConnectorType == 'output') ? endBlock.data.name : startBlock.data.name,
            to_connectionIndex: (startConnectorType == 'output') ? endConnectorIndex : startConnectorIndex
        };
        wiresData.push(wireDataModel);

        var outputConnector = startConnectorType == 'output' ? startConnector : endConnector;
        var inputConnector = startConnectorType == 'output' ? endConnector : startConnector;

        var wireViewModel = new viewmodels.wireViewModel(wiresData,
            this,
            outputConnector,
            inputConnector);
        wiresViewModels.push(wireViewModel);
    };

    //
    // Add a block to the view model.
    //
    this.createBlock = function (x, y, definition) {

        if (!this.data.blocks) {
            this.data.blocks = [];
        }

        var block = new DataBlock(x, y, definition);

        //
        // Update the data model.
        //
        this.data.blocks.push(block);

        //
        // Update the view model.
        //
        this.blocks.push(new viewmodels.blockViewModel(block));
    };

    var DataBlock = function(x, y, definition){

        // generate unique block name
        var index = 1;
        var name;
        do{
            name = definition.name + index;
            index++;
        }
        while(name in blockNames);
        // capture new block name
        blockNames[name] = true;

        this.name = name;
        this.definition = definition.name;
        this.state = 0;
        this.w = definition.w;
        this.x = x;
        this.y = y;
        this.inputConnectors = definition.inputConnectors;
        this.outputConnectors = definition.outputConnectors;
        this.parameters = definition.parameters;
    };

    //
    // Select all blocks and connections in the chart.
    //
    this.selectAll = function () {

        var blocks = this.blocks;
        for (var i = 0; i < blocks.length; ++i) {
            var block = blocks[i];
            block.select();
        }

        var wires = this.wires;
        for (var j = 0; j < connections.length; ++j) {
            var wire = wires[i];
            wire.select();
        }
    };

    //
    // Deselect all nodes and connections in the chart.
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
    this.handleBlockClicked = function (block) {

        this.deselectAll();
        block.select();

        // Move node to the end of the list so it is rendered after all the other.
        // This is the way Z-order is done in SVG.

        var blockIndex = this.blocks.indexOf(block);
        if (blockIndex == -1) {
            throw new Error("Failed to find block in view model!");
        }
        this.blocks.splice(blockIndex, 1);
        this.blocks.push(block);
    };

    //
    // Handle mouse down on a wire.
    //
    this.handleWireMouseDown = function (wire, ctrlKey) {

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
                deletedBlocks.push(block.data.name);
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
    };

    //
    // Select nodes and connections that fall within the selection rect.
    //
    this.applySelectionRect = function (selectionRect) {

        this.deselectAll();

        for (var i = 0; i < this.blocks.length; ++i) {
            var block = this.blocks[i];
            if (block.x() >= selectionRect.x &&
                block.y() >= selectionRect.y &&
                block.x() + block.width() <= selectionRect.x + selectionRect.width &&
                block.y() + block.height() <= selectionRect.y + selectionRect.height)
            {
                // Select nodes that are within the selection rect.
                block.select();
            }
        }

        for (var j = 0; j < this.wires.length; ++j) {
            var wire = this.wires[j];
            if (wire.source.parent().selected() &&
                wire.target.parent().selected())
            {
                // Select the connection if both its parent nodes are selected.
                wire.select();
            }
        }

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
};

