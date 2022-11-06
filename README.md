# Ariadne-Glasses
Proof-of-concept minecraft mod.

Basically since the cave update it is now possible to just run down the cave and not die instantly, but it can get *really* hard to get back to the surface.
One mod category that might help a bit are mini-maps, but those aren't really survival firendly and don't actually provide that much help with 3d navigation.
My idea was to make something graphically inspired by the lego games. that is levitating etheral naviation poins, except these would be placed as the player runs,
and would show the way back to the staring place and only to the staring place.

Futhermore if a player comes back to the same place (a.k.a creates a cycle in the path) that loop should be deleted. 
To do that we need to check for the closes point in a set. I've implemented an octree to do it very efficenlty in O(log(dist)).

I'm struggling to make it all look nice, as I've said, i was going for a bit smoother etheral appearance. I'm also not sure how to handle deaths.
It could be useful to somehow store the path in an item and give it back to the player to help  navigate to the death place.

Anyway, I might possibly finish this project at some point. If you feel like this idea is worth exploring I would wellcome and input and help.
I still need help with rendering, textures, items, UI elements, etc.

Backnd is basically done. With help form the Cardinal Components I was able to get everything working well on servers.

Sorry for a bit of a rant. Cheers!
