# ToxIdenticon (= Tox Default Avatar)

This is the specification of an Identicon generation algorithm, tailored to the use
in the Tox network. For the reference implementation see
https://github.com/qTox/qTox/blob/master/src/widget/tool/identicon.cpp

## steps to create the needed color values

***0)*** use the 32 bytes of the **Tox Public Key** as input, see

https://github.com/TokTok/spec/blob/master/spec.md#messenger

***1)*** calculate the SHA-256 of the **Tox Public Key**

```
pk_hash = SHA256(Tox Public Key);
```

***2)*** take the last 6 bytes of `pk_hash` and store them as `hashpart_1` (zero based index 26 - 31)

***3)*** reinterpret `hashpart_1` as unsigned integer, MSB first and store as `hue_uint_1`

```
// example for C99 and C++ (do NOT use this in Java!!)
// hashpart_1 is an array of type uint8_t
uint64_t hue_uint_1 = hashpart_1[0];

// convert the bytes to an uint
for (int i = 1; i < 6; ++i) {
    hue_uint_1 = hue_uint_1 << 8;
    hue_uint_1 += hashpart_1[i];
}
```

***4)*** normalize `hue_uint_1` to a float of the range 0.0 - 1.0 and store as `hue_color_1`

```
// be careful to do the division as float!
// 0xFFFF FFFF FFFF = 281474976710655, the maximum with 48 bits
// '.0' is appended to ensure float division (C and similar languages)
hue_color1 = hue_bytes1 / 281474976710655.0;
```
***5)*** `sat_color_1 = 0.5`,  `lig_color_1 = 0.3` these are constants choosen to look good

***6)*** convert `[hue, sat, lig]_color_1` from HSL to RGB and store as `color1_rbg`

see https://stackoverflow.com/questions/2353211/hsl-to-rgb-color-conversion

***7)*** take the next last 6 bytes of `PkHash` and store them as `hashpart2` (zero based index 20 - 25)

***8)*** reinterpret and normalize the bytes as in steps ***3)*** and ***4)*** and store as `hue_color_2`

***9)*** `sat_color_2 = 0.5`,  `lig_color_2 = 0.8` these are constants choosen to look good

***10)*** convert `[hue, sat, lig]_color_2` from HSL to RGB and store as `color2_rbg` as in step 6)

## steps to create the the Dots on a square grid (5 x 5)

*pseudo code*:

```
for (row = 0; row < 5; ++row)
{
  for (col = 0; col < 5; ++col)
  {
    // mirror on row2
    columnIdx = abs( ((col*2)-4) / 2 )
    pos = row * 3 + columnIdx
    // use one byte from the hash per dot
    byte_used = byte **pos** of **pk_hash**
    // check if byte_used is even or odd
    color_used = byte_used % 2

    if (color_used == 0)
    {
       // even, use color1
       dot has color **color1_rbg**
    }
    else
    {
       // odd, use color2
       dot has color **color2_rbg**
    }
  }
}
```

now draw those dots on a grid like:

|  **Grid**  |            |     | ||
| ----------- | ----------:| ---:|---:|---:|
| row0/col0  | * | * | * | row0/col4 |
| * | * | * | * | * |
| * | * | * | * | * |
| * | * | * | * | * |
| row4/col0 | * | * | * | row4/col4 |

This can now be scaled to fit your needs.

## Examples

Echobot ***Tox ID*** 0x76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39218F515C39A6 // in Hex

### steps to create the color values

0) ***Tox Public Key*** = 0x76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39 // in Hex
1) ***pk_hash*** = 0xecac3754ece2a229dc40f0adff6e3041b8ce4a44c8ec3bd778f90dfd3529e5b7 // in Hex
2) ***hashpart_1*** =  0x0dfd3529e5b7 // in Hex
3) ***hue_uint_1*** = 15381169825207 // in Decimal
4) ***hue_color_1*** = 0.054644894 // is [~ 19.6175 °] on a color wheel, range [0.0 .. 1.0]
5)
    - ***sat_color_1*** = 0.5 (is [50 %], range [0.0 .. 1.0])
    - ***lig_color_1*** = 0.3 (is [30 %], range [0.0 .. 1.0])
6) ***color1_rbg*** =  114, 63, 38 // [r, g, b] color values for 8bit per color

7) ***hashpart_2*** = 0xc8ec3bd778f9 // in Hex
8)
    - ***hue_uint_2*** = 220916941814009 // decimal
    - ***hue_color_2*** = 0.78485465 // is [~ 281.7628 °] on a color wheel, range [0.0 .. 1.0]
9)
    - ***sat_color_2*** = 0.5 // is [50 %], range [0.0 .. 1.0]  
    - ***lig_color_2*** = 0.8 // is [80 %], range [0.0 .. 1.0]  
10) ***color2_rbg*** =  214, 178, 229 // [r, g, b] color values for 8bit per color

*Note:* The color values can slightly differ, because of different HSV2RGB conversion formulas
and the use of `float`. In practice this should not be noticeable by the human eye.


###  color the dots

```
**hash_01** = 0xecac3754ece2a229dc40f0adff6e3041b8ce4a44c8ec3bd778f90dfd3529e5b7 // in Hex

for (row = 0; row < 5; ++row)
{
  for (col = 0; col < 5; ++col)
  {
    columnIdx = abs( ((col*2)-4) / 2 )
    // example: col=0 row=0 columnIdx=2

    pos = row * 3 + columnIdx
    // pos = 2 // in **hash_01**

    byte_used = byte **pos** of **hash_01**
    // byte_used = 0x37 // in Hex (= 55 in decimal)

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
