__config() ->
(
	return(
		m(
			l('scope','player'),
			l('stay_loaded',true);
		);
	);
);

beehiveCount(hiveBlock) ->
(
	bees = length(parse_nbt(block_data(pos(hiveBlock)):'Bees'));
	if(bees != 1,
		print(bees + ' bees'),
		print(bees + ' bee');
	);
);

__on_player_right_clicks_block(player,item_tuple,hand,block,face,hitvec) ->
(
	if(hand == 'mainhand' && (block == 'bee_nest' || block == 'beehive') && item_tuple == null,
		beehiveCount(block);
	);
);