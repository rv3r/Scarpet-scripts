__config() ->
(
	//stats to refresh showing of current face
	global_statsList = l(
		'walk_one_cm',
		'sprint_one_cm',
		'fly_one_cm',
		'crouch_one_cm',
		'jump',
		'aviate_one_cm',
		'walk_under_water_one_cm',
		'walk_on_water_one_cm',
		'swim_one_cm'
	);
	
	//lists of important stuff
	global_currentpoints = l();
	global_points = l();
	global_edges = l();
	global_faces = l();
	global_currentedge = l();
	global_currentface = l();
	global_reorder = l();
	global_suggestion_bools = l(true,true);
	
	//important but just not a list
	global_axis = null;
	
	//import from personal math library
	import('rv3r_math',
		'__magnitude',
		'__unit',
		'__roundnum',
		'__sum',
		'__sign',
		'__det',
		'__vector',
		'__dot',
		'__heron'
	);
	
	return(
		m(
			l('scope','player'),
			l('stay_loaded',true),
			
			l('commands',
				m(
					l('perimeter','perimeter'),
					l('area','area'),
					l('volume','vol'),
					l('logdata','logdata'),
					l('show <showmode>','show'),
					l('clear',['clear','all']),
					l('clear <clearmode>','clear'),
					l('fill <fillmode> <block>',['fill',false,false]),
					l('fill <fillmode> <block> <replace>',['fill',false]),
					l('fill <fillmode> <block> <replace> <update>',['fill']),
					l('extrude <pos>',['extrude',1,false,false]),
					l('extrude <pos> <period>',['extrude',false,false]),
					l('extrude <pos> <period> <replace>',['extrude',false]),
					l('extrude <pos> <period> <replace> <update>',['extrude'])
				)
			),
			
			l('arguments',
				m(
					l('showmode',
						m(
							l('type','term'),
							l('options',
								l('edges','faces')
							),
							l('suggest',
								l('edges','faces')
							)
						)
					),
					l('clearmode',
						m(
							l('type','term'),
							l('options',
								l('points','edges','faces','all')
							),
							l('suggest',
								l('points','edges','faces','all')
							)
						)
					),
					l('fillmode',
						m(
							l('type','term'),
							l('options',
								l('all','border')
							),
							l('suggest',
								l('all','border')
							)
						)
					),
					l('period',
						m(
							l('type','int'),
							l('min',0),
							l('max',1024),
							l('suggest',
								l(1,2,4,8,16)
							)
						)
					),
					l('replace',
						m(
							l('type','bool')
						)
					),
					l('update',
						m(
							l('type','bool')
						)
					)
				)
			)
		)
	)
);

__command() ->
(
	return();
);

//returns number of points that are not in the same x, y, or z plane as all other points
//along with main axis for the first three points and their common value
__checkpoints(pointlist) ->
(
	allpoints = copy(pointlist);
	axis = null;
	for(range(3),
		flag = true;
		testaxis = _;
		for(range(length(allpoints)),
			if(allpoints:_i:testaxis != allpoints:(_i+1):testaxis,
				flag = false;
				break();
			);
		);
		if(flag,
			value = allpoints:0:_;
			axis = _;
			break();
		);
	);
	if(axis == null,
		print(format('br Please clear all points and select points that are in the same plane.'));
		return(l(null,null,null)),
		global_axis = axis;
	);
	while(length(allpoints) > 0,length(allpoints),
		if(allpoints:0:axis == value,
			delete(allpoints,0);
		);
	);
	return(l(length(allpoints),axis,value));
);

//returns edge list length after first few paired edges are removed
//it had better be 0 if you want to find the volume
__checkedges() ->
(
	alledges = copy(global_edges);
	while(length(alledges) > 0,length(alledges),
		for(alledges,
			if(alledges:0 == l(_:1,_:0),
				delete(alledges,_i);
				delete(alledges,0);
				break();
			);
		);
	);
	return(length(alledges));
);

//sum of vector magnitudes of unpaired edges
perimeter() ->
(
	if(length(global_edges) == 0,
		print(format('br Please select at least one edge first.'));
		return();
	);
	//gets rids of EVERY duplicate edge
	alledges = copy(global_edges);
	enum = 0;
	while(length(alledges) > 0,length(alledges),
		check = alledges:enum;
		flag = true;
		for(alledges,
			if(_i > enum && alledges:enum == l(_:1,_:0),
				delete(alledges,_i);
				delete(alledges,enum);
				flag = false;
				break();
			);
		);
		if(flag,
			enum += 1;
		);
	);
	if(length(alledges) == 0,
		print(format('br This does not work on closed polyhedra.'));
		return();
	);
	__showfaces();
	//sums magnitudes of every unpaired edge
	perimeter = reduce(alledges,_a + __magnitude(__vector(_:0,_:1)),0);
	print(format('c Perimeter: ','w ' + str(__roundnum(perimeter,3))));
	return();
);

//applies Heron's semiperimeter formula to find the area of every current face
area() ->
(
	if(length(global_faces) == 0,
		print(format('br Please select at least one face first.'));
		return();
	);
	__showfaces();
	area = reduce(global_faces,_a +	__heron(__magnitude(__vector(_:0:0,_:0:1)),__magnitude(__vector(_:1:0,_:1:1)),__magnitude(__vector(_:2:0,_:2:1))),0);
	print(format('c Area: ','w ' + str(__roundnum(area,3))));
	return();
);

//applies tetrahedral shoelace method once edges are paired and faces are ordered properly
vol() ->
(
	if(length(global_edges) == 0,
		print(format('br Please select at least one edge first.'));
		return(),
		length(global_faces) == 0,
		print(format('br Please select at least one face first.'));
		return();
	);
	__showfaces();
	volume = 0;
	if(!__checkedges() && length(global_edges),
		volume = abs(reduce(global_faces,
			_a + __det(l(_:0:0,_:1:0,_:2:0)),
			0
		)/6);
		print(format('c Volume: ','w ' + str(__roundnum(volume,3))));
		check = __sanitycheck();
		if(volume > check,
			print(check);
			print('failed sanity check. please send error');
			print('  log to help find bugs.');
			logdata();
		);
		return();
	);
);

//ensures volume isnt greater than what is physically possible
__sanitycheck() ->
(
	minX = global_points:0:0;
	maxX = global_points:0:0;
	minY = global_points:0:1;
	maxY = global_points:0:1;
	minZ = global_points:0:2;
	maxZ = global_points:0:2;
	for(global_points,
		minX = min(minX,_:0);
		maxX = max(maxX,_:0);
		minY = min(minY,_:1);
		maxY = max(maxY,_:1);
		minZ = min(minZ,_:2);
		maxZ = max(maxZ,_:2);
	);
	draw_shape('box',100,'from',l(minX,minY,minZ)+0.5,'to',l(maxX,maxY,maxZ)+0.5);
	return((maxX-minX)*(maxY-minY)*(maxZ-minZ));
);

//writes down info for debugging
logdata() ->
(
	delete_file('faces','text');
	for(global_faces,
		i = _;
		write_file('faces','text','face - ' + str(_i) + ', reorder - ' + str(global_reorder:_i));
		c_for(x = 0, x < 3, x += 1,
			write_file('faces','text',str(i:x:0));
		);
	);
);

//sets a block using blocks from the player's inventory
//uses block name and state to ensure empty block inventory
__playerset(player,pos,block) ->
(
	placeable = m(
		l('tripwire','string'),
		l('wheat','wheat_seeds'),
		l('water','water_bucket'),
		l('lava','lava_bucket'),
		l('redstone_wire','redstone'),
		l('pumpkin_stem','pumpkin_seeds'),
		l('melon_stem','melon_seeds'),
		l('fire','fire_charge'),
		l('carrots','carrot'),
		l('potatoes','potato'),
		l('sweet_berry_bush','sweet_berries')
	);
	
	if(item_list() ~ block,
		item = str(block),
		item = placeable:str(block);
	);

	if(slot = inventory_find(player,item),
		if(set(pos,str(block),block_state(block)),
			result = inventory_remove(player,item);
			if(item ~ '_bucket',
				inventory_set(player,slot,1,'bucket');
			);
		);
	);
	return(result);
);

//sorts blocks to be placed so they are set in a reasonable order
__anglesort(blocklist,centroid) ->
(
	return(sort_key(blocklist,atan2(number((_-centroid):(global_axis-1)),number((_-centroid):(global_axis+1)))));
);

//fills the currently selected region ONLY if it is in one plane(xy,xz,yz) in the prescribed style
fill(fillmode,block,replace,update) ->
(
	p = player();
	if(fillmode == 'all',
		borderstyle = null;
	);
	filllist = __allfaceblocks();
	centroid = __sum(keys(filllist))/length(filllist);
	successes = 0;
	gamemode = p ~ 'gamemode';
	
	if(fillmode == 'all',
			filllist = filllist,
		fillmode == 'border',
			borderlist = m();
			for(keys(filllist),
				result = for(neighbours(_),
					has(filllist,pos(_));
				);
				if(result < 4 && !has(borderlist,_),
					borderlist += _;
				);
			);
			filllist = borderlist;
	);
	
	for(__anglesort(keys(filllist),centroid),
		if(gamemode == 'creative' && (replace || air(_)),
				result = without_updates(bool(set(_,block))),
			gamemode == 'survival' && (air(_) || liquid(_)),
				result = __playerset(p,_,block),
			result = 0;
		);
		if(gamemode == 'survival' || update,
			schedule(1,_(arg) -> update(arg),_);
		);
		successes += result;
	);
	
	print(format('l Successfully filled ' + str(successes) + ' block' + bool(successes-1) * 's'));
);

//repeats the selected region in a direction normal to the plane every <period> blocks up to <pos>
extrude(pos,period,replace,update) ->
(
	p = player();
	blockmap = __allfaceblocks();
	centroid = __sum(keys(blockmap))/length(blockmap);
	for(range(3),
		flag = true;
		test = _;
		for(keys(blockmap), 
			if(_:test != keys(blockmap):(_i+1):test,
				flag = false;
				break();
			);
		);
		if(flag,
			value = keys(blockmap):0:_;
			axis = _;
			break();
		);
	);
	
	direction = __sign(pos:axis - value);
	
	gamemode = p ~ 'gamemode';
	
	c_for(x = value + direction*period, x*direction <= (pos:axis)*direction, x += direction*period,
		for(__anglesort(keys(blockmap),centroid),
			newpos = copy(_);
			newpos:axis = x;
			if(gamemode == 'creative' && (replace || air(newpos)),
					result = without_updates(bool(set(newpos,block(_)))),
				gamemode == 'survival' && (air(newpos) || liquid(newpos)),
					result = __playerset(p,newpos,block(_)),
				result = 0;
			);
			if(gamemode == 'survival' || update,
				schedule(1,_(arg) -> update(arg),newpos);
			);
			successes += result;
		);
	);
		
	print(format('l Successfully filled ' + str(successes) + ' block' + bool(successes-1) * 's'));
);

//functions to clear current values
clear(arg) ->
(
	if(arg == 'points',
		global_points = l();
		return(),
		arg == 'edges',
		global_edges = l();
		return(),
		arg == 'faces',
		global_faces = l();
		return(),
		arg == 'all',
		global_points = l();
		global_edges = l();
		global_faces = l();
		global_currentedge = l();
		global_currentface = l();
		global_currentpoints = l();
		global_suggestion_bools = l(true,true);
		global_axis = null;
		return();
	)
);

//heart and soul of the program
//
//addpoint just, well, adds the point to a list.
//  more importantly, it calls addedge with the point
//addedge attempts to add the point to the current edge,
//  reorders the edge if necessary, calls addface,
//  checks to see if you can call volume, and can point you to your original point
//addface attempts to add the point to the current face,
//  reorders the face if necessary, and shows the face

__addpoint(triple) ->
(
	if(!filter(global_points,_ == triple),
		global_points += triple;
	);
	__addedge(triple);
);

__addedge(point) ->
(
	edgelength = length(global_currentedge);
	facelength = length(global_currentface);
	if(edgelength == 0,
		global_currentedge += point;
		global_currentpoints = l(point),
		
		
		if(
			facelength == 0 ||
			(facelength == 1 && (global_currentface:0:0 != point && global_currentface:0:1 != point)) ||
			(facelength == 2 && (global_currentface:0:0 == point || global_currentface:0:1 == point)),
			
			//runs if second face point(non origin) is added properly
			if(facelength == 1 && (global_currentface:0:0 != point && global_currentface:0:1 != point),
				global_currentpoints:1 = point;
			);
			
			//runs if player does continue to another point
			if(facelength == 2 && (global_currentface:0:0 == point || global_currentface:0:1 == point),
				thing = 1;
			);
			
			
			if(global_currentedge:0 != point,
				global_currentedge += point,
				print(format('br Point already used in current edge.'));
				return();
			),
			
			facelength == 2 && (global_currentface:0:0 != point && global_currentface:0:1 != point),
			
			print('Beginning new face');
			point1 = global_currentpoints:0;
			point2 = global_currentpoints:1;
			//complete current face
			__addedge(point1);
			//start new face
			__addedge(point1);
			__addedge(point2);
			//clear temporary variables
			point1 = null;
			point2 = null;		
			//add current point
			__addedge(point);
			return();
		);
	);
	edgelength = length(global_currentedge);
	if(edgelength == 2,
		flagforward = false;
		flagbackward = false;
		for(global_edges,
			if(_ == global_currentedge,
				flagforward = true,
				l(_:1,_:0) == global_currentedge,
				flagbackward = true;
			);
			if(flagforward && flagbackward,
				print(format('br Edge already used. Please use different points.'));
				global_currentedge = l();
				return();
			);
		);
		for(global_currentface,
			if(_ == global_currentedge || l(_:1,_:0) == global_currentedge,
				print(format('br Edge already used in current face.'));
				return();
			);
		);
		startpoint = global_currentedge:1;
		if(flagforward,
			print('Reordered edge');
			global_currentedge = l(global_currentedge:1,global_currentedge:0),
			global_currentedge = global_currentedge;
		);
		if(length(global_currentface) != 2,
			draw_shape('line',40,'color',0x000000FF,'line',7,'from',global_currentedge:0 + 0.5,'to',global_currentedge:1 + 0.5);
		);
		__addface(global_currentedge);
		__showcurrentface();
		global_edges += global_currentedge;
		//checks to see what you can do
		if(length(global_edges) > 0 && global_suggestion_bools:0,
			print(format('w You can now run ','c /volume perimeter.','^w Calculates perimeter of the open polyhedron you outlined.','!/volume perimeter'));
			global_suggestion_bools:0 = false;
		);
		if(!__checkedges(),
			print(format('w You can now run ','c /volume volume.','^w Calculates volume of the closed polyhedron you outlined.','!/volume volume'));
		);
		
		global_currentedge = l();
		facelength = length(global_currentface);;
		if(facelength == 1,
			global_currentedge += startpoint,
			facelength == 2,
			global_currentedge += startpoint;
			print(format('y Please return to the starting point ','w ' + str(global_currentface:0:0)));
			print(format('y   or move to a new point.'));
		);
	);
);

__addface(edge) ->
(
	breakflag = false;
	facelength = length(global_currentface);
	if(facelength != 1,
		if(facelength == 0,
			global_currentface += edge,
			facelength == 2,
			for(global_faces,
				if(sort(__facepoints(global_currentface)) == sort(__facepoints(_)),
					print(format('br Face is already in use.'));
					return();
				);
			);
			if(global_currentface:1:1 == edge:0,
				global_currentface += edge,
				global_currentface += l(edge:1,edge:0);
			);
		),
		loop(2,
			if(global_currentface:0:1 == edge:0,
				global_currentface += edge;
				break(),
				global_currentface:0:1 == edge:1,
				global_currentface += l(edge:1,edge:0);
				break(),
				global_currentface = l(l(global_currentface:0:1,global_currentface:0:0));
			);
		);
	);
	facelength = length(global_currentface);
	if(facelength == 3,
		draw_shape('line',0,'color',0xFF0000FF,'line',5,'from',__facepoints(global_currentface):0 + 0.5,'to',__facepoints(global_currentface):2 + 0.5);
		if(length(global_faces) > 0,
			reorderflag = __reorderface(),
			reorderflag = false;
		);
		global_reorder += reorderflag;
		global_faces += global_currentface;
		print(format('l Face added'));
		
		//checks to see what you can do
		if(length(global_faces) > 0 && global_suggestion_bools:1,
			print(format('w You can now run ','c /volume area.','^w Calculates surface area of the polyhedron you outlined.','!/volume area'));
			global_suggestion_bools:1 = false;
		);
		
		print(' ---------- ');
		__showplaneface(global_currentface);
		global_currentface = l();
	);
);

//reorders a face to make it match the edges.
//  this matters for volume calculation.
//  if necessary changes the matrix of points 0,1,2
//      [ 0 1 ]      [ 0 2 ]
//      [ 1 2 ]  to  [ 2 1 ]
//      [ 2 0 ]      [ 1 0 ]
//  this preserves the edges and the order
//  of the points within the face but reorders
//  the initial points and edges to
//  allow for correct volume calculation
__reorderface() ->
(
	reorderflag = false;
	test = __facepoints(global_currentface);
	for(global_faces,
		test1 = __facepoints(_);
		for(test1,
			tester = _;
			testnum = _i;
			for(test,
				if(_ == tester && (test1:(testnum-1) == test:(_i-1) || test1:(testnum-2) == test:(_i-2)),
					global_currentface = l(
						l(global_currentface:2:1,global_currentface:2:0),
						l(global_currentface:1:1,global_currentface:1:0),
						l(global_currentface:0:1,global_currentface:0:0)
					);
					print('Reordered face');
					reorderflag = true;
					break();
				);
			);
			if(reorderflag,
				break();
			);
		);
		if(reorderflag,
			break();
		);
	);
	
	return(reorderflag);
);

//just returns the vertices that make up a face
__facepoints(face) ->
(
	points = l();
	for(face,
		for(_,
			point = _;
			pointflag = false;
			for(points,
				if(_ == point,
					pointflag = true;
				);
			);
			if(!pointflag,
				points += point;
			);
		);
	);
	return(points);
);

//returns all points that are present in a face
__faceblocks(face) ->
(
	p1 = face:0:0;
	p2 = face:0:1;
	p3 = face:1:1;
	l(extra,axis,value) = __checkpoints(l(p1,p2,p3));
	if(axis == null,
		return(),
		global_axis == null,
		global_axis = axis,
		global_axis != axis && global_axis != null,
		print(format('br Not all faces are in the same plane.'));
		return();
	);
	if(extra > 0,
		print(format('br At least one point is not in the same plane as the others.'));
		return();
	);
	if(axis != 0,
		xaxis = 0,
		xaxis = 1;
	);
	yaxis = 3 - (axis + xaxis);
	l(p1,p2,p3) = sort_key(l(p1,p2,p3),_:xaxis);
	points = l(p1,p2,p3);
	centroid = (p1 + p2 + p3)/3;
	eqn12 = __linreg(l(p1:xaxis,p1:yaxis),l(p2:xaxis,p2:yaxis));
	eqn23 = __linreg(l(p2:xaxis,p2:yaxis),l(p3:xaxis,p3:yaxis));
	eqn31 = __linreg(l(p1:xaxis,p1:yaxis),l(p3:xaxis,p3:yaxis));
	eqnlist = l(eqn12,eqn23,eqn31);
	checksigns = l();
	for(eqnlist,
		checksigns:_i = __sign(centroid:yaxis - __linvalue(centroid:xaxis,_));
	);
	
	pointlist = m();
	checked = 0;
	
	c_for(x = min(p1:xaxis,p2:xaxis,p3:xaxis), x <= max(p1:xaxis,p2:xaxis,p3:xaxis), x += 1,
		if(checksigns:2 == 1,
			miny = ceil(__linvalue(x,eqnlist:2)),
			checksigns:2 == -1,
			maxy = floor(__linvalue(x,eqnlist:2))
		);
		if(x <= p2:xaxis,
			if(checksigns:0 == 1,
				miny = ceil(__linvalue(x,eqnlist:0)),
				checksigns:0 == -1,
				maxy = floor(__linvalue(x,eqnlist:0))
			),
			x > p2:xaxis,
			if(checksigns:1 == 1,
				miny = ceil(__linvalue(x,eqnlist:1)),
				checksigns:1 == -1,
				maxy = floor(__linvalue(x,eqnlist:1))
			)
		);
		if(x == p1:xaxis && x == p2:xaxis,
			miny = min(p1:yaxis,p2:yaxis);
			maxy = max(p1:yaxis,p2:yaxis)
		);
		c_for(y = miny, y <= maxy, y += 1,
			checked += 1;
			point = l();
			point:axis = value;
			point:xaxis = x;
			point:yaxis = y;
			pointlist += point;
		);
	);
	__showfaces();
	return(pointlist);
);

//constructs a linear equation through two 2d points
__linreg(p1,p2) ->
(
	m = (p2:1-p1:1)/(p2:0-p1:0);
	b = p1:1 - p1:0*m;
	return(l(m,b));
);

//takes an x-value and a line and returns a y-value
__linvalue(x,line) ->
(
	return(x * line:0 + line:1);
);

//gets every face block from global_faces
__allfaceblocks() ->
(
	if(length(global_faces) == 0,
		print(format('br Please select at least one face first.'));
		return();
	);
	blocks = m();
	for(global_faces,
		points = __faceblocks(_);
		if(points == null,
			return();
		);
		for(keys(points),
			if(!has(blocks,_),
				blocks += _;
			);
		);
	);
	return(blocks);
);

//places a red sphere along a vector
//  loop this by increasing the numerator
//  up to the denominator
__moveball(point,vector,num,denom) ->
(
	draw_shape('sphere',10,'color',0xFF0000FF,'fill',0xFF0000FF,'center',point + vector*num/denom,'radius',0.05);
);

//calls one of the show functions depending on the argument
show(type) ->
(
	if(type == 'edges',
		__showedges(),
		type == 'faces',
		__showfaces();
	);
);

//draws red lines to indicate edges, allowing
//	you to see which ones are unpaired
__showedges() -> 
(
	shapelist = l();
	divisions = 2;
	for(global_edges,
		e = _;
		for(range(divisions),
			shapelist += l('line',100,'line',5,'color',0xFF0000FF,'from',(e:0 + _*__vector(e:0,e:1)/divisions) + 0.5,'to',(e:0 + (_+0.5)*__vector(e:0,e:1)/divisions) + 0.5);
		);
	);
	draw_shape(shapelist);
);

//draws black lines along the face to
//  help in visualization and shows the
//  direction to the original point
__showcurrentface() ->
(
	face = global_currentface;
	if(length(face) > 1,
		draw_shape('line',100,'color',0xFF0000FF,'line',5,'from',__facepoints(face):0 + 0.5,'to',__facepoints(face):2 + 0.5);
		lastVec = __facepoints(face):0 - __facepoints(face):2;
		for(l(range(20)) + 1, 
			if(_ % 2 ==  0,
				schedule(_,'__moveball',face:1:1 + 0.5,lastVec,_/2,10);
			);
		);
	);
	
	for(face,
		draw_shape('line',40,'color',0x000000FF,'line',5,'from',face:_i:0 + 0.5,'to',face:_i:1 + 0.5);
	);
);

//calls showplaneface for every added face
__showfaces() ->
(
	for(global_faces,
		__showplaneface(_);
	);
	return();
);

//draws blue lines across the face to
//  aid in visualization
__showplaneface(face) ->
(
	if(length(face) == 3,
		point1 = face:0:0;
		point2 = face:1:0;
		point3 = face:2:0;
		vector1 = __vector(point2,point3);
		vector2 = __vector(point3,point1);
		vector3 = __vector(point1,point2);
		max_length = 10 * max(__magnitude(vector1),__magnitude(vector2),__magnitude(vector3));
		shapelist = l();
		c_for(x = 1, x < max_length, x += 1,
			shapelist += 
				l(
					'line',100,
					'color',0x0000FF2A,
					'line',5,
					'from',l(0.5,0.5,0.5) + point1 + x * vector3 / max_length,
					'to',l(0.5,0.5,0.5) + point3 - x * vector1 / max_length
				);
		);
		draw_shape(shapelist);
	);
	for(face,
		draw_shape('line',100,'color',0x000000FF,'line',5,'from',face:_i:0 + 0.5,'to',face:_i:1 + 0.5);
	);
	return();
);

//does what it says on the tin.
//  prints all relevant data
printall() ->
(
	print(global_points);
	print(global_edges);
	print(global_faces);
	print(global_currentedge);
	print(global_currentface);
	return();
);

//just prints the edges
printedges() ->
(
	print('-edges');
	for(global_edges,
		print(_);
	);
	return();
);

//just prints the faces, but is formatted a little
printfaces() ->
(
	for(global_faces,
		print('-face edges');
		for(_,
			print(_);
		);
	);
	return();
);

__on_player_clicks_block(player,block,face) ->
(
	if(query(player(),'holds','mainhand'):0 == 'golden_sword',
		__addpoint(pos(block));
	);
);

__on_statistic(player,category,event,value) ->
(
	for(global_statsList,
		if(event == _,
			schedule(0,'__showcurrentface');
			break();
		);
	);
);
