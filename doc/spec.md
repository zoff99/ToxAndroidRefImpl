
# TRIfA and ToxProxy addons
<br>

## Offline Messages Workflows
<br>
<br>




```
         +---------------------------------------------+  
         |                                             | 
         | Self Hosted                                 |
         |                                             | 
         |+-------------------+                        | 
         ||                   |                        | 
         ||    ToxProxy       |                        | 
         ||                   |                        | 
         |+--------^----------+                        | 
         +---------|-----------------------------------+ 
                   |                                              
                   |                                              
                   +-------------------------+                    
                                             |                     
                                             | (1) Add ToxProxy as friend                    
                                             |
                     Android Device          |                     
               +------------------------+    |  
               |                        |    |                     
               |                        |    |
               |                        |    |
               |         +-----------+  |    |
               |         |TRIfA      |  |    |
               |         |           +-------+
               |         |           |  |
               |         |           |  |
               |         +-----------+  |
               +------------------------+

                   (2) set ToxProxy as "own Relay" in the TRIfA app





```

<br>


```
         +---------------------------------------------+  
         |                                             | 
         | Self Hosted                                 |
         |                                             | 
         |+-------------------+                        | 
         ||                   |                        | (3) ToxProxy adds all TRIfA friends
         ||    ToxProxy       |                        |       without friendrequest
         ||                   |                        | 
         |+----------^--------+                        | 
         +-----------|---------------------------------+ 
                     |                                            
                     |                                            
                     +----------------------+                     
                                            | (2a) send all friends Pubkeys to ToxProxy
                                            | (2b) invite ToxProxy to all Tox Conferences TRIfA is in
                                            |  
                     Android Device         |                       
               +------------------------+   |    
               |                        |   |                       
               |                        |   |  
               |                        |   |  
               |         +-----------+  |   |  
               |         |TRIfA      |  |   | 
               |         |           +------+ 
               |         |           |  |
               |         |           +----------------~~~>> (1) send ToxProxy PubKey to all friends
               |         +-----------+  |                         (except Relays)
               +------------------------+


                                               (4) all participating Friends add ToxProxy
                                                     without friendrequest    




```

<br>


```
         +---------------------------------------------+  
         |                                             | 
         | Self Hosted                                 |
         |                                             | 
         |+-------------------+                        | 
         ||                   |                        |
         ||    ToxProxy       |                        |  
         ||                   |                        | 
         |+-------------------+                        | 
         +---------------------------------------------+ 
                                                                  
                                                                  
                                                                  
                                                            
                                                                    
                                               
                     Android Device                                 
               +------------------------+        
               |                        |                           
               |                        |      
               |                        |      
               |         +-----------+  |      
               |         |TRIfA      |  |     
               |         |           |  |     
               |         |           |  |
               |         |           <----------------~~~ (1) receive Friends ToxProxy Pubkey
               |         +-----------+  |                                               
               +------------------------+

                   (2) add Friends ToxProxy Pubkey and mark as Relay




```

<br>



## Push Notifications Workflows
<br>
<br>

```
         +---------------------------------------------+  +-------------------+
         |                                             |  |                   |
         | Self Hosted                   federated     |  |      Google       |
         |                            or self hosted   |  |                   |
         |+-------------------+     +----------------+ |  | +---------------+ |
         ||                   |     |                | |  | |               | |
         ||    ToxProxy       |     |  Push Gateway  | |  | | Push Provider | |
         ||                   |     |                | |  | |     (FCM)     | |
         |+-------------------+     +----------------+ |  | +--^-+----------+ |
         +---------------------------------------------+  +----|-|------------+
                                                               | |
                                                               | |
                 (1) Register for Push                         | |
            +--------------------------------------------------+ | (2) receive Notification Token
            |                                                    |
            |                                                    |
            |        Android Device                              |
            |  +------------------------+                        |
            |  |+----+                  |                        |
            |  ||    <-------------------------------------------+
            +---+    |                  |
               ||Tca |   +-----------+  |
               ||    |   |TRIfA      |  |
               ||    |   |           |  |
               ||    +---> (3) Token |  |
               ||    |   |           |  |
               |+----+   +-----------+  |
               +------------------------+

        Tca = TRIfA companion app





```

<br>


```
         +---------------------------------------------+  +-------------------+
         |                                             |  |                   |
         | Self Hosted                   federated     |  |      Google       |
         |                            or self hosted   |  |                   |
         |+-------------------+     +----------------+ |  | +---------------+ |
         ||                   |     |                | |  | |               | |
         ||    ToxProxy       |     |  Push Gateway  | |  | | Push Provider | |
         ||                   |     |                | |  | |     (FCM)     | |
         |+--------^----------+     +----------------+ |  | +---------------+ |
         +---------|-----------------------------------+  +-------------------+
                   |                                              
                   |                                              
                   +-------------------------+                    
                                             |                     
                                             |                     
                                             | (1) send Token to ToxProxy                    
                     Android Device          |                     
               +------------------------+    |  
               |+----+                  |    |                     
               ||    |                  |    |
               ||    |                  |    |
               ||Tca |   +-----------+  |    |
               ||    |   |TRIfA      |  |    |
               ||    |   |           +-------+
               ||    |   |           |  |
               ||    |   |           |  |
               |+----+   +-----------+  |
               +------------------------+

        Tca = TRIfA companion app





```

<br>


```
         +---------------------------------------------+  +-------------------+
         |                                             |  |                   |
         | Self Hosted                   federated     |  |      Google       |
         |                            or self hosted   |  |                   |
         |+-------------------+     +----------------+ |  | +---------------+ |
         ||                   |     |                | |  | |               | |
         ||    ToxProxy       +----->  Push Gateway  +------> Push Provider | |
         ||                   |     |                | |  | |     (FCM)     | |
         |+------^------------+     +----------------+ |  | +----+----------+ |
         +-------|-------------------------------------+  +------|------------+
                 |                                               |
                 |                                               |
  ~~~------------+                                               |
  (1) receive offline Message                                    | (2) receive FCM Push Message
                                                                 |     (empty Message,
                                                                 |      no real Data in FCM payload!)
                     Android Device                              |
               +------------------------+                        |
               |+----+                  |                        |
               ||    <-------------------------------------------+
               ||    |                  |
               ||Tca |   +-----------+  |
               ||    |   |TRIfA      |  |
               ||    |   |           |  |
               ||    +---> (3) wakeup|  |
               ||    |   |           |  |
               |+----+   +-----------+  |
               +------------------------+

        Tca = TRIfA companion app





```

<br>


```
         +---------------------------------------------+  +-------------------+
         |                                             |  |                   |
         | Self Hosted                   federated     |  |      Google       |
         |                            or self hosted   |  |                   |
         |+-------------------+     +----------------+ |  | +---------------+ |
         ||                   |     |                | |  | |               | |
         ||    ToxProxy       |     |  Push Gateway  | |  | | Push Provider | |
         ||                   |     |                | |  | |     (FCM)     | |
         |+--------+----------+     +----------------+ |  | +---------------+ |
         +---------|-----------------------------------+  +-------------------+
                   |                                              
                   |                                              
                   +-------------------------+                    
                                             |                     
                                             |                     
                                             | (1) sync missing Messages
                     Android Device          |                     
               +------------------------+    |  
               |+----+                  |    |                     
               ||    |                  |    |
               ||    |                  |    |
               ||Tca |   +-----------+  |    |
               ||    |   |TRIfA      |  |    |
               ||    |   |           <-------+
               ||    |   |           |  |
               ||    |   |           |  |
               |+----+   +-----------+  |
               +------------------------+

        Tca = TRIfA companion app





```
<br>


|component       | Data accessed (private IDs)
|:---------------|:--------------------
|ToxProxy        | FCM Device Token for 1 device using this ToxProxy
|ToxProxy        | all Messages in plain text for 1 device using this ToxProxy
|ToxProxy        | and some more things (like Timestamps and metadata)

<br>

|component       | Data accessed (private IDs)
|:---------------|:--------------------
|Push Gateway    | FCM Device Token for all devices using this Gateway
|Push Gateway    | FCM Server Key for Google Developer Account using FCM with this Gateway

<br>

|component       | Data accessed (private IDs)
|:---------------|:--------------------
|Tca             | FCM Device Token for 1 device where it is installed
|Tca             | Timestamp when an event happens for all devices using this System

<br>

|component       | Data accessed (private IDs)
|:---------------|:--------------------
|TRIfA           | FCM Device Token for 1 device where it is installed
|TRIfA           | all your private Tox Data

<br>

|component       | Data accessed (private IDs)
|:---------------|:--------------------
|Google          | FCM Device Token for all devices using this System
|Google          | FCM Server Keys for all Google Developer Accounts using this System
|Google          | Timestamp when an event happens for all devices using this System

<br>


## New packets


#### CONTROL_PROXY_MESSAGE_TYPE_FRIEND_PUBKEY_FOR_PROXY


|what       |Length               | Contents
|:----------|:--------------------|:-------------
|pkt id     | 1                   | `uint8_t` 175
|pubkey     | TOX_PUBLIC_KEY_SIZE | `*uint8_t` friend pubkey to send to ToxProxy


#### CONTROL_PROXY_MESSAGE_TYPE_PROXY_PUBKEY_FOR_FRIEND

|what       |Length               | Contents
|:----------|:--------------------|:-------------
|pkt id     | 1                   | `uint8_t` 176
|pubkey     | TOX_PUBLIC_KEY_SIZE | `*uint8_t` ToxProxy pubkey to send to a friend


#### CONTROL_PROXY_MESSAGE_TYPE_ALL_MESSAGES_SENT

|what       |Length               | Contents
|:----------|:--------------------|:-------------
|pkt id     | 1                   | `uint8_t` 177


#### CONTROL_PROXY_MESSAGE_TYPE_PROXY_KILLSWITCH

|what       |Length               | Contents
|:----------|:--------------------|:-------------
|pkt id     | 1                   | `uint8_t` 178


#### CONTROL_PROXY_MESSAGE_TYPE_NOTIFICATION_TOKEN

|what       |Length               | Contents
|:----------|:--------------------|:-------------
|pkt id     | 1                   | `uint8_t` 179
|token      | [11, 300]           | `*uint8_t` notification token





## New Filetransfer types

`TOX_MESSAGEV2_MAX_TEXT_LENGTH = 4096`

`TOX_MESSAGEV2_MAX_NON_SYNC_HEADER_SIZE + TOX_MESSAGEV2_MAX_TEXT_LENGTH = 4167`

`TOX_MAX_FILETRANSFER_SIZE_MSGV2 = 4241`


#### TOX_FILE_KIND_MESSAGEV2_SEND

|what           |Length      | Contents
|:--------------|:-----------|:-------------
|msg id         |32          | `*uint8_t` hash (what hash function?) to uniquely identify the message
|create ts      |4           | `uint32_t` unixtimestamp in UTC of local clock (NTP time if poosible - client?) when the user typed the message
|create ts ms   |2           | `uint16_t` unixtimestamp ms part
|msg txt        |[0, 4096]   | `*uint8_t` Message as a UTF8 byte string



#### TOX_FILE_KIND_MESSAGEV2_ANSWER

|what        |Length   | Contents
|:-----------|:--------|:------------------
|msg id      |32       | `*uint8_t` hash (what hash function?) to uniquely identify the message the answer is for
|ts          |4        | `uint32_t` unixtimestamp in UTC of local clock (NTP time if poosible -> client?) when the message was received
|ts ms       |2        | `uint16_t` unixtimestamp ms part


#### TOX_FILE_KIND_MESSAGEV2_ALTER


|what        |Length      | Contents
|:-----------|:-----------|:--------------
|msg id      |32          | `*uint8_t` hash (what hash function?) to uniquely identify the message
|alter ts    |4           | `uint32_t` unixtimestamp in UTC of local clock (NTP time if poosible -> client?) when the user typed the message
|alter ts ms |2           | `uint16_t` unixtimestamp ms part
|alter type  |1           | `uint8_t` values: 0 -> delete message, 1 -> change text
|alter id    |32          | `*uint8_t` hash to identify the message to alter/delete
|msg txt     |[0, 4096]   | `*uint8_t` Altered Message as a UTF8 byte string or 0 length on delete


#### TOX_FILE_KIND_MESSAGEV2_SYNC

|what           |Length       | Contents
|:--------------|:------------|:-----------------
|msg id         |32           | `*uint8_t` hash (what hash function?) to uniquely identify the message
|create ts      |4            | `uint32_t` unixtimestamp in UTC of local clock (NTP time if poosible -> client?) when the user typed the message
|create ts ms   |2            | `uint16_t` unixtimestamp ms part
|orig sender    |32           | `*uint8_t` pubkey of the original sender
|msgV2 type     |4            | `uint32_t` what msgV2 type is this sync message
|msgv2 data     |[0, 4167]    | `*uint8_t` msgV2 raw data including header as raw bytes






