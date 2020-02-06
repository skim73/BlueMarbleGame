*You are on the "master" branch, which contains all resources and source codes written in JavaFX to create this game. To run this game, you must download files in another branch, depending on your computer's OS.

# Running the Game

WINDOWS (.bat file):
- (1) Click on "Branch: master" on top left; switch to "batch" branch
- (2) Click on the green "Clone or download" button on top right; click "Download ZIP"
- (3) Extract it; I suggest having all these files in a folder named "BlueMarbleGame" or something
- (4) Go into "bin" folder & double-click "launch.bat" file
- (5) For convenience, create a shortcut to this "launch.bat" file and locate it somewhere much more accessible; I provided the icon for this shortcut outside the "bin" folder ("bluemarblegame.ico").

# ABOUT BLUE MARBLE 부루마불
"Blue Marble" (부루마불) is essentially the 1982 Korean spin-off of Monopoly, in which players travel around the board, buying properties around the world
    and constructing buildings to raise their rents for the ultimate goal of being the richest player in the game.
    
There are some rules and features that make Blue Marble and Monopoly distinct.

(1) DESERTED ISLAND (on the 10th space from GO)
    - This corresponds to JAIL in Monopoly. However, players who land on this space are immediately trapped there for 3 turns and
    lose their corresponding turn, regardless of whether they had a double or not.
    - During each of the 3 turns, just like Monopoly, players may attempt to escape the Deserted Island by rolling a double.
    If they fail to roll a double, they lose the turn.
    - After 3 turns, they can leave without any payment (unlike JAIL in Monopoly).
    
(2) WELFARE ZONE (20th space) & WELFARE PAY Space (38th space)
    - In some variants of Monopoly, when players land on INCOME TAX or LUXURY TAX, they must place $200 or $100 (respectively) from 
    their wallet to the FREE PARKING SPACE, and whoever lands there takes all the money accumulated there.
    - In Blue Marble, this house rule is enforced: players who land on WELFARE PAY Space must pay $1.50M to the WELFARE ZONE, and
    whoever lands on the WELFARE ZONE takes all money accumulated in this space.
    
(3) SPACE STATION (30th space)
    - When players land here, they can pay $2.00M to the owner of Columbia Space Shuttle (special property located on the 32nd space)
    if they want to choose to which space they want to go to in their next turn.
    - They can select literally any space on the board (except back to Space Station), even the Deserted Island - a popular pick
    when many properties have expensive rents due to numerous office buildings and hotels.
    
(4) CONSTRUCTING BUILDINGS
    - Blue Marble does not require a monopoly over the properties with the same color (In fact, that would be theoretically much 
    more difficult in Blue Marble) for players to start constructing buildings. Also, players do not have to construct buildings
    in hierarchical order (1 house -> 2 houses -> office building -> hotel).
    - After purchasing a property, a player can construct buildings if they land on that property again in the future.
    - The official rule book does not clearly state limits on how many buildings can be constructed on a property. However, I set
    the limit to 2 of each building type. That is, each property can have up to 2 houses, 2 office buildings, and 2 hotels, which
    can immediately be built after a player lands on their property for the first time after purchasing it.
    - Rent values on property cards are per building types (except houses). For example, if there are 2 houses, 2 office buildings, and
    1 hotel on London, the rent is
                    (rent for 2 houses) + 2 × (rent for office building) + 1 × (rent for hotel) = $42.00M
                    (...to be fair, it would cost $26.00M for the owner to have built that much)
                    
(5) SEOUL OLYMPICS (39th space)
    - The 1988 Summer Olympics at Seoul still serves to be a huge source of nationalism. Blue Marble wanted to make this international
    event stand out by making it the most expensive property (by base price) in the game with the base price of $10.00M and a whopping
    rent of $20.00M. Imagine buying Boardwalk(US)/Mayfair(UK) and building a hotel there instantly!
