# Scarpet Scripts
Pick the script you need and follow [these](https://github.com/gnembon/fabric-carpet/wiki/Installing-carpet-scripts-in-your-world) instructions for installation!

# Beehive Count
Right-click a bee nest or beehive with an empty mainhand to learn how many bees are in the block.

# Block Test
Create your own test in `your_test_function_here()` and let the script test every block in the game for you. It will create a map containing every block that passed the test, allowing you put the map into your script and call `has()` to see what blocks are valid for your use case. Setting `global_fail_bool` to true will produce a second map of every block that failed your test.

# Bolan Mine
Applies ideas from Matthew Bolan's [ore finding video](https://youtu.be/5Icj5TNmBUI) to determine the best spots to mine for ores based on the location of clay and gravel patches in your pre-1.17 world.

Left-click on a central clay or gravel block to do stuff.

Use a sword to scan the region below the patch for ores. Switch hands within 5 seconds of scanning to permanently log the data in a text file. The script will alert you if a block has already been scanned.

Use a pickaxe to display labels showing you where to mine for any given ore.

`/bolanmine <base> <ore>` - shows a histogram of offsets for the given `<ore>` relative to the center of the patch of `<base>`

# Bucket Stack
Bucket placement written by [Firigion](https://github.com/Firigion)

Be sure to edit the numbers at the beginning of the script so you can have your own custom bucket stack sizes!

Makes nonstackable bucket types stack and use similar to honey bottles. You get the empty buckets back in your inventory unless you don't have space.

`/bucketstack <bucket>` - print stack size of one or all buckets

`/bucketstack <bucket> <stack_size>` - change the stack size to a desired value

# Death
Gives players who don't use [Tweakeroo](https://masa.dy.fi/mcmods/all_mods/) a death message indicating coordinates and dimension of death. Also prints death messages to server log.

# Despawn
Gives players who don't use [MiniHUD](https://masa.dy.fi/mcmods/all_mods/) a despawn sphere around the player showing where mobs will despawn. You can pick any time in seconds under an hour and choose from one of six fantastic colors, red, green, blue, black, gray(or grey), and white. Default sphere is red and lasts for 60 seconds.

# Ellipsoid
Draws ellipsoids(default green color) with `draw_shape()` instead of setting blocks.

`/ellipsoid <center> <xradius> <yradius> <zradius> <radial> <longitudinal> <time>` - draws ellipsoid at `center` with the given radii(`xradius`,`yradius`,`zradius`) and line densities of `radial`(around the longest axis) and `longitudinal`(along the longest axis) for `ticks`

# Eye Remover
Shift right-click on an end portal frame containing an eye of ender to remove the eye. Note that an existing portal will be removed.

# Inventory Sort
### Requires my inventory.scl library
### This is still missing features for deleting and overwriting of preferences
Allows players to set up inventory preferences for later sorting with a single command(`/invsort`). Each slot can be individually tailored based on what the player currently has in their inventory. It is suggested that you remove all but the essentials before `/invsort setup`.

# No Void Damage
Heals players if they fall into the void. This script assumes you've fallen into the void in the End as it will teleport you back to the End platform after 10 seconds if you fail to leave the void. Useful for when the server lets you glitch through the world and fall into the void. There's no point in losing your stuff if it wasn't your fault.

# Portal Orient
Makes a player face away from a specified side of a Nether portal after a dimension change. Now you won't have to remember which way to face when getting in the portal. Settings are player specific and script will do nothing if each side has the same number of valid blocks.

`/portalorient off` - does not change player orientation after portalling

`/portalorient air` - makes the player face the portal side with more air blocks

`/portalorient solid` - makes the player face the portal side with fewer solid blocks

# Smelter Debug
Aids in debugging while designing smelter arrays by reducing need to open furnace GUIs. Hold coal and right-click on two corners of a cuboid to cache all furnaces, smokers, and blast furnaces in the region. Shift right-click on any furnace variant to compare all other furnace variants of the same type. Cuboid region and main furnace variant can always be overwritten.

Color legend:

- green: matches main block inventory
- yellow: correct item, incorrect count
- red: wrong item

`/smelterdebug clear data` - clears position cache and main furnace variant positions

`/smelterdebug clear inventories` - clears the inventory of every cached furnace variant, best used when everything breaks and `/fill` takes too long

`/smelterdebug display <display_bool>` - toggles display on the front of each furnace, indicating item discrepancies or count discrepancies

`/smelterdebug print` - prints positions and discrepancies of any furnaces that do not match the inventory of the main block, click on position in chat to look at the block

# Volume
### Requires my rv3r_math.scl library
Attack blocks around your build with a mainhand golden sword to make triangles until you obtain the region you want to manipulate.

### Data Manipulation

`/volume clear` - clears all data so you can start fresh, same as `/volume clear all`

`/volume clear <list>` - clears only one list of data, normally not necessary as `/volume clear` is most useful

`/volume show edges` - draws red lines to indicate edges, incomplete edges are unpaired and are used in `/volume perimeter`, `/volume fill ...`, and cause `/volume volume` to throw an error

`/volume show faces` - draw blue lines to indicate faces used in `/volume area`

### Geometric Properties

`/volume perimeter` - finds the perimeter of an open region that you've outlined with simple vector magnitudes

`/volume area` - finds the area of the region you've outlined using Heron's formula for each face

`/volume volume` - uses the [tetrahedral shoelace method](https://ysjournal.com/tetrahedral-shoelace-method-calculating-volume-of-irregular-solids) to find the volume of the region

This **does not** find the number of full blocks that will approximate this shape. It instead finds the mathematical volume of the exact outlined region. If you wanted the number of blocks, feel free to read [this](http://math.sfsu.edu/beck/papers/noprint.pdf) paper on the topic.

If the output seems off(especially much lower than expected), use `/volume logdata` to drop the relevant data in a text file.

### World Manipulation
These require you to select a set of faces in one plane(x-y,y-z,z-x). Failure to do so results in an error being thrown. In survival, this will use blocks from a player's inventory, will only replace air and liquids, will not replace blocks, will update set blocks, and will only set empty containers. In creative, this will freely set blocks, will default to not replacing any blocks, will default to setting blocks without updates, and will set filled containers.

`/volume fill all <block> <replace?> <update?>` - fills every face with the block of your choice

`/volume fill border <block> <replace?> <update?>` - fills just the outside border of the region with the block of your choice

`/volume extrude <pos> <period?> <replace?> <update?>` - repeats the entire region in a direction normal to the faces every `period` blocks up to `pos`
