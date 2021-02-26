__config() ->
(
	return(
		m(
			l('scope','player'),
			l('stay_loaded',true);
		);
	);
);

__on_player_right_clicks_block(player,item_tuple,hand,block,face,hitvec) ->
(
	if(hand == 'mainhand' && (block == 'bee_nest' || block == 'beehive') && item_tuple == null,
		bees = length(parse_nbt(block_data(pos(block)):'Bees'));
		string = bees + ' bee';
		if(bees != 1,
			string += 's';
		);
		print(player,string);
	);
);