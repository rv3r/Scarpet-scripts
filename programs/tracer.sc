__config() ->
(
	m(
		l('scope','player'),
		l('stay_loaded',true),
		
		l('commands',
			m(
				l('item <item>',l('__changeitem','item')),
				l('track <track_bool>',l('__changeitem','track')),
				l('landing <size>',l('__changeitem','landing')),
				l('path <path_bool>',l('__changeitem','path'))
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
__changeitem(value,mode) ->
(
	p = player();
	data = parse_nbt(load_app_data());
	//print(data);
	if(!data:str(p),
		data:str(p) = m(
			l('item','off'),
			l('track',false),
			l('landing',0),
			l('path',false)
		);
	);
	data:str(p):mode = value;
	//print(data);
	store_app_data(data);
);

//only really triggers with bows and tridents
__on_player_releases_item(player, item_tuple, hand) ->
(
	item = item_tuple:0;
	if(item == 'bow',
		entity = 'arrow',
		item == 'crossbow',
		entity = null,
		entity = item;
	);
	if(entity && global_settings:'track' && __validitem(item),
		//print('release call');
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
		entity = null,
		entity = item
	);
	if(entity && global_settings:'track' && __validitem(item),
		//print('use call');
		schedule(1,'__track',player,entity)
	)
);

//ensures item analysis is necessary based on player's settings
__validitem(item) ->
(
	throwables = l('egg','snowball','ender_pearl','experience_bottle','splash_potion');
	weapons = l('trident','crossbow','bow');
	if(global_settings:'item' == 'all' && keys(global_motion) ~ item > -1,
		output = true,
		global_settings:'item' == 'off',
		output = false,
		global_settings:'item' == 'weapons' && weapons ~ item > -1,
		output = true,
		global_settings:'item' == 'throwables' && throwables ~ item > -1,
		output = true,
		output = false;
	);
	return(output);
);

//tracks actual entity motion based on initial motion vector
__track(player,entity) ->
(
	//print(entity);
	entity_list = entity_area(entity,player ~ 'pos' + l(0,player ~ 'eye_height',0),l(0.25,0.25,0.25));
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
		colors = l(0xFFFF00FF,0xFFFF0033);
		__predict(entity ~ 'pos',motion,gravity,null,colors);
	);

	//schedule(1,'__get_pos',entity,motion);
);

//derecated method for gathering potion and bottle angular offset data
__phi(vector) ->
(
	magnitude = sqrt(reduce(vector,_a + _^2,0));
	unit = vector / magnitude;
	r_plane = sqrt((unit:0)^2 + (unit:2)^2);
	phi = atan2(unit:1,r_plane);
	return(phi);
);

//deprecated method for gathering entity motion data
__get_pos(entity,oldmotion) ->
(
	newmotion = entity ~ 'motion';
	if(oldmotion != newmotion,
		if(!blocks_movement(entity ~ 'pos'),
			global_actual = entity ~ 'pos',
			lower = global_actual;
			upper = entity ~ 'pos';
			loop(10,
				midpoint = (lower + upper) / 2;
				if(blocks_movement(midpoint),
					upper = midpoint;
					midpoint = (lower + midpoint) / 2,
					lower = midpoint;
					midpoint = (midpoint + upper) / 2
				);
			);
			global_actual = midpoint;
		);

		//print('\n' + str(newmotion));
		acceleration = newmotion - oldmotion;
		ratio = newmotion / oldmotion;
		//print(ratio);
		gravity = (newmotion - 0.99 * oldmotion):1;
		//print(round(gravity * 1000) / 1000);
		prediction = 0.99 * oldmotion - l(0,global_gravity,0);
		//print(prediction);
		//print(map(prediction - newmotion,(10^-3) * round(_ * 10^3)));
		schedule(1,'__get_pos',entity,newmotion),
		//print('  exit : ' + global_exit + ' m/s');

		e_pos = entity ~ 'pos';
		//print('entity : ' + map(e_pos,round(_*10^6)/(10^6)));
		entity_eye = entity ~ 'pos' + l(0,entity ~ 'eye_height',0);
		//print('entity eye : ' + map(entity_eye,round(_*10^6)/(10^6)));

		pos = entity ~ 'pos';
		error_list = global_bisect - pos;
		error = sqrt(reduce(error_list,_a + _^2,0));
		//print('age : ' + entity ~ 'age');
		//print('actual : ' + entity ~ 'pos');
		//print('ACTUAL : ' + global_actual);
		//print(' lower : ' + global_final:0);
		//print('bisect : ' + global_bisect);
		//print(' upper : ' + global_final:1);
		from_lower = (entity ~ 'pos' - global_final:0) / (global_final:1 - global_final:0);
		//print('%after : ' + round(from_lower:1 * 100) / 100);
		//print(' error : ' + round(error * 1000) + ' mm');
		actual_error = global_bisect - global_actual;
		actual_mm = round(sqrt(reduce(actual_error,_a + _^2,0)) * 1000);
		e_error = global_bisect - e_pos;
		e_mm = round(sqrt(reduce(e_error,_a + _^2,0)) * 1000);
		//print(' ERROR : ' + actual_mm + ' mm');
		//print('eERROR : ' + e_mm + ' mm');

		global_error += l(1000 * error,actual_mm);


		//print('better : ' + str(round(error * 1000) - actual_mm) + ' mm');
		average_error_list = reduce(global_error,_a + _,l(0,0)) / length(global_error);
		//print('   avg : ' + round(100 * average_error_list:0 / average_error_list:1) / 100 + 'x better than just getting position');
	)
);

//predicts where an entity will encounter a full movement-blocking block
__predict(pos,motion,gravity,ticks,colors) ->
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
			global_shapelist += l('line',ticks,'from',_,'to',pointlist:(_i + 1),'color',colors:0);
		)
	);
	__draw(validpos,__bisection_hitbox_search(validpos,pos),ticks,colors);
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
__draw(pos,hit,ticks,colors) ->
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
			draw_shape('box',ticks,'from',round_hit + from_offset,'to',round_hit + to_offset,'color',colors:0,'fill',colors:1);
		)
	);
	if(global_settings:'path',
		global_shapelist += l('line',ticks,'from',pos,'to',hit,'color',colors:0);
		draw_shape(global_shapelist);
	)
);

//approximates the angular offset experienced by splash potions and experience bottles
__angular_offset(pitch,yaw) ->
(
	//fifth order approximation of projectile launch angle as a function of player pitch
	// with check for looking straight up or straight down
	if(pitch % 90 == 0,
		angle_prediction = -1 * pitch,
		angle_prediction = 0.0000000031*pitch^5+0.0000003044*pitch^4-0.0000437532*pitch^3-0.0048864791*pitch^2-0.8392845326*pitch+19.1769378081;
	);
	//construct unit vector in this direction
	return(l(-1 * sin(yaw) * cos(angle_prediction),sin(angle_prediction),cos(yaw) * cos(angle_prediction)));
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
		gravity = global_motion:item:'gravity';
		colors = l(0x00FF00FF,0x00FF00FF);
		global_shapelist = l();
		sourcepos = p ~ 'pos' + l(0,p ~ 'eye_height' - 0.1,0);
		if(item == 'experience_bottle' || item == 'splash_potion',
			look = __angular_offset(p ~ 'pitch',yaw = p ~ 'yaw'),
			look = p ~ 'look';
		);
		motion = speed * look;

		//currently unused modification to ender pearl throw data based on player motion
		if(item == 'ender_pearl' && !(p ~ 'on_ground'),
			modifier = p ~ 'motion_y' + 0.784;
			modified = motion + l(0,modifier,0);
			mod_speed = reduce(modified,_a + _^2,0),
			modifier = 0;
		);


		__predict(sourcepos,motion,gravity,global_refresh,colors),
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