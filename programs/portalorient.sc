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
__initialize() ->
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
__on_player_changes_dimension(player, from_pos, from_dimension, to_pos, to_dimension) -> (
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
		corners =  __corners(center,axis);
		offset = l(0,0,0);
		offset:(2 - axis) = 1;

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
	);
);

//check block by block in each direction for non-nether portals to find the edges of the portal,
//	allowing us to determine the corners of the portal
//this is faster than scanning the area(22 blocks up, down, left, and right) to find the corners of the portal
//according to profile_expr(), it's about 9x faster,
//	probably because it checks at most 44 blocks instead of all 2025 blocks
__corners(center,axis) ->
(
	//generate a list of blocks in each direction for checking later
	//	and sort the blocks by distance from the center block
	//	left side
	w1checkrange = l(-22,0,-22);
	w1checkrange:(2 - axis) = 0;
	w1check = sort_key(map(rect(center,w1checkrange,l(0,0,0)),block(_)),sqrt(reduce(pos(_) - center,_a + _^2,0)));
	//	right side
	w2checkrange = l(22,0,22);
	w2checkrange:(2 - axis) = 0;
	w2check = sort_key(map(rect(center,l(0,0,0),w2checkrange),block(_)),sqrt(reduce(pos(_) - center,_a + _^2,0)));
	//	bottom side
	h1check = sort_key(map(rect(center,l(0,-22,0),l(0,0,0)),block(_)),sqrt(reduce(pos(_) - center,_a + _^2,0)));
	//	top side
	h2check = sort_key(map(rect(center,l(0,0,0),l(0,22,0)),block(_)),sqrt(reduce(pos(_) - center,_a + _^2,0)));

	//put them all in a bigger list
	blocklist = l(w1check,w2check,h1check,h2check);

	//initialize list with the two corner blocks
	corners = l(
		copy(center),
		copy(center)
	);

	//check each list of blocks for an edge
	for(blocklist,
		//get the whole list for later
		current = _;
		//check each block in the current direction to find the edge
		edge = pos(
			first(_,
				//if the next block in the list is not a nether portal, we're at the edge of the portal
				//we could check for obsidian but this doesn't mind if you removed it
				current:(_i + 1) != 'nether_portal';
			)
		);
		//use the current position to change the values of the corners
		for(l(1,axis),
			if(edge:_ < corners:0:_,
				corners:0:_ = edge:_,
				edge:_ > corners:1:_,
				corners:1:_ = edge:_;
			);
		);
	);
	//return the corner values we got
	return(corners);
);

__corners2(center,axis) ->
(
	corners = l(
		copy(center),
		copy(center)
	);
	scanrange = l(0,22,0);
	scanrange:axis = 22; 
	scan(center,scanrange,
		pos = pos(_);
		if(neighbours(pos) ~ 'obsidian',
			for(l(1,axis),
				if(pos:_ < corners:0:_,
					corners:0:_ = pos:_,
					pos:_ > corners:1:_,
					corners:1:_ = pos:_;
				);
			);
		);
	);
	return(l(corners,shapelist));
);

__initialize();