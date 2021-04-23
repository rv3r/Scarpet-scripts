//make this boolean true in case you also want a map of
//blocks that failed your test
global_fail_bool = false;

your_test_function_here(block) ->
(
	//this function should return a false result if it failed
	//the check and a true result if it worked
	
	//just open the game and run /blocktest
	//this script will provide a map with every block that worked
	//then in your script you just need to use has()
	//to see if the block is valid for your particular use case
);

__command() ->
(
	delete_file('blocktest','text');
	write_file('blocktest','text','{');
	delete_file('blocktest_fail','text');
	write_file('blocktest_fail','text','{');
	for(block_list(),
		test = null;
		test = your_test_function_here(_);
		if(test,
			result = l(str(_),1);
			write_file('blocktest','text',str('\'' + _ + '\'->1,')),
			if(global_fail_bool,
				result = l(str(_),1);
				write_file('blocktest_fail','text',str('\'' + _ + '\'->1,'))
			);
		);
	);
	write_file('blocktest','text','}');
	write_file('blocktest_fail','text','}');
);
