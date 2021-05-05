__config() ->
(
	//checks for pre 1.17 game version
	if(split('\\.',system_info('game_target')):1 > 16,
		exit();
	);
	
	//just some handy global variables
	global_block = null;
	global_data = m();
	global_ores = l(
		'coal_ore',
		'iron_ore',
		'gold_ore',
		'diamond_ore',
		'lapis_ore',
		'emerald_ore',
		'redstone_ore'
	);
	
	return(
		m(
			l('scope','player'),
			l('stay_loaded',true),
			
			//need to set up histogram command to let any player use it
			//this can be removed if you don't want players to abuse it as
			//  they can check before and after logging to manually find ores
			l('commands',
				m(
					l('<base> <ore>','__hist') 
				)
			),
			l('arguments',
				m(
					l('base',
						m(
							l('type','term'),
							l('options',
								l('gravel','nonswamp_clay','swamp_clay')
							)
						)
					),
					l('ore',
						m(
							l('type','term'),
							l('options',
								global_ores
							)
						)
					)
				)
			)
		)
	)
);

//checks for necessary files and creates them if they don't exist
__initialize() ->
(
	files = list_files('','text');
	gravelflag = false;
	swampclayflag = false;
	nonswampclayflag = false;
	blockflag = false;
	for(files,
		if(_ == 'gravel',
			gravelflag = true,
			_ == 'swamp_clay',
			swampclayflag = true,
			_ == 'nonswamp_clay',
			nonswampclayflag = true,
			_ == 'loggedblocks',
			blockflag = true;
		);
	);
	if(!gravelflag,
		write_file('gravel','text',encode_json(__return_zeros()));
	);
	if(!swampclayflag,
		write_file('swamp_clay','text',encode_json(__return_zeros()));
	);
	if(!nonswampclayflag,
		write_file('nonswamp_clay','text',encode_json(__return_zeros()));
	);
	if(!blockflag,
		write_file('loggedblocks','text',encode_json(m()));
	);
);

//chooses to scan for ores or display ores depending on what the player holds
__choose(player,block) ->
(
	item = player~'holds':0;
	if(item~'sword',
		__get_ores(block),
		item~'pickaxe',
		__display_ores(block);
	);
);

//scans the region under a block for every ore
__get_ores(block) ->
(
	p = player();
	blocks = decode_json(read_file('loggedblocks','text')):0;
	if(has(blocks,'[' + join(', ',pos(block)) + ']'),
		print(p,'Block has already been tested');
		return();
	);

	blockz = pos(block):2;
	chunkz = blockz % 16;
	chunkminz = blockz - __posmod(blockz,16);
	chunkmaxz = chunkminz + 15;
	blockx = pos(block):0;
	chunkx = blockx % 16;
	
	//print('Block z : ' + blockz);
	//print('Chunk z : ' + chunkz);
	//print(p,'Chunk z bounds : ' + l(chunkminz,chunkmaxz));
	//print(p,'Chunk x : ' + chunkx);
	
	//if(chunkx < -1,
		
		searchminx = blockx - 1;
		searchmaxx = blockx + 1;
		
		//,
		
		//chunkx == -1,
		//searchminx = blockx - 1;
		//searchmaxx = blockx,
		//chunkx == 0,
		//searchminx = blockx;
		//searchmaxx = blockx + 1;
	//);
	
	//draws a box to show you what area it's scanning
	draw_shape('box',100,'fill',0xFFFFFF11,'from',l(searchminx,62,chunkminz),'to',l(searchmaxx+1,63,chunkmaxz+1));
	
	flagmap = __return_zeros();
	
	volume(l(searchminx,1,chunkminz),l(searchmaxx,62,chunkmaxz),
		offset = _z - blockz;
		if(offset < 0,
			offset = offset + 16;
		);
		if(flagmap:str(_):offset == 0,
			flagmap:str(_):offset = 1;
		);
	);
	
	//__printdata(flagmap);

	print(p,'Swap hands in the next 5 seconds to log this data.');
	global_swap = 1;
	schedule(100,'__reset_swap');
	global_block = copy(block);
	global_data = copy(flagmap);
);

//indicates where a player should mine for each ore given a starting block
__display_ores(block) ->
(
	if(block == 'gravel',
		file = 'gravel',
		block == 'clay',
		if(biome(block) == 'swamp' || biome(block) == 'swamp_hills',
			file = 'swamp_clay',
			file = 'nonswamp_clay';
		);
	);
	table = decode_json(read_file(file,'text')):0;
	maxmap = m();
	loop(16,
		put(maxmap,_,l());
	);
	for(global_ores,
		data = table:_;
		ore = _;
		max = max(values(data));
		for(keys(data),
			value = data:_;
			if(value == max,
				if(maxmap:_ == null,
					maxmap:_ = l(ore),
					maxmap:_ += ore;
				);
				break();
			);
		);
	);
	
	//initialize list of shape and map of text and fill colors
	shapelist = l();
	namemap = m(
		l('C',l(0x000000FF,0x00000033)),
		l('I',l(0xC0C0C0FF,0xC0C0C033)),
		l('G',l(0xFFFF00FF,0xFFFF0033)),
		l('D',l(0x00FFFFFF,0x00FFFF33)),
		l('L',l(0x0000FFFF,0x0000FF33)),
		l('E',l(0x00FF00FF,0x00FF0033)),
		l('R',l(0xFF0000FF,0xFF000033))
	);
	x = pos(block):0;
	z = pos(block):2;
	chunkmaxz = z - __posmod(z,16) + 15 + 1;
	for(keys(maxmap),
		ores = maxmap:_;
		length = length(ores);
		offset = number(_);
		if(length > 0,
			for(ores,
				angle = __offset(_i,length);
				labelx = x + 0.5 + angle:0;
				labely = 63 + 0.5 + angle:1;
				labelz = z + 0.5 + offset;
				if(labelz > chunkmaxz,
					labelz = labelz - 16;
				);
				name = upper(slice(_,0,1));
				
				shapelist += l('label',300,'color',namemap:name:0,'fill',namemap:name:1,'text',name,'pos',l(labelx,labely,labelz),'size',20);
			);
		);
	);
	draw_shape(shapelist);
);

//angular offset necessary when more than one ore is likely at the same position
__offset(num,den) ->
(
	amplitudes = m(
		l(1,0),
		l(2,0.25),
		l(3,0.3),
		l(4,0.35),
		l(5,0.4),
		l(6,0.45),
		l(7,0.5)
	);
	angle = 360 / den;
	if(den % 2 == 0,
		phase = angle / 2 + 90,
		phase = 90;
	);
	total = phase + num * angle;
	amp = amplitudes:den;
	return(l(amp*cos(total),amp*sin(total)));
);

//returns map of ores with 0 at all offset positions
//kinda wish we had MATLAB's zeros()
__return_zeros() ->
(
	zeromap = m();
	zeros = m();
	loop(16,
		put(zeros,_,0);
	);
	for(global_ores,
		put(zeromap,_,copy(zeros));
	);
	return(zeromap);
);

//resets the count for a player triggering data logging
__reset_swap() ->
(
	p = player();
	if(global_swap == 2,
		__logdata();
		print(p,'Logged data')
	);
	global_swap = 0;
	global_block = null;
	global_data = m();
);

//prints a map of data revealing the results of a scan
__printdata(flagmap) ->
(
	p = player();
	for(keys(flagmap),
		print(title(split('\\_',_):0));
		ore = _;
		printlist = l();
		for(keys(flagmap:_),
			if(flagmap:ore:_,
				color = 'l ',
				color = 'r ';
			);
			offset = color + _;
			printlist += offset;
			if(_ != length(flagmap:ore) - 1,
				printlist += 'w ,';
			);
			
		);
		print(p,format(printlist));
	);
);

//adds most recent data to overall data
__logdata() ->
(
	blocks = decode_json(read_file('loggedblocks','text')):0;
	put(blocks,pos(global_block),0);
	delete_file('loggedblocks','text');
	write_file('loggedblocks','text',encode_json(blocks));
	
	if(global_block == 'gravel',
		file = 'gravel',
		global_block == 'clay',
		if(biome(global_block) == 'swamp',
			file = 'swamp_clay',
			file = 'nonswamp_clay'
		);
	);
	
	total = decode_json(read_file(file,'text')):0;
	for(keys(total),
		ore = _;
		for(keys(total:ore),
			total:ore:_ = total:ore:_ + global_data:ore:number(_);
		);
	);
	
	delete_file(file,'text');
	write_file(file,'text',encode_json(total));
);

//constructs a histogram of data for a particular base block and ore
__hist(base,ore) ->
(
	p = player();
	
	divisions = 8;
	rows = 6;
	//notepad++ won't show the non-half fractional blocks but the following list is
	//  from full block to one-eighth block in one-eighth block increments
	blocks = l('█','▇','▆','▅','▄','▃','▂','▁');
	
	data = decode_json(read_file(base,'text')):0:ore;
	max = max(values(data));
	sum = 0;
	for(keys(data),
		value = data:_;
		if(value == max,
			maxkey = _;
		);
		sum = sum + value;
		data:_ = round(divisions*rows*value/max);
	);
	
	print(p,title(join(' ',split('_',ore))) + ' Distribution in ' + title(join(' ',split('_',base))));
	print(p,round(100*max/sum) + '% of ore blocks at z offset of ' + maxkey + ' block' + bool(number(maxkey)-1)*'s');
	c_for(y = rows-1, y >= 0, y += -1,
		line = l('w |');
		for(range(0,16),
			count = data:str(_);
			line += 'k \'\'\'';
			//yes, i'm using three single quotes for spacing
			//they are equal in width to the block, █(alt 219), in minecraft chat
			blockflag = false;
			for(range(divisions - 1,-1,-1),
				if(count > divisions * y + _,
					line += 'l ' + blocks:(7 - _);
					blockflag = true;
					break();
				);
			);
			if(!blockflag,
				line += 'k \'\'\'';
			);
		);
		line += 'k \'\'\'';
		line += 'w |';
		print(p,format(line));
	);
	print(p,'--------------------------------------------------');
	numberlist = l(
		'w    0   ',
		'w 1   ',
		'w 2   ',
		'w 3   ',
		'w 4   ',
		'w 5   ',
		'w 6   ',
		'w 7   ',
		'w 8   ',
		'w 9  ',
		'w 10  ',
		'w 11 ',
		'w 12  ',
		'w 13 ',
		'w 14  ',
		'w 15  '
	);
	print(p,format(numberlist));
);

//necessary because scarpet returns -1 for -17 % 16
__posmod(number,modulus) ->
(
	output = number % modulus;
	if(output < -0.5,
		output = output + modulus;
	);
	return(output);
);

//check to see that the files are there on startup
__initialize();

//used to trigger data logging
__on_player_swaps_hands(player) ->
(
	if(global_swap < 2,
		global_swap = global_swap + 1;
	);
	schedule(0,'__reset_swap');
);

//used to log data or show mining spots
__on_player_clicks_block(player,block,face) ->
(
	if(block == 'gravel' || block == 'clay',
		__choose(player,block);
	);
);