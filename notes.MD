# Notes
- document organization will likely be optimized :)
## Misc. 

### thaddaeus' first read thru of specs
* Is it worth it to sense when anomolies will occur and to send buildings into portable mode to hide from them due to the 5% fury anomoly? 
* For charge it seems like we can just disperse droids before anomolies? 
* the anomolies seem managable ngl.  
* "It is guaranteed that at least one of your Archons will initially have a Lead deposit within its vision range." -- important
* "Each map will also have a “schedule” for Anomalies, which describes the round and type of all naturally-occurring Anomalies. Players can sense this schedule in order to vary their strategy!" -- anomolies are managable, as noted above
* "Every round, each team will gain a passive income of 2 Pb. Additionally, every 20 rounds, any square of the map containing at least 1 Pb will generate an additional 5 Pb." -- any incentive to leave some squares unmined? or at least leave squares with 1 pb?
* rubble: we should probably write different navigation algos...one could be something like "efficient path generator" which determines the fastest path to a point given knowledge of rubble in possible transit squares a long the way. 
        * or at least end step on minimal rubble spot lol (or balance that with time lost being further away)
* resource reclaim: seems very useful. if we can track death spots of ours / enemies bots (and if those dead bots have already been harvested), we can get a lot of income perhaps. 
        * we could call routines related to this scavenger routines (lol so original)
        * perhaps these will be most efficient right after anomolies happen
* communication
        * how do we know the other team won't just fill the array with junk? is this a strategy we can try to confuse them? perhaps we test to see if there are patterns in the other teams signals, if so either fill the array with junk or even clone commands that the other team uses and put them back in to confuse the other teams robots?
                * since the bytecode cost is 100 per write it may not be worth it to "jam the enemy signal" unless we see they are using it a lot. 
        * 2^16 = 65536 ... what are efficient ways to use each number? 
                * for 0 x 60 tiles there are 3600 location states.
                * what if we have some kind of signaling that sacrificies accuracy for information in each integer? so like if we only use the last 3 digits in the item itegers, we have 100% accurate locations up to 31x31 bit maps, after our location estimates get "noisier" I guess. like for a 60x60 map, max accuracy is 3600/999 ~= 3.6? 
                        * that would leave us with ~ 100 other states to work with (i.e. 00 to 99)
                                * actually two cheap robots like miners with a limit of 7500 could together write the entire array `7500*2=1500>>>64` items in shared array
                        * this kinda makes sense bc we can use this to guide robots into a region of interest, and once that goal location is within their radius they can zoom in on it. 
                        * **I have notes on this in my notebook with drawings, add these to this file sometime** we can call it something like *scaled distance communication* 
* winning
        * can we sense other teams levels of pb and gold? stockpile as we approach round 2000? 
        * I wonder how important mutating will be 
* general: we need offensive / defensive strategies. we also need meta. maybe we split like this during first two weeks: thaddaeus/miles: communication, mel/jason: attack/defense, and we all think about meta on the side (and possibly steal best meta in third week) ... idk
        * we gotta get that rusher / acheon hunter strat going while defending our own
        * we can prob play our own bot versions against themselves too
        * for charging / rushing the enemy archons, once the location is known / scouted out, we could destroy it quickly with rapid strike team equipped with sages as force multipliers LOL (charge/volly seem nice)
        * will there be bidding for array appending? 
