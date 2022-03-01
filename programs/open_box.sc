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
global_box_inventory = null;
global_pickup = false;
global_update_actions = ['pickup','quick_move','swap','clone','throw','quick_craft','pickup_all'];

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
	// Better be a shulker box
	// With 'open' in the name(borrowed from shulkerboxes.sc, that thing's a gold mine!)
	// And has pickup delay 40 since you just dropped it
	if(global_item_nbt:'Item':'id' ~ 'shulker_box$' && (global_box_name = parse_nbt(global_item_nbt:'Item':'tag':'display':'Name'):'text') ~ 'open' && global_item_nbt:'PickupDelay' == 40,
		// Reset the pickup delay so you can pick it back up
		modify(item_entity, 'pickup_delay', 0);
		// Allow a screen to be made
		global_allowed = true;
		// Disallow screen creation at the end of the tick
		schedule(0,_() -> global_allowed = false);
	);
);

__on_player_picks_up_item(player, item) ->
(
	// Wait until the player picks up the box to make the screen for the box
	// Since this gets reset, doesn't make a screen if you fail to pick up the box immediately
	if(global_allowed,
		make_box_screen(global_item_nbt);
	);
);

// Uses box nbt to set the items in the screen
make_box_screen(box_nbt) ->
(
	// Make a screen with the same name as the original box item
	box_screen = create_screen(player(), 'shulker_box', global_box_name, _(screen, player, action, data) -> update_data(screen, player, action, data));
	global_box_inventory = box_nbt:'Item':'tag':'BlockEntityTag':'Items' || [];
	// Sets all the items from the item nbt to the screen
	for(global_box_inventory,
		if(_:'tag' == null,
			inventory_set(box_screen, _:'Slot', _:'Count', _:'id'),
			// inventory_set() hates null nbt
			inventory_set(box_screen, _:'Slot', _:'Count', _:'id', encode_nbt(_:'tag') || null)
		)
	);
);

// Screen got updated! Better do something...
update_data(screen, player, action, data) ->
(
	if(action == 'slot_update' && global_pickup,
		update_box_inventory(screen, data),
		// Turns out that setting the original items causes slot updates, so wait until the player does something before updating the inventory
		global_update_actions ~ action != null,
		global_pickup = true,
		// On close, reset the global values
		action == 'close',
		global_box_name = null;
		global_box_inventory = null;
		global_pickup = false;
	)
);

// Changes the box item's inventory to match the screen
update_box_inventory(screen, data) ->
(
	// Save the player to a variable for the upcoming loop
	p = player();
	box_slot = first([range(36)],
		if(item_tuple = inventory_get(p, _),
			// Unpack for later usage, might not need to do this, might be too expensive
			[item, count, nbt] = copy(item_tuple);
			// It's gotta be a box with a stack size of 1 and the correct name from earlier
			item ~ 'shulker_box$' && count == 1 && (nametag = nbt:'display.Name') != null && parse_nbt(nametag):'text' == global_box_name,
			false
		);
	);

	// If the box is not in the inventory, close the screen(gonna be done later)
	// Otherwise set the updated inventory to the item nbt
	if(// box_slot == null,
		// schedule(0,_(screen) -> close_screen(screen), screen),
		box_slot != null,
		slot = data:'slot';
		if(slot < 27,
			stack = data:'stack';
			set_box_stack(slot, stack);
			box_items = map(global_box_inventory, encode_nbt(_));
			parsed_nbt = parse_nbt(nbt);
			// If there were no items in the box, set up the data structure
			if(parsed_nbt:'BlockEntityTag' == null,
				delete(parsed_nbt,'RepairCost');
				parsed_nbt:'BlockEntityTag' = 
				{
					['Items',global_box_inventory],
					['CustomName',
						{
							['"text"','"' + global_box_name + '"']
						}
					],
					['id','minecraft:shulker_box']
				},
				// Otherwise just put the inventory in the nbt
				parsed_nbt:'BlockEntityTag':'Items' = global_box_inventory;
			);
			// Set the item in the player's inventory
			inventory_set(p, box_slot, count, item, encode_nbt(parsed_nbt));
		);
	);
);

// Set the given slot and stack at the right spot
set_box_stack(slot, stack) ->
(
	// Find what got changed and where in the inventory list it is
	found = first(global_box_inventory,
		changed_slot = _i;
		_:'Slot' == slot
	);
	// If we're setting an empty slot, either make the inventory an empty list or just delete the entry
	set = if(stack == null,
		if(length(global_box_inventory) == 0,
			global_box_inventory = [],
			delete(global_box_inventory, changed_slot);
		),
		// Otherwise if we found the slot, set the new values
		found,
		global_box_inventory:changed_slot:'id' = stack:0;
		global_box_inventory:changed_slot:'Count' = stack:1;
		if(stack:2 != null,
			global_box_inventory:changed_slot:'tag' = stack:2,
			delete(global_box_inventory:changed_slot,'tag');
		),
		// If we didn't find the slot, make a new entry for the item
		data = {};
		data:'Slot' = slot;
		data:'id' = stack:0;
		data:'Count' = stack:1;
		if(stack:2 != null,
			data:'tag' = stack:2
		);
		global_box_inventory += data;
	);
);
