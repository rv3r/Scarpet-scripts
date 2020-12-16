__config() ->
(
	//stats to refresh showing of current face
	global_statsList = l(
		'walk_one_cm',
		'sprint_one_cm',
		'fly_one_cm',
		'crouch_one_cm',
		'jump','aviate_one_cm',
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
		'__cross3',
		'__plane',
		'__inPlane',
		'__heron'
	);
	
	return(
		m(
			l('scope','player'),
			l('stay_loaded',true)
		)
	);
);

__command() ->
(
	return();
);

//not actually used but mightve been in another universe

__choose(n,k) ->
(
	return(fact(n)/(fact(k)*fact(n-k)));
);

//returns the number of unpaired edges

checkedges() ->
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

perimeter() ->
(
	if(length(global_edges) == 0,
		print('Please select at least one edge first.');
		return();
	);
	alledges = copy(global_edges);
	enum = 0;
	while(length(alledges) > 0,length(alledges),
		check = alledges:enum;
		flag = true;
		for(alledges,
			if(_i > enum && alledges:enum == l(_:1,_:0),
				delete(alledges,_i);+
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
		print('This does not work on closed polyhedra.');
		return();
	);
	showfaces();
	perimeter = reduce(alledges,_a + __magnitude(__vector(_:0,_:1)),0);
	print(format('c Perimeter: ','w ' + str(perimeter)));
	return();
	//return(perimeter);
);

//applies Heron's semiperimeter formula to find the area of every current face

area() ->
(
	if(length(global_faces) == 0,
		print('Please select at least one face first.');
		return();
	);
	showfaces();
	area = reduce(global_faces,_a +	__heron(__magnitude(__vector(_:0:0,_:0:1)),__magnitude(__vector(_:1:0,_:1:1)),__magnitude(__vector(_:2:0,_:2:1))),0);
	print(format('c Area: ','w ' + str(area)));
	return();
	//return(area);
);

//applies tetrahedral shoelace method once edges are paired and faces are ordered properly

vol() ->
(
	showfaces();
	volume = 0;
	if(!checkedges() && length(global_edges),
		volume = abs(reduce(global_faces,
			//print(det(l(_:0:0,_:1:0,_:2:0)));
			_a + __det(l(_:0:0,_:1:0,_:2:0)),
			0
		)/6);
		//print('----- calculating volume -----');
		print(format('c Volume: ','w ' + str(volume)));
		check = sanitycheck();
		if(volume > check,
			print(check);
			print('failed sanity check. please send error');
			print('  log to help find bugs.');
			logdata();
		);
		return();
		//return(sum);
	);
);

//ensures volume isnt greater than what is physically possible

sanitycheck() ->
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
	draw_shape('box',100,'from',l(minX,minY,minZ),'to',l(maxX,maxY,maxZ));
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


//not used
//BUT
//accurately and kinda efficiently finds
//the number of lattice points contained
//in a tetrahedron described by the
//four input triples

__basiclattice(point1,point2,point3,point4) ->
(
	ticks = 1000;
	plane1 = __plane(point2,point3,point4);
	print('plane 1');
	print(plane1);
	print(point2);
	plane2 = __plane(point3,point4,point1);
	print('plane 2');
	print(plane2);
	print(point3);
	plane3 = __plane(point4,point1,point2);
	print('plane 3');
	print(plane3);
	print(point4);
	plane4 = __plane(point1,point2,point3);
	print('plane 4');
	print(plane4);
	print(point1);
	xlist = l(floor(point1:0),floor(point2:0),floor(point3:0),floor(point4:0));
	ylist = l(floor(point1:1),floor(point2:1),floor(point3:1),floor(point4:1));
	zlist = l(floor(point1:2),floor(point2:2),floor(point3:2),floor(point4:2));
	l(minX,maxX,minY,maxY,minZ,maxZ) = l(min(xlist),max(xlist+1),min(ylist),max(ylist+1),min(zlist),max(zlist+1));
	latticepoints = 0;
	blocksChecked = 0;
	pointlist = l();
	print('limits');
	print(l(minX,maxX,minY,maxY,minZ,maxZ));
	print('debug info');
	c_for(x = minX, x < maxX, x+=1,
		c_for(y = minY, y < maxY, y+=1,
			z1 = (-1*((plane1:0)*( x - point2:0)+(plane1:1)*( y - point2:1)))/(plane1:2) + (point2:2);
			z2 = (-1*((plane2:0)*( x - point3:0)+(plane2:1)*( y - point3:1)))/(plane2:2) + (point3:2);
			z3 = (-1*((plane3:0)*( x - point4:0)+(plane3:1)*( y - point4:1)))/(plane3:2) + (point4:2);
			z4 = (-1*((plane4:0)*( x - point1:0)+(plane4:1)*( y - point1:1)))/(plane4:2) + (point1:2);
			zlistnum = l(z1,z2,z3,z4);
			print(zlistnum);
			cleanz = l();
			for(zlistnum,
				if(_ > minZ && _ < maxZ,
					cleanz += _;
				);
			);
			if(!cleanz,
				cleanz = l(null);
			);
			
			newminZ = max(minZ,floor(min(cleanz)));
			newmaxZ = min(maxZ,ceil(max(cleanz)));
			
			c_for(z = newminZ, z < newmaxZ, z+=1,
			//c_for(z = minZ, z < maxZ, z+=1, deprecated
				blocksChecked += 1;
				testpoint = l(x,y,z);
				pointlist += l('sphere',ticks,'color',0xFF00FFFF,'fill',0xFF0000FF,'center',testpoint,'radius',0.1);
				result1 = __sum(plane1*(testpoint-point2));
				result2 = __sum(plane2*(testpoint-point3));
				result3 = __sum(plane3*(testpoint-point4));
				result4 = __sum(plane4*(testpoint-point1));
				sign1 = __sign(result1);
				sign2 = __sign(result2);
				sign3 = __sign(result3);
				sign4 = __sign(result4);
				signs = '';
				for(l(sign1,sign2,sign3,sign4),
					if(
						_ == -1,
							signs += '-',
						_ == 0,
							signs += '0',
						_ == 1,
							signs += '+'
					);
				);
				if(signs == '-+-+' || signs == '+-+-',
					//print(str(testpoint) + ' : ' + str(signs) + ' : ' + str(l(result1,result2,result3,result4)));
					pointlist += l('sphere',ticks,'color',0xFF0000FF,'fill',0xFF0000FF,'center',testpoint,'radius',0.1);
					latticepoints += 1//,
					//print(signs);
				);
			);
		);
	);
	
	draw_shape(pointlist);
	draw_shape('line',ticks,'color',0x000000FF,'from',point1,'to',point2);
	draw_shape('line',ticks,'color',0x000000FF,'from',point1,'to',point3);
	draw_shape('line',ticks,'color',0x000000FF,'from',point1,'to',point4);
	draw_shape('line',ticks,'color',0x000000FF,'from',point2,'to',point3);
	draw_shape('line',ticks,'color',0x000000FF,'from',point2,'to',point4);
	draw_shape('line',ticks,'color',0x000000FF,'from',point3,'to',point4);
	
	draw_shape('sphere',ticks,'color',0xFF00FFFF,'fill',0xFF00FFFF,'center',point1,'radius',0.1);
	draw_shape('sphere',ticks,'color',0xFF00FFFF,'fill',0xFF00FFFF,'center',point2,'radius',0.1);
	draw_shape('sphere',ticks,'color',0xFF00FFFF,'fill',0xFF00FFFF,'center',point3,'radius',0.1);
	draw_shape('sphere',ticks,'color',0xFF00FFFF,'fill',0xFF00FFFF,'center',point4,'radius',0.1);
	draw_shape('box',ticks,'color',0x000000FF,'fill',0x11111122,'from',l(minX,minY,minZ),'to',l(maxX,maxY,maxZ));
	return(l(latticepoints,blocksChecked));
);

//functions to clear current values

clearpoints() ->
(
	global_points = l();
	return();
);

clearedges() ->
(
	global_edges = l();
	return();
);

clearfaces() ->
(
	global_faces = l();
	return();
);

clearall() ->
(
	clearpoints();
	clearedges();
	clearfaces();
	global_currentedge = l();
	global_currentface = l();
	global_currentpoints = l();
	global_suggestion_bools = l(true,true);
	return();
);

//heart and soul of the program
//
//addpoint just, well, adds the point to a list.
//  more importantly, it calls addedge with the point
//addedge attempts to add the point to the current edge,
//  reorders the edge if necessary, calls addface,
//  checks to see if you can call vol, and can point you to your original point
//addface attempts to add the point to the current face,
//  reorders the face if necessary, and shows the face

__addpoint(triple) ->
(
	//print('----- adding point -----');
	flag = true;
	for(global_points,
		if(_ == triple,
			flag = false;
		);
	);
	if(flag,
		global_points += triple;
		//print(str(triple) + ' added to points');
		
		//print('Point added');
	);
	__addedge(triple);
);

__addedge(point) ->
(
	//print('----- adding edge -----');
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
				
				//print('Second face point added');
				
				global_currentpoints:1 = point;
				//print(global_currentpoints);
			);
			
			//runs if player does continue to another point
			if(facelength == 2 && (global_currentface:0:0 == point || global_currentface:0:1 == point),
				thing = 1;
				//print(format('biuc gottem'));
			);
			
			
			if(global_currentedge:0 != point,
				global_currentedge += point,
				print(format('br Point already used in current edge.'));
				return();
			),
			
			facelength == 2 && (global_currentface:0:0 != point && global_currentface:0:1 != point),
			
			print('Beginning new face');
			//print(global_currentpoints);
			//for(global_currentpoints,
				//draw_shape('box',1000,'from',_+0.5-0.1,'to',_+0.5+0.1,'color',0x000000FF);
			//);
			//global_currentpoints = l();
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
	//print('current edge: ' + global_currentedge);
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
		//print(str(global_currentedge) + ' edge added');
		
		//print('Edge added');
		
		//checks to see what you can do
		if(length(global_edges) > 0 && global_suggestion_bools:0,
			print(format('w You can now run ','c /volume perimeter.','^w Calculates perimeter of the open polyhedron you outlined.','?/volume perimeter'));
			global_suggestion_bools:0 = false;
		);
		if(!checkedges(),
			print(format('w You can now run ','c /volume vol.','^w Calculates volume of the closed polyhedron you outlined.','?/volume vol'));
		);
		
		global_currentedge = l();
		facelength = length(global_currentface);
		//print('---');
		//print(global_currentface);
		//print(facelength);
		if(facelength == 1,
			global_currentedge += startpoint,
			
			//print('Please select the final point'),
			
			facelength == 2,
			global_currentedge += startpoint;
			//print(global_currentedge);
			//print(global_currentface);
			print(format('y Please return to the starting point or'));
			print(format('y   move to a new point.'));
			//print('---');
		);
	);
);

__addface(edge) ->
(
	//print('----- adding face -----');
	breakflag = false;
	facelength = length(global_currentface);
	//print(facelength);
	if(facelength != 1,
		if(facelength == 0,
			global_currentface += edge,
			facelength == 2,
			//print('-----');
			for(global_faces,
				if(sort(__facepoints(global_currentface)) == sort(__facepoints(_)),
					print(format('br Face is already in use.'));
					return();
				);
			);
			//print(global_currentface);
			if(global_currentface:1:1 == edge:0,
				//print(global_currentface:1:1);
				//print(edge:0);
				global_currentface += edge,
				global_currentface += l(edge:1,edge:0);
				//print(global_currentface:1:1);
				//print(edge:0);
			);
		),
		//print('-----');
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
	//print('current face ' + str(global_currentface));
	facelength = length(global_currentface);
	if(facelength == 3,
		draw_shape('line',0,'color',0xFF0000FF,'line',5,'from',__facepoints(global_currentface):0 + 0.5,'to',__facepoints(global_currentface):2 + 0.5);
		if(length(global_faces) > 0,
			reorderflag = __reorderface(),
			reorderflag = false;
		);
		global_reorder += reorderflag;
		global_faces += global_currentface;
		//print(str(global_currentface) + ' face added');
		print(format('bl Face added'));
		
		//checks to see what you can do
		if(length(global_faces) > 0 && global_suggestion_bools:1,
			print(format('w You can now run ','c /volume area.','^w Calculates surface area of the polyhedron you outlined.','?/volume area'));
			global_suggestion_bools:1 = false;
		);
		
		print(' ---------- ');
		__showplaneface(global_currentface);
		//showplanecenter(global_currentface:0:0 + 0.5,global_currentface:1:0 + 0.5,global_currentface:2:0 + 0.5);
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
					//print(test);
					//print(test1);
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

//just returns the points that make up a face

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

//places a red sphere along a vector
//  loop this by increasing the numerator
//  up to the denominator

__moveball(point,vector,num,denom) ->
(
	draw_shape('sphere',10,'color',0xFF0000FF,'fill',0xFF0000FF,'center',point + num*vector/denom,'radius',0.1);
);

showedges() -> 
(
	shapelist = l();
	for(global_edges,
		shapelist += l('line',100,'line',5,'color',0xFF0000FF,'from',_:0 + 0.5,'to',((_:0 + _:1) / 2) + 0.5);
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

showfaces() ->
(
	for(global_faces,
		//showplanecenter(_:0:0+l(0.5,0.5,0.5),_:1:0+l(0.5,0.5,0.5),_:2:0+l(0.5,0.5,0.5));
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
		//print(l(point1,point2,point3));
		vector1 = __vector(point2,point3);
		vector2 = __vector(point3,point1);
		vector3 = __vector(point1,point2); 
		max_length = 2 * max(__magnitude(vector1),__magnitude(vector2),__magnitude(vector3));
		shapelist = l();
		c_for(x = 1, x < max_length, x += 1,
			shapelist += 
				l(
					'line',100,
					'color',0x0000FFFF,
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

//deprecated method
//draws plane normal vector in both directions and
//  scaled spheres to show the surface

__showplanecenter(point1,point2,point3) ->
(
	planeVec = __plane(point1,point2,point3);
	maxDir = max(l(abs(planeVec:0),abs(planeVec:1),abs(planeVec:2)));
	indices = l(0,1,2);
	for(planeVec,
		if(abs(_) == maxDir,
			maxIndex = _i;
			break();
		);
	);
	centerVec = l(1,1,1);
	centerVec:maxIndex = 0;
	
	minDir = 0.3 * sqrt(min(l(__magnitude(vector(point1,point2)),__magnitude(vector(point2,point3)),__magnitude(vector(point3,point1)))));
	radius = 0.2;
	
	centroid = (point1 + point2 + point3)/3;
	
	sphereList = l();
	offsetList = 1 * minDir * l(-1,0,1);
	for(offsetList,
		dir1 = _;
		for(offsetList,
			dir2 = _;
			sum = 0;
			flag1 = true;
			centerPoint = centerVec * planeVec;
			for(centerPoint,
				if(_i != maxIndex,
					if(flag1,
						centerPoint:_i = centroid:_i + dir1;
						sum += dir1 * planeVec:_i;
						flag1 = false,
						centerPoint:_i = centroid:_i + dir2;
						sum += dir2 * planeVec:_i;
					);
				);
			);
			centerPoint:maxIndex = sum/(-1*planeVec:maxIndex) + centroid:maxIndex;
			sphereList += l('sphere',60,'color',0x000000FF,'fill',0x00FF00FF,'center',centerPoint,'radius',radius);
		);
	);
	draw_shape(sphereList);	
	
	draw_shape(
		l(
			l('line',60,'color',0x000000FF,'line',5,'from',centroid - planeVec/magnitude(planeVec),'to',centroid + planeVec/magnitude(planeVec)),
			l('line',60,'color',0x000000FF,'line',5,'from',point1,'to',point2),
			l('line',60,'color',0x000000FF,'line',5,'from',point2,'to',point3),
			l('line',60,'color',0x000000FF,'line',5,'from',point3,'to',point1)
		)
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
			//print(length(global_edges));
			schedule(0,'__showcurrentface');
			break();
		);
	);
);

//__on_player_starts_sneaking(player) ->
//(
	//schedule(0,'vol');
//);

//__on_player_swaps_hands(player) ->
//(
	//schedule(0,'showfaces');
//);

//__on_player_drops_item(player) ->
//(
	//schedule(0,'clearall');
//);
