<?xml version="1.0" encoding="UTF-8"?>
<!-- The sheet uses 208 x 434 cells with a 10-pixel outer margin. -->
<!-- Rows: down, left, right, up. Four pictures per row, 120 ms per picture.
     Java selects animation definitions 3, 7, 11 and 15 using local tile IDs.
     These frame sizes describe the source image; Java W and H set display size. -->
<tileset version="1.10" tiledversion="1.12.2" name="chirurgeons" tilewidth="208" tileheight="434" margin="10" tilecount="16" columns="4">
 <image source="chirurgeons_walk.png" width="887" height="1774"/>
 <!-- Down: play the following four frames in order, then repeat. -->
 <tile id="3">
  <animation>
   <frame tileid="0" duration="120"/>
   <frame tileid="1" duration="120"/>
   <frame tileid="2" duration="120"/>
   <frame tileid="3" duration="120"/>
  </animation>
 </tile>
 <!-- Left: play the following four frames in order, then repeat. -->
 <tile id="7">
  <animation>
   <frame tileid="4" duration="120"/>
   <frame tileid="5" duration="120"/>
   <frame tileid="6" duration="120"/>
   <frame tileid="7" duration="120"/>
  </animation>
 </tile>
 <!-- Right: play the following four frames in order, then repeat. -->
 <tile id="11">
  <animation>
   <frame tileid="8" duration="120"/>
   <frame tileid="9" duration="120"/>
   <frame tileid="10" duration="120"/>
   <frame tileid="11" duration="120"/>
  </animation>
 </tile>
 <!-- Up: play the following four frames in order, then repeat. -->
 <tile id="15">
  <animation>
   <frame tileid="12" duration="120"/>
   <frame tileid="13" duration="120"/>
   <frame tileid="14" duration="120"/>
   <frame tileid="15" duration="120"/>
  </animation>
 </tile>
</tileset>
