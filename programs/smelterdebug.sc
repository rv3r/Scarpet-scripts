__config() ->
(
	{
		['scope','player'],
		['stay_loaded',true],

		['commands',
			{
				['clear data',_() -> (print('Cleared cached furnace positions');__clear_data())],
				['clear inventories','__clear_inventories'],
				['print','__print'],
				['display <display_bool>','__set_display_mode']
			}
		]
	}
);

//clears inventories of every cached furnace variant
__clear_inventories() ->
(
	//go through lists of furnace variants
	for(pairs(global_furnaces),
		//save block name to prevent many unnecessary list accesses
		key = _:0;
		//for list of furnace variant positions
		for(_:1,
			set(
				//set at the original position
				_,
				//set the original block
				key,
				{
					//set unlit block
					['lit',false],
					//keeps original facing direction
					['facing',block_state(_,'facing')]
				},
				//empty block data to set with clear inventory
				{}
			)
		)
	);
	//tell the player what happened
	print('Cleared furnace inventories')
);

//clears lists of cached furnace variants and main blocks
__clear_data() ->
(
	//copies both from an empty map with keys of each furnace variant name
	global_main_furnaces = copy(global_default_map);
	global_furnaces = copy(global_default_map)
);

//caches positions of all furnace variant blocks in region
__cache_furnaces(pos1,pos2) ->
(
	volume(pos1,pos2,
		name = str(_);
		//check to see if current block is actually a furnace variant
		if((keys(global_furnaces) ~ name) != null,
			//cache only the position
			global_furnaces:name += pos(_)
		)
	);
	//tell the player what happened
	print('Cached furnaces')
);

//compares a given inventory to the inventory of another block
__compare(inventory,pos) ->
(
	//get second position inventory 
	comparison = inventory_get(pos);
	//initialize output
	output = [null,null,null];
	for(inventory,
		//compare inventory items
		if(_:0 == comparison:_i:0,
			//if identical, output count difference
			output:_i = comparison:_i:1 - _:1,
			//if different, output list of current item and correct item
			output:_i = [comparison:_i:0,inventory:_i:0]
		)
	);
	output
);

//compares every furnace variant to its main block
__compare_all() ->
(
	//prepare output map
	output = {
		[
			'furnace',
			{}
		],
		[
			'smoker',
			{}
		],
		[
			'blast_furnace',
			{}
		]
	};
	for(pairs(global_furnaces),
		key = _:0;
		main = global_main_furnaces:(key);
		for(_:1,
			//check that a main block has been set
			if(main != [],
				output:key:_ = __compare(inventory_get(main),_),
				//set to null if main block hasn't been set
				output:key:_ = null
			)
		)
	);
	output
);

//prints errors for each furnace variant
__print() ->
(
	//use boolean for potential print() later
	result = false;
	for(pairs(__compare_all()),
		//check if any blocks have errors
		if(!all(values(_:1),_ == [0,0,0]),
			result = true;
			//print bold, underscored variant name with replace() for blast_furnace
			print(
				format(
					str('u %s errors:',title(replace(_:0,'_',' ')))
				)
			);
			//for positions and their error lists
			for(pairs(_:1),
				if(_:1 != [0,0,0],
					print(
						//print block position
						format(
							str(
								'b  %s:',
								str(_:0)
							),
							//hover text for block position
							'^c Click to look at block',
							//run command to make player look at block when position is clicked
							str(
								'!/player %s look at %.1f %.1f %.1f',
								str(player()),
								...(_:0 + 0.5)
							)
						)
					);
					//set up list for proper naming of errors
					slot_names = ['Input','Fuel','Output'];
					for(_:1,
						//each error will start with the slot name
						beginning = str('	- %s: ',slot_names:_i);
						//if it's a list, indicate that the item is wrong and should be replaced
						if(type(_) == 'list',
							print(beginning + format(str('r wrong item, %s instead of %s',if(_:0 == null,'empty',_:0),if(_:1 == null,'empty',_:1))));
							//if it's a number, indicate how many more or fewer items are required
							type(_) == 'number',
							if(_ < 0,
								print(beginning + format(str('y too few items, %d short',abs(_)))),
								_ > 0,
								print(beginning + format(str('y too many items, %d extra',abs(_))))
							)
						)
					)
				)
			)
		)
	);
	//if there were no errors, indicate that to the player
	if(!result,
		print(format('l No inventory errors found'))
	)
);

//toggles display mode for the player
__set_display_mode(setting) ->
(
	//get app data
	data = load_app_data();
	//set updated value
	data:str(player()) = setting;
	//store app data
	store_app_data(data)
);

//displays boxes and labels indicating furnace status
__display() ->
(
	//list of faces because we can't rotate about an axis with direction names and theres no way to convert list to direction
	faces = ['south','east','north','west',];
	//list of offset lists because mapping the previous list to this leads to weird orders from values()
	offsets = [[0,0,1],[1,0,0],[0,0,-1],[-1,0,0]];
	p = player();
	//offsets for input, fuel, and output slots
	box_offsets = [
		[-0.25,0.15],
		[-0.25,-0.15],
		[0.25,0]
	];
	//initialize empty shape list
	shapelist = l();
	for(values(__compare_all()),
		for(pairs(_),
			//start at position in center of block
			pos = _:0 + 0.5;
			//get face direction of furnace
			facing = block_state(pos,'facing');
			//get index of face, really wish I could rotate this later
			face_index = faces ~ facing;
			//move to center of all three boxes, can't use pos_offset() because it rounds
			face_center = pos + 0.5 * offsets:face_index + [0,0.2,0];
			//designate half the width and height of boxes, use index + 1 for hacky rotation
			shape_corner = 0.25 * offsets:(face_index + 1) + [0,0.15,0];
			for(_:1,
				text = str(_);
				//figure out proper color based on error status
				if(_ == 0,
					color = 0x00FF00FF,
					type(_) == 'list',
					//overwrite relevant text if necessary
					text = 'Item';
					color = 0xFF0000FF,
					color = 0xFFFF00FF
				);
				//center of the current box and label, more index + 1 for more hacky rotation
				shape_center = face_center + box_offsets:_i:0 * offsets:(face_index + 1) + [0,box_offsets:_i:1,0];
				//draw for 2 ticks because sometimes it flashes oddly, send only to player
				shapelist += ['box',2,player,p,'color',color,'fill',color,'from',shape_center + shape_corner,'to',shape_center - shape_corner + 0.02 * offsets:face_index];
				shapelist += ['label',2,player,p,'text',text,'pos',shape_center + 0.03 * offsets:face_index - [0,0.125,0],'color',0x000000FF,'facing',facing]
			)
		)
	);
	draw_shape(shapelist)
);

//triggers when player holds coal, calls relevant functions
__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) ->
(
	//check if item is coal
	if(item_tuple:0 == 'coal',
		//check for crouching and hit an acceptable block
		if(player ~ 'pose' == 'crouching' && keys(global_furnaces) ~ str(block) != null,
			//overwrite current main block position
			global_main_furnaces:str(block) = pos(block);
			//tell player what happened
			print('Set a reference furnace'),
			//otherwise set a corner
			if(global_pos1 == null,
				//if there wasn't a first corner, set it
				global_pos1 = pos(block);
				//tell player what happened
				print('Set position 1'),
				//cache furnaces with second position
				__cache_furnaces(global_pos1,pos(block));
				//reset first position
				global_pos1 = null;
				//tell player what happened
				print('Set position 2')
			)
		)
	)
);

//clears most data on start
__on_start() ->
(
	global_default_map = m(
		[
			'furnace',
			[]
		],
		[
			'smoker',
			[]
		],
		[
			'blast_furnace',
			[]
		]
	);
	global_pos1 = null;
	__clear_data();
	if(!load_app_data(),
		store_app_data(m())
	)
);

//attempt to display furnace data every tick
__on_tick() ->
(
	//only display if player variable is true
	if(load_app_data():str(player()),
		__display()
	)
);
