__config() ->
(
	return(
		m(
			l('scope','player'),
			l('stay_loaded',true),
			
			l('commands',
				m(
					l('<mode>','__change')
				)
			),

			l('arguments',
				m(
					l('mode',
						m(
							l('type','term'),
							l('options',
								l('off','air','solid')
							),
							l('suggest',
								l('off','air','solid')
							)
						)
					)
				)
			)
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

//set a new value for the player's mode from the command
__change(mode) ->
(
	p = player();
	data = load_app_data();
	data:p = mode;
	store_app_data(data);
);

//when a player goes through a portal, change their yaw if they don't have
//	it turned off and travelled to or from the nether
__on_player_changes_dimension(player, from_pos, from_dimension, to_pos, to_dimension) ->
(
	//get player and their current mode
	p = player;
	mode = load_app_data():p;

	//only trigger under certain conditions
	if(mode != 'off' && mode != null && (from_dimension == 'the_nether' || to_dimension == 'the_nether'),
		//round the player's position
		center = map(to_pos,floor(_));

		//get the portal axis
		if(block_state(block(center)):'axis' == 'x',
			axis = 0,
			axis = 2;
		);

		//get the corners of the portal and construct an offset to scan each side
		offset = l(0,0,0);
		offset:(2 - axis) = 1;
		corners =  __corners3(center,offset);
		print(corners:0 - corners:1);

		//scan each side of the portal for blocks based on the player's chosen mode
		//	air -> face the side with more air blocks
		//	solid -> face the side with less solid blocks
		side1 = volume(corners:0 + offset,corners:1 + offset,
			if(mode == 'air',
				air(_),
				mode == 'solid',
				solid(_);
			);
		);
		side2 = volume(corners:0 - offset,corners:1 - offset,
			if(mode == 'air',
				air(_),
				mode == 'solid',
				solid(_);
			);
		);

		print('side 1 : ' + side1);
		print('side 2 : ' + side2);

		//compare the two sides to determine which direction the player should face
		if(side1 > side2,
			yaw = -45 * axis + (mode == 'solid') * 180,
			side2 > side1,
			yaw = -45 * axis + (mode == 'air') * 180,
			//if the two sides had matching numbers of valid blocks, don't change anything
			yaw = p ~ 'yaw'
		);

		//modify the player's head and body yaw
		modify(p,'yaw',yaw);
		modify(p,'body_yaw',yaw);
	)
);

//tracks portal blocks along a diagonal until it encounters an edge,
//	and then travels along the edge to find the corner
//	thus sometimes checking slightly less blocks than a
//	purely horizontal/vertical method
//this is faster than scanning the area(20 blocks up, down, left, and right) to find the corners of the portal
//according to profile_expr(), it's about 10x faster,
//	probably because it checks at most 42 blocks instead of all 1681 blocks
__corners3(center,offset) ->
(
	//use the vector normal to the portal plane to construct a vector along the portal plane 
	corneroffset = l(1,1,1) - offset;
	//generate a list of blocks in each direction along one diagonal for checking later
	//generate them in order to prevent need for sorting
	positivecheck = l(block(center));
	negativecheck = copy(positivecheck);
	positive = copy(center);
	negative = copy(center);
	loop(20,
		positive = positive + corneroffset;
		positivecheck += block(positive);
		negative = negative - corneroffset;
		negativecheck += block(negative);
	);

	edgecheck = l();
	for(l(negativecheck,positivecheck),
		edge = __lastportal(_);
		iterator = _ ~ edge;
		listdirection = 2 * _i - 1;
		if(block(edge + listdirection * l(0,1,0)) != 'nether_portal',
			//block above or below is not a nether portal
			//start checking horizontally
			direction = corneroffset - l(0,1,0),
			block(edge + listdirection * (corneroffset - l(0,1,0))) != 'nether_portal',
			//block to left or right is not a nether portal
			//start checking vertically
			direction = l(0,1,0),
			block(edge + listdirection * l(0,1,0)) != 'nether_portal' && block(edge + listdirection * (corneroffset - l(0,1,0))) != 'nether_portal',
			//no blocks left that are a nether portal
			//stop
			direction = l(0,0,0);
		);
		magnitude = (length(_) - iterator) * direction;
		edgecheck:_i = sort_key(
			map(
				rect(
					edge,
					(1 - _i) * magnitude,
					_i * magnitude),
					block(_)
				),
			reduce(
				pos(_) - edge,
				_a + _^2,
				0
			)
		);
	);

	corners = l();
	for(edgecheck,
		corners:_i = __lastportal(_);
	);
	print(corners);
	return(corners);
);

//find last portal block before non portal block from a list of positions
__lastportal(positionlist) ->
(
	pos(
		first(positionlist,
			//if the next block in the list is not a nether portal, we're at the edge of the portal
			//we could check for obsidian but this way the script doesn't mind if you removed it
			positionlist:(_i + 1) != 'nether_portal';
		)
	)
);
