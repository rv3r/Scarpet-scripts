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

__clear_inventories() ->
(
	for(pairs(global_furnaces),
		key = _:0;
		for(_:1,
			set(
				_,
				key,
				{
					['lit',false],
					['facing',block_state(_,'facing')]
				},
				{}
			)
		)
	);
	print('Cleared furnace inventories')
);

__clear_data() ->
(
	global_main_furnaces = copy(global_default_map);
	global_furnaces = copy(global_default_map)
);

__log_furnaces(pos1,pos2) ->
(
	volume(pos1,pos2,
		name = str(_);
		if((keys(global_furnaces) ~ name) != null,
			global_furnaces:name += pos(_)
		)
	);
	print('Logged furnaces')
);

__compare(inventory,pos) ->
(
	comparison = inventory_get(pos);
	output = [null,null,null];
	for(inventory,
		if(_:0 == comparison:_i:0,
			output:_i = comparison:_i:1 - _:1,
			output:_i = [comparison:_i:0,inventory:_i:0]
		)
	);
	output
);

__compare_all() ->
(
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
			if(main != [],
				output:key:_ = __compare(inventory_get(main),_),
				output:key:_ = null
			)
		)
	);
	output
);

__print() ->
(
	result = false;
	for(pairs(__compare_all()),
		if(!all(values(_:1),_ == [0,0,0]),
			result = true;
			print(
				format(
					str('u %s errors:',title(replace(_:0,'_',' ')))
				)
			);
			for(pairs(_:1),
				if(_:1 != [0,0,0],
					print(
						format(
							str(
								'b  %s:',
								str(_:0)
							),
							'^c Click to look at block',
							str(
								'!/player %s look at %.1f %.1f %.1f',
								str(player()),
								...(_:0 + 0.5)
							)
						)
					);
					slot_names = ['Input','Fuel','Output'];
					for(_:1,
						beginning = str('	- %s: ',slot_names:_i);
						if(type(_) == 'list',
							print(beginning + format(str('r wrong item, %s instead of %s',if(_:0 == null,'empty',_:0),if(_:1 == null,'empty',_:1))));
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
	if(!result,
		print(format('l No inventory errors found'))
	)
);

__set_display_mode(setting) ->
(
	data = load_app_data();
	data:str(player()) = setting;
	store_app_data(data)
);

__display() ->
(
	faces = ['south','east','north','west',];
	offsets = [[0,0,1],[1,0,0],[0,0,-1],[-1,0,0]];
	p = player();
	box_offsets = [
		[-0.25,0.15],
		[-0.25,-0.15],
		[0.25,0]
	];
	shapelist = l();
	for(values(__compare_all()),
		for(pairs(_),
			pos = _:0 + 0.5;
			facing = block_state(pos,'facing');
			face_index = faces ~ facing;
			face_center = pos + 0.5 * offsets:face_index + [0,0.2,0];
			shape_corner = 0.25 * offsets:(face_index + 1) + [0,0.15,0];
			for(_:1,
				text = str(_);
				if(_ == 0,
					color = 0x00FF00FF,
					type(_) == 'list',
					text = 'Item';
					color = 0xFF0000FF,
					color = 0xFFFF00FF
				);
				shape_center = face_center + box_offsets:_i:0 * offsets:(face_index + 1) + [0,box_offsets:_i:1,0];
				shapelist += ['box',2,'color',color,'fill',color,'from',shape_center + shape_corner,'to',shape_center - shape_corner + 0.02 * offsets:face_index];
				shapelist += ['label',2,'text',text,'pos',shape_center + 0.03 * offsets:face_index - [0,0.125,0],'color',0x000000FF,'facing',facing]
			)
		)
	);
	draw_shape(shapelist)
);

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) ->
(
	if(item_tuple:0 == 'coal',
		if(player ~ 'pose' == 'crouching' && keys(global_furnaces) ~ str(block) != null,
			global_main_furnaces:str(block) = pos(block);
			print('Set a reference furnace'),
			if(global_pos1 == null,
				global_pos1 = pos(block);
				print('Set position 1'),
				print('Set position 2');
				__log_furnaces(global_pos1,pos(block));
				global_pos1 = null
			)
		)
	)
);

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

__on_tick() ->
(
	if(load_app_data():str(player()),
		__display()
	)
);
