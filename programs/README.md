# Scarpet Scripts
Pick the script you need and follow [these](https://github.com/gnembon/fabric-carpet/wiki/Installing-carpet-scripts-in-your-world) instructions for installation!

# Beehive Count
Right click a bee nest or beehive with an empty mainhand to learn how many bees are in the block.

# Block Test
Uses RamsaKd's array of every Minecraft block

Create your own test in `your_test_function_here()` and let the script test every block in the game(up to 1.16.5) for you. It will create a map containing every block that passed the test, allowing you put the map into your script and call `has()` to see what blocks are valid for your use case. Setting `global_fail_bool` to true will produce a second map of every block that failed your test.

# Bucket Stack
Bucket placement written by [Firigion](https://github.com/Firigion)

Be sure to edit the numbers at the beginning of the script so you can have your own custom bucket stack sizes!

Allows every(up to 1.16.5) nonstackable bucket type to stack when a player picks them up and allows for using buckets similar to honey bottles. You get the empty buckets back in your inventory unless you don't have space.

# Despawn
Gives those of us who don't use [MiniHUD](https://masa.dy.fi/mcmods/all_mods/?mcver=1.16.4&mod=minihud) a despawn sphere around the player showing where mobs will despawn. You can pick any time in seconds under an hour and choose from one of six fantastic colors, red, green, blue, black, gray(or grey), and white. Default sphere is red and lasts for 60 seconds.

# Eye Remover
Shift right-click on an end portal frame containing an eye of ender to remove the eye. Note that an existing portal will be removed.

# Volume
### Requires my rv3r_math.scl library
### Colors are not final and there are still some bugs to be worked out
Attack blocks around your build with a mainhand golden sword to make triangles until you obtain the region you want to manipulate.

### Data Manipulation

`/volume clear` - clears all data so you can start fresh, same as `/volume clear all`

`/volume clear <list>` - clears only one list of data, normally not necessary as `/volume clear` is most useful

`/volume show edges` - draws red lines to indicate edges, incomplete edges are unpaired and are used in `/volume perimeter`, `/volume fill border <block>`, and cause `/volume volume` to throw an error

`/volume show faces` - draw blue lines to indicate faces used in `/volume area`

### Geometric Properties

`/volume perimeter` - finds the perimeter of an open region that you've outlined with simple vector magnitudes

`/volume area` - finds the area of the region you've outlined using Heron's formula for each face

`/volume volume` - uses the [tetrahedral shoelace method](https://ysjournal.com/tetrahedral-shoelace-method-calculating-volume-of-irregular-solids) to find the volume of the region

This **does not** find the number of full blocks that will approximate this shape. It instead finds the mathematical volume of the exact outlined region. If you wanted the number of blocks, feel free to read [this](http://math.sfsu.edu/beck/papers/noprint.pdf) paper on the topic.

If the output seems off(especially much lower than expected), use `/volume logdata` to drop the relevant data in a text file.

### World Manipulation
These require you to select a set of faces in one plane(x-y,y-z,z-x). Failure to do so results in an error being thrown.

`/volume fill all <block>` - fills every face with the block of your choice

`/volume fill border <block>` - fills just the outside border of the region with the block of your choice

`/volume extrude <pos> <int>` - repeats the entire region in a direction normal to the faces every `int` blocks up to `pos`
