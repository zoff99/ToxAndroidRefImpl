## Tox Default Avatar ( = Identicon)


### steps to create the needed values

0) use the ***Tox-Public-key*** as start input
1) calculate the SHA-256 of ***Tox-Public-key*** = ***hash_01***
2) last 6 bytes of ***hash_01*** = ***hashpart_01***
3) convert ***hashpart_01*** to number (unsigned) = ***hue_01***
4) ***hue_color_1*** = ***hue_01*** / 281474976710655.0
5) ***lig_color_1*** = 0.0
6) ***sat_color_1*** = 0.5
7) convert ***[]_color1*** to RGB = ***color1_rbg***


### example:
Echobot ***Tox ID*** 76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39218F515C39A6

0) ***Tox-Public-key*** = 76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39
1) ***hash_01*** = ecac3754ece2a229dc40f0adff6e3041b8ce4a44c8ec3bd778f90dfd3529e5b7
2) ***hashpart_01*** = 0dfd3529e5b7
3) ***hue_01*** = 15381169825207
3) ***hue_color_1*** = 0.054644894
5) ***lig_color_1*** = 0.0
6) ***sat_color_1*** = 0.5
7) ***color1_rbg*** = [r, g, b] 114, 63, 38


<img src="https://github.com/zoff99/ToxAndroidRefImpl/blob/zoff99/Identicon_spec_dra/echobot_identicon_sqaure.png" width="200">

<img src="https://github.com/zoff99/ToxAndroidRefImpl/blob/zoff99/Identicon_spec_dra/echobot_identicon_round.png" width="200">
