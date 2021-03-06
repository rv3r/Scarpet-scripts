__config() ->
(
	return(
		m(
			l('scope','player'),
			l('stay_loaded',true)
		)
	)
);

global_tp = false;

__healplayer(player,amount) ->
(
	modify(player,'health',20);
);

//if you don't want to teleport players, you can remove this function and take out the if statement below
__tp(player) ->
(
	if(player~'y' < 0,
		modify(player,'effect','slow_falling',5,0,false,false);
		modify(player,'motion',0,0,0);
		modify(player(),'yaw',90);
		modify(player(),'pitch',0);
		modify(player,'pos',100.5,49,0.5);
		logger('Saved ' + player + ' from the void');
		print(player,format('l Saved you from the void'));
	);
	global_tp = false;
);

__on_player_takes_damage(player,amount,source,source_entity) ->
(
	if(source == 'outOfWorld',
		schedule(1,'__healplayer',player,amount);
		//if you don't want to teleport players, just remove the following if statement
		if(!global_tp,
			schedule(200,'__tp',player);
			print(player,'If you do not leave the void, you will be returned to the end platform in 10 seconds.');
			global_tp = true;
		);
	);
);