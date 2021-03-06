__config() ->
(
	import('inventory',
		'__swap',
		'__swapto',
		'__findcomplex'
	);
	return(
		m(
			l('scope','player'),
			l('stay_loaded',true),
			
			l('commands',
				m(
					l('','__sort'),
					l('setup',['__setup',0,0]),
					l('write_slot <playerSlot> <type> <choice>', 'write_slot') 
				)
			),
			l('arguments',
				m(
					l('playerSlot',
						m(
							//don't use type 'slot' because we're dealing with integer slots, not strings
							l('type','int'),
							l('min',0),
							l('max',40)
						)
					),
					l('type',
						m(
							l('type','term'),
							l('options',
								l('category','variant','sort','stack','item')
							)
						)
					),
					l('choice',
						m(
							l('type','term')
						)
					)
				)
			)
		)
	)
);

//necessary variables

//giant food lookup table for proper sorting until we get item_property() function
global_food = m(
	l('apple',
		m(
			l('hunger',4),
			l('saturation',2.4)
		)
	),
	l('baked_potato',
		m(
			l('hunger',5),
			l('saturation',6)
		)
	),
	l('beetroot',
		m(
			l('hunger',1),
			l('saturation',1.2)
		)
	),
	l('beetroot_soup',
		m(
			l('hunger',0),//actually 6 but 0 for sorting purposes
			l('saturation',0)//7.2
		)
	),
	l('bread',
		m(
			l('hunger',5),
			l('saturation',6)
		)
	),
	l('cake',
		m(
			l('hunger',0),//14
			l('saturation',0)//2.8
		)
	),
	l('carrot',
		m(
			l('hunger',3),
			l('saturation',3.6)
		)
	),
	l('chorus_fruit',
		m(
			l('hunger',4),
			l('saturation',2.4)
		)
	),
	l('cooked_beef',
		m(
			l('hunger',8),
			l('saturation',12.8)
		)
	),
	l('cooked_chicken',
		m(
			l('hunger',6),
			l('saturation',7.2)
		)
	),
	l('cooked_cod',
		m(
			l('hunger',5),
			l('saturation',6)
		)
	),
	l('cooked_mutton',
		m(
			l('hunger',6),
			l('saturation',9.6)
		)
	),
	l('cooked_porkchop',
		m(
			l('hunger',8),
			l('saturation',12.8)
		)
	),
	l('cooked_rabbit',
		m(
			l('hunger',5),
			l('saturation',6)
		)
	),
	l('cooked_salmon',
		m(
			l('hunger',6),
			l('saturation',9.6)
		)
	),
	l('cookie',
		m(
			l('hunger',2),
			l('saturation',0.4)
		)
	),
	l('dried_kelp',
		m(
			l('hunger',1),
			l('saturation',0.6)
		)
	),
	l('enchanted_golden_apple',
		m(
			l('hunger',4),
			l('saturation',9.6)
		)
	),
	l('golden_apple',
		m(
			l('hunger',4),
			l('saturation',9.6)
		)
	),
	l('golden_carrot',
		m(
			l('hunger',6),
			l('saturation',14.4)
		)
	),
	l('honey_bottle',
		m(
			l('hunger',6),
			l('saturation',14.4)
		)
	),
	l('melon_slice',
		m(
			l('hunger',2),
			l('saturation',1.2)
		)
	),
	l('mushroom_stew',
		m(
			l('hunger',0),//6
			l('saturation',0)//7.2
		)
	),
	l('poisonous_potato',
		m(
			l('hunger',2),
			l('saturation',1.2)
		)
	),
	l('potato',
		m(
			l('hunger',1),
			l('saturation',0.6)
		)
	),
	l('pufferfish',
		m(
			l('hunger',1),
			l('saturation',0.2)
		)
	),
	l('pumpkin_pie',
		m(
			l('hunger',8),
			l('saturation',4.8)
		)
	),
	l('rabbit_stew',
		m(
			l('hunger',0),//10
			l('saturation',0)//12
		)
	),
	l('raw_beef',
		m(
			l('hunger',3),
			l('saturation',1.8)
		)
	),
	l('raw_chicken',
		m(
			l('hunger',2),
			l('saturation',1.2)
		)
	),
	l('raw_cod',
		m(
			l('hunger',2),
			l('saturation',0.4)
		)
	),
	l('raw_mutton',
		m(
			l('hunger',2),
			l('saturation',1.2)
		)
	),
	l('raw_porkchop',
		m(
			l('hunger',3),
			l('saturation',1.8)
		)
	),
	l('raw_rabbit',
		m(
			l('hunger',3),
			l('saturation',1.8)
		)
	),
	l('raw_salmon',
		m(
			l('hunger',2),
			l('saturation',0.4)
		)
	),
	l('rotten_flesh',
		m(
			l('hunger',4),
			l('saturation',0.8)
		)
	),
	l('suspicious_stew',
		m(
			l('hunger',0),//6
			l('saturation',0)//7.2
		)
	),
	l('spider_eye',
		m(
			l('hunger',2),
			l('saturation',3.2)
		)
	),
	l('steak',
		m(
			l('hunger',8),
			l('saturation',12.8)
		)
	),
	l('sweet_berries',
		m(
			l('hunger',2),
			l('saturation',0.4)
		)
	),
	l('tropical_fish',
		m(
			l('hunger',1),
			l('saturation',0.2)
		)
	)
);

//material lookup table
global_material = m(
	l('netherite',7),
	l('diamond',6),
	l('turtle',5),
	l('golden',4),
	l('iron',3),
	l('chainmail',2),
	l('stone',1),
	l('wooden',0)
);

//things that are based on materials
global_material_based = l(
	'_pickaxe',
	'_sword',
	'_axe',
	'_shovel',
	'_hoe',
	'_helmet',
	'_chestplate',
	'_leggings',
	'_boots'
);

//items that have multiple options
// logs, ores, wools, stairs, etc
global_multivariate = l(
	'_nylium',
	'_planks',
	'_sapling',
	'_ore',
	'_log',
	'_wood',
	'_boat',
	'_leaves',
	'_rail',
	'_wool',
	'_block',
	'_slab',
	'_stairs',
	'_pressure_plate',
	'_fence',
	'_trapdoor',
	'_stone_bricks',
	'_fence_gate',
	'_button',
	'_wall',
	'_terracotta',
	'_carpet',
	'_stained_glass',
	'_stained_glass_pane',
	'shulker_box',
	'_glazed_terracotta',
	'_concrete',
	'_concrete_powder',
	'_coral_block',
	'_coral',
	'_coral_fan',
	'_door',
	'_ingot',
	'_sign',
	'_bucket',
	'_dye',
	'_bed',
	'_potion',
	'_spawn_egg',
	'enchanted_book',
	'_horse_armor',
	'_banner',
	'splash_potion',
	'_arrow',
	'music_disc_',
	'lingering_potion',
	'_banner_pattern',
	'_on_a_stick',
	'_pickaxe',
	'_sword',
	'_axe',
	'_shovel',
	'_hoe',
	'_helmet',
	'_chestplate',
	'_leggings',
	'_boots'
);

//did we ask the player about a choice, and did they answer
//	wouldn't want to ask them again now would we
global_ask = l(
	l(false,false),
	l(false,false),
	l(false,false),
	l(false,false)
);

__initialize() ->
(
	if(load_app_data() == null,
		store_app_data(encode_nbt(m()));
	);
);

__setup(slot,choice) ->
(
	//print(player(),'----------------------------');
	//print(player(),'current choice : ' + choice);
	p = player();
	if(inventory_has_items(p),
		if(item_tuple = inventory_get(p,slot),
			//print(player(),slot);
			l(item, count, nbt) = item_tuple;
			string_list = l();
				
			if(choice == 0,
				
				write_slot(slot,'category',item_category(item));
				
				//introduce the slot and item at hand
				string_list += 'e Slot ' + (slot+1) + ' ';
				string_list += 'e contains ' + count + ' of the following item: ' + title(replace(item,'_',' ') + '');
				
				global_ask = l(
					l(false,false),
					l(false,false),
					l(false,false),
					l(false,false)
				);
				
				//print(player(),'choice 1');
				
				//check to see if we should ask the player about variants of this item
				//	only if it wasn't a tool, sword, armor, or food
				//	done using multivariate list
				//defaults to any variant
				
				variant = '';
				for(global_multivariate,
					if(item ~ _,
						variant = _;
						break();
					);
				);
				if(variant,
					string_list += 'd \nAre all of the following item variants allowed?\n ';
					variant_list = l();
					for(item_list(),
						if(_ ~ variant,
							type = split(variant,_):0;
							name = title(replace(type,'_',' '));
							variant_list += name;
						);
					);
					string_list += 'w ' + join(', ',variant_list) + '\n ';
					string_list += 'c [Yes] ';
					string_list += '^y Allows any item variant';
					string_list += '!/invsort write_slot ' + slot + ' variant true';
					string_list += 'c [No] ';
					string_list += '^y Allows only the current item variant';
					string_list += '!/invsort write_slot ' + slot + ' variant false',
					global_ask:0:1 = true;
				);
				global_ask:0:0 = true,
				
				choice == 1 && !global_ask:1:0 && global_ask:0:1,
				//print(player(),'choice 2');
				
				//if tool, sword, or armor, ask about sorting options
				//	material (default 1)
				//	durability remaining (default 2)
				//	enchantments
				//	any
				//these can be chosen in any order
				
				//if food, ask about sorting options
				//	hunger haunches restored (default)
				//	saturation restored
				//	any
				//these can be chosen in any order
				
				//do we even ask about materials
				material_flag = false;
				for(global_material_based,
					if(item ~ _,
						material_flag = true;
						break();
					);
				);
				
				//maybe its something else that we should ask about
				other_flag = false;
				for(
					l('flint_and_steel',
						'fishing_rod',
						'shears',
						'bow',
						'shield',
						'trident',
						'crossbow',
						'carrot_on_a_stick',
						'warped_fungus_on_a_stick',
						'elytra'
					),
					if(item ~ _,
						other_flag = true;
						break();
					);
				);
				
				if(material_flag || other_flag || has(global_food,item),
					string_list += 'd How should valid items be sorted?\n';
					string_list += 'd (feel free to choose more than one option)\n ';
					
					if(has(global_food,item),
						string_list += 'c [Hunger] ';
						string_list += '^y Picks foods with higher hunger first';
						string_list += '!/invsort write_slot ' + slot + ' sort hunger';
						string_list += 'c [Saturation] ';
						string_list += '^y Picks foods with higher saturation first';
						string_list += '!/invsort write_slot ' + slot + ' sort saturation';
						string_list += 'c [Any] ';
						string_list += '^y Picks items in whatever order they are found';
						string_list += '!/invsort write_slot ' + slot + ' sort any',
					
						if(material_flag,
							string_list += 'c [Material] ';
							string_list += '^y Picks items made of rarer materials first';
							string_list += '!/invsort write_slot ' + slot + ' sort material';
						);
						string_list += 'c [Durability] ';
						string_list += '^y Picks items with higher durability first';
						string_list += '!/invsort write_slot ' + slot + ' sort durability';
						string_list += 'c [Enchantments] ';
						string_list += '^y Picks enchanted items first';
						string_list += '!/invsort write_slot ' + slot + ' sort enchantments';
						string_list += 'c [Any] ';
						string_list += '^y Picks items in whatever order they are found';
						string_list += '!/invsort write_slot ' + slot + ' sort any'
					),
					global_ask:1:1 = true;
				);
				global_ask:1:0 = true,
				
				//print(player(),'done with choice 2'),
				
				choice == 2 && !global_ask:2:0 && global_ask:1:1,
				//if stackable, check to see if they want larger or smaller stack sizes
				//	larger (default)
				//	smaller
				//	any
				//mutually exclusive
				
				if(stack_limit(item) > 1,
					string_list += 'd Which stacks should be prioritized?\n ';
					string_list += 'c [Larger] ';
					string_list += '^y Picks stacks with more items first';
					string_list += '!/invsort write_slot ' + slot + ' stack more';
					string_list += 'c [Smaller] ';
					string_list += '^y Picks stacks with fewer items first';
					string_list += '!/invsort write_slot ' + slot + ' stack fewer';
					string_list += 'c [Any] ';
					string_list += '^y Picks items in whatever order they are found';
					string_list += '!/invsort write_slot ' + slot + ' stack any',
					global_ask:2:1 = true;
				);
				global_ask:2:0 = true,
				
				choice == 3 && !global_ask:3:0 && global_ask:2:1,
				//ask if they want that exact item or its variants, depending on what they chose earlier
				
				currentdata = parse_nbt(load_app_data());
				playerdata = currentdata:str(p);
				variant = playerdata:str(slot):'variant';
				//print(player(),variant);
				if(!variant,
					string_list += 'd Should this slot be restricted to just this exact item?\n ';
					string_list += 'c [Yes] ';
					string_list += '^y Allows only this exact item, ignoring nbt, making it empty if no valid item is found',
					variant,
					string_list += 'd Should this slot be restricted to just this item and its variants?\n ';
					string_list += 'c [Yes] ';
					string_list += '^y Allows only this this item and its variants, ignoring nbt, making it empty if no valid item is found',
				);
				string_list += '!/invsort write_slot ' + slot + ' item ' + item;
				string_list += 'c [No] ';
				string_list += '^y Allows any valid item, allowing the slot to be left with any item if no valid item is found';
				string_list += '!/invsort write_slot ' + slot + ' item false';
				global_ask:3:0 = true;
			);
			if(string_list,
				print(p,format(string_list));
			);
			if(global_ask:(choice):1,
				schedule(0,'__setup',slot,choice+1);
			),
			if(slot < inventory_size(p) - 1,
				schedule(0,'__setup',slot+1,0);
			);
		);
	);
);

write_slot(slot,type,choice) ->
(
	p = player();
	
	//this type only supports booleans, not strings
	//  but the only generic way to do the command is with terms
	if(type == 'variant',
		if(choice == 'true',
			choice = true;
			choice == 'false',
			choice = false;
		),
		type == 'item',
		if(choice == 'false',
			choice = '';
		);
	);
	
	//print(player(),slot + ' : ' + type + ' : ' + choice);
	currentdata = parse_nbt(load_app_data());
	playerdata = currentdata:str(p);
	
	//construct variables that might not exist yet
	if(playerdata == null,
		playerdata = m();
	);
	slotdata = playerdata:str(slot);
	if(slotdata == null,
		slotdata = m();
	);
	if(slotdata:type == null,
		slotdata:type = '',
	);
	
	//print(player(),slotdata):'category';
	
	//put the relevant data where it needs to go, putting sorting options at the end of the list
	if(type != 'sort',
		put(slotdata:type,choice),
		
		type == 'sort',
		sorter = slotdata:'sort';
		if(type(sorter) != 'list',
			put(slotdata,type,l(choice)),
			put(sorter,null,choice);
			for(sorter,
				if(_ == choice && _i < length(sorter)-1,
					delete(sorter,_i);
					break();
				)
			);
			put(slotdata,'sort',sorter);
		);
	);
	
	//print(player(),slotdata):'category';
	
	//package everything up...
	put(playerdata,str(slot),slotdata);
	put(currentdata,str(p),playerdata);
	
	//... and attempt to save it, reporting the result to the player
	if(bool = store_app_data(encode_nbt(currentdata)),
		if(type != 'category',
			if(bool,
				list = 'l Saved choice',
				list = 'r Failed to save choice';
			);
		);
	);
	if(list,
		print(p,format(list));
	);
	
	//call __setup() for the next choice or slot based on current arguments
	//	and report that the player answered the question
	if(slot < inventory_size(p),
		//print(player(),type + ' : ' + slot);
		if(type == 'variant',
			global_ask:0:1 = true;
			schedule(0,'__setup',slot,1),
			type == 'sort',
			global_ask:1:1 = true;
			schedule(0,'__setup',slot,2),
			type == 'stack',
			global_ask:2:1 = true;
			schedule(0,'__setup',slot,3),
			type == 'item',
			global_ask:3:1 = true;
			schedule(0,'__setup',slot+1,0)
		);
	);
);

__sort() ->
(
	p = player();
	slotlist = parse_nbt(load_app_data()):str(p);
	//print(player(),'------------------------------');
	//print(player(),invlist);
	
	slots = keys(slotlist);
	for(slots,
		slots:_i = number(_);
	);
	slots = sort(slots);
	for(slots,
		preferences = slotlist:str(_);
		//print(p,'sorting slot ' + _);
		category = preferences:'category';
		items = __category_find(p,category);
		//print(p,_ + ' (unsorted): ' + items);
		option = preferences:'item';
		//print(p,option);
		if(option != '' && !preferences:'variant',
			items = filter(items,
				_:0 == option;
			);
		);
		if(preferences:'variant',
			for(global_multivariate,
				if(option ~ _,
					variant = _;
					break();
				);
			);
			items = filter(items,
				_:0 ~ variant;
			);
		);
		//print(p,_ + ' (filtered): ' + items);
		items = __sort_items(items,preferences:'sort');
		if(length(items) && stack_limit(items:0:0) > 1,
			items = __sort_items(items,preferences:'stack');
		);
		//print(p,_ + ' (sorted): ' + items);
		if(length(items) && inventory_get(p,_) != items:0,
			oldslot = __findcomplex(p,items:0):0;
			//print(p,'old slot : ' + oldslot);
			newslot = _;
			__swap(p,oldslot,newslot),
			!length(items) && options != '',
			__swap(p,inventory_find(p,null),_);
		);
		//print(player(),'--------');
	);
	//print(player(),'sort');
);


__sort_items(item_list,sort) ->
(
	if(sort == '' || length(item_list) < 2,
		return(item_list);
	);
	sorted = sort_key(
		item_list,
		keys = l();
		value = _;
		if(type(sort) != 'list',
			sort = l(sort);
		);
		for(sort,
			if(_ == 'material',
				keys:_i = -1*global_material:(split('_',value:0):0),
				_ == 'damage',
				keys:_i = value:2:'Damage',
				_ == 'hunger',
				keys:_i = -1*global_food:(value:0):'hunger',
				_ == 'saturation',
				keys:_i = -1*global_food:(value:0):'saturation',
				_ == 'enchantments',
				keys:_i = -1*bool(value:2:'Enchantments'),
				_ == 'more',
				keys:_i = -1*value:1,
				_ == 'fewer',
				keys:_i = value:1
			)
		);
		keys
	);
	return(sorted);
);

__category_find(player,category) ->
(
	item_list = l();
	if(inventory_has_items(player),
		loop(inventory_size(player),
			if(inventory_get(player,_) && item_category(inventory_get(player,_):0) == category,
				item_list += inventory_get(player,_);
				//print(player(),inventory_get(player,_));
			);
		);
	);
	return(item_list);
);

__initialize();