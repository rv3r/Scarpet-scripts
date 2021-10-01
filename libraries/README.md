# Scarpet Libraries
Pick the library you need and follow [these](https://github.com/gnembon/fabric-carpet/wiki/Installing-carpet-scripts-in-your-world) instructions for installation!

Be sure to call `import()` with the name of the library and all of the methods you would like to use.

# Block Position Library
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

### Description of methods
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

# Collision Library
Provides block collision boxes and other useful functions

Uses three common list structures as additional data types
* `prism` - pair of points that comprise opposite vertices of a rectangular prism
  - for example any solid block has a `prism` of [0,0,0],[1,1,1]
* `bounds` - list of prisms
  - solid block has [ [0,0,0],[1,1,1] ]
* `condensed bounds` - triple of lists of x-values, y-values, and z-values of any `bounds`, effectively just matrix transpose
  - solid block has [ [0,1],[0,1],[0,1] ]

## Description of methods
* `__bounds(block)` - `block`(block)
  - returns `bounds` of `block`, either positioned in world or passed as string
  - note that blocks such as bamboo and pointed dripstone have multiple possible collision boxes, so this script returns the maximum bounds for each
  - blocks with no collision return `null`
* `__inside(bounds,point)` - `bounds`(bounds),`point`(float triple)
  - returns `boolean` indicating if `point` is in `bounds`
  - positions on the border return `true`
* `__insideblock(point)` - `point`(float triple)
  - returns `boolean` indicating if `point` is currently inside a collision box
* `__concatenate(...lists)` - `lists`(nonzero number of lists of any lengths)
  - more general function returning `list` containing all individual elements of `lists`
* `__draw_bounds(block,...colors)` - `block`(block),`colors`(edge and fill colors as hex values)
  - draws bounds of `block` as rectangular prisms using optional `colors`
* `__all_collision_blocks()`
  - returns list of all unique blocks and their property combinations that produce all unique collision boxes in the game
* `__filter_sort_direction(blocks,axis,direction,...values)` - `blocks`(block list),`axis`(`'x'`, `'y'`, or `'z'`, leading `'+'` or `'-'` acceptable),`direction`(`'min'` or `'max'`),`values`(`'include'` or `'exclude'` then floats)
  - returns sorted `block list` by sorting `blocks` along `axis` in `'+'` or `'-'` direction by bottom value(`min`) or top value(`max`) while either only including(`'include'`) blocks with results in `values` or excluding(`'exclude'`) blocks with results in `values`
  - omitting `values` returns all provided blocks, that is, excludes no blocks
  - for example, sorting all blocks by top(`'max'`) height(`'y'`) descending(`'-'`) while ignoring heights of 0.5(slabs) and 1(solid blocks) can be done with: `__filter_sort_direction( __all_collision_blocks() , '-y' , 'max' , 'exclude' , 0.5 , 1 )`
* `__prism_in_prism(prism1,prism2)` - `prism1`(prism),`prism2`(prism)
  - returns `boolean` indicating if `prism1` intersects `prism2`
  - touching at a vertex, edge, or face returns `true`
* `__entity_block_collision(e)` - `e`(entity)
  - returns `block list` indicating all blocks that `entity` is currently in contact with
* `__prism_block_collision(pos,width,height)` - `pos`(float triple),`width`(float),`height`(float)
  - returns `block list` indicating all blocks that anything at `pos` with properties `width` and `height` would contact

# Inventory Library
Imitates two common actions the player uses in an inventory.

### Description of methods
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

# Math Library
My personal library of math functions that my scripts require.

### Description of methods
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
