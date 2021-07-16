# ExtraCells 2

A mighty Add-On for Applied Energistics 2

To build this, download the project (ZIP or fork or whatever) and execute the build.bat/build.sh. The build files will be in /build/lib/

Thanks to arbrarsyed for ForgeGradle, to AlgorithmX2 for his massive support and his *amazing* API, cyntain and tgame14 for some very cool textures and Vexatos, crafteverywhere, SSCXM and VeryBigBro for the cool localizations! 

ExtraCells 2 is licensed under the MIT license.

# ExtraCells2 fork changelog

- [47e5915](https://github.com/AndrewB330/ExtraCells2/commit/47e5915a175dfc0975be09a99dd790a9b047903c)
  Optimized OreDictBus filter - now filter is being processed only once! Now after any filter update we are building 
  simple expression tree and then using it.
  Added `!` `~` `@` operators, for negation, name search, mod search. Also added `|` and `&` operators.
  Example of possible query: `ore* & !*redstone* | crushed* & !*lead | dust*` (in theory, but filter length is limited :) ).

- [e1396d7](https://github.com/AndrewB330/ExtraCells2/commit/e1396d7347eb93e1972e02d82958ef6ee22dc114)
  Fixed GregTech fluid colors in Fluid Interface (previously most of them were grey).
  Added displaying for amount of fluid for fluids in the Fluid Interface.

- [18d0a59](https://github.com/AndrewB330/ExtraCells2/commit/18d0a59dfa27e35c2d34b2cfb04507adcd92bc38) 
  Added scrollbar to Fluid Terminal. Now fluid terminal extends AE terminal.
  +Small refactoring and fixed scroll behaviour. Now scrolling works perfectly!

- [45cb2ec](https://github.com/AndrewB330/ExtraCells2/commit/45cb2ecf89a5051341f1831690bab940a99888c4) 
  Fixed fluid terminal key events handling and searchbar focus.
  Now you can exit terminal by pressing E (Inventory button) and searchbar focusing now works properly.
  Now you can click on searchbar with right mouse button to clear the filter.

