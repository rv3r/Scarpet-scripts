# Scarpet Scripts
Pick the script you need and follow [these](https://github.com/gnembon/fabric-carpet/wiki/Installing-carpet-scripts-in-your-world) instructions for installation!

# Beehive Count Script
Right click a bee nest or beehive with an empty mainhand to learn how many bees are in the block.

# Block Position Library
### Has not been tested yet in another script but does work on its own 
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
        l('player','AnonymousRover'),                 //or you might want to link the block to a player
        l('block','armor_stand')                      //probably unnecessary, but you could store the block name
        l('armor',l('golden_helmet', 'golden_boots')) //or you might want to store a list of things in one tag
        l('foo','bar')                                //or you might create some app-specific tag with your own key and value
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
  
* `__find(container,type,values)` - `container`(list),`type`(string),`values`(3-element list if `'type' == 'pos'`, else a map if `'type' == 'tags'`)
  - returns a list of all values in `container` that match the `values` of `type`
    - This will give you all of the stored information(position and associated tags) for each block.
    - This is only meant to be used with this library as `type` must be `'pos'` or `'tags'`, so `container` should be the list of stored blocks, perhaps fetched from `__load_blocks(appname)`
      - `'pos'` - `values` requires a list of three elements
      - `'tags'` - `values` requires a map containing the key `'mode'` and an associated value of `'any'` or `'all'` along with at least one tag to be searched for, such as the key-value pair `l('dimension','the_end')`
        - `'any'` - returns blocks that match _any_ of the provided tags
        - `'all'` - returns blocks that match _all_ of the provided tags
          - if a block has extra tags that are not searched for, it will still be valid as long as the tags being searched for match the values
          - searching for `l('tags',m(l('mode','all'),l('player','AnonymousRover')))` in the above example map would return the first block even though it has extra tags, but searching for `l('tags',m(l('mode','all'),l('player','AnonymousRover'),l('bar','foo')))` would return null(unless, of course, the second block matches it) as the first block has a value of `null` for the key `'bar'`
            - you can use `null` as your value if you want to search for blocks that do not have the key you are searching for
            
* `__add(container,pos,tags)` - `container`(list),`pos`(list),`tags`(map)
  - add a new block at `pos` with associated `tags` to `container`
  - again, `container` should be the list of stored blocks, perhaps fetched from `__load_blocks(appname)`
  - note that `pos` must be a list of three elements. There are no other restrictions.
  - similarly, note that `tags` can be almost **any** map as the only restriction is that it cannot use a key of `'mode'`. Have fun with these flexible elements!
  
* `__delete_value(container,pos,tags)` - `container`(list),`pos`(list),`tags`(map))
  - deletes the first element of `container` that _exactly_ matches `pos` and `tags`
  - again, `container` should be the list of stored blocks, perhaps fetched from `__load_blocks(appname)`
  
* `__find_blocks(appname,type,values)` - `appname`(string),`type`(string),`values`(3-element list if `'type' == 'pos'`, else a map if `'type' == 'tags'`)
  - same as `__find` method above except automatically loads list of stored blocks
  
* `__add_block(appname,pos,tags)` - `appname`(string),`pos`(list),`tags`(map)
  - same as `__add` method above except automatically load list of stored blocks, adds block, and saves list of blocks
  
* `__delete_block(appname,pos,tags)` - `appname`(string),`pos`(list),`tags`(map)
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
  
# Volume Script
### Colors are not final and there are still some bugs to be worked out
Attack blocks around your build with a mainhand golden sword to make triangles until you have a complete solid.

Run `/volume calcvolume` to have the script use the [tetrahedral shoelace method](https://ysjournal.com/tetrahedral-shoelace-method-calculating-volume-of-irregular-solids) to find the volume of the region.

This **does not** find the number of full blocks that will approximate this shape. It instead finds the mathematical volume of the exact outlined region. If you wanted the number of blocks, feel free to read [this](http://math.sfsu.edu/beck/papers/noprint.pdf) paper on the topic.

If the output seems off(especially much lower than expected), use `/volume logdata` to drop the relevant data in a text file.
