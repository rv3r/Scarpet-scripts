# Scarpet Scripts
Pick the script you need and follow [these](https://github.com/gnembon/fabric-carpet/wiki/Installing-carpet-scripts-in-your-world) instructions for installation!

# Beehive Count Script
Right click a bee nest or beehive with an empty mainhand to learn how many bees are in the block.

# Block Test Script
Uses RamsaKd's array of every Minecraft block
Create your own test in `your_test_function_here()` and let the script test every block in the game(up to 1.16.4) for you. It will create a map containing every block that passed the test, allowing you put the map into your script and call `has()` to see what blocks are valid for your use case. Setting `global_fail_bool` to true will produce a second map of every block that failed your test.

# Eye Remover Script
Shift right-click on an end portal frame containing an eye of ender to remove the eye. Note that an existing portal will be removed.

# Volume Script
### Colors are not final and there are still some bugs to be worked out
Attack blocks around your build with a mainhand golden sword to make triangles until you have a complete solid.

Run `/volume calcvolume` to have the script use the [tetrahedral shoelace method](https://ysjournal.com/tetrahedral-shoelace-method-calculating-volume-of-irregular-solids) to find the volume of the region.

This **does not** find the number of full blocks that will approximate this shape. It instead finds the mathematical volume of the exact outlined region. If you wanted the number of blocks, feel free to read [this](http://math.sfsu.edu/beck/papers/noprint.pdf) paper on the topic.

If the output seems off(especially much lower than expected), use `/volume logdata` to drop the relevant data in a text file.
