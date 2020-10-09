# Scarpet Scripts
Pick the script you need and follow [these](https://github.com/gnembon/fabric-carpet/wiki/Installing-carpet-scripts-in-your-world) instructions for installation!

# blockPos Library
### Incomplete and potentially privated at the moment but has the following goals: 
Provides methods to
* combine a block position as a triple and any associated tags in a map into a list to be saved under <script_name>_blocks.data
* delete any saved block positions and their associated tags
* return a list of all instances of a particular position, anything that matches a given tag value, or positions that match all tag values

# Volume Script
### Colors are not final and there are still some bugs to be worked out
Attack blocks around your build with a mainhand golden sword to make triangles until you have a complete solid.

Run `/volume calcvolume` to have the script use the [tetrahedral shoelace method](https://ysjournal.com/tetrahedral-shoelace-method-calculating-volume-of-irregular-solids) to find the volume of the region.

This **does not** find the number of full blocks that will approximate this shape. It instead finds the mathematical volume of the exact outlined region. If you wanted the number of blocks, feel free to read [this](http://math.sfsu.edu/beck/papers/noprint.pdf) paper on the topic.

If the output seems off(especially much lower than expected), use `/volume logdata` to drop the relevant data in a text file.
