# Rushing Offense Notes

## strikeforce0

* General Summary: use random assignment based on last digit of ID for soldiers to either scout or stay close to base. While some soldiers are scouting for enemy soldiers, others are staying close to base building up the army. once an enemy soldier is found, more soldiers go out to attack and hopefully determine a "best guess" archon location (or even locate the archon itself). Once a best guess location (of high enemy density) is found (wait maybe this is something to measure and record...record high densitity regions coordinates and densities), or once archon loc is found, send out a combined strikeforce of lots of soldiers assisted by a sage and accompanied by miners (to pick up their dropped lead). Sage should execute charge if in high density region o fenemy droids, or fury if dangerous enemy buildings **(notes not complete yet)..also do we have code for builders to repair **
* I'm kinda tempted to just do defense strategy. Make watchtowers, high density of soldiers and sages with good defense operations (e.g. turrent to portable archon, idk)
* If enemy sage is detected in range of archon, should archon go to portable mode? 

## Initial Idea
* "Fan Out" search method: while the location of the Enemy Archon is unknown, I wonder if this would be a more efficient routine to find it. 
    * I wonder if a way to do this is to calculate the mean location of all friendly allies, and if that value is not the current value of the given robot, to move in the opposite direction to that average location. 

* pseudocode to code when coms are ready: 
```java


// updated version below 
boolean archon found
nearest archon loc 
lowest health archon nearby loc
lowest health nearby enemy loc
maplocation nearest friendly

check archon section of shared array. 
if an archon id is known: 
    if can attack archon: 
        attack archon; 
    else: 
        move towards archon
    
    update archon location in shared array
    

else:
    if found archon: // do if can detect by type? 
        if can attack it: 
            attack it
            
        update location for the archon id
    else: 
        if enemy soldiers in range: 
            attack enemy soldier with lowest health percent
        else: 
            initiate fan routine 
   
   


    if multiple archon locations are known: 
        if multiple archons are within attack distance: 
            try to attack the one with lowest health. 
        else: 
            if any enemy soldiers // fix prioritization 
            try to move toward closest 
    else: 
        if can attack known archon location: 
            attack known archon location
        else: 
            if any nearby enemy soldiers are within range: 
                try to attack soldier with lowest health 
            else: 
                try to move within strike distance of nearest archon
else: 
    
            
          


// fix this below 
if archon location is known: 
    check that archon hasn't moved. if it has but u can still find it, update shared array. 
    if multiple archons are known and within radius: 
        try to attack local archon with lowest health
    
    if archon loc is known but its out of range: 
        if able: 
            try move to archon
        else:  
            if any enemy soldiers nearby: 
                try to attack the one with lowest health
      
else: 
    search for archon
    if archon loc is found: 
        broadcast to shared array (but make sure it's unique using id...see if it moved)
        if archon can be attacked: 
            attack 
   
    then: 
        loop thru enemy soldiers in region
        find enemy soldier at lowest health % 
        if able to attack that soldier: 
            attack it 
   
    then: 
        move away from closest friendly robot
```
