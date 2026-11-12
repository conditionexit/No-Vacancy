<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.12.2" name="tileset" tilewidth="32" tileheight="32" tilecount="192" columns="12" objectalignment="topleft">
 <image source="tileset.png" width="384" height="512"/>
 <tile id="0">
  <objectgroup draworder="index" id="2">
   <object id="1" x="14.048" y="31.2006">
    <polygon points="0,0 -0.0776134,-7.83895 9.00315,-16.8421 17.9287,-16.9197 17.8511,-10.3226 9.00315,-10.1674 6.13146,-8.07179 5.89862,0.310454"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="1">
  <objectgroup draworder="index" id="2">
   <object id="1" x="0.310454" y="13.3495">
    <polygon points="0,0 31.511,0.310454 31.511,7.68373 0.0776134,7.5285"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="2">
  <objectgroup draworder="index" id="2">
   <object id="1" x="0.543294" y="15.1346">
    <polygon points="0,0 8.38225,0.0776134 18.0063,7.5285 18.0839,16.5317 11.4092,16.5317 11.5644,8.38225 8.22702,5.821 -0.0776134,5.89862"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="11">
  <animation>
   <frame tileid="8" duration="100"/>
   <frame tileid="9" duration="100"/>
   <frame tileid="10" duration="100"/>
   <frame tileid="11" duration="100"/>
  </animation>
 </tile>
 <tile id="12">
  <objectgroup draworder="index" id="2">
   <object id="1" x="13.4271" y="0.543294">
    <polygon points="0,0 0.388067,30.9677 6.44191,31.0454 6.90759,-0.620907"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="14">
  <objectgroup draworder="index" id="2">
   <object id="1" x="12.0301" y="0.388067">
    <polygon points="0,0 0.0776134,31.2006 6.05384,31.2782 5.97623,0"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="24">
  <objectgroup draworder="index" id="2">
   <object id="1" x="13.7376" y="0.23284">
    <polygon points="0,0 0,8.38225 8.77031,16.2212 17.463,16.2988 17.463,11.4868 10.0121,11.3316 7.14043,9.31361 6.9852,-0.0776134"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="25">
  <objectgroup draworder="index" id="2">
   <object id="1" x="0.46568" y="12.2629">
    <polygon points="0,0 31.2782,0 31.2782,5.51055 -0.46568,5.27771"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="26">
  <objectgroup draworder="index" id="2">
   <object id="1" x="18.5496" y="0.543294">
    <polygon points="0,0 0,8.30463 -10.0121,16.066 -18.1615,16.066 -18.2391,10.8659 -10.1674,10.8659 -6.67475,8.14941 -6.67475,-0.46568"/>
   </object>
  </objectgroup>
 </tile>
 <wangsets>
  <wangset name="ter" type="corner" tile="-1">
   <wangcolor name="cliff" color="#ff0000" tile="-1" probability="1"/>
   <wangcolor name="grass" color="#00ff00" tile="-1" probability="1"/>
   <wangtile tileid="0" wangid="0,2,0,1,0,2,0,2"/>
   <wangtile tileid="1" wangid="0,2,0,1,0,1,0,2"/>
   <wangtile tileid="2" wangid="0,2,0,2,0,1,0,2"/>
   <wangtile tileid="12" wangid="0,1,0,1,0,2,0,2"/>
   <wangtile tileid="14" wangid="0,2,0,2,0,1,0,1"/>
   <wangtile tileid="24" wangid="0,1,0,2,0,2,0,2"/>
   <wangtile tileid="25" wangid="0,1,0,2,0,2,0,1"/>
   <wangtile tileid="26" wangid="0,2,0,2,0,2,0,1"/>
   <wangtile tileid="181" wangid="0,2,0,2,0,2,0,2"/>
  </wangset>
 </wangsets>
</tileset>
