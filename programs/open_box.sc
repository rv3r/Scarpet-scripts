// Aren't these default nowadays?
__config() ->
(
	{
		['stay_loaded',true],
		['scope','player']
	}
);

// I highly suspect that most of these don't need to be global
global_box_name = null;
global_item_nbt = null;
global_inventory = null;
global_pickup = false;
global_update_actions = ['pickup','quick_move','swap','clone','throw','quick_craft','pickup_all'];
global_thrown = null;
global_screens = {};
global_nest = false;

// Turns out player swings hand for basically all item dropping events
__on_player_swings_hand(player, hand) ->
(
	pos = player ~ 'pos' + [0,player ~ 'eye_height' - 0.3,0];
	// Wait until end of tick to get the item entity, otherwise it doesn't exist yet
	schedule(0,'get_thrown_item',pos);
);

// Literally just grabs the item you threw
get_thrown_item(pos) ->
(
	// Get the only item that ought to be at your eye height
	// No this is not robust
	// I'm aware
	item_entity = entity_area('item', pos, [1e-6,1e-6,1e-6]):0;
	global_item_nbt = parse_nbt(item_entity ~ 'nbt');
	// The item should have a pickup delay 40 since you just dropped it
	if(global_item_nbt:'PickupDelay' == 40,
		// Check if it's a shulker box
		// With 'open' in the name(borrowed from shulkerboxes.sc, that thing's a gold mine!)
		if((name = get_item_id(global_item_nbt)) ~ 'shulker_box$' && (global_box_name = parse_nbt(global_item_nbt:'Item':'tag':'display':'Name'):'text') ~ 'open',
			global_thrown = 'shulker';
			global_screens:'shulker' = null;
			pickup(item_entity),
			// As it turns out, the script idea also works for ender chests
			name == 'minecraft:ender_chest',
			global_thrown = 'ender';
			global_screens:'ender' = null;
			pickup(item_entity);
		);
	);
);

get_item_id(parsed_nbt) ->
(
	parsed_nbt:'Item':'id'
);

pickup(item_entity) ->
(
	// Reset the pickup delay so you can pick it back up
	modify(item_entity, 'pickup_delay', 0);
	// Allow a screen to be made
	global_allowed = true;
	// Disallow screen creation at the end of the tick and reset pickup delay if you didn't pick it up
	schedule(0,_() -> (if(item_entity,modify(item_entity, 'pickup_delay', 40));global_allowed = false))
);

__on_player_picks_up_item(player, item) ->
(
	// Wait until the player picks up the box to make the screen for the box
	// Since this gets reset, doesn't make a screen if you fail to pick up the box immediately
	if(global_allowed && (global_thrown == 'ender' || (global_thrown == 'shulker' && get_box_slot())),
		make_box_screen(global_item_nbt)
	);
);

// Uses box nbt to set the items in the screen
make_box_screen(box_nbt) ->
(
	if(global_thrown == 'shulker',
		// Make a screen with the same properties as the original box item
		inv = 'shulker_box';
		name = global_box_name;
		global_inventory = box_nbt:'Item':'tag':'BlockEntityTag':'Items' || [],
		global_thrown == 'ender',
		inv = 'generic_9x3';
		name = 'Ender Chest';
		global_inventory = inventory_get('enderchest',player());
	);
	// Make a screen with the same name as the original box item
	global_screens:global_thrown = create_screen(player(), inv, name, _(screen, player, action, data) -> update_data(screen, player, action, data));
	// Sets all the items from the inventory to the screen
	if(global_thrown == 'shulker',
		for(global_inventory,
			if(_:'tag' == null,
				inventory_set(global_screens:global_thrown, _:'Slot', _:'Count', _:'id'),
				// inventory_set() hates null nbt
				inventory_set(global_screens:global_thrown, _:'Slot', _:'Count', _:'id', encode_nbt(_:'tag') || null)
			);
		),
		global_thrown == 'ender',
		for(range(inventory_size('enderchest',player())),
			item_tuple = global_inventory:_;
			if(item_tuple != null,
				if(item_tuple:2 == null,
					inventory_set(global_screens:global_thrown, _, item_tuple:1, item_tuple:0),
					inventory_set(global_screens:global_thrown, _, item_tuple:1, item_tuple:0, item_tuple:2)
				);
			);
		);
	);
);

// Screen got updated! Better do something...
update_data(screen, player, action, data) ->
(
	if(global_thrown == 'shulker' && action == 'pickup' && data:'slot' == null && get_box_slot():0 == -1,
		close_screen(screen);
	);
	if(action == 'slot_update' && global_pickup,
		if(global_thrown == 'shulker',
			update_box_inventory(screen, data),
			global_thrown == 'ender',
			update_ender_inventory(screen, data)
		),
		// Turns out that setting the original items causes slot updates, so wait until the player does something before updating the inventory
		global_update_actions ~ action != null,
		global_pickup = true,
		// On close, reset the global values
		action == 'close',
		if(screen == 'generic_9x3_screen' && global_pickup && data:'slot' < inventory_size('enderchest',player),
			delete(global_screens:'ender');
			global_nest = true,
			delete(global_screens:global_thrown);
		);
		if(global_nest && global_thrown == 'shulker' && screen == 'shulker_box_screen',
			delete(global_screens:'shulker');
			global_thrown = 'ender';
			global_screens:'ender' = null;
			global_nest = false;
			schedule(0,'make_box_screen',null);
		);
		if(global_screens == {} && !global_allowed,
			global_box_name = null;
			global_item_nbt = null;
			global_inventory = null;
			global_pickup = false;
			global_thrown = null;
			global_nest = false;
		);
	);
);

// Changes the box item's inventory to match the screen
update_box_inventory(screen, data) ->
(
	if([box_slot, item, count, nbt] = get_box_slot() || return(),
		slot = data:'slot';
		if(slot < 27,
			stack = data:'stack';
			set_box_stack(slot, stack);
			parsed_nbt = parse_nbt(nbt);
			// If there were no items in the box, set up the data structure
			if(parsed_nbt:'BlockEntityTag' == null,
				delete(parsed_nbt,'RepairCost');
				parsed_nbt:'BlockEntityTag' = 
				{
					['Items',global_inventory],
					['CustomName',
						{
							['"text"','"' + global_box_name + '"']
						}
					],
					['id','minecraft:shulker_box']
				},
				// Otherwise just put the inventory in the nbt
				// Inventory was parsed, so it will need to be encoded
				parsed_nbt:'BlockEntityTag':'Items' = global_inventory;
			);
			// Set the item in the player's inventory
			inventory_set(player(), box_slot, count, item, encode_nbt(parsed_nbt));
		);
	);
);

get_box_slot() ->
(
	// Save the player to a variable for the upcoming loop
	p = player();
	if(global_screens:global_thrown,
		cursor = inventory_get(global_screens:global_thrown,-1);
	);
	slot_range = [...range(36),40,-1];
	box = first([...filter(inventory_get(p),_i < 36 || _i == 40), cursor],
		if(_,
			// Unpack for later usage, might not need to do this, might be too expensive
			[item, count, nbt] = copy(_);
			slot = slot_range:_i;
			// It's gotta be a box with a stack size of 1 and the correct name from earlier
			item ~ 'shulker_box$' && count == 1 && (nametag = nbt:'display.Name') != null && parse_nbt(nametag):'text' == global_box_name,
			false
		);
	);
	if(box,
		[slot, item, count, nbt],
		null
	);
);

// Set the given slot and stack at the right spot
set_box_stack(slot, stack) ->
(
	// Find what got changed and where in the inventory list it is
	found = first(global_inventory,
		changed_slot = _i;
		_:'Slot' == slot
	);
	// If we're setting an empty slot, either make the inventory an empty list or just delete the entry
	set = if(stack == null,
		if(length(global_inventory) == 0,
			global_inventory = [],
			delete(global_inventory, changed_slot);
		),
		// Otherwise if we found the slot, set the new values
		found,
		global_inventory:changed_slot:'id' = stack:0;
		global_inventory:changed_slot:'Count' = stack:1;
		if(stack:2 != null,
			global_inventory:changed_slot:'tag' = parse_nbt(stack:2),
			delete(global_inventory:changed_slot,'tag');
		),
		// If we didn't find the slot, make a new entry for the item
		data = {};
		data:'Slot' = slot;
		data:'id' = stack:0;
		data:'Count' = stack:1;
		if(stack:2 != null,
			data:'tag' = parse_nbt(stack:2);
		);
		global_inventory += data;
	);
);

update_ender_inventory(screen, data) ->
(
	slot = data:'slot';
	if(slot < inventory_size('enderchest',player()),
		inventory_set('enderchest',player(),slot, data:'stack':1, data:'stack':0, data:'stack':2);
	);
);
