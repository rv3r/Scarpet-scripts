__config() ->
(
	{
		['stay_loaded',true],
		['scope','player']
	}
);

// uncomment this region and add my projectile_hit.scl library to allow direct hits with a lingering potion to apply effects to bows and crossbows
// allows you to apply water, mundane, thick, and awkward to your weapon

// import('projectile_hit','__handle_entity_hits');
// __handle_entity_hits('potion');
// // handle lingering potions hitting bows to give the bow the required status effect
// handle_event('projectile_hit', 
// 	_(args) -> 
// 	if(((args:2 ~ 'item'):0 == 'bow' || (args:2 ~ 'item'):0 == 'crossbow') && args:0 ~ 'nbt':'Item':'id' == 'minecraft:lingering_potion',
// 		effect = args:0 ~'nbt':'Item':'tag':'Potion' - 'minecraft:';
// 		__update_bow_nbt(args:2, effect);
// 		print(args:1,format(str('l Applied %s effect to a %s', title(replace(effect,'_',' ')), title(args:2~'item':0))))
// 	)
// );

entity_load_handler('area_effect_cloud', _(e, new) ->
	entity_event(e, 'on_tick',
		effect = (e ~ 'nbt'):'Potion';
		if(effect == null,
			particle = e ~ 'nbt':'Particle';
			if(particle == 'minecraft:dragon_breath',
				effect = particle - 'minecraft:',
				return();
			),
			effect = effect - 'minecraft:';
		);
		_(e, outer(effect)) -> (
			for(
				filter(
					entity_area('item',e~'pos',[radius = e ~ 'nbt':'Radius',0.5,radius]),
					item_tuple = _~'item';
					(item_tuple:0 == 'bow' || item_tuple:0 == 'crossbow') && item_tuple:2:'potion_effect' != effect
				),
				__update_bow_nbt(_, effect);
				print(player(),format(str('l Applied %s effect to a %s', title(replace(effect,'_',' ')), title(_~'item':0))))
			);
		);
	);
);

__on_player_uses_item(player, item_tuple, hand) ->
(
	// schedule since crossbow arrows don't exist until the end of the tick wtf
	schedule(0,'__replace_arrow_with_potion',player, item_tuple)
);

__on_player_releases_item(player, item_tuple, hand) ->
(
	__replace_arrow_with_potion(player, item_tuple)
);

__replace_arrow_with_potion(player, item_tuple) ->
(
	[item, count, nbt] = item_tuple;
	if(!nbt,
		return();
	);
	if((item == 'bow' || item == 'crossbow') && effect = nbt:'potion_effect',
		// arrow starts 0.1 blocks below player eyes
		// gets a big range in case you're moving
		// so also filter for fresh arrows
		arrow = filter(entity_area('arrows',player ~ 'pos' + [0,player ~ 'eye_height' - 0.1,0],[1,1,1]), _ ~ 'age' == 0):0;
		if(!arrow,
			return();
		);
		// get rid of arrow, all we need is the position and nbt
		modify(arrow,'remove');
		new_nbt = copy(arrow ~ 'nbt');
		// set item as the potion or dragon fireball
		if(effect == 'dragon_breath',
			projectile = 'dragon_fireball';
			// merge bc I suck at nbt manipulation
			put(new_nbt,'Motion',
				encode_nbt(
					// make it a little faster
					[
						
						2 * arrow ~ 'motion'
					]
				),
				'merge'
			);
			// merge bc I suck at nbt manipulation
			put(new_nbt,'power',
				encode_nbt(
					// give the fireball some acceleration to increase maximum range
					[
						0.05 * arrow ~ 'motion'
					]
				),
				'merge'
			),
			projectile = 'potion';
			// merge bc I suck at nbt manipulation
			put(new_nbt,'Item',
				encode_nbt(
					{
						['Count',1],
						['id','minecraft:splash_potion'],
						['tag',
							{
								['Potion','minecraft:' + effect]
							}
						]
					}
				),
				'merge'
			);
		);
		// spawn the new projectile where the arrow was
		spawn(projectile,arrow ~ 'pos',new_nbt);
	);
);

__update_bow_nbt(bow, effect) ->
(
	bow_nbt = bow ~ 'nbt';
	if(bow_nbt:'Item':'tag':'potion_effect' == null,
		modify(bow,'nbt_merge',
			encode_nbt(
				{
					['Item',
						{
							['tag',
								{
									['potion_effect',effect]
								}
							]
						}
					]
				}
			)
		);
		bow_nbt = bow ~ 'nbt',
		parsed = parse_nbt(bow_nbt);
		parsed:'Item':'tag':'potion_effect' = effect;
		modify(bow,'nbt',encode_nbt(parsed));
	);
	bow_nbt = bow ~ 'nbt';
	if(effect == 'water',
		lore = 'Arrows turn into Splash Water Bottles',
		['thick', 'mundane', 'awkward'] ~ effect != null,
		lore = str('Arrows turn into Splash %s Potions',title(effect)),
		effect == 'dragon_breath',
		lore = 'Arrows turn into Dragon Fireballs',
		prefix = 'Arrows turn into Splash Potions of ';
		effect_name = title(replace(effect, '_', ' '));
		lore = prefix + effect_name;
	);
	lore_entry = '{"text":"' + lore + '"}';
	if(bow_nbt:'Item':'tag':'display':'Lore' == null,
		modify(bow,'nbt_merge',
			encode_nbt(
				{
					['Item',
						{
							['tag',
								{
									['display',
										{
											['Lore',
												[lore_entry]
											]
										}
									]
								}
							]
						}
					]
				}
			)
		);
		bow_nbt = bow ~ 'nbt',
		parsed = parse_nbt(bow_nbt);
		parsed:'Item':'tag':'display':'Lore':0 = lore_entry;
		modify(bow,'nbt',encode_nbt(parsed));
	)
);
