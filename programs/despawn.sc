__config() ->
(
	global_colors = m(
		l('red',0xFF0000FF),
		l('green',0x00FF00FF),
		l('blue',0x0000FFFF),
		l('black',0x000000FF),
		l('gray',0x808080FF),
		l('grey',0x808080FF),
		l('white',0xFFFFFFFF)
	);
	return(
		m(
			l('scope','player'),
			l('stay_loaded',true),
			
			l('commands',
				m(
					l('',['__drawsphere',60,'red']),
					l('<time>',['__drawsphere','red']),
					l('<time> <color>','__drawsphere')
				)
			),
			
			l('arguments',
				m(
					l('time',
						m(
							l('type','int'),
							l('min',1),
							l('max',3600),
							l('suggest',
								l(60,120,300,600)
							)
						)
					),
					l('color',
						m(
							l('type','term'),
							l('options',
								l('red','green','blue','black','gray','grey','white')
							),
							l('suggest',
								l('red','green','blue','black','gray','grey','white')
							)
						)
					)
				)
			)
		)
	);
);

__drawsphere(time,color) ->
(
	ticks = time * 20;
	hexcolor = global_colors:color;
	draw_shape('sphere',ticks,'center',pos(player()),'radius',24,'color',hexcolor);
	draw_shape('sphere',ticks,'center',pos(player()),'radius',128,'color',hexcolor);
);
