__config() ->
(
	return(
		m(
			l('scope','player'),
			l('stay_loaded',true)
		)
	)
);

__on_player_dies(player) ->
(
	pos = map(pos(player),round(_));
	dim = title(split('_',player~'dimension'):1);
	string = player + ' died at ' + str(pos) + ' in the ' + dim + '.';
	logger(string);
	print(string);
);