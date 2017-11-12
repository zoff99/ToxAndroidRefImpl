## ToxIdenticon (= Tox Default Avatar)


### steps to create the needed color values

0) use the ***Tox-Public-key*** as start input
1) calculate the SHA-256 of ***Tox-Public-key*** = ***hash_01***
2) last 6 bytes of ***hash_01*** = ***hashpart_01***
3) convert ***hashpart_01*** to number (unsigned) = ***hue_01***
4) ***hue_color_1*** = (***hue_01*** / 281474976710655.0) <br>
    // decimal (0xff ff ff ff ff ff in Hex) ** *see image below!
5) ***sat_color_1*** = 0.5
6) ***lig_color_1*** = 0.3
7) convert ***[hue, sat, lig]_color1*** to RGB = ***color1_rbg*** <br>
    // convert HSL -> RGB ** *see https://stackoverflow.com/questions/2353211/hsl-to-rgb-color-conversion
<br>

8) last 12 bytes of ***hash_01*** and then the first 6 bytes of that = ***hashpart_02***
9) convert ***hashpart_02*** to number (unsigned) = ***hue_02***
10) ***hue_color_2*** = (***hue_02*** / 281474976710655.0) <br>
    // decimal (0xff ff ff ff ff ff in Hex) ** *see image below!
11) ***sat_color_2*** = 0.5
12) ***lig_color_2*** = 0.8
13) convert ***[hue, sat, lig]_color2*** to RGB = ***color2_rbg*** <br>
    // convert HSL -> RGB ** *see https://stackoverflow.com/questions/2353211/hsl-to-rgb-color-conversion
<br>

<img src="https://github.com/zoff99/ToxAndroidRefImpl/blob/zoff99/Identicon_spec_dra/hex_2_dec_fixed001.png" height="80">
<br>

### steps to create the the Dots on a square grid (5 x 5)

*pseudo code*:

```
for (row = 0; row < 5; ++row)
{
  for (col = 0; col < 5; ++col)
  {
    columnIdx = abs( ((col*2)-4) / 2 )
    pos = row * 3 + columnIdx
    byte_used = byte **pos** of **hash_01**
    color_used = byte_used % 2

    if (color_used == 0)
    {
       dot has color **color1_rbg**
    }
    else
    {
       dot has color **color2_rbg**
    }
  }
}
```

now draw those dots on a raster like:

|  **Raster**  |            |     | ||
| ----------- | ----------:| ---:|---:|---:|
| row0/col0  | * | * | * | row0/col4 |
| * | * | * | * | * |
| * | * | * | * | * |
| * | * | * | * | * |
| row4/col0 | * | * | * | row4/col4 |



### example:

Echobot ***Tox ID*** 0x76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39218F515C39A6 // in Hex

#### steps to create the needed color values

0) ***Tox-Public-key*** = 0x76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39 // in Hex
1) ***hash_01*** = 0xecac3754ece2a229dc40f0adff6e3041b8ce4a44c8ec3bd778f90dfd3529e5b7 // in Hex
2) ***hashpart_01*** = 0x0dfd3529e5b7 // in Hex
3) ***hue_01*** = 15381169825207 // decimal
4) ***hue_color_1*** = [0.0 .. 1.0] 0.054644894 [~ 19.6175 °]
5) ***sat_color_1*** = [0.0 .. 1.0] 0.5 [50 %]
6) ***lig_color_1*** = [0.0 .. 1.0] 0.3 [30 %]
7) ***color1_rbg*** = [r, g, b] 114, 63, 38
<br>

8) ***hashpart_02*** = 0xc8ec3bd778f9 // in Hex
9) ***hue_02*** = 220916941814009 // decimal
10) ***hue_color_2*** = [0.0 .. 1.0] 0.78485465 [~ 281.7628 °]
11) ***sat_color_2*** = [0.0 .. 1.0] 0.5 [50 %]
12) ***lig_color_2*** = [0.0 .. 1.0] 0.8 [80 %]
13) ***color2_rbg*** = [r, g, b] 214, 178, 229
<br>

### example:

```
**hash_01** = 0xecac3754ece2a229dc40f0adff6e3041b8ce4a44c8ec3bd778f90dfd3529e5b7 // in Hex

for (row = 0; row < 5; ++row)
{
  for (col = 0; col < 5; ++col)
  {
    columnIdx = abs( ((col*2)-4) / 2 )
    // example: col=0 row=0 columnIdx=2

    pos = row * 3 + columnIdx
    // pos = 2 // in **has_01**

    byte_used = byte **pos** of **hash_01**
    // byte_used = 0x37 // in Hex

    color_used = byte_used % 2
    // color_used = 1 // lighter color

    if (color_used == 0)
    {
       // left upper dot has the ligher color
       dot has color **color1_rbg**
    }
    else
    {
       dot has color **color2_rbg**
    }
  }
}
```




http://www.rapidtables.com/convert/color/hsl-to-rgb.htm

### Resulting Identicon for square images:

<img src="https://github.com/zoff99/ToxAndroidRefImpl/blob/zoff99/Identicon_spec_dra/echobot_identicon_sqaure.png" width="200">

### Resulting Identicon for round images:
fill extra space on background with ***color1_rbg***

<img src="https://github.com/zoff99/ToxAndroidRefImpl/blob/zoff99/Identicon_spec_dra/echobot_identicon_round.png" width="200">
