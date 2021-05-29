__config() ->
(
	return(
		m(
			l('scope','player'),
			l('stay_loaded',true),
			
			l('commands',
				m(
					l('<center> <xradius> <yradius> <zradius> <radial> <longitudinal> <time>','__ellipsoid') 
				)
			),
			
			l('arguments',
				m(
					l('center',
						m(
							l('type','location')
						)
					),
					l('xradius',
						m(
							l('type','float'),
							l('min',0.1),
							l('suggest',l(1,16,128))
						)
					),
					l('yradius',
						m(
							l('type','float'),
							l('min',0.1),
							l('suggest',l(1,16,128))
						)
					),
					l('zradius',
						m(
							l('type','float'),
							l('min',0.1),
							l('suggest',l(1,16,128))
						)
					),
					l('radial',
						m(
							l('type','int'),
							l('min',1),
							l('max',6),
							l('suggest',l(1,4,6))
						)
					),
					l('longitudinal',
						m(
							l('type','int'),
							l('min',1),
							l('max',6),
							l('suggest',l(1,4,6))
						)
					),
					l('ticks',
						m(
							l('type','time'),
							l('min',1)
						)
					)
				)
			)
		)
	)
);

//center is the center of the ellipsoid
//xradius is the minor radius in the x direction
//yradius is the minor radius in the y direction
//zradius is the minor radius in the z direction
//radial is the radial density of lines about the longest axis
//longitudinal is the longitudinal density of lines in the direction of the longest axis
//ticks is the time the ellipsoid exists for, in ticks

__ellipsoid(center,xradius,yradius,zradius,radial,longitudinal,ticks) ->
(
	global_color = 0x0000FFAA;
	global_shapelist = l();
	global_ticks = ticks;
	
	//print(global_ticks);
	
	global_axes = l(xradius,yradius,zradius);
	for(global_axes,
		if(_ == max(global_axes),
			global_axis = _i;
			break();
		);
	);
	//print('axis : ' + global_axis);
	
	global_center = center + 0.5;
	point1 = copy(global_center);
	point1:global_axis = point1:global_axis + global_axes:global_axis;
	point2 = copy(global_center);
	point2:global_axis = point2:global_axis - global_axes:global_axis;
	
	//start by finding critical distances along longest dimension
	
	global_l = l();
	temp = l(point1,point2);
	loop(longitudinal,
		for(temp,
			if(_i > 0,
				temp += __bisect(temp:(_i - 1),_,'l');
			);
		);
		temp = sort_key(temp,_:global_axis);
	);
	global_l = sort(global_l);
	
	//print(global_l);
	
	//then find critical angles
	
	point3 = copy(global_center);
	point3:((global_axis + 1) % 3) = point1:(global_axis + 1) + global_axes:(global_axis + 1);
	point4 = copy(global_center);
	point4:((global_axis + 1) % 3) = point1:(global_axis + 1) - global_axes:(global_axis + 1);
	
	global_r = l(90);
	temp = l(point3,point4);
	loop(radial,
		for(temp,
			if(_i > 0,
				temp += __bisect(temp:(_i - 1),_,'r');
			);
		);
		temp = sort_key(temp,_:(global_axis + 1));
	);
	//for(temp,
		//global_shapelist += l('sphere',global_ticks,'radius',0.1,'center',_,'color',global_color);
	//);
	
	for(global_r,
		global_r += _ + 180;
	);
	global_r = sort(global_r);
	global_r = global_r + 90;
	
	//print(global_r);
	
	previous = l(point2);
	for(global_l,
		list = __ring(_);
		__connect(previous,list);
		for(list,
			global_shapelist += l('line',global_ticks,'from',_,'to',list:(_i - 1),'color',global_color);
		);
		previous = copy(list);
	);
	__connect(list,l(point1));
	
	draw_shape(global_shapelist);
);

__bisect(point1,point2,mode) ->
(
	//print('-----------------------');
	//print('point 1 : ' + point1);
	//print('point 2 : ' + point2);
	
	if(mode == 'l',
		offset = 0,
		mode == 'r',
		offset = 1);
	
	x1 = point1:(global_axis + offset);
	y1 = point1:(global_axis + 1 + offset);
	x2 = point2:(global_axis + offset);
	y2 = point2:(global_axis + 1 + offset);
	x3 = (x1 + x2) / 2;
	y3 = (y1 + y2) / 2;
	m = (y1 - y2) / (x1 - x2);
	
	//print('center : ' + global_center);
	//print('midpoint : ' + l(x3,y3));
	
	h = global_center:(global_axis + offset);
	k = global_center:(global_axis + 1 + offset);
	
	ellipse_a = global_axes:(global_axis + offset);
	ellipse_b = global_axes:(global_axis + 1 + offset);
	
	//print('ellipse_a : ' + ellipse_a);
	//print('ellipse_b : ' + ellipse_b);
	
	//print('m : ' + m);
	
	if(m == 0,
		x4 = x3,
		
		x1 - x2 == 0,
		y4 = y3;
		x4 = ellipse_a * sqrt(1 - (y4 - k)^2 / ellipse_b^2) + h,
	
		a = ellipse_b^2 + ellipse_a^2/m^2;
		b = 2 * (ellipse_a^2 * k / m - ellipse_a^2 * x3 / m^2 - ellipse_a^2 * y3 / m - ellipse_b^2 * h);
		c = ellipse_a^2 * k^2 + ellipse_b^2 * h^2 - ellipse_a^2 * ellipse_b^2 + ellipse_a^2 * y3^2 + ellipse_a^2 * x3^2 / m^2 + 2 * ellipse_a^2 * x3 * y3 / m - 2 * ellipse_a^2 * k * y3 - 2 * ellipse_a^2 * k * x3 / m;
	
		//print('a : ' + round(a));
		//print('b : ' + round(b));
		//print('c : ' + round(c));
	
		x4 = (-b + sqrt(b^2 - 4 * a * c)) / (2 * a);
	);
	
	if(y4 != null,
		y4 = ellipse_b * sqrt(1 - (x4 - h)^2 / ellipse_a^2) + k;
	);
	
	//print('x4 : ' + x4);
	//print('y4 : ' + y4);
	
	d1 = sqrt(reduce(l(x1,y1) - l(x4,y4),_a += _^2,0));
	d2 = sqrt(reduce(l(x2,y2) - l(x4,y4),_a += _^2,0));
	
	if((abs(d1-d2)/d1 > 0.01 || abs(d1-d2)/d2 > 0.01) || (d1 > sqrt(ellipse_a^2 + ellipse_b^2) || d2 > sqrt(ellipse_a^2 + ellipse_b^2)),
		x4 = (-b - sqrt(b^2 - 4 * a * c)) / (2 * a);
		y4 = ellipse_b * sqrt(1 - (x4 - h)^2 / ellipse_a^2) + k;
		d1 = sqrt(reduce(l(x1,y1) - l(x4,y4),_a += _^2,0));
		d2 = sqrt(reduce(l(x2,y2) - l(x4,y4),_a += _^2,0));
	);
	
	output = l(0,0,0);
	output:((global_axis + offset) % 3) = x4;
	output:((global_axis + 1 + offset) % 3) = y4;
	output:((global_axis + 2 + offset) % 3) = point1:(global_axis + 2 + offset);
	//print(output);
	
	if(mode == 'l',
		global_l += x4 - global_center:global_axis,
		mode == 'r',
		angle = atan2((x4 - global_center:(global_axis + offset),y4 - global_center:(global_axis + 1 + offset)));
		//global_shapelist += l('label',global_ticks,'text',angle,'pos',output+l(0,0.5,0),'color',global_color);
		global_r += angle;
	);
	
	return(output);
);

__ring(l) ->
(
	output = l();
	for(global_r,
		r = sqrt((1 - l^2 / (global_axes:global_axis)^2) / ((cos(_)^2 / (global_axes:(global_axis + 1))^2) + (sin(_)^2 / (global_axes:(global_axis + 2))^2)));
		x = r * cos(_);
		y = r * sin(_);
		point = l();
		point:global_axis = global_center:global_axis + l;
		point:((global_axis + 1) % 3) = global_center:(global_axis + 1) + x;
		point:((global_axis + 2) % 3) = global_center:(global_axis + 2) + y;
		output += point;
		//global_shapelist += l('label',global_ticks,'text',_,'pos',point,'color',global_color);
	);
	return(output);
);

__connect(list1,list2) ->
(
	loop(max(length(list1),length(list2)),
		global_shapelist += l('line',global_ticks,'from',list1:_,'to',list2:_,'color',global_color);
	);
);
