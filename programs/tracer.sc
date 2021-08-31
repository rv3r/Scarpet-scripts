__config() ->
(
	m(
		l('scope','player'),
		l('stay_loaded',true),
		
		l('commands',
			m(
				l('item <item>',l('__changedata','item')),
				l('track bool <track_bool>',l('__changedata','track')),
				l('track color <color>',l('__changedata','track_color')),
				l('landing size <size>',l('__changedata','landing')),
				l('landing color <color>',l('__changedata','landing_color')),
				l('path <path_bool>',l('__changedata','path'))
			)
		),

		l('arguments',
			m(
				l('item',
					m(
						l('type','term'),
						l('options',
							l('off','throwables','weapons','all')
						),
						l('suggest',
							l('off','throwables','weapons','all')
						)
					)
				),
				l('size',
					m(
						l('type','float'),
						l('min',0.1),
						l('max',1),
						l('suggest',
							l(0.1,0.2,1)
						)
					)
				),
				l('color',
					m(
						l('type','int'),
						l('min',0),
						l('max',2 ^ 32 - 1),
						l('suggest',
							l(0x000000FF,0xFF0000FF,0x00FF00FF,0x0000FFFF,0xFFFF00FF,0xFF00FFFF,0x00FFFFFF,0xFFFFFFFF)
						)
					)
				)
			)
		)
	)
);

//ticks between refresh
global_refresh = 1;
//map of item name and corresponding gravity and throw speed values
global_motion = m(
	l('egg',
		m(
			l('gravity',0.03),
			l('speed',1.5)
		)
	),
	l('snowball',
		m(
			l('gravity',0.03),
			l('speed',1.5)
		)
	),
	l('ender_pearl',
		m(
			l('gravity',0.03),
			l('speed',1.5)
		)
	),
	l('experience_bottle',
		m(
			l('gravity',0.07),
			l('speed',0.7)
		)
	),
	l('splash_potion',	
		m(
			l('gravity',0.05),
			l('speed',0.5)
		)
	),
	l('trident',
		m(
			l('gravity',0.05),
			l('speed',2.5)
		)
	),
	l('crossbow',
		m(
			l('gravity',0.05),
			l('speed',3.15)
		)
	),
	l('bow',
		m(
			l('gravity',0.05),
			l('speed',3)
		)
	)
);

//setup the script's data if nothing is there already
//it's an empty map, so players without data will have a null mode
__on_start() ->
(
	if(!load_app_data(),
		store_app_data(m());
	)
);

//set a new value for the player's settings from the command
__changedata(value,mode) ->
(
	p = player();
	data = parse_nbt(load_app_data());
	if(!data:str(p),
		data:str(p) = m(
			l('item','off'),
			l('track',false),
			l('track_color',0x00FF00FF),
			l('landing',0),
			l('landing_color',0xFFFF00FF),
			l('path',false)
		);
	);
	data:str(p):mode = value;
	store_app_data(data);
);

//only really triggers with bows and tridents
__on_player_releases_item(player, item_tuple, hand) ->
(
	item = item_tuple:0;
	if(item == 'bow',
		entity = 'arrow';
		global_use_ticks = null,
		item == 'crossbow',
		entity = null,
		entity = item;
	);
	if(entity && global_settings:'track' && __validitem(item),
		schedule(0,'__track',player,entity);
	)
);

//triggers for all throwables and charged crossbows
__on_player_uses_item(player, item_tuple, hand) ->
(
	l(item,count,nbt) = item_tuple;
	if(item == 'splash_potion',
		entity = 'potion',
		item == 'crossbow' && nbt:'Charged',
		entity = 'arrow',
		(item == 'crossbow' && !(nbt:'Charged')) || item == 'bow' || item == 'trident',
		entity = null;
		if(item == 'bow',
			global_use_ticks = tick_time();
		),
		entity = item
	);
	if(entity && global_settings:'track' && __validitem(item),
		schedule(1,'__track',player,entity)
	)
);

//ensures item analysis is necessary based on player's settings
__validitem(item) ->
(
	throwables = l('egg','snowball','ender_pearl','experience_bottle','splash_potion');
	weapons = l('trident','crossbow','bow');
	if(global_settings:'item' == 'all' && keys(global_motion) ~ item != null,
		output = true,
		global_settings:'item' == 'off',
		output = false,
		global_settings:'item' == 'weapons' && weapons ~ item != null,
		output = true,
		global_settings:'item' == 'throwables' && throwables ~ item != null,
		output = true,
		output = false;
	);
	return(output);
);

//tracks actual entity motion based on initial motion vector
__track(player,entity) ->
(
	entity_list = entity_area(entity,player ~ 'pos' + l(0,player ~ 'eye_height' - 0.1,0),l(0.1,0.1,0.1));
	if(entity_list,
		entity = entity_list:0;
		motion = entity ~ 'motion';
		if(entity == 'Thrown Ender Pearl',
			entity_name = 'ender_pearl',
			entity_name = entity ~ 'type';
		);
		if(entity_name == 'arrow',
			gravity = 0.05,
			entity_name == 'potion',
			gravity = global_motion:'splash_potion':'gravity',
			gravity = global_motion:entity_name:'gravity';
		);
		color = number(global_settings:'track_color');

		__predict(entity ~ 'pos',motion,gravity,null,color);
	);
);

//predicts where an entity will encounter a full movement-blocking block
__predict(pos,motion,gravity,ticks,color) ->
(
	newpos = copy(pos);
	newmotion = copy(motion);
	validpos = pos;
	pointlist = l();
	while(!blocks_movement(newpos),1000,
		count = _;
		validpos = pos;
		newpos = pos + motion;
		newmotion = 0.99 * motion - l(0,gravity,0);
		displacement = sqrt(reduce(motion,_a + _^2,0));
		pointlist += validpos;
		if(displacement > 1,
			divisions = ceil(displacement);
			for(range(1,divisions),
				testblock = (_ * pos + (divisions - _) * newpos) / divisions;
				if(blocks_movement(testblock),
					newpos = testblock;
				);
			);
		);
		pos = copy(newpos);
		motion = copy(newmotion);
	);
	if(ticks == null,
		ticks = count,
		ticks = 1;
	);
	global_shapelist = l();
	for(pointlist,
		if(_i < (length(pointlist) - 1),
			global_shapelist += l('line',ticks,'from',_,'to',pointlist:(_i + 1),'color',color);
		)
	);
	__draw(validpos,__bisection_hitbox_search(validpos,pos),ticks,color);
);

//given two positions, the first outside a block and the other inside a block,
//	determines the coordinate between the two along the face of the block
__bisection_hitbox_search(pos1,pos2) ->
(
	lower = pos1;
	upper = pos2;
	loop(10,
		midpoint = (lower + upper) / 2;
		if(blocks_movement(midpoint),
			upper = midpoint;
			midpoint = (lower + midpoint) / 2,
			lower = midpoint;
			midpoint = (midpoint + upper) / 2
		);
	);
	//global_final = l(lower,upper);
	return(midpoint);
);

//might draw the last segment of the projectile's path
//	and definitely draws the landing box on the proper face of the block
__draw(pos,hit,ticks,color) ->
(
	size = global_settings:'landing';
	if(size,
		axis_check = map(hit,abs(_) % 1);
		for(axis_check,
			if(_ > 0.5,
				axis_check:_i = 1 - _;
			)
		);
		check_order = sort_key(l(range(3)),axis_check:_);
		axis = first(sort_key(l(range(3)),axis_check:_),
			axis = _;
			rounded = round(hit:_);
			round_hit = copy(hit);
			round_hit:_ = rounded;
			offset = abs(hit:_ - rounded);
			positive = copy(round_hit);
			positive:_ = positive:_ + offset;
			negative = copy(round_hit);
			negative:_ = negative:_ - offset;
			pos_bool = blocks_movement(block(positive)) && !blocks_movement(block(negative));
			neg_bool = !blocks_movement(block(positive)) && blocks_movement(block(negative));
			pos_bool || neg_bool
		);
		if(axis != null,
			if(pos_bool,
				height = -0.1,
				height = 0.1
			);
			from_offset = l(-1,-1,-1) * size / 2;
			from_offset:axis = 0;
			to_offset = l(1,1,1) * size / 2;
			to_offset:axis = height;
			draw_shape('box',ticks,'from',round_hit + from_offset,'to',round_hit + to_offset,'color',color,'fill',color);
		)
	);
	if(global_settings:'path',
		global_shapelist += l('line',ticks,'from',pos,'to',hit,'color',color);
		draw_shape(global_shapelist);
	)
);

//approximates the angular offset experienced by splash potions and experience bottles
__angular_offset(pitch,yaw) ->
(
	roll = -20;
	x_look = -1 * cos(pitch) * sin(yaw);
	y_look = -1 * sin(pitch + roll);
	z_look = cos(yaw) * cos(pitch);

	actual_yaw = -1 * atan2(x_look, z_look);
	actual_pitch = atan2(y_look, sqrt(x_look ^ 2 + z_look ^ 2));

	unit = l(-1 * sin(actual_yaw) * cos(actual_pitch),sin(actual_pitch),cos(actual_yaw) * cos(actual_pitch));

	return(unit);
);

//refreshes the currently drawn lines if the player's held item is valid
__refresh() ->
(
	p = player();
	mainhand = query(p,'holds','mainhand');
	offhand = query(p,'holds','offhand');
	if(mainhand && __validitem(mainhand:0),
		l(item,count,nbt) = mainhand,
		offhand && __validitem(offhand:0),
		l(item,count,nbt) = offhand,
		item = false
	);
	if(item,
		speed = global_motion:item:'speed';
		//adjustment for bow draw time
		if(item == 'bow',
			seconds = (tick_time() - global_use_ticks) / 20 || 20;
			speed = speed * min((seconds * (seconds + 2)) / 3,1);
		);

		gravity = global_motion:item:'gravity';
		color = number(global_settings:'landing_color');
		global_shapelist = l();
		sourcepos = p ~ 'pos' + l(0,p ~ 'eye_height' - 0.1,0);
		if(item == 'experience_bottle' || item == 'splash_potion',
			look = __angular_offset(p ~ 'pitch',p ~ 'yaw'),
			look = p ~ 'look';
		);
		motion = speed * look;

		__predict(sourcepos,motion,gravity,global_refresh,color),
	)
);

//grabs whatever new settings might exist and attempts to refresh the drawn items
__on_tick() ->
(
	global_settings = parse_nbt(load_app_data()):str(player());
	if(!(tick_time() % global_refresh),
		__refresh();
	)
);
