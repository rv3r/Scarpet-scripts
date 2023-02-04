__config() ->
(
	{
		['stay_loaded',true],
		['scope','player']
	}
);

import('projectile_hit','__handle_entity_hits');
__handle_entity_hits('potion');
// handle lingering potions hitting bows to give the bow the required status effect
handle_event('projectile_hit', 
	_(args) -> 
	if((args:2 ~ 'item'):0 ~ 'bow$' && args:0 ~ 'nbt':'Item':'id' == 'minecraft:lingering_potion',
		effect = args:0 ~'nbt':'Item':'tag':'Potion' - 'minecraft:';
		__update_bow_nbt(args:2, effect);
		print(args:1,format(str('l Applied %s effect to a bow', title(replace(effect,'_',' ')))))
	)
);

__on_player_releases_item(player, item_tuple, hand) ->
(
	[item, count, nbt] = item_tuple;
	if(item ~ 'bow$' && effect = nbt:'potion_effect',
		// arrow starts 0.1 blocks below player eyes
		arrow = entity_area('arrows',player ~ 'pos' + [0,player ~ 'eye_height' - 0.1,0],[0.000001,0.000001,0.000001]):0;
		// get rid of arrow, all we need is the position and nbt
		modify(arrow,'remove');
		new_nbt = copy(arrow ~ 'nbt');
		// set item as the potion
		new_nbt:'Item' = encode_nbt(
			{
				['Count',1],
				['id','minecraft:splash_potion'],
				['tag',
					{
						['Potion','minecraft:' + effect]
					}
				]
			}
		);
		// spawn the potion where the arrow was
		spawn('potion',arrow ~ 'pos',new_nbt);
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