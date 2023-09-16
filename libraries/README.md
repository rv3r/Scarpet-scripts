# Scarpet Libraries
Pick the library you need and follow [these](https://github.com/gnembon/fabric-carpet/wiki/Installing-carpet-scripts-in-your-world) instructions for installation!

Be sure to call `import()` with the name of the library and all of the methods you would like to use.
If you're using multiple libraries and want to import *every* function from each, I would suggest the following.
```
__on_start() ->
(
	libraries = ['foo','bar','baz'];
	for(libraries,
		import(_,...import(_));
	)
);
```
This allows you to simply list out the libraries and not make individual calls to `import()`.

# [Block Position Library](https://github.com/rv3r/Scarpet-scripts/blob/main/libraries/blockpos.scl)
Library to allow your scripts to save block positions along with associated data. Saved blocks can be searched for certain positions or data. You can save almost any data alongside the block, allowing for very flexible scripts.

Blocks are stored in <script_name>\_blocks.data with the following format:
```
l(                                                    //main list
  m(                                                  //one map for each block
    l('pos',l(x,y,z)),                                //first map value is for position
                                                      //stores position as a list of three elements
                                                      //so you could really put anything here

    l('tags',                                         //second map value is for the associated tags
      m(                                              //this is just an example map
        l('dimension','the_end'),                     //for example, you might want to store the dimension that the block is in
        l('player','rv3r'),                           //or you might want to link the block to a player
        l('block','armor_stand')                      //probably unnecessary, but you could store the block name
        l('armor',l('golden_helmet', 'golden_boots')) //or you might want to store a list of things in one tag
        l('foo','bar')                                //or you might create some app-specific tag with your own key and value
                                                      //you can store whatever you want in this map
      )
    )
  ),

  m(                                                  //next map is for the next block, etc.
    ...
    ...
    ...
  )
)
```

It is suggested that you set a variable to the name of the file that you would like the blocks to be stored in, such as `global_appname = 'beehiveCount'` if used in config or just `appname = 'beehiveCount'` if in the main program. This variable should then be used in any method that requires `appname`.

## Description of methods
* `__initialize(appname)` - `appname`(string)
  - if the current script does not already have an associated file for saving blocks, creates the file, otherwise does nothing
    - It is suggested that you call this in the config of the app or at the beginning of the main program.
    
* `__load_blocks(appname)` - `appname`(string)
  - returns the list of all currently saved blocks
  
* `__save_blocks(appname,data)` - `appname`(string),`data`(list)
  - saves the provided list `data` to the block file
  
* `__find(container,type,values)` - `container`(list),`type`(string),`values`(integer triple if `'type' == 'pos'`, else a map if `'type' == 'tags'`)
  - returns a list of all values in `container` that match the `values` of `type`
    - This will give you all of the stored information(position and associated tags) for each block.
    - This is only meant to be used with this library as `type` must be `'pos'` or `'tags'`, so `container` should be the list of stored blocks, perhaps fetched from `__load_blocks(appname)`
      - `'pos'` - `values` requires a list of three elements
      - `'tags'` - `values` requires a map containing the key `'mode'` and an associated value of `'any'` or `'all'` along with at least one tag to be searched for, such as the key-value pair `l('dimension','the_end')`
        - you might suggest that I use variable length arguments for `'mode'`, but I have avoided this to increase backwards compatibility
        - since the value of `'mode'` is saved and then removed from the map, you can't find blocks that match a value of `'mode'`
        - `'any'` - returns blocks that match _any_ of the provided tags
        - `'all'` - returns blocks that match _all_ of the provided tags
          - if a block has extra tags that are not searched for, it will still be valid as long as the tags being searched for match the values
          - calling `__find(data,'tags',m(l('mode','all'),l('player','rv3r')))` in the above example map would return the first block even though it has extra tags, but searching for `l('tags',m(l('mode','all'),l('player','rv3r'),l('bar','foo')))` would return null(unless, of course, the second block matches it) as the first block has a value of `null` for the key `'bar'`
            - you can use `null` as your value if you want to search for blocks that do not have the key you are searching for
            
* `__add(container,pos,tags)` - `container`(list),`pos`(integer triple),`tags`(map)
  - add a new block at `pos` with associated `tags` to `container`
  - again, `container` should be the list of stored blocks, perhaps fetched from `__load_blocks(appname)`
  - note that `pos` must be a list of three elements. There are no other restrictions.
  - similarly, note that `tags` can be **any** map as there are no restrictions. Have fun with these flexible elements!
  
* `__delete_value(container,pos,tags)` - `container`(list),`pos`(integer triple),`tags`(map)
  - deletes the first element of `container` that _exactly_ matches `pos` and `tags`
  - again, `container` should be the list of stored blocks, perhaps fetched from `__load_blocks(appname)`
  
* `__find_blocks(appname,type,values)` - `appname`(string),`type`(string),`values`(integer triple if `'type' == 'pos'`, else a map if `'type' == 'tags'`)
  - same as `__find` method above except automatically loads list of stored blocks
  
* `__add_block(appname,pos,tags)` - `appname`(string),`pos`(integer triple),`tags`(map)
  - same as `__add` method above except automatically load list of stored blocks, adds block, and saves list of blocks
  
* `__delete_block(appname,pos,tags)` - `appname`(string),`pos`(integer triple),`tags`(map)
  - same as `__delete_value` method above except automatically load list of stored blocks, deletes first block that matches `pos` and `tags` exactly, and saves list of blocks
  
* `__clear_all(appname)` -  `appname`(string)
  - clears _all_ saved blocks
  - **_this is permanent and cannot be undone_**
  

Do note that loading the stored data and parsing the nbt repeatedly is inefficient, so
```
__find_blocks(....);
__add_block(....);
__delete_block(....);
```
is less efficient than
```
data = __load_blocks(....);
__find(data,....);
__add(data,....);
__delete_value(data,....);
__save_blocks(....,data);
```
Thus, the methods that automatically load the data are most efficient when only one needs to be called at a time. If many need to be called in a row, store the current loaded data in a variable and use the simpler methods.

# [Collision Library](https://github.com/rv3r/Scarpet-scripts/blob/main/libraries/collision.scl)
Provides block collision boxes and other useful functions

Uses three common list structures as additional data types
* `prism` - pair of points that comprise opposite vertices of a rectangular prism
  - for example any bottom slab has a `prism` of [ [0,0,0],[1,0.5,1] ], while an azalea has two, [ [0.375,0,0.375],[0.625,0.5,0.625] ] (stem) and [ [0,0.5,0],[1,1,1] ] (bush) 
* `bounds` - list of prisms
  - bottom slab has [ [ [0,0,0],[1,0.5,1] ] ], azalea has [ [ [0.375,0,0.375],[0.625,0.5,0.625] ] , [ [0,0.5,0],[1,1,1] ] ]
* `condensed bounds` - triple of lists of unsorted x-values, y-values, and z-values of any `bounds`, effectively just matrix transpose
  - bottom slab has [ [0,1],[0,0.5],[0,1] ], azalea has [ [0.375,0.625,0,1],[0,0.5,0.5,1],[0.375,0.625,0,1] ]

## Description of methods
* [`__bounds(block)`](https://github.com/rv3r/Scarpet-scripts/blob/4ee08d909731b31b8b04af2025b616db83a99df8/libraries/collision.scl#L495) - `block`(block, float triple, or string)
  - returns `bounds` of `block`, either positioned in world or passed as string
  - note that blocks such as bamboo and pointed dripstone have multiple possible collision boxes, so this script returns the maximum bounds for each
  - blocks with no collision return `null`
* [`__inside(bounds,point)`](https://github.com/rv3r/Scarpet-scripts/blob/4ee08d909731b31b8b04af2025b616db83a99df8/libraries/collision.scl#L852) - `bounds`(bounds),`point`(float triple)
  - returns `boolean` indicating if `point` is in `bounds`
  - positions on the border return `true`
* [`__insideblock(point)`](https://github.com/rv3r/Scarpet-scripts/blob/4ee08d909731b31b8b04af2025b616db83a99df8/libraries/collision.scl#L865) - `point`(float triple)
  - returns `boolean` indicating if `point` is currently inside a collision box
  - positions on the border return `true`
* [`__concatenate(...lists)`](https://github.com/rv3r/Scarpet-scripts/blob/4ee08d909731b31b8b04af2025b616db83a99df8/libraries/collision.scl#L1011) - `lists`(nonzero number of lists of any lengths)
  - more general function returning `list` containing all individual elements of `lists`
* [`__draw_bounds(block,...colors)`](https://github.com/rv3r/Scarpet-scripts/blob/4ee08d909731b31b8b04af2025b616db83a99df8/libraries/collision.scl#L1150) - `block`(block, float triple, or string),`colors`(hex list)
  - draws bounds of `block` as rectangular prisms using optional `colors` for color and fill in that order
* [`__all_collision_blocks()`](https://github.com/rv3r/Scarpet-scripts/blob/4ee08d909731b31b8b04af2025b616db83a99df8/libraries/collision.scl#L1160)
  - returns list of the blocks and their property combinations that produce all unique collision boxes in the game
* [`__filter_sort_direction(blocks,axis,direction,...values)`](https://github.com/rv3r/Scarpet-scripts/blob/4ee08d909731b31b8b04af2025b616db83a99df8/libraries/collision.scl#L1160) - `blocks`(block list),`axis`(`'x'`, `'y'`, or `'z'`, leading `'+'` or `'-'` acceptable),`direction`(`'min'` or `'max'`),`values`(`'include'` or `'exclude'` then floats)
  - returns sorted `block list` by sorting `blocks` along `axis` in `'+'` or `'-'` direction by bottom value(`min`) or top value(`max`) while either only including(`'include'`) blocks with results in `values` or excluding(`'exclude'`) blocks with results in `values`
  - omitting `values` returns all provided blocks, that is, excludes no blocks
  - for example, sorting all blocks(`'__all_collision_blocks()'`) by top(`'max'`) height(`'y'`) descending(`'-'`) while ignoring heights of 0.5(slabs) and 1(solid blocks) can be done with: `__filter_sort_direction( __all_collision_blocks() , '-y' , 'max' , 'exclude' , 0.5 , 1 )`
* [`__prism_in_prism(prism1,prism2)`](https://github.com/rv3r/Scarpet-scripts/blob/4ee08d909731b31b8b04af2025b616db83a99df8/libraries/collision.scl#L1306) - `prism1`(prism),`prism2`(prism)
  - returns `boolean` indicating if `prism1` intersects `prism2`
  - touching at a vertex, edge, or face returns `true`
* [`__entity_block_collision(e)`](https://github.com/rv3r/Scarpet-scripts/blob/4ee08d909731b31b8b04af2025b616db83a99df8/libraries/collision.scl#L1320) - `e`(entity)
  - returns `block list` indicating all blocks that `entity` is currently in contact with
* [`__prism_block_collision(pos,width,height)`](https://github.com/rv3r/Scarpet-scripts/blob/4ee08d909731b31b8b04af2025b616db83a99df8/libraries/collision.scl#L1329) - `pos`(float triple),`width`(float),`height`(float)
  - returns `block list` indicating all blocks that anything at `pos` with properties `width` and `height` would contact

# [Inventory Library](https://github.com/rv3r/Scarpet-scripts/blob/main/libraries/inventory.scl)
Imitates some common actions a player takes.

## Description of methods
* `__shiftclick(source,slot,destination)` - `source`(entity or block with inventory),`slot`(integer),`destination`(entity or block with inventory)
  - shift-clicks item in `slot` from `source` inventory to `destination` inventory
  - imitates 'shift-clicking' of a stack from inventory to another
  - allows you to make a fake player move items between their inventory and a block or another player(isn't realistic but is still possible)
  - **this is only meant for use with players and storage blocks that allow any item in any slot**
    - use with other mobs or furnaces, smokers, blast furnaces, brewing stands, enchanting tables, beacons, anvils, grindstones, cartography tables, looms, smithing tables, etc at your own risk

* `__shiftitem(source,item,destination)` - `source`(entity or block with inventory),`item`(string),`destination`(entity or block with inventory)
  - shift-clicks `item` from `source` inventory to `destination` inventory
  - basically just `__shiftclick()`, but calls `inventory_find()` for you

* `__swap(inventory,slot1,slot2)` - `inventory`(entity or block with inventory),`slot1`(integer),`slot2`(integer)
  - swaps `slot1` and `slot2` inside `inventory`
  - transposes items between two slots, such as when you pick up a stack, left click on another, and set down the new stack
  - allows you to make a fake player move items around in their inventory, allowing for equipping/removing of armor and switching mainhand/offhand item with another item from their inventory

* `__swapto(inventory,item,newslot)` - `inventory`(entity or block with inventory),`item`(string),`newslot`(integer)
  - swaps `item` into `newslot` inside `inventory`
  - swaps a particular item to the desired slot
  - basically just `__swap()`, but calls `inventory_find()` for you

* `__playerset(player,pos,block)` - `player`(player),`pos`(float triple),`block`(block)
  - **note**
    - currently takes only blocks and will overwrite anything
  - if `player` is in creative or survival `player` has requisite block(s), places `block` at `pos`

# [Math Library](https://github.com/rv3r/Scarpet-scripts/blob/main/libraries/rv3r_math.scl)
My personal library of math functions that my scripts require.

## Description of methods
* `__vector(point1,point2)` - `point1`(float list),`point2`(float list of equal length)
  - returns a vector pointing from `point1` to `point2`
* `__magnitude(vector)` - `vector`(float list)
  - returns the magnitude of the provided vector
* `__unit(vector)` - `vector`(float list)
  - returns a vector of magnitude 1 that points in the same direction as the provided vector
* `__dot(vec1,vec2)` - `vec1`(float list),`vec2`(float list)
  - returns the dot product of two equal length vectors
* `__cross3(vec1,vec2)` - `vec1`(float triple),`vec2`(float triple)
  - returns the cross product of two 3D vectors
* `__roundnum(number,digits)` - `number`(float),`digits`(integer)
  - rounds a float to your preferred amount of decimal places
  - use negative numbers for nearest 10, 100, 1000, etc.
* `__sum(list)` - `list`(float list)
  - returns the sum of a list
* `__sign(number)` - `number`(float)
  - returns the sign of a number
* `__choose(n,k)` - `n`(int),`k`(int < `n`)
  - returns `n` choose `k`
* `__det(matrix)` - `matrix`(float 2D list)
  - returns determinant of any size square matrix
* `__plane(point1,point2,point3)` - `point1`(float triple),`point2`(float triple),`point3`(float triple)
  - returns the plane vector representing a plane that goes through all three points
* `__inPlane(point1,point2,point3,point4)` - `point1`(float triple),`point2`(float triple),`point3`(float triple),`point4`(float triple)
  - determines if a 4th point is in the same plane as three other points
* `__heron(a,b,c)` - `a`(float),`b`(float),`c`(float)
  - finds area of a triangle with side lengths `a`, `b`, and `c` using Heron's semiperimeter formula

# [Projectile Hit Library](https://github.com/rv3r/Scarpet-scripts/blob/main/libraries/projectile_hit.scl)
Mirrors original Minecraft code to detect projectile hits. On starting, sets up entity load handlers to signal events every time a projectile hits an entity. Event `projectile_hit` passes original projectile entity, throwing entity, and hit entity.

## Description of methods
* `__handle_entity_hits(...projectiles)` - `projectiles`(nonzero comma separated strings)
  - sets up entity load handler for the listed projectiles and signals event `projectile_hit` when any of those projectiles collide with an entity
* `__get_hit_entity(entity, ...args)` - `entity`(entity if `args` is length 0, entity or string if `args` is length 2, anything if `args` is length 4),`args`(see below)
  - returns entity that `entity` is about to collide with or `null` if no collision is predicted
  - `args` option 1: empty
    - requires `entity` to be the entity of interest
    - allows predicting entity collision of `entity` at `position` with `motion`
  - `args` option 2: `[position, motion]`
    - requires `entity` to be the entity or a string describing the type of the projectile entity
    - if `entity` is a string, library will get width and height itself and use those for calculations, so entity doesn't strictly need to exist
    - allows predicting entity collision if `entity` was in `position` with `motion`
  - `args` option 2: `[position, motion, width, height]`
    - function will just trust whatever values you gave it, so `width` and `height` can be whatever values your heart desires
    - this also means that the function has no need for `entity`, so you can pass it your thesis for all I care
    - function will take absolute value of your `width` and `height` inputs, so negatives are allowed but never suggested
    - allows predicting entity collision if `entity` existed at `position` with `motion` and has `width` and `height`

# [Quaternion Library](https://github.com/rv3r/Scarpet-scripts/blob/main/libraries/quaternions.scl)
Uses [classes.scl](https://github.com/gnembon/scarpet/blob/38ce6bac031ad74470292a3ea687e7f051b6bc57/programs/fundamentals/classes.scl) to implement quaternions in scarpet. I have never studied or used quaternions until about 5 minutes ago so use at your own risk.

## Description of methods
### Quaternion initialization
* `quaternion(real, i, j, k)` - `real`(number), `i`(number), `j`(number), `k`(number)
  - returns a quaternion object
  - takes 1-4 numbers and returns the quaternion object represented by them
  - if given fewer than 4 arguments, sets ending components to 0
    - `quaternion(-2,1) == quaternion(-2,1,0,0)`
* `quaternion(components)` - `components`(list of 1-4 numbers)
  - same as above, but takes a list rather than individual components
    - `quaternion([-2,1]) == quaternion(-2,1,0,0)`

### Object-specific methods as called by `call_function(object, 'function_name', ...args)`
* `components` - no arguments
  - returns a list of 4 numbers representing the real, i, j, and k components of the quaternion
  - `call_function(quaternion(-2,0,3,5),'components') -> [-2,0,3,5]`
* `norm` - no arguments
  - returns a number equal to the quaternion's norm
  - `call_function(quaternion(-2,0,3,5),'norm') -> 6.16`
* `unit` - no arguments
  - returns a quaternion object equal to the original quaternion scaled down by its norm
* `conjugate` - no arguments
  - returns a quaternion object equal to `a - bi - cj - dk`
* `inverse` - no arguments
  - returns a quaternion object equal to the multiplicative inverse of the original quaternion
* `sum` - quaternion
  - returns a quaternion object equal to the componentwise sum of the two quaternions
  - `call_function(quaternion(-2,0,3,5),'sum',quaternion(10,4,-13,6) == quaternion(8,4,-10,11)`
* `difference` - quaternion
  - returns a quaternion object equal to the componentwise difference of the two quaternions
* `product` - quaternion
  - returns a quaternion object equal to the product of the two quaternions by the distributive property and quaternions multiplication rules
  - `i^2 = j^2 = k^2 = ijk = -1`
* `quotient` - quaternion
  - returns a quaternion object equal to the product of the first quaternion and the inverse of the second quaternion
